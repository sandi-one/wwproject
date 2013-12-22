package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class UniquenessException extends PersistenceException {
    private static final long serialVersionUID = 1209409238473018574L;

    public UniquenessException(Class clazz, Exception e) {
        super(ErrorMessage.ENTITY_IS_NOT_UNIQUE, e, clazz.toString());
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    private Class clazz;
}
