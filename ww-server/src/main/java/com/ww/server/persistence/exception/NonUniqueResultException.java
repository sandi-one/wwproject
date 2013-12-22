package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class NonUniqueResultException extends PersistenceException {

    public NonUniqueResultException(Class clazz, Exception e) {
        super(ErrorMessage.NON_UNIQUE_RESULT, e, clazz.toString());
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    private Class clazz;

}
