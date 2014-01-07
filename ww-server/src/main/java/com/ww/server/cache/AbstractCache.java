package com.ww.server.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sandy
 */
public abstract class AbstractCache {

    private final Lock read;

    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        read  = lock.readLock();
    }

    protected abstract void put(String key, Object value);

    protected abstract <T> T get(String key);

    protected abstract void delete(String key);

    public abstract long getExpireTime();

    protected Cache<String, Object> initCache(int size) {
        final long expireTime = getExpireTime();
        return CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterAccess(expireTime, TimeUnit.SECONDS)
                .build();
    }

    public void deleteObject(String objectId) {
        read.lock();
        try {
            delete(objectId);
        } finally {
            read.unlock();
        }
    }

    public <T> T getObject(String objectId) {
        read.lock();
        try {
            return (T) get(objectId);
        } finally {
            read.unlock();
        }
    }

    public void putObject(String key, Object value) {
        read.lock();
        try {
            put(key, value);
        } finally {
            read.unlock();
        }
    }
}
