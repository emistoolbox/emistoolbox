package com.emistoolbox.server.model.analysis;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisIndicatorRatio;
import com.emistoolbox.common.model.analysis.EmisIndicatorScript;
import com.emistoolbox.common.model.analysis.EmisIndicatorSimple;
import java.util.Map;

public class IndicatorUtil
{
    public static double calculate(EmisIndicator indicator, Map<String, EmisAggregator> aggregators)
    {
        if ((indicator instanceof EmisIndicatorSimple))
            return get(aggregators, "result", 0.0D);
        if ((indicator instanceof EmisIndicatorRatio))
        {
            double numerator = get(aggregators, "numerator", 0.0D);
            double denominator = get(aggregators, "denominator", 0.0D);

            if ((denominator == 0.0D) || (Double.isNaN(denominator)))
            {
                return (0.0D / 0.0D);
            }
            return numerator / denominator;
        }
        if ((indicator instanceof EmisIndicatorScript))
            ;
        return (0.0D / 0.0D);
    }

    public static double get(Map<String, EmisAggregator> aggregators, String name, double defaultValue)
    {
        EmisAggregator aggr = (EmisAggregator) aggregators.get(name);
        if (aggr == null)
            return defaultValue;

        return aggr.getValue();
    }
}

