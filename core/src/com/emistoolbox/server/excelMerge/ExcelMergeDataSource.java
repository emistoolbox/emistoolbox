package com.emistoolbox.server.excelMerge;

import java.util.*;

/** Class to provide data for Excel merge. */
public interface ExcelMergeDataSource
{
    public Map<String, Object> getGlobals();
    public Object getGlobal(String name);

    Iterator<Map<String, Object>> getLoop();
}
