package com.emistoolbox.client.admin.ui;

import java.util.List;
import java.util.Set;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.impl.EmisEnumUtils;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;

public class DateTupleEditor extends FlexTable implements EmisEditor<EmisEnumTupleValue>, HasValueChangeHandlers<EmisEnumTupleValue>
{
    private List<EmisMetaDateEnum> dateTypes;
    private ListBox[] uiValues;
    private boolean[] requiredFields;
    private PushButton btnOk = new PushButton("OK"); 

    public DateTupleEditor(Set<EmisMetaDateEnum> newDateTypes)
    {
    	dateTypes = EmisEnumUtils.sort(newDateTypes);

    	uiValues = new ListBox[newDateTypes.size()]; 
    	requiredFields = new boolean[newDateTypes.size()]; 
    	
    	int row = 0; 
    	for (EmisMetaDateEnum dateType : dateTypes)
    	{
    		setHTML(row, 0, "<b>" + dateType.getName() + ":</b>"); 
    		uiValues[row] = getListBox(dateType); 
    		setWidget(row, 1, uiValues[row]); 
    		row++; 
    	}
    	
    	EmisUtils.init(btnOk, 80); 
    	setWidget(row, 1, btnOk); 
    	btnOk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				if (hasRequiredValues())
					ValueChangeEvent.fire(DateTupleEditor.this, get());
			}
    	}); 
    	
    	updateButton(); 
    }
    
    public void setRequiredFields(Set<EmisMetaDateEnum> requiredDateTypes)
    {
    	requiredFields = new boolean[dateTypes.size()];
    	for (EmisMetaDateEnum dateType : requiredDateTypes)
    	{
    		int index = findIndex(dateType); 
    		if (index != -1)
    			requiredFields[index] = true; 
    	}
    	
    	updateButton(); 
    }

    private ListBox getListBox(EmisMetaDateEnum dateType)
    {
    	ListBox result = new ListBox(); 
    	result.setVisibleItemCount(1);

    	result.addItem(""); 
    	for (String value : dateType.getValues())
    		result.addItem(value);

    	result.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateButton(); 
			}
    	}); 
    	
    	return result;
    }
    
    private void updateButton()
    { btnOk.setEnabled(hasRequiredValues()); }
    
    @Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EmisEnumTupleValue> handler) 
    { return addHandler(handler, ValueChangeEvent.getType()); }

	@Override
	public void commit() 
	{}

	@Override
	public EmisEnumTupleValue get() 
	{
		EmisEnumTupleValue result = new EnumTupleValueImpl(); 

		EmisMetaDateEnum dateType = dateTypes.get(dateTypes.size() - 1); 
		result.setEnumTuple(dateType);
		
		byte[] indexes = new byte[dateType.getDimensions()]; 
		EmisMetaEnum[] enums = dateType.getEnums(); 
		for (int i = 0; i < indexes.length; i++) 
			indexes[i] = getListBoxIndex((EmisMetaDateEnum) enums[i]); 

		result.setIndex(indexes);

		return result; 
	}

	@Override
	public void set(EmisEnumTupleValue value) 
	{
		if (value == null)
		{
			for (int i = 0; i < dateTypes.size(); i++)
				setListBoxIndex(dateTypes.get(i), (byte) -1); 

			return; 
		}
		
		EmisMetaEnum[] enums = value.getEnumTuple().getEnums(); 
		byte[] indexes = value.getIndex(); 
		
		for (int i = 0; i < enums.length; i++)
			setListBoxIndex((EmisMetaDateEnum) enums[i], indexes[i]);
	} 

	private byte getListBoxIndex(EmisMetaDateEnum dateType)
	{
		ListBox lb = findListBox(dateType);
		if (lb == null)
			return -1; 

		byte result = (byte) lb.getSelectedIndex();
		if (result == -1)
			return result; 
		
		result--; 
		return result; 
	}
	
	private void setListBoxIndex(EmisMetaDateEnum dateType, byte index)
	{
		ListBox lb = findListBox(dateType); 
		if (lb != null)
			lb.setSelectedIndex(index + 1);
	}
	
	private int findIndex(EmisMetaDateEnum dateType)
	{
		for (int i = 0; i < dateTypes.size(); i++)
			if (NamedUtil.sameName(dateTypes.get(i),  dateType))
				return i; 
		
		return -1; 
	}
	
	private ListBox findListBox(EmisMetaDateEnum dateType)
	{
		int index = findIndex(dateType); 
		if (index == -1)
			return null; 
		
		return (ListBox) getWidget(index, 1); 
	}
	
	private boolean hasRequiredValues()
	{
		for (int i = 0; i < uiValues.length; i++) 
		{
			if (!requiredFields[i])
				continue; 
			
			int index = uiValues[i].getSelectedIndex();
			if (index == -1 || uiValues[i].getItemText(index).equals(""))
				return false; 
		}
		
		return true; 
	}
}
