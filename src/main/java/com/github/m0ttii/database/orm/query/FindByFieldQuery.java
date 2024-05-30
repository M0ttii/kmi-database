package com.github.m0ttii.database.orm.query;

import java.lang.reflect.Field;

public class FindByFieldQuery<T> extends BaseQuery<T> {
    private final String fieldName;
    private final Object value;

    public FindByFieldQuery(Class<T> type, String fieldName, Object value) {
        super(type);
        this.fieldName = fieldName;
        this.value = value;
        conditions.put(fieldName, value);
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        StringBuilder columns = new StringBuilder();
        Field[] fields = type.getDeclaredFields();

        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
        }
        columns.setLength(columns.length() - 1); // Entfernt das letzte Komma

        StringBuilder whereClause = new StringBuilder(" WHERE ");
        whereClause.append(fieldName).append(" = ?");

        return "SELECT " + columns + " FROM " + tableName + whereClause.toString();
    }
}
