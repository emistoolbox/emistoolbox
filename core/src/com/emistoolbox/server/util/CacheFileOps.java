package com.emistoolbox.server.util;

import java.io.File;
import java.io.IOException;

public interface CacheFileOps<T>
{
    public T read(File f)
        throws IOException;
    
    public void save(T data, File f)
        throws IOException;
}
