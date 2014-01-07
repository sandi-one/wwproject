package com.ww.server.cache;

import com.google.common.cache.Cache;
import com.ww.server.service.session.Session;

/**
 *
 * @author sandy
 */
public class SessionCache extends AbstractCache {

    private volatile Cache<String, Object> cache;

    public SessionCache(int size) {
        cache = initCache(size);
    }

    @Override
    protected void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    protected Session get(String key) {
        return (Session) cache.getIfPresent(key);
    }

    @Override
    protected void delete(String key) {
        cache.invalidate(key);
    }

    public void closeConnection(String key) {
        get(key).getSession().close();
        deleteObject(key);
    }

    @Override
    public long getExpireTime() {
        return (long) 7 * 24 * 60 * 60;
    }
}
