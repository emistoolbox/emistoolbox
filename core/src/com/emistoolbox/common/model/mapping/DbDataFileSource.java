package com.emistoolbox.common.model.mapping;

import java.io.Serializable;

public interface DbDataFileSource extends Serializable
{
	public static final String PREFIX_HTTP = "http://"; 
	public static final String PREFIX_HTTPS = "https://"; 
	public static final String PREFIX_FILESYSTEM = "file://"; 
	public static final String PREFIX_DATASET = "emistoolbox://"; 
	public static final String PREFIX_MAGPI = "magpi://"; 
	
	public String getUrl(); 
	public void setUrl(String url); 
	
	public String getContextValue(); 
	public void setContextValue(String value);
	
	public String getCacheFilename(); 
	public void setCacheFilename(String filename); 
}
