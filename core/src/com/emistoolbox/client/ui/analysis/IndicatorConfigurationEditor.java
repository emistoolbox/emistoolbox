package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.impl.IndicatorRatio;
import com.emistoolbox.common.model.analysis.impl.IndicatorSimple;
import com.emistoolbox.common.model.analysis.impl.IndicatorTimeRatio;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IndicatorConfigurationEditor extends FlexTable implements EmisEditor<List<EmisIndicator>>
{
    private ListBoxWithUserObjects<EmisIndicator> uiList = new ListBoxWithUserObjects();
    private EmisMeta emisMeta;

    public IndicatorConfigurationEditor(EmisMeta emisMeta) 
    {
        this.emisMeta = emisMeta;

        this.uiList.setVisibleItemCount(40);

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(2);

        PushButton btn = new PushButton(Message.messageAdmin().iceBtnAddRatio(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                EmisIndicator indicator = new IndicatorRatio();
                indicator.setName(Message.messageAdmin().iceIndicatorNewRatio());
                int index = uiList.add(null, indicator.getName(), indicator);
                uiList.setSelectedIndex(index);
                show(indicator);
            }
        });
        EmisUtils.init(btn, 80);
        buttons.add(btn);

        btn = new PushButton(Message.messageAdmin().iceBtnAddTimeRatio(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                EmisIndicator indicator = new IndicatorTimeRatio();
                indicator.setName(Message.messageAdmin().iceIndicatorNewTimeRatio());
                int index = uiList.add(null, indicator.getName(), indicator);
                uiList.setSelectedIndex(index);
                show(indicator);
            }
        });
        EmisUtils.init(btn, 100);
        buttons.add(btn);

        btn = new PushButton(Message.messageAdmin().iceBtnAddCount(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                EmisIndicator indicator = new IndicatorSimple();
                indicator.setName(Message.messageAdmin().iceIndicatorNewCount());
                int index = uiList.add(null, indicator.getName(), indicator);
                uiList.setSelectedIndex(index);
                show(indicator);
            }
        });
        EmisUtils.init(btn, 80);
        buttons.add(btn);

        btn = new PushButton(Message.messageAdmin().iceBtnDelete(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                if (IndicatorConfigurationEditor.this.uiList.getSelectedIndex() != -1)
                    IndicatorConfigurationEditor.this.uiList.removeItem(IndicatorConfigurationEditor.this.uiList.getSelectedIndex());
                IndicatorConfigurationEditor.this.setWidget(2, 1, new Label(""));
            }
        });
        EmisUtils.init(btn, 80);
        buttons.add(btn);

        getFlexCellFormatter().setColSpan(1, 0, 2);
        setWidget(1, 0, buttons);
        this.uiList.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                IndicatorConfigurationEditor.this.show((EmisIndicator) IndicatorConfigurationEditor.this.uiList.getUserObject());
            }
        });
        CellFormatter formatter = getCellFormatter(); 
        formatter.setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        setWidget(2, 0, EmisToolbox.metaResultEditFrame(uiList));

        HorizontalPanel upDownButtons = new HorizontalPanel(); 
        btn = new PushButton("Up", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ uiList.moveUp(); } 
        }); 
        EmisUtils.init(btn, 40); 
        upDownButtons.add(btn);
        
        btn = new PushButton("Down", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ uiList.moveDown(); } 
        }); 
        EmisUtils.init(btn, 40); 
        upDownButtons.add(btn);

        setWidget(3, 0, upDownButtons); 
        
        formatter.setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);
    }

    public void show(EmisIndicator newIndicator)
    {
        commit();

        if (newIndicator == null)
            setWidget(2, 1, new Label(""));
        else
        {
            IndicatorEditor editor = new IndicatorEditor(this.emisMeta);
            editor.set(newIndicator);
            setWidget(2, 1, editor);
        }
    }

    public void commit()
    {
        Widget w = null;
        if ((getRowCount() > 2) && (getCellCount(2) > 1))
            w = getWidget(2, 1);

        if ((w instanceof IndicatorEditor))
        {
            EmisIndicator indicator = ((IndicatorEditor) w).get();
            for (int i = 0; i < uiList.getUserObjectCount(); i++)
            {
                if (this.uiList.getUserObject(i) != indicator)
                    continue;

                uiList.update(indicator.getGroupName(), indicator.getName(), indicator); 

                return;
            }
            
            this.uiList.add(indicator.getGroupName(), indicator.getName(), indicator);
        }
    }
    
    public static String getUiName(EmisIndicator indicator)
    {
    	if (indicator.getGroupName() == null || indicator.getGroupName().equals(""))
    		return indicator.getName();
    	
    	return indicator.getGroupName() + ": " + indicator.getName(); 
    }
    

    public List<EmisIndicator> get()
    {
        commit();

        List<EmisIndicator> result = new ArrayList<EmisIndicator>();
        for (int i = 0; i < this.uiList.getUserObjectCount(); i++)
            result.add(this.uiList.getUserObject(i));

        return result;
    }

    public void set(List<EmisIndicator> indicators)
    {
        this.uiList.clear();
        addAllIndicators(uiList, indicators); 
        
        if (indicators.size() > 0)
            show((EmisIndicator) indicators.get(0));
    }

    public static void addAllIndicators(ListBoxWithUserObjects<EmisIndicator> ui, List<EmisIndicator> indicators)
    {
    	sortIndicators(indicators); 
    	for (EmisIndicator indicator : indicators)
    		ui.add(indicator.getGroupName(), indicator.getName(), indicator); 
    }
    
    private static void sortIndicators(List<EmisIndicator> indicators)
    {
    	Collections.sort(indicators, new Comparator<EmisIndicator>() {
			@Override
			public int compare(EmisIndicator indicator1, EmisIndicator indicator2) 
			{
				String group1 = indicator1.getGroupName(); 
				if (group1 == null)
					group1 = ""; 
				
				String group2 = indicator2.getGroupName(); 
				if (group2 == null)
					group2 = ""; 
				
				int groupResult = group1.compareTo(group2); 
				return groupResult != 0 ? groupResult : indicator1.getName().compareTo(indicator2.getName()); 
			}
    	});
    }
    
    
}
