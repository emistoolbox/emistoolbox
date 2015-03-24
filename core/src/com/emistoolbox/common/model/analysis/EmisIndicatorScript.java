package com.emistoolbox.common.model.analysis;

public abstract interface EmisIndicatorScript extends EmisIndicator
{
    public abstract String getFormula();

    public abstract void setFormula(String paramString);
}
