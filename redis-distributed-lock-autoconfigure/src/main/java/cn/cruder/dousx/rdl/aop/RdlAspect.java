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
    private static final String KEY_PARTITION = ":";
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
        boolean lockFlag = false;
        try {
            long timeout = distributedLock.lockTime();
            lockKey = lockKey(point, distributedLock);
            lockFlag = distributedLockService.lock(lockKey, lockValue, timeout);
            if (lockFlag) {
                log.debug("[加锁成功]lockKey:{} lockValue:{} timeout:{}s ", lockKey, lockValue, timeout);
                return point.proceed();
            } else {
                log.debug("[加锁失败]lockKey:{} lockValue:{} timeout:{}s ", lockKey, lockValue, timeout);
                Class<? extends LockFailPolicy> aClass = distributedLock.lockFailPolicy();
                LockFailPolicy lockFailPolicy = aClass.newInstance();
                return lockFailPolicy.policy(point.getArgs(), lockKey);
            }
        } catch (Throwable e) {
            // 抛出,交业务处理
            throw e;
        } finally {
            if (lockFlag && StringUtils.isNotBlank(lockKey)) {
                boolean unlock = distributedLockService.unlock(lockKey, lockValue);
                if (!unlock) {
                    log.warn("[解锁失败] unLockKey:{} unLockValue:{}", lockKey, lockValue);
                    Class<? extends UnLockFailPolicy> aClass = distributedLock.unLockFailPolicy();
                    UnLockFailPolicy unLockFailPolicy = aClass.newInstance();
                    unLockFailPolicy.policy(point.getArgs(), lockKey, lockValue);
                }
            }
        }
    }

    private String lockKey(ProceedingJoinPoint point, DistributedLock enableRdl) {
        String defaultLockKey = defaultLockKey(point);
        if (StringUtils.isBlank(enableRdl.lockKey())) {
            return defaultLockKey;
        }
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
            return defaultLockKey + KEY_PARTITION + expression.getValue(context);
        } catch (EvaluationException e) {
            throw new IllegalArgumentException(expression.getExpressionString(), e.getCause());
        }

    }

    private static String defaultLockKey(ProceedingJoinPoint point) {
        String declaringTypeName = point.getSignature().getDeclaringTypeName();
        String sigName = point.getSignature().getName();
        return declaringTypeName + KEY_PARTITION + sigName;
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
