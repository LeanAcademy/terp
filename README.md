# TERP

T is a modular open-source ERP. The JavaFX **host** loads business features from plugin JARs; plugins depend only on the public SPI.

GPL-2.0. Source: [github.com/LeanAcademy/terp](https://github.com/LeanAcademy/terp)

## Modules

| Module | Role |
| --- | --- |
| `terp.plugin` | Public SPI: `IPlugin`, `TerpApplication`, desktop/menu/DAO contracts |
| `terp` | Host: login, desktop, Hibernate, plugin loader |
| `terp.core` | Reference plugin (company screens), installed as `plugins/terp.core.jar` |

Future business modules (stock, sales, finance, HR) should be separate plugin JARs. Do not add their screens to the host. Keep `IDatabaseFactory` in the SPI small; plugin-owned entities will get a dedicated registration SPI in a later phase.

## Requirements

- JDK 17
- Maven 3.8+

## Build and run

```bash
git clone https://github.com/LeanAcademy/terp.git
cd terp
cp terp/etc/hibernate.properties.example terp/etc/hibernate.properties
mvn package
mvn -pl terp javafx:run
```

The host uses `terp/` as the application home (`etc/` and `plugins/`). Override with `-Dterp.home=/path/to/terp`.

Any database with a **Java JDBC driver** can be used. Configure URL, dialect, user and password in `terp/etc/hibernate.properties`.

Drivers are discovered in this order:

1. JDBC 4 drivers already on the module path (Apache Derby is bundled for a local default)
2. `*.jar` files in `terp/lib/`
3. Extra JARs in `terp.jdbc.driver.jars` (comma-separated, also accepts the old `driver.jarfile.name` key)
4. Optional `hibernate.connection.driver_class` for older drivers without a JDBC 4 service file

Default sample setup is embedded Derby at `terp/etc/terp`. `hibernate.hbm2ddl.auto` is `update` so the schema is not wiped on each start. See `terp/etc/hibernate.properties.example` for PostgreSQL, MySQL, SQL Server and Oracle.

## Writing a plugin

1. Depend on `com.terp:terp.plugin`.
2. Implement `com.terp.plugin.IPlugin` (`getSystemVersion()` must be `1.0`).
3. Register it:

   `META-INF/services/com.terp.plugin.IPlugin`  
   `com.example.myplugin.Plugin`

4. Package a JAR and copy it to `terp/plugins/`.
5. Open screens with `IPlugin.loadProgram(fxmlName)` using FXML from the plugin JAR. Set the FXML class loader to the plugin class loader.

The host scans `plugins/*.jar` with `URLClassLoader` + `ServiceLoader`. It does not use a Java agent.

## Türkçe

T, eklenti tabanlı açık kaynak bir ERP iskeletidir. Java 17 ve Maven ile `mvn package` ardından `mvn -pl terp javafx:run` yeterlidir. Eklenti JAR dosyalarını `terp/plugins/` altına koyun. JDBC sürücü JAR dosyalarını `terp/lib/` altına koyup `hibernate.properties` içinde URL ve dialect ayarlayın; herhangi bir Java sürücülü veritabanı kullanılabilir.
