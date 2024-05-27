package com.github.m0ttii.test;

import com.github.m0ttii.annotations.*;

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

    @Column(name = "adresse")
    public Integer adresseId;  // Feld für die Fremdschlüssel-ID

    @JoinColumn(name = "adresse", referencedColumnName = "id")
    public AdresseEntity adresse;
}
