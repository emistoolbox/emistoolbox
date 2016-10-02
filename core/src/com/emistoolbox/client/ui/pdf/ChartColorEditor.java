package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.ChartColor;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class ChartColorEditor extends HTML implements EmisEditor<ChartColor>
{
	private ChartColor color;
	private HTML uiPicker = new HTML();  

	private PopupPanel uiPopup = new PopupPanel(); 

	public ChartColorEditor()
	{ 
		set(null); 
		uiPicker = new HTML(); 
		uiPicker.addStyleName("colorpicker");
		uiPicker.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				int x = event.getNativeEvent().getClientX() - uiPicker.getElement().getAbsoluteLeft(); 
				int y = event.getNativeEvent().getClientY() - uiPicker.getElement().getAbsoluteTop(); 
				
				uiPopup.hide(); 
			}
			
		}); 
		uiPopup.add(uiPicker);
		
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				uiPopup.showRelativeTo(ChartColorEditor.this);
			}
		}); 
	}

	public void addChangeHandler(ChangeHandler handler)
	{}
	
	@Override
	public void commit() 
	{}

	@Override
	public ChartColor get()
	{
		commit(); 
		return color; 
	} 

	@Override
	public void set(ChartColor color) 
	{ 
		this.color = color; 
		updateUi();  
	}
	
	private void updateUi()
	{
		if (color == null)
			setHTML("<div style='border: 1px solid #000; width: 20px; height: 10px;><small>X</small></div>"); 
		else
			setHTML("<div style='border: 1px solid #000; width: 20px; height: 10px; background-color: rgba(" + color.toString() + ");'></div>"); 
	}
}
