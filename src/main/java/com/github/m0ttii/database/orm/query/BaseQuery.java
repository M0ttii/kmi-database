package com.github.m0ttii.database.orm.query;

import com.github.m0ttii.database.DatabaseConnection;
import com.github.m0ttii.database.annotations.*;
import com.github.m0ttii.database.orm.DataORM;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    int test = 0;
    public List<T> execute() throws SQLException, ReflectiveOperationException {
        List<T> list = new ArrayList<>();
        String sql = buildSql();
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
                    if (field.isAnnotationPresent(JoinColumn.class)) {
                        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                        Object joinValue = rs.getObject(joinColumn.name());
                        if (joinValue != null) {
                            Object relatedEntity = loadRelatedEntity(field.getType(), joinColumn, joinValue);
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

    public int save() throws SQLException, IllegalAccessException {
        String sql = buildSql();
        List<Object> values = new ArrayList<>(conditions.values());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            return pstmt.executeUpdate();
        }
    }

    public T findOne() throws ReflectiveOperationException, SQLException {
        List<T> results = execute();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private Object loadRelatedEntity(Class<?> joinType, JoinColumn joinColumn, Object joinValue) throws SQLException, ReflectiveOperationException {
        DataORM<?> relatedOrm = new DataORM<>(joinType);
        BaseQuery<?> joinQuery = relatedOrm.findById((String) joinValue);
        return joinQuery.findOne();
    }

    protected String getPrimaryKeyColumn(Field[] fields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return getIdColumnName(field);
            }
        }
        throw new IllegalStateException("No primary key column found");
    }

    /*private Object loadRelatedEntity(Class<?> relatedType, JoinTable joinTable, Object joinValue) throws SQLException, ReflectiveOperationException {
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
    }*/

    protected Object getFieldValue(Field field, Object entity) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(entity);
    }

    protected void setFieldValue(Field field, Object entity, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(entity, value);
    }

    protected String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.name();
        }
        if(field.isAnnotationPresent(Id.class)){
            Id id = field.getAnnotation(Id.class);
            return id.name();
        }
        if(field.isAnnotationPresent(JoinColumn.class)){
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.name();
        }
        return field.getName();
    }

    protected String getIdColumnName(Field field){
        if(field.isAnnotationPresent(Id.class)){
            Id id = field.getAnnotation(Id.class);
            return id.name();
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

    protected boolean hasCompositeKey() {
        return type.isAnnotationPresent(CompositeKey.class);
    }

    protected Map<String, Object> getCompositeKeyValues(Object entity) throws IllegalAccessException, NoSuchFieldException {
        Map<String, Object> keyValues = new HashMap<>();
        String[] keyColumns = getCompositeKeyColumns();
        for (String keyColumn : keyColumns) {
            Field field = type.getDeclaredField(keyColumn);
            field.setAccessible(true);
            keyValues.put(keyColumn, field.get(entity));
        }
        return keyValues;
    }

    private String[] getCompositeKeyColumns() {
        CompositeKey compositeKey = type.getAnnotation(CompositeKey.class);
        return compositeKey.keyColumns();
    }

    protected String buildCondition2(String fieldName, Object value) throws NoSuchFieldException {
        String[] nestedFields = fieldName.split("_");
        StringBuilder condition = new StringBuilder();
        Class<?> currentType = type;
        String currentTable = getTableName();

        for (int i = 0; i < nestedFields.length; i++) {
            Field field = currentType.getDeclaredField(nestedFields[i]);
            if (i < nestedFields.length - 1) {
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                    String joinTable = field.getType().getSimpleName();
                    joins.add("JOIN " + joinTable + " ON " + currentTable + "." + joinColumn.name() + " = " + joinTable + "." + getPrimaryKeyColumn(field.getType().getDeclaredFields()));
                    currentType = field.getType();
                    currentTable = joinTable;
                } else {
                    throw new NoSuchFieldException("Field " + nestedFields[i] + " is not a join column");
                }
            } else {
                condition.append(currentTable).append(".").append(getColumnName(field)).append(" = ?");
            }
        }

        return condition.toString();
    }

    protected Field getFieldByNameOrColumn(Class<?> type, String name) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(name) || getColumnName(field).equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;
    }
}
