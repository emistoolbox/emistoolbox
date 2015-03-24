package com.emistoolbox.common.model.validation;

import java.util.List;

import com.emistoolbox.common.model.EmisEntity;

public interface EmisValidationErrorContext 
{
	public EmisEntity getEntity(); 
	public void setEntity(EmisEntity entity); 
	
	public List<EmisValidationErrorContext> getChildrenContexts(); 
	public void setChildrenContexts(List<EmisValidationErrorContext> contexts); 
	public void addChildrenContext(EmisValidationErrorContext context); 
	
	public List<EmisValidationError> getErrors(); 
	public void setErrors(List<EmisValidationError> errors); 
	public void addError(EmisValidationError error);
}
