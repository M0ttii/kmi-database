package com.github.m0ttii.database.orm;

import com.github.m0ttii.database.DatabaseConnection;
import com.github.m0ttii.database.annotations.*;
import com.github.m0ttii.database.orm.query.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//DataORM provides methods to interact with the Database.
public class DataORM<T> {

    //The Entity
    private Class<T> type;

    public DataORM(Class<T> type){
        this.type = type;
    }

    //Return the table name
    private String getTableName(){
        if(type.isAnnotationPresent(Entity.class)){
            Entity entity = type.getAnnotation(Entity.class);
            return entity.tableName();
        }
        return type.getSimpleName();
    }

    //Return the column name
    private String getColumnName(Field field){
        if(field.isAnnotationPresent(Column.class)){
            Column column = field.getAnnotation(Column.class);
            return column.name();
        }
        if (field.isAnnotationPresent(Id.class)) {
            Id id = field.getAnnotation(Id.class);
            return id.name();
        }
        return field.getName();
    }

    private String getIdColumnName(Field field){
        if(field.isAnnotationPresent(Id.class)){
            Id column = field.getAnnotation(Id.class);
            return column.name();
        }
        return field.getName();
    }

    //Generic method to find an object by a generic field
    //All "findBy..." methods defined in the Repository invoke this method with the field name after "findBy"
    public BaseQuery<T> findByField(String fieldName, Object value) {
        return new FindByFieldQuery<>(type, fieldName, value);
    }

    public BaseQuery<T> findByJoinField(String fieldName, Object value) {
        return new FindByJoinFieldQuery<>(type, fieldName, value);
    }

    //Inserts a new object into the database
    public void insert(Object obj) throws SQLException, IllegalAccessException {
        new InsertQuery<>(type, obj).save();
    }

    //Finds an object by id
    public FindByIdQuery<T> findById(Object id) {
        if (id instanceof String) {
            return new FindByIdQuery<>(type, id);
        } else if (id instanceof Map) {
            return new FindByIdQuery<>(type, (Map<String, Object>) id);
        } else {
            throw new IllegalArgumentException("Unsupported ID type");
        }
    }

    //Updates an object
    public void update(Object obj) throws SQLException, IllegalAccessException {
        if (hasCompositeKey()) {
            Map<String, Object> compositeId = getCompositeKeyValues(obj);
            new UpdateQuery<>(type, obj, compositeId).save();
        } else {
            new UpdateQuery<>(type, obj).save();
        }
    }

    //Deletes an object
    public void delete(Object id) throws SQLException {
        if (id instanceof Map) {
            new DeleteQuery<>(type, (Map<String, Object>) id).save();
        } else {
            new DeleteQuery<>(type, id).save();
        }
    }

    public <R> R executeCustomUpdateQuery(Class<R> returnType, String sql, Object... params) throws SQLException {
        return returnType.cast(executeUpdateQuery(sql, params));
    }

    public <R> R executeCustomSelectQuery(Class<R> returnType, String sql, Object... params) throws SQLException, ReflectiveOperationException {
        if (returnType.equals(List.class)) {
            return returnType.cast(executeCustomQueryList(sql, params));
        } else {
            return returnType.cast(executeCustomQuerySingle(sql, params));
        }
    }

    private int executeUpdateQuery(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        }
    }

    public FindAllQuery<T> findAll() {
        return new FindAllQuery<>(type);
    }

    private List<T> executeCustomQueryList(String sql, Object... params) throws SQLException, ReflectiveOperationException {
        List<T> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
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
        }
        return list;
    }

    private Object loadRelatedEntity(Class<?> joinType, JoinColumn joinColumn, Object joinValue) throws SQLException, ReflectiveOperationException {
        DataORM<?> relatedOrm = new DataORM<>(joinType);
        BaseQuery<?> joinQuery = relatedOrm.findById((String) joinValue);
        return joinQuery.findOne();
    }

    private T executeCustomQuerySingle(String sql, Object... params) throws SQLException, ReflectiveOperationException {
        T obj = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                obj = type.getDeclaredConstructor().newInstance();
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
            }
        }
        return obj;
    }

    private boolean hasCompositeKey() {
        return type.isAnnotationPresent(CompositeKey.class);
    }

    private Map<String, Object> getCompositeKeyValues(Object entity) throws IllegalAccessException {
        Map<String, Object> keyValues = new HashMap<>();
        String[] keyColumns = getCompositeKeyColumns();
        for (String keyColumn : keyColumns) {
            Field field = getFieldByColumnName(keyColumn);
            if (field == null) {
                throw new RuntimeException("Failed to get composite key field: " + keyColumn);
            }
            field.setAccessible(true);
            keyValues.put(keyColumn, field.get(entity));
        }
        return keyValues;
    }

    private Field getFieldByColumnName(String columnName) {
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                Id idAnnotation = field.getAnnotation(Id.class);
                String idName = idAnnotation.name();
                if (!idName.isEmpty() && idName.equals(columnName)) {
                    return field;
                } else if (field.getName().equals(columnName)) {
                    return field;
                }
            }
        }
        return null;
    }

    private String[] getCompositeKeyColumns() {
        CompositeKey compositeKey = type.getAnnotation(CompositeKey.class);
        return compositeKey.keyColumns();
    }
}
