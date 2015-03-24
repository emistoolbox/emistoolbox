package com.emistoolbox.server.mapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigHsqldb;
import com.emistoolbox.server.EmisConfig;
import com.emistoolbox.server.ServerUtil;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileOutput;

public class DbDataSourceHsqldb implements DbDataSource 
{
	private DbDataSourceJdbc jdbc; 
	
	public DbDataSourceHsqldb(DbDataSourceConfigHsqldb hsqlConfig, String dataset)
		throws IOException
	{
		// Copy files. 
		//
		for (DbDataFileSource fileSource : hsqlConfig.getFileSources())
		{
			File hsqlDir = new File(getHsqldbFolder(dataset)); 
			if (!hsqlDir.exists())
				hsqlDir.mkdirs(); 
			
			IOInput in = DbDataSourceFileSource.getIOInput(fileSource, dataset);
			IOOutput out = new IOFileOutput(getTargetFile(dataset, fileSource));
			
			InputStream is = null; 
			OutputStream os = null; 
			try {
				is = in.getInputStream(); 
				os = out.getOutputStream(); 
				IOUtils.copy(is, os); 
				os.flush(); 
			}
			finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}
		}
		
		// Create JDBC driver for HSQL and run initilization query (this sets up the tables). 
		//
		jdbc = new DbDataSourceJdbc(hsqlConfig, dataset);
		jdbc.query("[" + DbDataSourceJdbc.HSQL_INIT_QUERY + "]");
	}
	
	private File getTargetFile(String dataset, DbDataFileSource fileSource)
	{
		String filename = null; 
		if (fileSource.getUrl().startsWith(DbDataFileSource.PREFIX_HTTP) || fileSource.getUrl().startsWith(DbDataFileSource.PREFIX_HTTPS) || fileSource.getUrl().startsWith(DbDataFileSource.PREFIX_MAGPI))
		{
			if (StringUtils.isEmpty(fileSource.getCacheFilename()))
				throw new IllegalArgumentException("No cache filename specified."); 
			filename = fileSource.getCacheFilename(); 
		}
		else
		{
			int pos = Math.max(fileSource.getUrl().lastIndexOf("\\"), fileSource.getUrl().lastIndexOf("/"));  
			filename = fileSource.getUrl().substring(pos + 1); 
		}

		return new File(getHsqldbFolder(dataset), filename); 
	}
	
	@Override
	public Map<String, List<String>> getDataInfo() 
		throws IOException 
	{ return jdbc.getDataInfo(); }

	@Override
	public List<String> getTableNames() 
		throws IOException 
	{ return jdbc.getTableNames(); }

	@Override
	public List<String> getFieldNames(String table) 
		throws IOException 
	{ return jdbc.getFieldNames(table); }

	@Override
	public DbResultSet query(String query) 
		throws IOException 
	{ return jdbc.query(query); } 

	@Override
	public void setDataset(String dataset) 
	{ jdbc.setDataset(dataset); }

	@Override
	public String getDataset() 
	{ return jdbc.getDataset(); } 
	
	public static String getHsqldbFolder(String dataset)
	{ return EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH_WRITABLE, ServerUtil.ROOT_PATH) + dataset + "/hsqldb/"; }
}
