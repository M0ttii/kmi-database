package com.github.m0ttii.database.repository;

import com.github.m0ttii.database.orm.query.FindAllQuery;
import com.github.m0ttii.database.orm.query.FindByIdQuery;

//Generic Repository interface that provides the basic CRUD operations
public interface Repository <T, ID>{
    void insert(T entity);
    FindByIdQuery<T> findById(ID id);
    FindAllQuery<T> findAll();
    void update(T entity);
    void delete(ID id);
}
