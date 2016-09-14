package com.emistoolbox.server.mapping;

import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigAccess;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigHsqldb;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc.JdbcDriver;
import com.emistoolbox.server.EmisConfig;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.ServerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class DbDataSourceJdbc extends DbDataSourceBase implements DbDataSource
{
    public final static String HSQL_INIT_QUERY = "HSQL_INIT";  
    private DbDataSourceConfigJdbc config;
    private String excelFile = null;

//    private final String CONNECT_ACCESS = "Driver={Driver do Microsoft Access (*.mdb)};DBQ=[FILE];DriverID=22;READONLY=false}";
//    private final String CONNECT_EXCEL = "Driver={Driver do Microsoft Excel(*.xls)};DBQ=[FILE];DriverID=22;READONLY=false}";

    public DbDataSourceJdbc(DbDataSourceConfigJdbc config) 
    { this.config = config; }

    public DbDataSourceJdbc(DbDataSourceConfigAccess fileConfig, DbDataFileSource fileSource, String dataset) 
    {
        this.config = new DbDataSourceConfigJdbc();
        this.config.setDriverType(DbDataSourceConfigJdbc.JdbcDriver.ODBC);

        String path = EmisToolboxIO.getPath(fileSource, dataset);
        this.config.setDbName(getConnectionString("jdbc.msaccess", "Driver={Driver do Microsoft Access (*.mdb)};DBQ=[FILE];DriverID=22;READONLY=false}", path));
        this.config.setContextName(fileConfig.getContextName());
        this.config.setContextValue(fileSource.getContextValue()); 
        this.config.setQueries(fileConfig.getQueries());
    }
    
    public DbDataSourceJdbc(DbDataSourceConfigHsqldb hsqlConfig, String dataset)
    {
    	setDataset(dataset);
    	
    	config = new DbDataSourceConfigJdbc();
    	config.setDriverType(DbDataSourceConfigJdbc.JdbcDriver.HSQL);
    	config.setQueries(hsqlConfig.getQueries());
    	config.setDbName("db");
    }

    private String getConnectionString(String key, String defaultValue, String path)
    {
        String value = EmisConfig.get(key, defaultValue);
        return value.replace("[FILE]", path);
    }

    public Map<String, List<String>> getDataInfo() throws IOException
    {
        String lastQueryId = null; 
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        
        Connection conn = null; 
        try
        {
            conn = getConnection(); 
            
            DatabaseMetaData meta = conn.getMetaData();
            List<String> tables = this.excelFile != null ? getTablesExcel() : getTablesJdbc(meta);

            for (String table : tables)
                result.put(table, getFields(meta, table));

            if (this.config.hasQueries())
            {
                for (String queryId : this.config.getQueries().keySet())
                {
                    if (queryId.equals(DbDataSourceConfig.GLOBAL_QUERY) || queryId.equals(HSQL_INIT_QUERY))
                        continue; 
                    
                    try {
	                    lastQueryId = queryId; 
	                    String key = "[" + queryId + "]";
	                    result.put(key, getFieldNamesInternal(key, conn));
                    }
                    catch (IOException ex)
                    {
                    	System.out.println(queryId); 
                    	ex.printStackTrace(System.out); 
                    	throw ex; 
                    }
                }
            }
        }
        catch (SQLException ex)
        {
            if (lastQueryId != null)
                throw DbDataSourceBase.getIOException("Query [" + lastQueryId + "] failed.", ex); 
            else
                throw DbDataSourceBase.getIOException("Failed to retrieve database meta information for " + getConnectionString(), ex);
        }
        finally 
        { DbUtils.closeQuietly(conn); } 

        return result;
    }

    private List<String> getTablesJdbc(DatabaseMetaData meta) 
        throws IOException, SQLException
    {
        List<String> result = new ArrayList<String>();

        ResultSet rs = null;
        try
        {
            rs = meta.getTables(null, null, null, new String[] { "TABLE", "VIEW" });
            while (rs.next())
                result.add(rs.getString("TABLE_NAME"));
        }
        finally
        { DbUtils.closeQuietly(rs); }
        
        return result;
    }

    private List<String> getTablesExcel() throws IOException
    {
        List<String> result = new ArrayList<String>();

        InputStream is = null;
        try
        {
            is = new FileInputStream(this.excelFile);
            HSSFWorkbook workbook = new HSSFWorkbook(is);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++)
            {
                result.add(workbook.getSheetName(i) + "$");
            }
            
            return result;
        }
        finally
        { IOUtils.closeQuietly(is); }
    }

    private List<String> getFields(DatabaseMetaData meta, String table) throws SQLException, IOException
    {
        List<String> result = new ArrayList<String>();

        ResultSet rs = null;
        try
        {
            rs = meta.getColumns(null, null, table, null);
            while (rs.next())
                result.add(rs.getString("COLUMN_NAME"));
        }
        catch (SQLException ex)
        { throw DbDataSourceBase.getIOException("Failed to retrieve field meta information for " + getConnectionString(), ex); }
        finally
        { DbUtils.closeQuietly(rs); }
        
        return result;
    }

    public List<String> getTableNames() throws IOException
    {
        return super.getTableNames();
    }

    private List<String> getFieldNamesInternal(String query, Connection reuseConnection) throws IOException
    {
        List<String> result = new ArrayList<String>();

        ResultSetResult rsr = null;
        try
        {
            String[] queries= getQueries(getQueryForResultSet(query, true)); 
            
            rsr = getResultSet(queries[0], reuseConnection);
            ResultSetMetaData meta = rsr.getResultSet().getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++)
                result.add(meta.getColumnName(i));
        }
        catch (SQLException ex)
        { throw DbDataSourceBase.getIOException("Failed to retrieve field names names for " + getConnectionString(), ex); }
        finally
        { rsr.close(); }
        
        return result;
    }

    public List<String> getFieldNames(String query) throws IOException
    {
        List<String> result = super.getFieldNames(query);
        if (result != null)
        {
            return result;
        }
        return getFieldNamesInternal(query,  null);
    }

    public DbResultSet query(String query) throws IOException
    {
        String[] queries = getQueries(getQueryForResultSet(query, false)); 
        return new DbMultiResultSetJdbc(this, queries, config.getContextName(), config.getContextValue());
    }

    private String getQueryForResultSet(String query, boolean headersOnly)
    {
        if ((this.config.hasQueries()) && (query.startsWith("[")) && (query.endsWith("]")))
        {
            String global = config.getQuery(DbDataSourceConfig.GLOBAL_QUERY); 
            if (StringUtils.isEmpty(global))
                global = ""; 
            else if (!global.trim().endsWith(";"))
                global += ";"; 

            global = replaceQueryInclude(global); 
            
            return global + addLimit(replaceQueryInclude(this.config.getQuery(query.substring(1, query.length() - 1))), headersOnly);
        }
        
        if (this.excelFile != null)
            return "SELECT * FROM [" + query + "]";

        return addLimit("SELECT * FROM " + query, headersOnly);
    }

    private String replaceQueryInclude(String query)
    {
        StringBuffer result = new StringBuffer(query); 

        int start = result.indexOf("["); 
        int end = result.indexOf("]", start); 
        while (start != -1 && end != -1)
        {
            String name = query.substring(start, end + 1); 
            String replacement = getQuery(name); 
            if ("".equals(replacement))
                replacement = null; 

            if (replacement != null)
            {
                result.replace(start,  end + 1, replacement);
                end += replacement.length() - (end - start); 
            }
            
            start = result.indexOf("[", end);
            end = result.indexOf("]", start); 
        }
        
        return result.toString();
    }

    private String addLimit(String query, boolean headersOnly)
    {
        if (!headersOnly)
            return query; 
        
        query = query.trim(); 
        if (query.endsWith(";"))
            query = query.substring(0, query.length() - 1); 
        
        if (config.getDriverType() == JdbcDriver.MYSQL || config.getDriverType() == JdbcDriver.POSTGRESQL)
            query += " LIMIT 0"; 
        else if (config.getDriverType() == JdbcDriver.MSSQL)
            query.replaceAll("SELECT ", "SELECT TOP 0 "); 
        else if (config.getDriverType() == JdbcDriver.HSQL)
        	query.replaceAll("SELECT ", "SELECT TOP 1 "); 

        return query; 
    }

    private String getQuery(String queryName)
    {
        if (queryName.startsWith("["))
            queryName = queryName.substring(1, queryName.length() -1); 
        
        return config.getQuery(queryName); 
    }
    
    private int getQueryCount(String sql)
    {
        int result = 0; 
        for (String part : sql.split(";"))
        {
            if (!StringUtils.isEmpty(part.trim()))
                result++; 
        }
        
        return result; 
    }
    
    public ResultSetResult getResultSet(String query)
        throws IOException
    { return getResultSet(query, null); }
    
    public ResultSetResult getResultSet(String query, Connection reuseConn)
        throws IOException
    {   
        Connection conn = reuseConn; 
        Statement stmt = null; 

        try
        {
            if (reuseConn == null)
                conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(query);
            ResultSet rs = stmt.getResultSet(); 
            
            int count = getQueryCount(query); 
            for (int i = 1; i < count; i++) 
            {
                stmt.getMoreResults(); 
                rs = stmt.getResultSet(); 
            }
                
            return new ResultSetResult(conn, stmt, rs, reuseConn != null);  
        }
        catch (Throwable ex)
        {
            DbUtils.closeQuietly(stmt); 
            if (reuseConn == null)
            { DbUtils.closeQuietly(conn); }
            
            throw DbDataSourceBase.getIOException("Failed to get ResultSet for " + getConnectionString() + " - " + query, ex); 
        }
    }
    
    private Connection getConnection() 
        throws SQLException, IOException
    {
        try { Class.forName(getDriver()); }
        catch (ClassNotFoundException ex)
        { throw getIOException("Failed to load driver: " + getDriver(), ex); }

        Connection result = null; 
        if (StringUtils.isEmpty(this.config.getUserId()))
            result = DriverManager.getConnection(getConnectionString());
        else
            result = DriverManager.getConnection(getConnectionString(), this.config.getUserId(), this.config.getPassword());
        
        if (config.getDriverType() == DbDataSourceConfigJdbc.JdbcDriver.HSQL)
        {
        	String hsqlInitQuery = getQuery(HSQL_INIT_QUERY); 
        	if (!StringUtils.isEmpty(hsqlInitQuery))
        	{
        		Statement stmt = null; 	
        		try { 
    				stmt = result.createStatement(); 
        			for (String sql : splitQueries(hsqlInitQuery))
        			{
        				if (!StringUtils.isEmpty(sql))
        					stmt.execute(sql); 
        			}
        		}
        		finally {
        			if (stmt != null)
        				stmt.close(); 
        		}
        	}
        }
        
        return result; 
    }

    private List<String> splitQueries(String query)
    {
    	List<String> result = new ArrayList<String>(); 
    	
    	int start = 0;
    	while (start < query.length())
    	{
    		int nextStart = findNextQueryStart(query, start);    		
    		if (nextStart == -1)
    			nextStart = query.length(); 
    		
    		String subQuery = query.substring(start, nextStart).trim();  
    		if (!StringUtils.isEmpty(subQuery))
    			result.add(subQuery);
    		
    		start = nextStart + 1; 
    	}
    	
    	return result; 
    }
    
    private int findNextQueryStart(String query, int start)
    {
    	boolean isQuoted = false; 
    	boolean isEscaped = false; 
    	
    	int index = start; 
    	while (index < query.length())
    	{
    		if (isEscaped)
    			isEscaped = false; 
    		if (query.charAt(index) == '"')
    			isQuoted = !isQuoted; 
    		else if (query.charAt(index) == '\\')
    			isEscaped = true;
    		else if (isQuoted)
    			; 
    		else if (query.charAt(index) == ';')
    			return index; 
    		
    		index++; 
    	}

    	return -1; 
    }

    private String getDriver()
    {
        DbDataSourceConfigJdbc.JdbcDriver driver = this.config.getDriverType();
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.MSSQL)
            return "net.sourceforge.jtds.jdbc.Driver";

        if (driver == DbDataSourceConfigJdbc.JdbcDriver.MYSQL)
            return "com.mysql.jdbc.Driver";

        if (driver == DbDataSourceConfigJdbc.JdbcDriver.POSTGRESQL)
        	return "org.postgresql.Driver"; 
        
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.ODBC)
            return "sun.jdbc.odbc.JdbcOdbcDriver";
        
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.HSQL)
        	return "org.hsqldb.jdbc.JDBCDriver"; 

        return null;
    }

    private String getConnectionString()
    	throws IOException
    {
        DbDataSourceConfigJdbc.JdbcDriver driver = this.config.getDriverType();
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.MSSQL)
            return "jdbc:jtds:sqlserver://" + this.config.getHostAndPort() + "/" + this.config.getDbName();
        
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.MYSQL)
        {
            String multipleQueries = StringUtils.isEmpty(config.getQuery(DbDataSourceConfig.GLOBAL_QUERY)) ? "" : "?allowMultiQueries=true"; 
            return "jdbc:mysql://" + this.config.getHostAndPort() + "/" + this.config.getDbName() + multipleQueries;
        }
        
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.POSTGRESQL)
        	return "jdbc:postgresql://" + this.config.getHostAndPort() + "/" + this.config.getDbName(); 

        if (driver == DbDataSourceConfigJdbc.JdbcDriver.HSQL)
        {
        	File hsqlPath = ServerUtil.getFile(getDataset(), "hsqldb", true);
        	if (!hsqlPath.exists())
        		hsqlPath.mkdirs();

         	return "jdbc:hsqldb:file:" + hsqlPath.getAbsolutePath() + "/" + this.config.getDbName() + ";user=SA;password=;shutdown=true"; 
        }
        
        if (driver == DbDataSourceConfigJdbc.JdbcDriver.ODBC)
            return "jdbc:odbc:" + this.config.getDbName();

        return null;
    }

    private Pattern p = Pattern.compile("FOREACH \\@(.+) IN (.+)");
    
    private String[] getQueries(String query)
    {
        int start = query.indexOf("FOREACH "); 
        if (start == -1)
            return new String[] { query }; 
        
        int end = query.indexOf(";", start); 
        if (end == -1)
            end = query.length();
        else
            end--; 
        
        String strForeach = query.substring(start, end + 1);
        Matcher m = p.matcher(strForeach); 
        if (!m.matches())
            throw new IllegalArgumentException("Invalid FOREACH syntax: " + strForeach); 
        
        String variable = m.group(1); 
        String[] values = m.group(2).split(","); 

        // Test if variable is used in query. 
        //
        StringBuffer simpleQuery = new StringBuffer(query); 
        simpleQuery.replace(start, Math.min(end + 2, simpleQuery.length()), ""); 
        if (simpleQuery.indexOf("@" + variable) == -1)
            return new String[] { simpleQuery.toString() }; 
        
        for (int i = 0; i < values.length; i++)
        {
            StringBuffer tmp = new StringBuffer(query); 
            tmp.replace(start,  Math.min(end + 2, tmp.length()), "SELECT @" + variable + " := " + values[i].trim() + ";"); 
            values[i] = tmp.toString();
        }
        
        return values; 
    }
}
