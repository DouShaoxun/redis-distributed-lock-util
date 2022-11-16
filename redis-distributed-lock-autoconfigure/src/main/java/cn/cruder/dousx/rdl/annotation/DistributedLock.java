package cn.cruder.dousx.rdl.annotation;

import cn.cruder.dousx.rdl.policy.LockFailPolicy;
import cn.cruder.dousx.rdl.policy.UnLockFailPolicy;
import cn.cruder.dousx.rdl.policy.impl.DefaultLockFailPolicy;
import cn.cruder.dousx.rdl.policy.impl.DefaultUnLockFailPolicy;

import java.lang.annotation.*;

/**
 * todo 注解属性待优化  el表达式和字符串同时支持
 *
 * @author dousx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedLock {
    /**
     * 每个接口的锁时间都不一样
     * <br/>
     * 单位:秒
     *
     * @return 锁时间
     */
    long lockTime() default 10L;

    /**
     * "" 则取  全限定类名+":"+方法名称
     *
     * @return 锁key
     */
    String lockKey() default "";

    /**
     * 加锁失败处理方案
     *
     * @return {@link LockFailPolicy}
     */
    Class<? extends LockFailPolicy> lockFailPolicy() default DefaultLockFailPolicy.class;

    /**
     * 解锁失败处理方案
     *
     * @return {@link LockFailPolicy}
     */
    Class<? extends UnLockFailPolicy> unLockFailPolicy() default DefaultUnLockFailPolicy.class;
}
