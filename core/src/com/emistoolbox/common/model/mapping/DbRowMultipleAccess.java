package com.emistoolbox.common.model.mapping;

public abstract interface DbRowMultipleAccess extends DbRowAccess
{
    public abstract String[] getIndexes();

    public abstract void setIndexes(String[] paramArrayOfString);

    public abstract DbRowAccess[] getAccesses();

    public abstract void setAccesses(DbRowAccess[] paramArrayOfDbRowAccess);

    public abstract DbRowAccess getIndexAccess();
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.mapping.DbRowMultipleAccess
 * JD-Core Version: 0.6.0
 */