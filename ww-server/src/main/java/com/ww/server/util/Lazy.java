package com.ww.server.util;

/**
 *
 * @author sandy
 */
public abstract class Lazy<T> {

    protected final Callback<T> callback;

    public static <C> Lazy<C> async(final Class<C> c) {
        return new AsyncLazy(new Callback<C>() {

            @Override
            public C init() throws Exception {
                return c.newInstance();
            }
        });
    }

    public static <C> Lazy<C> sync(final Class<C> c) {
        return new SyncLazy(new Callback<C>() {

            @Override
            public C init() throws Exception {
                return c.newInstance();
            }
        });
    }

    public static <C> Lazy<C> async(Callback<C> callback) {
        return new AsyncLazy(callback);
    }

    public static <C> Lazy<C> sync(Callback<C> callback) {
        return new SyncLazy(callback);
    }

    private Lazy(Callback<T> callback) {
        this.callback = callback;
    }

    public interface Callback<C> {

        public C init() throws Exception;
    }

    public abstract T get();

    public abstract void reset(T value);

    public abstract void reset();

    private static class AsyncLazy<C> extends Lazy<C> {

        private C value = null;

        private AsyncLazy(Callback<C> callback) {
            super(callback);
        }

        @Override
        public C get() {
            if (value == null) {
                try {
                    value = callback.init();
                } catch (Exception ex) {
                    throw new RuntimeException("Cannot instantiate lazy instance", ex);
                }
            }
            return value;
        }

        @Override
        public void reset() {
            value = null;
        }

        @Override
        public void reset(C value) {
            synchronized (this) {
                this.value = value;
            }
        }
    }

    private static class SyncLazy<C> extends Lazy<C> {

        private volatile C value = null;

        private SyncLazy(Callback<C> callback) {
            super(callback);
        }

        @Override
        public C get() {
            C c = value;
            if (c == null) {
                synchronized (this) {
                    if ((c = value) == null) {
                        try {
                            c = (value = callback.init());
                        } catch (Exception ex) {
                            throw new RuntimeException("Cannot instantiate lazy instance", ex);
                        }
                    }
                }
            }
            return c;
        }

        @Override
        public void reset() {
            value = null;
        }

        @Override
        public void reset(C value) {
            synchronized (this) {
                this.value = value;
            }
        }
    }
}
