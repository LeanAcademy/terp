/*
 * Copyright (C) 2015 lean danışmanlık
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
package com.terp.plugin.data;

import java.util.Date;

/**
 *
 * @author cevdet
 */
public interface ICommonFields {    
    //field getter end setter
    public Long getRowId();
    public void setRowId(Long rowId);
    public Date getLastUpdateDate();
    public void setLastUpdateDate(Date lastUpdateDate);
    public Date getAddedDate();
    public void setAddedDate(Date addedDate);
    public Long getUpdatedByUserId();
    public void setUpdatedByUserId(Long updatedByUserId);
    public Long getAddedByUserId();
    public void setAddedByUserId(Long addedByUserId);
}
