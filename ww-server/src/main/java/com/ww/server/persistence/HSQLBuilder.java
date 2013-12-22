package com.ww.server.persistence;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author sandy
 */

public class HSQLBuilder {

    private static String FROM = "FROM ";
    private static String DEFAULT_TABLE_ALIAS = "t";
    private static String WHERE = " Where ";
    private static String HAVING = " Having ";
    private static String ORDER_BY = " ORDER BY ";

    private static String AND = " and ";
    private static String EQ = " = ";
    private static String NOT_EQ = " != ";
    private static String IS_NULL = " is null ";
    private static String IN_PREFIX = " in ( ";
    private static String IN_POSTFIX = " ) ";
    private static String NOT = " not ";
    private static String COMMA = " , ";
    private static String COLON = " :";
    private static String LIKE = " like ";

    private StringBuilder fromStatement = new StringBuilder();
    private StringBuilder whereClause = new StringBuilder();
    // todo: add group by support
    private StringBuilder havingClause = new StringBuilder();
    private StringBuilder orderByClause = new StringBuilder();
    private String tableAlias;
    private String fieldsPrefix;

    public HSQLBuilder(Class entityClass) {
        this(entityClass, DEFAULT_TABLE_ALIAS);
    }

    public HSQLBuilder(Class entityClass, String tableAlias) {
        // todo: check, that tableAlias doesn't contain '.', ' ' and other bad symbols.
        this.tableAlias = tableAlias;

        // init From
        makeFrom(entityClass);

        fieldsPrefix = this.tableAlias.concat(".");
    }

    protected String getFromString() {
        return fromStatement.toString();
    }

    protected String getWhereString() {
        if (0 == whereClause.length()) {
            return "";
        }
        return WHERE + whereClause.toString();
    }

    protected String getHavingString() {
        if (0 == havingClause.length()) {
            return "";
        }
        return HAVING + havingClause.toString();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(fromStatement);
        if (0 != whereClause.length()) {
            res.append(WHERE);
            res.append(whereClause);
        }
        if (0 != havingClause.length()) {
            res.append(HAVING);
            res.append(havingClause);
        }
        if (0 != orderByClause.length()) {
            res.append(ORDER_BY);
            res.append(orderByClause);
        }
        return res.toString();
    }

    public HSQLBuilder andEq(String fieldName, String val) {
        return andEq(fieldName, val, false);
    }

    public HSQLBuilder andNotEq(String fieldName, String val) {
        return andEq(fieldName, val, true);
    }

    private HSQLBuilder andEq(String fieldName, String val, boolean not) {

        isStartOfClause(whereClause, AND);

        whereClause.append(fieldsPrefix).append(fieldName).append(not ? NOT_EQ : EQ).append(COLON).append(
                val);

        return this;
    }

    public HSQLBuilder andIsNull(String fieldName) {

        isStartOfClause(whereClause, AND);

        whereClause.append(fieldsPrefix).append(fieldName).append(IS_NULL);

        return this;
    }

    public HSQLBuilder andIsIn(String fieldName, String val) {
        return andNotIn(fieldName, val, false);
    }

    public HSQLBuilder andNotIn(String fieldName, String val) {
        return andNotIn(fieldName, val, true);
    }

    private HSQLBuilder andNotIn(String fieldName, String val, boolean not) {

        isStartOfClause(whereClause, AND);

        whereClause.append(fieldsPrefix).append(fieldName).append(not ? NOT : "").append(IN_PREFIX).append(COLON).append(val).append(
                IN_POSTFIX);

        return this;
    }

    public HSQLBuilder andLike(String fieldName, String val) {

        isStartOfClause(whereClause, AND);

        whereClause.append(fieldsPrefix).append(fieldName).append(LIKE).append(COLON).append(val);

        return this;
    }

    //todo: make protected the method
    public HSQLBuilder and(String fieldName, Object val, String valParamName) {

        if (val == null) {
            return andIsNull(fieldName);
        } else if (val instanceof Collection) {
            return andIsIn(fieldName, valParamName);
        } else {
            return andEq(fieldName, valParamName);
        }
    }

    public HSQLBuilder orderBy(String fieldName, OrderDirection orderByConditions) {

        isStartOfClause(orderByClause, COMMA);

        orderByClause.append(fieldsPrefix).append(fieldName).append(" ").append(orderByConditions);

        return this;
    }

    public HSQLBuilder orderBy(Map<String, OrderDirection> order) {
        if (order == null || order.size() <= 0) {
            return this;
        }

        for (Map.Entry<String, OrderDirection> orderItem : order.entrySet()) {
            orderBy(orderItem.getKey(), orderItem.getValue());
        }

        return this;
    }

    private void makeFrom(Class entityClass) {
        fromStatement.append(FROM);
        fromStatement.append(entityClass.getSimpleName());
        fromStatement.append(" ");
        fromStatement.append(this.tableAlias);
        fromStatement.append(" ");
    }

    private void isStartOfClause(StringBuilder clause, String separator) {
        if (0 != clause.length()) {
            clause.append(separator);
        }
    }

    private String catFirstColon(String val) {
        if (val.startsWith(":")) {
            val = val.substring(1);
        }
        return val;
    }

}
