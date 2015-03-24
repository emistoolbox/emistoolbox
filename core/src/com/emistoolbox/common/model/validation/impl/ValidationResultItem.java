package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.validation.EmisValidationResultItem;

public class ValidationResultItem implements EmisValidationResultItem, Serializable 
{
	private EmisEntity[] entityPath; 
	private String[] results; 
	private String[] additionalValues; 
	
	@Override
	public EmisEntity[] getEntities()
	{ return entityPath; }

	public void setEntities(EmisEntity[] entities)
	{ this.entityPath = entities; } 
	
	@Override
	public String[] getResults() 
	{ return results; }
	
	public void setResults(String[] results)
	{ this.results = results; }

	@Override
	public String[] getAdditionalValues() 
	{ return additionalValues; } 

	@Override
	public void setAdditionalValues(String[] values) 
	{ this.additionalValues = values; }
}
