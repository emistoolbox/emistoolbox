package com.emistoolbox.common.excelMerge;

import java.util.*;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;

/** Configuration of Excel merge. */
public interface ExcelMergeConfig
{
    public String getSheetName();
    public void setSheetName(String name);

    public enum MergeDirection { 
        ROWS, COLUMNS;
        MergeDirection() {}
    };
    
    public enum CellType { 
        ORIGINAL, COPY, EMPTY, GLOBAL_VARIABLE, LOOP_VARIABLE, CONSTANT;
        CellType() {}
    };

    public MergeDirection getDirection();
    public void setDirection(MergeDirection direction);

    public CellPosition getTopLeft();
    public void setTopLeft(CellPosition cellPosition);

    public int getCellCount();

    public void setCells(CellType[] types, String[] values, EmisIndicator[] indicators, EmisAggregatorDef[] aggregators);

    public CellType getCellType(int index);
    public void setCellType(int index, CellType type);

    public String getCellValue(int index);
    public void setCellValue(int index, String value);

    public Map<String, String> getContext();
    public void setContext(Map<String, String> context);
    public void putContext(String key, String value);
    
	public EmisIndicator getCellIndicator(int index); 
    public void setCellIndicator(int index, EmisIndicator indicator); 
    
	public EmisAggregatorDef getCellAggregator(int index);
    public void setCellAggregator(int index, EmisAggregatorDef aggr); 
}

