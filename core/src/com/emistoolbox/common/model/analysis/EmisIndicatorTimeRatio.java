package com.emistoolbox.common.model.analysis;

public abstract interface EmisIndicatorTimeRatio extends EmisIndicatorRatio
{
    public abstract void setTimeOffset(int paramInt);

    public abstract int getTimeOffset();
}
