package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity.EmisGisType;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.impl.NamedImpl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class MetaEntity extends NamedImpl implements EmisMetaEntity, Serializable
{
    private static final long serialVersionUID = 1L;
    private NamedIndexList<EmisMetaData> data = new NamedIndexList();
    private EmisMetaEntity.EmisGisType gisType = EmisMetaEntity.EmisGisType.NONE;

    public NamedIndexList<EmisMetaData> getData()
    { return this.data; }

    public void setData(NamedIndexList<EmisMetaData> data)
    { this.data = data; }

    public EmisMetaEntity.EmisGisType getGisType()
    { return this.gisType; }

    public void setGisType(EmisMetaEntity.EmisGisType gisType)
    { this.gisType = gisType; }

    public boolean isChildOf(EmisMetaEntity entityType, EmisMetaHierarchy hierarchy)
    {
        NamedIndexList<EmisMetaEntity> entities = hierarchy.getEntityOrder();
        int indexThis = entities.getIndex(this);
        int indexEntity = entities.getIndex(entityType); 
        if (indexThis == -1 ||indexEntity == -1)
        	return false; 
        
        return indexEntity < indexThis;
    }

	@Override
	public int hashCode() 
	{ return getName().hashCode(); }

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		return getName().equals(((MetaEntity) obj).getName()); 
	}

	@Override
	public EmisMetaDateEnum getRequiredDateEnum() 
	{
		EmisMetaDateEnum result = null; 
		for (EmisMetaData field : getData())
		{
			EmisMetaDateEnum tmp = field.getDateType();
			if (result == null || tmp.getDimensions() > result.getDimensions())
				result = tmp; 
		}

		return result;
	}

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{
		Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>(); 
		for (EmisMetaData field : getData())
			result.add(field.getDateType());

		return result;
	}
	
	
}
