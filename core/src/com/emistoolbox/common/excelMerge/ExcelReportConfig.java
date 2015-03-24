package com.emistoolbox.common.excelMerge;

import java.util.List;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.Named;

public interface ExcelReportConfig extends Named
{
    public boolean isReady(); 
    public String getTemplateFile(); 
    public void setTemplateFile(String filename); 
    
    public EmisMetaEntity getEntityType(); 
    public void setEntityType(EmisMetaEntity entity); 
    
    public List<ExcelMergeConfig> getMergeConfigs();
    public void setMergeConfigs(List<ExcelMergeConfig> configs); 
    public void addMergeConfig(ExcelMergeConfig config); 
}
