package com.emistoolbox.common.results.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.results.ExcelReportMetaResult;

public class ExcelReportMetaResultImpl extends MetaResultImpl implements ExcelReportMetaResult, Serializable
{
    private ExcelReportConfig excelReportConfig; 
    private List<String> headers = new ArrayList<String>(); 
    
    public ExcelReportConfig getReportConfig()
    { return excelReportConfig; } 

    public void setReportConfig(ExcelReportConfig reportConfig)
    { this.excelReportConfig = reportConfig; } 
    
    public List<String> getHeaders()
    { return headers; } 
    
    public void setHeaders(List<String> headers)
    { this.headers = headers; }
}
