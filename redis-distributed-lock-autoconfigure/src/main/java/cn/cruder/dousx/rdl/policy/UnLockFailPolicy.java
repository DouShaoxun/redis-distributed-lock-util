package cn.cruder.dousx.rdl.policy;

/**
 * @author dousx
 */
@FunctionalInterface

public interface UnLockFailPolicy {
    /**
     * 解锁失败策略
     *
     * @param args      参数
     * @param lockKey   key
     * @param lockValue v
     * @return Object
     */
    Object policy(Object[] args, String lockKey, String lockValue);

}
