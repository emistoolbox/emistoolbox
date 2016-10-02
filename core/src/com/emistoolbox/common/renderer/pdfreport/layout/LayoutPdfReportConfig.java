package com.emistoolbox.common.renderer.pdfreport.layout;

import java.util.List;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;

/** Frame base layout configuration. */ 
public interface LayoutPdfReportConfig extends EmisPdfReportConfig
{
	public static final String PDF_REPORT_VERSION = "layout";
	
	public List<LayoutPageConfig> getPages();
	public void setPages(List<LayoutPageConfig> pages);
	public void addPage(LayoutPageConfig page);
}
