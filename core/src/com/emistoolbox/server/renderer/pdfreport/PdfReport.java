package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import java.util.List;

public abstract interface PdfReport
{
    public EmisPdfReportConfig getReportConfig();
    public void setReportConfig(EmisPdfReportConfig config);

    public void addPage(EmisPdfPage page);
    public List<EmisPdfPage> getPages();
    public int getPageCount(); 
    
    public EmisPageGroup getPageGroup(); 
    public void setPageGroup(EmisPageGroup group); 
}
