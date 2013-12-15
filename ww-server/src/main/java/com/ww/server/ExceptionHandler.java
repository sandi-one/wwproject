package com.ww.server;

import com.ww.server.exception.RuntimeWrapperException;

/**
 *
 * @author sandy
 */
public interface ExceptionHandler<T> {

    public static final ExceptionHandler<Void> DEFAULT = new ExceptionHandler() {

        @Override
        public Void handleException(Throwable thr) {
            if (!(thr instanceof RuntimeException)) {
                throw new RuntimeWrapperException(thr);
            } else {
                throw (RuntimeException) thr;
            }
        }
    };

    public T handleException(Throwable thr);

}
