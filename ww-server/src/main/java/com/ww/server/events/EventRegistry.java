/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ww.server.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author sandy
 */
public class EventRegistry {

    private static Map<Class<?>, AbstractEventImpl> registry = new HashMap<Class<?>, AbstractEventImpl>();
    private static Lock lock = new ReentrantLock();

    /**
     * Get {@link Event} object for passed handler class.
     * Always return the same Object for the same parameter.
     * @param <T> type of handler class objects
     * @param handlerClass handler class
     * @return event for passed handler class
     */
    public static <T extends Handler> Event<T> getEvent(Class<T> handlerClass) {
        lock.lock();
        try {
            AbstractEventImpl<T> event = registry.get(handlerClass);
            if (event == null) {
                Method method = getHandlerMethod(handlerClass);
                event = new AbstractEventImpl<T>(method);
                registry.put(handlerClass, event);
            }
            return event;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get instance of Handler class that will notify all observers about Event
     * @param <T> type of returned value
     * @param handlerClass class of handlers that will be notified by returned dispatcher
     * @return event dispatcher
     */
    public static <T extends Handler> T getDispatcher(Class<T> handlerClass) {

        final AbstractEvent<T> event = (AbstractEvent<T>) getEvent(handlerClass);

        return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[] { handlerClass }, new InvocationHandler() {

            @Override
            public Object invoke(Object o, Method m, Object[] os) throws Throwable {
                event.notifyObservers(os == null ? new Object[] {} : os);
                return null;
            }
        });
    }

    private static Method getHandlerMethod(Class handlerClass) {
        if (!handlerClass.isInterface()) {
            throw new RuntimeException("Cannot initialize event for non-interface handler class");
        }
        Method[] methods = handlerClass.getMethods();
        if (methods.length != 1) {
            throw new RuntimeException("Handler interface must contain one method");
        }
        return methods[0];
    }

    private static class AbstractEventImpl<T extends Handler> extends AbstractEvent<T> {

        private final Method method;

        public AbstractEventImpl(Method method) {
            this.method = method;
        }

        @Override
        protected Observer getObserver(final T handler) {
            return new EventHandlerImpl() {

                @Override
                protected void runHandler(Object... params) {
                    try {
                        method.invoke(handler, params);
                    } catch (IllegalAccessException illegalAccessException) {
                    } catch (IllegalArgumentException illegalArgumentException) {
                    } catch (InvocationTargetException ex) {
                        Throwable targetException = ex.getTargetException();
                        if (targetException instanceof Error) {
                            throw (Error) targetException;
                        } else if (targetException instanceof RuntimeException) {
                            throw (RuntimeException) targetException;
                        } else {
                            throw new RuntimeException(targetException);
                        }
                    }
                }
            };
        }
    }
}
