package com.ww.server.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;

/**
 *
 * @author sandy
 */
public class QueryUtils {

    public static String convertToParameterName(String key) {
        if (key == null) {
            return null;
        }
        return key.replace('.', '_');
    }

    private static void addQueryParamsByKeys(Query query, String key, Object value) {
        String keyParam = convertToParameterName(key);
        if (value != null) {

            if (value instanceof Collection) {
                query.setParameterList(keyParam, (Collection) value);
            } else if (value instanceof Object[]) {
                query.setParameterList(keyParam, (Object[]) value);
            } else {
                query.setParameter(keyParam, value);
            }
        } else {
            query.setParameter(keyParam, value);
        }
    }

    private static void appendAlias(DetachedCriteria criteria, String key) {
        if (key == null) {
            return;
        }

        String[] res = key.split("\\.");
        if (res.length > 1) {
            criteria.createAlias(res[0], res[0]);
        }
    }

    private static void addCriteriaParamsByKeys(DetachedCriteria criteria, String key, Object value) {
        //todo: check in case of double aliases
        appendAlias(criteria, key);
        if (null == value) {
            criteria.add(Property.forName(key).isNull());
        } else if (value instanceof Collection) {
            criteria.add(Property.forName(key).in((Collection) value));
        } else {
            criteria.add(Property.forName(key).eq(value));
        }
    }

    public static Query makeQuery(final String query, final Map<String, Object> params) {
        return makeQuery(query, params, Persistence.NON_LIMIT, Persistence.HQL_QUERY);
    }

    public static Query makeQuery(final String query, final Map<String, Object> params,
            final Integer limit, boolean isHQL) {
        return makeQuery(new MapParamsHandler(params), query, limit, isHQL);
    }

    public static Query makeQuery(final String query, final Map<String, Object> params,
            final Integer limit, int offset, boolean isHQL) {
        Query q = makeQuery(new MapParamsHandler(params), query, limit, isHQL);

        if (offset != 0) {
            q.setFirstResult(offset);
        }

        return q;
    }

    public static Query makeQuery(final String query,
            String[] fieldNames, Object[] fieldValues) {
        return makeQuery(query, fieldNames, fieldValues, Persistence.NON_LIMIT,
                Persistence.HQL_QUERY);
    }

    public static Query makeQuery(final String query,
            final String[] fieldNames, final Object[] fieldValues,
            final Integer limit, boolean isHQL) {

        return makeQuery(new ArrayParamsHandler(fieldNames, fieldValues), query, limit, isHQL);
    }

    public static interface QueryParamsHandler {

        public void addQueryParams(Query q);

        public void addCriteriaParams(DetachedCriteria criteria);
    }

    public static class ArrayParamsHandler implements QueryParamsHandler {

        private String[] fieldNames;
        private Object[] fieldValues;

        public ArrayParamsHandler(String[] fieldNames, Object[] fieldValues) {
            this.fieldNames = fieldNames;
            this.fieldValues = fieldValues;

            checkInParams();
        }

        public void addQueryParams(Query query) {
            //checkInParams();

            if ((fieldNames != null) && (fieldValues != null)
                    && (fieldValues.length == fieldNames.length)) {
                for (int i = 0; i < fieldNames.length; ++i) {
                    addQueryParamsByKeys(query, fieldNames[i], fieldValues[i]);
                }
            }
        }

        private void checkInParams() {
            if ((fieldNames != null) && (fieldValues != null)
                    && (fieldValues.length != fieldNames.length)) {
                // todo: throw exception
            }
        }

        public void addCriteriaParams(DetachedCriteria criteria) {
            //checkInParams();

            if ((fieldNames != null) && (fieldValues != null)
                    && (fieldValues.length == fieldNames.length)) {
                for (int i = 0; i < fieldNames.length; ++i) {
                    addCriteriaParamsByKeys(criteria, fieldNames[i], fieldValues[i]);
                }
            }
        }
    }

    public static class MapParamsHandler implements QueryParamsHandler {

        private Map<String, Object> params;

        public MapParamsHandler(Map<String, Object> params) {
            this.params = params;
        }

        public void addQueryParams(Query q) {
            addQueryParams(q, params);
        }

        private static void addQueryParams(Query query, Map<String, Object> keys) {
            if (keys != null && query != null) {
                for (Map.Entry<String, Object> keyValue : keys.entrySet()) {
                    addQueryParamsByKeys(query, keyValue.getKey(), keyValue.getValue());
                }
            }
        }

        public void addCriteriaParams(DetachedCriteria criteria) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                addCriteriaParamsByKeys(criteria, entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Query makeQuery(
            QueryParamsHandler handler,
            final String query,
            final Integer limit, boolean isHQL) {

        Session session = TransactionScope.getSessionForScope();

        Query q = null;
        if (isHQL) {
            q = session.createQuery(query);
        } else {
            q = session.createSQLQuery(query);
        }

        handler.addQueryParams(q);

        if (!(Persistence.NON_LIMIT == limit)) {
            q.setMaxResults(limit);
        }

        return q;
    }

    public static DetachedCriteria makeCriteria(Class entityClass, Map<String, Object> params) {
        return makeCriteria(entityClass, new MapParamsHandler(params));
    }

    public static DetachedCriteria makeCriteria(Class entityClass, String[] keys, Object[] values) {
        return makeCriteria(entityClass, new ArrayParamsHandler(keys, values));
    }

    private static DetachedCriteria makeCriteria(Class entityClass, QueryParamsHandler handler) {

        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        handler.addCriteriaParams(criteria);

        return criteria;
    }

    //--------------------------------------------------------------------------
    public static Query buildQueryByKeys(Session session, Class entityClass,
            Map<String, Object> keys, LinkedHashMap<String, OrderDirection> orderByConditions) {

        HSQLBuilder builder = new HSQLBuilder(entityClass);
        builder.orderBy(orderByConditions);

        FilterUtil filter = new FilterUtil(keys);
        filter.setWhereClause(builder);

        Query query = session.createQuery(builder.toString());

        Map<String, Object> namedParams = new HashMap<String, Object>();
        filter.setParams(namedParams);
        addQueryParamsByKeys(query, namedParams);

        return query;
    }

    public static interface IFilterUtil {

        public HSQLBuilder setWhereClause(HSQLBuilder builder);

        public void setParams(Map<String, Object> params);
    }

    public static class FilterUtil implements IFilterUtil {

        private Map<String, Object> params;
        private Map<String, Object> namedParams = null;

        public FilterUtil(Map<String, Object> params) {
            this.params = params;
        }

        public Map<String, Object> getParams() {
            return this.params;
        }

        @SuppressWarnings("deprecation")
        public HSQLBuilder setWhereClause(HSQLBuilder builder) {
            this.namedParams = QueryUtils.buidWhereByKeys(builder, this.getParams());
            return builder;
        }

        public void setParams(Map<String, Object> params) {
            if (null != namedParams) {
                params.putAll(this.namedParams);
            }
        }
    }

    private static Map<String, Object> buidWhereByKeys(HSQLBuilder builder, Map<String, Object> keys) {
        Map<String, Object> keyWithNamedParams = new HashMap<String, Object>();
        if (keys != null) {
            for (Map.Entry<String, Object> key : keys.entrySet()) {
                String keyNamedParam = convertToParameterName(key.getKey());
                builder.and(key.getKey(), key.getValue(), keyNamedParam);

                keyWithNamedParams.put(keyNamedParam, key.getValue());
            }
        }

        return keyWithNamedParams;
    }

    private static void addQueryParamsByKeys(Query query, Map<String, Object> keys) {
        if (keys != null && query != null) {
            for (Map.Entry<String, Object> key : keys.entrySet()) {
                addQueryParamsByKeys(query, key.getKey(), key.getValue());
            }
        }
    }
}
