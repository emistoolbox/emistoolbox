package com.emistoolbox.server.model.analysis.impl;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.server.model.analysis.EmisAggregator;

public class Aggregator implements EmisAggregator
{
    private EmisAggregatorDef aggregatorDef;

    public EmisContext getContext()
    {
        return null;
    }

    public EmisAggregatorDef getDefinition()
    {
        return this.aggregatorDef;
    }

    public double getValue()
    {
        return 0.0D;
    }

    public void reset()
    {
    }

    public void setContext(EmisContext context)
    {
    }

    public void setDefinition(EmisAggregatorDef def)
    {
        this.aggregatorDef = def;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.analysis.impl.Aggregator JD-Core
 * Version: 0.6.0
 */