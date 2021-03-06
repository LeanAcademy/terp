/*
 * Copyright (C) 2016 Your Organisation
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
package com.terp.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author cevdet
 */
public class TerpClassLoader {
    
    private static final Class[] parameters = new Class[]{URL.class};
    
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }//end method
    
    public static void addFile(File f) throws IOException {
        addURL(f.toURL());
    }//end method
    
    public static void addURL(URL u) throws IOException {
        
        // check if file exists
        File file = new File(u.getPath());
        if(!file.exists() || file.isDirectory()){
            throw new IOException("Error, file not found");
        }
        
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
           Method method = sysclass.getDeclaredMethod("addURL", parameters);
           method.setAccessible(true);
           method.invoke(sysloader, new Object[]{u});
        } catch (NoSuchMethodException 
                | SecurityException 
                | IllegalAccessException 
                | IllegalArgumentException 
                | InvocationTargetException t) {
           t.printStackTrace();
           throw new IOException("Error, could not add URL to system classloader");
        }

     
    }//end method
}
