package com.emistoolbox.server.renderer.charts.impl;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;

public class PercentLimitedPieSectionLabelGenerator extends StandardPieSectionLabelGenerator
{
    private final double percentLimiter;
    private static final long serialVersionUID = 1L;

    public PercentLimitedPieSectionLabelGenerator(String format, DecimalFormat numberFormat, DecimalFormat percentageFormat, double percentLimiter) {
        super(format, numberFormat, percentageFormat);

        if ((percentLimiter < 0.0D) || (percentLimiter > 100.0D))
            throw new IllegalArgumentException("Percent Limitation not between 0 and 100");
        this.percentLimiter = (percentLimiter / 100.0D);
    }

    public String generateSectionLabel(PieDataset dataset, Comparable key)
    {
        String result = null;
        if (dataset != null)
        {
            Object[] items = createItemArray(dataset, key);
            try
            {
                Double percent = (Double) items[2];
                if (percent.doubleValue() < this.percentLimiter)
                    return null;
                items[2] = getPercentFormat().format(percent.doubleValue());
            }
            catch (Exception e)
            {
            }
            result = MessageFormat.format(getLabelFormat(), items);
        }
        return result;
    }

    protected Object[] createItemArray(PieDataset dataset, Comparable key)
    {
        Object[] result = new Object[4];
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        result[0] = key.toString();
        Number value = dataset.getValue(key);
        if (value != null)
            result[1] = getNumberFormat().format(value);
        else
            result[1] = "null";

        double percent = 0.0D;
        if (value != null)
        {
            double v = value.doubleValue();
            if (v > 0.0D)
            {
                percent = v / total;
            }
        }
        result[2] = new Double(percent);
        result[3] = getNumberFormat().format(total);
        return result;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.charts.impl.
 * PercentLimitedPieSectionLabelGenerator JD-Core Version: 0.6.0
 */