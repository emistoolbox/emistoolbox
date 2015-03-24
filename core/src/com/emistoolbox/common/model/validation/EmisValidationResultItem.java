package com.emistoolbox.common.model.validation;

import com.emistoolbox.common.model.EmisEntity;

public interface EmisValidationResultItem 
{
	public EmisEntity[] getEntities();
	public String[] getResults(); 

	public String[] getAdditionalValues(); 
	public void setAdditionalValues(String[] values); 
}
