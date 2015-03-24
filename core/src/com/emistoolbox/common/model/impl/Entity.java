package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.impl.NamedImpl;

import java.io.Serializable;

public class Entity extends NamedImpl implements EmisEntity, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEntity entityType;
    private int id;
    private double[] gisData;

    public Entity() 
    {}

    public Entity(EmisMetaEntity entityType, int id) {
        this.entityType = entityType;
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public EmisMetaEntity getEntityType()
    {
        return this.entityType;
    }

    public void setEntityType(EmisMetaEntity entityType)
    {
        this.entityType = entityType;
    }

    public double[] getGisData()
    {
        return this.gisData;
    }

    public void setGisData(double[] gisData)
    {
        this.gisData = gisData;
    }
    
    public boolean equals(EmisEntity entity)
    { return NamedUtil.sameName(entity.getEntityType(), getEntityType()) && entity.getId() == getId(); }

	@Override
	public int hashCode() 
	{
		final int prime = 31;

		int result = 1;
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + id;

		return result;
	}
}
