package com.github.m0ttii.orm.query;

import com.github.m0ttii.DatabaseConnection;

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
                    pstmt.setObject(index++, field.get(entity));
                }
            }
            return pstmt.executeUpdate();
        }
    }
}
