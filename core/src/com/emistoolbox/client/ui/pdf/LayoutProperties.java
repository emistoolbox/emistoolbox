package com.emistoolbox.client.ui.pdf;

import java.util.HashMap;
import java.util.Map;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;

public abstract class LayoutProperties<T> extends FlexTable implements EmisEditor<T>, HasValueChangeHandlers<T> 
{
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) 
	{ return addHandler(handler, ValueChangeEvent.getType()); }

	protected void fireValueChangeEvent()
	{ ValueChangeEvent.fire(this, get()); }
	
	private ChangeHandler handler = null; 
	protected ChangeHandler getChangeHandler()
	{ 
		if (handler == null)
			handler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				fireValueChangeEvent(); 
			}
		};
		
		return handler; 
	}
	
	protected Map<String, TextBox> initTexts(String[] keys, String[] labels)
	{
		Map<String, TextBox> uiTexts = new HashMap<String, TextBox>(); 
		for (int i = 0; i < keys.length; i++)
		{
			int row = getRowCount();
			setText(row, 0, labels == null || labels.length <= i ? keys[i] : labels[i]);

			TextBox uiText = new TextBox(); 
			setWidget(row, 1, uiText);
			uiTexts.put(keys[i], uiText); 
		}
		
		return uiTexts; 
	}
	
	protected void setTextSet(Map<String, TextBox> uiTexts, TextSet texts)
	{
		for (Map.Entry<String, TextBox> entry : uiTexts.entrySet())
		{
			String value = texts.getText(entry.getKey()); 
			entry.getValue().setText(value == null ? ""  : value);
		}
	}
	
	protected void updateTextSet(Map<String, TextBox> uiTexts, TextSet texts)
	{
		for (Map.Entry<String, TextBox> entry : uiTexts.entrySet())
			texts.putText(entry.getKey(), entry.getValue().getText());  
	}
}
