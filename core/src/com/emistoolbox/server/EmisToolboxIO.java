package com.emistoolbox.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.impl.EmisMetaImpl;
import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.util.CacheFileOps;
import com.emistoolbox.server.util.LoadingCache;

public class EmisToolboxIO
{
    private static LoadingCache<Object> xmlFileCache = new LoadingCache<Object>(new XmlFileOps());
    private static LoadingCache<EmisDataSet> datasetCache = new LoadingCache<EmisDataSet>(new DatasetFileOps());

    
    public static String getPath(DbDataFileSource fileSource, String dataset)
    {
    	if (fileSource == null)
    		return null; 
    	
    	return EmisToolboxIO.getPath(fileSource.getUrl(), dataset); 
    }
    
    public static String getPath(String path, String dataset)
    {
    	if (StringUtils.isEmpty(path) || path.startsWith(DbDataFileSource.PREFIX_HTTP) || path.startsWith(DbDataFileSource.PREFIX_HTTPS) || path.startsWith(DbDataFileSource.PREFIX_MAGPI))
    		return null; 
    	
    	if (path.startsWith(DbDataFileSource.PREFIX_FILESYSTEM))
    		return path.substring(DbDataFileSource.PREFIX_FILESYSTEM.length()); 
    	
    	if (path.startsWith(DbDataFileSource.PREFIX_DATASET))
    	{
    		String basePath = EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH, ServerUtil.ROOT_PATH); 
    		if (!(basePath.endsWith("/") || basePath.endsWith("\\")))
    			basePath += "/"; 
    		return  basePath + dataset + "/" + path.substring(DbDataFileSource.PREFIX_DATASET.length()); 
    	}
    	
    	return null; 
    }
    
    
    public static EmisDataSet loadDataset(String dataset)
        throws IOException
    {
        EmisDataSet result = datasetCache.get(ServerUtil.getFile(dataset, "data.bin", false));
        ((EmisMetaImpl) result.getMetaDataSet()).setDatasetName(dataset); 
        return result;
    } 

    public static EmisMeta loadModelXml(String dataset)
        throws IOException
    { 
    	EmisMeta meta = (EmisMeta) xmlFileCache.get(ServerUtil.getFile(dataset, "model.xml", false)); 
    	meta.setDatasetName(dataset);
    	return meta; 
    }

    public static EmisReportConfig loadReportXml(String dataset, EmisMeta meta)
        throws IOException
    { 
        try {
            setThreadEmisMeta(meta);
            return (EmisReportConfig) xmlFileCache.get(ServerUtil.getFile(dataset, "report.xml", false)); 
        }
        finally
        { setThreadEmisMeta(null); }
    }
    
    public static EmisDbMap loadMappingXml(String dataset, EmisMeta meta)
        throws IOException
    { 
        try {
            setThreadEmisMeta(meta);
            return (EmisDbMap) xmlFileCache.get(ServerUtil.getFile(dataset, "mapping.xml", false)); 
        }
        finally
        { setThreadEmisMeta(null); }
    }
    
    public static List<EmisValidation> loadValidationXml(String dataset, EmisMeta meta)
    	throws IOException
    {
    	try { 
    		setThreadEmisMeta(meta); 
    		List<EmisValidation> result = (List<EmisValidation>) xmlFileCache.get(ServerUtil.getFile(dataset, "validation.xml", false)); 
    		if (result == null)
    			result = new ArrayList<EmisValidation>(); 
    		
    		return result; 
    	}
    	finally 
    	{ setThreadEmisMeta(null); } 
    }

    public static List<EmisUser> loadUsersXml()
        throws IOException
    { return (List<EmisUser>) xmlFileCache.get(ServerUtil.getFile(null, "users.xml", false)); }
    
    public static void saveDataset(EmisDataSet dataset, String name)
            throws IOException
    { datasetCache.put(ServerUtil.getFile(name, "data.bin", false), dataset); }

    public static void saveReportXml(String dataset, EmisReportConfig config)
        throws IOException
    { xmlFileCache.put(ServerUtil.getFile(dataset, "report.xml", false), config); }
    
    public static void saveModelXml(String dataset, EmisMeta meta)
        throws IOException
    { xmlFileCache.put(ServerUtil.getFile(dataset, "model.xml", false), meta); }

    public static void saveMappingXml(String dataset, EmisDbMap mapping)
        throws IOException
    { xmlFileCache.put(ServerUtil.getFile(dataset, "mapping.xml", false), mapping); }

    public static void saveUsersXml(List<EmisUser> users)
        throws IOException
    { xmlFileCache.put(ServerUtil.getFile(null, "users.xml",false), users); }
    
    public static void saveValidationXml(String dataset, List<EmisValidation> validations)
    	throws IOException
    { xmlFileCache.put(ServerUtil.getFile(dataset, "validation.xml", false), validations); }
    
    public static Document readDocument(File f)
        throws IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(f);
        }
        catch (IOException ex)
        { throw ex; }
        catch (Throwable err)
        { throw ((IOException) new IOException("Failed to load XML " + f.getPath()).initCause(err)); }
    }
    
    public static void writeDocument(Document doc, File f)
        throws IOException
    {
        if (!f.getParentFile().exists())
            f.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(f);

            OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
            of.setIndent(2);
            of.setIndenting(true);

            XMLSerializer serializer = new XMLSerializer(fos, of);
            serializer.asDOMSerializer();
            serializer.serialize(doc);
        }
        finally
        { IOUtils.closeQuietly(fos); }
    }

    static class XmlFileOps implements CacheFileOps<Object>
    {
        @Override
        public Object read(File f) throws IOException
        { return parseDocument(f.getName(), EmisToolboxIO.readDocument(f)); }

        private Object parseDocument(String filename, Document doc)
            throws IOException
        {
            XmlReader reader = new XmlReader();
            if (filename.endsWith("users.xml"))
                return reader.getUsers(doc.getDocumentElement());
            
            if (filename.endsWith("report.xml"))
                return reader.getEmisReportConfig(doc.getDocumentElement(), EmisToolboxIO.getThreadEmisMeta());
            
            if (filename.endsWith("model.xml"))
                return reader.getEmisMeta(doc.getDocumentElement());
            
            if (filename.endsWith("mapping.xml"))
                return reader.getEmisDbMap(doc.getDocumentElement(), EmisToolboxIO.getThreadEmisMeta());
            
            if (filename.endsWith("validation.xml"))
            	return reader.getValidation(doc.getDocumentElement(), EmisToolboxIO.getThreadEmisMeta()); 
            
            throw new IllegalArgumentException("Unknown XML file: " + filename);
        }
        
        @Override
        public void save(Object data, File f) 
            throws IOException
        {
            Document doc = getDocument(f.getName(), data);
            EmisToolboxIO.writeDocument(doc, f);
        }
        
        private Document getDocument(String filename, Object data)
        {
            XmlWriter writer = new XmlWriter();
            if (filename.endsWith("users.xml"))
                return writer.getXml((List<EmisUser>) data);
            
            if (filename.endsWith("report.xml"))
                return writer.getXml((EmisReportConfig) data);
            
            if (filename.endsWith("model.xml"))
                return writer.getXml((EmisMeta) data);
            
            if (filename.endsWith("mapping.xml"))
                return writer.getXml((EmisDbMap) data);
            
            if (filename.endsWith("validation.xml"))
            	return writer.getXmlValidations((List<EmisValidation>) data); 
            
            throw new IllegalArgumentException("Unknown XML file: " + filename);
        }
    }
    
    static class DatasetFileOps implements CacheFileOps<EmisDataSet>
    {
        @Override
        public EmisDataSet read(File f) 
            throws IOException
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            try
            {
                ois.readInt();
                return (EmisDataSet) ois.readObject();
            }
            catch (ClassNotFoundException ex)
            { throw new IOException(ex); }
            finally
            { ois.close(); }
        }

        @Override
        public void save(EmisDataSet data, File f)
            throws IOException
        {
            if (!f.getParentFile().exists())
                f.getParentFile().mkdirs();

            if (data == null)
            {
                f.delete();
                return;
            }
            
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            try
            {
                oos.writeInt(1);
                oos.writeObject(data);
            }
            catch (IOException ex)
            { throw ex; }
            catch (Throwable ex)
            { throw new IOException(ex); } 
            finally
            { oos.close(); }
        }
    }
    
    private static final String[] extensions = { 
        "png", "json", "js", "gml", 
        "xls", "csv", "pdf", 
        "xml", "txt", "log", 
        "shp", "dbf"
    }; 
    
    private static final String[] contentTypes = { 
        "image/png", "application/json", "application/json", "text/xml", 
        "application/vnd.ms-excel", "text/csv", "application/pdf", 
        "text/xml", "text/plain", "text/plain", 
        "application/octet-stream", "application/octet-stream"
    }; 

    public static String getContentType(String filename)
    { return getContentType(filename, null); }

    public static String getContentType(String filename, String defaultValue)
    {
        int pos = filename.lastIndexOf("."); 
        if (pos == -1)
            return defaultValue; 
        
        String ext = filename.substring(pos + 1); 
        for (int i = 0; i < extensions.length; i++) 
        {
            if (extensions[i].equals(ext))
                return contentTypes[i]; 
        }
        
        return defaultValue; 
    }
    
    private static ThreadLocal<EmisMeta> metaThread = new ThreadLocal<EmisMeta>();

    private static EmisMeta getThreadEmisMeta()
    { return metaThread.get(); }
    
    private static void setThreadEmisMeta(EmisMeta meta)
    { metaThread.set(meta); }
    
    public static void syncFolder(File sourceFolder, File targetFolder, FilenameFilter filter)
    	throws IOException
    {
    	if (sourceFolder.getCanonicalPath().equals(targetFolder.getCanonicalPath()))
    		return; 

    	if (!sourceFolder.exists())
    		return; 
    	
    	if (sourceFolder.isFile())
    		throw new IllegalArgumentException("Cannot sync folders from file '" + sourceFolder.getAbsolutePath() + "'") ;
    	
    	if (targetFolder.isFile())
    		throw new IllegalArgumentException("Cannot sync folder to file '" + targetFolder.getAbsolutePath() + "'");

    	if (!targetFolder.exists())
    		targetFolder.mkdirs(); 
    	
    	for (String filename : sourceFolder.list())
    	{
    		File src = new File(sourceFolder, filename); 
    		File target = new File(targetFolder, filename); 

    		if (src.isDirectory())
    			syncFolder(src, target, filter); 
    		else
    		{
    			if (filter.accept(sourceFolder, filename))
        			syncFile(src, target); 
    		}
    	}
    }
    
    public static void syncFile(File sourceFile, File targetFile)
    	throws IOException
    {
		if (sourceFile.isDirectory() || targetFile.isDirectory())
			throw new IllegalArgumentException("Failed to sync files: '" + sourceFile.getPath() + "' to '" + targetFile.getPath() + "'"); 

		if (!sourceFile.exists())
			return; 
		
		if (targetFile.exists())
		{
			if (targetFile.lastModified() > sourceFile.lastModified() && sourceFile.length() == targetFile.length())
				return; 

			targetFile.delete(); 
		}
		
		InputStream is = new FileInputStream(sourceFile); 
		OutputStream os = new FileOutputStream(targetFile); 
		try { IOUtils.copy(is, os); }
		finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os); 
		}
    }
}
