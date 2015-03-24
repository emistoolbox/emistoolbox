package com.emistoolbox.common.model.analysis;

import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.Named;

public interface EmisAggregatorDef extends Named
{
    public EmisMetaEntity getEntity();

    public void setEntity(EmisMetaEntity paramEmisMetaEntity);

    public EmisMetaData getMetaData();
    
    public EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy);  

    public Set<EmisMetaDateEnum> getUsedDateTypes(); 

    public void setMetaData(EmisMetaData metaData);

    public EmisContext getContext();

    public EmisMetaDateEnum getCountDateType(); 
    public void setCountDateType(EmisMetaDateEnum dateType); 

    public List<FilterTarget> getIgnoreFilters();  
    public void setIgnoreFilters(List<FilterTarget> filterTargets); 
    public void addIgnoreFilter(FilterTarget filterTarget); 

    public boolean ignoreFilter(EmisMetaData field);
    public boolean ignoreFilter(EmisMetaData field, EmisMetaEnum enumType);

    public void setContext(EmisContext paramEmisContext);
}
