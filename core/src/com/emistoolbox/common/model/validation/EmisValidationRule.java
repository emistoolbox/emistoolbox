package com.emistoolbox.common.model.validation;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.util.Named;

public interface EmisValidationRule extends Named
{
	public String getDescription(); 
	public void setDescription(String description); 
	
	public EmisMetaData getField(); 
	public void setField(EmisMetaData field); 

	public EmisValidationFilter getFilter(); 
	public void setFilter(EmisValidationFilter filter); 

	public int getFieldCount(); 
	public EmisMetaData getField(int index); 
	public EmisValidationFilter getFilter(int index); 
	public String getFieldName(int index); 
	public int getDateOffset(int index); 

	public void setFields(EmisMetaData[] fields, EmisValidationFilter[] filters); 
	
	public EmisValidationFilter getGroupBy();
	public void setGroupBy(EmisValidationFilter groupBy); 
	
	public String getValidationError(int[] values);
	
	public String getRuleName(); 
}
