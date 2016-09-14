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
