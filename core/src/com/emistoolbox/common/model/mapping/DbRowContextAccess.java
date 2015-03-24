package com.emistoolbox.common.model.mapping;

public abstract interface DbRowContextAccess extends DbRowAccess
{
    public abstract String getContextParameter();

    public abstract void setContextParameter(String paramString);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.mapping.DbRowContextAccess
 * JD-Core Version: 0.6.0
 */