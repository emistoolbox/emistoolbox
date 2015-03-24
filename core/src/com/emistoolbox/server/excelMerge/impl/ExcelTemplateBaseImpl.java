package com.emistoolbox.server.excelMerge.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.emistoolbox.common.excelMerge.CellPosition;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.MergeDirection;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.server.excelMerge.ExcelMergeDataSource;
import com.emistoolbox.server.excelMerge.ExcelTemplate;

public abstract class ExcelTemplateBaseImpl implements ExcelTemplate
{
    private List<ExcelMergeConfig> configs = new ArrayList<ExcelMergeConfig>();

    public List<ExcelMergeConfig> getMergeConfigs()
    { return configs; }

    public void setMergeConfigs(List<ExcelMergeConfig> configs)
    { this.configs = configs; }

    public Set<String> getGlobalVariables()
    { return getVariables(CellType.GLOBAL_VARIABLE); }

    public Set<String> getLoopVariables()
    { return getVariables(CellType.LOOP_VARIABLE); }

    private Set<String> getVariables(CellType type)
    {
        Set<String> result = new HashSet<String>();
        for (ExcelMergeConfig config : configs)
        {
            
            for (int i = 0; i < config.getCellCount(); i++)
            {
                if (config.getCellType(i) == type)
                    result.add(config.getCellValue(i));
            }
        }
        
        return result;
    }

    protected abstract Workbook getExcelTemplate()
        throws IOException;

    public void saveMergedDocument(OutputStream out, ExcelMergeDataSource dataSource)
        throws IOException
    {
        Workbook wb = getExcelTemplate();
        replace(wb, dataSource);
        wb.write(out);
    }

    private void replace(Workbook wb, ExcelMergeDataSource dataSource)
        throws IOException
    {
        List<Sheet> sheets = new ArrayList<Sheet>(); 
        for (ExcelMergeConfig config : configs)
            sheets.add(wb.getSheet(config.getSheetName())); 

        Map<String, Object> globalVars = dataSource.getGlobals();
        EvaluationWorkbook evalWorkbook = getEvaluatingWorkbook(wb); 
        List<Cell[]> topRows = new ArrayList<Cell[]>(); 
        for (ExcelMergeConfig config : configs)
            topRows.add(new Cell[config.getCellCount()]);  

        int dataIndex = 0;
        Iterator<Map<String, Object>> iter = dataSource.getLoop();
        while (iter.hasNext())
        {
            Map<String, Object> localVars = iter.next();
            for (int i = 0; i < configs.size(); i ++) 
            {
                ExcelMergeConfig config = configs.get(i); 
                Sheet sheet = sheets.get(i); 
                int sheetIndex = wb.getSheetIndex(config.getSheetName()); 
                replace(dataIndex, config, sheet, globalVars, localVars, topRows.get(i), evalWorkbook, sheetIndex); 
            }
            
            dataIndex++; 
        }
    }
    
    private void replace(int dataIndex, ExcelMergeConfig config, Sheet sheet, Map<String, Object> globals, Map<String, Object> data, Cell[] topRow, EvaluationWorkbook evalWorkbook, int sheetIndex)
        throws IOException
    {
        for (int cellIndex = 0; cellIndex < config.getCellCount(); cellIndex++)
            setCell(config.getCellType(cellIndex), config.getCellValue(cellIndex), getCell(sheet, dataIndex, cellIndex, config), globals, data, config.getDirection(), dataIndex, cellIndex, topRow, evalWorkbook, sheetIndex);
    }

    private void setCell(CellType cellType, String value, Cell c, Map<String, Object> globals, Map<String, Object> locals, MergeDirection dir, int dataIndex, int cellIndex, Cell[] topRow, EvaluationWorkbook evalWorkbook, int sheetIndex)
        throws IOException
    {
        switch (cellType)
        {
        case CONSTANT:
            c.setCellType(Cell.CELL_TYPE_STRING);
            c.setCellValue(value);
            break;

        case COPY:
            if (dataIndex == 0)
            {
                // Remember cells in first row
                topRow[cellIndex] = c; 
            }
            else
            {
                Cell topCell = topRow[cellIndex];
                c.setCellType(topCell.getCellType()); 

                // Copies value from first row (adjust formulae if necessary)
                switch (topCell.getCellType())
                {
                case Cell.CELL_TYPE_FORMULA:
                    c.setCellFormula(getOffsetFormula(topCell.getCellFormula(), evalWorkbook, sheetIndex, dir == MergeDirection.COLUMNS ? dataIndex  : 0, dir == MergeDirection.ROWS ? dataIndex : 0)); 
                    break; 
                    
                case Cell.CELL_TYPE_BOOLEAN: 
                    c.setCellValue(topCell.getBooleanCellValue());
                    break; 
                    
                case Cell.CELL_TYPE_NUMERIC: 
                    c.setCellValue(topCell.getNumericCellValue()); 
                    break; 
                    
                case Cell.CELL_TYPE_STRING: 
                    c.setCellValue(topCell.getStringCellValue()); 
                    break; 
                } 
            }
            
            break; 
            
        case EMPTY:
            c.setCellType(Cell.CELL_TYPE_BLANK);
            break;

        case GLOBAL_VARIABLE:
            setValue(c, globals.get(value));
            break;

        case LOOP_VARIABLE:
            setValue(c, locals.get(value));
            break;

        case ORIGINAL:
            break;

        default:
            break;
        }
    }

    private String getOffsetFormula(String formula, EvaluationWorkbook evalWorkbook, int sheetIndex, int rowOffset, int colOffset)
    {
        Ptg[] parts = FormulaParser.parse(formula, (FormulaParsingWorkbook) evalWorkbook, FormulaType.CELL, sheetIndex); 
        for (int i = 0; i < parts.length; i++) 
        {
            if (parts[i] instanceof RefPtgBase)
            { 
                RefPtgBase ref = (RefPtgBase) parts[i]; 
                if (ref.isColRelative())
                    ref.setColumn(ref.getColumn() + colOffset); 

                if (ref.isRowRelative())
                    ref.setRow(ref.getRow() + rowOffset);                 
            }                
        }

        return FormulaRenderer.toFormulaString((FormulaRenderingWorkbook) evalWorkbook, parts); 
    }
    
    private EvaluationWorkbook getEvaluatingWorkbook(Workbook wb)
    {
        if (wb instanceof HSSFWorkbook)
            return HSSFEvaluationWorkbook.create((HSSFWorkbook) wb); 
        else if (wb instanceof XSSFWorkbook)
            return XSSFEvaluationWorkbook.create((XSSFWorkbook) wb); 
            
        return null; 
    }

    private void setValue(Cell c, Object value)
        throws IOException
    {
        if (value == null)
            c.setCellType(Cell.CELL_TYPE_BLANK);
        else if (value instanceof String)
        {
            c.setCellType(Cell.CELL_TYPE_STRING);
            c.setCellValue((String) value);
        }
        else if (value instanceof Integer)
        {
            c.setCellType(Cell.CELL_TYPE_NUMERIC);
            c.setCellValue((Integer) value);
        }
        else if (value instanceof Double)
        {
            Double dbl = (Double) value; 
            if (Double.isNaN(dbl))
                c.setCellType(Cell.CELL_TYPE_BLANK); 
            else
            {
                c.setCellType(Cell.CELL_TYPE_NUMERIC);
                c.setCellValue((Double) value);
            }
        }
        else if (value instanceof Float)
        {
            c.setCellType(Cell.CELL_TYPE_NUMERIC);
            c.setCellValue((Float) value);
        }
        else
            throw new IOException("Unsupported data type: " + value.getClass());
    }

    private Cell getCell(Sheet s, int dataRow, int dataCol, ExcelMergeConfig config)
    {
        if (config.getDirection() == MergeDirection.ROWS)
            return getCell(s, new CellPosition(config.getTopLeft(), dataCol, dataRow));
        else
            return getCell(s, new CellPosition(config.getTopLeft(), dataRow, dataCol));
    }

    private Cell getCell(Sheet s, CellPosition pos)
    {
        Row row = s.getRow(pos.getRowIndex());
        if (row == null)
            row = s.createRow(pos.getRowIndex());

        Cell c = row.getCell(pos.getColumnIndex());
        if (c == null)
            c = row.createCell(pos.getColumnIndex());

        return c;
    }
}
