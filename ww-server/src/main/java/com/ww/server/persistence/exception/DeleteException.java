package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class DeleteException extends PersistenceException {

    private Class clazz;

    public DeleteException(Class clazz, Exception e) {
        super(ErrorMessage.ERROR_WHILE_DELETING, e, clazz.getName());
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }
}
