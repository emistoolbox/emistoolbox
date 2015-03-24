package com.emistoolbox.common.model.analysis.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.FilterTarget;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.impl.NamedImpl;

public class AggregatorDef extends NamedImpl implements EmisAggregatorDef
{
    private static final long serialVersionUID = 1L;

    private EmisContext context;
    private EmisMetaEntity entity;
    private EmisMetaData data;
    
    private EmisMetaDateEnum countDateType = null; 
    private List<FilterTarget> ignoreFilters = new ArrayList<FilterTarget>(); 
    
    public EmisContext getContext()
    { return this.context; }

    public EmisMetaEntity getEntity()
    { return this.entity; }

    public EmisMetaData getMetaData()
    { return this.data; }

    public void setContext(EmisContext context)
    { this.context = context; }

    public void setEntity(EmisMetaEntity entity)
    {
        this.entity = entity;
        this.data = null;
    }

    public void setMetaData(EmisMetaData data)
    {
        this.data = data;
        if (data != null)
            this.entity = data.getEntity();
    }
    
    public EmisMetaDateEnum getCountDateType()
    { return countDateType; } 

    public void setCountDateType(EmisMetaDateEnum dateType)
    { this.countDateType = dateType; } 

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{
		if (data != null && data.getDateType() != null)
			return Collections.singleton(data.getDateType());

		if (countDateType != null)
			return Collections.singleton(countDateType);
		
		return Collections.emptySet(); 
	}

	@Override
	public EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy) 
	{
		if (data == null)
			return null; 

		return data.getEntity(); 
	}

    public List<FilterTarget> getIgnoreFilters()
    { return ignoreFilters; } 
    
    public void setIgnoreFilters(List<FilterTarget> filterTargets)
    { this.ignoreFilters = filterTargets; } 
    
    public void addIgnoreFilter(FilterTarget filterTarget)
    { ignoreFilters.add(filterTarget); }
    
    public boolean ignoreFilter(EmisMetaData field)
    {
    	for (FilterTarget ignoreFilter : ignoreFilters)
    	{
    		if (ignoreFilter.matches(field))
    			return true; 
    	} 
    	
    	return false; 
    }
    
    public boolean ignoreFilter(EmisMetaData field, EmisMetaEnum enumType)
    {
    	for (FilterTarget ignoreFilter : ignoreFilters)
    	{
    		if (ignoreFilter.matches(field, enumType))
    			return true; 
    	} 
    	
    	return false; 
    }
}
