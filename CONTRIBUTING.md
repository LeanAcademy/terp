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
- In `IPlugin.run()`, after the host has set the menu manager, add toolbar buttons with `getMenuManager().addToolKit(button, menuId)` so the host can hide them when the user’s group lacks that program. Build the `Button` in the plugin (JavaFX + `IIconFactory`); do not introduce extra SPI helper classes for this — plugin JARs load in a `URLClassLoader` that cannot see new host-only types.
- Use `TerpApplication.getPersistence().createDao(MyEntity.class)` for plugin tables. Do not grow `IDatabaseFactory` with every new entity. Host entities (user, menu, plugin registry, group rights) stay in `terp`. Company and branch belong to `terp.core`. Current accounts belong to `terp.cari`; other plugins look them up with `TerpApplication.getAccountLookup()`. Stock quantity direction is `hareket_turu` / `StockDirection`; business meaning is `hareket_neden` via `IMovementReasonLookup`. Cross-plugin stock writes go through `IStockLedger.post` / `reverseByDocument` and `StockPosting` (codes + `sourceType`/`sourceId`), never concrete `Item` / `StockMovement`. Materials, warehouses, and reasons: `IItemLookup`, `IWarehouseLookup`, `IMovementReasonLookup`. Check `IUser.canOpen` / `FormRights.forMenu` for CRUD. Active company is `TerpApplication.getCurrentCompany()`. Stock, warehouse, movement, reason, cari, purchase-request, purchase-order, and purchase-receipt rows store `firma_ref` as a Long (`ICompanyScoped` / `CompanyScope`); do not `@ManyToOne` `Company`. Filter those lists with `CompanyScope.from`.

`terp.core` is the reference plugin (company/branch entities and forms). `terp.stok` is the stock plugin (material master, warehouses, movement reasons, movements, on-hand report, including per-partner item codes). `terp.cari` is the current-account plugin (one unique code per cari, used as customer and/or supplier). `terp.satin` is purchasing: request (`SAT03`) → order (`SAT04`) → goods receipt from order (`SAT05`) / receipt list (`SAT02`) and settings (`SAT06`). Convert an approved request into a draft order. Convert an approved order into a draft receipt (also from the SAT05 menu); remaining qty is the default and is editable up to the over-receipt percent. The order closes (`Kapalı`) when posted qty covers the ordered balance. Order approve does not post stock. Receipt approve posts inbound `ALIM`, cancel reverses. Build with `mvn -pl terp.core,terp.stok,terp.cari,terp.satin package` so JARs land in `terp/plugins/`. Restart TERP after a new plugin JAR so Hibernate maps new tables.

## Pull requests

1. Branch from `master`.
2. Keep changes focused (platform vs a single plugin).
3. Do not commit `target/`, Derby data under `terp/etc/terp/`, vendor JARs in `terp/lib/`, or a local `hibernate.properties`. JDBC driver JARs belong in `lib/`, not `etc/`.
4. Open a PR against [LeanAcademy/terp](https://github.com/LeanAcademy/terp).

The host is database-agnostic: JDBC drivers are loaded from the module path, `terp/lib/`, or `terp.jdbc.driver.jars`.
