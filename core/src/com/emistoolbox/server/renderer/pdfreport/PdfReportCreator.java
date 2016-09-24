package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.server.model.EmisDataSet;

/** Interface to */ 
public interface PdfReportCreator<T extends EmisPdfReportConfig>
{
	public PdfReport create(ReportMetaResult metaResult, EmisDataSet dataSet);
}
