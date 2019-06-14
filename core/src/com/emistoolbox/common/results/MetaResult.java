package com.emistoolbox.common.results;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

import java.util.List;
import java.util.Set;

public abstract interface MetaResult
{
    public EmisMetaHierarchy getHierarchy();

    public void setHierarchy(EmisMetaHierarchy paramEmisMetaHierarchy);

    public EmisIndicator getIndicator();

    public void setIndicator(EmisIndicator paramEmisIndicator);

    public int getMetaResultValueCount();

    public MetaResultValue getMetaResultValue(int paramInt);

    public List<MetaResultValue> getMetaResultValues();

    public void setMetaResultValues(List<MetaResultValue> paramList);

    public void addMetaResultValue(MetaResultValue paramMetaResultValue);

    public EmisContext getContext();

    public EmisContext getContextWithGlobalFilter();

    public void setContext(EmisContext paramEmisContext);

    public EmisContext getGlobalFilter(); 
    
    public void setGlobalFilter(EmisContext globalFilter); 
    
    public Set<EmisMetaDateEnum> getUsedDateTypes(); 
    
    public EmisMetaEntity getSeniorEntity(); 
}
