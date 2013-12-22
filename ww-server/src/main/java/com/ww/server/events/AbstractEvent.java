package com.ww.server.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author sandy
 */
public abstract class AbstractEvent<T extends Handler> implements Event<T> {

    //classes
    private static class EventObservable extends Observable {

        @Override
        public void notifyObservers(Object arg0) {
            setChanged();
            super.notifyObservers(arg0);
        }

        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

    public static abstract class EventHandlerImpl implements Observer {

        @Override
        public void update(Observable arg0, Object arg1) {
            if (!(arg1 instanceof Object[])) {
                throw new RuntimeException();
            }
            Object[] args = (Object[]) arg1;
            runHandler(args);
        }

        protected abstract void runHandler(Object... params);
    }

    //fields
    protected final Observable observable = new EventObservable();
    private final Map<T, Observer> observers = new HashMap<T, Observer>();
    private final Map<AbstractEvent, Observable> deferredEvents = new HashMap<AbstractEvent, Observable>();
    private final Map<Observable, ThreadLocal<List<Object[]>>> deferredDependentArgs = new HashMap<Observable, ThreadLocal<List<Object[]>>>();

    //abstract methods
    protected abstract Observer getObserver(T handler);

    private void addHandler(Observable observable, T handler) {
        Observer observer = getObserver(handler);
        observable.addObserver(observer);
        observers.put(handler, observer);
    }

    @Override
    public void addHandler(T handler) {
        if ((handler == this) || Proxy.isProxyClass(handler.getClass())) {
            throw new AddSelfException();
        }
        addHandler(observable, handler);
    }

    @Override
    public <C extends Handler> void addDeferredHandler(T handler, Class<C> dependsOnHandlerClass) {
        final Observable deferredEventObservable = new EventObservable();
        deferredDependentArgs.put(deferredEventObservable, new ThreadLocal<List<Object[]>>() {

            @Override
            protected List<Object[]> initialValue() {
                return new ArrayList<Object[]>();
            }
        });
        addHandler(deferredEventObservable, handler);

        AbstractEvent<C> dependantEvent = (AbstractEvent<C>) EventRegistry.getEvent(dependsOnHandlerClass);
        C deferredHandler = (C) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[] { dependsOnHandlerClass }, new InvocationHandler() {

            private final Object delegate = new Object();

            @Override
            public Object invoke(Object proxy, Method method, Object[] o) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(delegate, o);
                }
                List<Object[]> deferredArgsList = deferredDependentArgs.get(deferredEventObservable).get();
                for (Object[] args : deferredArgsList) {
                    deferredEventObservable.notifyObservers(args);
                }
                deferredArgsList.clear();
                return null;
            }
        });
        dependantEvent.addHandler(dependantEvent.observable, deferredHandler);
        dependantEvent.deferredEvents.put(this, deferredEventObservable);
    }

    @Override
    public void removeHandler(T handler) {
        //TODO not sure that it'll work with deferred handlers, but we don't use this function anyway
        Observer observer = observers.get(handler);
        if (observer != null) {
            observable.deleteObserver(observer);
            List<Observable> forDelete = new ArrayList<Observable>();
            for (Map.Entry<Observable, ThreadLocal<List<Object[]>>> entry : deferredDependentArgs.entrySet()) {
                Observable deferredObservable = entry.getKey();
                List<Object[]> deferredArgs = entry.getValue().get();
                deferredObservable.deleteObserver(observer);
                if (deferredObservable.countObservers() == 0) {
                    deferredArgs.clear();
                    forDelete.add(deferredObservable);
                }
            }
            for (Observable deferredObservable : forDelete) {
                deferredDependentArgs.remove(deferredObservable);
            }
        }
    }

    @Override
    public void clear() {
        observable.deleteObservers();
        for (Map.Entry<Observable, ThreadLocal<List<Object[]>>> entry : deferredDependentArgs.entrySet()) {
            Observable deferredObservable = entry.getKey();
            List<Object[]> deferredArgs = entry.getValue().get();
            deferredObservable.deleteObservers();
            if (deferredObservable.countObservers() == 0) {
                deferredArgs.clear();
            }
        }
        deferredDependentArgs.clear();
    }

    @Override
    public void resetDeferredEvents() {
        for (Map.Entry<Observable, ThreadLocal<List<Object[]>>> entry : deferredDependentArgs.entrySet()) {
            List<Object[]> deferredArgs = entry.getValue().get();
            deferredArgs.clear();
        }
        for (Map.Entry<AbstractEvent, Observable> entry : deferredEvents.entrySet()) {
            AbstractEvent event = entry.getKey();
            Observable ob = entry.getValue();
            Map<Observable, ThreadLocal<List<Object[]>>> argsMap
                    = (Map<Observable, ThreadLocal<List<Object[]>>>) event.deferredDependentArgs;
            argsMap.get(ob).get().clear();
        }
    }

    protected void notifyObservers(Object... params) {
        observable.notifyObservers(params);
        for (Map.Entry<Observable, ThreadLocal<List<Object[]>>> entry : deferredDependentArgs.entrySet()) {
            Observable deferredObservable = entry.getKey();
            List<Object[]> deferredArgs = entry.getValue().get();
            if (deferredObservable.countObservers() > 0) {
                deferredArgs.add(params);
            }
        }
    }
}
