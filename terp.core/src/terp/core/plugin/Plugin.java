/*
 * Copyright (C) 2014 ilknur
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package terp.core.plugin;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import terp.core.gui.CompanyForm;
import terp.plugin.Application;
import terp.plugin.IDesktopManager;
import terp.plugin.IMenuManager;
import terp.plugin.IPlugin;

/**
 *
 * @author ilknur
 */
public class Plugin implements IPlugin{
    private final String name = "terp.core";
    private final String sysVersion = "1.0";
    private final String pluginVersion = "1.0";
    private final int type = 1; //core plugin
    private JPanel coreToolBar;
    private IMenuManager menuManager = null;
    private Application app = null;
    private IDesktopManager desktopManager = null;
    //private IUser user = null;
    //private IDatabase database = null;
    
    /**
     * initialize toolbar according to user authority
     */
    private void initToolBar(){
        
        //create core tab
        coreToolBar = new JPanel();
        coreToolBar.setName("Administration");
        coreToolBar.setLayout(new BoxLayout(coreToolBar, BoxLayout.LINE_AXIS));
        
        /**
         * company button
         */
        
        //create buttons
        JButton btnCompanyManagement = new JButton();
        btnCompanyManagement.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnCompanyManagement.setIcon(new ImageIcon(getClass()
                .getResource("/terp/core/resources/images/coreCompany.png"))); //TODO set button image
        btnCompanyManagement.setText("Company");
        btnCompanyManagement.setToolTipText("Manage company");
        btnCompanyManagement.setHorizontalTextPosition(
                javax.swing.SwingConstants.CENTER);
        btnCompanyManagement.setMaximumSize(new java.awt.Dimension(70, 60));
        btnCompanyManagement.setMinimumSize(new java.awt.Dimension(69, 59));
        btnCompanyManagement.setPreferredSize(new java.awt.Dimension(70, 60));
        btnCompanyManagement.setVerticalTextPosition(
                javax.swing.SwingConstants.BOTTOM);
        btnCompanyManagement.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnCompanyActionPerformed(e);
            }
        });
        
        //add button to tab
        coreToolBar.add(btnCompanyManagement);
        
        /**
         * branch button
         */
        
        //create button
        JButton btnBranch = new JButton();
        btnBranch.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnBranch.setIcon(new ImageIcon(getClass()
                .getResource("/terp/core/resources/images/coreBranch.png"))); //TODO set button image
        btnBranch.setText("Branch");
        btnBranch.setToolTipText("Manage branchs");
        btnBranch.setHorizontalTextPosition(
                javax.swing.SwingConstants.CENTER);
        btnBranch.setMaximumSize(new java.awt.Dimension(70, 60));
        btnBranch.setMinimumSize(new java.awt.Dimension(69, 59));
        btnBranch.setPreferredSize(new java.awt.Dimension(70, 60));
        btnBranch.setVerticalTextPosition(
                javax.swing.SwingConstants.BOTTOM);
        btnBranch.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnBranchActionPerformed(e);
            }
        });
        
        //add button to tab
        coreToolBar.add(btnBranch);
    }
    
    /**
     * company button action
     * @param e 
     */
    public void btnCompanyActionPerformed(ActionEvent e){
        //create company form
        CompanyForm companyForm = new CompanyForm();
        companyForm.setVisible(true);
        this.desktopManager.addToDesktop(companyForm);
        this.menuManager.setButtonEnabled("Administration", "Company", false);
    }
    
    /**
     * branch button action
     * @param e 
     */
    public void btnBranchActionPerformed(ActionEvent e){
        
    }
    
    /**
     * run plugin
     */
    @Override
    public void run() {
        
            
        //get application
        this.app = Application.getInstance();
        
        //set desktop manager
        this.desktopManager = app.getDesktopManager();
        
        /**
         * create menu manager and setup toolbar
         * according to user rights
         */
        menuManager = this.app.getMenuManager();
        this.initToolBar();
        menuManager.addToolKit(coreToolBar);        
        
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSystemVersion() {
        return this.sysVersion;
    }

    @Override
    public String getPluginVersion() {
        return this.pluginVersion;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public void install() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isInstalled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //logger
    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());
}
