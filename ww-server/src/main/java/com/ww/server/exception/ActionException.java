package com.ww.server.exception;

/**
 *
 * @author sandy
 */
public class ActionException extends Exception {

    private ActionErrors error;
    private String[] fields = null;

    protected ActionException(String errorMessage) {
        super(errorMessage);
    }

    protected ActionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public ActionException(ActionErrors error, Throwable t, String... fields) {
        super(error.formatMessage(fields), t);
        this.error = error;
        this.fields = fields;
    }

    public ActionException(ActionErrors error, Throwable t) {
        this(error, t, (String[]) null);
    }

    public ActionException(ActionErrors error, String... fields) {
        this(error, null, fields);
    }

    public ActionException(ActionErrors error) {
        this(error, (String[]) null);
    }

    public String getErrorCode() {
        return error.name();
    }

    public boolean isServerSideError() {
        return error.isServerSideError();
    }

    public String[] getFields() {
        return this.fields;
    }
}
