package com.github.m0ttii.repository;

import com.github.m0ttii.orm.DataORM;
import com.github.m0ttii.orm.query.FindAllQuery;
import com.github.m0ttii.orm.query.FindByIdQuery;

import java.util.List;

//Generic Repository interface that provides the basic CRUD operations
public interface Repository <T, ID>{
    void insert(T entity);
    FindByIdQuery<T> findById(ID id);
    FindAllQuery<T> findAll();
    void update(T entity);
    void delete(ID id);
}
