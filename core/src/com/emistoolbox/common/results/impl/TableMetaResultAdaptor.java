package com.emistoolbox.common.results.impl;

import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.TableMetaResult;

public abstract class TableMetaResultAdaptor extends MetaResultAdaptor implements TableMetaResult
{
    private TableMetaResult tableMetaResult;

    public TableMetaResultAdaptor(TableMetaResult metaResult) {
        super(metaResult);
        this.tableMetaResult = metaResult;
    }

    public MetaResultDimension getDimension(int index)
    { return this.tableMetaResult.getDimension(index); }

    public int getDimensionCount()
    { return this.tableMetaResult.getDimensionCount(); }

    public void setDimension(int index, MetaResultDimension dimension)
    { throw new IllegalArgumentException("Adaptor doesn't support set*"); }

    public void setDimensionCount(int count)
    { throw new IllegalArgumentException("Adaptor doesn't support set*"); }

    public int getSortOrder()
    { return this.tableMetaResult.getSortOrder(); }

    public void setSortOrder(int order)
    { throw new IllegalArgumentException("Adaptor doesn't support set*"); }

	@Override
	public Set<EmisMetaDateEnum> getUsedDateTypes(boolean withoutAxis) 
	{ return tableMetaResult.getUsedDateTypes(withoutAxis); }
}
