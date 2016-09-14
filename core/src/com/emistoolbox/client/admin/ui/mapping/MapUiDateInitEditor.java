package com.emistoolbox.client.admin.ui.mapping;

import java.util.List;
import java.util.Map;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.EmisDateInitDbMap;
import com.emistoolbox.common.model.mapping.impl.DbRowFieldAccessImpl;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

public class MapUiDateInitEditor extends MapUiEditorBase<EmisDateInitDbMap> 
{
	private DbMapEditor mapUi; 
	
	private MapUiDbContextEditor uiDbContext; 
	private MapUiField uiAccess; 

	public MapUiDateInitEditor(DbMapEditor mapUi, EmisToolboxServiceAsync service, String dataset) 
    {
    	super(service, dataset); 

    	this.mapUi = mapUi;
    	
    	int row = 0; 
        setHTML(row, 0, Message.messageAdmin().mueeHtmlDataSource() + ":");
        EmisToolbox.css(this, row, 0, "sectionBlue");

        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(row, 0, Message.messageAdmin().mueeHtmlDataSource() + ":");
        EmisToolbox.css(this, row, 0, "sectionBlue");
        
        uiDbContext = new MapUiDbContextEditor(mapUi, null);
        uiDbContext.setDataSourceConfigs(mapUi.get().getDataSources());

        uiDbContext.addValueChangeHandler(new ValueChangeHandler<DbContext>() {
            public void onValueChange(ValueChangeEvent<DbContext> event)
            { onChangedDbContext(event.getValue()); }
        });

    	setWidget(row, 1, uiDbContext); 
    	row++; 
    	
        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(row, 0, Message.messageAdmin().mueeHtmlDateAccess() + ":");
        EmisToolbox.css(this, row, 0, "sectionBlue");

        uiAccess = new MapUiField(service, dataset, true); 

        setWidget(row, 1, uiAccess); 
    } 
    
	private void onChangedDbContext(DbContext value)
	{
		uiDbContext.updateDbMetaInfo();
		uiAccess.setDbContext(value);
	    uiAccess.updateDbMetaInfo(value, mapUi.getDbMetaInfo(value == null ? null : value.getDataSource())); 
	}
	
	@Override
	public void set(EmisDateInitDbMap data) 
	{
		super.set(data);
		uiDbContext.set(data.getDbContext());
		onChangedDbContext(data.getDbContext());
		if (data.getValueAccess() == null)
			uiAccess.set(new DbRowFieldAccessImpl());
		else
			uiAccess.set(data.getValueAccess());
	}

	@Override
	public void commit() 
	{ get(); }

	@Override
	public EmisDateInitDbMap get() 
	{
		EmisDateInitDbMap result = super.get();
		result.setDbContext(uiDbContext.get()); 
		result.setValueAccess(uiAccess.get()); 
		
		return result; 
	}
}
