package com.ww.server.service;

/**
 *
 * @author sandy
 */
public class Instance {

    private static ThreadLocal<WWFactory> factory = new ThreadLocal<WWFactory>() {
        @Override
        protected synchronized WWFactory initialValue() {
            return new WWService();
        }
    };

    public static WWFactory get() {
        return factory.get();
    }

    public static void remove() {
        factory.remove();
    }
}
