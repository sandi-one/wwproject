package com.ww.server.enums;

/**
 *
 * @author sandy
 */
public enum TagName {

    SUCCESS("success"),
    ERROR_ID("errorid"),
    ERROR_MESSAGE("message"),
    EXCEPTION_FIELDS("exceptionFields"),
    TRACE("stacktrace"),
    EXCEPTION("exception"),

    USER_LOGIN("login"),
    USER_PASSWORD("pass"),
    ACTION("action"),
    CLASS("class");


    private String name;

    private TagName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
