package cn.cruder.dousx.rdl.service.impl;

import cn.cruder.dousx.rdl.service.DistributedLockService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 依赖{@link RedisTemplate}
 *
 * @author dousx
 */
@Service(value = RedisDistributedLockServiceImpl.BEAN_NAME)
@ConditionalOnClass(RedisTemplate.class)
@AllArgsConstructor
public class RedisDistributedLockServiceImpl implements DistributedLockService {
    public static final String BEAN_NAME = "rdl_RedisDistributedLockServiceImpl";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean lock(String lockKey, String lockValue, long timeout) {
        if (StringUtils.isBlank(lockKey) || StringUtils.isBlank(lockValue)) {
            throw new IllegalArgumentException();
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException(String.format("timeout:%s", timeout));
        }
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, TimeUnit.SECONDS));
    }

    @Override
    public boolean unlock(String lockKey, String lockValue) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unlock.lua")));
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        if (Objects.isNull(result)) {
            return false;
        }
        return result != 0;
    }
}
