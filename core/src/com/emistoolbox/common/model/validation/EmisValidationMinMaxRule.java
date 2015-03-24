package com.emistoolbox.common.model.validation;

public interface EmisValidationMinMaxRule extends EmisValidationRule
{
	public Double getMinValue(); 
	public void setMinValue(Double min); 
	
	public Double getMaxValue();
	public void setMaxValue(Double max); 
}
