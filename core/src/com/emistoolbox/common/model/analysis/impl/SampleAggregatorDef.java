package com.emistoolbox.common.model.analysis.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisSampleAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisSampleCollector;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

public abstract class SampleAggregatorDef extends AggregatorDef implements EmisSampleAggregatorDef 
{
    private AggregatorList aggregators; 

    public SampleAggregatorDef()
    { this(null); } 
    
    public SampleAggregatorDef(String[] aggregatorNames)
    { aggregators = new AggregatorList(aggregatorNames); } 

	@Override
	public EmisMetaEntity getSampleEntityType(EmisMetaHierarchy hierarchy)
	{
		EmisMetaEntity result = null;
		for (EmisAggregatorDef aggrs : aggregators.getAggregators().values())
		{
			if (result == null)
				result = aggrs.getMetaData().getEntity(); 
			else if (result.isChildOf(aggrs.getMetaData().getEntity(), hierarchy))
				result = aggrs.getMetaData().getEntity(); 
		}
				
		return result;
	}

	@Override
	public String[] getAggregatorNames() 
	{ return this.aggregators.getAggregatorNames(); } 

	@Override
	public Map<String, EmisAggregatorDef> getAggregators() 
	{ return this.aggregators.getAggregators(); } 

	@Override
	public EmisAggregatorDef getAggregator(String name) 
	{ return aggregators.getAggregator(name); }

	@Override
	public void setAggregator(String name, EmisAggregatorDef aggr) 
	{ this.aggregators.setAggregator(name, aggr); } 

	@Override
	public void setAggregators(Map<String, EmisAggregatorDef> aggregators) 
	{ this.aggregators.setAggregators(aggregators); }

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{
		if (aggregators == null)
			return Collections.emptySet(); 

		return aggregators.getUsedDateTypes(); 
	}

	@Override
	public EmisMetaDateEnum getSeniorDateEnum() 
	{
		if (aggregators == null)
			return null; 
		
		return aggregators.getSeniorDateEnum(); 
	}

	@Override
	public EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy) 
	{ 
		if (aggregators == null)
			return null; 
		
		return aggregators.getSeniorEntity(hierarchy); 
	}
}
