package com.ww.server.exception;

/**
 *
 * @author sandy
 */
public class RuntimeWrapperException extends RuntimeException {

    public final Throwable wrapedException;

    public RuntimeWrapperException(Throwable thr) {
        super(thr);
        wrapedException = thr;
    }

}
