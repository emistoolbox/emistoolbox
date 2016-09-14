package com.emistoolbox.common.results;

import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

public abstract interface TableMetaResult extends MetaResult
{
    public static final int SORT_NONE = 0;
    public static final int SORT_ASCENDING = 1;
    public static final int SORT_DESCENDING = -1;
    public static final int SORT_NAME = 2;

    public abstract int getDimensionCount();

    public abstract void setDimensionCount(int paramInt);

    public abstract MetaResultDimension getDimension(int paramInt);

    public abstract void setDimension(int paramInt, MetaResultDimension paramMetaResultDimension);

    public abstract int getSortOrder();

    public abstract void setSortOrder(int paramInt);

    public Set<EmisMetaDateEnum> getUsedDateTypes(boolean withoutAxis); 
    
    public TableMetaResult createCopy(); 
}
