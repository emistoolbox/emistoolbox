package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultValue;

import java.util.List;
import java.util.Set;

public class MetaResultAdaptor implements MetaResult
{
    private MetaResult metaResult;

    public MetaResultAdaptor(MetaResult metaResult) 
    { this.metaResult = metaResult; }

    public EmisContext getContext()
    { return this.metaResult.getContext(); }

    public EmisMetaHierarchy getHierarchy()
    { return this.metaResult.getHierarchy(); }

    public EmisIndicator getIndicator()
    { return this.metaResult.getIndicator(); }

	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{ return this.metaResult.getUsedDateTypes(); }

	public void addMetaResultValue(MetaResultValue value)
    {
        throw new IllegalArgumentException("Adaptor doesn't support add");
    }

    public MetaResultValue getMetaResultValue(int index)
    {
        return this.metaResult.getMetaResultValue(index);
    }

    public int getMetaResultValueCount()
    {
        return this.metaResult.getMetaResultValueCount();
    }

    public List<MetaResultValue> getMetaResultValues()
    {
        return this.metaResult.getMetaResultValues();
    }

    public void setMetaResultValues(List<MetaResultValue> values)
    {
        throw new IllegalArgumentException("Adaptor doesn't support set*");
    }

    public void setContext(EmisContext context)
    {
        throw new IllegalArgumentException("Adaptor doesn't support set*");
    }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    {
        throw new IllegalArgumentException("Adaptor doesn't support set*");
    }

    public void setIndicator(EmisIndicator indicator)
    {
        throw new IllegalArgumentException("Adaptor doesn't support set*");
    }
}
