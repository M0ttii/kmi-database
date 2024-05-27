package com.github.m0ttii.test;

import com.github.m0ttii.annotations.Column;
import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.Id;
import com.github.m0ttii.annotations.JoinTable;

@Entity(tableName = "kunde")
public class KundeEntity {

    @Id
    public int kunden_nr;

    @Column(name = "name")
    public String name;

    @Column(name = "vorname")
    public String vorname;

    @Column(name = "email")
    public String email;

    @Column(name = "telefon")
    public String telefon;

    @JoinTable(name = "adresse", joinColumn = "adresse_id", referencedColumnName = "id")
    @Column(name = "adresse_id")
    public AdresseEntity adresse;
}
