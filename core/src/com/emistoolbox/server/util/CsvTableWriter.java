package com.emistoolbox.server.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CsvTableWriter implements TableWriter
{
    private PrintWriter out;
    private boolean emptyRow = true;

    public CsvTableWriter(File output) throws FileNotFoundException, IOException {
        this.out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output)));
    }

    public void nextRow()
    {
        this.out.println();
        this.emptyRow = true;
    }

    public void nextCell(String content)
    {
        if (!this.emptyRow)
        {
            this.out.print(",");
        }
        this.out.print(content);
        this.emptyRow = false;
    }

    public void close()
    {
        this.out.flush();
        this.out.close();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.util.CsvTableWriter JD-Core Version:
 * 0.6.0
 */