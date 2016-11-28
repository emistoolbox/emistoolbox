package com.emistoolbox.client.ui.pdf.layout;
			
import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.ui.pdf.LayoutFrameConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPageConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPdfReportEditor;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.CSSCreator;
import com.emistoolbox.common.util.Point;
import com.emistoolbox.common.util.Rectangle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
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
	
	private PageSize pageSize; 
	private PageOrientation pageOrientation; 

	private LayoutPageConfigProperties uiPageProps;  
	private LayoutFrameConfigProperties uiFrameProps; 

	private LayoutFrameWidget uiFrameCurrent = null;
	private LayoutPdfReportEditor reportEditor = null; 
	
	private HTML uiTitle = new HTML(); 
	private HTML uiSubtitle = new HTML(); 
	private HTML uiFooter = new HTML(); 
	
	public LayoutPageEditor(LayoutPdfReportEditor editor)
	{
		setWidth("100%"); 
		setHeight("100%"); 

		uiPageProps = new LayoutPageConfigProperties(editor);
		uiFrameProps = new LayoutFrameConfigProperties(editor); 
		
		uiFrameProps.setMoveToPageUi(editor.getMoveToPageUi());

		add(uiOuterPage); 
		uiOuterPage.add(uiPage);
		uiOuterPage.addStyleName("layout-outer-page");
		uiPage.addStyleName("layout-inner-page");

		this.reportEditor = editor; 
		
		uiPageProps.addValueChangeHandler(new ValueChangeHandler<LayoutPageConfig>() {
			@Override
			public void onValueChange(ValueChangeEvent<LayoutPageConfig> event) 
			{
				set(get());
				reportEditor.showPageProperties(); 
			}
		}); 
		
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
		frame.updateFrameSize(width, height);
		
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
		pos.setLeft(getPageX(uiPage.getWidgetLeft(uiFrame)));
		pos.setRight(pos.getLeft() + getPageX(uiFrame.getOffsetWidth()));
		pos.setTop(getPageY(uiPage.getWidgetTop(uiFrame)));
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
		uiPage.add(uiTitle);
		uiPage.add(uiSubtitle);
		uiPage.add(uiFooter);
		uiTitle.setWidth("100%");
		uiSubtitle.setWidth("100%");
		uiFooter.setWidth("100%");
		
		uiFrameProps.set(null);
		if (pageConfig == null || pageConfig.getFrames().size() == 0)
		{
			uiPage.add(uiNoContent); 
			uiNoContent.setVisible(true);
		}
		else
		{
			for (LayoutFrameConfig frame : pageConfig.getFrames())
				addFrameUi(frame); 
		}

		// Set and position texts.
		int titleHeight = 0; 
		int subtitleHeight = 0; 
		int footerHeight = 0; 
		
		if (pageConfig != null)
		{
			titleHeight = setTextPreview(uiTitle, PdfText.TEXT_TITLE); 
			uiTitle.getElement().getStyle().setPosition(Position.ABSOLUTE);
			
			subtitleHeight = setTextPreview(uiSubtitle, PdfText.TEXT_SUBTITLE);
			uiSubtitle.getElement().getStyle().setPosition(Position.ABSOLUTE);

			footerHeight = setTextPreview(uiFooter, PdfText.TEXT_FOOTER);
			uiFooter.getElement().getStyle().setPosition(Position.ABSOLUTE);
			
			// Position elements. 
			if (titleHeight > 0)
				uiTitle.getElement().getStyle().setTop(0.0, Unit.PX);
	
			if (subtitleHeight > 0)
				uiSubtitle.getElement().getStyle().setTop(titleHeight, Unit.PX);
			
			if (footerHeight > 0)
				uiFooter.getElement().getStyle().setTop(uiPage.getOffsetHeight() - footerHeight * 4 / 3, Unit.PX);
		}

		uiTitle.setVisible(titleHeight > 0); 
		uiSubtitle.setVisible(subtitleHeight > 0);
		uiFooter.setVisible(subtitleHeight > 0);
	}

	private int setTextPreview(HTML html, String key)
	{
		String text = pageConfig.getText(key); 
		if (text == null || text.isEmpty())
		{
			html.setHTML("");
			return 0; 
		}
		
		ChartFont font = pageConfig.getFont(key); 
		if (font == null)
			font = ChartFont.DEFAULT_FONT; 

		html.setHTML(HtmlPreview.getHtmlText(pageConfig, key));
		return font.getSize() * 15 / 10; 
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
		
		int width = getPanelX(pos.getWidth()); 
		int height = getPanelY(pos.getHeight()); 
		uiFrame.setPixelSize(width, height); 

		uiPage.add(uiFrame, getPanelX(pos.getLeft()), getPanelY(pos.getTop()));
		uiFrame.updateFrameStyle();
		uiFrame.updateFrameSize(width, height); 
		
		uiNoContent.setVisible(false);
		
		selectFrame(uiFrame);
	}

	public void updatePageIndex(int index, int total)
	{ uiFrameProps.updatePageIndex(index, total); }
	
	public void updatePageStyle(LayoutPageConfig config)
	{
		uiPage.getElement().getStyle().setBackgroundColor(CSSCreator.getCss(config.getBackgroundColour()));
		uiOuterPage.getElement().getStyle().setBackgroundColor(CSSCreator.getCss(config.getBackgroundColour().darker()));
	}
}
