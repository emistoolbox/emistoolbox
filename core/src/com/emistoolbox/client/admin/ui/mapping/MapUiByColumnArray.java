package com.emistoolbox.client.admin.ui.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.mapping.MapUiField.FIELD_TYPES;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowAccessFn;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class MapUiByColumnArray extends MapUiWidgetBase implements ValueChangeHandler<MapUiField.FIELD_TYPES>
{
    private DbRowArrayAccess access;
    
    private ListBox uiNewColumn = new ListBox(); 
    
    private int rowValueAccess; 
    private int rowEnumAccess; 

    private FlexTable uiValueAccess = new FlexTable(); 
    private FlexTable uiEnumAccess = new FlexTable();  

    private MapUiEnumField uiValueEnumAccess = null; 
    private DbRowByColumnAccess byColumnAccess = null;
    private Map<String, List<String>> dbMetaInfo = null; 

    public MapUiByColumnArray(EmisMetaData field, EmisToolboxServiceAsync service, String dataset)
    { 
        super(service, dataset); 
        
        if (field.getType() == EmisMetaData.EmisDataType.ENUM || field.getType() == EmisMetaData.EmisDataType.ENUM_SET)
        {
        	uiValueEnumAccess = new MapUiEnumField(field, service, field.getType() == EmisMetaData.EmisDataType.ENUM_SET, dataset); 
        	uiValueEnumAccess.setWithFieldWidget(false);
        }
        
        uiNewColumn.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                int index = uiNewColumn.getSelectedIndex(); 
                if (index == -1)
                    return; 

                String colName = uiNewColumn.getValue(index); 
                if (colName == null)
                    return; 
                
                commitByColumnAccess(); 
                byColumnAccess.findColumn(colName, true); 

                uiNewColumn.setSelectedIndex(0); 
                updateUi(); 
            }
        }); 
        
        int row = 0; 
        getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
        this.rowEnumAccess = row;
        setHTML(row, 0, Message.messageAdmin().mapuiaRowEnumAccess());
        getCellFormatter().setStyleName(row, 0, "sectionBarDark");
        row++; 
        
        setWidget(row, 0, uiEnumAccess);
        row++; 
        
        rowValueAccess = row; 
        getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
        setHTML(row, 0, Message.messageAdmin().mapuiaRowValueAccess());
        getCellFormatter().setStyleName(row, 0, "sectionBarDark");
        row++; 
        setWidget(row, 0, uiValueAccess); 
        row++; 
        if (uiValueEnumAccess != null)
        	setWidget(row, 0, uiValueEnumAccess); 
        row++; 
    }
    
    public void onValueChange(ValueChangeEvent<FIELD_TYPES> event)
    {
        commitByColumnAccess(); 
        
        DbRowAccess[] indexAccess = access.getIndexAccess(); 
        // We need to check if we need to switch the UI between by column and plain access. 
        List<EmisMetaEnum> byColumnEnums = new ArrayList<EmisMetaEnum>();  
        for (int row = 0; row < uiEnumAccess.getRowCount(); row++) 
        {
            indexAccess[row] = MapUiUtil.getAccess(uiEnumAccess.getWidget(row, 1));
            if (indexAccess[row] instanceof DbRowEnumAccess)
            {
                DbRowEnumAccess enumAccess = (DbRowEnumAccess) indexAccess[row]; 
                if (enumAccess.getAccess() instanceof DbRowByColumnIndexAccess)
                    byColumnEnums.add(enumAccess.getEnumType()); 
            }
        }

        if (byColumnEnums.size() > 0)
            // We want to display access in multiple columns. 
            updateEnums(byColumnEnums); 
        else
        {
            if (byColumnAccess != null)
            {
                byColumnAccess = null; 
                access.setValueAccess(null); 
                
                updateUi(false, true); 
            }
        }
    }

    public void getValuePreview(AsyncCallback<List<String>> paramAsyncCallback)
    {}

    public void updateDbMetaInfo()
    {}

    public void updateDbMetaInfo(DbContext dbContext, Map<String, List<String>> dbMetaInfo)
    {
        if (dbContext == null)
            return;
        setDbContext(dbContext);

        // Ensure child fields have the context set. 
        // 
        this.dbMetaInfo = dbMetaInfo; 
        for (int row = 0; row < getMetaContext().getArrayDimensions().getDimensions(); row++)
            updateMetaInfo(uiEnumAccess.getWidget(row, 1)); 
        
        if (access != null && getByColumnAccess(access.getValueAccess()) == null)
            updateMetaInfo(uiValueAccess.getWidget(0, 0)); 
        
        List<String> fields = dbMetaInfo == null ? null : dbMetaInfo.get(getDbContext().getQuery());
        if (fields == null)
            uiNewColumn.addItem(Message.messageAdmin().mapuifInfoWaitingForFieldNames(), (String) null);
        else
        {
            uiNewColumn.addItem("(add value field)", (String) null); 
            for (String field : fields)
                uiNewColumn.addItem(field);
        }
    }

    public void set(DbRowAccess data)
    {
        if (!(data instanceof DbRowArrayAccess))
            return;

        access = (DbRowArrayAccess) data; 
        byColumnAccess = getByColumnAccess(access.getValueAccess()); 
        if (access.getValueAccess() instanceof DbRowEnumAccess)
        	uiValueEnumAccess.set(access.getValueAccess());
        
        updateUi(); 
    }
    
    private DbRowByColumnAccess getByColumnAccess(DbRowAccess access)
    {
    	if (access instanceof DbRowByColumnAccess)
    		return (DbRowByColumnAccess) access; 
    	
    	if (access instanceof DbRowEnumAccess)
    		return getByColumnAccess(((DbRowEnumAccess) access).getAccess()); 

    	return null; 
    }

    public DbRowAccess get()
    {
        commit();
        return this.access;
    }

    public void updateEnums(List<EmisMetaEnum> enums)
    {
        if (byColumnAccess == null)
        {
            byColumnAccess = new DbRowByColumnAccess(); 
            if (getMetaContext().getType() == EmisMetaData.EmisDataType.ENUM || getMetaContext().getType() == EmisMetaData.EmisDataType.ENUM_SET)
            {
            	DbRowEnumAccess enumAccess = new DbRowEnumAccess(byColumnAccess, getMetaContext().getEnumType()); 
// Configure settings? 
            	access.setValueAccess(enumAccess);
            }
            else
            	access.setValueAccess(byColumnAccess);
        }
        
        // Remove enums that are no longer required. 
        for (String enumName : byColumnAccess.getEnumNames())
        {
            if (NamedUtil.find(enumName, enums) == null)
                byColumnAccess.removeEnum(enumName); 
        }

        // Remove new enums that are no part of our set-up, yet. 
        for (EmisMetaEnum e : enums)
        {
            if (null == byColumnAccess.getEnumType(e.getName()))
                byColumnAccess.addEnum(e); 
        }
        
        updateUi(false, true); 
    }

    private void commitByColumnAccess()
    {
        for (int row = 1; row < uiValueAccess.getRowCount(); row++)
        {
            String colName = uiValueAccess.getText(row,  0); 
            for (int col = 2; col < uiValueAccess.getCellCount(row); col++) 
                byColumnAccess.setValue(colName,  uiValueAccess.getText(0, col), getListBoxValue(uiValueAccess.getWidget(row, col)));  
        }

        // Point DbRowByColumnIndexAccess to current DbRowByColumnAccess object. 
        DbRowAccessFn accessUpdateFn = new DbRowAccessFn() {
            public void fn(DbRowAccess access)
            {
            	if (access instanceof DbRowByColumnIndexAccess)
            		((DbRowByColumnIndexAccess) access).setByColumnAccess(byColumnAccess);
            	else if (access instanceof DbRowEnumAccess && ((DbRowEnumAccess) access).getAccess() instanceof DbRowByColumnIndexAccess)
            	{
            		DbRowEnumAccess enumAccess = (DbRowEnumAccess) access; 
            		((DbRowByColumnIndexAccess) enumAccess.getAccess()).setByColumnAccess(byColumnAccess);
            	}
            }
        };
        
        for (DbRowAccess indexAccess : access.getIndexAccess())
        {
        	if (indexAccess != null)
        		indexAccess.map(accessUpdateFn); 
        }
    }
    
    public void commit()
    {
        if (access == null)
            return; 
        
        EmisMetaEnum[] enums = getMetaContext().getArrayDimensions().getEnums();
        DbRowAccess[] indexAccess = new DbRowAccess[enums.length];
        for (int i = 0; i < enums.length; i++)
            indexAccess[i] = MapUiUtil.getAccess(uiEnumAccess.getWidget(i, 1));
        
        access.setIndexAccess(indexAccess); 

        
        DbRowAccess valueAccess = null; 
        if (byColumnAccess != null)
        {
            commitByColumnAccess();
            valueAccess = byColumnAccess; 
        }
        else
        	valueAccess = MapUiUtil.getAccess(uiValueAccess.getWidget(0, 0)); 
        
        if (uiValueEnumAccess != null)
        {
        	DbRowEnumAccess enumAccess = (DbRowEnumAccess) uiValueEnumAccess.get();
        	enumAccess.setAccess(valueAccess);
        	valueAccess = enumAccess; 
        }
        
        access.setValueAccess(valueAccess); 
    }

    public void setMetaContext(EmisMetaData field)
    {
        super.setMetaContext(field);
        if (field == null || field.getArrayDimensions().getDimensions() == 0)
            throw new IllegalArgumentException(Message.messageAdmin().mapuiaErrorArrayNeedsToSpecifyEmisMetaDataType());

        if (this.access == null)
            this.access = MapUiUtil.createArrayAccess(getMetaContext()); 

        updateUi(); 
    }

    private boolean hasMultipleValuesInUi()
    {
        for (int row = 1; row < uiEnumAccess.getRowCount(); row++) 
        {
            Widget w = uiEnumAccess.getWidget(row, 1); 
            if (!(w instanceof MapUiField))
            {
                // check if byColumn is selected and ...
                // return true
            }
        }
        
        return false; 
    }
    
    private void updateUi()
    { updateUi(true, true); } 
    
    private void updateUi(boolean updateEnums, boolean updateValue)
    {
        EmisMetaData field = getMetaContext();
        if (field == null)
        {
            clearCell(rowEnumAccess, 1);
            clearCell(rowValueAccess, 1); 
            return; 
        }
    
        if (updateEnums)
            updateEnumUi(); 
        
        if (updateValue)
            updateValueUi();
    }
    
    private void updateValueUi()
    {
        uiValueAccess.removeAllRows(); 
        if (access == null)
            return; 
        
        if (getByColumnAccess(access.getValueAccess()) != null)
        {
            byColumnAccess = getByColumnAccess(access.getValueAccess()); 
            List<String> colNames = byColumnAccess.getColumnNames();
            List<String> enumNames = byColumnAccess.getEnumNames();
            
            int row = 0;
            for (int col = 0; col < enumNames.size(); col++)
                uiValueAccess.setText(row, 2 + col, enumNames.get(col));
            
            row++;
            for (int i = 0; i < colNames.size(); i++)
            {
                final int rowValue = row;
                
                uiValueAccess.setText(row, 0, colNames.get(i));
                HTML btnDel = new HTML("[X]");
                btnDel.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event)
                    { uiValueAccess.removeRow(rowValue); }
                });
                uiValueAccess.setWidget(row, 1, btnDel);

                for (int col = 0; col < enumNames.size(); col++)
                    uiValueAccess.setWidget(row, col + 2, getEnumListBox(byColumnAccess.getValue(colNames.get(i), enumNames.get(col)), byColumnAccess.getEnumType(enumNames.get(col))));
                
                row++;
            }
            
            // Control to add new column. 
            uiValueAccess.setWidget(row, 0, uiNewColumn);
        }
        else
        {
            Widget w = MapUiUtil.getMapUiAsWidget(access.getValueAccess(), getMetaContext(), getService(), getDataset());
            updateMetaInfo(w);
            uiValueAccess.setWidget(0, 0, w);
        }
    }

    private void updateMetaInfo(Widget w)
    {
        if (w instanceof DbMetaInfoAware)
            ((DbMetaInfoAware) w).updateDbMetaInfo(getDbContext(), dbMetaInfo); 
    }
    
    
    private void updateEnumUi()
    {
        EmisMetaEnumTuple enumTuple = getMetaContext().getArrayDimensions();
        EmisMetaEnum enums[] = enumTuple.getEnums(); 
        DbRowAccess[] indexAccess = access == null ? null : access.getIndexAccess(); 

        uiEnumAccess.removeAllRows(); 
        
        int row = 0; 
        for (int i = 0; i < enums.length; i++) 
        {
        	getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
        	
            uiEnumAccess.setText(row, 0, enums[i].getName() + ":"); 
            uiEnumAccess.getCellFormatter().setStyleName(row, 0, "section");
            uiEnumAccess.getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
            
            Widget w = MapUiUtil.getMapUiAsWidget(indexAccess == null ? null : indexAccess[i], enums[i], false, null, getService(), getDataset()); 
            if (w instanceof MapUiEnumField)
            {
                ((MapUiEnumField) w).addByColumnType(); 
                ((MapUiEnumField) w).addValueChangeHandler(this); 
            }
            
            updateMetaInfo(w); 
            uiEnumAccess.setWidget(row, 1, w);
            row++; 
        }
    }

    private String getListBoxValue(Widget w)
    {
        if (!(w instanceof ListBox))
            return null; 
        
        ListBox lb = (ListBox) w; 
        if (lb.getSelectedIndex() == -1)
            return null; 
        
        return lb.getValue(lb.getSelectedIndex()); 
    }
    
    private ListBox getEnumListBox(String value, EmisMetaEnum enumType)
    {
        int selectedIndex = -1; 
        
        ListBox result = new ListBox();
        for (String text : enumType.getValues())
        {
            if (value != null && value.equals(text))
                selectedIndex = result.getItemCount(); 
            
            result.addItem(text); 
        }
        
        if (selectedIndex != -1)
            result.setSelectedIndex(selectedIndex);
        
        return result; 
    }
}
