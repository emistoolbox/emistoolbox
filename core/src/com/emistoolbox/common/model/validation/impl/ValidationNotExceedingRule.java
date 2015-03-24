package com.emistoolbox.common.model.validation.impl;

import com.emistoolbox.common.model.validation.EmisValidationNotExceedingRule;

public class ValidationNotExceedingRule extends ValidationRuleImpl implements EmisValidationNotExceedingRule 
{
	public ValidationNotExceedingRule()
	{ super(new String[] { "value", "limit" }); } 
	
	@Override
	public String getValidationError(int[] values) 
	{
		if (values.length != 2)
			throw new IllegalArgumentException("Expect two values."); 
		
		if (values[0] > values[1])
			return getField(0).getName() + " = " + values[0] + " > " + getField(1).getName() + " = " + values[1]; 
		
		return null; 
	}

	@Override
	public String getRuleName() 
	{ return "Not Exceed"; }
}
