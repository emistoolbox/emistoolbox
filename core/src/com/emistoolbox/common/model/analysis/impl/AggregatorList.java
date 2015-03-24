package com.emistoolbox.common.model.analysis.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisAggregatorList;
import com.emistoolbox.common.model.analysis.EmisSampleAggregatorDef;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;

public class AggregatorList implements EmisAggregatorList, Serializable
{
    private String[] aggregatorNames;
    private Map<String, EmisAggregatorDef> aggregators = new HashMap<String, EmisAggregatorDef>();

    public AggregatorList()
    {}
    
    public AggregatorList(String[] names)
    { this.aggregatorNames = names; } 
    
    @Override
	public String[] getAggregatorNames() 
	{ return this.aggregatorNames; }

	@Override
    public Map<String, EmisAggregatorDef> getAggregators()
    { return this.aggregators; }

	@Override
    public EmisAggregatorDef getAggregator(String name)
    { return (EmisAggregatorDef) this.aggregators.get(name); }

    @Override
    public void setAggregator(String name, EmisAggregatorDef aggregator)
    {
        if (!validAggregatorName(name))
            throw new IllegalArgumentException("Aggregator '" + name + "' not allowed.");

        this.aggregators.put(name, aggregator);
    }

    @Override
    public void setAggregators(Map<String, EmisAggregatorDef> aggregators)
    {
        this.aggregators = aggregators;
    }

    private boolean validAggregatorName(String name)
    {
        if (this.aggregatorNames == null)
        {
            return true;
        }
        for (String tmp : this.aggregatorNames)
        {
            if (tmp.equals(name))
                return true;
        }
        return false;
    }
    
    public EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy)
    {
        NamedIndexList entities = hierarchy.getEntityOrder();

        int result = -1;
        for (EmisAggregatorDef aggregator : getAggregators().values())
        {
            EmisMetaEntity entity = aggregator.getSeniorEntity(hierarchy);
            if (entity == null)
            	entity = aggregator.getEntity(); 
            if (entity == null)
                continue;

            int index = NamedUtil.findIndex(entity, entities);
            if (result == -1 || result > index)
                result = index;
        }
        
        if (result == -1)
            return null;

        return (EmisMetaEntity) entities.get(result);
    }
    
	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{
		Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>(); 
		for (EmisAggregatorDef aggr : getAggregators().values())
			result.addAll(aggr.getUsedDateTypes()); 
		
		return result;
	}

	@Override
	public EmisMetaDateEnum getSeniorDateEnum() 
	{
		EmisMetaDateEnum result = null; 
		for (EmisMetaDateEnum dateType : getUsedDateTypes())
		{
			if (result == null || result.getDimensions() > dateType.getDimensions())
				result = dateType; 
		}
		
		return result; 
	}
}
