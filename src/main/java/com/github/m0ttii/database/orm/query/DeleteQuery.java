package com.github.m0ttii.database.orm.query;

import com.github.m0ttii.database.DatabaseConnection;
import com.github.m0ttii.database.annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class DeleteQuery<T> extends BaseQuery<T> {
    private final Object id;
    private final Map<String, Object> compositeId;

    public DeleteQuery(Class<T> type, Object id) {
        super(type);
        this.id = id;
        this.compositeId = null;
    }

    public DeleteQuery(Class<T> type, Map<String, Object> compositeId) {
        super(type);
        this.id = null;
        this.compositeId = compositeId;
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        StringBuilder whereClause = new StringBuilder(" WHERE ");

        if (hasCompositeKey()) {
            // Handling composite keys
            try {
                for (Map.Entry<String, Object> entry : compositeId.entrySet()) {
                    whereClause.append(entry.getKey()).append(" = ? AND ");
                    conditions.put(entry.getKey(), entry.getValue());
                }
                whereClause.setLength(whereClause.length() - 5);
            } catch (Exception e) {
                throw new RuntimeException("Failed to build where clause for composite key", e);
            }
        } else {
            // Handling single key
            String idColumn = getPrimaryKeyColumn(type.getDeclaredFields());
            conditions.put(idColumn, id);
            whereClause.append(idColumn).append(" = ?");
        }

        return "DELETE FROM " + tableName + whereClause.toString();
    }

    @Override
    public int save() throws SQLException {
        String sql = buildSql();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object value : conditions.values()) {
                pstmt.setObject(index++, value);
            }
            return pstmt.executeUpdate();
        }
    }
}

