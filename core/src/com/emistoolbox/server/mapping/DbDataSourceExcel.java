package com.emistoolbox.server.mapping;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.model.mapping.DbDataFileSource;

import es.jbauer.lib.tables.TableReader;
import es.jbauer.lib.tables.impl.excel.ExcelTableFactory;
import es.jbauer.lib.tables.impl.excel.ExcelTableReader;

public class DbDataSourceExcel extends DbDataSourceFileSource
{
	private static ExcelTableFactory factory = new ExcelTableFactory(); 

	public DbDataSourceExcel(DbDataFileSource fileSource, String dataset)
	{ super(fileSource, dataset); } 
			
	@Override
	public Map<String, List<String>> getDataInfo() 
		throws IOException 
	{
		Map<String, List<String>> result = new HashMap<String, List<String>>(); 
		for (String sheet : ExcelTableReader.getSheetNames(getIOInput()))
		{
			TableReader reader = null; 
			try { 
				reader = factory.create(getIOInput(), sheet);
				result.put(sheet, Arrays.asList(reader.getHeaders())); 
			}
			catch (Throwable err)
			{}
			finally 
			{ 
				if (reader != null)
					reader.close(); 
			} 
		}

		return result; 
	}
	
	@Override
	public DbResultSet query(String paramString) 
		throws IOException 
	{ return new DbResultSetTableReader(factory.create(getIOInput(), paramString)); }
}
