package com.emistoolbox.common.results.impl;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.TableMetaResult;

import java.io.Serializable;
import java.util.Set;

public class TableMetaResultImpl extends MetaResultImpl implements TableMetaResult, Serializable
{
    private static final long serialVersionUID = 1L;
    private int sortOrder = 0;
    private MetaResultDimension[] dimensions;

    public MetaResultDimension getDimension(int index)
    { return this.dimensions[index]; }

    public int getDimensionCount()
    { return this.dimensions == null ? 0 : this.dimensions.length; }

    public void setDimension(int index, MetaResultDimension dimension)
    { this.dimensions[index] = dimension; }

    public void setDimensionCount(int count)
    { this.dimensions = new MetaResultDimension[count]; }

    public int getSortOrder()
    { return this.sortOrder; }

    public void setSortOrder(int order)
    { this.sortOrder = order; }

    public Set<EmisMetaDateEnum> getUsedDateTypes(boolean withoutAxis)
    { return MetaResultUtil.getUsedDateTypes(getMetaResultValues(), withoutAxis ? dimensions : null); }

	@Override
	public EmisMetaEntity getSeniorEntity() 
	{
		EmisMetaEntity result= super.getSeniorEntity();

		for (MetaResultDimension dim : dimensions)
		{
			if (dim instanceof MetaResultDimensionEntity)
				result = EmisUtils.getSeniorEntity(((MetaResultDimensionEntity) dim).getEntityType(), result, getHierarchy());  
		}

		return result; 
	}

	@Override
	public TableMetaResult createCopy() 
	{
		TableMetaResultImpl result = new TableMetaResultImpl(); 
		super.copy(result); 
		result.sortOrder = sortOrder; 

		if (dimensions != null)
		{
			result.dimensions = new MetaResultDimension[dimensions.length]; 
			for (int i = 0; i < dimensions.length; i++)
				result.dimensions[i] = dimensions[i]; 
		}
		
		return result; 
	}
    
}
