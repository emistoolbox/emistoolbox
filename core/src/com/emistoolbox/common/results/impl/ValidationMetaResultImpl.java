package com.emistoolbox.common.results.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.results.ValidationMetaResult;

public class ValidationMetaResultImpl extends MetaResultImpl implements Serializable, ValidationMetaResult 
{
	private List<String> validationIds = new ArrayList<String>(); 
    private EmisMetaEntity entityType;
	
	@Override
	public List<String> getValidationIds() 
	{ return validationIds; }

	@Override
	public void setValidationIds(List<String> validationIds) 
	{ this.validationIds = validationIds; }

    public EmisMetaEntity getListEntity()
    { return this.entityType; }

    public void setListEntity(EmisMetaEntity entityType)
    { this.entityType = entityType; }

	@Override
	public void addValidationId(String validationId) 
	{ 
		if (!hasValidationId(validationId))
			this.validationIds.add(validationId); 
	} 

	@Override
	public boolean hasValidationId(String validationId) 
	{ return validationIds.contains(validationId); }

	public byte getDateIndex()
	{
		int result = -1; 
		for (EmisEnumTupleValue date : getContext().getDates())
			result = Math.max(result, date.getIndex()[0]);  
		
		return (byte) result; 
	}
}
