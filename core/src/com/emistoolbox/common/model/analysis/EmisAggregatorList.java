package com.emistoolbox.common.model.analysis;

import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

public interface EmisAggregatorList 
{
    String[] getAggregatorNames();

    Map<String, EmisAggregatorDef> getAggregators();

    EmisAggregatorDef getAggregator(String name);

    void setAggregator(String name, EmisAggregatorDef aggregator);

    void setAggregators(Map<String, EmisAggregatorDef> aggregators);

    Set<EmisMetaDateEnum> getUsedDateTypes(); 

    EmisMetaDateEnum getSeniorDateEnum();
    
    EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy); 
}
