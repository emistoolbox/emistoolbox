package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.results.Result;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class ResultToDatasetUtils
{
    private static String[] changeDuplicateLabels(String[] labels)
    {
        List dups = new ArrayList();
        for (String check : labels)
        {
            int loop = 0;
            String checkbase = check;
            while (dups.contains(check))
            {
                check = checkbase + StringUtils.rightPad(" ", loop);
                loop++;
            }
            dups.add(check);
        }
        return (String[]) dups.toArray(labels);
    }

    static CategoryDataset get1DCategoryDataset(Result result)
    {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        String[] labels = changeDuplicateLabels(result.getHeadings(0));

        int[] indexes = new int[1];
        for (int i = 0; i < labels.length; i++)
        {
            indexes[0] = i;
            double value = result.get(indexes);

            data.addValue(value > 0.0D ? value : 0.0D, labels[i], labels[i]);
        }
        return data;
    }
    
    static CategoryDataset get1DCategoryDatasetAsSeries(Result result)
    {
        String mainHeading = " "; 
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        String[] headings = new String[] { mainHeading }; 
        String[] seriesHeadings = changeDuplicateLabels(result.getHeadings(0));

        int[] indexes = new int[1];

        for (int i = 0; i < seriesHeadings.length; i++)
        {
            indexes[0] = i;
            double value = result.get(indexes);
                
            data.addValue(value > 0.0D ? value : 0.0D, mainHeading, seriesHeadings[i]); 
        }

        return data;

    }

    static PieDataset get1DPieDataset(Result result)
    {
        DefaultPieDataset data = new DefaultPieDataset();
        String[] labels = changeDuplicateLabels(result.getHeadings(0));

        int[] indexes = new int[1];

        for (int i = 0; i < labels.length; i++)
        {
            indexes[0] = i;
            double value = result.get(indexes);

            data.setValue(labels[i], value > 0.0D ? value : 0.0D);
        }

        return data;
    }

    static XYDataset get2DXYDataset(Result result)
    {
        XYSeriesCollection data = new XYSeriesCollection();

        String[] labels = changeDuplicateLabels(result.getHeadings(1));

        int[] indexes = new int[2];
        XYSeries series = new XYSeries(labels[0]);
        for (int i = 0; i < result.getDimensionSize(1); i++)
        {
            indexes[0] = 0;
            indexes[1] = i;
            double x = result.get(indexes);

            indexes[0] = 1;
            double y = result.get(indexes);
            series.add(x, y);
        }
        data.addSeries(series);

        return data;
    }

    static XYDataset getMultiSeriesXYDataset(Result result)
    {
        XYSeriesCollection data = new XYSeriesCollection();

        int[] indexes = new int[result.getDimensions()];
        for (int i = 1; i < result.getDimensions(); i++)
        {
            XYSeries series = new XYSeries(result.getHeading(i, 0));
            for (int j = 0; j < result.getDimensionSize(i); j++)
            {
                indexes[0] = 0;
                setIndexesForSeries(indexes, i, j);
                double x = result.get(indexes);

                indexes[0] = 1;
                double y = result.get(indexes);
                series.add(x, y);
            }
            data.addSeries(series);
        }
        return data;
    }

    private static void setIndexesForSeries(int[] indexes, int series, int seriesVal)
    {
        for (int loop = 1; loop < indexes.length; loop++)
        {
            if (loop == series)
                indexes[loop] = seriesVal;
            else
                indexes[loop] = 0;
        }
    }

    static CategoryDataset get2DCategoryDataset(Result result)
    { return get2DCategoryDataset(result, false); }
    
    static CategoryDataset get2DCategoryDataset(Result result, boolean reverseSeriesOrder)
    {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        String[] headings = changeDuplicateLabels(result.getHeadings(0));
        String[] seriesHeadings = changeDuplicateLabels(result.getHeadings(1));

        int[] indexes = new int[2];

        for (int i = 0; i < headings.length; i++)
        {
            for (int j = 0; j < seriesHeadings.length; j++)
            {
                int seriesIndex = reverseSeriesOrder ? seriesHeadings.length - 1 - j : j; 
                indexes[0] = i;
                indexes[1] = seriesIndex;
                double value = result.get(indexes);

                data.addValue(value > 0.0D ? value : 0.0D, seriesHeadings[seriesIndex], headings[i]);
            }
        }

        return data;
    }
}
