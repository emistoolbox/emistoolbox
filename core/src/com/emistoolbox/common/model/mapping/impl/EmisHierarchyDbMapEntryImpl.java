package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import java.io.Serializable;

public class EmisHierarchyDbMapEntryImpl extends EmisDbMapBaseImpl implements EmisHierarchyDbMapEntry, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEntity child;
    private DbRowAccess childAccess;
    private EmisMetaEntity parent;
    private DbRowAccess parentAccess;
    private EmisMetaDateEnum dateType;
    private DbRowDateAccess dateAccess;

    public DbRowAccess getChildAccess()
    {
        return this.childAccess;
    }

    public EmisMetaEntity getChildEntity()
    {
        return this.child;
    }

    public DbRowAccess getParentAccess()
    {
        return this.parentAccess;
    }

    public EmisMetaEntity getParentEntity()
    {
        return this.parent;
    }

    public void setChildAccess(DbRowAccess access)
    {
        this.childAccess = access;
    }

    public void setChildEntity(EmisMetaEntity child)
    {
        this.child = child;
    }

    public void setParentAccess(DbRowAccess access)
    {
        this.parentAccess = access;
    }

    public void setParentEntity(EmisMetaEntity parent)
    {
        this.parent = parent;
    }

    public EmisMetaDateEnum getDateType()
    {
        return this.dateType;
    }

    public void setDateType(EmisMetaDateEnum dateType)
    {
        this.dateType = dateType;
    }

    public DbRowDateAccess getDateAccess()
    {
        return this.dateAccess;
    }

    public void setDateAccess(DbRowDateAccess dateAccess)
    {
        this.dateAccess = dateAccess;
    }

    public String toString()
    {
        return getChildEntity().getName() + " > " + getParentEntity().getName();
    }

	@Override
	public void updateDimensions() 
	{
	    DbRowAccessBase.updateDimensions(childAccess);
	    DbRowAccessBase.updateDimensions(parentAccess);
	    DbRowAccessBase.updateDimensions(dateAccess);
	}
}

