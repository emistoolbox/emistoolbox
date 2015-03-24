package com.emistoolbox.server.mapping;

import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigAccess;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigCsv;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigExcel;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigFile;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigGeo;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigHsqldb;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc;
import com.emistoolbox.common.model.mapping.impl.UnflattenDbQuery;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.XmlReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import shapefile.ShapefileFamily;

public class DbUtil
{
    public static DbDataSource getDataSource(DbDataSourceConfig config, String dataset)
    	throws IOException
    {
    	DbDataSource result = getDataSourceInternal(config, dataset); 
    	result.setDataset(dataset); 
    	
    	return result; 
    }
    
    private static DbDataSource getDataSourceInternal(DbDataSourceConfig config, String dataset)
    	throws IOException
    {
    	if ((config instanceof DbDataSourceConfigJdbc))
            return new DbDataSourceJdbc((DbDataSourceConfigJdbc) config);
        
        if (config instanceof DbDataSourceConfigCsv)
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) config;
            DbDataSourceMultiple result = new DbDataSourceMultiple(); 
            for (DbDataFileSource fileSource : fileConfig.getFileSources())
            	result.addDataSource(new DbDataSourceCsv(fileSource, dataset));

            if (result.getDataSources().size() == 1)
            	return result.getDataSources().get(0); 
            else
            	return result;
        }

        if (config instanceof DbDataSourceConfigExcel)
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) config;
            DbDataSourceMultiple result = new DbDataSourceMultiple(); 
            for (DbDataFileSource fileSource : fileConfig.getFileSources())
            	result.addDataSource(new DbDataSourceExcel(fileSource, dataset));
            	
            return result; 
        }
        
        if ((config instanceof DbDataSourceConfigGeo))
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) config;
            DbDataSourceMultiple result = new DbDataSourceMultiple();
            for (DbDataFileSource fileSource : fileConfig.getFileSources())
            {
                try
                {
                	String path = EmisToolboxIO.getPath(fileSource, dataset); 
                    if (path.endsWith(".shp"))
                        path = path.substring(0, path.length() - 4);

                    ShapefileFamily shpFile = new ShapefileFamily();
                    shpFile.read(path); 
                    result.addDataSource(shpFile.getDataSource());
                }
                catch (IOException ex)
                { ex.printStackTrace(); }
            }
            return result;
        }

        if ((config instanceof DbDataSourceConfigAccess))
        {
            DbDataSourceMultiple result = new DbDataSourceMultiple();
            for (DbDataFileSource fileSource : ((DbDataSourceConfigFile) config).getFileSources())
            	result.addDataSource(new DbDataSourceJdbc((DbDataSourceConfigAccess) config, fileSource, dataset)); 

            return result;
        }
        
        if (config instanceof DbDataSourceConfigHsqldb)
        	return new DbDataSourceHsqldb((DbDataSourceConfigHsqldb) config, dataset);  

        return null;
    }

    private static Document readXml(String path) throws IOException
    {
        File in = new File(path);
        if (!in.exists())
            return null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(in);
        }
        catch (IOException ex)
        {
            throw ex;
        }
        catch (Throwable err)
        {
            throw ((IOException) new IOException("Failed to load XML " + in.getPath()).initCause(err));
        }
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.mapping.DbUtil JD-Core Version: 0.6.0
 */