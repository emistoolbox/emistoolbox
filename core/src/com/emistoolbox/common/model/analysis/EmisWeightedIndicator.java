package com.emistoolbox.common.model.analysis;

public interface EmisWeightedIndicator extends EmisIndicator
{
	public EmisIndicator getIndicator();
	public void setIndicator(EmisIndicator indicator);
	
	public String getWeightedAggregatorName();
}
