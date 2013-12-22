package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class SaveException extends PersistenceException {

    Class clazz;

    public SaveException(Class clazz, Exception e) {
        super(ErrorMessage.ERROR_WHILE_SAVING, e, clazz.getName());
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }
}
