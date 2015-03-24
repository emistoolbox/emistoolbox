package com.emistoolbox.server.mapping;

import java.sql.*; 

import org.apache.commons.dbutils.DbUtils;

public class ResultSetResult
{
    private boolean reuseConnection; 
    private Connection conn; 
    private Statement stmt; 
    private ResultSet rs; 
    
    public ResultSetResult(Connection conn, Statement stmt, ResultSet rs, boolean reuseConnection)
    {
        this.conn = conn; 
        this.stmt = stmt; 
        this.rs = rs; 
        this.reuseConnection = reuseConnection;
    }
    
    public ResultSet getResultSet()
    { return rs; } 
    
    public void close()
    {
        DbUtils.closeQuietly(rs);
        rs = null; 
        
        DbUtils.closeQuietly(stmt); 
        stmt = null;
        
        if (!reuseConnection)
        {
            DbUtils.closeQuietly(conn); 
            conn = null; 
        }
    }
}
