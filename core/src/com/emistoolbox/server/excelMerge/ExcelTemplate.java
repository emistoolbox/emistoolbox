package com.emistoolbox.server.excelMerge;

import java.io.*;
import java.util.*;

import com.emistoolbox.common.excelMerge.ExcelMergeConfig;

/** Class to load an Excel Spreadsheet and populate it with table style data. */
public interface ExcelTemplate
{
    public List<ExcelMergeConfig> getMergeConfigs();
    public void setMergeConfigs(List<ExcelMergeConfig> configs);

    public Set<String> getGlobalVariables();
    public Set<String> getLoopVariables();

    public void saveMergedDocument(OutputStream out, ExcelMergeDataSource dataSource)
        throws IOException;
}
