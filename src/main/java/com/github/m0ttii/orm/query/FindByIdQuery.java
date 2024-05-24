package com.github.m0ttii.orm.query;

import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.Id;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

public class FindByIdQuery<T> extends BaseQuery<T> {
    private final Object id;

    public FindByIdQuery(Class<T> type, Object id) {
        super(type);
        this.id = id;
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        String idColumn = null;
        StringBuilder joinClause = new StringBuilder();

        for (String join : joins) {
            joinClause.append(join).append(" ");
        }

        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = getColumnName(field);
                String x = idColumn;
            }
        }

        columns.setLength(columns.length() - 1);
        if (idColumn == null) {
            throw new IllegalStateException("No ID column found");
        }

        conditions.put(idColumn, id);

        StringBuilder whereClause = new StringBuilder(" WHERE ");
        for (String fieldName : conditions.keySet()) {
            whereClause.append(fieldName).append(" = ? AND ");
        }
        whereClause.setLength(whereClause.length() - 5);


        return "SELECT " + columns + " FROM " + tableName + " " + joinClause.toString() + whereClause.toString();
    }

    @Override
    public T findOne() throws ReflectiveOperationException, SQLException {
        return super.findOne();
    }


    @Override
    public List<T> execute() throws SQLException, ReflectiveOperationException {
        return super.execute();
    }
}
