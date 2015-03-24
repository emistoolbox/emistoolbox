package com.emistoolbox.common.model.mapping;

import java.util.Map;

public abstract interface DbRowArrayAccess extends DbRowAccess
{
    public abstract DbRowAccess[] getIndexAccess();

    public abstract void setIndexAccess(DbRowAccess[] paramArrayOfDbRowAccess);

    public abstract DbRowAccess getValueAccess();

    public abstract void setValueAccess(DbRowAccess paramDbRowAccess);

    public abstract int getIndex(int paramInt, Map<String, String> paramMap)
    	throws EmisAccessException; 

    public abstract void setDimensions(int[] paramArrayOfInt);
}

