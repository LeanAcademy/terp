/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.gui.controllers;

import com.terp.data.model.MenuSource;
import com.terp.gui.MenuItem;
import com.terp.plugin.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author cevdet
 */
public class TerpMainFormController implements Initializable, 
        IMenuManager, IDesktopManager, IStatusbarManager {
    
    ///////////////////////////////////////////////////////////////////////////
    // FXML defined variables
    ///////////////////////////////////////////////////////////////////////////
   
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
    private AnchorPane apMainMenu;

    @FXML
    private TabPane tpDesktopContainer;
    
    @FXML
    private Label lblStatusText;
    
    @FXML
    private ImageView imgStatus;
    
    @FXML
    private ToolBar tbMainToolBar;
    
    @FXML
    private ToggleButton tbtnShowHideMenu;
    
    @FXML
    private Button btnRunProgram;

    @FXML
    private ToggleButton tbtnPopupMenu;
    
    @FXML
    private void tvMainMenuOnMouseClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            
            // get selected menu item 
            MenuItem selectedItem = (MenuItem) tvMainMenu.getSelectionModel().getSelectedItem();
          
            // find out if there is program for it
            if(selectedItem != null){
                if(selectedItem.getCurrentItem().getIsPlugin() == 0){
                    loadProgram(selectedItem.getCurrentItem().getProgramName());
                } else if (selectedItem.getCurrentItem().getIsPlugin() == 1 ){
                    loadProgram(selectedItem.getCurrentItem().getProgramName(),
                            selectedItem.getCurrentItem().getPluginId());
                }
            }
        }
    }
    
    @FXML
    private void btnRunProgramOnAction(ActionEvent actionEvent){
        
    }
    
    /**
     * handle tbtnShowHideMenu onAction event.
     * @param actionEvent 
     */
    @FXML
    private void tbtnShowHideMenuOnAction(ActionEvent actionEvent){
        if(this.tbtnShowHideMenu.selectedProperty().get()){
            
            // show menu
            this.spMainPane.getItems().add(0, this.apMainMenu);
            // TODO : set divider position to it's default (saved) value
            
        } else {
            
            // hide menu
            this.spMainPane.getItems().remove(this.apMainMenu);
        }
    }
    
    
    @FXML
    private void tbPopupMenuOnAction(ActionEvent event){
       if(this.tbtnPopupMenu.selectedProperty().get()){
           
           // show popup menu
           this.popOverMenu.show((Node) event.getSource());
           
       }else{
           this.popOverMenu.hide();
       }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Private variables and routines
    ///////////////////////////////////////////////////////////////////////////    
    
    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(
            TerpMainFormController.class.getName());
    
    /**
     * Primary stage. It is used to change application title
     */
    private Stage primaryStage;

    /**
     * Suggessions for search box
     */
    private SortedSet<String> entries;
    
    /**
     * popup menu
     */
    private PopOver popOverMenu;
    
    /**
     * Event handler for adjusting of divider position
     */
    private final ChangeListener<Number> paneWidthChangeListener = 
            new ChangeListener<Number>(){
        @Override
        public void changed(ObservableValue<? extends Number> observable, 
                Number oldValue, Number newValue) {
            Divider divider = spMainPane.getDividers().get(0);
            Double position = divider.getPosition();
                      
            divider.positionProperty().set(
                    position * oldValue.doubleValue()/newValue.doubleValue());
         
        }
    };
    
    /**
     * Event handler for search text field to manage run button
     */
    private final ChangeListener<String> txtSearchChangeListener =
            new ChangeListener<String>(){
        @Override
        public void changed(ObservableValue<? extends String> observable, 
                String oldValue, String newValue) {
            if(txtSearchMenuItem.getText().length() > 0){
                btnRunProgram.setDisable(false);
            } else {
                btnRunProgram.setDisable(true);
            }
        }
                
    };    
    
    /**
     * load main menus
     */
    private void loadMenu(){
        
        // update status
        this.updateStatus("Loading menu", null);
        
        // create root node
        TreeItem rootNode = new TreeItem("Ana menü");
        rootNode.setExpanded(true);
        
        // read menu records
        MenuSource menuSource = new MenuSource();
        String sql = "from MenuSource e where e.menuType=0"; 
        List<Object> list =  menuSource.findAll(sql);
        
        //build tree menu
        for(Object item : list){
            
            // create titled pane
            MenuItem menuLeaf = new MenuItem((MenuSource) item);
            rootNode.getChildren().add(menuLeaf);
            
        }
        
        // add into tree view
        tvMainMenu.rootProperty().set(rootNode);
        
        // load all programs for search text field
        String sql2 = "from MenuSource e where e.programName is not null";
        List<Object> list2 = menuSource.findAll(sql2);
        for(Object item2 : list2){
            entries.add(((MenuSource)item2).getMenuId() + " - " 
                    + ((MenuSource)item2).getMenuName());
        }
        
        // update status
        this.updateStatus("Menu loaded!", null);
    }

    /**
     * load program into destop.
     * @param program
     * @param isPlugin 
     */
    private void loadProgram(String program) {
        
        // load program related to menu
        try{
            // load fxml program
            Node nodeProgram = FXMLLoader.load(getClass().getResource(
                    "/fxml/" + program + ".fxml"));
            this.addToDesktop(nodeProgram, program);           
            
        }catch(Exception e){
            LOG.log(Level.SEVERE, null,e);
        }
    }
    
    /**
     * load program from plugin.
     * @param program
     * @param pluginId 
     */
    private void loadProgram(String program, Long pluginId){
        
        // load program from plugin related to menu
        TerpApplication terpApp = TerpApplication.getInstance();
        IPlugin plg = terpApp.getPluginFactory().getPlugin(pluginId);
        plg.loadProgram(program);
           
    }
    
    private void createPopup(){
        
        try {
            // load popup menu fxml
            Node node = FXMLLoader.load(getClass().getResource("/fxml/PopupMenu.fxml"));
            
            this.popOverMenu = new PopOver();
            this.popOverMenu.setOnAutoHide(new EventHandler<Event>(){
                @Override
                public void handle(Event event) {
                    tbtnPopupMenu.setSelected(false);
                }
            });
            this.popOverMenu.setContentNode(node);
            this.popOverMenu.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            this.popOverMenu.setCornerRadius(0);
            
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    //////////////////////////////////////////////////////////////////////////
    // public routines
    //////////////////////////////////////////////////////////////////////////
    
    
    //////////////////////////////////////////////////////////////////////////
    // Overrides
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * Initialize this controller and main frame.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // create application
        TerpApplication terpApp = TerpApplication.getInstance();
        
        // create sorted list for auto complete
        this.entries = new TreeSet<>();        
        
        // adding listener
        this.apMainFrame.widthProperty().addListener(this.paneWidthChangeListener);
        this.txtSearchMenuItem.textProperty().addListener(txtSearchChangeListener);
        
        // setting desktop manager to give plugins access
        terpApp.setDesktop(this);
        
        // setting menu manager to grant access
        terpApp.setMenuManager(this);
        
        // settin statusbar manager
        terpApp.setStatusbar(this);
        
        // load menu
        loadMenu();
        
        // create context menu for auto complete
        TextFields.bindAutoCompletion(txtSearchMenuItem, entries);
        
        // create popup menu
        createPopup();
        
    }

    @Override
    public void addToolKit(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addToDesktop(Node node, String headerText) {
        if (node != null){
            Tab newProgramContainer = new Tab();
            newProgramContainer.setContent(node);
            newProgramContainer.setText(headerText);            
            this.tpDesktopContainer.getTabs().add(newProgramContainer);
        }
    }

    @Override
    public void updateStatus(String status, Image img) {
        this.lblStatusText.setText(status);
        this.imgStatus.setImage(img);
    }

    @Override
    public void updateUser(String userName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateCompany(String company) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateDatabase(String database) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    /**
     * set primary stage of this class
     * @param stage 
     */
    
    @Override
    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }    
}
