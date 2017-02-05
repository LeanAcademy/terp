package com.terp.data.model;

import com.terp.data.CommonFields;
import com.terp.plugin.IPlugin;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="eklenti",catalog="terp", uniqueConstraints = {
    @UniqueConstraint(name="pk_eklenti", columnNames="ref_num"),
    @UniqueConstraint(name="ix_eklenti", columnNames="eklenti_adi")
})
public  class PluginSource extends CommonFields implements Serializable {
    
    @Column(name="eklenti_adi",nullable=false,length=128)
    private String pluginName;
    
    @Column(name="tip")
    private int type;
    
    @Column(name="sinif_adi")
    private String mainClassName;

    //////////////////////////////////////////////////////////////////////////
    // Gettters and setters
    //////////////////////////////////////////////////////////////////////////
    
    public String getPluginName() {
        return this.pluginName;
    }

    public void setPluginName (String pluginName) {
        this.pluginName = pluginName;
    }
    
    public int getType() {
        return this.type;
    }

    public void setType (int type) {
        this.type = type;
    }
    
    public PluginSource(){
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
    }

}

