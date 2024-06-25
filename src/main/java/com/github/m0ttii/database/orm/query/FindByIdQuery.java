package com.github.m0ttii.database.orm.query;

import com.github.m0ttii.database.annotations.Id;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FindByIdQuery<T> extends BaseQuery<T> {
    private final Object idEntity;
    private final Map<String, Object> compositeId;

    public FindByIdQuery(Class<T> type, Object idEntity) {
        super(type);
        this.idEntity = idEntity;
        this.compositeId = null;
    }

    public FindByIdQuery(Class<T> type, Map<String, Object> compositeId) {
        super(type);
        this.idEntity = null;
        this.compositeId = compositeId;
    }

    @Override
    protected String buildSql() {
        String tableName = getTableName();
        Field[] fields = type.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder joinClause = new StringBuilder();

        for (String join : joins) {
            joinClause.append(join).append(" ");
        }

        for (Field field : fields) {
            columns.append(getColumnName(field)).append(",");
        }

        columns.setLength(columns.length() - 1);

        StringBuilder whereClause = new StringBuilder(" WHERE ");
        if (hasCompositeKey()) {
            try {
                Map<String, Object> keyValues = compositeId != null ? compositeId : getCompositeKeyValues(idEntity);
                for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                    whereClause.append(entry.getKey()).append(" = ? AND ");
                    conditions.put(entry.getKey(), entry.getValue());
                }
                whereClause.setLength(whereClause.length() - 5);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException("Failed to get composite key values", e);
            }
        } else {
            String idColumn = getPrimaryKeyColumn(fields);
            conditions.put(idColumn, idEntity);
            whereClause.append(idColumn).append(" = ?");
        }

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
