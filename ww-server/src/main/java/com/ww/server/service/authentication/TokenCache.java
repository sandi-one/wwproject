package com.ww.server.service.authentication;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sandy
 */
public class TokenCache {

    private final Lock read;

    private volatile Cache<String, Object> cache;
    //private volatile long maxSize;
    //private final Cache<String, String> index;

    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        read  = lock.readLock();
    }

    public TokenCache(int size) {
        //index = CacheBuilder.newBuilder().build();

        cache = initCache(size);
    }

    public void putToken(Token token) {
        read.lock();
        try {
            put(token.getFullTokenId(), token.clone());
        } finally {
            read.unlock();
        }
    }

    public Token getToken(String tokenId) {
        read.lock();
        try {
            Token token = get(TokenManager.getFullTokenId(tokenId));
            return (token == null) ? null : token.clone();
        } finally {
            read.unlock();
        }
    }

    public void deleteToken(String tokenId) {
        read.lock();
        try {
            delete(TokenManager.getFullTokenId(tokenId));
        } finally {
            read.unlock();
        }
    }

    protected void put(String key, Object value) {
        cache.put(key, value);
    }

    protected <T> T get(String key) {
        return (T) cache.getIfPresent(key);
    }

    protected void delete(String key) {
        cache.invalidate(key);
    }

    public static long getExpireTime() {
        return (long) 7 * 24 * 60 * 60;
    }

    private Cache<String, Object> initCache(int size) {

        //maxSize = size;
        final long expireTime = getExpireTime();
        return CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterAccess(expireTime, TimeUnit.SECONDS)
                .build();
    }
}
