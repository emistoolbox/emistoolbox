package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.validation.EmisValidationTimeRatioRule;

public class ValidationTimeRatioRuleImpl extends ValidationRatioRuleImpl implements EmisValidationTimeRatioRule, Serializable 
{
	private EmisMetaDateEnum dateType;
	
	@Override
	public EmisMetaDateEnum getDateType()
	{ return dateType; }

	@Override
	public void setDateType(EmisMetaDateEnum dateType)
	{ this.dateType = dateType; }
	
	@Override
	public int getDateOffset(int index)
	{ return index == 0 ? 0 : -1; }

	@Override
	public String getRuleName() 
	{ return "Time Ratio"; } 
}
