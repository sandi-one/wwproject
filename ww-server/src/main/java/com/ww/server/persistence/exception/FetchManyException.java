package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class FetchManyException extends PersistenceException {

    private Class clazz;

    public FetchManyException(Class clazz, Exception e) {
        super(ErrorMessage.ERROR_WHILE_FETCHING_MANY_BY_IDS, e,
                clazz == null ? null : clazz.getName());
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }
}
