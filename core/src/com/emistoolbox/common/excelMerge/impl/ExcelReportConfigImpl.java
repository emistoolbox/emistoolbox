package com.emistoolbox.common.excelMerge.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.impl.NamedImpl;

public class ExcelReportConfigImpl extends NamedImpl implements ExcelReportConfig, Serializable
{
    private String templateFile; 
    private List<ExcelMergeConfig> configs = new ArrayList<ExcelMergeConfig>(); 
    private EmisMetaEntity entity; 
    
    public String toString()
    { 
        if (templateFile == null || templateFile.equals(""))
            return getName(); 
        else
            return getName() + " (" + templateFile + ")"; 
    }
    
    public boolean isReady()
    {
        if (templateFile == null || templateFile.equals(""))
            return false; 
        
        if (configs.size() < 1)
            return false; 
        
        if (entity == null)
            return false; 
        
        for (ExcelMergeConfig config : configs)
            if (config.getCellCount() > 0)
                return true; 
        
        return false; 
    }
    
    public String getTemplateFile()
    { return templateFile; } 

    public void setTemplateFile(String filename)
    { this.templateFile = filename; } 

    public EmisMetaEntity getEntityType()
    { return entity; } 

    public void setEntityType(EmisMetaEntity entity)
    { this.entity = entity; } 

    public List<ExcelMergeConfig> getMergeConfigs()
    { return configs; } 

    public void setMergeConfigs(List<ExcelMergeConfig> configs)
    { this.configs = configs; } 

    public void addMergeConfig(ExcelMergeConfig config)
    { configs.add(config); }
    
    public static EmisIndicator getIndicator(CellType cellType, String value, List<EmisIndicator> indicators)
    {
    	if (cellType != CellType.LOOP_VARIABLE)
    		return null; 
    	
    	String name = null; 
    	if (value.startsWith("i:"))
    		name = value.substring(2); 
    	else if (value.startsWith("a:"))
    		name = value.substring(2, value.indexOf(":")); 
    	else 
    		return null; 
    	
    	return NamedUtil.find(name, indicators); 
    }
    
    public static EmisAggregatorDef getAggregator(EmisIndicator indicator, String value)
    {
    	if (indicator == null)
    		return null; 
    	
    	if (!value.startsWith("a:"))
    		return null; 
    	
    	return indicator.getAggregator(value.substring(value.indexOf(":") + 1)); 
    }    
}
