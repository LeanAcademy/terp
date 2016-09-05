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
package com.terp.gui;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author ilknur
 */

public class SplashScreen extends JWindow {
    
    //local variables
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel imageLabel = new JLabel();
    JPanel southPanel = new JPanel();
    FlowLayout southPanelFlowLayout = new FlowLayout();
    JProgressBar progressBar = new JProgressBar();
    ImageIcon imageIcon;
    
    /**
     * 
     * @param imageIcon 
     */
    public SplashScreen() {
      imageIcon = new ImageIcon(getClass().
              getResource("/terp/resources/images/splashscreen.png"));
      try {
        Initialize();
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }

    /**
     * 
     * @throws Exception 
     */
    void Initialize() throws Exception {
      imageLabel.setIcon(imageIcon);
      this.getContentPane().setLayout(borderLayout1);
      southPanel.setLayout(southPanelFlowLayout);
      southPanel.setBackground(Color.BLACK);
      this.getContentPane().add(imageLabel, BorderLayout.CENTER);
      this.getContentPane().add(southPanel, BorderLayout.SOUTH);
      southPanel.add(progressBar, null);
      this.pack();
    }  
    
    /**
     * 
     * @param maxProgress 
     */
    public void setProgressMax(int maxProgress){
      progressBar.setMaximum(maxProgress);
    }

    /**
     * 
     * @param progress 
     */
    public void setProgress(int progress){
      final int theProgress = progress;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          progressBar.setValue(theProgress);
        }
      });
    }

    /**
     * 
     * @param message
     * @param progress 
     */
    public void setProgress(String message, int progress){
      final int theProgress = progress;
      final String theMessage = message;
      setProgress(progress);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          progressBar.setValue(theProgress);
          setMessage(theMessage);
        }
      });
    }

    /**
     * 
     * @param b 
     */
    public void setScreenVisible(boolean b){
      final boolean boo = b;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          setVisible(boo);
        }
      });
    }

    /**
     * 
     * @param message 
     */
    private void setMessage(String message){
      if (message==null)
      {
        message = "";
        progressBar.setStringPainted(false);
      }
      else
      {
        progressBar.setStringPainted(true);
      }
      progressBar.setString(message);
    }    
}
