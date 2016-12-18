/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.core.gui;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IIconFactory;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class CompanyFormController implements Initializable {

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

    
    private final IIconFactory iconFactory = TerpApplication.getInstance().getIconFactory();
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        // set button images
        // delete button
        this.imgBtnDelete.setImage(this.iconFactory.getIconBin());
        //add button
        this.imgBtnAdd.setImage(this.iconFactory.getIconPlus());
        //search button
        this.imgBtnSearch.setImage(this.iconFactory.getIconSearch());
        //edit button
        this.imgBtnEdit.setImage(this.iconFactory.getIconPencil());
    }    
    
}
