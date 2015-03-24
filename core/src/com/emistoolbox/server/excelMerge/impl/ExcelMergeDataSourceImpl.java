package com.emistoolbox.server.excelMerge.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.emistoolbox.server.excelMerge.ExcelMergeDataSource;

public class ExcelMergeDataSourceImpl implements ExcelMergeDataSource
{
    private List<String> headings; 
    private List<Object[]> values; 
    
    public ExcelMergeDataSourceImpl(List<String> headings, List<Object[]> values)
    {
        this.headings = headings; 
        this.values = values; 
    }
    
    public Map<String, Object> getGlobals()
    { return null; }

    public Object getGlobal(String name)
    { return null; }

    public Iterator<Map<String, Object>> getLoop()
    { return new ExcelMergeIterator(); }
    
    class ExcelMergeIterator implements Iterator<Map<String, Object>>
    {
        private int nextPos = 0; 
        
        public boolean hasNext()
        { return nextPos < values.size(); }

        public Map<String, Object> next()
        {
            Object[] v = values.get(nextPos); 
            
            Map<String, Object> result = new HashMap<String, Object>(); 
            for (int i = 0; i < headings.size(); i++)
            {
                if (i < v.length)
                    result.put(headings.get(i), v[i]); 
            }
            
            nextPos++; 
            
            return result; 
        }

        public void remove()
        {}
    }
}
