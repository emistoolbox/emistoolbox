package com.emistoolbox.server.excelMerge.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;

public class ExcelFileTemplateImpl extends ExcelTemplateBaseImpl
{
    private File file;

    public ExcelFileTemplateImpl(String filename)
        throws IOException
    { this(new File(filename)); }
    
    public ExcelFileTemplateImpl(File xlsFile)
        throws IOException
    {
        this.file = xlsFile;
        if (!file.exists())
            throw new IOException("File '" + file.getName() + "' not found.");
    }

    protected Workbook getExcelTemplate()
        throws IOException
    { return getWorkbook(file); }
    
    public static Workbook getWorkbook(File f)
        throws IOException
    {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            return WorkbookFactory.create(is);
        }
        catch (InvalidFormatException e)
        { throw new IOException("Invalid Excel format for " + f.getName(), e); }
        finally
        { IOUtils.closeQuietly(is); }
        
    }

    public static String[] getSheets(File excelFile)
        throws IOException
    {
        Workbook wb = ExcelFileTemplateImpl.getWorkbook(excelFile); 
        String[] result = new String[wb.getNumberOfSheets()]; 
        for (int i = 0; i < result.length; i++) 
            result[i] = wb.getSheetName(i); 
            
        return result; 
    }
}
