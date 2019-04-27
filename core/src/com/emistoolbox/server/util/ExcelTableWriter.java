package com.emistoolbox.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelTableWriter implements TableWriter
{
    private File output;
    private HSSFWorkbook wb = new HSSFWorkbook();
    private HSSFSheet sheet;
    private HSSFRow currentRow;
    private short colCount = 0;
    private short rowCount = 0;

    public ExcelTableWriter(File output) {
        this.output = output;
        this.wb = new HSSFWorkbook();
        this.sheet = this.wb.createSheet("EMISToolbox");

        this.currentRow = this.sheet.createRow(this.rowCount);
        this.rowCount = (short) (this.rowCount + 1);
    }

    public void close()
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(this.output);
            this.wb.write(out);
            out.flush();
            out.close();
        }
        catch (IOException ex)
        { ex.printStackTrace(); }
    }

    public void nextCell(String content)
    {
        this.currentRow.createCell(this.colCount).setCellValue(content);
        this.colCount = (short) (this.colCount + 1);
    }

    public void nextRow()
    {
        this.currentRow = this.sheet.createRow(this.rowCount);
        this.rowCount = (short) (this.rowCount + 1);
        this.colCount = 0;
    }
}
