package com.ww.server.enums;

/**
 *
 * @author sandy
 */
public enum TransactionIsolation {
    REPEATABLE_READ("REPEATABLE-READ"),
    READ_COMMITTED("READ-COMMITTED"),
    READ_UNCOMMITTED("READ-UNCOMMITTED"),
    SERIALIZABLE("SERIALIZABLE");

    private String value;

    private TransactionIsolation(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
