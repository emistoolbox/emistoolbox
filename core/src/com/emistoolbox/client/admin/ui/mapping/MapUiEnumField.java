package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.mapping.MapUiField.FIELD_TYPES;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowConstAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.Map;

public class MapUiEnumField extends MapUiWidgetBase implements HasValueChangeHandlers<MapUiField.FIELD_TYPES>
{
    private ListBox uiType = new ListBox();
    private TextBox uiOffset = new TextBox();
    private DbRowEnumAccess access;
    private MapUiWidget fieldWidget;
    private boolean withByColumnType; 
    private boolean asSet; 
    private EmisMetaData field; 
    private boolean withFieldWidget = true; 
    
    public MapUiEnumField(EmisMetaData metaField, EmisToolboxServiceAsync service, boolean asSet, String dataset) 
    {
        super(service, dataset);
        this.field = metaField; 
        this.asSet = asSet; 

        this.uiType.addItem(Message.messageAdmin().muefItemEnumIndexes());
        this.uiType.addItem(Message.messageAdmin().muefItemEnumValues());
        this.uiType.addItem(Message.messageAdmin().muefItemMappedEnumValues());
        
        this.uiType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { MapUiEnumField.this.updateUi(); }
        });
        this.uiOffset.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { MapUiEnumField.this.updateUi(); }
        });
        getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);

        setWidget(1, 1, this.uiType);

        getFlexCellFormatter().setColSpan(0, 1, 3);
        getFlexCellFormatter().setColSpan(2, 1, 3);

        updateUi();
    }

    public void setWithFieldWidget(boolean withFieldWidget)
    { this.withFieldWidget = withFieldWidget; } 
    
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<FIELD_TYPES> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }

    private int getOffset()
    {
    	if (asSet)
    		return 0;  
    				
        try
        { return Integer.parseInt(this.uiOffset.getText()); }
        catch (Throwable err)
        {}
        
        return 0;
    }

    private String getIndexText()
    {
        if (this.access == null)
            return "";

        int offset = getOffset();

        StringBuffer result = new StringBuffer();
        String[] values = this.access.getEnumType().getValues();
        for (int i = 0; i < values.length; i++)
        {
            if (i != 0)
                result.append(", ");

            if (asSet)
            	result.append("" + (1 << i)); 
            else
            	result.append("" + (i + offset));
            result.append("->");
            result.append(values[i]);
        }

        return result.toString();
    }

    private String getValueText()
    {
        if (this.access == null)
        {
            return "";
        }
        StringBuffer result = new StringBuffer();
        String[] values = this.access.getEnumType().getValues();
        for (int i = 0; i < values.length; i++)
        {
            if (i != 0)
            {
                result.append(", ");
            }
            result.append(values[i]);
        }

        return result.toString();
    }

    private String getValueText(Map<String, String> maps)
    {
        if (this.access == null)
            return "";

        if (maps == null)
        {
            return getValueText();
        }
        StringBuffer result = new StringBuffer();
        for (String key : maps.keySet())
        {
            if (result.length() > 0)
                result.append(", ");
            result.append(key);
            result.append("->");
            result.append((String) maps.get(key));
        }

        return result.toString();
    }

    public DbRowAccess get()
    {
        commit();
        return this.access;
    }

    public void getValuePreview(AsyncCallback<List<String>> callback)
    {
    }

    public void addByColumnType()
    { 
        if (fieldWidget instanceof MapUiField)
            ((MapUiField) this.fieldWidget).addByColumnType(); 
        
        withByColumnType = true; 
    }
    
    public boolean isSelectedByColumnType()
    { return (fieldWidget instanceof MapUiField) && ((MapUiField) fieldWidget).isSelectedByColumnType(); }

    public void set(DbRowAccess newAccess)
    {
        if (!(newAccess instanceof DbRowEnumAccess))
            return;

        this.access = ((DbRowEnumAccess) newAccess);
        if (access != null && withFieldWidget)
        {
        	this.fieldWidget = MapUiUtil.getMapUi(this.access.getAccess(), false, field, getService(), getDataset());
        	this.fieldWidget.setConstraint(new MapUiEnumConstraint(this.access.getEnumType()));
        	
	        if (this.fieldWidget instanceof MapUiField)
	        {
	            ((MapUiField) this.fieldWidget).addValueChangeHandler(new ValueChangeHandler<FIELD_TYPES>() {
	                public void onValueChange(ValueChangeEvent<FIELD_TYPES> event)
	                {
	                    boolean showEnumUi = (event.getValue() != null) && (event.getValue() != MapUiField.FIELD_TYPES.FIELD_TYPE_CONST) && (event.getValue() != MapUiField.FIELD_TYPES.FIELD_TYPE_NONE) && (event.getValue() != MapUiField.FIELD_TYPES.FIELD_TYPE_BYCOLUMN);
	                    MapUiEnumField.this.show(showEnumUi);
	
	                    // Pass on event. 
	                    ValueChangeEvent.fire(MapUiEnumField.this, event.getValue());
	                }
	            });
	        }
	        
	        if (withByColumnType)
	            ((MapUiField) fieldWidget).addByColumnType(); 
	        
	        setWidget(0, 1, (Widget) this.fieldWidget);
        }
        else
        {
        	fieldWidget = null; 
        	setWidget(0, 1, null); 
        }
        
        if (!this.access.getAsValue())
        {
            this.uiType.setSelectedIndex(0);
            this.uiOffset.setText("" + this.access.getOffset());
        }
        else
            this.uiType.setSelectedIndex(this.access.getMap() == null ? 1 : 2);

        updateUi();
    }

    private void show(boolean showEnumUi)
    {
        this.uiType.setVisible(showEnumUi);
        if (showEnumUi)
        {
            updateUi();
        }
        else
        {
            clearCell(2, 1);
            clearCell(1, 2);
            clearCell(1, 3);
        }
    }

    private void commit()
    {
        DbRowAccess internalAccess = MapUiUtil.getAccess(getWidget(0, 1));
        if (internalAccess != null && withFieldWidget)
        	this.access.setAccess(internalAccess);
        
        if ((internalAccess instanceof DbRowConstAccess))
        {
            this.access.setAsValue(true);
            this.access.setMap(null);
        }
        else
        {
        	if (access == null)
        		access = new DbRowEnumAccess(null, field.getEnumType());
        	
            this.access.setAsValue(this.uiType.getSelectedIndex() > 0);
            if (!this.access.getAsValue())
                this.access.setOffset(getOffset());
            else
                this.access.setMap(getMapFromUi());
        }
    }

    private Map<String, String> getMapFromUi()
    {
        if (this.uiType.getSelectedIndex() == 2)
        {
            Widget w = getWidget(1, 3);
            if ((w instanceof EnumMapEditor))
                return ((EnumMapEditor) w).get();
        }
        return null;
    }

    private void updateUi()
    {
        if (!this.uiType.isVisible())
            return;

        int index = this.uiType.getSelectedIndex();
        if (index == 0)
        {
            setText(2, 1, getIndexText());
            if (asSet)
            {
            	setText(1, 2, "");
            	setText(1, 3, ""); 
            }
            else
            {
            	setHTML(1, 2, Message.messageAdmin().muefWithOffset() + " ");
            	setWidget(1, 3, this.uiOffset);
            }
        }
        else
        {
            setText(2, 1, getValueText(getMapFromUi()));
            setText(1, 2, "");
            setText(1, 3, "");
        }

        if (index == 2)
        {
            setText(2, 1, "");
            setText(1, 2, "");
            EnumMapEditor editor = new EnumMapEditor(this.access.getEnumType());
            editor.set(this.access.getMap());
            setWidget(1, 3, editor);
        }
    }

    public void updateDbMetaInfo()
    {
    	if (fieldWidget != null)	
    		this.fieldWidget.updateDbMetaInfo();
    }

    public void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo)
    {
    	if (fieldWidget != null)
    		this.fieldWidget.updateDbMetaInfo(dbContext, dbMetaInfo);
    }
}
