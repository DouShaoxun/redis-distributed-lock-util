package cn.cruder.dousx.rdl.policy.impl;

import cn.cruder.dousx.rdl.policy.UnLockFailPolicy;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dousx
 */
@Slf4j
public class DefaultUnLockFailPolicy implements UnLockFailPolicy {
    @Override
    public Object policy(Object[] args, String lockKey, String lockValue) {
        log.warn("[DefaultUnLockFailPolicy] unLockKey:{} unLockValue:{}", lockKey, lockValue);
        return null;
    }


}
