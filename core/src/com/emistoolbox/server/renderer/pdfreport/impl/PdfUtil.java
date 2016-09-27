package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportCreator;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPdfReportCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
