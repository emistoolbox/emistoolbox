package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import java.io.File;
import java.io.IOException;

public abstract interface PdfReportWriter
{
    public abstract void writeReport(PdfReport paramPdfReport, File paramFile) throws IOException, PdfReportWriterException;
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.PdfReportWriter
 * JD-Core Version: 0.6.0
 */