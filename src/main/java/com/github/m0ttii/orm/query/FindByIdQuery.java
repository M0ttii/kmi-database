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

        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = getColumnName(field);
            }
        }

        columns.setLength(columns.length() - 1);
        if (idColumn == null) {
            throw new IllegalStateException("No ID column found");
        }

        StringBuilder whereClause = new StringBuilder(" WHERE " + idColumn + " = ?");
        if (!conditions.isEmpty()) {
            whereClause.append(" AND ");
            for (String fieldName : conditions.keySet()) {
                whereClause.append(fieldName).append(" = ? AND ");
            }
            whereClause.setLength(whereClause.length() - 5);
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

    @Override
    public List<T> execute() throws SQLException, ReflectiveOperationException {
        conditions.put("id", id); // Ensure ID is part of the conditions
        return super.execute();
    }
}
