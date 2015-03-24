package com.emistoolbox.common.model.validation;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

public interface EmisValidationTimeRatioRule extends EmisValidationRule 
{
	public EmisMetaDateEnum getDateType(); 
	public void setDateType(EmisMetaDateEnum dateType); 

	public void setMaxCountDifference(Integer count);
	public Integer getMaxCountDifference();
	
	public void setMaxPercentDifference(Integer percent);
	public Integer getMaxPercentDifference(); 
}
