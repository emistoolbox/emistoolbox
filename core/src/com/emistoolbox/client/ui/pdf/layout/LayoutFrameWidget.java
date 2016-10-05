package com.emistoolbox.client.ui.pdf.layout;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HTML;

public class LayoutFrameWidget extends HTML implements EmisEditor<LayoutFrameConfig>
{
	private LayoutPageEditor pageEditor; 
	private LayoutFrameConfig frameConfig; 

	public LayoutFrameWidget(final LayoutPageEditor pageEditor)
	{
		this.pageEditor = pageEditor; 
		setStyleName("layoutFrame"); 
		
		setWidth("100%");
		setHeight("100%");

		MouseHandler mouseHandler = new MouseHandler(); 
		addMouseDownHandler(mouseHandler); 
		addMouseUpHandler(mouseHandler); 
		addMouseMoveHandler(mouseHandler); 
	}
	
//	public void updatePageIndex(int index, int totalPages)
//	{
//		clear(); 
//		
//		uiMove.addItem(""); 
//		for (int i = 0; i < totalPages; i++) 
//		{
//			if (i != index)
//				uiMove.addItem("Page " + (i + 1), "" + i);
//		}
//
//		uiMove.addItem("(delete)", "del"); 
//	}
	
	@Override
	public void commit() 
	{}

	@Override
	public LayoutFrameConfig get() 
	{	
		commit(); 
		return frameConfig; 
	}

	@Override
	public void set(LayoutFrameConfig frameConfig) 
	{
		this.frameConfig = frameConfig; 
	}
	
	private enum MouseMode { MOVE, RESIZE };

	public class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler
	{
		
		private MouseMode mode; 
		
		private Integer lastX = null; 
		private Integer lastY = null; 
		
		@Override
		public void onMouseMove(MouseMoveEvent event) 
		{
			if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT)
				return;
			
			if (lastX != null && lastY != null)
				updatePosition(event); 
		}

		@Override
		public void onMouseUp(MouseUpEvent event) 
		{
			if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT)
				return; 
			
			updatePosition(event); 
			lastX = null; 
			lastY = null;
			mode = null; 
		}

		@Override
		public void onMouseDown(MouseDownEvent event) 
		{
			pageEditor.selectFrame(LayoutFrameWidget.this); 

			if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT)
			{
				lastX = null; 
				lastY = null; 
				
				showContextMenu(); 
			}
					
			lastX = event.getScreenX(); 
			lastY = event.getScreenY(); 

			if (event.isShiftKeyDown())
				mode = MouseMode.RESIZE; 
			else
				mode = MouseMode.MOVE; 
			
		}
		
		private void updatePosition(MouseEvent event)
		{
			int xOffset = event.getScreenX() - lastX;  
			int yOffset = event.getScreenY() - lastY; 
			
			lastX = event.getScreenX(); 
			lastY = event.getScreenY(); 
			
			if (event.isShiftKeyDown()) // mode == MouseMode.RESIZE)
				pageEditor.resizeFrame(LayoutFrameWidget.this, xOffset, yOffset); 
			else
				pageEditor.moveFrame(LayoutFrameWidget.this, xOffset, yOffset); 
		}
	}
	
	public void updateFrameStyle()
	{ 
		getElement().setAttribute("style", CSSCreator.getCssAsString(frameConfig));
		setHTML(getHtmlText(frameConfig, PdfText.TEXT_TITLE) + getHtmlText(frameConfig, PdfText.TEXT_SUBTITLE) + getContentHtml(frameConfig));  
	}
	
	private String getHtmlText(TextSet texts, String key)
	{
		String text = texts.getText(key); 
		if (text == null || text.equals(""))
			return "";
		
		ChartFont font = texts.getFont(key); 
		if (font == null)
			return "<span>" + text + "</span>"; 
		
		return "<span style='" + CSSCreator.getCssAsString(font) + "'>" + text + "</span>"; 
	}
	
	private void showContextMenu()
	{}
	
	private String getContentHtml(LayoutFrameConfig config)
	{ return config.getContentConfig().getClass().getName(); }
}
