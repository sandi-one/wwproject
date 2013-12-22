package com.ww.server.persistence.exception;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author sandy
 */
public class PersistenceException extends RuntimeException {

    private ErrorMessage message;

    protected PersistenceException(ErrorMessage message, Exception e, String... fields) {
        super(message.formatMessage(fields), e);
        this.message = message;
    }

    public String getErrorCode() {
        return message.name();
    }

    protected static enum ErrorMessage {

        ENTITY_IS_NOT_UNIQUE, //constraint violation while save/update
        ERROR_WHILE_LISTING_BY_ORGID,
        ERROR_WHILE_COUNTING_ALL,
        ERROR_WHILE_COUNTING_BY_ORGID,
        ERROR_WHILE_FETCHING_BY,
        ERROR_WHILE_FETCHING_UNIQUE,
        ERROR_WHILE_FETCHING_BY_ORGID_AND_NAME,
        ERROR_WHILE_FETCHING_BY_TYPE,
        ERROR_WHILE_FETCHING_BY_ID,
        ERROR_WHILE_FETCHING_BY_PHPBB_ID,
        ERROR_WHILE_FETCHING_MANY_BY_IDS,
        ERROR_WHILE_FETCHING_ALL,
        ERROR_WHILE_FETCHING_BY_PAGEID,
        ERROR_WHILE_FETCHING_VALID,
        ERROR_WHILE_UPDATING,
        ERROR_WHILE_SAVING,
        ERROR_WHILE_DELETING,
        ERROR_WHILE_CHECKING_DATABASE_EXISTENCE,
        NON_UNIQUE_RESULT, //while retriving unique result
        PERSISTENCE_LEVEL_UNEXPECTED_ERROR,
        COMPLEX_DATABASE_OPERATION_ERROR,
        PURE_SQL_QUERY;

        public String formatMessage(String... fields) {
            String key = name().toLowerCase().replaceAll("_", ".");

            // get message with code
            ResourceBundle resources = ResourceBundle.getBundle(
                    "com.ww.server.exception.PersistenceErrors");

            return MessageFormat.format(resources.getString(key), (Object[]) fields);
        }
    }
}
