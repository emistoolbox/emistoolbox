package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.results.PriorityMetaResult;

public class PriorityMetaResultImpl extends MetaResultImpl implements PriorityMetaResult
{
    private EmisMetaEntity entityType;
    private String[] additionalFields = new String[] { "name" }; 

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
}
