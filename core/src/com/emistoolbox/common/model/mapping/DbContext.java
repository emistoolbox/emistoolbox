package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import java.io.Serializable;

public abstract interface DbContext extends Serializable
{
    public abstract DbDataSourceConfig getDataSource();

    public abstract void setDataSource(DbDataSourceConfig paramDbDataSourceConfig);

    public abstract String getQuery();

    public abstract void setQuery(String paramString);

    public abstract String getLoopVariable();

    public abstract void setLoopVariable(String paramString);

    public abstract String[] getLoopValues();

    public abstract void setLoopValues(String[] paramArrayOfString);

    public abstract EmisMetaDateEnum getLoopEnum();

    public abstract void setLoopEnum(EmisMetaDateEnum paramEmisMetaDateEnum);
}
