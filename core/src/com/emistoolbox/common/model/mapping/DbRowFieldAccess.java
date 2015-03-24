package com.emistoolbox.common.model.mapping;

public abstract interface DbRowFieldAccess extends DbRowAccess
{
    public abstract String getFieldName();

    public abstract void setFieldName(String paramString);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.mapping.DbRowFieldAccess JD-Core
 * Version: 0.6.0
 */