package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.mapping.DataSourceReplace;

public class DataSourceReplaceImpl implements DataSourceReplace, Serializable
{
	private String col; 
	private String needle; 
	private String replacement; 
	
	public DataSourceReplaceImpl()
	{}
	
	public DataSourceReplaceImpl(String col, String needle, String replacement)
	{
		this.col = col; 
		this.needle = needle; 
		this.replacement = replacement; 
	}

	@Override
	public String getColumn() 
	{ return col; } 
	
	@Override
	public String getNeedle()
	{ return needle; }
	
	@Override
	public String getReplacement() 
	{ return replacement; } 

	@Override
	public void setReplacement(String replacement) 
	{ this.replacement = replacement; } 
}
