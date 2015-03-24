package com.emistoolbox.client.ui.analysis;

import java.util.List;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabbedEntityDataPicker extends VerticalPanel implements HasSelectionHandlers<EmisAggregatorDef>
{
	private TabPanel tabs = new TabPanel(); 

	public TabbedEntityDataPicker(EmisMeta meta, EmisAggregatorDef aggregator) 
    {
		setWidth("625px"); 
		setSpacing(4);
		
		HTML title = new HTML("Select Field");
		title.addStyleName("title");
		add(title); 
		
		add(new Label("Please select the location and then a field from the location.")); 

		DeckPanel p = tabs.getDeckPanel();
		p.setSize("600px", "400px");
		
		add(tabs); 
		for (EmisMetaEntity entityType : EntityDataPicker.getEntityList(meta))
    		tabs.add(getFieldListUi(entityType, aggregator), entityType.getName());
		
		tabs.selectTab(getWidgetCount() - 1);
    }
    
    private Widget getFieldListUi(EmisMetaEntity entityType, EmisAggregatorDef aggr)
    {
    	ScrollPanel scroll = new ScrollPanel();
    	scroll.setAlwaysShowScrollBars(false);
    	scroll.setSize("580px", "400px");
    	FlexTable grid = new FlexTable(); 
    	grid.setCellPadding(3);
    	scroll.setWidget(grid);
    	
    	List<Label> labels = EntityDataPicker.getEntityFields(this, aggr, entityType);
    	for (int i = 0; i < labels.size(); i++)
    		grid.setWidget(i / 4, i % 4, labels.get(i)); 
    	
        return scroll; 
    }

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<EmisAggregatorDef> handler) 
	{ return addHandler(handler, SelectionEvent.getType()); }
}
