# ORM-Framework

Dieses Framework wurde im Rahmen eines Universitätsprojekts entwickelt. Es ist keineswegs produktionsreif, vollständig oder performant und dient ausschließlich zu Lernzwecken.

## Was ist ein ORM?

Ein Object-Relational Mapping Framework (ORM) ist ein Programmierkonzept, das zur Vereinfachung der Datenbankinteraktion verwendet wird. Es ermöglicht Entwicklern, Datenbankabfragen und -operationen in der jeweiligen Programmiersprache durchzuführen, ohne direkt SQL schreiben zu müssen. Ein ORM übersetzt die Objekte im Code in Datenbanktabellen und umgekehrt, wodurch die Datenbankzugriffe abstrahiert und vereinfacht werden. Dadurch wird die Entwicklung effizienter und der Code leichter wartbar, da komplexe SQL-Abfragen in verständlichere Programmanweisungen umgewandelt werden.

## 1. Datenbankverbindung konfigurieren

Tragt euren Nutzernamen und Ihr Passwort in die `DatabaseConnection` Klasse ein, um die Verbindung zur Datenbank herzustellen.

```java
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://159.69.241.119:3306/dev<YOUR_DEV_NUMBER>_db";
    private static final String USER = "dev<YOUR_DEV_NUMBER>";
    private static final String PASSWORD = "dev<YOUR_DEV_NUMBER>_password";
}
```

## 2. Entity-Klassen erstellen

Für jede Datenbank-Entität wird eine Entity-Klasse erstellt. Diese Klasse nutzt folgende Annotationen:

- `@Entity(tableName = "kunde")`: Markiert die Klasse als Datenbank-Entität und weist auf die entsprechende Tabelle hin.
- `@Id(name = "name")`: Kennzeichnet das Primärschlüssel-Feld. Name ist der Spaltenname in der Datenbank.
- `@Column(name = "name")`: Weist auf die entsprechende Spalte in der Datenbank hin.
- `@JoinColumn(name = "adresse_nr", referencedColumnName = "adresse_nr")`: Verknüpft das Attribut mit einer anderen Tabelle durch eine Fremdschlüsselbeziehung.
- `@CompositeKey(keyColumns = {"lager_id", "artikel_nr"})`: Definiert den zusammengesetzten Primärschlüssel

**:warning: Achtung: Jede Entity-Klasse <ins>MUSS</ins> einen No-Args-Konstruktor beinhalten.** 

### Beispielklasse

```java
@Entity(tableName = "kunde")
public class KundeEntity {

    @Id(name = "kunden_nr")
    private int id;

    @Column(name = "name")
    private String name;

    @JoinColumn(name = "adresse_nr", referencedColumnName = "adresse_nr")
    private AdresseEntity adresse;

    //No-Args-Konstruktor <-- Wird benötigt
    public KundeEntity(){
    }

    // Getter und Setter
}
```

### Beispiel für zusammengesetzte Primärschlüssel

```java
@Entity(tableName = "lagerbestand")
@CompositeKey(keyColumns = {"lager_id", "artikel_nr"})
public class Lagerbestand {

    @Id(name = "artikel_nr")
    private String artikelNr;

    @Id(name = "lager_nr")
    private String lagerNr;

    @Column(name = "menge")
    private int mengge;

    //No-Args-Konstruktor <-- Wird benötigt
    public Lagerbestand(){
    }

    // Getter und Setter
}
```

## 3. Repository-Interface erstellen

Für jede Entität wird ein Repository-Interface erstellt. Dieses Interface dient als Abstraktionsebene für Datenbankoperationen und ermöglicht es, CRUD-Operationen (Create, Read, Update, Delete) durchzuführen.

```java
public interface KundeRepository extends Repository<KundeEntity, String> {
}
```

## 4. Verwendung des Repositorys

Um Daten zu manipulieren, wird eine Instanz des Repositories erstellt.

```java
KundeRepository kundeRepository = RepositoryProxy.newInstance(KundeRepository.class);
```

## 5. Standardmethoden im Repository

Das Repository bietet folgende Standardmethoden an:

- `insert(T entity)`: Fügt eine neue Entität in die Datenbank ein.
- `findById(ID id)`: Findet eine Entität anhand ihrer ID.
- `findAll()`: Gibt alle Entitäten zurück.
- `update(T entity)`: Aktualisiert eine bestehende Entität.
- `delete(ID id)`: Löscht eine Entität anhand ihrer ID.

### Hilfsmethoden

Um WHERE-Bedingungen oder JOIN-Anweisungen manuell nutzen zu können, gibt es die `.where()` und `.join()` Methoden. Diese können bei allen `find...()` Methoden benutzt werden.

### Beispiel: Einfache Abfrage

```java
KundeEntity kundeEntity = kundeRepository.findById("2").findOne();
```

### Beispiel: Einfügen einer neuen Entität

```java
KundeEntity kundeEntity = new KundeEntity();
kundeEntity.setName("Kunde");
kundeRepository.insert(kundeEntity);
```

### Beispiel: Find mit where

```java
AdresseEntity adresseEntity = adresseRepository.findAll().where("stadt", "Berlin");
```

### Beispiel: Zusammengesetzte Primärschlüssel

```java
LagerbestandRepository lagerbestandRepository = RepositoryProxy.newInstance(LagerbestandRepository.class);
Map<String, Object> compositeKeys = new HashMap<>();
compositeKeys.put("artikel_nr", "1");
compositeKeys.put("lager_nr", "1");
Lagerbestand lagerbestand = lagerbestandRepository.findById(compositeKeys).findOne();
```

### Beispiel: Custom Query

```java
String sql = "SELECT * FROM kunde WHERE name = ?";
KundeEntity kunde = kundeRepository.executeCustomQuery(sql, "Klaus");
```

## 6. Verwendung von Java Dynamic Proxy

Das ORM nutzt Java Dynamic Proxy, um Interface-Methoden in funktionierende Methoden umzuwandeln. Definiere dazu eine Methode im jeweiligen Interface:

```java
public interface KundeRepository extends Repository<KundeEntity, String> {
    public KundeEntity findByName(String name);
}
```

Nun kannst du Kunden anhand des Namens finden:

```java
KundeEntity kundeEntity = kundeRepository.findByName("Kunde");
```

Diese formatierte und strukturierte Anleitung soll Ihnen helfen, das ORM-Framework besser zu verstehen und effizient zu nutzen.
