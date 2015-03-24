package com.emistoolbox.server.renderer.charts.impl;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.DataUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

class NormalizedStackedBarRenderer extends StackedBarRenderer
{
    private static final long serialVersionUID = 1L;
    private static final double DEFAULT_MAX_VAL = 100.0D;
    private final double maxVal;

    public NormalizedStackedBarRenderer() {
        this(100.0D);
    }

    public NormalizedStackedBarRenderer(double max) {
        this.maxVal = max;
    }

    public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass)
    {
        if (!isSeriesVisible(row))
        {
            return;
        }

        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null)
        {
            return;
        }

        double value = dataValue.doubleValue();
        double total = 0.0D;

        total = DataUtilities.calculateColumnTotal(dataset, column, state.getVisibleSeriesArray());

        value = value / total * this.maxVal;

        PlotOrientation orientation = plot.getOrientation();
        double barW0 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;

        double positiveBase = getBase();
        double negativeBase = positiveBase;

        for (int i = 0; i < row; i++)
        {
            Number v = dataset.getValue(i, column);
            if ((v != null) && (isSeriesVisible(i)))
            {
                double d = v.doubleValue();

                d = d / total * this.maxVal;

                if (d > 0.0D)
                {
                    positiveBase += d;
                }
                else
                {
                    negativeBase += d;
                }

            }

        }

        boolean positive = value > 0.0D;
        boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if (orientation == PlotOrientation.HORIZONTAL)
        {
            if (((positive) && (inverted)) || ((!positive) && (!inverted)))
            {
                barBase = RectangleEdge.RIGHT;
            }
            else
                barBase = RectangleEdge.LEFT;
        }
        else
        {
            if (((positive) && (!inverted)) || ((!positive) && (inverted)))
            {
                barBase = RectangleEdge.BOTTOM;
            }
            else
            {
                barBase = RectangleEdge.TOP;
            }
        }

        RectangleEdge location = plot.getRangeAxisEdge();
        double translatedValue;
        double translatedBase;
        if (positive)
        {
            translatedBase = rangeAxis.valueToJava2D(positiveBase, dataArea, location);
            translatedValue = rangeAxis.valueToJava2D(positiveBase + value, dataArea, location);
        }
        else
        {
            translatedBase = rangeAxis.valueToJava2D(negativeBase, dataArea, location);
            translatedValue = rangeAxis.valueToJava2D(negativeBase + value, dataArea, location);
        }

        double barL0 = Math.min(translatedBase, translatedValue);
        double barLength = Math.max(Math.abs(translatedValue - translatedBase), getMinimumBarLength());

        Rectangle2D bar = null;
        if (orientation == PlotOrientation.HORIZONTAL)
        {
            bar = new Rectangle2D.Double(barL0, barW0, barLength, state.getBarWidth());
        }
        else
        {
            bar = new Rectangle2D.Double(barW0, barL0, state.getBarWidth(), barLength);
        }

        if (pass == 0)
        {
            if (getShadowsVisible())
            {
                boolean pegToBase = ((positive) && (positiveBase == getBase())) || ((!positive) && (negativeBase == getBase()));

                getBarPainter().paintBarShadow(g2, this, row, column, bar, barBase, pegToBase);
            }

        }
        else if (pass == 1)
        {
            getBarPainter().paintBar(g2, this, row, column, bar, barBase);

            EntityCollection entities = state.getEntityCollection();
            if (entities != null)
            {
                addItemEntity(entities, dataset, row, column, bar);
            }
        }
        else if (pass == 2)
        {
            CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);

            if ((generator != null) && (isItemLabelVisible(row, column)))
                drawItemLabel(g2, dataset, row, column, plot, generator, bar, value < 0.0D);
        }
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.charts.impl.NormalizedStackedBarRenderer
 * JD-Core Version: 0.6.0
 */