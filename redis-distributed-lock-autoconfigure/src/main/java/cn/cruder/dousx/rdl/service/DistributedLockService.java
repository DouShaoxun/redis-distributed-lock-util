package cn.cruder.dousx.rdl.service;

import java.util.concurrent.TimeUnit;

/**
 * @author dousx
 */
public interface DistributedLockService {
    /**
     * 加锁
     *
     * @param lockKey   key
     * @param lockValue value
     * @param timeout   超时时间 单位{@link TimeUnit#SECONDS}
     * @return <li/> true 成功 <li/> false 失败
     */
    boolean lock(String lockKey, String lockValue, long timeout);

    /**
     * 解锁
     *
     * @param lockKey   key
     * @param lockValue value
     * @return <li/> true 成功 <li/> false 失败
     */
    boolean unlock(String lockKey, String lockValue);
}
