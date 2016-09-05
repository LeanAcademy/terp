/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.gui.controllers;

import com.terp.data.dao.MenuSourceDao;
import com.terp.data.model.MenuSource;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author cevdet
 */
public class TerpMainFormController implements Initializable {
    
    @FXML
    private Button btnSlideMenu;

    @FXML
    private SplitPane spMainPane;

    @FXML
    private TextField txtSearchMenuItem;

    @FXML
    private Accordion acMainMenu;
    
    @FXML
    private AnchorPane apMainMenu;
    
    @FXML
    private AnchorPane apMainFrame;
    
    @FXML
    private AnchorPane apMainContent;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        // adding listener to divider
        Divider divider = this.spMainPane.getDividers().get(0);
        divider.positionProperty().addListener(this.dividerPositionChangeListener);
        
        
        // adding listener to width of anchorpane
        this.apMainFrame.widthProperty().addListener(this.paneWidthChangeListener);

        // load menu
        loadMenu();
        
    }

    private final ChangeListener<Number> dividerPositionChangeListener = new ChangeListener<Number>(){
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            txtSearchMenuItem.setText(newValue.toString());
            // set min width
            if(apMainMenu.getWidth() / apMainFrame.getWidth() < 0.3){
                
                
            }
        }
        
    };
    
    private ChangeListener<Number> paneWidthChangeListener = new ChangeListener<Number>(){
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            
        }
    };
    
    @FXML
    void btnSlideMenuOnAction(ActionEvent event) {
        // TODO : implement close button for side menu divider
    }
    
    private void loadMenu(){
        // TODO : load menu
        
        // read menu records
        MenuSourceDao menuSourceDao = new MenuSourceDao();
        String sql = "from MenuSource e where e.menuType=0"; // TODO : change this to manage main / child node
        List<MenuSource> list =  menuSourceDao.findAll(sql);
        
        //build accordion
        for(MenuSource item : list){
            
            // create titled pane
            TitledPane menuItem = new TitledPane();
            menuItem.setText(item.getMenuName());
            
            // find submenu items
            sql = "from MenuSource e where e.menuType = 1 and e.menuParent = " + item.getRowid();
            List<MenuSource> submenuList = menuSourceDao.findAll(sql);
            
            // check if menu item has submenu
            if(submenuList.size() > 0){
                
                // menu item has submenu
                // build submenu
                ObservableList<String> subMenuItems = FXCollections.observableArrayList();
                for(MenuSource subitem : submenuList){                     
                    subMenuItems.add(subitem.getMenuName());
                }
                ListView<String> subMenu = new ListView<>();
                subMenu.setItems(subMenuItems);
                AnchorPane content = new AnchorPane();
                content.getChildren().add(subMenu);
                AnchorPane.setLeftAnchor(subMenu, 0d);
                AnchorPane.setRightAnchor(subMenu, 0d);
                menuItem.setContent(content);
            }
            
            // add into accordion
            acMainMenu.getPanes().add(menuItem);
        }
        
    }
}
