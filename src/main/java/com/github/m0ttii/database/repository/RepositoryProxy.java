package com.github.m0ttii.database.repository;

import com.github.m0ttii.database.orm.DataORM;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//The RepositoryProxy class is a dynamic proxy handler that enables the use of repository interfaces for
//performing CRUD operations and custom queries without explicit implementation.
//It leverages Java's reflection and proxy capabilities to map method calls on the repository interfaces to the corresponding methods in the DataORM class.
public class RepositoryProxy<T> implements InvocationHandler {

    private final Class<T> repositoryInterface;
    private final Map<String, Method> methods = new HashMap<>();
    private final Map<Class<?>, DataORM<?>> ormInstances = new HashMap<>();

    public RepositoryProxy(Class<T> repositoryInterface){
        this.repositoryInterface = repositoryInterface;
        for (Method method : repositoryInterface.getMethods()){
            methods.put(method.getName(), method);
        }
    }

    //Handles method calls made on the dynamic proxy instance and delegates these calls to the appropriate methods in the DataORM instance based on the method name.
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Class<?> entityType = getEntityType();
            DataORM<?> orm = ormInstances.computeIfAbsent(entityType, k -> new DataORM<>(entityType));

            String methodName = method.getName();

            //Handling generic CRUD operations
            switch (methodName){
                case "insert":
                    orm.insert(args[0]);
                    return null;
                case "findById":
                    return orm.findById(args[0]);
                case "findAll":
                    return orm.findAll();
                case "update":
                    orm.update(args[0]);
                    return null;
                case "delete":
                    orm.delete(args[0]);
                    return null;
            }

            //Handling custom methods (e.g. findByName)
            if(methodName.startsWith("findBy")){
                String fieldName = methodName.substring(6);
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                if(fieldName.contains("_")){
                    if (method.getReturnType().equals(List.class)) {
                        return orm.findByJoinField(fieldName, args[0]).execute();
                    } else {
                        return orm.findByJoinField(fieldName, args[0]).findOne();
                    }
                }
                if (method.getReturnType().equals(List.class)) {
                    return orm.findByField(fieldName, args[0]).execute();
                } else {
                    return orm.findByField(fieldName, args[0]).findOne();
                }
            }  else if (methodName.startsWith("executeCustomQuery")) {
                String sql = (String) args[0];
                Object[] params = (Object[]) args[1];
                Class<?> returnType = method.getReturnType();
                if(returnType.equals(Integer.class) || returnType.equals(int.class) || returnType.equals(void.class)) {
                    return orm.executeCustomUpdateQuery(returnType, sql, params);
                }
                return orm.executeCustomSelectQuery(returnType, sql, params);
            }

            throw new UnsupportedOperationException("Method not implemented: " + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> repositoryInterface) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new RepositoryProxy<>(repositoryInterface)
        );
    }

    private Class<?> getEntityType() {
        Type[] genericInterfaces = repositoryInterface.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                if (parameterizedType.getRawType().equals(Repository.class)) {
                    return (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }
        throw new IllegalStateException("Could not determine entity type for repository: " + repositoryInterface.getName());
    }
}
