package com.ww.server.service.exception;

import com.ww.server.exception.ActionException;

/**
 *
 * @author sandy
 */
public class ServiceException extends ActionException {

    ServiceErrors error;

    public ServiceException(ServiceErrors error) {
        super(new String());
        this.error = error;
    }

    public ServiceException(ServiceErrors error, String... fields) {
        super(error.formatMessage(fields));
        this.error = error;
    }

    public ServiceException(ServiceErrors error, Throwable cause, String... fields) {
        super(error.formatMessage(fields), cause);
        this.error = error;
    }

    @Override
    public String getErrorCode() {
        return error.name();
    }

    @Override
    public boolean isServerSideError() {
        return error.isServerSideError();
    }
}
