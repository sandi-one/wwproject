package com.ww.server.cache;

import com.google.common.cache.Cache;
import com.ww.server.service.authentication.Token;

/**
 *
 * @author sandy
 */
public class TokenCache extends AbstractCache {

    private volatile Cache<String, Object> cache;

    public TokenCache(int size) {
        cache = initCache(size);
    }

    @Override
    public long getExpireTime() {
        return (long) 7 * 24 * 60 * 60;
    }

    @Override
    protected void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    protected Token get(String key) {
        return (Token) cache.getIfPresent(key);
    }

    @Override
    protected void delete(String key) {
        cache.invalidate(key);
    }
}
