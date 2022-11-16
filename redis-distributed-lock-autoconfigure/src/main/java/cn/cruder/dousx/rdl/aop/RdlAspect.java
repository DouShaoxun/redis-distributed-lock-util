package cn.cruder.dousx.rdl.aop;

import cn.cruder.dousx.rdl.annotation.DistributedLock;
import cn.cruder.dousx.rdl.policy.LockFailPolicy;
import cn.cruder.dousx.rdl.policy.UnLockFailPolicy;
import cn.cruder.dousx.rdl.service.DistributedLockService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * @author dousx
 */
@Slf4j
@Aspect
@AllArgsConstructor
public class RdlAspect {
    public static final String BEAN_NAME = "rdl_LockAspect";
    private final DistributedLockService distributedLockService;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(cn.cruder.dousx.rdl.annotation.DistributedLock)")
    public void rdlAspect() {
    }


    /**
     * 记录日志
     *
     * @param point 切入点
     * @return result
     */
    @Around("rdlAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        DistributedLock distributedLock = pointDistributedLock(point);
        if (Objects.isNull(distributedLock)) {
            return point.proceed();
        }

        String lockValue = UUID.randomUUID().toString();
        String lockKey = null;
        try {
            long timeout = distributedLock.lockTime();
            lockKey = lockKey(point, distributedLock);
            boolean success = distributedLockService.lock(lockKey, lockValue, timeout);
            if (log.isDebugEnabled()) {
                log.debug("lockKey:{} lockValue:{} timeout:{}s lock:{}", lockKey, lockValue, timeout, success);
            }
            if (success) {
                return point.proceed();
            } else {
                log.warn("加锁失败 lockKey:{} lockValue:{} timeout:{} ", lockKey, lockValue, timeout);
                Class<? extends LockFailPolicy> aClass = distributedLock.lockFailPolicy();
                LockFailPolicy lockFailPolicy = aClass.newInstance();
                return lockFailPolicy.policy(point.getArgs());
            }
        } catch (Throwable e) {
            // 抛出,交业务处理
            throw e;
        } finally {
            if (StringUtils.isNotBlank(lockKey)) {
                boolean unlock = distributedLockService.unlock(lockKey, lockValue);
                if (log.isDebugEnabled()) {
                    log.debug("unLockKey:{} unLockValue:{} unlock:{}", lockKey, lockValue, unlock);
                }
                if (!unlock) {
                    Class<? extends UnLockFailPolicy> aClass = distributedLock.unLockFailPolicy();
                    UnLockFailPolicy unLockFailPolicy = aClass.newInstance();
                    unLockFailPolicy.policy(point.getArgs());
                    log.warn("解锁失败");
                }
            }
        }
    }

    private String lockKey(ProceedingJoinPoint point, DistributedLock enableRdl) {
        if (StringUtils.isBlank(enableRdl.lockKey())) {
            return defaultLockKey(point);
        } else {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
            Object[] args = point.getArgs();
            if (paramNames == null || args == null
                    || paramNames.length == 0
                    || args.length == 0
                    || args.length != paramNames.length) {
                throw new IllegalArgumentException();
            }
            Expression expression = parser.parseExpression(enableRdl.lockKey());
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            try {
                return String.valueOf(expression.getValue(context));
            } catch (EvaluationException e) {
                throw new IllegalArgumentException(expression.getExpressionString(), e.getCause());
            }
        }
    }

    private static String defaultLockKey(ProceedingJoinPoint point) {
        String declaringTypeName = point.getSignature().getDeclaringTypeName();
        String sigName = point.getSignature().getName();
        return declaringTypeName + ":" + sigName;
    }

    /**
     * 获取方法上aop注解
     *
     * @param joinPoint 切点
     * @return {@link DistributedLock}
     */
    private DistributedLock pointDistributedLock(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(DistributedLock.class);
    }


}
