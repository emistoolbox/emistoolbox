package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import java.io.Serializable;
import java.util.List;

public class DbDataSourceConfigMultiple extends DbDataSourceConfigBase implements DbDataSourceConfig, Serializable
{
    private static final long serialVersionUID = 1L;
    private List<DbDataSourceConfig> configs;

    public DbDataSourceConfigMultiple() {
        super(false, true);
    }

    public List<DbDataSourceConfig> getConfigs()
    {
        return this.configs;
    }

    public void setConfigs(List<DbDataSourceConfig> configs)
    {
        this.configs = configs;
    }

    public String getName()
    {
        return "(multiple)";
    }

    public boolean hasConnected()
    {
        if ((this.configs == null) || (this.configs.size() == 0))
        {
            return false;
        }
        return configs.get(0).hasConnected();
    }

    public void setHasConnected(boolean connected)
    {
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigMultiple JD-Core
 * Version: 0.6.0
 */