package com.emistoolbox.common.results.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.results.PriorityMetaResult;

public class PriorityMetaResultImpl extends MetaResultImpl implements PriorityMetaResult, Serializable
{
    private EmisMetaEntity entityType;
    private String[] additionalFields = new String[] { "name" }; 
    private boolean filterEmpty; 

    public EmisMetaEntity getListEntity()
    { return this.entityType; }

    public void setListEntity(EmisMetaEntity entityType)
    { this.entityType = entityType; }

	@Override
	public String[] getAdditionalFields() 
	{ return additionalFields; }

	@Override
	public void setAdditionalFields(String[] fields) 
	{ this.additionalFields = fields; }

	@Override
	public boolean getFilterEmpty() 
	{ return filterEmpty; } 

	@Override
	public void setFilterEmpty(boolean filter) 
	{ this.filterEmpty = filter; }

	@Override
	public PriorityMetaResult createCopy() 
	{
		PriorityMetaResultImpl result = new PriorityMetaResultImpl(); 
		super.copy(result);
		result.setListEntity(getListEntity());
		result.setAdditionalFields(getAdditionalFields());
		
		return result;
	} 
	
	
}
