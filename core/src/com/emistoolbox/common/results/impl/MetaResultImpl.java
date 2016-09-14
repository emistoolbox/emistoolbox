package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.analysis.impl.MultipleContext;
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
    private EmisContext globalFilter; 
    private List<MetaResultValue> values = new ArrayList<MetaResultValue>();

    protected void copy(MetaResultImpl result)
    {
    	result.hierarchy = hierarchy; 
    	if (context != null)
    		result.context = ((Context) context).createCopy(); 
    	
    	if (globalFilter != null)
    		result.globalFilter = ((Context) globalFilter).createCopy(); 
    	
    	if (values != null)
    	{
    		result.values = new ArrayList<MetaResultValue>(); 
    		result.values.addAll(values); 
    	}
    }
    
    public EmisContext getContext()
    { return this.context; }

    @Override
	public EmisContext getContextWithGlobalFilter() 
    {
    	if (globalFilter == null)
    		return context; 
    	
    	return new MultipleContext(new EmisContext[] { context, globalFilter}, context.getDateType());
	}

	public EmisContext getGlobalFilter() 
    { return globalFilter; }

	public void setGlobalFilter(EmisContext globalFilter) 
	{ this.globalFilter = globalFilter; }

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
