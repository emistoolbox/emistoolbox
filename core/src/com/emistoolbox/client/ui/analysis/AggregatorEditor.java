package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.ui.analysis.FilterPanel.FILTER_STATE;
import com.emistoolbox.client.util.ui.CenteredPositionCallback;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.FilterTarget;
import com.emistoolbox.common.model.analysis.impl.AggregatorDef;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.analysis.impl.SampleAggregatorDef;
import com.emistoolbox.common.model.analysis.impl.WeightedAggregatorDef;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AggregatorEditor extends FlexTable implements EmisEditor<EmisAggregatorDef>
{
    private EmisAggregatorDef aggregator;
    private Label uiData = new Label();
    private TextBox uiName = new TextBox();
    private CheckBox uiWeighted = new CheckBox("Weighted Value"); 
    private FilterPanel<EnumSetEditor> fieldEnumFilter; 
    private FilterPanel<EnumSetEditor>[] enumSetEditors;
    private FilterPanel<BoolFilterEditor>[] entityBoolFilterEditors;
    private FilterPanel<EnumSetEditor>[] entityEnumFilterEditors;
    
    private int editorRow = -1; 
    
    private boolean weightUi = false; 
    private EmisEditor<EmisAggregatorDef> weightEditor = null; 
    
    public AggregatorEditor(final EmisMeta meta, int nameHeight) 
    {
    	this.weightUi = nameHeight > 0;
    	
    	int row = 0; 
    	if (!weightUi)
    	{
	    	setHTML(row, 0, EmisToolbox.span(EmisToolbox.CSS_SECTION, Message.messageAdmin().aeHtmlName() + ":") + "<br><small>" + Message.messageAdmin().aeHtmlOnlyToDisplayInTable() + "</small>");
	        setWidget(row, 1, this.uiName);
    	}
    	else
    	{
    		HTML html = new HTML();
    		html.setHeight(nameHeight + "px");
    		setWidget(row, 0, html); 
    	}
		row++; 
    	
    	
        setHTML(row, 0, EmisToolbox.span(EmisToolbox.CSS_SECTION, weightUi ? "Weight Field:" : Message.messageAdmin().aeHtmlDataField()));
        setWidget(row, 1, this.uiData);
        row++; 

        if (!weightUi)
        {
        	setWidget(row, 1, uiWeighted); 
        	uiWeighted.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				public void onValueChange(ValueChangeEvent<Boolean> event) 
				{ weightEditor.set(uiWeighted.getValue() ? new AggregatorDef() : null); }
        	}); 
        	row++; 
        }
        
        uiData.setStyleName("value");
        this.uiData.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                TabbedEntityDataPicker uiDataPicker = new TabbedEntityDataPicker(meta, AggregatorEditor.this.aggregator);

                final PopupPanel popup = new PopupPanel();
                popup.setAutoHideEnabled(true);
                popup.setModal(true);
                popup.add(uiDataPicker);
				popup.setPopupPositionAndShow(new CenteredPositionCallback(popup));

                uiDataPicker.addSelectionHandler(new SelectionHandler<EmisAggregatorDef>() {
                    public void onSelection(SelectionEvent<EmisAggregatorDef> event)
                    {
                        popup.hide();
                        AggregatorEditor.this.updateUi();
                    }
                });
            }
        });
        
        editorRow = row; 
    }

    public int getNameHeight()
    { return 20; }
    
    public void setWeightEditor(EmisEditor<EmisAggregatorDef> weightEditor)
    {
    	if (!weightUi)
    		this.weightEditor = weightEditor; 
    } 
    
    public void commit()
    {
    	if (this.aggregator == null)
    		return; 
    	
        EmisContext context = new Context();
        if (!weightUi)
        	this.aggregator.setName(this.uiName.getText());

        // Clear ignore filters. 
        aggregator.setIgnoreFilters(new ArrayList<FilterTarget>());
        
        if (fieldEnumFilter != null)
        {
        	if (fieldEnumFilter.hasFilter())
        	{
        		EmisEnumSet values = fieldEnumFilter.getEditor().get(); 
        		if (!values.hasAllValues())
        			context.addEnumFilter(values); 
        	}
        	else if (fieldEnumFilter.hasIgnoreFilter())
        		aggregator.addIgnoreFilter(new FilterTarget(fieldEnumFilter.getField())); 
        }
       
        if (this.enumSetEditors != null)
        {
            for (FilterPanel<EnumSetEditor> panel : this.enumSetEditors)
            {
            	if (panel.hasFilter())
            	{
                    EmisEnumSet values = panel.getEditor().get();
                    if (!values.hasAllValues())
                        context.addEnumFilter(values);
            	}
            	else if (panel.hasIgnoreFilter())
            		aggregator.addIgnoreFilter(new FilterTarget(panel.getField(), panel.getEnumType()));
            }
        }
        
        if (this.entityBoolFilterEditors != null)
        {
            for (FilterPanel<BoolFilterEditor> panel : this.entityBoolFilterEditors)
            {
            	if (panel.hasFilter())
            	{
            		Boolean filterValue = panel.getEditor().get();
            		if (filterValue != null)
            			context.addBooleanEntityFilter(panel.getEditor().getMetaData(), filterValue);
                }
            	else if (panel.hasIgnoreFilter())
            		aggregator.addIgnoreFilter(new FilterTarget(panel.getField(), panel.getEnumType())); 
            }
        }

        if (this.entityEnumFilterEditors != null)
        {
            for (FilterPanel<EnumSetEditor> panel : this.entityEnumFilterEditors)
            {
            	if (panel.hasFilter())
            	{
            		EmisEnumSet values = panel.getEditor().get();
            		if (!values.hasAllValues())
            			context.addEnumEntityFilter(panel.getEditor().getMetaData(), values);
            	}
            	else if (panel.hasIgnoreFilter())
            		aggregator.addIgnoreFilter(new FilterTarget(panel.getField(), panel.getEnumType())); 
            		
            }
        }

        Set<String> names = context.getEntityFilterNames(this.aggregator.getEntity());
        if (((context.getEnumFilters() == null) || (context.getEnumFilters().size() == 0)) && ((names == null) || (names.size() == 0)))
            this.aggregator.setContext(null);
        else
            this.aggregator.setContext(context);
        
    }

    public EmisAggregatorDef get()
    {
        commit();

        EmisAggregatorDef weightAggregator = null; 
        if (!weightUi && weightEditor != null)
        	weightAggregator = weightEditor.get(); 

        if (weightAggregator == null)
        	return aggregator;
        
        WeightedAggregatorDef result = new WeightedAggregatorDef(); 
        result.setAggregator(WeightedAggregatorDef.AGGR_VALUE, aggregator);
        result.setAggregator(WeightedAggregatorDef.AGGR_WEIGHT, weightAggregator); 
        
        return result; 
    }

    public void set(EmisAggregatorDef newAggregator)
    {
        if (newAggregator instanceof WeightedAggregatorDef)
    	{
    		if (!weightUi)
    		{
    			WeightedAggregatorDef weightedAggr = (WeightedAggregatorDef) newAggregator; 
    			this.aggregator = weightedAggr.getAggregator(WeightedAggregatorDef.AGGR_VALUE);
    			weightEditor.set(weightedAggr.getAggregator(WeightedAggregatorDef.AGGR_WEIGHT));
    		}
    		else
    			throw new IllegalArgumentException("Invalid aggregator type");
    	}
    	else if (!(newAggregator instanceof SampleAggregatorDef))
            aggregator = newAggregator;
    	else
    		throw new IllegalArgumentException("Invalid aggregator type"); 
    	
        if (aggregator == null)
            aggregator = new AggregatorDef();

        this.uiName.setText(aggregator.getName());
        updateEnumEditors(aggregator);
        updateEntityFilterEditors(aggregator);

        updateUi();
    }

    
    private void updateEntityFilterEditors(EmisAggregatorDef aggregator)
    {
        this.entityBoolFilterEditors = null;
        this.entityEnumFilterEditors = null;

        EmisContext context = aggregator.getContext();
        if (aggregator.getEntity() == null && aggregator.getMetaData() == null)
        	return; 
        
        List<FilterPanel<BoolFilterEditor>> boolFilters = new ArrayList<FilterPanel<BoolFilterEditor>>();
        List<FilterPanel<EnumSetEditor>> enumFilters = new ArrayList<FilterPanel<EnumSetEditor>>();

        EmisMetaEntity entity = aggregator.getEntity() == null ? aggregator.getMetaData().getEntity() : aggregator.getEntity();
        Set<String> names = getEntityFilterNames(entity, context, aggregator); 
        for (String name : names)
        {
            EmisMetaData field = (EmisMetaData) NamedUtil.find(name, entity.getData());
            if (field == null)
                continue;

            byte[] values = context == null ? null : context.getEntityFilterValues(field);
            if (field.getType() == EmisMetaData.EmisDataType.BOOLEAN)
            {
            	FilterPanel<BoolFilterEditor> panel = initBoolFilterPanel(aggregator, field, getBooleanFromValues(values)); 
                boolFilters.add(panel);
            }
            else
            {
                FilterPanel<EnumSetEditor> panel = initEnumFilterPanel(aggregator, field, field.getEnumType(), getEnumSetFromValues(field.getEnumType(), values)); 
                enumFilters.add(panel);
            }
        }

        if (boolFilters.size() > 0)
            this.entityBoolFilterEditors = ((FilterPanel<BoolFilterEditor>[]) boolFilters.toArray(new FilterPanel[0]));

        if (enumFilters.size() > 0)
            this.entityEnumFilterEditors = ((FilterPanel<EnumSetEditor>[]) enumFilters.toArray(new FilterPanel[0]));
    }
    
    private Set<String> getEntityFilterNames(EmisMetaEntity entityType, EmisContext context, EmisAggregatorDef aggr)
    {
    	Set<String> result = new HashSet<String>(); 
    	
    	if (context != null)
    		result.addAll(context.getEntityFilterNames(entityType));
    	
    	for (FilterTarget filter : aggr.getIgnoreFilters())
    	{
    		EmisMetaData field = filter.getField(); 
    		if (field == null)
    			continue; 
    		
    		if (field.getType() != EmisDataType.ENUM && field.getType() != EmisDataType.ENUM_SET)
    			continue;

    		if (!NamedUtil.sameName(field.getEntity(), entityType))
    			continue; 
    		
    		if (!NamedUtil.sameName(field.getEnumType(), filter.getEnumType()))
    			continue; 
    		
    		result.add(filter.getField().getName());
    	}
    	
    	return result; 
    }
    
    private Boolean getBooleanFromValues(byte[] values)
    {
    	if (values == null)
    		return null; 
    	
    	return Boolean.valueOf(values.length > 0 && values[0] == 1); 
    }
    
    private EmisEnumSet getEnumSetFromValues(EmisMetaEnum enumType, byte[] values)
    {
        EnumSetImpl result = new EnumSetImpl();
        result.setEnum(enumType);

        if (values == null)
        	result.setAll(); 
        else
		{
        	Set<Byte> valueSet = new HashSet<Byte>();
			for (byte value : values)
				valueSet.add(Byte.valueOf(value));
			result.setAllIndexes(valueSet);
		}
        
    	return result; 
    }
    
    private FilterPanel<BoolFilterEditor> initBoolFilterPanel(EmisAggregatorDef aggr, EmisMetaData field, Boolean value)
    {
    	FilterPanel<BoolFilterEditor> panel = new FilterPanel<BoolFilterEditor>(field, null, new BoolFilterEditor(field));
    	if (aggr != null && aggr.ignoreFilter(field))
    		panel.updateUi(FILTER_STATE.IGNORE_FILTER);
    	else if (value == null)
    	{
    		panel.getEditor().set(null);
    		panel.updateUi(FILTER_STATE.NONE);
    	}
    	else
    	{
    		panel.getEditor().set(value);
    		panel.updateUi(FILTER_STATE.WITH_FILTER);
    	}
    	
    	return panel; 
    }
    
    private FilterPanel<EnumSetEditor> initEnumFilterPanel(EmisAggregatorDef aggr, EmisMetaData field, EmisMetaEnum enumType, EmisEnumSet values)
    {
        FilterPanel<EnumSetEditor> panel = new FilterPanel<EnumSetEditor>(field, enumType, new EnumSetEditor(enumType));
        panel.getEditor().setMetaData(field);

        if (aggr != null && aggr.ignoreFilter(field, enumType))
        	panel.updateUi(FILTER_STATE.IGNORE_FILTER);
        else 
        {
        	if (values != null)
        		panel.getEditor().set(values);

        	if (values == null || values.hasAllValues())
            	panel.updateUi(FILTER_STATE.NONE);
        	else
        		panel.updateUi(FILTER_STATE.WITH_FILTER);
        }
        
        return panel; 
    }

    private void updateEnumEditors(EmisAggregatorDef aggregator)
    {
        this.enumSetEditors = null;

//        if ((aggregator.getContext() == null) || (aggregator.getContext().getEnumFilters() == null) || (aggregator.getContext().getEnumFilters().size() == 0))
//            return;

        EmisMetaData data = aggregator.getMetaData();
        EmisMetaEnumTuple tuple = data == null ? null : data.getArrayDimensions();
        if (tuple == null)
            return;

        EmisMetaEnum[] enums = tuple.getEnums();
        if (enums == null || enums.length == 0)
            return;

        this.enumSetEditors = new FilterPanel[enums.length];
        for (int i = 0; i < enums.length; i++)
            enumSetEditors[i] = initEnumFilterPanel(aggregator, data, enums[i], getEnumFilterValue(aggregator, enums[i])); 
    }

    private EmisEnumSet getEnumFilterValue(EmisAggregatorDef aggregator, EmisMetaEnum enumType)
    {
    	if (aggregator.getContext() == null)
    		return null; 
    	
    	return aggregator.getContext().getEnumFilter(enumType.getName()); 
    }
    
    public void updateUi()
    {
        while (getRowCount() > editorRow)
            removeRow(editorRow);

        EmisMetaEnumTuple enumTuple = null;

        EmisMetaEntity entity = this.aggregator.getEntity();
        EmisMetaData data = this.aggregator.getMetaData();

        if ((data == null) && (entity == null))
            this.uiData.setText(Message.messageAdmin().aeTextSelectDataField());
        else if (data == null && aggregator.getCountDateType() == null)
            this.uiData.setText(Message.messageAdmin().aeTextEntityNameCount(entity.getName()));
        else if (data == null)
            this.uiData.setText(Message.messageAdmin().edpLabelEntityCount(entity.getName(), aggregator.getCountDateType().getName()));
        else
            this.uiData.setText(entity.getName() + " - " + data.getName());

        int row = editorRow; 
        if (data != null)
        {
        	enumTuple = this.aggregator.getMetaData().getArrayDimensions();
        	if (data.getType() == EmisDataType.ENUM || data.getType() == EmisDataType.ENUM_SET)
        	{
            	fieldEnumFilter = initEnumFilterPanel(null, data, data.getEnumType(), null); 
            	getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
                setHTML(row, 0, EmisToolbox.span(EmisToolbox.CSS_SUBSECTION, "Value (" + data.getEnumType().getName() + "):"));
                setWidget(row, 1, fieldEnumFilter);
        	}
        	else
        		fieldEnumFilter = null; 
        	
        	row = initEnumEditors(data, enumTuple, row);
        	row = initEntityFilters(data.getEntity(), row);
        } 
        else
        	row = initEntityFilters(entity, row);

        if (!weightUi)
        	uiWeighted.setValue(weightEditor.get() != null);
    }

    private int initEntityFilters(EmisMetaEntity entity, int row)
    {
        if (entity == null)
            return row;

        List<FilterPanel<BoolFilterEditor>> boolEditors = new ArrayList<FilterPanel<BoolFilterEditor>>();
        List<FilterPanel<EnumSetEditor>> enumEditors = new ArrayList<FilterPanel<EnumSetEditor>>();

        boolean firstFilter = true;
        for (EmisMetaData field : entity.getData())
        {
            if (field.getType() != EmisMetaData.EmisDataType.BOOLEAN)
                continue;
            
            FilterPanel<BoolFilterEditor> panel = findBoolFilterPanel(field, this.entityBoolFilterEditors);
            if (true)
            {
                if (firstFilter && (row > editorRow))
                {
                    setHTML(row, 1, "<hr>");
                    row++;
                }

            	if (panel == null)
            		panel = initBoolFilterPanel(null, field, null); 
            
            	getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
	            setHTML(row, 0, EmisToolbox.span(EmisToolbox.CSS_SUBSECTION, entity.getName() + "." + field.getName()));
	            setWidget(row, 1, panel);
	            row++;
	
	            boolEditors.add(panel);
            }
            // TODO - remember that this filter is available. 

            firstFilter = false;
        }

        firstFilter = true;
        for (EmisMetaData field : entity.getData())
        {
            if (field.getType() != EmisMetaData.EmisDataType.ENUM && field.getType() != EmisMetaData.EmisDataType.ENUM_SET)
                continue;

            FilterPanel<EnumSetEditor> panel = findFilterPanel(field, this.entityEnumFilterEditors);
            if (panel == null && field.getEnumType() != null)
            	panel = initEnumFilterPanel(null, field, field.getEnumType(), null);

            if (panel == null)
                continue;

            if (firstFilter && (row > editorRow))
            {
                setHTML(row, 1, "<hr>");
                row++;
            }

            firstFilter = false;

            getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
            setHTML(row, 0, EmisToolbox.span(EmisToolbox.CSS_SUBSECTION, entity.getName() + "." + field.getName()));
            setWidget(row, 1, panel); 
            row++;

            enumEditors.add(panel);
        }

        this.entityBoolFilterEditors = ((FilterPanel<BoolFilterEditor>[]) boolEditors.toArray(new FilterPanel[0]));
        this.entityEnumFilterEditors = ((FilterPanel<EnumSetEditor>[]) enumEditors.toArray(new FilterPanel[0]));

        return row;
    }

    private FilterPanel<BoolFilterEditor> findBoolFilterPanel(EmisMetaData field, FilterPanel<BoolFilterEditor>[] panels)
    {
        if (panels == null)
            return null;

        for (FilterPanel<BoolFilterEditor> panel : panels)
        {
            if (field.equals(panel.getEditor().getMetaData()))
                return panel;
        }
        
        return null;
    }

    private FilterPanel<EnumSetEditor> findFilterPanel(EmisMetaData field, FilterPanel<EnumSetEditor>[] panels)
    {
        if (panels == null)
            return null;

        for (FilterPanel<EnumSetEditor> panel : panels)
        {
            if (field.equals(panel.getEditor().getMetaData()))
                return panel;
        }

        return null;
    }

    private int initEnumEditors(EmisMetaData field, EmisMetaEnumTuple enumTuple, int row)
    {
        if (enumTuple == null)
            return row;

        EmisMetaEnum[] enums = enumTuple.getEnums();
        FilterPanel<EnumSetEditor>[] oldEnumSetEditors = this.enumSetEditors;
        this.enumSetEditors = new FilterPanel[enums.length];

        for (int i = 0; i < enums.length; i++)
        {
        	enumSetEditors[i] = findFilterPanel(enums[i], oldEnumSetEditors);
            if (enumSetEditors[i] == null)
            	enumSetEditors[i] = initEnumFilterPanel(null, field, enums[i], null); 

            getCellFormatter().setVerticalAlignment(row + i, 0, HasVerticalAlignment.ALIGN_TOP);
            setHTML(row + i, 0, EmisToolbox.span(EmisToolbox.CSS_SUBSECTION, field.getName() + "[" + enums[i].getName() + "]"));
            setWidget(row + i, 1, enumSetEditors[i]);
        }

        return row + enums.length;
    }

    private FilterPanel<EnumSetEditor> findFilterPanel(EmisMetaEnum enumType, FilterPanel<EnumSetEditor>[] editors)
    {
        if (editors == null)
            return null;

        for (FilterPanel<EnumSetEditor> panel : editors)
        {
            if (panel == null)
                continue;

            EmisEnumSet value = panel.getEditor().get();
            if (NamedUtil.sameName(enumType, value.getEnum()))
                return panel;
        }
        return null;
    }
}
