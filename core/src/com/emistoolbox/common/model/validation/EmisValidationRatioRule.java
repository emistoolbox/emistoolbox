package com.emistoolbox.common.model.validation;

import com.emistoolbox.common.model.meta.EmisMetaData;

public interface EmisValidationRatioRule extends EmisValidationMinMaxRule 
{
	public EmisMetaData getBaseLineField();
	public void setBaseLineField(EmisMetaData field); 
	
	public EmisValidationFilter getBaseLineFilter(); 
	public void setBaseLineFilter(EmisValidationFilter filter); 
	
	public void setMaxCountDifference(Integer count);
	public Integer getMaxCountDifference();
	
	public void setMaxPercentDifference(Integer percent);
	public Integer getMaxPercentDifference(); 
}
