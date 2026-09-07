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
- Put the built JAR in `terp/plugins/`, or install it from **Eklenti yükleme** and restart TERP.
- Return Hibernate entity classes from `IPlugin.getPersistentClasses()`. The host scans plugins before it builds the SessionFactory.
- Declare menus with `IPlugin.getMenus()`. The host writes missing `menu` rows (by `menu_kodu`) when it binds the plugin.
- In `IPlugin.run()`, after the host has set the menu manager, add toolbar buttons with `getMenuManager().addToolKit(button)`. Build the `Button` in the plugin (JavaFX + `IIconFactory`); do not introduce extra SPI helper classes for this — plugin JARs load in a `URLClassLoader` that cannot see new host-only types.
- Use `TerpApplication.getPersistence().createDao(MyEntity.class)` for plugin tables. Do not grow `IDatabaseFactory` with every new entity. Host entities (user, menu, plugin registry) stay in `terp`. Company and branch belong to `terp.core`. Current accounts belong to `terp.cari`; other plugins look them up with `TerpApplication.getAccountLookup()`.

`terp.core` is the reference plugin (company/branch entities and forms). `terp.stok` is the stock plugin (material master, warehouses, movements, on-hand report, including per-partner item codes). `terp.cari` is the current-account plugin (one unique code per cari, used as customer and/or supplier). Build with `mvn -pl terp.core,terp.stok,terp.cari package` so JARs land in `terp/plugins/`.

## Pull requests

1. Branch from `master`.
2. Keep changes focused (platform vs a single plugin).
3. Do not commit `target/`, Derby data under `terp/etc/terp/`, vendor JARs in `terp/lib/`, or a local `hibernate.properties`. JDBC driver JARs belong in `lib/`, not `etc/`.
4. Open a PR against [LeanAcademy/terp](https://github.com/LeanAcademy/terp).

The host is database-agnostic: JDBC drivers are loaded from the module path, `terp/lib/`, or `terp.jdbc.driver.jars`.
