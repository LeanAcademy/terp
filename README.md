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

Default database is **Apache Derby** embedded at `terp/etc/terp`. The Derby JDBC driver comes from Maven; no `/opt/derby` install is required. `hibernate.hbm2ddl.auto` is `update` so the schema is not wiped on each start.

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

T, eklenti tabanlı açık kaynak bir ERP iskeletidir. Java 17 ve Maven ile `mvn package` ardından `mvn -pl terp javafx:run` yeterlidir. Eklenti JAR dosyalarını `terp/plugins/` altına koyun.
