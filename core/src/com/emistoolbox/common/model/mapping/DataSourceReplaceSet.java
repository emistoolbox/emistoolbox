package com.emistoolbox.common.model.mapping;

import java.util.List;
import java.util.Set;

public interface DataSourceReplaceSet 
{
	public Set<String> getColumns();
	
	public List<DataSourceReplace> getAll(); 
	public List<DataSourceReplace> getForColumn(String col); 
	public void addReplace(String col, String needle, String replacement); 
	
	public String replace(String col, String text);
}
