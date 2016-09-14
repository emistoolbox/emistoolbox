package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import java.io.Serializable;

public class DbContextImpl implements DbContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private DbDataSourceConfig config;
    private String query;
    private String loopVariable;
    private EmisMetaDateEnum loopEnum;
    private String[] loopValues;

    public DbDataSourceConfig getDataSource()
    { return this.config; }

    public String getQuery()
    { return this.query; }

    public void setDataSource(DbDataSourceConfig datasource)
    { this.config = datasource; }

    public void setQuery(String query)
    { this.query = query; }

    public String[] getLoopValues()
    {
    	if (loopEnum != null)
    		return loopEnum.getValues(); 
    	else
    		return this.loopValues;
    }

    public String getLoopVariable()
    { return this.loopVariable; }

    public EmisMetaDateEnum getLoopEnum()
    { return this.loopEnum; }

    public void setLoopEnum(EmisMetaDateEnum dateEnum)
    { this.loopEnum = dateEnum; }

    public void setLoopValues(String[] values)
    { this.loopValues = values; }

    public void setLoopVariable(String variable)
    { this.loopVariable = variable; }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.mapping.impl.DbContextImpl
 * JD-Core Version: 0.6.0
 */