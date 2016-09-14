package com.emistoolbox.common;

import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.analysis.impl.ReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

public class EmisReportModuleData implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String datasetName; 
    private EmisMeta model;
    private EmisReportConfig reportConfig = new ReportConfig();
    private Map<String, String> config; 

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

	public Map<String, String> getConfig() 
	{ return config; }

	public String getConfig(String key)
	{ return config.get(key); } 
	
	public boolean getConfigAsBoolean(String key, boolean defaultValue)
	{
		String value = getConfig(key); 
		if (value == null || value.trim().equals(""))
			return defaultValue; 
		
		try { return new Boolean(value); }
		catch (Throwable err)
		{ return defaultValue; } 
	}

	public void setConfig(Map<String, String> config) 
	{ this.config = config; }
}
