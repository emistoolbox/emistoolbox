package com.emistoolbox.common.model.mapping;

public abstract interface GisEntityDbMap extends EntityBaseDbMap
{
    public abstract DbRowAccess getPrimaryAccess();

    public abstract void setPrimaryAccess(DbRowAccess paramDbRowAccess);

    public abstract DbRowAccess getSecondaryAccess();

    public abstract void setSecondaryAccess(DbRowAccess paramDbRowAccess);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.mapping.GisEntityDbMap JD-Core
 * Version: 0.6.0
 */