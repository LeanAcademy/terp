module terp {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires terp.plugin;
    requires java.logging;
    requires java.naming;
    requires java.sql;
    requires org.hibernate.orm.core;
    requires org.controlsfx.controls;
    requires java.prefs;
    requires java.base;
    requires jakarta.persistence;
    requires org.apache.derby.engine;
    requires org.apache.derby.commons;

    uses com.terp.plugin.IPlugin;
    uses java.sql.Driver;

    opens com.terp.main to javafx.fxml, org.controlsfx.controls;
    opens com.terp.gui.controllers to javafx.fxml;
    opens com.terp.data.model to org.hibernate.orm.core, javafx.base, javafx.fxml;
    opens com.terp.data to org.hibernate.orm.core, javafx.base, javafx.fxml;
    opens fxml;
    opens styles;

    exports com.terp.main;
}
