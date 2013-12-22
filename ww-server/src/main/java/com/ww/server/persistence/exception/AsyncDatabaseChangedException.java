package com.ww.server.persistence.exception;

import org.hibernate.exception.LockAcquisitionException;

/**
 *
 * @author sandy
 */
public class AsyncDatabaseChangedException extends PersistenceException {

    public AsyncDatabaseChangedException() {
        super(ErrorMessage.ERROR_WHILE_SAVING, new LockAcquisitionException(null, null), "");
    }
}