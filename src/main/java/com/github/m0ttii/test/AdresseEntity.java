package com.github.m0ttii.test;

import com.github.m0ttii.annotations.Column;
import com.github.m0ttii.annotations.Entity;
import com.github.m0ttii.annotations.Id;
import com.github.m0ttii.annotations.JoinTable;

@Entity(tableName = "adresse")
public class AdresseEntity {

    @Id
    public int id;

    @Column(name = "strasse")
    public String strasse;

    @Column(name = "hausnummer")
    public String hausnummer;

    @Column(name = "postleitzahl")
    public String postleitzahl;

    @Column(name = "stadt")
    public String stadt;

    @Column(name = "land")
    public String land;
}
