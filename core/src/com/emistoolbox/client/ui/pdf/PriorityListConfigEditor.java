package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

public class PriorityListConfigEditor extends HTML implements EmisEditor<PdfPriorityListContentConfig>, HasValueChangeHandlers<PdfPriorityListContentConfig>
{
	private CheckBox uiFilterEmpty = new CheckBox("Filter empty values"); 
	private TextBox uiShowMaxRows = new TextBox(); 

	private PopupEditor uiPopup = new PopupEditor(); 

	private PdfPriorityListContentConfig config; 
	
	public PriorityListConfigEditor()
	{
		uiPopup.add(new HTML("<b>Settings for the priority list</b>")); 
		uiPopup.add(uiFilterEmpty);
		uiPopup.add("Max rows", uiShowMaxRows); 
		
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				uiPopup.show(PriorityListConfigEditor.this); 
			}
		}); 
		
		uiPopup.addValueChangeHandler(new ValueChangeHandler<PopupEditor>() {
			@Override
			public void onValueChange(ValueChangeEvent<PopupEditor> event) {
				ValueChangeEvent.fire(PriorityListConfigEditor.this, get());
				updateUi(); 
			}
		}); 
		
		set(null); 
	}
	
	@Override
	public void commit() 
	{
		if (config == null)
			return; 
		
		config.setFilterEmpty(uiFilterEmpty.getValue());
		try { config.setMaxRowCount(new Integer(uiShowMaxRows.getText())); }
		catch (Throwable err)
		{ config.setMaxRowCount(null); }
	}

	@Override
	public PdfPriorityListContentConfig get() 
	{
		commit(); 
		return config;
	}

	@Override
	public void set(PdfPriorityListContentConfig config) 
	{
		this.config = config; 
		if (config == null)
		{
			uiFilterEmpty.setChecked(false);
			uiShowMaxRows.setText("");
		}
		else
		{
			uiFilterEmpty.setChecked(config.getFilterEmpty());
			uiShowMaxRows.setText(config.getMaxRowCount() == null ? "" : "" + config.getMaxRowCount()); 
		}
		
		updateUi(); 
	}

	private void updateUi()
	{
		if (config == null)
			setHTML(""); 
		else
		{
			String result = config.getFilterEmpty() ? "Filter empty. " : "";
			result += ((config.getMaxRowCount() == null) ? "Show all rows." : ("Show " + config.getMaxRowCount() + " rows.")); 
			setHTML(result); 
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PdfPriorityListContentConfig> handler) 
	{ return addHandler(handler, ValueChangeEvent.getType()); }
}
