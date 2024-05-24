package com.github.m0ttii.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {
    String name();
    String joinColumn();
    String referencedColumnName();
}
