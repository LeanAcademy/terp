# TERP

T is a modular open-source ERP. The JavaFX **host** loads business features from plugin JARs; plugins depend only on the public SPI.

GPL-2.0. Source: [github.com/LeanAcademy/terp](https://github.com/LeanAcademy/terp)

## Modules

| Module | Role |
| --- | --- |
| `terp.plugin` | Public SPI: `IPlugin`, `TerpApplication`, desktop/menu/DAO contracts |
| `terp` | Host: login, desktop, Hibernate, plugin loader |
| `terp.core` | Reference plugin: company/branch entities and screens (`plugins/terp.core.jar`) |
| `terp.stok` | Stock plugin (material, warehouse, movements, on-hand report), installed as `plugins/terp.stok.jar` |
| `terp.cari` | Current-account plugin (cari kartı); one code per account as customer and/or supplier (`plugins/terp.cari.jar`) |

Future business modules (sales, finance, HR) should be separate plugin JARs. Do not add their screens to the host. Keep `IDatabaseFactory` small (geography lookups); plugin-owned tables use `IPlugin.getPersistentClasses()` and `TerpApplication.getPersistence().createDao(...)`.

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

The host uses `terp/` as the application home. Override with `-Dterp.home=/path/to/terp`.

| Directory | Contents |
|-----------|----------|
| `etc/` | Config (`hibernate.properties`) and embedded Derby data (`etc/terp/`) |
| `lib/` | Extra JDBC driver JARs (PostgreSQL, MySQL, …). Not Derby: that comes from Maven. |
| `plugins/` | Plugin JARs (`terp.core.jar`, …) |

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

4. Package a JAR and copy it to `terp/plugins/`, or use **Sistem yönetimi → Eklenti yükleme** (`PluginInstaller`) in the host. Restart after install so Hibernate can map plugin entities.
5. Return entity classes from `IPlugin.getPersistentClasses()`. Declare menus with `IPlugin.getMenus()` (`PluginMenu.folder` / `PluginMenu.program`); the host inserts missing `menu` rows at bind time. Add toolbar buttons from `IPlugin.run()` with `getMenuManager().addToolKit(button)` after the main form exists (create the `Button` in the plugin). Open screens with `IPlugin.loadProgram(fxmlName)` using FXML from the plugin JAR. Set the FXML class loader to the plugin class loader. Access plugin tables with `TerpApplication.getInstance().getPersistence().createDao(MyEntity.class)` — do not add methods to `IDatabaseFactory`.

`terp.stok` is the stock plugin: material master (`ItemForm` / `malzeme`), warehouses (`WarehouseForm` / `depo`), stock movements (`MovementForm` / `stok_hareket`: in, out, transfer, count, scrap), and on-hand report (`StockStatusForm`). After `mvn -pl terp.stok package` the JAR is `terp/plugins/terp.stok.jar`. `terp.cari` owns the current-account master (`AccountForm` / `cari`). A material can store many supplier/customer item codes (`malzeme_cari_kod`); each row is one partner’s own stock code and name, chosen from `IAccountLookup`.

The host scans `plugins/*.jar` with `URLClassLoader` + `ServiceLoader`. It does not use a Java agent.

## Türkçe

T, eklenti tabanlı açık kaynak bir ERP iskeletidir. Java 17 ve Maven ile `mvn package` ardından `mvn -pl terp javafx:run` yeterlidir. Eklenti JAR dosyalarını `terp/plugins/` altına koyun veya Sistem yönetimi → Eklenti yükleme ile kurun; entity içeren eklentiler için uygulamayı yeniden başlatın. Cari kartı (`terp.cari`) her hesaba tek kod verir. Malzeme kartındaki tedarikçi/müşteri stok kodları ayrı tabloda, cari başına kod ve ad olarak tutulur. Stok eklentisi depo tanımı, giriş/çıkış/transfer/sayım/fire hareketleri ve stok durum raporunu içerir. JDBC sürücü JAR dosyalarını `terp/lib/` altına koyup `hibernate.properties` içinde URL ve dialect ayarlayın.
