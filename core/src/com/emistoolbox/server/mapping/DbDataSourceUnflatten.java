package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.model.mapping.impl.UnflattenDbQuery;

public class DbDataSourceUnflatten extends DbDataSourceBase
{
    private List<UnflattenDbQuery> configs;
    private DbDataSource dataSource;

    public DbDataSourceUnflatten(List<UnflattenDbQuery> configs) {
        this.configs = configs;
    }

    public void setDataSource(DbDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public Map<String, List<String>> getDataInfo() throws IOException
    {
        Map<String, List<String>> result = dataSource.getDataInfo();
        for (UnflattenDbQuery config : configs)
            result.put("{" + config.getName() + "}", config.getFieldNames());

        return result;
    }

    public DbResultSet query(String paramString) throws IOException
    {
        if (paramString.startsWith("{") && paramString.endsWith("}"))
        {
            UnflattenDbQuery config = getConfig(paramString.substring(1, paramString.length() - 1));
            if (config == null)
                return null;

            return new DbResultSetUnflattenDb(dataSource.query(config.getTable()), config);
        }
        else
            return dataSource.query(paramString);
    }

    private UnflattenDbQuery getConfig(String name)
    {
        for (UnflattenDbQuery config : configs)
            if (config.getName().equals(name))
                return config;

        return null;
    }

	@Override
	public void setDataset(String dataset) 
	{ dataSource.setDataset(dataset); } 

	@Override
	public String getDataset() 
	{ return dataSource.getDataset(); } 
}
