package com.emistoolbox.client.ui.pdf.layout;
			
import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.ui.pdf.LayoutFrameConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPageConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPdfReportEditor;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.emistoolbox.common.util.Point;
import com.emistoolbox.common.util.Rectangle;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class LayoutPageEditor extends ScrollPanel implements EmisEditor<LayoutPageConfig> 
{
	static final int MIN_FRAME_SIZE = 50;
	private LayoutPageConfig pageConfig; 
	
	private AbsolutePanel uiOuterPage = new AbsolutePanel(); 
	private AbsolutePanel uiPage = new AbsolutePanel(); 
	private HTML uiNoContent= new HTML("<p>This page has no content.</p><p>To add new content, click on the 'New Content' frame.</p>"); 

	private double panelToPageScale = 1.0;
	private int xOffset; 
	private int yOffset; 
	
	private PageSize pageSize; 
	private PageOrientation pageOrientation; 

	private LayoutPageConfigProperties uiPageProps = new LayoutPageConfigProperties(); 
	private LayoutFrameConfigProperties uiFrameProps= new LayoutFrameConfigProperties(); 

	private LayoutFrameWidget uiFrameCurrent = null; 
	private LayoutPdfReportEditor reportEditor = null; 
	
	public LayoutPageEditor(LayoutPdfReportEditor editor)
	{
		setWidth("100%"); 
		setHeight("100%"); 

		add(uiOuterPage); 
		uiOuterPage.add(uiPage);
		uiOuterPage.addStyleName("layout-outer-page");
		uiPage.addStyleName("layout-inner-page");

		this.reportEditor = editor; 
		
		uiFrameProps.addValueChangeHandler(new ValueChangeHandler<LayoutFrameConfig>() {
			@Override
			public void onValueChange(ValueChangeEvent<LayoutFrameConfig> event) 
			{
				if (uiFrameCurrent != null)
					uiFrameCurrent.updateFrameStyle(); 
			}
		}); 
		
		set(null);
	}

	public LayoutPdfReportEditor getReportEditor()
	{ return reportEditor; } 
	
	public LayoutPageConfigProperties getPageConfigProps()
	{ return uiPageProps; } 
	
	public LayoutFrameConfigProperties getFrameConfigProps()
	{ return uiFrameProps; } 

	public LayoutFrameWidget getCurrentFrame()
	{ return uiFrameCurrent; }
	
	public void selectFrame(LayoutFrameWidget uiFrame)
	{
		if (uiFrameCurrent != null)
		{
			uiFrameProps.commit(); 
			uiFrameCurrent.removeStyleName("selected");
		}
		
		uiFrameCurrent = uiFrame; 
		if (uiFrameCurrent != null)
		{
			uiFrameProps.set(uiFrame.get());
			uiFrameCurrent.addStyleName("selected");
		}
		else
			uiFrameProps.set(null);
		
		reportEditor.showFrameProperties(); 
	}
	
	private int getWidth(Widget w)
	{ return w.getElement().getClientWidth(); } 

	private int getHeight(Widget w)
	{ return w.getElement().getClientHeight(); } 

	public Point resizeFrame(LayoutFrameWidget frame, int widthDelta, int heightDelta)
	{
		int left = uiPage.getWidgetLeft(frame); 
		int top = uiPage.getWidgetTop(frame); 

		int width = getWidth(frame) + widthDelta; 
		int height = getHeight(frame) + heightDelta;
		
		if  (width < MIN_FRAME_SIZE)
			width = MIN_FRAME_SIZE; 
		
		if (height < MIN_FRAME_SIZE)
			height = MIN_FRAME_SIZE; 
		
		if (left + width > getWidth(uiPage))
			width = getWidth(uiPage) - left; 
		
		if (top + height > getHeight(uiPage))
			height = getHeight(uiPage) - top; 
		
		frame.setPixelSize(width, height);  
		
		return new Point(0, 0); 
	}
	
	public void positionFrame(LayoutFrameWidget frame, int x, int y)
	{ uiPage.setWidgetPosition(frame, x, y); }
	
	public Point moveFrame(LayoutFrameWidget frame, int xOffset, int yOffset)
	{
		int x = uiPage.getWidgetLeft(frame) + xOffset; 
		int y = uiPage.getWidgetTop(frame) + yOffset; 
		
		if (x < 0)
			x = 0; 
		
		if (y < 0)
			y = 0; 
		
		if (x >= uiPage.getOffsetWidth() - frame.getOffsetWidth())
			x = uiPage.getOffsetWidth() - frame.getOffsetWidth(); 
		
		if (y >= uiPage.getOffsetHeight() - frame.getOffsetHeight())
			y = uiPage.getOffsetHeight() - frame.getOffsetHeight(); 

		// Round x and y to next 10. 
		Point gridAdjustment = new Point(0, 0); // new Point(x % 10, y % 10); 

		positionFrame(frame, x - gridAdjustment.getIntX(), y - gridAdjustment.getIntY());
		
		return gridAdjustment; 
	}
	
	public void removeFrame(LayoutFrameWidget frame)
	{
		uiFrameProps.commit();
		commit(frame);  
		uiPage.remove(frame); 
	} 
	
	public void setSizes(PageSize pageSize, PageOrientation pageOrientation)
	{ 
		this.pageSize = pageSize;  
		this.pageOrientation = pageOrientation; 
		
		Point size = PdfText.getPageSize(pageSize, pageOrientation);
		Rectangle margins = PdfText.getMargins(pageSize); 

		uiOuterPage.setPixelSize(size.getIntX(), size.getIntY());
		uiOuterPage.setWidgetPosition(uiPage, margins.getIntLeft(), margins.getIntTop()); 
		uiPage.setPixelSize(size.getIntX() - margins.getIntLeft() - margins.getIntRight(), size.getIntY() - margins.getIntTop() - margins.getIntBottom());
		
		xOffset = (int) Math.round(margins.getLeft());
		yOffset = (int) Math.round(margins.getTop());
	}
	
	@Override
	public void commit() 
	{
		uiPageProps.commit(); 
		uiFrameProps.commit(); 
		
		for (int i = 0; i < uiPage.getWidgetCount(); i++)
		{
			Widget w = uiPage.getWidget(i); 
			if (!(w instanceof LayoutFrameWidget))
				continue; 

			commit((LayoutFrameWidget) w); 
		}
	}

	private void commit(LayoutFrameWidget uiFrame)
	{
		Rectangle pos = new Rectangle(); 
		pos.setLeft(getPageX(uiPage.getWidgetLeft(uiFrame) - xOffset));
		pos.setRight(pos.getLeft() + getPageX(uiFrame.getOffsetWidth()));
		pos.setTop(getPageY(uiPage.getWidgetTop(uiFrame) - yOffset));
		pos.setBottom(pos.getTop() + getPageY(uiFrame.getOffsetHeight())); 

		LayoutFrameConfig config = uiFrame.get(); 
		config.setPosition(pos);
	}
	
	@Override
	public LayoutPageConfig get() 
	{ 
		commit(); 
		return pageConfig; 
	} 

	@Override
	public void set(LayoutPageConfig pageConfig) 
	{
		this.pageConfig = pageConfig; 
		uiPageProps.set(pageConfig);
		updateUi(); 
	}

	public int getPanelX(double pageX)
	{ return (int) Math.round(pageX * panelToPageScale); }
	
	public int getPanelY(double pageY)
	{ return (int) Math.round(pageY * panelToPageScale); }
	
	public double getPageX(int panelX)
	{ return panelX / panelToPageScale; } 
	
	public double getPageY(int panelY)
	{ return panelY / panelToPageScale; }
	
	private void updateUi()
	{
		uiPage.clear(); 
		uiFrameProps.set(null);
		if (pageConfig == null || pageConfig.getFrames().size() == 0)
			uiPage.add(uiNoContent); 
		else
		{
			for (LayoutFrameConfig frame : pageConfig.getFrames())
				addFrameUi(frame); 
		}
	}

	public void addFrame(LayoutFrameConfig frame, boolean isNew)
	{
		if (isNew)
			frame.setPosition(new Rectangle(0, 0, 200, 100));

		pageConfig.addFrame(frame);
		addFrameUi(frame); 
	}
	
	public void addFrameUi(LayoutFrameConfig frame)
	{
		LayoutFrameWidget uiFrame = new LayoutFrameWidget(this);
		uiFrame.set(frame);
	
		Rectangle pos = frame.getPosition();
		uiFrame.setPixelSize(getPanelX(pos.getWidth()), getPanelY(pos.getHeight())); 
		uiPage.add(uiFrame, xOffset + getPanelX(pos.getLeft()), yOffset + getPanelY(pos.getTop()));
		uiFrame.updateFrameStyle();
	}

	public void updatePageIndex(int index, int total)
	{ uiFrameProps.updatePageIndex(index, total); }
	
	public void updatePageStyle(LayoutPageConfig config)
	{
		uiPage.getElement().getStyle().setBackgroundColor(CSSCreator.getCss(config.getBackgroundColour()));
		uiOuterPage.getElement().getStyle().setBackgroundColor(CSSCreator.getCss(config.getBackgroundColour().darker()));
	}
}
