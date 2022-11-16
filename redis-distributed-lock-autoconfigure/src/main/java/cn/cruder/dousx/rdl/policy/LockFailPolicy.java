package cn.cruder.dousx.rdl.policy;

/**
 * @author dousx
 */
public interface LockFailPolicy {
    /**
     * 加锁失败策略
     * todo 参数待优化
     * @param args
     * @return
     */
    Object policy(Object[] args);
}
