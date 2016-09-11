package com.terp.data.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="eklenti",catalog="terp", uniqueConstraints = {
    @UniqueConstraint(name="pk_eklenti", columnNames="ref_num"),
    @UniqueConstraint(name="ix_eklenti", columnNames="eklenti_adi")
})
@NamedQueries({
    @NamedQuery(name="PLUGINSOURCE_FIND_ALL", query="from PluginSource ps"),
    @NamedQuery(name="PLUGINSOURCE_FIND_BY_PK", query="from PluginSource ps "
            + "where ps.rowid = :id")
})
public  class PluginSource implements Serializable {

    @Id    
    @Column(name="ref_num",nullable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long rowid;
    
    @Column(name="eklenti_adi",nullable=false,length=128)
    private String pluginName;
    
    @Column(name="tip")
    private int type;
    
    @Column(name="sinif_adi")
    private String mainClassName;

    // Gettters and setters
    ///////////////////////////
    public Long getRowid() {
        return this.rowid;
    }

    public void setRowid (Long rowid) {
        this.rowid = rowid;
    }    
    
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

