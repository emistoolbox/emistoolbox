package com.emistoolbox.client.ui.pdf;

import java.util.HashMap;
import java.util.Map;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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
	
	private ValueChangeHandler valueHandler = null; 
	protected ValueChangeHandler getValueChangeHandler()
	{
		if (valueHandler == null)
			valueHandler = new ValueChangeHandler() {
				@Override
				public void onValueChange(ValueChangeEvent event) {
					fireValueChangeEvent(); 
				}
			};
			
		return valueHandler; 
	}
	
	protected Map<String, TextSetEntryUi> initTexts(String[] keys, String[] labels)
	{
		Map<String, TextSetEntryUi> result = new HashMap<String, TextSetEntryUi>(); 

		int row = getRowCount();
		for (int i = 0; i < keys.length; i++)
		{
			TextSetEntryUi ui = new TextSetEntryUi(); 
			ui.addValueChangeHandlers(getValueChangeHandler()); 
			result.put(keys[i], ui); 

			setWidget(row, 1, ui); 
			setText(row, 0, labels == null || labels.length <= i ? keys[i] : labels[i]);
			
			row++; 
		}
		
		return result; 
	}
	
	protected void setTextSet(Map<String, TextSetEntryUi> ui, TextSet texts)
	{
		for (Map.Entry<String, TextSetEntryUi> entry : ui.entrySet())
		{
			String text = texts.getText(entry.getKey()); 
			ChartFont font = texts.getFont(entry.getKey()); 
			
			entry.getValue().setText(text);
			entry.getValue().setFont(font); 
		}
	}
	
	protected void updateTextSet(Map<String, TextSetEntryUi> ui, TextSet texts)
	{
		for (Map.Entry<String, TextSetEntryUi> entry : ui.entrySet())
			texts.putText(entry.getKey(), entry.getValue().getText(), entry.getValue().getFont());  
	}
	
	public static class TextSetEntryUi extends VerticalPanel
	{
		private TextBox uiText = new TextBox(); 
		private ChartFontEditor uiFont = new ChartFontEditor(); 
	
		public TextSetEntryUi()
		{
			add(uiText); 
			add(uiFont);
		}
		
		public void addValueChangeHandlers(ValueChangeHandler handler)
		{
			uiText.addValueChangeHandler(handler); 
			uiFont.addValueChangeHandler(handler); 
		}
		
		public void setText(String text)
		{ uiText.setText(text); }
		
		public void setFont(ChartFont font)
		{ uiFont.set(font); }
		
		public String getText()
		{ return uiText.getText(); } 
		
		public ChartFont getFont()
		{ return uiFont.get(); } 
		
		public TextBox getTextUi()
		{ return uiText; } 
		
		public ChartFontEditor getFontUi()
		{ return uiFont; } 
	}
}
