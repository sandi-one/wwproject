package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class FetchByIdException extends PersistenceException {

    private Class clazz;
    private String id;

    public FetchByIdException(Class clazz, String id, Exception e) {
        super(ErrorMessage.ERROR_WHILE_FETCHING_BY_ID, e, clazz.getName(), id);
        this.clazz = clazz;
        this.id = id;
    }

    public Class getClazz() {
        return clazz;
    }
}
