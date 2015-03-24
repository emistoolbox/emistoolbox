package com.emistoolbox.common.excelMerge.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.emistoolbox.common.excelMerge.CellPosition;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;

public class ExcelMergeConfigImpl implements ExcelMergeConfig, Serializable
{
    private String sheetName;
    private MergeDirection direction;
    private CellPosition topLeft;

    private CellType[] cellTypes = null;
    private String[] cellValues = null;
    private EmisIndicator[] cellIndicators = null; 
    private EmisAggregatorDef[] cellAggregators = null; 
    
    private Map<String, String> context = new HashMap<String, String>();

    public String getSheetName()
    { return sheetName; }

    public void setSheetName(String name)
    { this.sheetName = name; }

    public MergeDirection getDirection()
    { return direction; }

    public void setDirection(MergeDirection direction)
    { this.direction = direction; }

    public CellPosition getTopLeft()
    { return topLeft; }

    public void setTopLeft(CellPosition cellPosition)
    { this.topLeft = cellPosition; }

    public int getCellCount()
    { return cellTypes != null ? cellTypes.length : 0; }

    public CellType getCellType(int index)
    { return cellTypes[index]; }

    public void setCells(CellType[] types, String[] values, EmisIndicator[] indicators, EmisAggregatorDef[] aggregators)
    {
        this.cellTypes = types;
        this.cellValues = values;
        this.cellIndicators = indicators; 
        this.cellAggregators = aggregators; 
    }

    public String getCellValue(int index)
    { return cellValues[index]; }

    public Map<String, String> getContext()
    { return context; }

    public void setContext(Map<String, String> context)
    { this.context = context; }

    public void putContext(String key, String value)
    { context.put(key, value); }

    public void setCellType(int index, CellType type)
    { cellTypes[index] = type; }

    public void setCellValue(int index, String value)
    { cellValues[index] = value; }

    public void setCellIndicator(int index, EmisIndicator indicator)
    { cellIndicators[index] = indicator; } 
    
    public void setCellAggregator(int index, EmisAggregatorDef aggr)
    { cellAggregators[index] = aggr; } 

	public EmisIndicator getCellIndicator(int index) 
	{ return cellIndicators[index]; } 

	public EmisAggregatorDef getCellAggregator(int index) 
	{ return cellAggregators[index]; } 

	/*
		if (CellType.LOOP_VARIABLE != getCellType(index))
			return null; 
		
		String value = getCellValue(index);
		if (value.startsWith("a:"))
		{
			EmisIndicator indicator = findIndicator(index, config);
			if (indicator == null)
				return null; 

			int pos = value.indexOf(":"); 
			if (pos == -1)
				return null; 

			return indicator.getAggregator(value.substring(pos + 1));  
		}
		else 
			return null; 
	}
	
	@Override
	public EmisIndicator findIndicator(int index, EmisReportConfig config)
	{
		if (CellType.LOOP_VARIABLE != getCellType(index))
			return null; 
		
		String value = getCellValue(index);
		String name = null; 
		if (value.startsWith("i:"))
			name = value.substring(2);
		else if (value.startsWith("a:"))
		{
			int pos = value.indexOf(":"); 
			if (pos == -1)
				return null; 
			name = value.substring(2, pos); 
		}
		else 
			return null; 
		
		return (EmisIndicator) NamedUtil.find(name, config.getIndicators()); 
	}
	*/ 
}
