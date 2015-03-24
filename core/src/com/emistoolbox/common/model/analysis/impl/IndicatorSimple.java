package com.emistoolbox.common.model.analysis.impl;

import java.util.List;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicatorSimple;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

public class IndicatorSimple extends IndicatorBase implements EmisIndicatorSimple
{
    private static final long serialVersionUID = 1L;

    public IndicatorSimple() 
    { super(new String[] { RESULT }); }

    public EmisAggregatorDef getAggregator()
    { return getAggregator(RESULT); }

    public void setAggregator(EmisAggregatorDef aggregator)
    { setAggregator(RESULT, aggregator); }

    public List<EmisMetaEnum> getSplitEnums()
    { return getEnums(getAggregator()); }

    public boolean isAddableResult(EmisMetaEnum split)
    { return true; } 
}
