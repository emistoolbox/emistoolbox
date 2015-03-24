package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.mapping.DbDataFileSource;

public class DbDataFileSourceImpl implements DbDataFileSource, Serializable
{
	private String url; 
	private String contextValue; 
	private String cacheFilename;
		
	@Override
	public String getUrl() 
	{ return url; } 

	@Override
	public void setUrl(String url) 
	{ this.url = url; } 

	@Override
	public String getContextValue() 
	{ return contextValue; } 

	@Override
	public void setContextValue(String value) 
	{ this.contextValue = value; }

	@Override
	public String getCacheFilename() 
	{ return cacheFilename; }

	@Override
	public void setCacheFilename(String filename) 
	{ this.cacheFilename = filename; }
	
	public static String getMagpiHost(String url)
	{ return "magpi.com"; }
	
	public static String getMagpiUsername(String url)
	{
		url = stripMagpiProtocol(url); 

		int passEnd = url.indexOf("@"); 
		if (passEnd == -1)
			return null; 

		int userEnd = url.indexOf(":");
		if (userEnd == -1 || userEnd > passEnd)
			return url.substring(0, passEnd);
		
		return url.substring(0, userEnd); 
	}
	
	public static String getMagpiPassword(String url) 
	{
		url = stripMagpiProtocol(url); 

		int passEnd = url.indexOf("@"); 
		if (passEnd == -1)
			return null; 
		
		int passStart = url.indexOf(":"); 
		if (passStart == -1 || passStart > passEnd)
			return null;
		
		return url.substring(passStart + 1, passEnd); 
	}
	
	public static String getMagpiDocumentId(String url)
	{
		url = stripMagpiProtocol(url);

		int startPos = url.lastIndexOf("/"); 
		if (startPos == -1)
			return null;  
		else 
			startPos++; 

		int endPos = url.lastIndexOf("."); 
		if (endPos == -1 || startPos > endPos)
			endPos = url.length(); 
		
		return url.substring(startPos, endPos);
	}

	public static String getMagpiFormat(String url)
	{
		int slashPos = url.lastIndexOf("/"); 
		int startPos = url.lastIndexOf("."); 
		if (startPos == -1 || startPos < slashPos)
			return  null; 
		
		return url.substring(startPos + 1); 
	}
	
	private static String stripMagpiProtocol(String url)
	{
		if (!url.startsWith(DbDataFileSource.PREFIX_MAGPI))
			throw new IllegalArgumentException("Invalid format. URL should start with " + DbDataFileSource.PREFIX_MAGPI); 
		
		return url.substring(DbDataFileSource.PREFIX_MAGPI.length()); 
	}
}
