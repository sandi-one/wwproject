package com.ww.server.enums;

/**
 *
 * @author sandy
 */
public enum TagName {

    USER_LOGIN("login"),
    USER_PASSWORD("pass");


    private String name;

    private TagName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
