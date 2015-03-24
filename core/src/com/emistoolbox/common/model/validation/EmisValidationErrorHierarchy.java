package com.emistoolbox.common.model.validation;

import java.util.List;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public interface EmisValidationErrorHierarchy 
{
	public EmisEntity getEntity(); 
	public void setEntity(EmisEntity entity); 
	
	public List<EmisValidationError> getErrors(); 
	public void setErrors(List<EmisValidationError> errors); 
	public void addError(EmisValidationError error);
	public EmisValidationError findError(EmisValidation validation, EmisValidationRule rule, EmisMetaEntity entityType); 

	public List<EmisValidationErrorHierarchy> getChildren(); 
	public void setChildren(List<EmisValidationErrorHierarchy> children); 
	public void addChild(EmisValidationErrorHierarchy child); 

	public boolean hasAnyEntries();
	public EmisValidationErrorHierarchy findChild(EmisEntity entity); 
}
