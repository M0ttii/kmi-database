package com.github.m0ttii.database.orm.query;

import com.github.m0ttii.database.DatabaseConnection;
import com.github.m0ttii.database.annotations.Id;
import com.github.m0ttii.database.annotations.JoinColumn;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertQuery<T> extends BaseQuery<T> {
    private final Object entity;

    public InsertQuery(Class<T> type, Object entity) {
        super(type);
        this.entity = entity;
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(entity) != null) {
                    columns.append(getColumnName(field)).append(",");
                    values.append("?,");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        columns.setLength(columns.length() - 1); // Entfernt das letzte Komma
        values.setLength(values.length() - 1); // Entfernt das letzte Komma

        return "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + values.toString() + ")";
    }

    @Override
    public int save() throws SQLException, IllegalAccessException {
        String sql = buildSql();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Field[] fields = type.getDeclaredFields();
            int index = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.get(entity) != null) {
                    if (field.isAnnotationPresent(JoinColumn.class)) {
                        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                        Object relatedEntity = field.get(entity);
                        if (relatedEntity != null) {
                            Field idField = getIdField(relatedEntity.getClass());
                            idField.setAccessible(true);
                            Object joinValue = idField.get(relatedEntity);
                            pstmt.setObject(index++, joinValue);
                        }
                    } else {
                        pstmt.setObject(index++, field.get(entity));
                    }
                }
            }
            return pstmt.executeUpdate();
        }
    }

    private Field getIdField(Class<?> relatedClass) {
        for (Field field : relatedClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new RuntimeException("No ID field found in related class: " + relatedClass.getName());
    }
}
