package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.validation.EmisValidationFilter;

public class ValidationFilter implements EmisValidationFilter, Serializable 
{
	private Map<String, EmisEnumSet> filters = new HashMap<String, EmisEnumSet>();

	@Override
	public Map<String, EmisEnumSet> getFilters() 
	{ return filters; }

	@Override
	public void setFilters(Map<String, EmisEnumSet> filters) 
	{ this.filters = filters; }

	@Override
	public void addFilter(EmisEnumSet filter) 
	{ filters.put(filter.getEnum().getName(), filter); }

	@Override
	public void addAll(Collection<EmisEnumSet> filters) 
	{
		for (EmisEnumSet filter : filters)
			addFilter(filter); 
	}

	@Override
	public EmisEnumSet getFilterFor(EmisMetaEnum enumType) 
	{ return filters.get(enumType.getName()); } 
}
