package com.emistoolbox.common.model.validation;

import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.Named;

public interface EmisValidation extends Named
{
	public String getId(); 
	public void setId(String id); 
	
	public EmisMetaEntity getEntityType(); 
	public void setEntityType(EmisMetaEntity entityType); 
	
	public List<EmisValidationRule> getRules(); 
	public void setRules(List<EmisValidationRule> rules);

	public void clearAdditionalFields(); 
	public List<EmisMetaData> getAdditionalFields(); 
	public void addAdditionalField(EmisMetaData field); 
}
