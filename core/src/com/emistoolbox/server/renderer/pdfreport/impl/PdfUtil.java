package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportCreator;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPdfReportCreator;

public class PdfUtil
{
    public static PdfReport getPdfReport(ReportMetaResult metaResult, EmisDataSet dataSet)
    {
        EmisPdfReportConfig config = metaResult.getReportConfig();
        PdfReport result = getPdfReportCreator(config).create(metaResult, dataSet);
        result.setReportConfig(config);
        
        return result; 
    }
    
    private static PdfReportCreator getPdfReportCreator(EmisPdfReportConfig config)
    {
    	if (config instanceof LayoutPdfReportConfig)
    		return new LayoutPdfReportCreator(); 
    	else if (config instanceof PdfReportConfig)
    		return new LegacyPdfReportCreator(); 
    	else
    		throw new IllegalArgumentException("Unknown PDF report configuration."); 
    }
}
