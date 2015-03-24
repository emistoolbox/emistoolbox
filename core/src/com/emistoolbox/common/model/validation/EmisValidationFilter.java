package com.emistoolbox.common.model.validation;

import java.util.Collection;
import java.util.Map;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

public interface EmisValidationFilter 
{
	public Map<String, EmisEnumSet> getFilters(); 
	public void setFilters(Map<String, EmisEnumSet> filters); 
	public void addFilter(EmisEnumSet filter); 
	public void addAll(Collection<EmisEnumSet> filters); 
	public EmisEnumSet getFilterFor(EmisMetaEnum enumType);
}
