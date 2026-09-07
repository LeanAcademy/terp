# Contributing to TERP

TERP is a modular JavaFX ERP. New business features belong in **plugin JARs**, not in the host.

## Setup

- JDK 17
- Maven 3.8+
- Copy `terp/etc/hibernate.properties.example` to `terp/etc/hibernate.properties`

```bash
mvn package
mvn -pl terp javafx:run
```

## Plugin contract

- Depend only on `terp.plugin` (`IPlugin`, `TerpApplication`, GUI and DAO interfaces).
- `getSystemVersion()` must match the host (`1.0`).
- Register the implementation in `META-INF/services/com.terp.plugin.IPlugin`.
- Put the built JAR in `terp/plugins/`.
- Do not grow `IDatabaseFactory` with every new entity. Host entities stay in `terp` for now; plugin-owned Hibernate entities will get a dedicated SPI later.

`terp.core` is the reference plugin (company forms).

## Pull requests

1. Branch from `master`.
2. Keep changes focused (platform vs a single plugin).
3. Do not commit `target/`, Derby data under `terp/etc/terp/`, vendor JARs in `terp/lib/`, or a local `hibernate.properties`.
4. Open a PR against [LeanAcademy/terp](https://github.com/LeanAcademy/terp).

The host is database-agnostic: JDBC drivers are loaded from the module path, `terp/lib/`, or `terp.jdbc.driver.jars`.
