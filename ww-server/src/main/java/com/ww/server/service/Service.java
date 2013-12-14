package com.ww.server.service;

/**
 *
 * @author sandy
 */
public abstract class Service {

    public static Factory getRegistry() {
        return Instance.get();
    }

    protected final Factory registry = getRegistry();
}
