package com.github.m0ttii.test;

import com.github.m0ttii.annotations.Column;
import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.Id;

@Entity(tableName = "departments")
public class DepartmentEntity {

    @Id
    public int id;

    @Column(name = "name")
    public String name;
}
