package com.emistoolbox.server.renderer.pdfreport.layout;


import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.impl.BasePdfReportCreator;

public class LayoutPdfReportCreator extends BasePdfReportCreator<LayoutPdfReportConfig>
{
	@Override
	protected void addEntityPages(EmisEntity entity, int[] ids, String[] names, int totalPages) 
	{
		init(entity, ids, names);
		for (LayoutPageConfig pageConfig : config.getPages())
			reportResult.addPage(createPage(pageConfig)); 
	}
	
	private LayoutPage createPage(LayoutPageConfig pageConfig)
	{
		LayoutPage page = new LayoutPageImpl(); 
		page.setPageConfig(pageConfig);
		for (LayoutFrameConfig frameConfig : pageConfig.getFrames())
		{
			LayoutFrame frame = createFrame(frameConfig);
			if (frame != null)
				page.addFrame(frame);
		}
		
		return page; 
	}
	
	private LayoutFrame createFrame(LayoutFrameConfig frameConfig)
	{
		LayoutFrame frame = new LayoutFrameImpl(); 
		frame.setFrameConfig(frameConfig);
		
		if (frameConfig.getContentConfig() != null)
			frame.setContent(createContent(frameConfig.getContentConfig()));
		
		return frame; 
	}

	@Override
	protected int getPageCountPerEntity()
	{ return config.getPages().size(); }
	
    protected PdfContent createContent(PdfContentConfig contentConfig)
    {
    	PdfContent result = super.createContent(contentConfig); 
    	if (result != null)
    		return result; 
    	
        return result; 
    }
}
