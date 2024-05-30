package com.github.m0ttii.database.orm.query;

import com.github.m0ttii.database.DatabaseConnection;
import com.github.m0ttii.database.annotations.Column;
import com.github.m0ttii.database.annotations.Id;
import com.github.m0ttii.database.annotations.JoinColumn;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateQuery<T> extends BaseQuery<T> {
    private final Object entity;

    public UpdateQuery(Class<T> type, Object entity) {
        super(type);
        this.entity = entity;
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder setClause = new StringBuilder();
        String idColumn = null;

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = getColumnName(field);
            } else {
                try {
                    if (field.get(entity) != null) {
                        if (field.isAnnotationPresent(Column.class)) {
                            setClause.append(getColumnName(field)).append(" = ?,");
                        } else if (field.isAnnotationPresent(JoinColumn.class)) {
                            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                            Object relatedEntity = field.get(entity);
                            if (relatedEntity != null) {
                                UpdateQuery<?> updateQuery = new UpdateQuery<>(field.getType(), relatedEntity);
                                updateQuery.save();
                                Object joinValue = field.getType().getMethod("get" + capitalize(joinColumn.referencedColumnName())).invoke(relatedEntity);

                                setClause.append(joinColumn.name()).append(" = ?,");
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        setClause.setLength(setClause.length() - 1); // Entfernt das letzte Komma

        if (idColumn == null) {
            throw new IllegalStateException("No ID column found");
        }

        return "UPDATE " + tableName + " SET " + setClause.toString() + " WHERE " + idColumn + " = ?";
    }

    @Override
    public int save() throws SQLException {
        String sql = buildSql();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Field[] fields = type.getDeclaredFields();
            int index = 1;
            Object idValue = null;
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    try {
                        idValue = field.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new SQLException("Failed to access ID field", e);
                    }
                } else {
                    try {
                        if (field.get(entity) != null) {
                            if (field.isAnnotationPresent(Column.class)) {
                                pstmt.setObject(index++, field.get(entity));
                            } else if (field.isAnnotationPresent(JoinColumn.class)) {
                                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                                if (field.get(entity) != null) {
                                    // Verwenden Sie die id anstelle des gesamten Foreign-Key-Objekts
                                    Object joinValue = field.get(entity).getClass().getMethod("get" + capitalize(joinColumn.referencedColumnName())).invoke(field.get(entity));
                                    pstmt.setObject(index++, joinValue);
                                }
                            }
                        }
                    } catch (IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (idValue == null) {
                throw new IllegalStateException("No ID value found");
            }
            pstmt.setObject(index, idValue);
            return pstmt.executeUpdate();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
