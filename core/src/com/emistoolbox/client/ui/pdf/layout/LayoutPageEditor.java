package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.util.Point;
import com.emistoolbox.common.util.Rectangle;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public class LayoutPageEditor extends SimplePanel implements EmisEditor<LayoutPageConfig> 
{
	private LayoutPageConfig pageConfig; 
	private HTML uiLayout = new HTML(); 
	private Object gridstack; 
	
	private double gridCellWidth;
	private double gridCellHeight; 
	private int gridMaxRows;
	
	public LayoutPageEditor()
	{
		uiLayout.setStyleName("gridstack");
	}
	
	public void setSizes(PageSize pageSize, PageOrientation pageOrientation)
	{ 
		int gridColumns = 12; 
		int gridCellSpacing = 10; 
		
		Point size = PdfText.getPageSize(pageSize, pageOrientation); 
		Rectangle margins = PdfText.getMargins(pageSize);
		
		double layoutWidth = size.x - margins.getLeft() - margins.getRight(); 
		gridCellWidth = (layoutWidth - (gridColumns - 1 * gridCellSpacing)) / gridColumns;
		
		double layoutHeight = size.y - margins.getTop() - margins.getBottom(); 
		int gridRows = (int) Math.round(Math.floor(layoutHeight + gridCellSpacing) / (gridCellWidth + gridCellSpacing));
		gridCellHeight = (layoutHeight - (gridRows - 1 * gridCellSpacing)) / gridRows; 
	}
	
	@Override
	public void commit() 
	{}

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
		updateUi(); 
	}

	private void initGridstack()
	{ gridstack = gridstackInit(".gridstack"); }
	
	private int getGridX(double pageX)
	{ return (int) Math.round(pageX / gridCellWidth); }
	
	private int getGridY(double pageY)
	{ return (int) Math.round(pageY / gridCellHeight); }
	
	private double getPageX(int gridX)
	{ return gridX * gridCellWidth; } 
	
	private double getPageY(int gridY)
	{ return gridY * gridCellHeight; }
	
	private void updateUi()
	{
		if (pageConfig == null || pageConfig.getFrames().size() == 0)
			setWidget(new HTML("No frames - display layout help")); 
		else
		{
			uiLayout.setHTML("");
			setWidget(uiLayout); 
			initGridstack(); 
			
			gridstackBatchUpdate(gridstack);
			// Set all existing frames. 
			for (LayoutFrameConfig frame : pageConfig.getFrames())
				addFrame(frame, false); 
			gridstackCommit(gridstack);
		}
	}

	public void addFrame(LayoutFrameConfig frame, boolean isNew)
	{
		if (isNew)
			gridstackAddFrame(gridstack, "" + frame.hashCode(), 0, 0, 2, 2, isNew); 
		else
		{
			Rectangle pos = frame.getPosition(); 
			gridstackAddFrame(gridstack, "" + frame.hashCode(), getGridX(pos.getLeft()), getGridY(pos.getRight()), getGridX(pos.getWidth()), getGridY(pos.getHeight()), isNew); 
		}
	}
	
	private native void gridstackBatchUpdate(Object gs) /*-{
		gs.batchUpdate(); 
	}-*/; 
	
	private native void gridstackCommit(Object gs) /*-{
		gs.commit(); 
	}-*/;
	
    private native Object gridstackInit(String jQuerySelector) /*-{
		var result = $wnd.jQuery(jQuerySelector).gridstack({
			alwaysShowResizeHandle: /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent), 
			verticalMargin: 6			
		}); 
		
		return result; 
	}-*/;
    
    private native Object gridstackAddFrame(Object gs, String id, int x,  int y, int width, int height, boolean isNew) /*-{
    	return gs.addWidget(document.createElement("<div>", x, y, width, height, isNew, null, null, null, null, id)); 
    }-*/; 
}

