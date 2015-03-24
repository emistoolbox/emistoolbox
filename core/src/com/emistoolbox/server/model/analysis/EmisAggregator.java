package com.emistoolbox.server.model.analysis;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;

public abstract interface EmisAggregator
{
    public abstract EmisAggregatorDef getDefinition();

    public abstract void setDefinition(EmisAggregatorDef paramEmisAggregatorDef);

    public abstract EmisContext getContext();

    public abstract void setContext(EmisContext paramEmisContext);

    public abstract void reset();

    public abstract double getValue();
}
