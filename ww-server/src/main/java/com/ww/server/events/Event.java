package com.ww.server.events;

/**
 *
 * @author sandy
 */
public interface Event<T extends Handler> {

    void addHandler(T handler);
    <C extends Handler> void addDeferredHandler(T handler, Class<C> dependsOnHandlerClass);
    void removeHandler(T handler);
    void clear();
    void resetDeferredEvents();
}
