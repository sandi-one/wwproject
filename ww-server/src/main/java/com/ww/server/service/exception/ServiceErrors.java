package com.ww.server.service.exception;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author sandy
 */
public enum ServiceErrors {

    TRANSACTION_COMMIT_EXCEPTION(true),
    TRANSACTION_BEGIN_EXCEPTION(true),
    TRANSACTION_ROLLBACK_EXCEPTION(true),
    TRANSACTION_REGISTER_SYNCHRONIZATION_ERROR(true);

    private final boolean serverSideError;

    private ServiceErrors(boolean serverSideError) {
        this.serverSideError = serverSideError;
    }

    private ServiceErrors() {
        this(false);
    }

    public boolean isServerSideError() {
        return this.serverSideError;
    }

     public String getCode() {
        return name();
    }

    public String formatMessage(String... fields) {

        String key = name().toLowerCase().replaceAll("_", ".");

        // get message with code
        ResourceBundle resources = ResourceBundle.getBundle("com.ww.server.exception.ServiceErrors");

        return MessageFormat.format(resources.getString(key), (Object[]) fields);
    }
}
