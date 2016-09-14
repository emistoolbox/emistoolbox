package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.impl.IndicatorRatio;
import com.emistoolbox.common.model.analysis.impl.IndicatorTimeRatio;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

public class IndicatorEditor extends FlexTable implements EmisEditor<EmisIndicator>
{
    private int aggregatorRow = 0;
    private EmisIndicator indicator;
    private TextBox uiName = new TextBox();
    private TextBox uiGroupName = new TextBox();
    private TextBox uiYAxis = new TextBox(); 

    private TextBox uiMaxValue = new TextBox();
    private TextBox uiFactor = new TextBox();
    private HTML uiFactorText = new HTML(EmisToolbox.div(EmisToolbox.CSS_SECTION, Message.messageAdmin().ieHtmlFactor()));

    private ListBox uiTimeOffset = new ListBox();
    private HTML uiTimeOffsetText = new HTML(EmisToolbox.div(EmisToolbox.CSS_SECTION, Message.messageAdmin().ieTimeOffset()));

    private Grid uiThresholdGrid = new Grid(2, 4);
    private ListBox uiThresholdType = new ListBox();
    private TextBox uiBadThreshold = new TextBox();
    private TextBox uiBadThresholdText = new TextBox();
    private TextBox uiGoodThreshold = new TextBox();
    private TextBox uiGoodThresholdText = new TextBox();
    private EmisMeta meta;

    public IndicatorEditor(EmisMeta meta) {
        this.meta = meta;

        this.uiName.setWidth("350px");
        this.uiGroupName.setWidth("350px");
        this.uiYAxis.setWidth("350px");
        this.uiMaxValue.setWidth("50px");
        this.uiBadThreshold.setWidth("50px");
        this.uiGoodThreshold.setWidth("50px");
        this.uiFactor.setWidth("50px");
        this.uiTimeOffset.addItem(Message.messageAdmin().ieCurrentPrev(meta.getDefaultDateType().getName()));
        this.uiTimeOffset.addItem(Message.messageAdmin().ieCurrentNext(meta.getDefaultDateType().getName()));

        this.uiThresholdType.addItem(Message.messageAdmin().ieTextBadLowerThanGood());
        this.uiThresholdType.addItem(Message.messageAdmin().ieTextGoodLowerThanBad());
        this.uiThresholdType.addItem(Message.messageAdmin().ieTextGoodLowerThan().toLowerCase());
        this.uiThresholdType.addItem(Message.messageAdmin().ieTextGoodHigherThan().toLowerCase());
        this.uiThresholdType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                IndicatorEditor.this.updateThresholds();
            }
        });
        int row = 0; 
        setHTML(row, 0, EmisToolbox.div(EmisToolbox.CSS_SECTION, Message.messageAdmin().ieHtmlName() + ":"));
        setWidget(row, 1, this.uiName);
        row++; 
        
        setHTML(row, 0, EmisToolbox.div(EmisToolbox.CSS_SECTION, "Group Name:")); 
        setWidget(row, 1, this.uiGroupName);
        row++; 

        getFlexCellFormatter().setColSpan(row, 0, 2);
        setHTML(row, 0, "<hr>");
        row++; 
        
        setHTML(row, 0, EmisToolbox.div(EmisToolbox.CSS_SECTION, Message.messageAdmin().ieHtmlThresholds() + ":"));
        setWidget(row, 1, this.uiThresholdType);
        row++; 
        
        setWidget(row, 1, this.uiThresholdGrid);
        row++; 
        
        setHTML(row, 0, EmisToolbox.div(EmisToolbox.CSS_SECTION, Message.messageAdmin().ieHtmlMaxValue() + ":"));
        setWidget(row, 1, this.uiMaxValue);
        row++; 
        
        setHTML(row, 0, EmisToolbox.div(EmisToolbox.CSS_SECTION, Message.messageAdmin().ieHtmlYAxis() + ":")); 
        setWidget(row, 1, this.uiYAxis);
        row++; 
        
        getFlexCellFormatter().setColSpan(row, 0, 2);
        setHTML(row, 0, "<hr>");
        row++; 
        
        setWidget(row, 0, this.uiFactorText);
        setWidget(row, 1, this.uiFactor);
        row++; 
        
        setWidget(row, 0, this.uiTimeOffsetText);
        setWidget(row, 1, this.uiTimeOffset);
        row++; 
        
        getFlexCellFormatter().setColSpan(row, 0, 2);
        setHTML(row, 0, "<hr>");
        row++; 

        aggregatorRow = row; 
    }

    private void updateThresholds()
    {
        int index = this.uiThresholdType.getSelectedIndex();
        this.uiThresholdGrid.clear(true);

        if (index == 0)
        {
            this.uiThresholdGrid.setText(0, 0, Message.messageAdmin().ieTextBadLowerThan());
            this.uiThresholdGrid.setWidget(0, 1, this.uiBadThreshold);
            this.uiThresholdGrid.setText(0, 2, Message.messageAdmin().ieLabelBad());
            this.uiThresholdGrid.setWidget(0, 3, this.uiBadThresholdText);

            this.uiThresholdGrid.setText(1, 0, Message.messageAdmin().ieTextGoodHigherThan());
            this.uiThresholdGrid.setWidget(1, 1, this.uiGoodThreshold);
            this.uiThresholdGrid.setText(1, 2, Message.messageAdmin().ieLabelGood());
            this.uiThresholdGrid.setWidget(1, 3, this.uiGoodThresholdText);
        }
        else if (index == 1)
        {
            this.uiThresholdGrid.setText(0, 0, Message.messageAdmin().ieTextGoodLowerThan());
            this.uiThresholdGrid.setWidget(0, 1, this.uiGoodThreshold);
            this.uiThresholdGrid.setText(0, 2, Message.messageAdmin().ieLabelGood());
            this.uiThresholdGrid.setWidget(0, 3, this.uiGoodThresholdText);

            this.uiThresholdGrid.setText(1, 0, Message.messageAdmin().ieTextBadHigherThan());
            this.uiThresholdGrid.setWidget(1, 1, this.uiBadThreshold);
            this.uiThresholdGrid.setText(1, 2, Message.messageAdmin().ieLabelBad());
            this.uiThresholdGrid.setWidget(1, 3, this.uiBadThresholdText);
        }
        else if (index == 2)
        {
            this.uiThresholdGrid.setText(0, 0, Message.messageAdmin().ieTextGoodLowerThan());
            this.uiThresholdGrid.setWidget(0, 1, this.uiGoodThreshold);
            this.uiThresholdGrid.setText(0, 2, Message.messageAdmin().ieLabel());
            this.uiThresholdGrid.setWidget(0, 3, this.uiGoodThresholdText);
        }
        else if (index == 3)
        {
            this.uiThresholdGrid.setText(0, 0, Message.messageAdmin().ieTextGoodHigherThan());
            this.uiThresholdGrid.setWidget(0, 1, this.uiGoodThreshold);
            this.uiThresholdGrid.setText(0, 2, Message.messageAdmin().ieLabel());
            this.uiThresholdGrid.setWidget(0, 3, this.uiGoodThresholdText);
        }
    }

    private void setTextBoxFromDouble(TextBox tb, double value)
    {
        if (Double.isNaN(value))
            tb.setText("");
        else
            tb.setText("" + value);
    }

    private double getDoubleFromTextBox(TextBox tb)
    {
        String text = tb.getText();
        if ((text == null) || (text.equals("")))
            return (0.0D / 0.0D);
        try
        {
            return Double.parseDouble(text);
        }
        catch (Throwable err)
        {
        }
        return (0.0D / 0.0D);
    }

    public void updateUi()
    {
        this.uiName.setText(this.indicator.getName());
        this.uiGroupName.setText(this.indicator.getGroupName());
        this.uiYAxis.setText(indicator.getYAxisLabel()); 
        setTextBoxFromDouble(this.uiMaxValue, this.indicator.getMaxValue());

        int index = 0;
        if (Double.isNaN(this.indicator.getBadThreshold()))
            index = this.indicator.getBiggerIsBetter() ? 4 : 3;
        else if (!Double.isNaN(this.indicator.getGoodThreshold()))
            index = this.indicator.getGoodThreshold() < this.indicator.getBadThreshold() ? 1 : 0;
        this.uiThresholdType.setSelectedIndex(index);
        updateThresholds();

        setTextBoxFromDouble(this.uiBadThreshold, this.indicator.getBadThreshold());
        this.uiBadThresholdText.setText(this.indicator.getBadThresholdText());

        setTextBoxFromDouble(this.uiGoodThreshold, this.indicator.getGoodThreshold());
        this.uiGoodThresholdText.setText(this.indicator.getGoodThresholdText());

        if ((this.indicator instanceof IndicatorRatio))
        {
            setTextBoxFromDouble(this.uiFactor, ((IndicatorRatio) this.indicator).getFactor());
            this.uiFactor.setVisible(true);
            this.uiFactorText.setVisible(true);
        }
        else
        {
            this.uiFactor.setVisible(false);
            this.uiFactorText.setVisible(false);
        }

        if ((this.indicator instanceof IndicatorTimeRatio))
        {
            this.uiTimeOffset.setSelectedIndex(((IndicatorTimeRatio) this.indicator).getTimeOffset() < 0 ? 0 : 1);
            this.uiTimeOffset.setVisible(true);
            this.uiTimeOffsetText.setVisible(true);
        }
        else
        {
            this.uiTimeOffset.setVisible(false);
            this.uiTimeOffsetText.setVisible(false);
        }

        int row = aggregatorRow;
        for (String aggregator : this.indicator.getAggregatorNames())
        {
            if (row != aggregatorRow)
            {
                setHTML(row, 1, "<hr>");
                row++;
            }

            getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
            setHTML(row, 0, EmisToolbox.div(EmisToolbox.CSS_SECTION_BLUE, aggregator));
            
            EmisEditor<EmisAggregatorDef>[] editors = getEditor(this.indicator.getAggregator(aggregator)); 
            for (int i = 0; i < editors.length; i++)
            	setWidget(row, i + 1, (Widget) editors[i]); 
            
            row++;
        }
    }

    private EmisEditor<EmisAggregatorDef>[] getEditor(EmisAggregatorDef aggregator)
    {
        AggregatorEditor editor = new AggregatorEditor(this.meta, 0);

        EmisEditor<EmisAggregatorDef> weightEditor = new OptionalAggregatorEditor(new AggregatorEditor(this.meta, editor.getNameHeight()));
        
        editor.setWeightEditor(weightEditor); 
        editor.set(aggregator);

        return new EmisEditor[] { editor, weightEditor };
    }

    public void commit()
    {
        this.indicator.setName(uiName.getText());
        this.indicator.setGroupName(uiGroupName.getText());
        this.indicator.setYAxisLabel(uiYAxis.getText()); 
        this.indicator.setMaxValue(getDoubleFromTextBox(this.uiMaxValue));

        int index = this.uiThresholdType.getSelectedIndex();
        if ((index == 0) || (index == 1))
            this.indicator.setThreshold(getDoubleFromTextBox(this.uiGoodThreshold), this.uiGoodThresholdText.getText(), getDoubleFromTextBox(this.uiBadThreshold), this.uiBadThresholdText.getText());
        else
            this.indicator.setThreshold(getDoubleFromTextBox(this.uiGoodThreshold), this.uiGoodThresholdText.getText(), index == 4);

        if ((this.indicator instanceof IndicatorRatio))
            ((IndicatorRatio) this.indicator).setFactor(getDoubleFromTextBox(this.uiFactor));
        if ((this.indicator instanceof IndicatorTimeRatio))
            ((IndicatorTimeRatio) this.indicator).setTimeOffset(this.uiTimeOffset.getSelectedIndex() == 0 ? -1 : 1);

        Map<String, EmisAggregatorDef> aggregators = this.indicator.getAggregators();
        int row = aggregatorRow;
        while (row < getRowCount())
        {
            Widget w = getWidget(row, 1);
            if ((w instanceof AggregatorEditor))
            {
                EmisAggregatorDef aggregator = ((AggregatorEditor) w).get();
                aggregators.put(getText(row, 0), aggregator);
            }

            row++;
        }

        this.indicator.setAggregators(aggregators);
    }

    public EmisIndicator get()
    {
        commit();
        return this.indicator;
    }

    public void set(EmisIndicator data)
    {
        this.indicator = data;
        updateUi();
    }
}

class OptionalAggregatorEditor extends SimplePanel implements EmisEditor<EmisAggregatorDef>
{
	private boolean shown; 
	private AggregatorEditor editor; 
	private boolean visible = false; 
	
	public OptionalAggregatorEditor(AggregatorEditor editor)
	{
		this.editor = editor; 
		editor.setVisible(false);
		setWidget(editor); 
	}
	
	@Override
	public void commit() 
	{
		if (visible)
			editor.commit(); 
	}

	@Override
	public EmisAggregatorDef get() 
	{
		if (visible)
			return editor.get(); 
		
		return null;
	}

	@Override
	public void set(EmisAggregatorDef aggr) 
	{
		if (aggr == null)
			visible = false; 
		else
		{
			visible = true; 
			editor.set(aggr); 
		}

		editor.setVisible(visible);
	}
}
