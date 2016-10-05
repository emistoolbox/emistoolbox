package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class ChartColorEditor extends HTML implements EmisEditor<ChartColor>, HasValueChangeHandlers<ChartColor>
{
	private ChartColor color;
//	private Image uiPicker = new Image("images/palette.png");
	private HTML uiPicker = new HTML("<img id='colourPicker' src='images/palette.png'>");

	private PopupPanel uiPopup = new PopupPanel(); 

	public ChartColorEditor()
	{ 
		uiPopup.setModal(true);
		uiPopup.setAutoHideEnabled(true);

		set(null); 
//		uiPicker.getElement().setId("colourPicker");
		uiPicker.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				int x = event.getX(); 
				int y = event.getY(); 
				
				String colours = getColor((Element) uiPicker.getElement().getFirstChild(), x, y); 
				String[] values = colours.split(","); 

				color = new ChartColor(new Integer(values[0]), new Integer(values[1]), new Integer(values[2]), new Integer(values[3]));
				uiPopup.hide(); 

				updateUi(); 
				
				ValueChangeEvent.fire(ChartColorEditor.this, color); 
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

	private native String getColor(Element img, int x, int y) /*-{
		var canvas = document.createElement('canvas');
		canvas.width = img.width;
		canvas.height = img.height;
		canvas.getContext('2d').drawImage(img, 0, 0, img.width, img.height);
		var values = canvas.getContext('2d').getImageData(x, y, 1, 1).data;

		return values[0] + "," + values[1] + "," + values[2] + "," + values[3];  
	}-*/; 
	
	private void updateUi()
	{
		if (color == null)
			setHTML("<div style='border: 1px solid #000; width: 20px; height: 10px;'><small>XXX</small></div>"); 
		else
			setHTML("<div style='border: 1px solid #000; width: 20px; height: 10px; background-color: " + CSSCreator.getCss(color) + ";'></div>"); 
	}
	

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ChartColor> handler) 
	{ return super.addHandler(handler, ValueChangeEvent.getType()); }
}
