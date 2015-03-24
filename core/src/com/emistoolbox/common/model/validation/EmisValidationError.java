package com.emistoolbox.common.model.validation;

import java.util.Set;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public interface EmisValidationError 
{
	public EmisValidation getValidation(); 
	public void setValidation(EmisValidation validation); 
	
	public EmisValidationRule getValidationRule(); 
	public void setValidationRule(EmisValidationRule rule); 

	public EmisEnumTupleValue getDate(); 
	public EmisMetaEntity getEntityType(); 
	public void setEntityType(EmisMetaEntity entityType); 
	
	public Set<Integer> getEntityIds();
	public void addEntityId(int id);
}
