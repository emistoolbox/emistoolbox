package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

public class LayoutFrameWidget extends AbsolutePanel implements EmisEditor<LayoutFrameConfig>
{
	private LayoutPageEditor pageEditor; 
	private LayoutFrameConfig frameConfig; 
	private HTML uiHtml = new HTML(); 

	private ListBox uiMove = new ListBox(); 
	
	public LayoutFrameWidget(final LayoutPageEditor pageEditor)
	{
		this.pageEditor = pageEditor; 
		setStyleName("layoutFrame"); 
		
		add(uiHtml, 0, 0);
		setWidth("100%");
		setHeight("100%");

		add(uiMove, 5, 5); 
		uiMove.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int index = uiMove.getSelectedIndex(); 
				if (index <= 0)
					return; 
				
				String value = uiMove.getValue(index);
				if (value.equals("del"))
					pageEditor.getReportEditor().moveFrame(LayoutFrameWidget.this, null);
				else
					pageEditor.getReportEditor().moveFrame(LayoutFrameWidget.this, new Integer(value));
			}
		}); 
		
		MouseHandler mouseHandler = new MouseHandler(); 
		uiHtml.addMouseDownHandler(mouseHandler); 
		uiHtml.addMouseUpHandler(mouseHandler); 
		uiHtml.addMouseMoveHandler(mouseHandler); 
	}
	
	public void updatePageIndex(int index, int totalPages)
	{
		uiMove.clear(); 
		
		uiMove.addItem(""); 
		for (int i = 0; i < totalPages; i++) 
		{
			if (i != index)
				uiMove.addItem("Page " + (i + 1), "" + i);
		}

		uiMove.addItem("(delete)", "del"); 
	}
	
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
	
	private void resize(int xOffset, int yOffset)
	{ setPixelSize(getElement().getClientWidth() + xOffset, getElement().getClientHeight() + yOffset); }
	
	private enum MouseMode { MOVE, RESIZE };

	public class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler
	{
		
		private MouseMode mode; 
		
		private Integer lastX = null; 
		private Integer lastY = null; 
		
		@Override
		public void onMouseMove(MouseMoveEvent event) 
		{
			if (lastX != null && lastY != null)
				updatePosition(event); 
		}

		@Override
		public void onMouseUp(MouseUpEvent event) 
		{
			updatePosition(event); 
			lastX = null; 
			lastY = null;
			mode = null; 
		}

		@Override
		public void onMouseDown(MouseDownEvent event) 
		{
			lastX = event.getScreenX(); 
			lastY = event.getScreenY(); 
			
			if (lastX >= getOffsetWidth() - 15 && lastY >= getOffsetHeight() - 15)
				mode = MouseMode.RESIZE; 
			else
				mode = MouseMode.MOVE; 
			
			pageEditor.selectFrame(LayoutFrameWidget.this); 
		}
		
		private void updatePosition(MouseEvent event)
		{
			int xOffset = event.getScreenX() - lastX;  
			int yOffset = event.getScreenY() - lastY; 
			
			lastX = event.getScreenX(); 
			lastY = event.getScreenY(); 
			
			if (mode == MouseMode.RESIZE)
				resize(xOffset, yOffset); 
			else
				pageEditor.moveFrame(LayoutFrameWidget.this, xOffset, yOffset); 
		}
	}
}
