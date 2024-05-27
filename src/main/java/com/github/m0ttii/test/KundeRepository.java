package com.github.m0ttii.test;

import com.github.m0ttii.repository.Repository;

public interface KundeRepository extends Repository<KundeEntity, Integer> {
    public KundeEntity findByName(String name);
}
