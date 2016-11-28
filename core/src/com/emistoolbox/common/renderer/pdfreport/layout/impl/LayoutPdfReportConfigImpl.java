package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.renderer.pdfreport.EmisReportBaseConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.util.Rectangle;

public class LayoutPdfReportConfigImpl extends EmisReportBaseConfigImpl implements LayoutPdfReportConfig, Serializable
{
	private List<LayoutPageConfig> pages = new ArrayList<LayoutPageConfig>();
	
	@Override
	public List<PdfContentConfig> getContentConfigs()
	{
		List<PdfContentConfig> result = new ArrayList<PdfContentConfig>(); 
		for (LayoutPageConfig page : pages)
		{
			for (LayoutFrameConfig frame : page.getFrames())
			{
				PdfContentConfig content = frame.getContentConfig();
				if (content != null)
					result.add(content); 
			}
		}
		
		return result; 
	}

	@Override
	public List<LayoutPageConfig> getPages() 
	{ return pages; }

	@Override
	public void setPages(List<LayoutPageConfig> pages) 
	{ this.pages = pages; } 

	@Override
	public void addPage(LayoutPageConfig page) 
	{ pages.add(page); }

	@Override
	public void addContentConfig(PdfContentConfig contentConfig, EmisMetaEntity entityType) 
	{
        if (!allowContentConfig(contentConfig))
        	return; 
        
        if (pages.size() == 0)
        {
			LayoutPageConfig pageConfig = new LayoutPageConfigImpl();
			pageConfig.putText(PdfText.TEXT_TITLE, "New Page");
			pages.add(pageConfig);
        }

		LayoutFrameConfig frame = new LayoutFrameConfigImpl(contentConfig instanceof PdfTextContentConfig); 
		if (contentConfig instanceof PdfMetaResultContentConfig && !(contentConfig instanceof PdfPriorityListContentConfig))
			frame.putText(PdfText.TEXT_TITLE, "{content_title}"); 
		
		frame.setContentConfig(contentConfig); 
		frame.setPosition(getDefaultPosition());
		
		pages.get(0).addFrame(frame);

        if (getEntityType() == null)
            setEntityType(entityType);
	}

	private Rectangle getDefaultPosition()
	{ return new Rectangle(0, 0, 250, 200); }
}
