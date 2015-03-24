package com.emistoolbox.server.results;

import com.emistoolbox.common.model.analysis.EmisContext;

public abstract interface ResultDimension
{
    public abstract EmisContext getContext(int paramInt);

    public abstract void updateContext(int paramInt, EmisContext paramEmisContext);

    public abstract int getItemCount();

    public abstract String getItemName(int paramInt);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.results.ResultDimension JD-Core
 * Version: 0.6.0
 */