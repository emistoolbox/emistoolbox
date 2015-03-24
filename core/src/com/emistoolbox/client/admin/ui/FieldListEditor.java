package com.emistoolbox.client.admin.ui;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;

public class FieldListEditor extends FlexTable implements EmisEditor<List<String>>, HasValueChangeHandlers<List<String>>
{
	private ListBox uiFields = new ListBox(true);
	private PushButton btnOk = new PushButton("OK");
    private HandlerManager manager = new HandlerManager(this);

	public FieldListEditor(EmisMetaEntity entity)
	{
		for (EmisMetaData field : entity.getData())
		{
			if (field.getArrayDimensions() == null)
				uiFields.addItem(field.getName());
		}

		uiFields.setHeight("150px");

		getFlexCellFormatter().setRowSpan(0, 0, 2);
		setWidget(0, 0, uiFields);

		setHTML(0, 1, "Select multiple fields by pressing 'CTRL' key."); 
		getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		setWidget(1, 1, btnOk);
		
		EmisUtils.init(btnOk, 60); 
		btnOk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ ValueChangeEvent.fire(FieldListEditor.this, get());}
		}); 
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) 
	{ return this.manager.addHandler(ValueChangeEvent.getType(), handler); }

    public void fireEvent(GwtEvent<?> event)
    { this.manager.fireEvent(event); }

	@Override
	public void commit() 
	{}

	@Override
	public List<String> get() 
	{
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < uiFields.getItemCount(); i++)
		{
			if (uiFields.isItemSelected(i))
				result.add(uiFields.getItemText(i)); 
		}
		
		return result; 
	}

	@Override
	public void set(List<String> fields) 
	{
		if (fields == null)
			return; 
		
		for (int i = 0; i < uiFields.getItemCount(); i++)
			uiFields.setItemSelected(i, fields.contains(uiFields.getItemText(i)));
	}
}
