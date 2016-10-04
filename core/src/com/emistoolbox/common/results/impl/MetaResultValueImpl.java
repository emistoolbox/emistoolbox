package com.emistoolbox.common.results.impl;

import java.util.HashSet;
import java.util.Set;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.impl.IndicatorRatio;
import com.emistoolbox.common.model.analysis.impl.IndicatorSimple;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResultValue;

public class MetaResultValueImpl implements MetaResultValue
{
    private EmisIndicator indicator;
    private String aggregatorKey;
    private String aggregatorName;
    private Double target;

    public MetaResultValueImpl() 
    {}

    public MetaResultValueImpl(EmisIndicator indicator) 
    { this.indicator = indicator; }

    public MetaResultValueImpl(EmisIndicator indicator, String name) {
        this.indicator = indicator;
        this.aggregatorKey = name;
    }

    @Override
	public EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy) 
    {
    	if (aggregatorKey != null)
    		return indicator.getAggregator(aggregatorKey).getSeniorEntity(hierarchy); 

		return indicator.getSeniorEntity(hierarchy);
	}

	public String getName(boolean fullName)
    {
        if (this.aggregatorKey == null)
        {
            return this.indicator.getName();
        }
        String aggr = this.aggregatorName;
        if (aggr == null)
        {
            aggr = this.aggregatorKey;
        }
        if (this.aggregatorKey.startsWith("?"))
            return "required " + aggr + (this.target == null ? "" : new StringBuilder().append(" to reach ").append(this.target).toString());
        if (this.aggregatorKey.startsWith("+"))
        {
            return "surplus " + aggr + (this.target == null ? "" : new StringBuilder().append(" over ").append(this.target).toString());
        }
        return (fullName ? this.indicator.getName() + " - " : "") + aggr;
    }

    public String getAggregatorKey()
    {
        return this.aggregatorKey;
    }

    public EmisIndicator getIndicator()
    {
        return this.indicator;
    }

    public void setAggregatorKey(String name)
    {
        this.aggregatorKey = name;
    }

    public String getAggregatorName()
    {
        return this.aggregatorName;
    }

    public void setAggregatorName(String aggregatorName)
    {
        this.aggregatorName = aggregatorName;
    }

    public Double getTarget()
    {
        return this.target;
    }

    public void setTarget(Double target)
    {
        this.target = target;
    }

    public void setIndicator(EmisIndicator indicator)
    {
        this.indicator = indicator;
    }

    public String getFormat()
    {
        if (this.aggregatorKey == null)
        {
            if ((this.indicator instanceof IndicatorRatio))
            {
                if (Math.abs(((IndicatorRatio) indicator).getFactor() - 100.0) < 0.1)
                    return "#,##0.#%"; 

                return "#,##0.0";
            }
            if ((this.indicator instanceof IndicatorSimple))
            {
                return "#,##0";
            }
        }

        return "#,##0";
    }

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{
		if (this.aggregatorKey == null)
			return indicator.getUsedDateTypes(); 
		else
		{
			Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>(); 
			EmisAggregatorDef aggrDef = indicator.getAggregator(getKeyName(aggregatorKey));
			result.add(aggrDef.getMetaData().getDateType()); 
			return result; 
		}
	}
	
	public static String getKeyName(String aggregatorKey)
	{
		if (aggregatorKey.startsWith("?") || aggregatorKey.startsWith("+"))
			return aggregatorKey.substring(1); 
		
		return aggregatorKey; 
	}
}
