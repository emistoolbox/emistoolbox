package com.emistoolbox.common.model.analysis;

public abstract interface EmisIndicatorRatio extends EmisIndicator
{
    public static final String DENOMINATOR = "denominator";
    public static final String NUMERATOR = "numerator";

    public abstract EmisAggregatorDef getDenominator();

    public abstract void setDenominator(EmisAggregatorDef paramEmisAggregatorDef);

    public abstract EmisAggregatorDef getNumerator();

    public abstract void setNumerator(EmisAggregatorDef paramEmisAggregatorDef);

    public abstract double getFactor();

    public abstract void setFactor(double paramDouble);
}
