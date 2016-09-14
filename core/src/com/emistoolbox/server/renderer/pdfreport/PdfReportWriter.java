package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.results.ReportMetaResult;

import java.io.File;
import java.io.IOException;

public abstract interface PdfReportWriter
{
    public abstract void writeReport(PdfReport paramPdfReport, File paramFile) throws IOException, PdfReportWriterException;
    
    public abstract void setDateInfo(ReportMetaResult metaInfo); 
}
