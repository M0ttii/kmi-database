package com.github.m0ttii.repository;

import com.github.m0ttii.orm.DataORM;

import java.util.List;

//Generic Repository interface that provides the basic CRUD operations
public interface Repository <T, ID>{
    void insert(T entity);
    T findById(ID id);
    DataORM.Query findAll();
    void update(T entity);
    void delete(ID id);
}
