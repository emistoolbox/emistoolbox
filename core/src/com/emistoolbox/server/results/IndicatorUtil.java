package com.emistoolbox.server.results;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisIndicatorRatio;
import com.emistoolbox.common.model.analysis.EmisIndicatorSimple;
import java.util.Map;

public class IndicatorUtil
{
    public static double calculate(EmisIndicator indicator, Map<String, Double> values)
    {
        if ((indicator instanceof EmisIndicatorRatio))
            return calculateRatio((EmisIndicatorRatio) indicator, values);
        if ((indicator instanceof EmisIndicatorSimple))
        {
            return calculateSimple((EmisIndicatorSimple) indicator, values);
        }
        return (0.0D / 0.0D);
    }

    public static double calculateSurplus(EmisIndicator indicator, Map<String, Double> values, String resourceKey, Double target)
    {
        double result = getRequired(indicator, values, resourceKey, target);
        if (Double.isNaN(result))
        {
            return result;
        }
        return result > 0.0D ? 0.0D : -result;
    }

    public static double calculateRequired(EmisIndicator indicator, Map<String, Double> values, String resourceKey, Double target)
    {
        double result = getRequired(indicator, values, resourceKey, target);
        if (Double.isNaN(result))
        {
            return result;
        }
        return result < 0.0D ? 0.0D : result;
    }

    private static double getRequired(EmisIndicator indicator, Map<String, Double> values, String resourceKey, Double target)
    {
        if (!(indicator instanceof EmisIndicatorRatio))
        {
            return (0.0D / 0.0D);
        }
        EmisIndicatorRatio indicatorRatio = (EmisIndicatorRatio) indicator;

        Double numerator = (Double) values.get("numerator");
        Double denominator = (Double) values.get("denominator");

        if ((denominator == null) || (Double.isNaN(denominator.doubleValue())) || (numerator == null) || (Double.isNaN(numerator.doubleValue())))
        {
            return (0.0D / 0.0D);
        }
        double factor = indicatorRatio.getFactor();
        if (Double.isNaN(factor))
        {
            factor = 1.0D;
        }
        double result = 0.0D;
        if (resourceKey.equals("numerator"))
            result = Math.floor(target.doubleValue() * denominator.doubleValue() / factor - numerator.doubleValue());
        else if (resourceKey.equals("denominator"))
            result = Math.floor(factor * numerator.doubleValue() / target.doubleValue() - denominator.doubleValue());
        else
        {
            return (0.0D / 0.0D);
        }
        return result;
    }

    public static double calculateRatio(EmisIndicatorRatio indicator, Map<String, Double> values)
    {
        Double numerator = (Double) values.get("numerator");
        Double denominator = (Double) values.get("denominator");

        if ((denominator == null) || (Double.isNaN(numerator.doubleValue())) || (Double.isNaN(denominator.doubleValue())) || (denominator.doubleValue() == 0.0D))
        {
            return (0.0D / 0.0D);
        }
        if (numerator == null)
        {
            return 0.0D;
        }
        if (Double.isNaN(indicator.getFactor()))
        {
            return numerator.doubleValue() / denominator.doubleValue();
        }
        return indicator.getFactor() * numerator.doubleValue() / denominator.doubleValue();
    }

    public static double calculateSimple(EmisIndicatorSimple indicator, Map<String, Double> values)
    {
        Double result = (Double) values.get("result");
        if (result == null)
        {
            return (0.0D / 0.0D);
        }
        return result.doubleValue();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.results.IndicatorUtil JD-Core Version:
 * 0.6.0
 */