package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.ui.pdf.SimpleTableStyleEditor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
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
import com.emistoolbox.common.renderer.pdfreport.impl.SimpleTableStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.util.Point;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public class LayoutFrameWidget extends SimplePanel implements EmisEditor<LayoutFrameConfig>
{
	private LayoutPageEditor pageEditor; 
	private LayoutFrameConfig frameConfig; 
	private HTML uiContent = new HTML(); 
	
	private int containerWidth = 0; 
	private int containerHeight = 0; 
	
	public LayoutFrameWidget(final LayoutPageEditor pageEditor)
	{
		this.pageEditor = pageEditor; 
		
		setStyleName("layoutFrame");

		add(uiContent); 

		MouseHandler mouseHandler = new MouseHandler(); 
		addDomHandler(mouseHandler, MouseDownEvent.getType()); 
		addDomHandler(mouseHandler, MouseUpEvent.getType());
		addDomHandler(mouseHandler, MouseMoveEvent.getType());
		addDomHandler(mouseHandler, MouseOutEvent.getType()); 
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

	public class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler
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
		public void onMouseOut(MouseOutEvent event) 
		{ finish(event); } 

		@Override
		public void onMouseUp(MouseUpEvent event) 
		{ finish(event); }
		
		public void finish(MouseEvent event) 
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
			if (event == null || lastX == null || lastY == null)
				return;
			
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
	
	public void updateFrameSize(int width, int height)
	{
		this.containerWidth = width; 
		this.containerHeight = height; 
		
		width -= frameConfig.getPadding().getRight() + frameConfig.getPadding().getLeft(); 
		height -= frameConfig.getPadding().getTop() + frameConfig.getPadding().getBottom();

		if (frameConfig.getBorders().getLeft() != null)
			width -= frameConfig.getBorders().getLeft().getWidth(); 
		if (frameConfig.getBorders().getRight() != null)
			width -= frameConfig.getBorders().getRight().getWidth(); 
		if (frameConfig.getBorders().getTop() != null)
			height-= frameConfig.getBorders().getTop().getWidth(); 
		if (frameConfig.getBorders().getBottom() != null)
			height -= frameConfig.getBorders().getBottom().getWidth();	

		uiContent.setPixelSize(width, height);

		int textHeight = getTextHeight(frameConfig, PdfText.TEXT_TITLE) + getTextHeight(frameConfig, PdfText.TEXT_SUBTITLE) + getTextHeight(frameConfig, PdfText.TEXT_FOOTER); 
		// TODO - size the content div to height less text spacing. 
	}
	
	public void updateFrameStyle()
	{ 
		uiContent.getElement().setAttribute("style", CSSCreator.getCssAsString(frameConfig));
		uiContent.setHTML(HtmlPreview.getHtmlText(frameConfig, PdfText.TEXT_TITLE) + HtmlPreview.getHtmlText(frameConfig, PdfText.TEXT_SUBTITLE) + getContentHtml(frameConfig) + HtmlPreview.getHtmlText(frameConfig, PdfText.TEXT_FOOTER));
		
		updateFrameSize(containerWidth, containerHeight); 
	}

	private int getTextHeight(TextSet texts, String key)
	{
		String text = texts.getText(key); 
		if (text == null || text.equals(""))
			return 0; 
		
		ChartFont font = texts.getFont(key);
		return font == null ? 12 : font.getSize() * 15 / 10; 
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
		HtmlPreview.divOpen(result, "class", "content-text");
		
		String body = frameConfig.getText(PdfText.TEXT_BODY); 
		if (body != null && !body.equals(""))
		{
			HtmlPreview.divOpen(result, "style", CSSCreator.getCssAsString(frameConfig.getFont(PdfText.TEXT_BODY))); 
			result.append(body);
			HtmlPreview.divClose(result); 
		}
		
		HtmlPreview.divClose(result); 
		
		return result.toString();
	}

	@Override
	public String visit(PdfVariableContentConfig config) 
	{
		int rows = config.getItemCount() + 1; 
		String[][] data = new String[rows][2];
		data[0][0] = "Name"; 
		data[0][1] = "Value"; 
		
		for (int i = 0; i < config.getItemCount(); i++)
			data[i + 1] = new String[] { config.getItemTitle(i), config.getItemVariable(i) }; 
		
		return SimpleTableStyleEditor.getTablePreview(data, (SimpleTableStyle) config.getTableStyle()); 
	}

	private String getPreviewImage(String key, int height)
	{ return "<img src='css/img/" + key + ".png' width='100%' height='" + height + "px'>"; }
	
	private static final String[] chartTypes = new String[] { "bar", "pie", "stacked", "scaled", "line"}; 
	@Override
	public String visit(PdfChartContentConfig config) 
	{
		int dim = config.getMetaResult().getDimensionCount();
		int chartType = config.getChartType();
		
		String id = "content-chart-" + chartTypes[chartType];
		if (dim > 1 && (chartType == ChartType.LINE.ordinal() || chartType == ChartType.BAR.ordinal()))
			id += "-2d";

		return getPreviewImage(id, 100); 
	}

	@Override
	public String visit(PdfGisContentConfig config)
	{ return getPreviewImage("content-gis", 100); }

	@Override
	public String visit(PdfPriorityListContentConfig config) 
	{
		EmisTableStyle style = config.getTableStyle(); 
		if (style instanceof SimpleTableStyle)
		{
			PriorityMetaResult meta = config.getMetaResult(); 
			
			int rowCount = 3; 
			int colCount = meta.getMetaResultValueCount() + meta.getAdditionalFields().length + 1; 
			String[][] data = new String[rowCount ][colCount]; 

			String[] headers = new String[colCount]; 
			String[] fields = meta.getAdditionalFields();
			headers[0] = ""; 
			for (int i = 0; i < fields.length; i++)
				headers[i + 1] = fields[i]; 

			for (int i = 0; i < meta.getMetaResultValueCount(); i++)
				headers[1 + i + fields.length] = meta.getMetaResultValue(i).getName(true); 

			data[0] = headers; 
			
			for (int row = 1; row < rowCount; row++)
			{
				String[] cells = new String[colCount]; 
				data[row] = cells; 
				
				cells[0] = row + ": Location " + (char) (64 + row); 
				for (int col = 1; col < colCount; col++)
				{
					if (col - 1 < fields.length)
						cells[col] = "value";
					else
					{
						String format = config.getMetaResult().getMetaResultValues().get(col - 1 - fields.length).getFormat(); 
						cells[col] = EmisUtils.getFormattedValue(format, col * 10); 
					}
				}
			}
			
			return SimpleTableStyleEditor.getTablePreview(data, (SimpleTableStyle) config.getTableStyle()); 
		}
		else
			return getPreviewImage("content-table", 100); 
	}

	@Override
	public String visit(TableStyleConfig styleConfig) 
	{
		EmisTableStyle style = styleConfig.getTableStyle(); 
		if (style instanceof SimpleTableStyle)
			return SimpleTableStyleEditor.getTablePreview((SimpleTableStyle) style);
		
		return "(no preview)";
	}
}
