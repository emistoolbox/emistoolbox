package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisAggregatorList;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisSampleAggregatorDef;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.impl.NamedImpl;
import com.emistoolbox.server.model.analysis.EmisAggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class IndicatorBase extends NamedImpl implements EmisIndicator, EmisAggregatorList
{
	private String groupName; 
    private static final long serialVersionUID = 1L;

    private AggregatorList aggregators; 
    
    private double badThreshold = (0.0D / 0.0D);
    private String badText;
    private double goodThreshold = (0.0D / 0.0D);
    private String goodText;
    private boolean biggerIsBetter;
    private double maxValue = (0.0D / 0.0D);

    private String yAxisLabel; 
    
    public IndicatorBase() 
    { this(null); }

    public IndicatorBase(String[] aggregatorNames) 
    { this.aggregators = new AggregatorList(aggregatorNames); }

    
    @Override
	public String getGroupName() 
    { return groupName; }

	@Override
	public String getYAxisLabel() 
	{ return yAxisLabel; } 

	@Override
	public void setYAxisLabel(String yAxis) 
	{ this.yAxisLabel = yAxis; } 

	@Override
	public void setGroupName(String groupName) 
	{ this.groupName = groupName; } 

    public double getBadThreshold()
    {
        return this.badThreshold;
    }

    public String getBadThresholdText()
    {
        return this.badText;
    }

    public double getGoodThreshold()
    {
        return this.goodThreshold;
    }

    public String getGoodThresholdText()
    {
        return this.goodText;
    }

    public double getMaxValue()
    {
        return this.maxValue;
    }

    public void setMaxValue(double maxValue)
    {
        this.maxValue = maxValue;
    }

    public void setThreshold(double good, String goodText, double bad, String badText)
    {
        this.goodThreshold = good;
        this.goodText = goodText;
        this.badThreshold = bad;
        this.badText = badText;
        this.biggerIsBetter = (good > bad);
    }

    public void setThreshold(double threshold, String text, boolean biggerIsBetter)
    {
        this.goodThreshold = threshold;
        this.goodText = text;
        this.badThreshold = (0.0D / 0.0D);
        this.badText = null;
        this.biggerIsBetter = biggerIsBetter;
    }

    public boolean getBiggerIsBetter()
    {
        return this.biggerIsBetter;
    }

    protected List<EmisMetaEnum> getEnums(EmisAggregatorDef aggr)
    {
        if (aggr == null)
            return new ArrayList<EmisMetaEnum>(); 
        
        EmisMetaData data = aggr.getMetaData();
        if (data == null)
            return new ArrayList<EmisMetaEnum>(); 

        List<EmisMetaEnum> result = new ArrayList<EmisMetaEnum>(); 
        if (data.getEnumType() != null)
        	result.add(data.getEnumType()); 
        
        EmisMetaEnumTuple tuple = data.getArrayDimensions(); 
        if (tuple == null)
        	return result; 
        
        result.addAll(Arrays.asList(tuple.getEnums())); 
        return result; 
    }

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes() 
	{ return aggregators.getUsedDateTypes(); }

	@Override
	public EmisMetaDateEnum getSeniorDateEnum() 
	{ return aggregators.getSeniorDateEnum(); }

	@Override
	public EmisMetaEntity getSeniorEntity(EmisMetaHierarchy hierarchy)
	{ return aggregators.getSeniorEntity(hierarchy); }

	@Override
	public String[] getAggregatorNames() 
	{ return this.aggregators.getAggregatorNames(); } 

	@Override
	public Map<String, EmisAggregatorDef> getAggregators() 
	{ return this.aggregators.getAggregators(); } 

	@Override
	public EmisAggregatorDef getAggregator(String name) 
	{ return aggregators.getAggregator(name); }

	@Override
	public void setAggregator(String name, EmisAggregatorDef aggr) 
	{ this.aggregators.setAggregator(name, aggr); } 

	@Override
	public void setAggregators(Map<String, EmisAggregatorDef> aggregators) 
	{ this.aggregators.setAggregators(aggregators); }
}
