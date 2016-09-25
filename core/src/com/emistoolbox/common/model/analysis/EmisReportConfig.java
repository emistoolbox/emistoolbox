package com.emistoolbox.common.model.analysis;

import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaGroupEnum;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.util.NamedUtil;

import java.util.ArrayList;
import java.util.List;

public interface EmisReportConfig
{
    public int getDefaultDateIndex();
    public void setDefaultDateIndex(int paramInt);

    public List<EmisIndicator> getIndicators();
    public void setIndicators(List<EmisIndicator> paramList);

    public List<EmisPdfReportConfig> getPdfReports(); 
    public void setPdfReports(List<EmisPdfReportConfig> reports); 

    public List<ExcelReportConfig> getExcelReports();
    public void setExcelReports(List<ExcelReportConfig> reports); 
    
    public List<EmisMetaGroupEnum> getGroupEnums(); 
    public List<EmisMetaGroupEnum> getGroupEnums(EmisMetaEnum baseEnum); 
    public void setGroupEnums(List<EmisMetaGroupEnum> groupEnums); 
}

