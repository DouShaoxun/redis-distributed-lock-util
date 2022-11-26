package cn.cruder.dousx.rdl.policy.impl;

import cn.cruder.dousx.rdl.policy.LockFailPolicy;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dousx
 */
@Slf4j
public class DefaultLockFailPolicy implements LockFailPolicy {
    @Override
    public Object policy(Object[] args, String lockKey) {
        log.warn("[DefaultLockFailPolicy] lockKey:{}", lockKey);
        return null;
    }
}
