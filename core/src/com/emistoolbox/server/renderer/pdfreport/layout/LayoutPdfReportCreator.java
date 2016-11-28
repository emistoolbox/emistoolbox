package com.emistoolbox.server.renderer.pdfreport.layout;


import org.apache.commons.lang.StringUtils;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.server.renderer.pdfreport.EmisPageGroup;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.impl.BasePdfReportCreator;
import com.emistoolbox.server.util.TemplateEngine;

public class LayoutPdfReportCreator extends BasePdfReportCreator<LayoutPdfReportConfig>
{
	@Override
	protected void addEntityPages(EmisEntity entity, int[] ids, String[] names, int totalPages, EmisPageGroup group, TemplateEngine templates) 
	{
		init(entity, ids, names);
		boolean firstPage = true; 

		templates.put("group_pageTotal", config.getPages().size());
		
		int pageIndex = 0; 
		for (LayoutPageConfig pageConfig : config.getPages())
		{
			templates.put("group_page", pageIndex + 1);
			templates.put("report_page", reportResult.getPageCount() + 1);
			
			LayoutPage page = createPage(pageConfig, templates); 
			reportResult.addPage(page); 
			group.addPage(page); 
			if (firstPage)
				page.putText(PdfText.TEXT_GROUP, names[names.length -1]);
			
			pageIndex++; 
		}
	}
	
	private LayoutPage createPage(LayoutPageConfig pageConfig, TemplateEngine templates)
	{
		LayoutPage page = new LayoutPageImpl(); 
		renderTexts(page, pageConfig, templates); 
		
		page.setPageConfig(pageConfig);
		for (LayoutFrameConfig frameConfig : pageConfig.getFrames())
		{
			LayoutFrame frame = createFrame(frameConfig, templates);
			if (frame != null)
				page.addFrame(frame);
		}
		
		return page; 
	}
	
	private void renderTexts(TextSet target, TextSet source, TemplateEngine templates)
	{
		for (String key : source.getTextKeys())
			target.putText(key, templates.render(source.getText(key)), source.getFont(key), source.getAlignment(key)); 
	}
	
	private LayoutFrame createFrame(LayoutFrameConfig frameConfig, TemplateEngine templates)
	{
		LayoutFrame frame = new LayoutFrameImpl(frameConfig.getContentConfig() instanceof PdfTextContentConfig ? LayoutFrameConfig.TEXT_KEYS_WITH_BODY: LayoutFrameConfig.TEXT_KEYS); 
		frame.setFrameConfig(frameConfig);
		if (frameConfig.getContentConfig() != null)
		{
			PdfContent content = createContent(frameConfig.getContentConfig(), templates);
			frame.setContent(content);
			if (StringUtils.isEmpty(content.getTitle()))
				templates.remove("content_title");
			else
				templates.put("content_title", content.getTitle());
		}
		
		renderTexts(frame, frameConfig, templates);

		return frame;
	}

	@Override
	protected int getPageCountPerEntity()
	{ return config.getPages().size(); }
	
    protected PdfContent createContent(PdfContentConfig contentConfig, TemplateEngine templates)
    {
    	PdfContent result = super.createContent(contentConfig, templates);
    	if (result != null)
    		return result;
    	
        return result;
    }
}
