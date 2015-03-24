package com.emistoolbox.server.results.impl;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.server.results.ResultDimension;
import java.util.List;

public class ResultDimensionMetaResultValues implements ResultDimension
{
    private List<MetaResultValue> values;

    public ResultDimensionMetaResultValues(List<MetaResultValue> values) {
        this.values = values;
    }

    public EmisContext getContext(int index)
    {
        return null;
    }

    public int getItemCount()
    {
        return this.values.size();
    }

    public String getItemName(int index)
    {
        MetaResultValue value = (MetaResultValue) this.values.get(index);
        if (value.getAggregatorKey() == null)
        {
            return value.getIndicator().getName();
        }
        return value.getAggregatorName();
    }

    public void updateContext(int index, EmisContext context)
    {
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.results.impl.ResultDimensionMetaResultValues JD-Core
 * Version: 0.6.0
 */