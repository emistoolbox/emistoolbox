package com.emistoolbox.server.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.excelMerge.ExcelReportUtil;
import com.emistoolbox.common.results.ExcelReportMetaResult;
import com.emistoolbox.server.model.EmisDataSet;

public class ExcelResultCollector extends ResultCollector
{
    private ExcelReportMetaResult excelMetaResult;
    private List<String> fields = new ArrayList<String>(); 
    private List<String> headers = new ArrayList<String>(); 
    private String entityName;  
    
    public ExcelResultCollector(EmisDataSet emisDataSet, ExcelReportMetaResult metaResult) 
    { 
        super(emisDataSet, metaResult); 

        excelMetaResult = metaResult; 
        entityName = metaResult.getReportConfig().getEntityType().getName(); 
        
        headers.clear(); 
        headers.addAll(metaResult.getHeaders());
        
        for (ExcelMergeConfig config : metaResult.getReportConfig().getMergeConfigs())
        {
            for (int i = 0; i < config.getCellCount(); i++)
            {
                if (config.getCellType(i) != CellType.LOOP_VARIABLE)
                    continue; 

                String fieldPrefix = ExcelReportUtil.EXCEL_PREFIX_FIELD + entityName + ".";
                String variable = config.getCellValue(i); 
                if (variable != null && variable.startsWith(fieldPrefix))
                {
                    headers.add(variable); 
                    fields.add(variable.substring(ExcelReportUtil.EXCEL_PREFIX_FIELD.length())); 
                }
            }
        }
    }
    
    public List<String> getHeadings()
    { return headers; }
    
    public List<Object[]> getResults()
    {
        final List<Object[]> result = new ArrayList<Object[]>();
        calculateAllResults(excelMetaResult, excelMetaResult.getReportConfig().getEntityType(), getPlainFields(fields), new ResultCollectorCallback() {
            public void setResult(int id, Map<String, String> entityValues, double[] values)
            {
                Object[] rowValues = new Object[headers.size()]; 
                for (int i = 0; i < values.length; i++) 
                    rowValues[i] = values[i]; 
                
                for (int i = 0; i < fields.size(); i++) 
                    rowValues[i + values.length] = entityValues.get(getPlainField(fields.get(i))); 
                
                result.add(rowValues); 
            }
        }); 

        return result; 
    }
    
    private String getPlainField(String field)
    {
        int pos = field.indexOf("."); 
        if (pos == -1)
            return field; 
        return field.substring(pos + 1); 
    }
    
    private String[] getPlainFields(List<String> fields)
    {
        String[] result = new String[fields.size()]; 
        for (int i = 0; i < result.length; i++)
            result[i] = getPlainField(fields.get(i)); 
        
        return result; 
    }
}
