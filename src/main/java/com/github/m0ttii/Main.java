package com.github.m0ttii;

import com.github.m0ttii.repository.RepositoryProxy;
import com.github.m0ttii.test.EmployeeEntity;
import com.github.m0ttii.test.EmployeeRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        EmployeeRepository employeeRepository = RepositoryProxy.newInstance(EmployeeRepository.class);

        try{
            List<EmployeeEntity> employeeEntities = employeeRepository.findAll();
            System.out.println(employeeEntities.get(0).name);
        }catch (Exception ex){
            System.out.println(ex);
        }
    }
}