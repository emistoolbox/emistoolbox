package com.emistoolbox.common.model.analysis;

import java.util.Map;

public interface EmisSampleCollector
{
	public void addSample(Map<String, Double> values); 
	
	public double getFinalValue(); 
}
