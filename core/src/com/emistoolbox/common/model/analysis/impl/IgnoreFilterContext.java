package com.emistoolbox.common.model.analysis.impl;

import java.util.HashSet;
import java.util.Set;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.FilterTarget;
import com.emistoolbox.common.model.meta.EmisMetaData;

public class IgnoreFilterContext extends ContextAdaptor 
{
	private Set<String> ignoreFilters = new HashSet<String>(); 
	
	public static boolean hasIgnoreFilters(EmisAggregatorDef aggr)
	{
		if (aggr.getIgnoreFilters() == null)
			return false; 

		for (FilterTarget filter : aggr.getIgnoreFilters())
			if (filter.getEnumType() == null)
				return true; 

		return false; 
	}
	
	public IgnoreFilterContext(EmisAggregatorDef aggr, EmisContext context)
	{
		super(context); 
		for (FilterTarget filter : aggr.getIgnoreFilters())
		{
			if (filter.getEnumType() == null)
				ignoreFilters.add(getFieldKey(filter.getField())); 
		}
	}
	
	@Override
	public boolean allowEntityWithValue(EmisMetaData field, int value) 
	{
		if (field.getType() == EmisMetaData.EmisDataType.ENUM || field.getType() == EmisMetaData.EmisDataType.ENUM_SET || field.getType() == EmisMetaData.EmisDataType.BOOLEAN)
		{
			if (ignoreFilters.contains(getFieldKey(field)))
				return true; 
		} 
		
		return super.allowEntityWithValue(field, value);
	}
	
	private String getFieldKey(EmisMetaData field)
	{ return field.getEntity().getName() + "." + field.getName(); } 
}
