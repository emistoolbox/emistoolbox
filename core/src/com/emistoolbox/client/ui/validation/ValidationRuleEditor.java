package com.emistoolbox.client.ui.validation;

import java.util.Iterator;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class ValidationRuleEditor<T extends EmisValidationRule> extends FlexTable implements EmisEditor<T>, HasValueChangeHandlers<String>
{
    private EmisMetaEntity entityType; 
    private ValidationFilterEditor uiGroupBy; 
    
    private T item; 
    private String[] fieldNames;
    protected int row = 0; 
    private int rowFieldsStart = -1; 
    
    private TextBox uiName = new TextBox(); 
    private TextBox uiDescription = new TextBox(); 
    
    public ValidationRuleEditor(EmisMetaEntity entityType, String[] fieldNames)
    {
        this.fieldNames = fieldNames; 
        this.entityType = entityType;
        
        uiName.setWidth("340px"); 
        uiDescription.setWidth("340px");
        
        uiGroupBy = new ValidationFilterEditor(entityType); 
        uiName.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) 
			{ updateName(); } 
        }); 
    }
    
    private void updateName()
    {
		String name = uiName.getText(); 
		if (name == null || name.equals(""))
			name = item.getRuleName(); 
		else
			name = item.getRuleName() + ": " + name; 

		ValueChangeEvent.fire(this, name);
    }
        
    private void updateGroupBy()
    {}
    
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }

    private void updateFieldUi(int row, String name, EmisMetaData currentField, EmisValidationFilter currentFilter)
    {
        setHTML(row, 0, "<b>" + name + ":</b>"); 
        getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP); 

        final ListBoxWithUserObjects<EmisMetaData> uiFields = new ListBoxWithUserObjects<EmisMetaData>(); 
        
        for (EmisMetaData field : entityType.getData())
        {
        	if (field.getType() != EmisDataType.BYTE && field.getType() != EmisDataType.BOOLEAN && field.getType() != EmisDataType.INTEGER)
        		continue; 
        	
            uiFields.add(field.getName(), field); 
            if (currentField != null && NamedUtil.sameName(field, currentField))
                uiFields.setSelectedIndex(uiFields.getItemCount() - 1); 
        }
        
        ValidationFilterEditor uiFilter = new ValidationFilterEditor(entityType); 
        if (currentFilter != null)
            uiFilter.set(currentFilter); 
        
        final VerticalPanel vp = new VerticalPanel(); 
        vp.setStyleName("adminEdit"); 
        vp.setSpacing(2); 
        vp.add(uiFields); 
        vp.add(uiFilter); 

        uiFields.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event)
            {
                EmisMetaData field = uiFields.getUserObject(); 
                ValidationFilterEditor uiFilter = new ValidationFilterEditor(field); 
                vp.remove(1); 
                vp.add(uiFilter);
                
                updateGroupBy(); 
            }
        });

        setWidget(row, 1, vp); 
    }
    
    protected void updateUi()
    {
    	updateNameUi(); 
    	updateFieldsUi(); 
    }
    
    protected void updateNameUi()
    {
    	setHTML(row, 0, "<b>Name:</b>"); 
    	setWidget(row, 1, uiName); 
    	row++; 
    	
    	setHTML(row, 0, "<b>Description:</b>"); 
    	setWidget(row, 1, uiDescription); 
    	row++; 
    }
    
    protected void updateFieldsUi()
    {
        rowFieldsStart = row; 
        for (String fieldName : fieldNames)
        {
            setText(row, 0, fieldName + ":"); 
            updateFieldUi(row, fieldName, null, null); 
            row++; 
        }

        setHTML(row, 0, "<b>Group by:</b>"); 
        setWidget(row, 1, uiGroupBy);
        getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP); 

        row++; 
    }
    
    
    @Override
    public void commit()
    {
        item.setName(uiName.getText());
        item.setDescription(uiDescription.getText());
    	
        EmisMetaData[] fields = new EmisMetaData[fieldNames.length]; 
        EmisValidationFilter[] filters = new EmisValidationFilter[fieldNames.length]; 

        for (int i = 0; i < fieldNames.length; i++)  
        {
            fields[i] = null; 
            filters[i] = null; 

            Widget w = getWidget(rowFieldsStart + i, 1);
            if (w instanceof VerticalPanel)
            {
            	ListBoxWithUserObjects<EmisMetaData> lb = (ListBoxWithUserObjects<EmisMetaData>) getWidget((VerticalPanel) w, ListBoxWithUserObjects.class.getName());
                if (lb != null)
                    fields[i] = lb.getUserObject(); 
                
                ValidationFilterEditor vfe = (ValidationFilterEditor) getWidget((VerticalPanel) w, ValidationFilterEditor.class.getName()); 
                if (vfe != null)
                    filters[i] = vfe.get(); 
            }
        }
        
        item.setFields(fields, filters); 
        item.setGroupBy(uiGroupBy.get());
    }

    @Override
    public T get()
    {
        commit(); 

        return item;
    }

    private Widget getWidget(CellPanel p, String name)
    {
        Iterator<Widget> iter = p.iterator(); 
        while (iter.hasNext())
        {
            Widget w = iter.next(); 
            if (w.getClass().getName().equals(name))
                return w; 
        }
        
        return null;         
    }
    
    @Override
    public void set(T item)
    {
        this.item = item; 
        
        uiName.setText(item.getName()); 
        uiDescription.setText(item.getDescription()); 
        
        for (int i = 0; i < fieldNames.length; i++) 
            updateFieldUi(rowFieldsStart + i, fieldNames[i], item.getField(i), item.getFilter(i));
        
        uiGroupBy.set(item.getGroupBy()); 
    }
    
    abstract public String getText(); 
    
    protected String getFieldName(EmisValidationRule rule, int index)
    {
        EmisMetaData field = rule.getField(index); 
        if (field == null)
            return "(field)";
        
        return field.getName(); 
    }

    protected T getItem()
    { return item; } 
    
    public void focusField(String name)
    { 
    	if (name == null || name.equals("name"))
    		uiName.setFocus(true);
    	else if (name.equals("description"))
    		uiDescription.setFocus(true); 
    	else
    	{
    		for (int i = 0; i < fieldNames.length; i++) 
    		{
    			if (fieldNames.equals(name))
    				; // TODO
    		}
    	}
    }
}
