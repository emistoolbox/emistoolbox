package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MetaResultImpl implements Serializable, MetaResult
{
    private static final long serialVersionUID = 1L;
    private EmisMetaHierarchy hierarchy;
    private EmisContext context;
    private List<MetaResultValue> values = new ArrayList<MetaResultValue>();

    public EmisContext getContext()
    { return this.context; }

    public EmisMetaHierarchy getHierarchy()
    { return this.hierarchy; }

    public Set<EmisMetaDateEnum> getUsedDateTypes()
    { return MetaResultUtil.getUsedDateTypes(values, null); }
    
    public EmisIndicator getIndicator()
    {
        if (this.values.size() > 1)
            throw new IllegalArgumentException("getIndicator doesn't allow more than one MetaResultValue.");
        if (this.values.size() == 0)
            return null;

        MetaResultValue result = (MetaResultValue) this.values.get(0);
        if (result.getAggregatorKey() != null)
            throw new IllegalArgumentException("getIndicator doesn't allow aggregator name");

        return result.getIndicator();
    }

    public void setContext(EmisContext context)
    { this.context = context; }

    public void setHierarchy(EmisMetaHierarchy hierarchy)
    { this.hierarchy = hierarchy; }

    public void setIndicator(EmisIndicator indicator)
    {
        this.values.clear();
        this.values.add(new MetaResultValueImpl(indicator));
    }

    public void addMetaResultValue(MetaResultValue value)
    { this.values.add(value); }

    public MetaResultValue getMetaResultValue(int index)
    { return (MetaResultValue) this.values.get(index); }

    public int getMetaResultValueCount()
    { return this.values.size(); }

    public List<MetaResultValue> getMetaResultValues()
    { return this.values; }

    public void setMetaResultValues(List<MetaResultValue> values)
    { this.values = values; }
}
