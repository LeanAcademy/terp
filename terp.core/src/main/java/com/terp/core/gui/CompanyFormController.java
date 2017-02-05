/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.core.gui;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.dao.ICompanyDao;
import com.terp.plugin.data.model.ICompany;
import com.terp.plugin.gui.IIconFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class CompanyFormController implements Initializable {

    // <editor-fold defaultstate="collapsed" desc="FXML variables ">
    @FXML
    private ImageView imgBtnAdd;

    @FXML
    private ImageView imgBtnSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private ImageView imgBtnEdit;

    @FXML
    private ImageView imgBtnDelete;

    @FXML
    private TextField txtCompanyLongName;

    @FXML
    private TextField txtCompanyName;

    @FXML
    private CheckBox cbStatusSearch;

    @FXML
    private TextField txtRefNum;

    @FXML
    private TableView<ICompany> tblvCompanyView;

    @FXML
    private Pagination pgnCompanyData;

    @FXML
    private TableColumn<ICompany, String> tcStateTaxCode;

    @FXML
    private TableColumn<ICompany, String> tcCompanyLongName;

    @FXML
    private TableColumn<ICompany, String> tcCompanyName;

    @FXML
    private TableColumn<ICompany, String> tcNotes;

    @FXML
    private TableColumn<ICompany, String> tcStateTaxRegion;

    @FXML
    private TableColumn<ICompany, Long> tcRefNum;

    @FXML
    private TableColumn<ICompany, Integer> tcStatus;

    @FXML
    private TableColumn<ICompany, Long> tcUpdatedById;

    @FXML
    private TableColumn<ICompany, Long> tcAddedById;

    @FXML
    private TableColumn<ICompany, Long> tcAddedDate;

    @FXML
    private TableColumn<ICompany, Long> tcLastUpdateDate;
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private variables">
    private final IIconFactory iconFactory = TerpApplication.getInstance().getIconFactory();
    
    private long recordCount = -1;
    
    private ICompanyDao companyDao;
    
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    
    private int rowsPerPage;
    
    private int pageCount;
    
    private int currentPageNum;
    
    private String searchSqlStatement;
    
    //private ObjectProperty<ObservableList<CompanyModel>> currentPageProperty;
    private ObjectProperty<ObservableList<ICompany>> currentPage;
    
    private static final Logger LOG 
            = Logger.getLogger(CompanyFormController.class.getName());
    //</editor-fold>
    
    @FXML
    public void onActionBtnSearch(ActionEvent event) {

        boolean addAnd = false;
        boolean whereAdded = false;

        //build sql statement
        String sql = "from Company company";

        if (!this.txtRefNum.getText().isEmpty()) {
            if (!whereAdded) {
                sql += " where";
                whereAdded = true;
            }
            sql += " company.rowId = " + this.txtRefNum.getText();
            addAnd = true;
        }

        if (!this.txtCompanyName.getText().isEmpty()) {
            if (!whereAdded) {
                sql += " where";
            }
            if (addAnd) {
                sql += " and";
            }
            sql += " company.companyName like '" + 
                    this.txtCompanyName.getText() + "'";
            addAnd = true;
        }

        if (!this.txtCompanyLongName.getText().isEmpty()) {
            if (!whereAdded) {
                sql += " where";
            }
            if (addAnd) {
                sql += " and";
            }
            sql += " company.companyLongName like '" + 
                    this.txtCompanyLongName.getText() + "'";
        }
        
        this.setSearchSqlStatement(sql);
        this.setCurrentPageNum(1);
        this.companyViewUpdate();
    }

    @FXML
    public void onActionBtnAdd(ActionEvent event) {
        // TODO : implement action event for add
        //open add new form
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/fxml/AddCompanyForm.fxml"));
            Node node = loader.load();
            Scene scene = new Scene((Parent)node);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(scene);
            stage.showAndWait();
        }catch(IOException e){
            LOG.log(Level.SEVERE,null,e);
        }
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        // TODO : implement action event for edit
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        // TODO : implement action event for delete
    }
    

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO      

        //set database access obejct for company database
        this.companyDao = TerpApplication.getInstance().getDatabaseFactory().getCompanyDao();

        // set button images
        // delete button
        this.imgBtnDelete.setImage(this.iconFactory.getIconBin());
        //add button
        this.imgBtnAdd.setImage(this.iconFactory.getIconPlus());
        //search button
        this.imgBtnSearch.setImage(this.iconFactory.getIconSearch());
        //edit button
        this.imgBtnEdit.setImage(this.iconFactory.getIconPencil());

        //get record count
        this.recordCount = this.getRecordCount();

        //set pagination
        this.pgnCompanyData.currentPageIndexProperty()
                .addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue,
                            Object newValue) {
                        setCurrentPageNum((int) newValue);
                        companyViewUpdate();
                    }
                });
        //page num

        this.pgnCompanyData.setPageCount(1);

        //set table view columns
        this.tcRefNum.setCellValueFactory(new PropertyValueFactory("rowId"));
        this.tcCompanyName.setCellValueFactory(new PropertyValueFactory("companyName"));
        this.tcCompanyLongName.setCellValueFactory(new PropertyValueFactory("companyLongName"));
        this.tcNotes.setCellValueFactory(new PropertyValueFactory("notes"));
        this.tcStatus.setCellValueFactory(new PropertyValueFactory("status"));
        this.tcStateTaxCode.setCellValueFactory(new PropertyValueFactory("stateTaxCode"));
        this.tcStateTaxRegion.setCellValueFactory(new PropertyValueFactory("stateTaxRegion"));
        this.tcAddedById.setCellValueFactory(new PropertyValueFactory("addedById"));
        this.tcUpdatedById.setCellValueFactory(new PropertyValueFactory("updatedById"));
        this.tcAddedDate.setCellValueFactory(new PropertyValueFactory("addedDate"));
        this.tcLastUpdateDate.setCellValueFactory(new PropertyValueFactory("lastUpdateDate"));

        //set page count
        this.pgnCompanyData.setPageCount(this.getPageCount());
    }

    private void companyViewUpdate() {
        // TODO implement table view

        //get page count and set table view data source
        this.tblvCompanyView.setItems(this.getCurrentPage());
    }

    private int getRowsPerPage() {
        if (this.rowsPerPage <= 0) {
            this.setRowsPerPage(DEFAULT_ROWS_PER_PAGE);
        }
        return this.rowsPerPage;
    }

    private void setRowsPerPage(int value) {
        this.rowsPerPage = value;
    }

    private int getPageCount() {
        this.setPageCount((int) (this.getRecordCount() / this.getRowsPerPage() + 1));
        return this.pageCount;
    }

    private void setPageCount(int value) {
        this.pageCount = value;
    }

    private int getCurrentPageNum() {
        return this.currentPageNum;
    }

    private void setCurrentPageNum(int value) {
        this.currentPageNum = value;
    }

    private String getSearchSqlStatement() {
        return this.searchSqlStatement;
    }

    private void setSearchSqlStatement(String value) {
        this.searchSqlStatement = value;
    }

    /**
     * find current page recordset
     *
     * @return
     */
    private ObservableList<ICompany> getCurrentPage() {
        assert (this.companyDao != null) : "Database connection not set";
        assert (this.getCurrentPageNum() != -1) : "Current page num is not set";

        ObservableList<ICompany> list;

        if (!this.getSearchSqlStatement().isEmpty()) {
            list = FXCollections.observableArrayList(
                    this.companyDao.findPage(this.getCurrentPageNum(),
                            this.getRowsPerPage(), this.getSearchSqlStatement())
            );
        } else {
            list = FXCollections.observableArrayList(
                    this.companyDao.findPage(this.getCurrentPageNum(), 
                            this.getRowsPerPage())
            );
        }
        return list;
    }

    /**
     * adds or upadate given row
     *
     * @param row
     */
    private void addOrUpdate(ICompany row) {
        assert (this.companyDao != null) : "Database connection not setup";
        this.companyDao.addOrUpdate(row);
    }

    /**
     * return record count of company table
     *
     * @return
     */
    public long getRecordCount() {
        assert (this.companyDao != null) : "Database connection not setup";
        return this.companyDao.getRecordCount();
    }
}
