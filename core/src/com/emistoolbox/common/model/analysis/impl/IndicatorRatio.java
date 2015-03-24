package com.emistoolbox.common.model.analysis.impl;

import java.util.List;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicatorRatio;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;

public class IndicatorRatio extends IndicatorBase implements EmisIndicatorRatio
{
    private static final long serialVersionUID = 1L;
    private double factor = 1.0D;

    public IndicatorRatio() 
    { super(new String[] { NUMERATOR, DENOMINATOR }); }

    public EmisAggregatorDef getDenominator()
    {
        return getAggregator(DENOMINATOR);
    }

    public double getFactor()
    {
        return this.factor;
    }

    public EmisAggregatorDef getNumerator()
    { return getAggregator(NUMERATOR); }

    public void setDenominator(EmisAggregatorDef denominator)
    { setAggregator(DENOMINATOR, denominator); }

    public void setFactor(double factor)
    {
        this.factor = factor;
    }

    public void setNumerator(EmisAggregatorDef numerator)
    { setAggregator(NUMERATOR, numerator); }

    @Override
    public List<EmisMetaEnum> getSplitEnums()
    {
        // Return enums used in numerator. 
        return getEnums(getAggregator(NUMERATOR)); 
    }

    public boolean isAddableResult(EmisMetaEnum splitEnum)
    {
        if (!hasEnum(getAggregator(NUMERATOR), splitEnum))
            return false; 
        
        if (hasEnum(getAggregator(DENOMINATOR), splitEnum))
            return false; 
        
        return true; 
    }
    
    private boolean hasEnum(EmisAggregatorDef aggr, EmisMetaEnum needleEnum)
    {
        if (aggr == null)
            return false; 
        
        EmisMetaData field = aggr.getMetaData();
        if (field == null) 
            return false; 
        
        EmisMetaEnumTuple fieldEnums = field.getArrayDimensions(); 
        if (fieldEnums == null)
            return false; 
        
        for (EmisMetaEnum e : fieldEnums.getEnums())
            if (NamedUtil.sameName(e, needleEnum))
                return true; 
                
        return false; 
    }
}
