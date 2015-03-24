package com.emistoolbox.common.model.mapping;

public abstract interface DbRowConstAccess extends DbRowAccess
{
    public abstract String getConstValue();

    public abstract void setConstValue(String paramString);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.mapping.DbRowConstAccess JD-Core
 * Version: 0.6.0
 */