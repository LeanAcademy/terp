/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.gui.controllers;

import com.terp.data.model.MenuSource;
import com.terp.gui.MenuItem;
import com.terp.plugin.*;
import com.terp.util.TerpProperties;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    
// <editor-fold defaultstate="collapsed" desc=" FXML variables and routines ">

    @FXML
    private SplitPane spMainPane; //main pane
    
    @FXML
    private TextField txtSearchMenuItem; //search box for programs
    
    @FXML
    private TreeView tvMainMenu; //tree view for main menu
    
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
        if (mouseEvent.getClickCount() == 2) {

            // get selected menu item 
            MenuItem selectedItem = (MenuItem) tvMainMenu.getSelectionModel().getSelectedItem();

            // find out if there is program for it
            if (selectedItem != null) {
                if (selectedItem.getCurrentItem().getIsPlugin() == 0) {
                    loadProgram(selectedItem.getCurrentItem().getProgramName());
                } else if (selectedItem.getCurrentItem().getIsPlugin() == 1) {
                    loadProgram(selectedItem.getCurrentItem().getProgramName(),
                            selectedItem.getCurrentItem().getPluginId());
                }
            }
        }
    }
    
    /**
     * find program and run it
     * @param actionEvent 
     */
    @FXML
    private void btnRunProgramOnAction(ActionEvent actionEvent) {
        //find program id
        String[] menuText = this.txtSearchMenuItem.getText().split("[-]+");
        String programName = menuText[0];
        
        //find program data
        MenuSource menuSource = new MenuSource();
        String sql = "from MenuSource e where e.menuId = '" + programName + "'"; 
        Object prog = menuSource.firstOrDefault(sql);
        
        //check if program is found
        assert(prog != null);
        
        //run program
        menuSource = (MenuSource)prog;
        if(menuSource.getIsPlugin() == 0){
            loadProgram(menuSource.getProgramName());
        }else if (menuSource.getIsPlugin() == 1) {
            loadProgram(menuSource.getProgramName(), menuSource.getPluginId());
        }
        
        //clear textfield
        this.txtSearchMenuItem.clear();
    }

    /**
     * handle tbtnShowHideMenu onAction event.
     *
     * @param actionEvent
     */
    @FXML
    private void tbtnShowHideMenuOnAction(ActionEvent actionEvent) {
        if (this.tbtnShowHideMenu.selectedProperty().get()) {

            //show menu
            this.spMainPane.getItems().add(0, this.apMainMenu);
            
            //set divider position to it's default (saved) value
            this.spMainPane.setDividerPosition(0, Double.parseDouble(
                    this.props.getProperty("terp.main.dividerpos")));
            //save properties
            this.props.setProperty("terp.main.showmainmenu", "true");
            
        } else {

            // hide menu
            this.spMainPane.getItems().remove(this.apMainMenu);
            
            //save properties
            this.props.setProperty("terp.main.showmainmenu", "false");
            
        }
    }
    
    @FXML
    private void tbPopupMenuOnAction(ActionEvent event) {
        if (this.tbtnPopupMenu.selectedProperty().get()) {

            // show popup menu
            this.popOverMenu.show((Node) event.getSource());
            
        } else {
            this.popOverMenu.hide();
        }
    }
// </editor-fold>
    
// <editor-fold defaultstate="collapsed" desc=" Private variables and routines ">
   
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
     * gui properties
     */
    private Properties props;
    
    /**
     * Event handler for adjusting of divider position
     */
    private final ChangeListener<Number> paneWidthChangeListener
            = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable,
                Number oldValue, Number newValue) {
            if(!spMainPane.getDividers().isEmpty()){
                Divider divider = spMainPane.getDividers().get(0);
                Double position = divider.getPosition();

                divider.positionProperty().set(
                        position * oldValue.doubleValue() / newValue.doubleValue());
            }
        }
    };
    
    private final ChangeListener<Number> splitDividerPostionListener 
            = new ChangeListener<Number>(){
        @Override
        public void changed(ObservableValue<? extends Number> observable, 
                Number oldValue, Number newValue) {
            props.setProperty("terp.main.dividerpos", newValue.toString());
            TerpProperties.getInstance().setViewProps(props);
        }
                
    };

    /**
     * Event handler for search text field to manage run button
     */
    private final ChangeListener<String> txtSearchChangeListener
            = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable,
                String oldValue, String newValue) {
            if (txtSearchMenuItem.getText().length() > 0) {
                btnRunProgram.setDisable(false);
            } else {
                btnRunProgram.setDisable(true);
            }
        }
        
    };

    /**
     * load main menus
     */
    private void loadMenu() {

        // update status
        this.updateStatus("Loading menu", null);

        // create root node
        TreeItem rootNode = new TreeItem("Ana menü");
        rootNode.setExpanded(true);

        // read menu records
        MenuSource menuSource = new MenuSource();
        String sql = "from MenuSource e where e.menuType=0";        
        List<Object> list = menuSource.findAll(sql);

        //build tree menu
        for (Object item : list) {

            // create titled pane
            MenuItem menuLeaf = new MenuItem((MenuSource) item);
            rootNode.getChildren().add(menuLeaf);
            
        }

        // add into tree view
        tvMainMenu.rootProperty().set(rootNode);

        // load all programs for search text field
        String sql2 = "from MenuSource e where e.programName is not null";
        List<Object> list2 = menuSource.findAll(sql2);
        for (Object item2 : list2) {
            this.entries.add(((MenuSource) item2).getMenuId() + " - "
                    + ((MenuSource) item2).getMenuName());
        }

        // update status
        this.updateStatus("Menu loaded!", null);
    }

    /**
     * load program into destop.
     *
     * @param program
     * @param isPlugin
     */
    private void loadProgram(String program) {

        // load program related to menu
        try {
            // load fxml program
            Node nodeProgram = FXMLLoader.load(getClass().getResource(
                    "/fxml/" + program + ".fxml"));
            this.addToDesktop(nodeProgram, program);            
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * load program from plugin.
     *
     * @param program
     * @param pluginId
     */
    private void loadProgram(String program, Long pluginId) {

        // load program from plugin related to menu
        TerpApplication terpApp = TerpApplication.getInstance();
        IPlugin plg = terpApp.getPluginFactory().getPlugin(pluginId);
        plg.loadProgram(program);
        
    }

    /**
     * create popup window
     */
    private void createPopup() {
        
        try {
            // load popup menu fxml
            Node node = FXMLLoader.load(getClass().getResource("/fxml/PopupMenu.fxml"));
            
            this.popOverMenu = new PopOver();
            this.popOverMenu.setOnAutoHide(new EventHandler<Event>() {
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
    
    /**
     * Update view according to saved properties. Properties are saved into 
     * a file called application.properties and it is loadded during firt start
     * 
     * This routine can be used after changing any property to make it visible
     * TerpPropeties and it's getViewProps and setViewProps store used propeties
     * class
     */
    private void updateView(){
        // TODO : set view properties
        
        //set divider position of main split pane
        this.spMainPane.setDividerPosition(0, Double.parseDouble(
                this.props.getProperty("terp.main.dividerpos")));
        
        //set tree view show property
        if (this.props.getProperty("terp.main.showmainmenu").equals("true")) {
            // show menu
            this.spMainPane.getItems().add(0, this.apMainMenu);
            this.tbtnPopupMenu.setSelected(true);
        } else if (this.props.getProperty("terp.main.showmainmenu").equals("false")){
            // hide menu
            this.spMainPane.getItems().remove(this.apMainMenu);
            this.tbtnPopupMenu.setSelected(false);
        } else {
            LOG.log(Level.WARNING, null, "\'terp.main.showmainmenu\' in "
                    + "application.properties has wrong value. Please check the file");
        }
        
    }
// </editor-fold>    
 
// <editor-fold defaultstate="collapsed" desc=" Overrides ">

    /**
     * Initialize this controller and main frame.
     *
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
        this.spMainPane.getDividers().get(0).positionProperty()
                .addListener(splitDividerPostionListener);

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
        
        // get application properties
        this.props = TerpProperties.getInstance().getViewProps();
        
    }
    
    @Override
    public void addToolKit(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void addToDesktop(Node node, String headerText) {
        if (node != null) {
            Tab newProgramContainer = new Tab();
            newProgramContainer.setContent(node);
            newProgramContainer.setText(headerText);            
            this.tpDesktopContainer.getTabs().add(newProgramContainer);
            this.tpDesktopContainer.getSelectionModel().select(newProgramContainer);
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
     *
     * @param stage
     */
    @Override
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
// </editor-fold>

}
