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
- Put the built JAR in `terp/plugins/`, or add/remove it from **Eklenti yönetimi** and restart TERP. Remove deletes the JAR and `eklenti` row; menus and plugin tables stay. Core plugins (`getType() == 1`) cannot be removed.
- Return Hibernate entity classes from `IPlugin.getPersistentClasses()`. The host scans plugins before it builds the SessionFactory.
- Declare menus with `IPlugin.getMenus()`. The host writes missing `menu` rows (by `menu_kodu`) when it binds the plugin.
- Do not add program shortcuts to the main toolbar; users open screens from the menu tree. The host toolbar has **Yazdır** and **Excel'e aktar** for the active tab’s tables. Extra toolbar controls are still possible with `getMenuManager().addToolKit(button, menuId)`.
- Use `TerpApplication.getPersistence().createDao(MyEntity.class)` for plugin tables. Do not grow `IDatabaseFactory` with every new entity. Host entities (user, menu, plugin registry, group rights) stay in `terp`. Company and branch belong to `terp.core`. Current accounts belong to `terp.cari`; other plugins look them up with `TerpApplication.getAccountLookup()`. Cross-plugin current-account writes go through `IAccountLedger.post` / `reverseByDocument` and `AccountPosting` (codes + `sourceType`/`sourceId`), never concrete `Cari` / `AccountMovement`. Stock quantity direction is `hareket_turu` / `StockDirection`; business meaning is `hareket_neden` via `IMovementReasonLookup`. Cross-plugin stock writes go through `IStockLedger.post` / `reverseByDocument` and `StockPosting` (codes + `sourceType`/`sourceId`), never concrete `Item` / `StockMovement`. Materials, warehouses, and reasons: `IItemLookup`, `IWarehouseLookup`, `IMovementReasonLookup`. VAT rates: `IVatRateLookup`. Document numbers: `IDocumentNumbers` / `DocumentNumbers.assign` (company + type series in `terp.core`); stamp on draft save when the field is blank. Check `IUser.canOpen` / `FormRights.forMenu` for CRUD. Active company is `TerpApplication.getCurrentCompany()`. Stock, warehouse, movement, reason, cari, account-movement, purchase-request, purchase-order, purchase-receipt, purchase-invoice, stock-count, sales-order, sales-delivery, sales-invoice, sales-quote, production-recipe, vat-rate, and document-series rows store `firma_ref` as a Long (`ICompanyScoped` / `CompanyScope`); do not `@ManyToOne` `Company`. Filter those lists with `CompanyScope.from`.

`terp.core` is the reference plugin (company/branch entities, document number series `SYS07` / `SeriesForm` / `belge_seri`, VAT rates `SYS08` / `VatRateForm` / `kdv_oran` with Turkish 0/1/10/20 seeds). `terp.stok` is the stock plugin (material master, warehouses, movements, stock count document, on-hand report, including per-partner item codes). `terp.cari` is the current-account plugin (one unique code per cari as customer and/or supplier; movements `CRI03` / `AccountMovementForm` / `cari_hareket`; balance `CRI04` / `AccountStatusForm`). Other plugins post debit/credit through `IAccountLedger`. `terp.uretim` is the production plugin (recipe `URT02` / `RecipeForm` / `uretim_recete`: one BOM per semi/finished item; production voucher later). Receipt approve posts inbound `ALIM` only; cancel reverses stock (and leftover `SATIN_KABUL` cari from older rows). Purchase invoice approve posts a supplier credit (`SATIN_FATURA`) including VAT, not stock; cancel reverses the cari posting. The receipt status becomes **Faturalandı** when posted invoice qty covers every line. Sales invoice approve posts a customer debit (`SATIS_FATURA`) including VAT, not stock; cancel reverses the cari posting. The delivery status becomes **Faturalandı** when posted invoice qty covers every line. Standalone receipts, deliveries, and invoices remain allowed. Stock count (`STK07` / `CountForm`) approve posts `SAYIM_ART` / `SAYIM_AZ` for counted minus live on-hand; cancel reverses; STK04 manual count lines stay unbound. Draft save of request, order, receipt, purchase invoice, stock count, sales quote, sales order, sales delivery, sales invoice, and manual cari movement allocates a number from the company series when the field is empty. Build with `mvn -pl terp.core,terp.stok,terp.cari,terp.satin,terp.satis,terp.uretim package` so JARs land in `terp/plugins/`. Restart TERP after a new plugin JAR so Hibernate maps new tables.

## Pull requests

1. Branch from `master`.
2. Keep changes focused (platform vs a single plugin).
3. Do not commit `target/`, Derby data under `terp/etc/terp/`, vendor JARs in `terp/lib/`, or a local `hibernate.properties`. JDBC driver JARs belong in `lib/`, not `etc/`.
4. Open a PR against [LeanAcademy/terp](https://github.com/LeanAcademy/terp).

The host is database-agnostic: JDBC drivers are loaded from the module path, `terp/lib/`, or `terp.jdbc.driver.jars`.
