package com.github.m0ttii.orm.query;

import com.github.m0ttii.DatabaseConnection;
import com.github.m0ttii.annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteQuery<T> extends BaseQuery<T> {
    private final Object id;

    public DeleteQuery(Class<T> type, Object id) {
        super(type);
        this.id = id;
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        String idColumn = null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = getColumnName(field);
                break;
            }
        }

        if (idColumn == null) {
            throw new IllegalStateException("No ID column found");
        }

        return "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
    }

    @Override
    public int save() throws SQLException {
        String sql = buildSql();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            return pstmt.executeUpdate();
        }
    }
}

