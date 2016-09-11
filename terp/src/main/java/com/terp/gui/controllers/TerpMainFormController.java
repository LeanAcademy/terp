/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.gui.controllers;

import com.terp.data.dao.MenuSourceDao;
import com.terp.data.model.MenuSource;
import com.terp.gui.MenuItem;
import com.terp.plugin.IDesktopManager;
import com.terp.plugin.IMenuManager;
import com.terp.plugin.TerpApplication;
import com.terp.plugins.PluginManager;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javax.swing.JPanel;

/**
 *
 * @author cevdet
 */
public class TerpMainFormController implements Initializable, IMenuManager, IDesktopManager {
    
    ///////////////////////////////////////////////////////////////////////////
    // FXML defined variables
    ///////////////////////////////////////////////////////////////////////////
    
    @FXML
    private Button btnSlideMenu;

    @FXML
    private SplitPane spMainPane;

    @FXML
    private TextField txtSearchMenuItem;
    
    @FXML
    private TreeView tvMainMenu;
    
    @FXML
    private AnchorPane apMainFrame;
    
    @FXML
    private AnchorPane apMainContent;    
    
    @FXML
    void btnSlideMenuOnAction(ActionEvent event) {
        // TODO : implement close button for side menu divider
    }
    
    @FXML
    void tvMainMenuOnMouseClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            
            // get selected menu item 
            MenuItem selectedItem = (MenuItem) tvMainMenu.getSelectionModel().getSelectedItem();
          
            // find out if there is program for it
            if(selectedItem != null){
                loadProgram(selectedItem.getCurrentItem().getProgramName(),
                        selectedItem.getCurrentItem().getIsPlugin());
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Private variables and routines
    ///////////////////////////////////////////////////////////////////////////
    
    private final ChangeListener<Number> dividerPositionChangeListener = 
            new ChangeListener<Number>(){
        @Override
        public void changed(ObservableValue<? extends Number> observable, 
                Number oldValue, Number newValue) {
            // set min width
        }
        
    };
    
    private ChangeListener<Number> paneWidthChangeListener = 
            new ChangeListener<Number>(){
        @Override
        public void changed(ObservableValue<? extends Number> observable, 
                Number oldValue, Number newValue) {
            
        }
    };
    
    /**
     * load main menus
     */
    private void loadMenu(){
        // TODO : load menu        
        // create root node
        TreeItem rootNode = new TreeItem("Ana menü");
        rootNode.setExpanded(true);
        
        // read menu records
        MenuSourceDao menuSourceDao = new MenuSourceDao();
        String sql = "from MenuSource e where e.menuType=0"; 
        List<MenuSource> list =  menuSourceDao.findAll(sql);
        
        //build accordion
        for(MenuSource item : list){
            
            // create titled pane
            MenuItem menuLeaf = new MenuItem(item);
            rootNode.getChildren().add(menuLeaf);
            
            // add into tree view
            tvMainMenu.rootProperty().set(rootNode);
        }
        
    }
    
    /**
     * load all plugins that setup in database 
     */
    private void loadPlugins(){
        
        PluginManager pm = new PluginManager();
        pm.loadAllPlugin();
        
    }

    private void loadProgram(String program, Integer isPlugin) {
        
        // load menu
        try{
            
            if(program != null){
                // menu found
                switch (isPlugin){
                    case 0:
                        // load fxml program
                        Node nodeProgram = FXMLLoader.load(getClass().getResource(
                                "/fxml/" + program + ".fxml"));
                        this.addToDesktop(nodeProgram);
                        break;
                    case 1:
                        // load plugin fxml program
                        break;
                    default:
                        break;
                }
            }
            
        }catch(Exception e){
            LOG.log(Level.SEVERE, null,e);
        }
    }
    
    private static final Logger LOG = Logger.getLogger(
            TerpMainFormController.class.getName());
    
    //////////////////////////////////////////////////////////////////////////
    // Overrides
    //////////////////////////////////////////////////////////////////////////
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // create application
        TerpApplication terpApp = TerpApplication.getInstance();
        
        // adding listener to divider
        Divider divider = this.spMainPane.getDividers().get(0);
        divider.positionProperty().addListener(this.dividerPositionChangeListener);
        
        
        // adding listener to width of anchorpane
        this.apMainFrame.widthProperty().addListener(this.paneWidthChangeListener);
        
        // setting desktop manager to give plugins access
        terpApp.setDesktop(this);
        
        // setting menu manager to grant access
        terpApp.setMenuManager(this);
        
        // load menu
        loadMenu();
        
        // load palugins
        loadPlugins();
        
    }
    
    @Override
    public void setButtonEnabled(String tabName, String btnName, 
            boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addToolKit(JPanel tb) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addToDesktop(Node node) {
        if (node != null){
            this.apMainContent.getChildren().removeAll();
            this.apMainContent.getChildren().add(node);
        }
    }
    
}
