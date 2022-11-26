package cn.cruder.dousx.rdl.policy;

/**
 * @author dousx
 */
@FunctionalInterface
public interface LockFailPolicy {
    /**
     * 加锁失败策略
     *
     * @param args      参数
     * @param lockKey   key
     * @return Object
     */
    Object policy(Object[] args, String lockKey);
}
