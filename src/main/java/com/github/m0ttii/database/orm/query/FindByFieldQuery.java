package com.github.m0ttii.database.orm.query;

import java.lang.reflect.Field;

public class FindByFieldQuery<T> extends BaseQuery<T> {
    private final String fieldName;
    private final Object value;

    public FindByFieldQuery(Class<T> type, String fieldName, Object value) {
        super(type);
        this.fieldName = resolveFieldName(fieldName);
        this.value = value;
        conditions.put(fieldName, value);
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        StringBuilder columns = new StringBuilder();
        StringBuilder joinClause = new StringBuilder();
        Field[] fields = type.getDeclaredFields();

        for (String join : joins) {
            joinClause.append(join).append(" ");
        }

        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
        }
        columns.setLength(columns.length() - 1); // Entfernt das letzte Komma

        StringBuilder whereClause = new StringBuilder(" WHERE ");
        whereClause.append(fieldName).append(" = ?");

        return "SELECT " + columns + " FROM " + tableName + whereClause.toString();
    }

    private String resolveFieldName(String fieldName) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equals(fieldName) || getColumnName(field).equals(fieldName)) {
                return getColumnName(field);
            }
        }
        throw new IllegalArgumentException("No field found with name: " + fieldName);
    }
}
