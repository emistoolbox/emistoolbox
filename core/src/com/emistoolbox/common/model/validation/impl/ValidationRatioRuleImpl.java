package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.validation.EmisValidationRatioRule;
import com.emistoolbox.common.model.validation.EmisValidationFilter;

public class ValidationRatioRuleImpl extends ValidationMinMaxRuleImpl implements EmisValidationRatioRule, Serializable
{
	private EmisMetaData baseLineField; 
	private EmisValidationFilter baseLineFilter; 
	
	private Integer maxCountDiff; 
	private Integer maxPercentDiff; 
	
	public ValidationRatioRuleImpl()
	{ super(new String[] { "numerator", "denominator" }); } 
	
	@Override
	public EmisMetaData getBaseLineField() 
	{ return baseLineField; }

	@Override
	public void setBaseLineField(EmisMetaData field) 
	{ this.baseLineField = field; } 

	@Override
	public EmisValidationFilter getBaseLineFilter() 
	{ return baseLineFilter; } 

	@Override
	public void setBaseLineFilter(EmisValidationFilter filter) 
	{ this.baseLineFilter = filter; }

	@Override
	public int getFieldCount() 
	{ return 2; }

	@Override
	public EmisMetaData getField(int index) 
	{
		if (index == 0)
			return getField();
		else if (index == 1)
			return getBaseLineField();
		else
			throw new IndexOutOfBoundsException("Ratio can only have two fields - index was " + index);
	}

	@Override
	public EmisValidationFilter getFilter(int index) 
	{
		if (index == 0)
			return getFilter(); 
		else if (index == 1)
			return getBaseLineFilter(); 
		else 
			throw new IndexOutOfBoundsException("Ratio can only have two fields - index was " + index);
	}

	@Override
	public void setFields(EmisMetaData[] fields, EmisValidationFilter[] filters) 
	{
		if (fields.length != getFieldCount() || filters.length != getFieldCount())
			throw new IllegalArgumentException("Fields and filters need to have two entries");

		setField(fields[0]); 
		setBaseLineField(fields[1]); 
		setFilter(filters[0]);
		setBaseLineFilter(filters[1]);
	}

	@Override
	public void setMaxCountDifference(Integer count) 
	{ this.maxCountDiff = count; }

	@Override
	public Integer getMaxCountDifference() 
	{ return maxCountDiff; }

	@Override
	public void setMaxPercentDifference(Integer percent) 
	{ this.maxPercentDiff = percent; }

	@Override
	public Integer getMaxPercentDifference() 
	{ return maxPercentDiff; } 

	@Override
	protected double getValue(int[] values)
	{
		if (values == null || values.length == 0)
			throw new IllegalArgumentException("No data."); 
		
		if (values.length != 2)
			throw new IllegalArgumentException("Expeted 2 data points."); 

		if (values[1] == 0 || values[0] == -1 || values[1] == -1)
			return Double.NaN; 
		
		return (double) values[0] / (double) values[1]; 
	}

	@Override
	public String getValidationError(int[] values) 
	{
		String result = super.getValidationError(values);
		if (result != null)
			return result; 
		
		if (maxCountDiff != null && Math.abs(values[0] - values[1]) > maxCountDiff)
			return "Exceeds max count change of " + maxCountDiff; 
		
		if (values[0] <= 0 && values[1] <= 0)
			return null; 
		
		if (maxPercentDiff != null && (Math.abs(values[0] - values[1]) * 100 / values[0]) > maxPercentDiff)
			return "Exceeds max percentage change of " + maxPercentDiff + "%"; 
					
		return null; 
	}

	@Override
	public String getRuleName() 
	{ return "Ratio"; }
}
