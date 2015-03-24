package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.validation.EmisValidationMinMaxRule;

public class ValidationMinMaxRuleImpl extends ValidationRuleImpl implements EmisValidationMinMaxRule, Serializable
{
	private Double minValue; 
	private Double maxValue; 
	
    public ValidationMinMaxRuleImpl()
    { super(new String[] { "value" }); }
    
    public ValidationMinMaxRuleImpl(String[] fieldNames)
    { super(fieldNames); } 

	@Override
	public String getRuleName() 
	{ return "Min/Max"; } 

	@Override
	public Double getMinValue() 
	{ return minValue; }

	@Override
	public void setMinValue(Double min) 
	{ this.minValue = min; } 

	@Override
	public Double getMaxValue() 
	{ return maxValue; } 

	@Override
	public void setMaxValue(Double max) 
	{ this.maxValue = max; } 

	@Override
	public String getValidationError(int[] values) 
	{
		double value = getValue(values);
		if (minValue != null && value < minValue)
			return "Below min value of " + minValue; 
		
		if (maxValue != null && value > maxValue)
			return "Above max value of " + maxValue; 
		
		return null; 
	}
}
