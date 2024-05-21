package com.github.m0ttii.test;

import com.github.m0ttii.annotations.Column;
import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.Id;

@Entity(tableName = "employees")
public class EmployeeEntity {

    @Id
    public int emp_id;

    @Column(name = "name")
    public String name;

    @Column(name = "position")
    public String position;

    @Column(name = "salary")
    public double salary;
}
