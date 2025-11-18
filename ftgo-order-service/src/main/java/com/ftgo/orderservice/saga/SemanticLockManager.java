package com.ftgo.orderservice.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * Manages semantic locks for saga operations using Redis for distributed locking.
 * Implements semantic locking pattern to handle lack of isolation in distributed transactions.
 * 
 * Uses Redis SET with NX (set if not exists) and EX (expiration) for atomic lock acquisition.
 * Uses Lua scripts for atomic lock release to ensure we only release locks we own.
 */
@Component
@Slf4j
public class SemanticLockManager {
    private static final String LOCK_PREFIX = "saga:lock:";
    private static final Duration DEFAULT_LOCK_TIMEOUT = Duration.ofMinutes(30); // Saga timeout
    private static final String RELEASE_LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('del', KEYS[1]) " +
        "else " +
        "  return 0 " +
        "end";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> releaseLockScript;

    public SemanticLockManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.releaseLockScript = new DefaultRedisScript<>();
        this.releaseLockScript.setScriptText(RELEASE_LOCK_SCRIPT);
        this.releaseLockScript.setResultType(Long.class);
    }

    /**
     * Acquires a semantic lock for a resource.
     * Prevents other transactions from modifying the resource until the saga completes.
     * 
     * @param resourceType Type of resource (e.g., "Order", "Customer")
     * @param resourceId ID of the resource
     * @param sagaInstanceId ID of the saga instance acquiring the lock
     * @return true if lock was acquired, false if already held by another saga
     */
    public boolean acquireLock(String resourceType, String resourceId, String sagaInstanceId) {
        String lockKey = LOCK_PREFIX + resourceType + ":" + resourceId;
        
        // Use SET with NX (set if not exists) and EX (expiration) for atomic lock acquisition
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
            lockKey, 
            sagaInstanceId, 
            DEFAULT_LOCK_TIMEOUT
        );
        
        if (Boolean.TRUE.equals(acquired)) {
            log.info("Acquired semantic lock: {} for saga: {}", lockKey, sagaInstanceId);
            return true;
        } else {
            // Check if lock is held by the same saga (re-entrant lock)
            String currentLockHolder = redisTemplate.opsForValue().get(lockKey);
            if (sagaInstanceId.equals(currentLockHolder)) {
                log.debug("Lock already held by same saga: {} for resource: {}", sagaInstanceId, lockKey);
                // Refresh expiration
                redisTemplate.expire(lockKey, DEFAULT_LOCK_TIMEOUT);
                return true;
            } else {
                log.warn("Lock already held by another saga: {} for resource: {}", 
                        currentLockHolder, lockKey);
                return false;
            }
        }
    }

    /**
     * Releases a semantic lock for a resource.
     * Uses Lua script to ensure atomicity - only releases if we own the lock.
     * 
     * @param resourceType Type of resource
     * @param resourceId ID of the resource
     * @param sagaInstanceId ID of the saga instance releasing the lock
     */
    public void releaseLock(String resourceType, String resourceId, String sagaInstanceId) {
        String lockKey = LOCK_PREFIX + resourceType + ":" + resourceId;
        
        // Use Lua script to atomically check and delete the lock
        Long result = redisTemplate.execute(
            releaseLockScript,
            Collections.singletonList(lockKey),
            sagaInstanceId
        );
        
        if (result != null && result > 0) {
            log.info("Released semantic lock: {} for saga: {}", lockKey, sagaInstanceId);
        } else {
            log.warn("Attempted to release lock not held by saga: {} for resource: {}", 
                    sagaInstanceId, lockKey);
        }
    }

    /**
     * Releases all locks for a saga instance.
     * Scans for all locks with the given saga instance ID and releases them.
     * 
     * @param sagaInstanceId ID of the saga instance
     */
    public void releaseAllLocks(String sagaInstanceId) {
        String pattern = LOCK_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys != null) {
            int releasedCount = 0;
            for (String key : keys) {
                String currentLockHolder = redisTemplate.opsForValue().get(key);
                if (sagaInstanceId.equals(currentLockHolder)) {
                    Long result = redisTemplate.execute(
                        releaseLockScript,
                        Collections.singletonList(key),
                        sagaInstanceId
                    );
                    if (result != null && result > 0) {
                        releasedCount++;
                        log.info("Released semantic lock: {} for saga: {}", key, sagaInstanceId);
                    }
                }
            }
            log.info("Released {} locks for saga: {}", releasedCount, sagaInstanceId);
        }
    }

    /**
     * Extends the expiration time of a lock.
     * Useful for long-running sagas.
     * 
     * @param resourceType Type of resource
     * @param resourceId ID of the resource
     * @param sagaInstanceId ID of the saga instance
     * @return true if lock was extended, false if lock not found or not owned by saga
     */
    public boolean extendLock(String resourceType, String resourceId, String sagaInstanceId) {
        String lockKey = LOCK_PREFIX + resourceType + ":" + resourceId;
        String currentLockHolder = redisTemplate.opsForValue().get(lockKey);
        
        if (sagaInstanceId.equals(currentLockHolder)) {
            Boolean extended = redisTemplate.expire(lockKey, DEFAULT_LOCK_TIMEOUT);
            if (Boolean.TRUE.equals(extended)) {
                log.debug("Extended semantic lock: {} for saga: {}", lockKey, sagaInstanceId);
                return true;
            }
        }
        return false;
    }
}
