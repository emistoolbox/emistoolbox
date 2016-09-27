package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.renderer.pdfreport.EmisReportBaseConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;

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
}
