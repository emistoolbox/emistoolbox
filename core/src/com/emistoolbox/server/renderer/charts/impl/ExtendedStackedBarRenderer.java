package com.emistoolbox.server.renderer.charts.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

class ExtendedStackedBarRenderer extends StackedBarRenderer
{
    private static final long serialVersionUID = 1L;
    private boolean showPositiveTotal = true;

    private boolean showNegativeTotal = true;

    private Font totalLabelFont = null;
    private NumberFormat totalFormatter;
    private ItemLabelPosition _positiveLabelPos = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BOTTOM_CENTER, TextAnchor.BOTTOM_CENTER, 0.0D);
    private int seriesCount;

    public ExtendedStackedBarRenderer(int seriesCount, Font font) {
        this.seriesCount = seriesCount; 
        this.totalFormatter = NumberFormat.getInstance();
        this.totalLabelFont = font;
    }

    public NumberFormat getTotalFormatter()
    {
        return this.totalFormatter;
    }

    public void setTotalFormatter(NumberFormat format)
    {
        if (format == null)
        {
            throw new IllegalArgumentException("Null format not permitted.");
        }
        this.totalFormatter = format;
    }

    public ItemLabelPosition getTotalPositiveLabelPosition()
    {
        return this._positiveLabelPos;
    }

    public void setTotalPositiveLabelPosition(ItemLabelPosition labelPos)
    {
        this._positiveLabelPos = labelPos;
    }

    public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass)
    {
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null)
        {
            return;
        }

        double value = dataValue.doubleValue();

        PlotOrientation orientation = plot.getOrientation();
        double barW0 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;

        double positiveBase = 0.0D;
        double negativeBase = 0.0D;

        for (int i = 0; i < row; i++)
        {
            Number v = dataset.getValue(i, column);
            if (v != null)
            {
                double d = v.doubleValue();
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

        RectangleEdge location = plot.getRangeAxisEdge();
        double translatedValue;
        double translatedBase;
        if (value > 0.0D)
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

        Paint seriesPaint = getItemPaint(row, column);
        g2.setPaint(seriesPaint);
        g2.fill(bar);
        if ((isDrawBarOutline()) && (state.getBarWidth() > 3.0D))
        {
            g2.setStroke(getItemStroke(row, column));
            g2.setPaint(getItemOutlinePaint(row, column));
            g2.draw(bar);
        }

        CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);

        if ((generator != null) && (isItemLabelVisible(row, column)))
        {
            drawItemLabel(g2, dataset, row, column, plot, generator, bar, value < 0.0D);
        }

        if (value > 0.0D)
        {
            if ((this.showPositiveTotal) && (isLastPositiveItem(dataset, row, column)))
            {
                g2.setPaint(Color.black);
                g2.setFont(this.totalLabelFont);
                double total = calculateSumOfPositiveValuesForCategory(dataset, column);

                String text = this.totalFormatter.format(total);
                Rectangle2D box = TextUtilities.getTextBounds(text, g2, g2.getFontMetrics(this.totalLabelFont));

                TextUtilities.drawRotatedString(text, g2, (float) bar.getCenterX(), (float) bar.getMinY() - (this._positiveLabelPos.getAngle() == 0.0D ? 0.0F : (float) box.getWidth()), this._positiveLabelPos.getTextAnchor(),
                        this._positiveLabelPos.getAngle(), this._positiveLabelPos.getRotationAnchor());
            }

        }
        else if ((this.showNegativeTotal) && (isLastNegativeItem(dataset, row, column)))
        {
            g2.setPaint(Color.black);
            g2.setFont(this.totalLabelFont);
            double total = calculateSumOfNegativeValuesForCategory(dataset, column);

            TextUtilities.drawRotatedString(String.valueOf(total), g2, (float) bar.getCenterX(), (float) (bar.getMaxY() + 4.0D), TextAnchor.TOP_CENTER, 0.0D, TextAnchor.TOP_CENTER);
        }

        if (state.getInfo() != null)
        {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null)
            {
                String tip = null;
                CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);

                if (tipster != null)
                {
                    tip = tipster.generateToolTip(dataset, row, column);
                }
                String url = null;
                if (getItemURLGenerator(row, column) != null)
                {
                    url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
                }

                CategoryItemEntity entity = new CategoryItemEntity(bar, tip, url, dataset, Integer.valueOf(row), dataset.getColumnKey(column));

                entities.add(entity);
            }
        }
    }

    private boolean isLastPositiveItem(CategoryDataset dataset, int row, int column)
    {
        boolean result = true;
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null)
        {
            return false;
        }
        for (int r = row + 1; r < dataset.getRowCount(); r++)
        {
            dataValue = dataset.getValue(r, column);
            if (dataValue != null)
            {
                result = (result) && (dataValue.doubleValue() <= 0.0D);
            }
        }
        return result;
    }

    private boolean isLastNegativeItem(CategoryDataset dataset, int row, int column)
    {
        boolean result = true;
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null)
        {
            return false;
        }
        for (int r = row + 1; r < dataset.getRowCount(); r++)
        {
            dataValue = dataset.getValue(r, column);
            if (dataValue != null)
            {
                result = (result) && (dataValue.doubleValue() >= 0.0D);
            }
        }
        return result;
    }

    private double calculateSumOfPositiveValuesForCategory(CategoryDataset dataset, int column)
    {
        double result = 0.0D;
        for (int r = 0; r < dataset.getRowCount(); r++)
        {
            Number dataValue = dataset.getValue(r, column);
            if (dataValue != null)
            {
                double v = dataValue.doubleValue();
                if (v > 0.0D)
                {
                    result += v;
                }
            }
        }
        return result;
    }

    private double calculateSumOfNegativeValuesForCategory(CategoryDataset dataset, int column)
    {
        double result = 0.0D;
        for (int r = 0; r < dataset.getRowCount(); r++)
        {
            Number dataValue = dataset.getValue(r, column);
            if (dataValue != null)
            {
                double v = dataValue.doubleValue();
                if (v < 0.0D)
                {
                    result += v;
                }
            }
        }
        return result;
    }

    @Override
    public LegendItem getLegendItem(int datasetIndex, int series)
    { return super.getLegendItem(datasetIndex, seriesCount - series - 1); }
}
