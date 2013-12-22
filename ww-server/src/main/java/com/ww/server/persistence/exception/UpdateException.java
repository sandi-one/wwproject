package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class UpdateException extends PersistenceException {

    Class clazz;

    public UpdateException(Class clazz, Exception e) {
        super(ErrorMessage.ERROR_WHILE_UPDATING, e, clazz.getName());
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

}
