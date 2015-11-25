package com.habosa.yoursquare.sql;

/**
 * Created by samstern on 11/25/15.
 */
public class QueryBuilder {

    private StringBuilder sb = new StringBuilder();

    public QueryBuilder() {
        sb.append('(');
    }

    public QueryBuilder equals(String col, Object value) {
        sb.append(col);
        sb.append(" = ");
        sb.append(value);

        return this;
    }

    public QueryBuilder contains(String col, String value) {
        sb.append(col);
        sb.append(" LIKE ");
        sb.append("'%");
        sb.append(value);
        sb.append("%'");

        return this;
    }

    public QueryBuilder and() {
        sb.append(" AND ");

        return this;
    }

    public QueryBuilder or() {
        sb.append(" OR ");

        return this;
    }

    public String build() {
        sb.append(')');
        return sb.toString();
    }
}
