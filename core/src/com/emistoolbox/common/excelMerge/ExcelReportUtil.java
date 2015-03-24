package com.emistoolbox.common.excelMerge;

import java.util.List;
import java.util.Map;

import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.impl.MetaResultValueImpl;

public class ExcelReportUtil
{
    public static final String EXCEL_PREFIX_INDICATOR = "i:"; 
    public static final String EXCEL_PREFIX_AGGREGATOR = "a:"; 
    public static final String EXCEL_PREFIX_FIELD = "f:";
    
    public static String getExcelIndicatorId(EmisIndicator i)
    { return EXCEL_PREFIX_INDICATOR + normalize(i.getName()); }
    
    public static EmisIndicator getIndicator(String id, List<EmisIndicator> indicators)
    {
        if (id == null)
            return null;
        
        if (id.startsWith(EXCEL_PREFIX_INDICATOR))
            return findIndicator(id.substring(EXCEL_PREFIX_INDICATOR.length()), indicators); 
        else if (id.startsWith(EXCEL_PREFIX_AGGREGATOR))
        {
            int endPos = id.indexOf(":", EXCEL_PREFIX_AGGREGATOR.length());
            if (endPos == -1)
                return null; 
            else
                return findIndicator(id.substring(EXCEL_PREFIX_AGGREGATOR.length(), endPos), indicators);  
        }

        return null; 
    }
    
    public static String getAggregator(String id)
    {
        if (id == null)
            return null; 
        
        if (!id.startsWith(EXCEL_PREFIX_AGGREGATOR))
            return null; 

        int startPos = id.indexOf(":", EXCEL_PREFIX_AGGREGATOR.length()); 
        if (startPos == -1)
            return null; 
        
        return id.substring(startPos + 1); 
    }
    
    public static String getExcelAggregatorId(EmisIndicator i, EmisAggregatorDef a)
    { 
        String key = findKey(i, a); 
        if (key == null)
            return  null; 
        
        return getExcelAggregatorId(i, key); 
    }

    public static String getExcelAggregatorId(EmisIndicator i, String key)
    { return EXCEL_PREFIX_AGGREGATOR + normalize(i.getName()) + ":" + key; }

    public static String getFieldId(EmisMetaData f)
    { return EXCEL_PREFIX_FIELD + f.getEntity().getName() + "." + f.getName(); }
    
    private static String normalize(String s)
    { return s == null ? null : s.replace(";", ","); }; 
    
    private static EmisIndicator findIndicator(String id, List<EmisIndicator> indicators)
    {
        if (id == null)
            return null; 
        
        for (EmisIndicator indicator : indicators)
            if (id.equals(normalize(indicator.getName())))
                return indicator; 
                
        return null; 
    }
    
    private static String findKey(EmisIndicator indicator, EmisAggregatorDef aggr)
    {
        for (Map.Entry<String, EmisAggregatorDef> entry : indicator.getAggregators().entrySet())
            if (aggr == entry.getValue())
                return entry.getKey(); 
                
        return null; 
    }

    public static void addMetaResultValues(List<MetaResultValue> values, List<String> headers, ExcelReportConfig excelConfig, List<EmisIndicator> indicators)
    {
        for (ExcelMergeConfig config : excelConfig.getMergeConfigs())
            addMetaResultValues(values, headers, config, indicators); 
    }

    private static void addMetaResultValues(List<MetaResultValue> values, List<String> headers, ExcelMergeConfig config, List<EmisIndicator> indicators)
    {
        for (int i = 0; i < config.getCellCount(); i++)
        {
            if (config.getCellType(i) != CellType.LOOP_VARIABLE)
                continue; 
            
            String variable = config.getCellValue(i);
            if (headers.contains(variable))
                continue; 
            
            MetaResultValue metaResultValue = getMetaResultValue(variable, indicators); 
            if (metaResultValue == null)
                continue; 
            
            headers.add(variable); 
            values.add(metaResultValue); 
        }
    }
    
    private static MetaResultValue getMetaResultValue(String id, List<EmisIndicator> indicators)
    {
        EmisIndicator indicator = ExcelReportUtil.getIndicator(id, indicators); 
        if (indicator == null)
            return null; 
        
        String key = ExcelReportUtil.getAggregator(id);
        if (key == null)
            return new MetaResultValueImpl(indicator); 
        
        return new MetaResultValueImpl(indicator, key); 
    }
}

