/*
 * Copyright (C) 2015 ilknur
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.terp.plugin;

import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICompanyLookup;
import com.terp.plugin.data.IDatabaseFactory;
import com.terp.plugin.data.IDocumentNumbers;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IMovementReasonLookup;
import com.terp.plugin.data.IPersistence;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.IUserLookup;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.model.ICompany;
import com.terp.plugin.gui.IDesktopManager;
import com.terp.plugin.gui.IMenuManager;
import com.terp.plugin.gui.IIconFactory;

/**
 *
 * @author ilknur
 */
public class TerpApplication {
    
    private static TerpApplication app;
    private IMenuManager menuManager;
    private IUser user;
    private IDesktopManager desktop;
    private IStatusbarManager statusbar;
    private IPluginFactory pluginFactory;
    private IDatabaseFactory databaseFactory;
    private IPersistence persistence;
    private IAccountLookup accountLookup;
    private ICompanyLookup companyLookup;
    private IItemLookup itemLookup;
    private IWarehouseLookup warehouseLookup;
    private IMovementReasonLookup movementReasonLookup;
    private IStockLedger stockLedger;
    private IUserLookup userLookup;
    private IDocumentNumbers documentNumbers;
    private ICompany currentCompany;
    private IIconFactory iconFactory;
    private ClassLoader loader;
    
    private TerpApplication(){
        
    }
    
    public ClassLoader getClassLoader(){
        return loader;
    }
    
    public void setClassLoader(ClassLoader loader){
        this.loader = loader;
    }
    
    /**
     * get instance of application
     * @return 
     */
    public static synchronized TerpApplication getInstance(){
        if(app == null){
            app = new TerpApplication();
        }
        
        return app;
    }
    
    /**
     * get menu manager
     * @return 
     */
    public synchronized IMenuManager getMenuManager(){
        return this.menuManager;
    }
    
    /**
     * get user
     * @return 
     */
    public synchronized IUser getUser(){
        return this.user;
    }
    
    /**
     * return desktop pane of related main frame of application
     * @return 
     */
    public synchronized IDesktopManager getDesktopManager(){
        return this.desktop;
    }
    
    /**
     * set menu manager
     * @param manager 
     */
    public void setMenuManager(IMenuManager manager){
        this.menuManager = manager;
    }
    
    /**
     * set user
     * @param user 
     */
    public void setUser(IUser user){
        this.user = user;
    }
    
    /**
     * set desktop pane
     * @param desktop 
     */
    public void setDesktop(IDesktopManager desktop){
        this.desktop = desktop;
    }

    public IStatusbarManager getStatusbar() {
        return statusbar;
    }

    public void setStatusbar(IStatusbarManager statusbar) {
        this.statusbar = statusbar;
    }

    public IPluginFactory getPluginFactory() {
        return pluginFactory;
    }

    public void setPluginFactory(IPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    public IDatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public void setDatabaseFactory(IDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public IPersistence getPersistence() {
        return persistence;
    }

    public void setPersistence(IPersistence persistence) {
        this.persistence = persistence;
    }

    public IAccountLookup getAccountLookup() {
        return accountLookup;
    }

    public void setAccountLookup(IAccountLookup accountLookup) {
        this.accountLookup = accountLookup;
    }

    public ICompanyLookup getCompanyLookup() {
        return companyLookup;
    }

    public void setCompanyLookup(ICompanyLookup companyLookup) {
        this.companyLookup = companyLookup;
    }

    public IItemLookup getItemLookup() {
        return itemLookup;
    }

    public void setItemLookup(IItemLookup itemLookup) {
        this.itemLookup = itemLookup;
    }

    public IWarehouseLookup getWarehouseLookup() {
        return warehouseLookup;
    }

    public void setWarehouseLookup(IWarehouseLookup warehouseLookup) {
        this.warehouseLookup = warehouseLookup;
    }

    public IMovementReasonLookup getMovementReasonLookup() {
        return movementReasonLookup;
    }

    public void setMovementReasonLookup(IMovementReasonLookup movementReasonLookup) {
        this.movementReasonLookup = movementReasonLookup;
    }

    public IStockLedger getStockLedger() {
        return stockLedger;
    }

    public void setStockLedger(IStockLedger stockLedger) {
        this.stockLedger = stockLedger;
    }

    public IUserLookup getUserLookup() {
        return userLookup;
    }

    public void setUserLookup(IUserLookup userLookup) {
        this.userLookup = userLookup;
    }

    public IDocumentNumbers getDocumentNumbers() {
        return documentNumbers;
    }

    public void setDocumentNumbers(IDocumentNumbers documentNumbers) {
        this.documentNumbers = documentNumbers;
    }

    public ICompany getCurrentCompany() {
        return currentCompany;
    }

    public void setCurrentCompany(ICompany currentCompany) {
        this.currentCompany = currentCompany;
    }

    public IIconFactory getIconFactory() {
        return iconFactory;
    }

    public void setIconFactory(IIconFactory iconFactory) {
        this.iconFactory = iconFactory;
    }
    
}
