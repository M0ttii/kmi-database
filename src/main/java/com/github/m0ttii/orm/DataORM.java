package com.github.m0ttii.orm;

import com.github.m0ttii.DatabaseConnection;
import com.github.m0ttii.annotations.Column;
import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.Id;
import com.github.m0ttii.orm.query.FindAllQuery;
import com.github.m0ttii.orm.query.FindByIdQuery;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        return field.getName();
    }

    //Generic method to find an object by a generic field
    //All "findBy..." methods defined in the Repository invoke this method with the field name after "findBy"
    public List<T> findByField(String fieldName, Object value) throws SQLException, ReflectiveOperationException {
        List<T> list = new ArrayList<>();
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        String targetColumn = null;
        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
            if (field.getName().equals(fieldName)) {
                targetColumn = getColumnName(field);
            }
        }
        columns.setLength(columns.length() - 1);

        if (targetColumn == null) {
            throw new IllegalStateException("No column found for field: " + fieldName);
        }

        String sql = "SELECT " + columns + " FROM " + tableName + " WHERE " + targetColumn + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, value);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                T obj = type.getDeclaredConstructor().newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);
                    field.set(obj, rs.getObject(getColumnName(field)));
                }
                list.add(obj);
            }
        }
        return list;
    }

    //Inserts a new object into the database
    public void insert(Object obj) throws SQLException, IllegalAccessException {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Id.class)) {
                columns.append(getColumnName(field)).append(",");
                values.append("?,");
            }
        }
        columns.setLength(columns.length() - 1);
        values.setLength(values.length() - 1);

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    pstmt.setObject(index++, field.get(obj));
                }
            }
            pstmt.executeUpdate();
        }
    }

    //Finds an object by id
    public FindByIdQuery<T> findById(int id) {
        return new FindByIdQuery<>(type, id);
    }

    //Updates an object
    public void update(Object obj) throws SQLException, IllegalAccessException {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        String idColumn = null;
        Object idValue = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = getColumnName(field);
                field.setAccessible(true);
                idValue = field.get(obj);
            } else {
                columns.append(getColumnName(field)).append(" = ?,");
            }
        }
        columns.setLength(columns.length() - 1); // Entfernt das letzte Komma

        if (idColumn == null) {
            throw new IllegalStateException("No ID column found");
        }

        String sql = "UPDATE " + tableName + " SET " + columns + " WHERE " + idColumn + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    pstmt.setObject(index++, field.get(obj));
                }
            }
            pstmt.setObject(index, idValue);
            pstmt.executeUpdate();
        }
    }

    //Deletes an object
    public void delete(int id) throws SQLException {
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

        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public FindAllQuery<T> findAll() {
        return new FindAllQuery<>(type);
    }
}
