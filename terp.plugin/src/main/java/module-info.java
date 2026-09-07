/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module terp.plugin {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jakarta.persistence;
    
    opens com.terp.plugin.data;
    opens com.terp.plugin.gui;
    
    exports com.terp.plugin.data;
    exports com.terp.plugin.gui;
    exports com.terp.plugin.data.dao;
    exports com.terp.plugin.data.model;
    exports com.terp.plugin;
}
