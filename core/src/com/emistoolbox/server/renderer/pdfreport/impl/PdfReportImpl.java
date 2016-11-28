package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.server.renderer.pdfreport.EmisPageGroup;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import java.util.ArrayList;
import java.util.List;

public class PdfReportImpl implements PdfReport
{
    private EmisPdfReportConfig reportConfig;
    private List<EmisPdfPage> pages = new ArrayList<EmisPdfPage>();
    private EmisPageGroup group = new PageGroupImpl(); 

    public void addPage(EmisPdfPage page)
    { this.pages.add(page); }

    public List<EmisPdfPage> getPages()
    { return this.pages; }
    
    public int getPageCount()
    { return pages.size(); } 

    public EmisPdfReportConfig getReportConfig()
    { return this.reportConfig; }

    public void setReportConfig(EmisPdfReportConfig reportConfig)
    { this.reportConfig = reportConfig; }

	@Override
	public EmisPageGroup getPageGroup() 
	{ return group; }

	@Override
	public void setPageGroup(EmisPageGroup group) 
	{ this.group = group; } 
}
