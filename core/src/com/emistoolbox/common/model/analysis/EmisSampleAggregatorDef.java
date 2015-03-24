package com.emistoolbox.common.model.analysis;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

public interface EmisSampleAggregatorDef extends EmisAggregatorDef, EmisAggregatorList 
{
	public EmisMetaEntity getSampleEntityType(EmisMetaHierarchy hierarchy); 

	public EmisSampleCollector getSampleCollector(); 
}
