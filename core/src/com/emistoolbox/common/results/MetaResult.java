package com.emistoolbox.common.results;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

import java.util.List;
import java.util.Set;

public abstract interface MetaResult
{
    public abstract EmisMetaHierarchy getHierarchy();

    public abstract void setHierarchy(EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract EmisIndicator getIndicator();

    public abstract void setIndicator(EmisIndicator paramEmisIndicator);

    public abstract int getMetaResultValueCount();

    public abstract MetaResultValue getMetaResultValue(int paramInt);

    public abstract List<MetaResultValue> getMetaResultValues();

    public abstract void setMetaResultValues(List<MetaResultValue> paramList);

    public abstract void addMetaResultValue(MetaResultValue paramMetaResultValue);

    public abstract EmisContext getContext();

    public abstract void setContext(EmisContext paramEmisContext);
    
    public Set<EmisMetaDateEnum> getUsedDateTypes(); 
}
