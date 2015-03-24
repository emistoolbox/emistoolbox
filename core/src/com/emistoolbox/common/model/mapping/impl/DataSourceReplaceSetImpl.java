package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.mapping.DataSourceReplace;
import com.emistoolbox.common.model.mapping.DataSourceReplaceSet;

public class DataSourceReplaceSetImpl implements DataSourceReplaceSet, Serializable 
{
	private List<DataSourceReplace> replacements = new ArrayList<DataSourceReplace>(); 
	
	@Override
	public Set<String> getColumns() 
	{
		Set<String> result = new HashSet<String>(); 
		for (DataSourceReplace replace : replacements)
			result.add(replace.getColumn()); 
		
		return result; 
	}

	@Override
	public List<DataSourceReplace> getAll() 
	{ return replacements; }

	@Override
	public List<DataSourceReplace> getForColumn(String col) 
	{
		List<DataSourceReplace> result = new ArrayList<DataSourceReplace>(); 
		for (DataSourceReplace replace : replacements)
		{
			if (replace.getColumn().equals(col))
				result.add(replace); 
		}
		
		return result; 
	}

	@Override
	public void addReplace(String col, String needle, String replacement) 
	{
		for (DataSourceReplace replace : replacements)
		{
			if (replace.getColumn().equals(col) && replace.getNeedle().equals(needle))
			{
				replace.setReplacement(replacement); 
				return; 
			}
		}
		
		replacements.add(new DataSourceReplaceImpl(col, needle, replacement)); 
	}
	
	@Override
	public String replace(String col, String text) 
	{
		if (text == null)
			return null; 
		
		for (DataSourceReplace replace : replacements)
			text = text.replaceAll(replace.getNeedle(), replace.getReplacement()); 

		return text; 
	}
}
