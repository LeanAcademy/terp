/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.gui.controllers;

import com.terp.data.dao.MenuSourceDao;
import com.terp.plugin.gui.IDesktopManager;
import com.terp.plugin.gui.IMenuManager;
import com.terp.data.model.MenuSource;
import com.terp.gui.MenuItem;
import com.terp.plugin.*;
import com.terp.plugin.data.ICompanyLookup;
import com.terp.plugin.data.model.ICompany;
import com.terp.util.CompanyScopeSchema;
import com.terp.util.TerpProperties;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;

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
    private ComboBox<ICompany> cmbCompany;

    @FXML
    private Label lblUser;

    @FXML
    private Label lblCompany;

    @FXML
    private GridPane gpStatusBar;
    
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
            if (selectedItem != null && allowedToOpen(selectedItem.getCurrentItem())) {
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
        MenuSourceDao menuSourceDao = new MenuSourceDao();
        String sql = "from MenuSource e where e.menuId = '" + programName + "'"; 
        MenuSource prog = menuSourceDao.firstOrDefault(sql);
        
        //check if program is found
        assert(prog != null);
        
        //run program
        MenuSource menuSource = (MenuSource)prog;
        if (!allowedToOpen(menuSource)) {
            this.txtSearchMenuItem.clear();
            return;
        }
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
    private boolean pluginToolsStarted;
    private boolean companyComboBound;
    private boolean schemaAligned;
    private Separator pluginToolSeparator;
    private static final String PLUGIN_TOOL_KEY = "terp.pluginTool";
    private static final String MENU_ID_KEY = "terp.menuId";
    
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
    
    private final ChangeListener<Number> splitDividerPositionListener 
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
        MenuSourceDao menuSourceDao = new MenuSourceDao();
        String sql = "from MenuSource e where e.menuType=0";        
        List<MenuSource> list = menuSourceDao.findAll(sql);

        //build tree menu
        for (Object item : list) {
            MenuSource folder = (MenuSource) item;
            if (!folderHasVisibleProgram(folder)) {
                continue;
            }
            MenuItem menuLeaf = new MenuItem(folder);
            rootNode.getChildren().add(menuLeaf);
            
        }

        // add into tree view
        tvMainMenu.rootProperty().set(rootNode);

        // load all programs for search text field
        String sql2 = "from MenuSource e where e.programName is not null";
        List<MenuSource> list2 = menuSourceDao.findAll(sql2);
        for (MenuSource item2 : list2) {
            if (!allowedToOpen(item2)) {
                continue;
            }
            this.entries.add((item2).getMenuId() + " - "
                    + (item2).getMenuName());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/" + program + ".fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            Node nodeProgram = loader.load();
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
        if (plg == null) {
            LOG.log(Level.SEVERE, "Plugin id {0} is not loaded", pluginId);
            return;
        }
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
                .addListener(splitDividerPositionListener);

        // setting desktop manager to give plugins access
        terpApp.setDesktop(this);

        // setting menu manager to grant access
        terpApp.setMenuManager(this);

        // settin statusbar manager
        terpApp.setStatusbar(this);

        // load menu
        loadMenu();

        // create context menu for auto complete
        //TextFields.bindAutoCompletion(txtSearchMenuItem, entries);

        // create popup menu
        createPopup();
        
        // get application properties
        this.props = TerpProperties.getInstance().getViewProps();

        bindUserSwitchMenu();
    }

    private void bindUserSwitchMenu() {
        ContextMenu menu = new ContextMenu();
        javafx.scene.control.MenuItem switchUser = new javafx.scene.control.MenuItem("Kullanıcı değiştir");
        switchUser.setOnAction(event -> confirmAndSwitchUser());
        menu.getItems().add(switchUser);
        if (this.lblUser != null) {
            this.lblUser.setTooltip(new Tooltip("Sağ tık: kullanıcı değiştir"));
        }
        Node bar = this.gpStatusBar != null ? this.gpStatusBar : this.lblUser;
        if (bar == null) {
            return;
        }
        bar.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            menu.show(bar, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private void confirmAndSwitchUser() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Açık programlar kapanır. Kullanıcı değiştirilsin mi?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Kullanıcı değiştir");
        confirm.setHeaderText("Oturum değişecek");
        confirm.initOwner(this.primaryStage);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showSwitchUserDialog();
            }
        });
    }

    private void showSwitchUserDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginForm.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            Parent root = loader.load();
            LoginFormController controller = loader.getController();
            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(this.primaryStage);
            dialog.setTitle("Kullanıcı değiştir");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            controller.prepareSwitchUser(this::applyUserSwitch);
            dialog.showAndWait();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void applyUserSwitch() {
        if (this.tpDesktopContainer != null) {
            this.tpDesktopContainer.getTabs().clear();
        }
        if (this.entries != null) {
            this.entries.clear();
        }
        loadMenu();
        applyPluginToolRights();
        applySessionContext();
    }
    
    @Override
    public void addToolKit(Node node) {
        addToolKit(node, null);
    }

    @Override
    public void addToolKit(Node node, String menuId) {
        if (node == null || this.tbMainToolBar == null) {
            return;
        }
        if (node instanceof ToolBar bar) {
            for (Node item : List.copyOf(bar.getItems())) {
                tagPluginTool(item, menuId);
                insertPluginTool(item);
            }
            applyPluginToolRights();
            return;
        }
        tagPluginTool(node, menuId);
        insertPluginTool(node);
        applyPluginToolRights();
    }

    private void tagPluginTool(Node node, String menuId) {
        if (node == null) {
            return;
        }
        node.getProperties().put(PLUGIN_TOOL_KEY, Boolean.TRUE);
        if (menuId != null && !menuId.isBlank()) {
            node.getProperties().put(MENU_ID_KEY, menuId);
        }
    }

    private void applyPluginToolRights() {
        if (this.tbMainToolBar == null) {
            return;
        }
        boolean anyVisible = false;
        for (Node node : this.tbMainToolBar.getItems()) {
            if (node == null || node == this.pluginToolSeparator) {
                continue;
            }
            if (!Boolean.TRUE.equals(node.getProperties().get(PLUGIN_TOOL_KEY))) {
                continue;
            }
            Object menuId = node.getProperties().get(MENU_ID_KEY);
            boolean show = menuId == null || allowedToOpen(menuId.toString());
            node.setVisible(show);
            node.setManaged(show);
            if (show) {
                anyVisible = true;
            }
        }
        if (this.pluginToolSeparator != null) {
            this.pluginToolSeparator.setVisible(anyVisible);
            this.pluginToolSeparator.setManaged(anyVisible);
        }
    }

    private void insertPluginTool(Node node) {
        if (node == null) {
            return;
        }
        int insertAt = pluginInsertIndex();
        if (!pluginToolsStarted) {
            Separator separator = new Separator();
            separator.setOrientation(Orientation.VERTICAL);
            separator.setPrefHeight(24.0);
            separator.getProperties().put(PLUGIN_TOOL_KEY, Boolean.TRUE);
            this.pluginToolSeparator = separator;
            this.tbMainToolBar.getItems().add(insertAt, separator);
            insertAt++;
            pluginToolsStarted = true;
        }
        this.tbMainToolBar.getItems().add(insertAt, node);
    }

    /**
     * Keep the trailing spacer (popup menu) on the right of the toolbar.
     */
    private int pluginInsertIndex() {
        int size = this.tbMainToolBar.getItems().size();
        if (size > 0 && this.tbMainToolBar.getItems().get(size - 1) instanceof AnchorPane) {
            return size - 1;
        }
        return size;
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
        if (this.lblUser != null) {
            this.lblUser.setText(userName == null ? "" : userName);
        }
    }
    
    @Override
    public void updateCompany(String company) {
        if (this.lblCompany != null) {
            this.lblCompany.setText(company == null ? "" : company);
        }
    }
    
    public void applySessionContext() {
        IUser user = TerpApplication.getInstance().getUser();
        if (user != null) {
            updateUser(user.getUserName());
        }
        applyPluginToolRights();
        bindCompanyCombo();
    }

    private void bindCompanyCombo() {
        if (cmbCompany == null) {
            return;
        }
        if (!companyComboBound) {
            cmbCompany.setConverter(new javafx.util.StringConverter<ICompany>() {
                @Override
                public String toString(ICompany company) {
                    return company == null ? "" : company.getDisplayLabel();
                }

                @Override
                public ICompany fromString(String string) {
                    return null;
                }
            });
            cmbCompany.valueProperty().addListener((obs, old, value) -> {
                TerpApplication.getInstance().setCurrentCompany(value);
                updateCompany(value == null ? "" : value.getDisplayLabel());
            });
            companyComboBound = true;
        }
        cmbCompany.getItems().clear();
        ICompanyLookup lookup = TerpApplication.getInstance().getCompanyLookup();
        IUser user = TerpApplication.getInstance().getUser();
        if (lookup != null) {
            List<ICompany> companies = lookup.findActive();
            for (ICompany company : companies) {
                if (company == null || company.getRowId() == null) {
                    continue;
                }
                if (user != null && !user.hasAllCompanies()
                        && !user.canAccessCompany(company.getRowId())) {
                    continue;
                }
                cmbCompany.getItems().add(company);
            }
        }
        if (cmbCompany.getItems().isEmpty()) {
            TerpApplication.getInstance().setCurrentCompany(null);
            updateCompany("");
            alignSchema(null);
            if (user != null && !user.hasAllCompanies()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Firma");
                alert.setHeaderText("Grubunuza firma atanmamış");
                alert.setContentText("Sistem yöneticisi gruba en az bir şirket vermelidir.");
                alert.show();
            }
            return;
        }
        cmbCompany.getSelectionModel().select(0);
        ICompany selected = cmbCompany.getSelectionModel().getSelectedItem();
        TerpApplication.getInstance().setCurrentCompany(selected);
        updateCompany(selected == null ? "" : selected.getDisplayLabel());
        alignSchema(selected == null ? null : selected.getRowId());
    }

    private void alignSchema(Long companyId) {
        if (schemaAligned) {
            return;
        }
        CompanyScopeSchema.align(companyId);
        if (companyId != null) {
            schemaAligned = true;
        }
    }

    private boolean folderHasVisibleProgram(MenuSource folder) {
        if (folder == null || folder.getRowId() == null) {
            return false;
        }
        MenuSourceDao dao = new MenuSourceDao();
        List<MenuSource> children = dao.findAll(
                "from MenuSource e where e.menuType=1 and e.menuParent=" + folder.getRowId());
        if (children == null) {
            return false;
        }
        for (MenuSource child : children) {
            if (allowedToOpen(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean allowedToOpen(MenuSource menu) {
        if (menu == null) {
            return false;
        }
        return allowedToOpen(menu.getMenuId());
    }

    private static boolean allowedToOpen(String menuId) {
        IUser user = TerpApplication.getInstance().getUser();
        return user != null && user.canOpen(menuId);
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
