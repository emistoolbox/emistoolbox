package com.emistoolbox.common.model.analysis;

import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaGroupEnum;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.util.NamedUtil;

import java.util.ArrayList;
import java.util.List;

public abstract interface EmisReportConfig
{
    public abstract int getDefaultDateIndex();

    public abstract void setDefaultDateIndex(int paramInt);

    public abstract List<EmisIndicator> getIndicators();

    public abstract void setIndicators(List<EmisIndicator> paramList);

    public abstract List<PdfReportConfig> getReports();

    public abstract void setReports(List<PdfReportConfig> paramList);

    public abstract List<ExcelReportConfig> getExcelReports();
    
    public abstract void setExcelReports(List<ExcelReportConfig> reports); 
    
    public List<EmisMetaGroupEnum> getGroupEnums(); 

    public List<EmisMetaGroupEnum> getGroupEnums(EmisMetaEnum baseEnum); 

    public void setGroupEnums(List<EmisMetaGroupEnum> groupEnums); 
}

