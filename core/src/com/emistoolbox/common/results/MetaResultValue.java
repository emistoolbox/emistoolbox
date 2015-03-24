package com.emistoolbox.common.results;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

import java.io.Serializable;
import java.util.Set;

public abstract interface MetaResultValue extends Serializable
{
    public abstract String getName(boolean paramBoolean);

    public abstract EmisIndicator getIndicator();

    public abstract void setIndicator(EmisIndicator paramEmisIndicator);

    public abstract String getAggregatorKey();

    public abstract void setAggregatorKey(String paramString);

    public abstract String getAggregatorName();

    public abstract void setAggregatorName(String paramString);

    public abstract Double getTarget();

    public abstract void setTarget(Double paramDouble);

    public abstract String getFormat();
    
    public abstract Set<EmisMetaDateEnum> getUsedDateTypes(); 
}

