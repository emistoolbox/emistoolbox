package com.emistoolbox.common.results.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.TableMetaResult;

public class MetaResultUtil 
{
	public static Set<EmisMetaDateEnum> getUsedDateTypes(Collection<MetaResultValue> values, MetaResultDimension[] dimensions)
	{
		Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>(); 
    	for (MetaResultValue metaValues : values)
    		addUsedDateTypes(result, metaValues.getUsedDateTypes(), true);

    	if (dimensions == null)
    		return result; 

    	for (MetaResultDimension dim : dimensions)
    	{
    		if (dim != null)
    			removeDateType(dim, result); 
    	}
    	
		return result; 
	}

	public static Set<EmisMetaDateEnum> getUsedDateTypes(ExcelReportConfig excelReport)
	{
    	if (excelReport == null)
    		return null; 

    	Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>();
    	for (ExcelMergeConfig config : excelReport.getMergeConfigs())
    	{
    		for (int i = 0; i < config.getCellCount(); i++) 
    		{
        		if (CellType.LOOP_VARIABLE != config.getCellType(i))
        			continue; 

        		EmisAggregatorDef aggr = config.getCellAggregator(i);
        		if (aggr != null)
        			MetaResultUtil.addUsedDateTypes(result, aggr.getMetaData().getDateType(), true); 
        		else 
        		{
        			EmisIndicator indicator = config.getCellIndicator(i);
        			if (indicator != null)
            			MetaResultUtil.addUsedDateTypes(result, indicator.getUsedDateTypes(), true); 
        		}
    		}
    	}
    	
    	return result; 
		
	}
	
	public static Set<EmisMetaDateEnum> getUsedDateTypes(PdfReportConfig reportConfig)
	{
		Set<EmisMetaDateEnum> result = new HashSet<EmisMetaDateEnum>();
		if (reportConfig == null)
			return result; 
		
		for (PdfContentConfig contentConfig : reportConfig.getContentConfigs())
    	{
    		if (contentConfig instanceof PdfMetaResultContentConfig)
    		{
    			MetaResult metaResult = ((PdfMetaResultContentConfig) contentConfig).getMetaResult();
    			if (metaResult instanceof TableMetaResult)
    				result.addAll(((TableMetaResult) metaResult).getUsedDateTypes(true));  
    			else
    				result.addAll(metaResult.getUsedDateTypes());  
    		}
    		else if (contentConfig instanceof PdfVariableContentConfig)
    			result.addAll(((PdfVariableContentConfig) contentConfig).getUsedDateTypes());  
    	}
			
		return result; 
	}
	
    public static void addUsedDateTypes(Set<EmisMetaDateEnum> dateTypes, Set<EmisMetaDateEnum> newDateTypes, boolean withParents)
    {
    	for (EmisMetaDateEnum newDateType : newDateTypes)
    		addUsedDateTypes(dateTypes, newDateType, withParents); 
    }
    
    public static void addUsedDateTypes(Set<EmisMetaDateEnum> dateTypes, EmisMetaDateEnum newDateType, boolean withParents)
    {
    	// Always add dateType and then (only if desired) the parents. 
    	do {
    		dateTypes.add(newDateType); 
    		newDateType = newDateType.getParent(); 
    	} while (newDateType != null && withParents); 
    }
    
    public static void removeDateType(MetaResultDimension dim, Set<EmisMetaDateEnum> dateTypes)
    {
    	if (dim instanceof MetaResultDimensionDate)
    	{
    		EmisMetaDateEnum removeDateType = ((MetaResultDimensionDate) dim).getDateEnumType(); 
    		dateTypes.remove(removeDateType); 
    	}
    }
    
    public static void addParentDateTypes(Set<EmisMetaDateEnum> dateTypes)
    {
    	Set<EmisMetaDateEnum> tmp = new HashSet<EmisMetaDateEnum>(); 
    	
    }
}
