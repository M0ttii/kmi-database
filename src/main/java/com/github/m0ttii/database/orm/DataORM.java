package com.github.m0ttii.database.orm;

import com.github.m0ttii.database.annotations.Column;
import com.github.m0ttii.database.annotations.Entity;
import com.github.m0ttii.database.orm.query.*;

import java.lang.reflect.Field;
import java.sql.*;

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
    public BaseQuery<T> findByField(String fieldName, Object value) {
        return new FindByFieldQuery<>(type, fieldName, value);
    }

    //Inserts a new object into the database
    public void insert(Object obj) throws SQLException, IllegalAccessException {
        new InsertQuery<>(type, obj).save();
    }

    //Finds an object by id
    public FindByIdQuery<T> findById(int id) {
        return new FindByIdQuery<>(type, id);
    }

    //Updates an object
    public void update(Object obj) throws SQLException, IllegalAccessException {
        new UpdateQuery<>(type, obj).save();
    }

    //Deletes an object
    public void delete(Object id) throws SQLException {
        new DeleteQuery<>(type, id).save();
    }

    public FindAllQuery<T> findAll() {
        return new FindAllQuery<>(type);
    }
}
