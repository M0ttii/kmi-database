package com.github.m0ttii.orm.query;

import com.github.m0ttii.annotations.Entity;

import java.lang.reflect.Field;

public class FindAllQuery<T> extends BaseQuery<T> {

    public FindAllQuery(Class<T> type){
        super(type);
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();

        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
            if (conditions.containsKey(field.getName())) {
                whereClause.append(getColumnName(field)).append(" = ? AND ");
            }
        }

        columns.setLength(columns.length() - 1);
        if (whereClause.length() > 0) {
            whereClause.setLength(whereClause.length() - 5);
            whereClause.insert(0, " WHERE ");
        }

        return "SELECT " + columns + " FROM " + tableName + whereClause.toString();
    }

    private String getTableName() {
        if (type.isAnnotationPresent(Entity.class)) {
            Entity entity = type.getAnnotation(Entity.class);
            return entity.tableName();
        }
        return type.getSimpleName();
    }
}
