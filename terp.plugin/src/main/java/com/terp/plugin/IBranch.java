/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.plugin;

import java.util.List;

/**
 *
 * @author cevdet
 */
public interface IBranch {
    public List<Object> findAll();
    public Object firstOrDefault(long key);
    public Object addOrUpdate(Object row);
    public void delete(long rowId);
}
