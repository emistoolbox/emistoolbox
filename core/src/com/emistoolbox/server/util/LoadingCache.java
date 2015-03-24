package com.emistoolbox.server.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoadingCache<T>
{
    private Map<String, FileCacheEntry<T>> entries = new HashMap<String, FileCacheEntry<T>>();
    private CacheFileOps<T> fileOps;
    
    public LoadingCache(CacheFileOps<T> fileOps)
    { this.fileOps = fileOps; }
    
    private String getKey(File f)
    {
        try { return f.getCanonicalPath(); }
        catch (IOException ex)
        { return f.toString(); }
    }
    
    public T get(File f)
        throws IOException
    {
        FileCacheEntry<T> entry = getEntry(f);
        
        T result = null;
        synchronized(entry) {
            result = entry.get();
            if (result == null)
            {
                if (f.exists())
                {
                    long lastModified = f.lastModified();
                    result = fileOps.read(f); 
                    entry.update(result, lastModified);
                }
            }
        }
        
        return result;
    }
    
    synchronized private FileCacheEntry<T> getEntry(File f)
    {
        String key = getKey(f);

        FileCacheEntry<T> entry = entries.get(key);
        if (entry == null)
        {
            entry = new FileCacheEntry<T>(f);
            entries.put(key, entry);
        }
        
        return entry;
    }
    
    public void put(File f, T data)
        throws IOException
    {
        FileCacheEntry<T> entry = getEntry(f);
        synchronized(entry) {
            fileOps.save(data, f); 
            entry.update(data, f.lastModified());
        }
    }
    
    synchronized public void clear(File f)
    { entries.remove(getKey(f)); }
}

class FileCacheEntry<T>
{
    private final long ONE_HOUR_MS = 60 * 60 * 1000;
    private T data;
    private File f;
    
    private long lastUpdated;
    private long lastAccess;
    
    public FileCacheEntry(File f)
    { this.f = f; }

    public void update(T data, long lastModified)
    {
        this.data = data;
        lastUpdated = lastModified;
        lastAccess = System.currentTimeMillis();
    }
    
    public T get()
    {
        lastAccess = System.currentTimeMillis();
        if (isExpired())
            return null;
        
        return data;
    }
    
    public boolean isExpired()
    { 
        boolean expired = lastUpdated < f.lastModified() || System.currentTimeMillis() - lastAccess > ONE_HOUR_MS; 
        if (expired)
            data = null;

        return expired;
    }
}
