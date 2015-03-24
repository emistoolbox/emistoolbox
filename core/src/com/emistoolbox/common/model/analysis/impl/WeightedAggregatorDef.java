package com.emistoolbox.common.model.analysis.impl;

import java.util.Map;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisSampleCollector;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.results.ResultUtil;

public class WeightedAggregatorDef extends SampleAggregatorDef 
{
	public static final String AGGR_VALUE = "value"; 
	public static final String AGGR_WEIGHT = "weight"; 

	public WeightedAggregatorDef()
	{ super(new String[] { AGGR_VALUE, AGGR_WEIGHT }); }

	@Override
	public EmisMetaData getMetaData() 
	{
		EmisAggregatorDef aggr = getAggregator(AGGR_VALUE); 
		if (aggr == null)
			return null; 
		
		return aggr.getMetaData();  
	}

	@Override
	public EmisSampleCollector getSampleCollector() 
	{ return new WeightedSampleCollector(); }
}

class WeightedSampleCollector implements EmisSampleCollector
{
	private double weightedValue = Double.NaN; 
	private double weightTotal = Double.NaN;
	
	@Override
	public void addSample(Map<String, Double> values) 
	{
		Double weight = values.get(WeightedAggregatorDef.AGGR_WEIGHT); 
		if (weight == null || Double.isNaN(weight) || weight < 0.0d)
			return; 
		
		Double value = values.get(WeightedAggregatorDef.AGGR_VALUE); 
		if (value == null || Double.isNaN(value) || value < 0.0d)
			return; 

		weightedValue = ResultUtil.add(weightedValue, values.get(WeightedAggregatorDef.AGGR_VALUE) * values.get(WeightedAggregatorDef.AGGR_WEIGHT)); 
		weightTotal = ResultUtil.add(weightTotal, values.get(WeightedAggregatorDef.AGGR_WEIGHT)); 
	}

	@Override
	public double getFinalValue() 
	{
		if (Double.isNaN(weightTotal) || Double.isNaN(weightedValue) || weightTotal == 0)
			return Double.NaN; 
		
		return weightedValue / weightTotal;
	}
}