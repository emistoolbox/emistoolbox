package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.EntityBaseDbMap;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public class EntityBaseDbMapImpl extends EmisDbMapBaseImpl implements EntityBaseDbMap
{
    private EmisMetaEntity entity;
    private DbRowAccess idAccess;

    public EmisMetaEntity getEntity()
    { return this.entity; }

    public DbRowAccess getIdAccess()
    { return this.idAccess; }

    public void setEmisMetaEntity(EmisMetaEntity entity)
    { this.entity = entity; }

    public void setIdAccess(DbRowAccess access)
    { this.idAccess = access; }

    public String toString()
    { return "Location " + this.entity.getName() + "."; }

	@Override
	public void updateDimensions() 
	{ DbRowAccessBase.updateDimensions(idAccess); }
}
