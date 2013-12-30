package com.ww.server.exception;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author sandy
 */
public enum ActionErrors {

    UNKNOWN_SERVER_ERROR(true),
    BAD_REQUEST,
    DEADLOCK_LIMIT_REACHED(true),
    PARAM_MUST_NOT_BE_NULL,
    PARAM_MUST_NOT_BE_EMPTY,
    INCORRECT_BOOLEAN_VALUE,
    INCORRECT_INTEGER_VALUE,
    NOT_AUTHENTICATED;

    private final boolean serverSideError;


    private ActionErrors(boolean serverSideError) {
        this.serverSideError = serverSideError;
    }

    private ActionErrors() {
        this(false);
    }

    public boolean isServerSideError() {
        return this.serverSideError;
    }

    public String getCode() {
        return name();
    }

    public String formatMessage(String[] fields) {
        String key = name().toLowerCase().replaceAll("_", ".");

        // get message with code
        ResourceBundle resources = ResourceBundle.getBundle("com.ww.server.exception.ActionErrors");
        return MessageFormat.format(resources.getString(key), (Object[]) fields);

    }
}
