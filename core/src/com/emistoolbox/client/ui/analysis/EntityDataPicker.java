package com.emistoolbox.client.ui.analysis;

import com.emistoolbox.client.Message;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityDataPicker extends FlexTable implements HasSelectionHandlers<EmisAggregatorDef>
{
    private EmisMeta meta;
    private EmisAggregatorDef aggregator;

    public EntityDataPicker(EmisMeta meta, EmisAggregatorDef aggregator) 
    {
        setCellPadding(3);
        getColumnFormatter().setWidth(1, "20px");

        this.aggregator = aggregator;
        this.meta = meta;

        int row = 0;
        Label selectedLabel = null;
        for (final EmisMetaEntity entity : getEntityList(meta))
        {
            final Label l = new Label(entity.getName());
            l.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                { EntityDataPicker.this.select(entity, l, null); }
            });
            setWidget(row, 0, l);
            row++;

            if (NamedUtil.sameName(entity, aggregator.getEntity()))
            {
                selectedLabel = l;
            }
        }
        getFlexCellFormatter().setRowSpan(0, 2, row);
        setWidget(0, 2, new Label(""));
        setWidget(0, 1, new Label(""));

        if (selectedLabel != null)
            select(aggregator.getEntity(), selectedLabel, aggregator.getMetaData());
    }
    
    private void select(final EmisMetaEntity entity, Label label, EmisMetaData selectedData)
    {
        for (int i = 0; i < getRowCount(); i++)
        {
            Widget w = getWidget(i, 0);
            if (w == label)
                label.addStyleName("selected");
            else
                label.removeStyleName("selected");
        }

        VerticalPanel vp = new VerticalPanel();

        for (Label l : getEntityFields(this, aggregator, entity))
        	vp.add(l); 

        for (final EmisMetaData data : NamedUtil.sort(entity.getData()))
        {
            if (data.getType() == EmisMetaData.EmisDataType.STRING)
            	continue; 
            
            Label l = null; 
            if (data.getType() == EmisMetaData.EmisDataType.ENUM)
                l = new Label(data.getName() + " (enum)"); 
            else if (data.getType() == EmisMetaData.EmisDataType.ENUM_SET)
                l = new Label(data.getName() + " (enum set)"); 
            else 
            	l = new Label(data.getName());

            l.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    EntityDataPicker.this.aggregator.setMetaData(data);
                    SelectionEvent.fire(EntityDataPicker.this, EntityDataPicker.this.aggregator);
                }
            });
            if (NamedUtil.sameName(selectedData, data))
            {
                l.addStyleName("selected");
            }
            vp.add(l);
        }

        setWidget(0, 2, vp);
    }
    
    private static Label getEntityCountLabel(final HasSelectionHandlers<EmisAggregatorDef> widget, final EmisAggregatorDef aggregator, final EmisMetaEntity entity, final EmisMetaDateEnum dateEnum)
    {
        Label l = new Label(Message.messageAdmin().edpLabelEntityCount(entity.getName(), dateEnum.getName()));
        l.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                aggregator.setEntity(entity);
                aggregator.setCountDateType(dateEnum);
                SelectionEvent.fire(widget, aggregator);
            }
        });
        
        return l; 
    }

    private static Label getEntityFieldLabel(final HasSelectionHandlers<EmisAggregatorDef> widget, final EmisAggregatorDef aggr, final EmisMetaData field)
    {
    	if (field.getType() == EmisMetaData.EmisDataType.STRING)
    		return null; 
            
    	Label result = null; 
    	if (field.getType() == EmisMetaData.EmisDataType.ENUM)
    		result = new Label(field.getName() + " (enum)"); 
    	else if (field.getType() == EmisMetaData.EmisDataType.ENUM_SET)
    		result = new Label(field.getName() + " (enum set)"); 
    	else 
    		result = new Label(field.getName());

        result.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event)
            {
                aggr.setMetaData(field);
                SelectionEvent.fire(widget, aggr);
            }
        });

    	return result; 
    }
    
    public static List<Label> getEntityFields(HasSelectionHandlers<EmisAggregatorDef> widget, EmisAggregatorDef aggr, EmisMetaEntity entity)
    {
    	List<Label> result = new ArrayList<Label>();
    	
        for (EmisMetaDateEnum dateEnum : entity.getUsedDateTypes())
            result.add(getEntityCountLabel(widget, aggr, entity, dateEnum));

        for (final EmisMetaData field : NamedUtil.sort(entity.getData()))
        {
        	Label lbl = getEntityFieldLabel(widget, aggr, field); 
        	if (lbl != null)
        		result.add(lbl);
        }
        
        return result; 
    }
    

    public static List<EmisMetaEntity> getEntityList(EmisMeta meta)
    {
        Set<EmisMetaEntity> set = new HashSet<EmisMetaEntity>();
        for (EmisMetaHierarchy h : meta.getHierarchies())
            set.addAll(h.getEntityOrder());

        List<EmisMetaEntity> result = new ArrayList<EmisMetaEntity>();
        result.addAll(set);

        return result;
    }

    public HandlerRegistration addSelectionHandler(SelectionHandler<EmisAggregatorDef> handler)
    {
        return addHandler(handler, SelectionEvent.getType());
    }
}
