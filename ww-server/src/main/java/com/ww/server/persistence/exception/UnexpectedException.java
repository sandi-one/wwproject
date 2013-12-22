package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class UnexpectedException extends PersistenceException {

    public UnexpectedException(Exception e) {
        super(ErrorMessage.PERSISTENCE_LEVEL_UNEXPECTED_ERROR, e);
    }
}
