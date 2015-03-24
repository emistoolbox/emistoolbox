package com.emistoolbox.server.mapping;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.impl.DbDataFileSourceImpl;
import com.emistoolbox.server.EmisConfig;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.ServerUtil;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOCachedInput;
import es.jbauer.lib.io.impl.IOFileInput;
import es.jbauer.lib.io.impl.IOHttpInput;
import es.jbauer.lib.io.impl.IOMagpiInput;

/** Class to read data from a DbDataConfigFileSource object. */ 
public abstract class DbDataSourceFileSource extends DbDataSourceBase 
{
	private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
	private static final long ONE_MINUTE_MS = 60 * 1000;
	
	private DbDataFileSource fileSource; 
	private String dataset; 

	public DbDataSourceFileSource(DbDataFileSource fileSource, String dataset)
	{
		this.fileSource = fileSource; 
		this.dataset = dataset; 
	}
	
	public DbDataFileSource getFileSource()
	{ return fileSource; } 
	
	public void setFileSource(DbDataFileSource fileSource)
	{ this.fileSource = fileSource; }
	
	private static String getCacheFileName(DbDataFileSource file, String dataset)
	{
		String basePath = EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH_WRITABLE, ServerUtil.ROOT_PATH) + dataset + "/cached/"; 
		return basePath + file.getCacheFilename(); 
	}

	protected IOInput getIOInput()
		throws IOException
	{ return getIOInput(fileSource, dataset); } 
	
	public static IOInput getIOInput(DbDataFileSource fileSource, String dataset)
		throws IOException
	{
		String url = fileSource.getUrl();
		if (url.startsWith(DbDataFileSource.PREFIX_HTTP) || url.startsWith(DbDataFileSource.PREFIX_HTTPS))
		{
			IOInput result = new IOHttpInput(url);
			if (!StringUtils.isEmpty(fileSource.getCacheFilename()))
			{
				File cacheFile = new File(getCacheFileName(fileSource, dataset)); 
				if (!cacheFile.getParentFile().exists())
					cacheFile.getParentFile().mkdirs(); 
				
				result = new IOCachedInput(result, cacheFile, ONE_DAY_MS);
			}
			
			return result; 
		}
		
		if (url.startsWith(DbDataFileSource.PREFIX_MAGPI))
		{
			IOInput result = new IOMagpiInput(DbDataFileSourceImpl.getMagpiDocumentId(url), DbDataFileSourceImpl.getMagpiFormat(url), DbDataFileSourceImpl.getMagpiUsername(url), DbDataFileSourceImpl.getMagpiPassword(url));
			if (!StringUtils.isEmpty(fileSource.getCacheFilename()))
			{
				File cacheFile = new File(getCacheFileName(fileSource, dataset));
				if (!cacheFile.getParentFile().exists())
					cacheFile.getParentFile().mkdirs();
				
				result = new IOCachedInput(result, cacheFile, ONE_MINUTE_MS); 
			}

			return result; 
		}
		
		String path = EmisToolboxIO.getPath(fileSource, dataset); 
		if (path != null)
		{
			System.out.println("Loading from " + path); 
			return new IOFileInput(new File(path)); 
		}
		
		throw new IOException("Unsupported file source protocol.");  
	}
}
