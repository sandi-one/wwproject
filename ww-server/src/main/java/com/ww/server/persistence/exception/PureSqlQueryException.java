package com.ww.server.persistence.exception;

/**
 *
 * @author sandy
 */
public class PureSqlQueryException extends PersistenceException {

    public PureSqlQueryException(String query, Exception ex) {
        super(ErrorMessage.PURE_SQL_QUERY, ex, query);
    }
}
