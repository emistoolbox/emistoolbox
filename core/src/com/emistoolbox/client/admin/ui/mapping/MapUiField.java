package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowConstAccess;
import com.emistoolbox.common.model.mapping.DbRowContextAccess;
import com.emistoolbox.common.model.mapping.DbRowFieldAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowConstAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowContextAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowFieldAccessImpl;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUiField extends MapUiWidgetBase implements HasValueChangeHandlers<MapUiField.FIELD_TYPES>
{
    private DbRowAccess access;
    private ListBox uiType = new ListBox();
    private Label uiLabel = new Label();
    private Widget uiWidget;
    private ListBox uiFieldList = new ListBox();
    private String fieldValue;
    private FIELD_TYPES currentType = FIELD_TYPES.FIELD_TYPE_NONE;
    private String dataset; 
    private boolean fieldOnly = false; 
    
    public MapUiField(EmisToolboxServiceAsync service, String dataset) 
    { this(service, dataset, false); } 
    
    public MapUiField(EmisToolboxServiceAsync service, String dataset, boolean fieldOnly) 
    {
        super(service, dataset);
        
        this.fieldOnly = fieldOnly; 
        this.dataset = dataset; 

        this.uiType.addItem("", "" + FIELD_TYPES.FIELD_TYPE_NONE);
        this.uiType.addItem(Message.messageAdmin().mapuifInfoValueIsConstant(), "" + FIELD_TYPES.FIELD_TYPE_CONST);
        this.uiType.addItem(Message.messageAdmin().mapuifInfoValueInField(), "" + FIELD_TYPES.FIELD_TYPE_FIELD);
        this.uiType.addItem(Message.messageAdmin().mapuifInfoValueInVariable(), "" + FIELD_TYPES.FIELD_TYPE_CONTEXT);
        
        this.uiType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                if (MapUiField.this.uiType.getSelectedIndex() == -1)
                {
                    MapUiField.this.fireFieldTypeHandlers();
                    return;
                }

                MapUiField.FIELD_TYPES fieldType = MapUiField.FIELD_TYPES.valueOf(MapUiField.this.uiType.getValue(MapUiField.this.uiType.getSelectedIndex()));
                updateFieldType(fieldType); 
            }
        });
        
        setHTML(0, 0, Message.messageAdmin().mapuifHtmlSourceType());
        

        if (fieldOnly)
        {
        	setHTML(0, 1, Message.messageAdmin().mapuifInfoValueInField());
            updateFieldType(FIELD_TYPES.FIELD_TYPE_FIELD); 
    	}
        else
        {
        	setWidget(0, 1, this.uiType);
        	set(null);
        }
        
        setWidget(1, 0, this.uiLabel);
    }

    private void updateFieldType(MapUiField.FIELD_TYPES fieldType)
    {
        switch (fieldType) {
        case FIELD_TYPE_CONST:
            if ((MapUiField.this.access instanceof DbRowConstAccess))
                break;
            MapUiField.this.set(new DbRowConstAccessImpl());
            break;
        case FIELD_TYPE_FIELD:
            if ((MapUiField.this.access instanceof DbRowFieldAccess))
                break;
            MapUiField.this.set(new DbRowFieldAccessImpl());
            break;
        case FIELD_TYPE_CONTEXT:
            if ((MapUiField.this.access instanceof DbRowContextAccess))
                break;
            MapUiField.this.set(new DbRowContextAccessImpl());
            break;
        case FIELD_TYPE_BYCOLUMN: 
            MapUiField.this.set(new DbRowByColumnIndexAccess());
            break; 
        default:
            MapUiField.this.set(null);
        }
    }
    
    public void addByColumnType()
    {
        if (uiType.getItemCount() < 5)
            uiType.addItem("By Column", "" + FIELD_TYPES.FIELD_TYPE_BYCOLUMN); 
    }

    public boolean isSelectedByColumnType()
    { return access instanceof DbRowByColumnIndexAccess; }
    
    public void set(DbRowAccess access)
    { setInternal(access); }

    public void setInternal(DbRowAccess access)
    {
        this.access = access;

        if (access == null)
            displayUi();
        else if ((access instanceof DbRowConstAccess))
            displayUi((DbRowConstAccess) access);
        else if ((access instanceof DbRowFieldAccess))
            displayUi((DbRowFieldAccess) access);
        else if ((access instanceof DbRowContextAccess))
            displayUi((DbRowContextAccess) access);
        else if ((access instanceof DbRowByColumnIndexAccess))
            displayUi((DbRowByColumnIndexAccess) access); 
    }

    public void setDbContext(DbContext dbContext)
    {
        super.setDbContext(dbContext);
        this.fieldValue = GwtUtils.getListValue(this.uiFieldList);

        this.uiFieldList.clear();
        this.uiFieldList.addItem(Message.messageAdmin().mapuifInfoWaitingForFieldNames(), null);
    }

    private void displayUi()
    {
        if (fieldOnly)
        {
        	setWidget(FIELD_TYPES.FIELD_TYPE_FIELD);
            uiLabel.setText("");
        }
        else
        {
        	setWidget(FIELD_TYPES.FIELD_TYPE_NONE);
            uiLabel.setText(Message.messageAdmin().mapuifLabelNone());
        }
    }

    private void displayUi(DbRowConstAccess access)
    {
        this.uiLabel.setText(Message.messageAdmin().mapuifLabelConstantValue());
        setWidget(FIELD_TYPES.FIELD_TYPE_CONST);
        setValue(access.getConstValue());
    }

    private void displayUi(DbRowFieldAccess access)
    {
        this.uiLabel.setText(Message.messageAdmin().mapuifLabelFieldName());
        setWidget(FIELD_TYPES.FIELD_TYPE_FIELD);
        this.fieldValue = access.getFieldName();
        setValue(this.fieldValue);
    }

    private void displayUi(DbRowContextAccess access)
    {
        this.uiLabel.setText(Message.messageAdmin().mapuifLabelContextVariable());
        setWidget(FIELD_TYPES.FIELD_TYPE_CONTEXT);
        setValue(access.getContextParameter());
    }
    
    private void displayUi(DbRowByColumnIndexAccess access)
    {
        this.uiLabel.setText("By Column"); // jb2do - appropriate label

        addByColumnType(); 
        setWidget(FIELD_TYPES.FIELD_TYPE_BYCOLUMN);
    }
    

    private void commit()
    {
        if (this.access == null)
            return;

        if ((this.access instanceof DbRowConstAccess))
            ((DbRowConstAccess) this.access).setConstValue(getValue());
        else if ((this.access instanceof DbRowFieldAccess))
            ((DbRowFieldAccess) this.access).setFieldName(getValue());
        else if ((this.access instanceof DbRowContextAccess))
            ((DbRowContextAccess) this.access).setContextParameter(getValue());
    }

    public DbRowAccess get()
    {
        commit();
        return this.access;
    }

    protected String getValue()
    {
        if ((this.uiWidget instanceof TextBox))
            return ((TextBox) this.uiWidget).getText();
        if ((this.uiWidget instanceof ListBox))
            return GwtUtils.getListValue((ListBox) this.uiWidget);

        return null;
    }

    protected void setValue(String value)
    {
        if ((this.uiWidget instanceof TextBox))
            ((TextBox) this.uiWidget).setText(value);
        else if ((this.uiWidget instanceof ListBox))
            GwtUtils.setListValueWithAdd((ListBox) this.uiWidget, value);
    }

    protected void setWidget(FIELD_TYPES type)
    {
        this.currentType = type;

        GwtUtils.setListValue(this.uiType, "" + type);
        switch (type) {
        case FIELD_TYPE_CONST:
            if ((getConstraint() != null) && (getConstraint().getValues() != null))
            {
                ListBox lb = new ListBox();
                for (String value : getConstraint().getValues())
                    lb.addItem(value);
                this.uiWidget = lb;
            }
            else
            {
                this.uiWidget = new TextBox();
            }
            break;
        case FIELD_TYPE_FIELD:
            this.uiWidget = this.uiFieldList;
            break;
        case FIELD_TYPE_CONTEXT:
            this.uiWidget = new TextBox();
            break;
        default:
            this.uiWidget = new Label();
        }

        setWidget(1, 1, this.uiWidget);

        fireFieldTypeHandlers();
    }

    public void getValuePreview(AsyncCallback<List<String>> callback)
    {
        List<String> result = new ArrayList<String>();

        switch (currentType) {
        case FIELD_TYPE_CONST:
            result.add(getValue());
            callback.onSuccess(result);
            break;
        case FIELD_TYPE_FIELD:
            getService().getValues(getDbContext(), dataset, getValue(), callback);
            break;
        case FIELD_TYPE_CONTEXT:
            callback.onSuccess(null);
            break;
        default:
            callback.onSuccess(null);
        }
    }

    public void updateDbMetaInfo()
    {}

    public void updateDbMetaInfo(DbContext context, Map<String, List<String>> dbMetaInfo)
    {
        if (context == null)
            return;
        setDbContext(context);

        this.uiFieldList.clear();

        List<String> fields = dbMetaInfo == null ? null : dbMetaInfo.get(getDbContext().getQuery());
        if (fields == null)
            this.uiFieldList.addItem(Message.messageAdmin().mapuifInfoWaitingForFieldNames(), null);
        else
        {
            for (String field : fields)
                this.uiFieldList.addItem(field);
        }
        if (this.fieldValue != null)
            GwtUtils.setListValueWithAdd(this.uiFieldList, this.fieldValue);
    }

    private void fireFieldTypeHandlers()
    {
    	FIELD_TYPES value; 
    	if (fieldOnly)
    		value = FIELD_TYPES.FIELD_TYPE_FIELD; 
    	else
            value = this.uiType.getSelectedIndex() == -1 ? null : FIELD_TYPES.valueOf(this.uiType.getValue(this.uiType.getSelectedIndex()));

    	ValueChangeEvent.fire(this, value);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<FIELD_TYPES> handler)
    {
        HandlerRegistration result = addHandler(handler, ValueChangeEvent.getType());
        fireFieldTypeHandlers();
        return result;
    }

    public static enum FIELD_TYPES {
        FIELD_TYPE_NONE, FIELD_TYPE_CONST, FIELD_TYPE_FIELD, FIELD_TYPE_CONTEXT, FIELD_TYPE_BYCOLUMN;
    }
}
