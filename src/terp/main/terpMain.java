/*
 * Copyright (C) 2014 ilknur
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

package terp.main;

import terp.plugin.Application;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import terp.gui.LoginForm;
import terp.gui.MainFrame;
import terp.gui.SplashScreen;
import terp.plugin.IPlugin;
import terp.plugins.PluginManager;

/**
 *
 * @author ilknur
 */
public class terpMain {
    
    private static SplashScreen spsc;
    private static LoginForm login;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try{
            
            MainFrame mainFrame = new MainFrame();
            Application app = Application.getInstance();
            
            //set applications
            app.setMenuManager(mainFrame);
            app.setDesktop(mainFrame);
            app.setDatabase(null); //TODO: change
            app.setUser(null);//TODO: change
            
            
            //show splash screen
            spsc = new SplashScreen();
            spsc.setLocationRelativeTo(null);
            spsc.setProgressMax(100);
            spsc.setScreenVisible(true);

            //hide splash screen
            spsc.setScreenVisible(false);
            spsc=null;
            
            //show login form
            login = new LoginForm(mainFrame);
            login.setVisible(true);            
            if(!app.getUser().isAuthenticated())
                throw new Exception("Login failed");
            
            //load core plugin
            LOG.log(Level.INFO, "loading plugin terp.core ");
            PluginManager pm = new PluginManager();
            IPlugin plg = pm.getPlugin("terp.core");
            
            //show main frame
            SwingUtilities.invokeLater(() -> {
                mainFrame.setVisible(true);

                if(plg != null){
                    //run plugin
                    LOG.log(Level.INFO, "plugin terp.core is loaded. Try to run");
                    plg.run();
                } 
            });           
         
        } catch(Exception e){
            
            //stop splashscreen
            if(spsc != null){
                spsc.setScreenVisible(false);
                spsc = null;
            }
            
            //stop LoginForm
            if(login != null){
                login.setVisible(false);
                login = null;
            }
            
            LOG.log(Level.SEVERE, e.getMessage());
            System.exit(0);
            
        }
    }
    private static final Logger LOG = Logger.getLogger(terpMain.class.getName());

}
