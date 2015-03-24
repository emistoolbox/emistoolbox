package com.emistoolbox.server;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.server.mapping.MapProcess;

public class ImportProcessList
{
    private static final long FIVE_MINUTES_AS_MILLISECS = 5 * 60 * 60 * 1000; 
    private static ImportProcessList instance = new ImportProcessList(); 
    
    /** Pending import processes. */ 
    private Map<Integer, MapProcess> processes = new HashMap<Integer, MapProcess>(); 

    /** Next import process id. */ 
    private int nextProcessId = 1; 
    
    private ImportProcessList()
    {}
    
    public static ImportProcessList get()
    { return instance; } 
    
    public MapProcess get(int id)
    { return processes.get(id); }
    
    public int add(MapProcess process)
    {
        int id = -1; 
        synchronized(this) {
            id = nextProcessId; 
            nextProcessId++; 
        }
        
        processes.put(id, process); 
        process.setId(id);
        
        return id; 
    }

    public void clearProcesses()
    {
        Set<Integer> keysToRemove = new HashSet<Integer>();
        
        for (Integer key : processes.keySet())
        {
            Date created = processes.get(key).getCreatedDate(); 
            if (System.currentTimeMillis() - created.getTime() > FIVE_MINUTES_AS_MILLISECS)
                keysToRemove.add(key);  
        }
        
        for (Integer key : keysToRemove)
            processes.remove(key);
    }
    
    public Collection<MapProcess> getAll()
    {
        clearProcesses(); 
        return processes.values(); 
    }
}
