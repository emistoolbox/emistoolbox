package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.analysis.EmisIndicatorTimeRatio;

public class IndicatorTimeRatio extends IndicatorRatio implements EmisIndicatorTimeRatio
{
    private int timeStep;

    public void setTimeOffset(int step)
    {
        this.timeStep = step;
    }

    public int getTimeOffset()
    {
        return this.timeStep;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.analysis.impl.IndicatorTimeRatio
 * JD-Core Version: 0.6.0
 */