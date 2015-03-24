package com.emistoolbox.common;

import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.analysis.impl.ReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import java.io.Serializable;

public class EmisReportModuleData implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String datasetName; 
    private EmisMeta model;
    private EmisReportConfig reportConfig = new ReportConfig();

    public String getDatasetName()
    { return datasetName; } 
    
    public void setDatasetName(String datasetName)
    { this.datasetName = datasetName; } 
    
    public EmisMeta getModel()
    { return this.model; }

    public void setModel(EmisMeta model)
    { this.model = model; }

    public EmisReportConfig getReportConfig()
    {
        if (reportConfig == null)
            reportConfig = new ReportConfig(); 
        
        return this.reportConfig;
    }

    public void setReportConfig(EmisReportConfig reportConfig)
    { this.reportConfig = reportConfig; }
}
