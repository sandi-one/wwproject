package com.ww.server.service.authentication;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.eclipse.jetty.websocket.WebSocket;

/**
 *
 * @author sandy
 */
public class TokenCache {

    private final Lock read;

    private volatile Cache<String, Object> cache;
    private volatile Cache<String, Object> sessionCache;

    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        read  = lock.readLock();
    }

    public TokenCache(int size) {
        cache = initCache(size);
        sessionCache = initCache(size);
    }

    public void putToken(Token token, WebSocket.Connection connection) {
        read.lock();
        try {
            cache.put(token.getFullTokenId(), token.clone());
            sessionCache.put(token.getFullTokenId(), connection);
        } finally {
            read.unlock();
        }
    }

    public Token getToken(String tokenId) {
        read.lock();
        try {
            Token token = (Token) cache.getIfPresent(TokenManager.getFullTokenId(tokenId));
            return (token == null) ? null : token.clone();
        } finally {
            read.unlock();
        }
    }

    public WebSocket.Connection getSession(String tokenId) {
        read.lock();
        try {
            return (WebSocket.Connection) sessionCache.getIfPresent(TokenManager.getFullTokenId(tokenId));
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

    public void deleteSession(String tokenId) {
        read.lock();
        try {
            sessionCache.invalidate(TokenManager.getFullTokenId(tokenId));
        } finally {
            read.unlock();
        }
    }

    private void delete(String key) {
        cache.invalidate(key);
        sessionCache.invalidate(key);
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
