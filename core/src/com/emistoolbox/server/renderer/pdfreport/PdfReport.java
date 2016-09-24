package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import java.util.List;

public abstract interface PdfReport
{
    public abstract EmisPdfReportConfig getReportConfig();
    public abstract void setReportConfig(EmisPdfReportConfig config);

    public abstract void addPage(EmisPdfPage page);
    public abstract List<EmisPdfPage> getPages();
}
