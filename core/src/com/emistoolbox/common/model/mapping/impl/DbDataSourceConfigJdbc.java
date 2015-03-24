package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import java.io.Serializable;

public class DbDataSourceConfigJdbc extends DbDataSourceConfigBase implements DbDataSourceConfig, Serializable
{
    private static final long serialVersionUID = 1L;
    private String driver;
    private String host;
    private int port;
    private String dbname;
    private String password;
    private String uid;
    private boolean connected;
    private String contextName;
    private String contextValue;

    public DbDataSourceConfigJdbc() {
        super(true, true);
    }

    public JdbcDriver getDriverType()
    { return JdbcDriver.valueOf(this.driver); }

    public void setDriverType(JdbcDriver driver)
    { this.driver = driver.toString(); }

    public String getHost()
    {
        return this.host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return this.port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getHostAndPort()
    {
        if (this.port == -1)
        {
            return this.host;
        }
        return this.host + ":" + this.port;
    }

    public String getDbName()
    {
        return this.dbname;
    }

    public void setDbName(String dbname)
    {
        this.dbname = dbname;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getUserId()
    {
        return this.uid;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setUserId(String uid)
    {
        this.uid = uid;
    }

    public String getName()
    {
        StringBuffer result = new StringBuffer();
        result.append(this.driver);
        result.append(": ");
        if ((this.host != null) && (this.driver != null) && (this.driver.equals(JdbcDriver.ODBC.toString())))
        {
            result.append(this.host);
            result.append("/");
        }

        result.append(this.dbname);

        if (this.connected)
        {
            result.append(" [OK]");
        }
        return result.toString();
    }

    public String getContextName()
    {
        return this.contextName;
    }

    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    public String getContextValue()
    {
        return this.contextValue;
    }

    public void setContextValue(String contextValue)
    {
        this.contextValue = contextValue;
    }

    public boolean hasConnected()
    {
        return this.connected;
    }

    public void setHasConnected(boolean connected)
    { this.connected = connected; }

    public static enum JdbcDriver {
        MYSQL, POSTGRESQL, MSSQL, ODBC, HSQL;
    }
}
