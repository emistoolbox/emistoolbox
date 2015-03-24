package com.emistoolbox.server.mapping;

import java.io.IOException;

import es.jbauer.lib.tables.TableReader;
import es.jbauer.lib.tables.TableRow;

public class DbResultSetTableReader extends DbResultSetBase implements DbResultSet 
{
	private TableReader reader; 
	private TableRow currentRow; 
	
	public DbResultSetTableReader(TableReader reader)
	{
		super(null);
		this.reader = reader; 
	} 

	@Override
	public boolean next() 
		throws IOException 
	{
		if (reader == null)
			throw new IOException("Already closed."); 

		currentRow = reader.getNextRow(); 
		return currentRow != null;
	}

	@Override
	public String get(String paramString) 
		throws IOException 
	{ return currentRow.get(paramString); }

	@Override
	public void close() 
	{ 
		if (reader != null)
			reader.close(); 
		reader = null; 
	}
}
