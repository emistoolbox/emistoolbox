package com.emistoolbox.common.results;

import java.util.List;

public interface ValidationMetaResult extends ListEntityMetaResult
{
	public List<String> getValidationIds(); 
	public boolean hasValidationId(String validationId); 

	public void setValidationIds(List<String> validationIds); 
	public void addValidationId(String validationId); 
	
	public byte getDateIndex(); 
}
