package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TableStyleConfig;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.emistoolbox.common.util.Point;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public class LayoutFrameWidget extends SimplePanel implements EmisEditor<LayoutFrameConfig>
{
	private LayoutPageEditor pageEditor; 
	private LayoutFrameConfig frameConfig; 
	private HTML uiContent = new HTML(); 
	
	public LayoutFrameWidget(final LayoutPageEditor pageEditor)
	{
		this.pageEditor = pageEditor; 
		
		setStyleName("layoutFrame"); 	
		uiContent.setWidth("100%");
		uiContent.setHeight("100%");

		add(uiContent); 
		
		MouseHandler mouseHandler = new MouseHandler(); 
		uiContent.addMouseDownHandler(mouseHandler); 
		uiContent.addMouseUpHandler(mouseHandler); 
		uiContent.addMouseMoveHandler(mouseHandler); 
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
			
			Point gridAdjustment = null; 
			if (event.isShiftKeyDown()) // mode == MouseMode.RESIZE)
				gridAdjustment = pageEditor.resizeFrame(LayoutFrameWidget.this, xOffset, yOffset); 
			else
				gridAdjustment = pageEditor.moveFrame(LayoutFrameWidget.this, xOffset, yOffset); 
			
//			lastX -= gridAdjustment.getIntX(); 
//			lastY -= gridAdjustment.getIntY(); 
		}
	}
	
	public void updateFrameStyle()
	{ 
		uiContent.getElement().setAttribute("style", CSSCreator.getCssAsString(frameConfig));
		uiContent.setHTML(getHtmlText(frameConfig, PdfText.TEXT_TITLE) + getHtmlText(frameConfig, PdfText.TEXT_SUBTITLE) + getContentHtml(frameConfig));
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
	{
		PdfContentConfig content = config.getContentConfig();
		if (config == null || content == null)
			return null; 
		else
			return content.accept(new ContentConfigPreview(config)); 
	}
}

class ContentConfigPreview implements PdfContentConfigVisitor<String>
{
	private LayoutFrameConfig frameConfig; 
	
	public ContentConfigPreview(LayoutFrameConfig config)
	{ this.frameConfig = config; } 

	@Override
	public String visit(PdfTextContentConfig config) 
	{
		StringBuffer result = new StringBuffer(); 
		divOpen(result, "content-text");
		if (config.getTitle() != null)
		{
			divOpen(result, "class", "title", "style", CSSCreator.getCssAsString(frameConfig.getFont(PdfText.TEXT_TITLE))); 
			result.append(config.getTitle());
			divClose(result);
		}
		divOpen(result, "class", "body", "style", CSSCreator.getCssAsString(frameConfig.getFont(PdfText.TEXT_PLAIN))); 
		result.append(config.getText());
		divClose(result); 
		divClose(result); 
		
		return result.toString();
	}

	@Override
	public String visit(PdfVariableContentConfig config) 
	{
		return null;
	}

	@Override
	public String visit(PdfChartContentConfig config) 
	{
		StringBuffer result = new StringBuffer(); 
		divOpen(result, "content-chart"); 
		divClose(result); 
		
		return result.toString();
	}

	@Override
	public String visit(PdfGisContentConfig config) 
	{
		StringBuffer result = new StringBuffer(); 
		divOpen(result, "content-gis"); 
		divClose(result); 
		
		return result.toString();
	}

	@Override
	public String visit(PdfPriorityListContentConfig config) 
	{
		StringBuffer result = new StringBuffer(); 
		divOpen(result, "content-prio"); 
		divClose(result); 
		
		return result.toString();
	}

	@Override
	public String visit(TableStyleConfig config) 
	{ return ""; }

	private void divOpen(StringBuffer result, String ... namedValues)
	{ tag(result, "div", namedValues); }
	
	private void tag(StringBuffer result, String tag, String[] namedValues)
	{
		result.append("<"); 
		result.append(tag); 
		attr(result, namedValues); 
		result.append(">"); 
	}
	
	private void attr(StringBuffer result, String[] namedValues)
	{
		for (int i = 0; i < namedValues.length - 1; i++)
			attr(result, namedValues[i], namedValues[i + 1]); 
	}
	
	private void attr(StringBuffer result, String name, String value)
	{ 
		if (value == null || value.equals(""))
			return; 

		value.replaceAll("'", "\""); 
		
		result.append(' '); 
		result.append(name); 
		result.append("='"); 
		result.append(value); 
		result.append("'"); 
	}

	private void divClose(StringBuffer result)
	{ result.append("</div>"); } 
}
