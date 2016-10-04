package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.ui.pdf.LayoutFrameConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPageConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPdfReportEditor;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.util.Point;
import com.emistoolbox.common.util.Rectangle;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class LayoutPageEditor extends ScrollPanel implements EmisEditor<LayoutPageConfig> 
{
	private LayoutPageConfig pageConfig; 
	
	private AbsolutePanel uiPage = new AbsolutePanel(); 
	private HTML uiNoContent= new HTML("<p>This page has no content.</p><p>To add new content, click on the 'New Content' frame.</p>"); 

	private double panelToPageScale = 1.0;
	private int xOffset; 
	private int yOffset; 
	
	private PageSize pageSize; 
	private PageOrientation pageOrientation; 

	private LayoutPageConfigProperties uiPageProps = new LayoutPageConfigProperties(); 
	private LayoutFrameConfigProperties uiFrameProps = new LayoutFrameConfigProperties(); 

	private LayoutFrameWidget uiFrameCurrent = null; 
	private LayoutPdfReportEditor reportEditor = null; 
	
	public LayoutPageEditor(LayoutPdfReportEditor editor)
	{
		setWidth("100%"); 
		setHeight("100%"); 
		getElement().getStyle().setBackgroundColor("#ccc");
		add(uiPage); 

		this.reportEditor = editor; 
		
		set(null); 
	}

	public LayoutPdfReportEditor getReportEditor()
	{ return reportEditor; } 
	
	public LayoutPageConfigProperties getPageConfigProps()
	{ return uiPageProps; } 
	
	public LayoutFrameConfigProperties getFrameConfigProps()
	{ return uiFrameProps; } 

	public void selectFrame(LayoutFrameWidget uiFrame)
	{
		if (uiFrameCurrent != null)
		{
			uiFrameCurrent.commit(); 
			uiFrameCurrent.removeStyleName("selected");
		}
		
		uiFrameCurrent = uiFrame; 
		if (uiFrame != null)
		{
			uiFrameProps.set(uiFrame.get());
			uiFrameProps.addStyleName("selected");
		}
		else
			uiFrameProps.set(null);
	}
	
	public void moveFrame(LayoutFrameWidget frame, int xOffset, int yOffset)
	{ uiPage.setWidgetPosition(frame, uiPage.getWidgetLeft(frame) + xOffset, uiPage.getWidgetTop(frame) + yOffset); }
	
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
		
		uiPage.setPixelSize((int) Math.round(size.x), (int) Math.round(size.y));
		uiNoContent.setPixelSize((int) Math.round(size.x), (int) Math.round(size.y));
		
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
			setWidget(uiNoContent); 
		else
		{
			setWidget(uiPage);
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
	}
	
	public void updatePageIndex(int index, int total)
	{
		for (int i = 0; i < uiPage.getWidgetCount(); i++)
		{
			Widget w = uiPage.getWidget(i); 
//			if (w instanceof LayoutFrameWidget)
//				((LayoutFrameWidget) w).updatePageIndex(index, total);
		}
	}
}
