package cn.cruder.dousx.rdl.policy;

public interface UnLockFailPolicy {
    /**
     * 加锁失败策略
     * todo 参数待优化
     * @param args
     * @return
     */
    Object policy(Object[] args);

}
