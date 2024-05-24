package com.github.m0ttii.orm.query;

import com.github.m0ttii.DatabaseConnection;
import com.github.m0ttii.annotations.Column;
import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.JoinTable;
import com.github.m0ttii.orm.DataORM;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseQuery<T> {
    protected final Class<T> type;
    protected final HashMap<String, Object> conditions = new HashMap<>();
    protected final List<String> joins = new ArrayList<>();

    public BaseQuery(Class<T> type) {
        this.type = type;
    }

    public BaseQuery<T> where(String fieldName, Object value) {
        conditions.put(fieldName, value);
        return this;
    }

    public BaseQuery<T> join(String joinTable, String joinField, String baseField) {
        joins.add("JOIN " + joinTable + " ON " + getTableName() + "." + baseField + " = " + joinTable + "." + joinField);
        return this;
    }

    protected abstract String buildSql();


    public List<T> execute() throws SQLException, ReflectiveOperationException {
        List<T> list = new ArrayList<>();
        String sql = buildSql();
        String x = sql;
        List<Object> values = new ArrayList<>(conditions.values());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                T obj = type.getDeclaredConstructor().newInstance();
                Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(JoinTable.class)) {
                        JoinTable joinTable = field.getAnnotation(JoinTable.class);
                        Object joinValue = rs.getObject(joinTable.joinColumn());
                        if (joinValue != null) {
                            Object relatedEntity = loadRelatedEntity(field.getType(), joinTable, joinValue);
                            field.set(obj, relatedEntity);
                        }
                    } else {
                        field.set(obj, rs.getObject(getColumnName(field)));
                    }
                }
                list.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public T findOne() throws ReflectiveOperationException, SQLException {
        List<T> results = execute();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private Object loadRelatedEntity(Class<?> relatedType, JoinTable joinTable, Object joinValue) throws SQLException, ReflectiveOperationException {
        DataORM<?> orm = new DataORM<>(relatedType);

        String sql = "SELECT * FROM " + joinTable.name() + " WHERE " + joinTable.referencedColumnName() + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, joinValue);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Object relatedEntity = relatedType.getDeclaredConstructor().newInstance();
                Field[] fields = relatedType.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    field.set(relatedEntity, rs.getObject(getColumnName(field)));
                }
                return relatedEntity;
            }
        }
        return null;
    }

    protected String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.name();
        }
        return field.getName();
    }

    protected String getTableName() {
        if (type.isAnnotationPresent(Entity.class)) {
            Entity entity = type.getAnnotation(Entity.class);
            return entity.tableName();
        }
        return type.getSimpleName();
    }

}
