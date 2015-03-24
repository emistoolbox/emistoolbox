package com.emistoolbox.common.model.mapping;

public interface DataSourceReplace 
{
	public String getColumn(); 
	public String getNeedle(); 
	public String getReplacement();
	
	public void setReplacement(String replacement); 
}
