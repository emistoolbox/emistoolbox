package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.model.mapping.DbDataFileSource;

import es.jbauer.lib.tables.TableReader;
import es.jbauer.lib.tables.impl.csv.CsvTableConfig;
import es.jbauer.lib.tables.impl.csv.CsvTableFactory;

public class DbDataSourceCsv extends DbDataSourceFileSource 
{
	private Map<String, String> csvConfig = new HashMap<String, String>(); 
	private static CsvTableFactory factory = new CsvTableFactory(); 
		
	public DbDataSourceCsv(DbDataFileSource fileSource, String dataset)
	{
		super(fileSource, dataset); 
		csvConfig.put(CsvTableConfig.CSV_DELIMITER, ","); 
	} 

	@Override
	public Map<String, List<String>> getDataInfo() 
		throws IOException 
	{
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		TableReader reader = null; 
		try { 
			reader = factory.create(getIOInput(), csvConfig); 
			result.put("csv", Arrays.asList(reader.getHeaders())); 
			return result;
		}
		finally { 
			if (reader != null)
				reader.close();
		}
	}

	@Override
	public DbResultSet query(String paramString) 
		throws IOException
	{ return new DbResultSetTableReader(factory.create(getIOInput(), csvConfig)); }
}
