package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationError;
import com.emistoolbox.common.model.validation.EmisValidationRule;

public class ValidationErrorImpl implements EmisValidationError, Serializable 
{
	private EmisValidation validation; 
	private EmisValidationRule rule; 
	private EmisEnumTupleValue date; 
	private EmisMetaEntity entityType; 
	private Set<Integer> ids = new HashSet<Integer>(); 
	
	@Override
	public EmisValidation getValidation() 
	{ return validation; }

	@Override
	public EmisValidationRule getValidationRule() 
	{ return rule; }

	@Override
	public EmisEnumTupleValue getDate() 
	{ return date; }

	@Override
	public EmisMetaEntity getEntityType() 
	{ return entityType; } 

	@Override
	public Set<Integer> getEntityIds() 
	{ return ids; }

	public void setValidation(EmisValidation validation) 
	{ this.validation = validation; }

	public void setValidationRule(EmisValidationRule rule) 
	{ this.rule = rule; }

	public void setDate(EmisEnumTupleValue date) 
	{ this.date = date; }

	public void setEntityType(EmisMetaEntity entityType) 
	{ this.entityType = entityType; }

	public void setIds(Set<Integer> ids) 
	{ this.ids = ids; }
	
	public void addEntityId(int id)
	{ ids.add(id); }
}
