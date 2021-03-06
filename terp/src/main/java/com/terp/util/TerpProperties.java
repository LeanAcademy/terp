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

import java.util.Properties;

/**
 *
 * @author cevdet
 */
public class TerpProperties {
    
    private static TerpProperties props;
    private Properties hibernateProps;
    private Properties viewProps;
    
    
    private TerpProperties(){}
    
    public static synchronized TerpProperties getInstance(){
        if(props == null){
            props = new TerpProperties();
        }
        return props;
    }
    
    public Properties getHibernateProps() {
        return hibernateProps;
    }

    public void setHibernateProps(Properties hibernateProps) {
        this.hibernateProps = hibernateProps;
    }

    public Properties getViewProps() {
        return viewProps;
    }

    public void setViewProps(Properties viewProps) {
        this.viewProps = viewProps;
    }
}
