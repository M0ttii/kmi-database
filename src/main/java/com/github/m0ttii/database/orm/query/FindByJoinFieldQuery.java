package com.github.m0ttii.database.orm.query;


import com.github.m0ttii.database.annotations.JoinColumn;

import java.lang.reflect.Field;

public class FindByJoinFieldQuery<T> extends BaseQuery<T> {
    private final String fieldName;
    private final Object value;

    public FindByJoinFieldQuery(Class<T> type, String fieldName, Object value) {
        super(type);
        this.fieldName = resolveFieldName(fieldName);
        this.value = value;
        try {
            String condition = buildCondition(this.fieldName, value);
            conditions.put(condition, value);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No field found with name: " + this.fieldName, e);
        }
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
            columns.append("main." + getColumnName(field)).append(",");
        }
        columns.setLength(columns.length() - 1); // Entfernt das letzte Komma

        StringBuilder whereClause = new StringBuilder(" WHERE ");
        //whereClause.append(fieldName).append(" = ?");
        String fieldSuffix = fieldName.substring(fieldName.indexOf('.') + 1);
        String newFieldName = "second." + fieldSuffix;
        whereClause.append(newFieldName).append(" = ?");
        //whereClause.append("second.").append(fieldName.split(".")[1]).append(" = ?");
        //System.out.println(whereClause);

        return "SELECT " + columns + " FROM " + tableName + " main " + joinClause.toString() + whereClause.toString();
    }

    private String buildCondition(String fieldName, Object value) throws NoSuchFieldException {
        StringBuilder condition = new StringBuilder();
        String[] parts = fieldName.split("\\.");
        Class<?> currentType = type;

        for (String part : parts) {
            Field field = getFieldByNameOrColumn(currentType, part);
            if (field == null) {
                return null;
            }

            if (condition.length() > 0) {
                condition.append(".");
            }
            condition.append(getColumnName(field));
            currentType = field.getType();
        }

        return condition.toString();
    }

    private String resolveFieldName(String fieldName) {
        String[] parts = fieldName.split("_");
        Class<?> currentType = type;
        StringBuilder resolvedField = new StringBuilder();
        String currentTableName = getTableName();

        for (String part : parts) {
            Field field = getFieldByNameOrColumn(currentType, part);
            if (field == null) {
                throw new IllegalArgumentException("No field found with name: " + part);
            }

            if (field.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                String joinTableName = field.getType().getSimpleName().toLowerCase();
                joins.add("JOIN " + joinTableName + " second ON " + "main" + "." + joinColumn.name() + " = " + "second" + "." + joinColumn.referencedColumnName());
                currentTableName = joinTableName;
            }

            currentType = field.getType();
        }

        return currentTableName + "." + parts[parts.length - 1];
    }
}
