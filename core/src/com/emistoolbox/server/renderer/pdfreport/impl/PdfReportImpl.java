package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import java.util.ArrayList;
import java.util.List;

public class PdfReportImpl implements PdfReport
{
    private PdfReportConfig reportConfig;
    private List<PdfPage> pages = new ArrayList();

    public void addPage(PdfPage page)
    {
        this.pages.add(page);
    }

    public List<PdfPage> getPages()
    {
        return this.pages;
    }

    public PdfReportConfig getReportConfig()
    {
        return this.reportConfig;
    }

    public void setReportConfig(PdfReportConfig reportConfig)
    {
        this.reportConfig = reportConfig;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.impl.PdfReportImpl
 * JD-Core Version: 0.6.0
 */