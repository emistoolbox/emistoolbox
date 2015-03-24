package com.emistoolbox.common.results;

import java.util.List;

import com.emistoolbox.common.excelMerge.ExcelReportConfig;

public interface ExcelReportMetaResult extends MetaResult
{
    public ExcelReportConfig getReportConfig(); 
    public void setReportConfig(ExcelReportConfig config); 

    public List<String> getHeaders(); 
    public void setHeaders(List<String> headers); 
}
