package com.emistoolbox.common.model.validation;

import java.util.List;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMetaData;

public interface EmisValidationResult 
{
	public List<EmisValidation> getValidations();
	public List<EmisValidationRule> getRules(); 

	public List<EmisMetaData> getAdditionalFields(); 
	public void setAdditionalFields(List<EmisMetaData> fields); 
	
	public List<EmisValidationResultItem> getItems(); 

	public List<EmisEntity> getChildren(EmisEntity parent); 
	public List<EmisValidationResultItem> getItems(EmisEntity item); 
}
