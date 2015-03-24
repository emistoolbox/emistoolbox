package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import java.util.List;

public abstract interface PdfReport
{
    public abstract PdfReportConfig getReportConfig();

    public abstract void setReportConfig(PdfReportConfig paramPdfReportConfig);

    public abstract void addPage(PdfPage paramPdfPage);

    public abstract List<PdfPage> getPages();
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.PdfReport JD-Core
 * Version: 0.6.0
 */