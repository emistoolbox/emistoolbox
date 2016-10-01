package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.ui.pdf.LayoutFrameConfigProperties;
import com.emistoolbox.client.ui.pdf.LayoutPageConfigProperties;
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

	private double panelToPageScale = 1.0;
	private int xOffset; 
	private int yOffset; 
	
	private PageSize pageSize; 
	private PageOrientation pageOrientation; 

	private LayoutPageConfigProperties uiPageProps = new LayoutPageConfigProperties(); 
	private LayoutFrameConfigProperties uiFrameProps = new LayoutFrameConfigProperties(); 

	private LayoutFrameWidget uiFrameCurrent = null; 
	public LayoutPageEditor()
	{
		setWidth("100%"); 
		setHeight("100%"); 
		getElement().getStyle().setBackgroundColor("#ccc");
		add(uiPage); 
	}

	public LayoutPageConfigProperties getPageConfigProps()
	{ return uiPageProps; } 
	
	public LayoutFrameConfigProperties getFrameConfigProps()
	{ return uiFrameProps; } 

	public void selectFrameConfig(LayoutFrameWidget uiFrame)
	{
		if (uiFrameCurrent != null)
		{
			uiFrameCurrent.commit(); 
			uiFrameCurrent.removeStyleName("selected");
		}
		
		uiFrameProps.set(uiFrame.get());
		uiFrameProps.addStyleName("selected");
		uiFrameCurrent = uiFrame; 
	}
	
	public void moveFrame(LayoutFrameWidget frame, int xOffset, int yOffset)
	{
		uiPage.setWidgetPosition(frame, uiPage.getWidgetLeft(frame) + xOffset, uiPage.getWidgetTop(frame) + yOffset); 
	}
	
	public void setSizes(PageSize pageSize, PageOrientation pageOrientation)
	{ 
		this.pageSize = pageSize;  
		this.pageOrientation = pageOrientation; 
		
		Point size = PdfText.getPageSize(pageSize, pageOrientation);
		Rectangle margins = PdfText.getMargins(pageSize); 
		
		uiPage.setPixelSize((int) Math.round(size.x), (int) Math.round(size.y));
		
		xOffset = (int) Math.round(margins.getLeft());
		yOffset = (int) Math.round(margins.getTop());
	}
	
	@Override
	public void commit() 
	{
		for (int i = 0; i < uiPage.getWidgetCount(); i++)
		{
			Widget w = uiPage.getWidget(i); 
			if (!(w instanceof LayoutFrameWidget))
				continue; 
			
			LayoutFrameWidget uiFrame = (LayoutFrameWidget) w; 

			Rectangle pos = new Rectangle(); 
			pos.setLeft(getPageX(uiPage.getWidgetLeft(uiFrame) - xOffset));
			pos.setRight(pos.getLeft() + getPageX(uiFrame.getOffsetWidth()));
			pos.setTop(getPageY(uiPage.getWidgetTop(uiFrame) - yOffset));
			pos.setBottom(pos.getTop() + getPageY(uiFrame.getOffsetHeight())); 

			LayoutFrameConfig config = uiFrame.get(); 
			config.setPosition(pos);
		}
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
		if (pageConfig == null || pageConfig.getFrames().size() == 0)
			setWidget(new HTML("No frames - display layout help")); 
		else
		{
			uiPage.clear(); 
			for (LayoutFrameConfig frame : pageConfig.getFrames())
				addFrame(frame, false); 
		}
	}

	public void addFrame(LayoutFrameConfig frame, boolean isNew)
	{
		LayoutFrameWidget uiFrame = new LayoutFrameWidget(this);
		uiFrame.set(frame);
		if (isNew)
			frame.setPosition(new Rectangle(0, 0, 100, 50));
	
		Rectangle pos = frame.getPosition();
		uiFrame.setPixelSize(getPanelX(pos.getWidth()), getPanelY(pos.getHeight())); 
		uiPage.add(uiFrame, xOffset + getPanelX(pos.getLeft()), yOffset + getPanelY(pos.getTop()));
	}
}
