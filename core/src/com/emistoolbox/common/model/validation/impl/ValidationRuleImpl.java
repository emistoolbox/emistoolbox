package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.util.impl.NamedImpl;

public abstract class ValidationRuleImpl extends NamedImpl implements EmisValidationRule, Serializable 
{
	private String description; 
	
	private EmisMetaData[] fields = new EmisMetaData[2];
	private String[] fieldNames = null; 
	private EmisValidationFilter[] filters = new EmisValidationFilter[2]; 
	private EmisValidationFilter groupBy; 
	
	public ValidationRuleImpl()
	{}
	
	public ValidationRuleImpl(int i)
	{}

	public ValidationRuleImpl(String[] fieldNames)
	{
		this.fieldNames = fieldNames; 
		this.fields = new EmisMetaData[fieldNames.length]; 
		this.filters = new EmisValidationFilter[fieldNames.length]; 
	}

	@Override
	public String getDescription() 
	{ return description; }

	@Override
	public String getFieldName(int index) 
	{
		if (fieldNames != null && index < fieldNames.length)
			return fieldNames[index]; 

		return null; 
	}

	@Override
	public void setDescription(String description) 
	{ this.description = description; } 

	@Override
	public EmisMetaData getField()
	{ return fields[0]; } 
	
	@Override
	public void setField(EmisMetaData field)
	{ fields[0] = field; } 

	
	@Override
	public EmisValidationFilter getFilter()
	{ return filters[0]; }
	
	@Override
	public void setFilter(EmisValidationFilter filter)
	{ filters[0] = filter; } 

	@Override
	public int getFieldCount() 
	{ return fields.length; }

	@Override
	public EmisMetaData getField(int index)
	{ return fields[index]; }

	@Override
	public EmisValidationFilter getFilter(int index)
	{ return filters[index]; }

	@Override
	public int getDateOffset(int index) 
	{ return 0; }

	@Override
	public EmisValidationFilter getGroupBy() 
	{ return groupBy; }

	@Override
	public void setGroupBy(EmisValidationFilter groupBy) 
	{ this.groupBy = groupBy; } 

	public void setFields(EmisMetaData[] fields, EmisValidationFilter[] filters)
	{
		if (fields == null)
			fields = new EmisMetaData[0];
		if (filters == null)
			filters = new EmisValidationFilter[0];
		
		if (fields.length != filters.length)
			throw new IllegalArgumentException("Incompatible field and filter count");
		
		this.fields = fields;
		this.filters = filters;
	}

	protected double getValue(int[] values)
	{
		if (values == null || values.length == 0)
			throw new IllegalArgumentException("No data"); 
		
		if (values.length > 1)
			throw new IllegalArgumentException("More than one "); 
		
		return values[0]; 
	}
}
