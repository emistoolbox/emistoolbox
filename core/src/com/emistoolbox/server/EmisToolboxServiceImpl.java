package com.emistoolbox.server;

import com.emistoolbox.client.EmisToolboxService;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.EmisAdminModuleData;
import com.emistoolbox.common.EmisReportModuleData;
import com.emistoolbox.common.excelMerge.ExcelReportUtil;
import com.emistoolbox.common.fileDialog.FileDirectoryInfo;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.impl.EmisDbMapTestImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationErrorHierarchy;
import com.emistoolbox.common.model.validation.EmisValidationResult;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.results.ExcelReportMetaResult;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.emistoolbox.common.util.ImportStatus;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.lib.highchart.renderer.HighchartPhantomJsFileRenderer;
import com.emistoolbox.lib.highchart.renderer.HighchartPhantomJsPostRenderer;
import com.emistoolbox.lib.highchart.renderer.HighchartRenderer;
import com.emistoolbox.server.excelMerge.ExcelTemplate;
import com.emistoolbox.server.excelMerge.impl.ExcelFileTemplateImpl;
import com.emistoolbox.server.excelMerge.impl.ExcelMergeDataSourceImpl;
import com.emistoolbox.server.mapping.DbDataSource;
import com.emistoolbox.server.mapping.DbResultSet;
import com.emistoolbox.server.mapping.DbUtil;
import com.emistoolbox.server.mapping.MapProcess;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;
import com.emistoolbox.server.renderer.HtmlFieldRenderer;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.ChartRendererImpl;
import com.emistoolbox.server.renderer.charts.impl.ChartUtil;
import com.emistoolbox.server.renderer.charts.impl.highcharts.HighchartChartRenderer;
import com.emistoolbox.server.renderer.gis.GisUtil;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.html.ResultToTableGenerator;
import com.emistoolbox.server.renderer.pdfreport.html.ResultToTableGeneratorImpl;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfUtil;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.pdflayout.PDFLayoutReportWriter;
import com.emistoolbox.server.results.ExcelResultCollector;
import com.emistoolbox.server.results.PriorityResultCollector;
import com.emistoolbox.server.results.TableResultCollector;
import com.emistoolbox.server.servlet.ApiUserAuthServlet;
import com.emistoolbox.server.util.CsvTableWriter;
import com.emistoolbox.server.util.ExcelTableWriter;
import com.emistoolbox.server.util.TableWriter;
import com.emistoolbox.server.validation.ValidationTask;
import com.emistoolbox.server.validation.ValidationTaskList;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

public class EmisToolboxServiceImpl extends RemoteServiceServlet implements EmisToolboxService
{
    private static final long serialVersionUID = 1L;
    private ResultToTableGenerator tableGenerator = new ResultToTableGeneratorImpl();

    private ChartRenderer renderer = getChartRenderer(); 
    
    private static ChartRenderer cachedChartRenderer = null; 
    public synchronized static ChartRenderer getChartRenderer()
    {
    	if (cachedChartRenderer != null)
    		return cachedChartRenderer; 
    	
    	String chartRenderer = EmisConfig.get(EmisConfig.EMISTOOLBOX_RENDERER_CHART, EmisConfig.RENDERER_CHART_JFREECHART);
    	if (EmisConfig.RENDERER_CHART_JFREECHART.equals(chartRenderer))
    		cachedChartRenderer = new ChartRendererImpl(); 
    	else if (EmisConfig.RENDERER_CHART_HIGHCHART.equals(chartRenderer))
    	{
    		HighchartRenderer renderer = null; 
    		
    		String phantomHost = EmisConfig.get(EmisConfig.EMISTOOLBOX_PHANTOMJS_SERVER, null); 
    		if (phantomHost == null)
    		{
    			String emisPath = EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH, ServerUtil.ROOT_PATH); 
	        	renderer = new HighchartPhantomJsFileRenderer(emisPath + "phantomjs",emisPath + "highcharts"); 
    		}
    		else
    			renderer = new HighchartPhantomJsPostRenderer(phantomHost, new Integer(EmisConfig.get(EmisConfig.EMISTOOLBOX_PHANTOMJS_PORT, "9999"))); 
    		
    		cachedChartRenderer = new HighchartChartRenderer(renderer); 
    	}
    	else
    		throw new IllegalArgumentException("Invalid chart renderer configuration."); 
    	
    	return cachedChartRenderer; 
    }
    
    private static PdfReportWriter cachedPdfRenderer = null; 
    
    public synchronized static PdfReportWriter getPdfRenderer()
    {
    	if (cachedPdfRenderer != null)
        	return cachedPdfRenderer; 

    	String pdfRenderer = EmisConfig.get(EmisConfig.EMISTOOLBOX_RENDERER_PDF, EmisConfig.RENDERER_PDF_ITEXT);
    	if (EmisConfig.RENDERER_PDF_ITEXT.equals(pdfRenderer))
        	cachedPdfRenderer = new ItextPdfReportWriter();    	
    	else if (EmisConfig.RENDERER_PDF_JORIKI.equals(pdfRenderer))
    	{
    		cachedPdfRenderer = new PDFLayoutReportWriter(null);
    		((PDFLayoutReportWriter) cachedPdfRenderer).setDebug(true); 
    	}
    	else
    		throw new IllegalArgumentException("Invalid pdf renderer configuration."); 

    	return cachedPdfRenderer; 
    }
    

    public String[] getDataSets(final boolean forAdmin)
    {
        File dir = new File(EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH, ServerUtil.ROOT_PATH)); 
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File data)
            {
                if (!data.isDirectory())
                    return false;

                String[] files = null; 
                if (forAdmin)
                    files = new String[] { "model.xml", "mapping.xml" }; 
                else 
                    files = new String[] { "model.xml", "mapping.xml", "data.bin" }; 
                
                for (String folder : files)
                {
                    if (!new File(data, folder).exists())
                        return false; 
                }
                
                return true; 
            }
        }); 
        
        if (files == null)
            return null; 
        
        String[] result = new String[files.length]; 
        for (int i = 0; i < files.length; i++)
            result[i] = files[i].getName(); 
        
        return result;
    }
    
    public EmisAdminModuleData createDataSet(String name)
        throws IOException
    {
        File dir = new File(EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH, ServerUtil.ROOT_PATH)); 
        File newDir = new File(dir, name); 
        if (!newDir.exists())
            newDir.mkdir(); 

        EmisAdminModuleData result = new EmisAdminModuleData(); 
        saveAdminData(name, result); 
        
        return result; 
    }

    public boolean changeDataSet(String name)
        throws IOException
    { 
        try { loadData(name); }
        catch (IOException ex)
        { return false; }
        
        return true; 
    }
    
    public ImportStatus getImportStatus(int id, int fromMessage) throws IOException
    {
        MapProcess process = ImportProcessList.get().get(id);
        if (process == null)
            return null;

        return process.getImportStatus(fromMessage);
    }

    public synchronized int testImportData(String name, EmisAdminModuleData adminData, EmisEntityDbMap testEntityMap, boolean withGis, boolean withHierarchy) throws IOException
    {
        EmisAdminModuleData testConfig = new EmisAdminModuleData();
        testConfig.setModel(adminData.getModel());
        EmisDbMapTestImpl mapping = new EmisDbMapTestImpl(adminData.getMapping(), testEntityMap);
        mapping.setWithGis(withGis);
        mapping.setWithHiearchy(withHierarchy);

        testConfig.setMapping(mapping);

        return importData(name, false, testConfig);
    }

    public static MapProcess runImport(String filename)
        throws IOException
    { return runImport(filename, null); }
    
    private static void setGlobalQuery(EmisDbMap mapping, String globalQuery)
    {
        for (DbDataSourceConfig ds : mapping.getDataSources())
        {
            if (ds.hasQueries())
                continue; 

            ds.setQuery(DbDataSourceConfig.GLOBAL_QUERY, globalQuery); 
        }
    }
    
    public static MapProcess runImport(String datasetName, String globalQuery)
        throws IOException
    {
        EmisAdminModuleData adminData = loadAdminDataInternal(datasetName); 
        if (!StringUtils.isEmpty(globalQuery))
            setGlobalQuery(adminData.getMapping(), globalQuery); 
        
        final MapProcess process = new MapProcess(adminData.getModel(), 50);
        process.setDataset(datasetName); 
        ImportProcessList.get().add(process);
        
        importTask(datasetName, process, adminData, true);
        
        return process; 
    }
    
    private static void importTask(String filename, MapProcess process, EmisAdminModuleData adminData, boolean saveResult)
        throws IOException
    {
            ImportProcessList.get().clearProcesses(); 

            process.importData(adminData.getMapping());
            if (saveResult)
            {
            	if (adminData.getMapping().getDateInitMappings().size() > 0)
            		EmisToolboxIO.saveModelXml(filename, process.getData().getMetaDataSet());
            	
                EmisToolboxIO.saveDataset(process.getData(), filename);
            }
            
            PrintStream out = null;
            try
            {
                File logFile = ServerUtil.getFile(filename, "data.log", true); 
                logFile.getParentFile().mkdirs(); 
                out = new PrintStream(new FileOutputStream(logFile));
                ServerUtil.trace(out, process.getData());
                out.flush();
            }
            finally
            { IOUtils.closeQuietly(out); }
            
            out = null;
            try
            {
                process.logErrorCount(); 
                out = new PrintStream(new FileOutputStream(ServerUtil.getFile(filename, "errors.log", true)));
                for (String error : process.getErrorMessages())
                    out.println(error);
                out.flush();
                        
                process.clearErrors(); 
            }
            finally
            { IOUtils.closeQuietly(out); }
    }
    
    public int importData(String filename, EmisAdminModuleData adminData) 
        throws IOException
    { return importData(filename, true, adminData); }
    
    private synchronized int importData(final String datasetName, final boolean saveResults, final EmisAdminModuleData adminData) throws IOException
    {
        if (saveResults)
            saveAdminData(datasetName, adminData);
        
        final MapProcess process = new MapProcess(adminData.getModel(), 50);
        process.setDataset(datasetName);
        int result = ImportProcessList.get().add(process);

        Thread t = new Thread() {
            public void run()
            {
                try
                { EmisToolboxServiceImpl.importTask(datasetName, process, adminData, saveResults); }
                catch (Throwable ex)
                { EmisToolboxServiceImpl.this.handleException(ex); }
            }
        };
        process.setThread(t); 
        t.start();
        
        return result;
    }

    public EmisAdminModuleData loadAdminData(String name)
        throws IOException
    { return EmisToolboxServiceImpl.loadAdminDataInternal(name); }
    
    public static EmisAdminModuleData loadAdminDataInternal(String name)
        throws IOException
    {
        EmisAdminModuleData result = null;
        try
        {
            result = new EmisAdminModuleData();

            EmisMeta meta = EmisToolboxIO.loadModelXml(name);    
            if (meta != null)
                result.setModel(meta);
            
            EmisDbMap dbMap = EmisToolboxIO.loadMappingXml(name, meta);
            if (dbMap != null)
                result.setMapping(dbMap);
            
            List<EmisValidation> validations = EmisToolboxIO.loadValidationXml(name, meta); 
            if (validations != null)
            	result.setValidations(validations); 
        }
        catch (Throwable err)
        {
            err.printStackTrace();
            throw ((IOException) (IOException) new IOException("Failed to load XML admin data.").initCause(err));
        }

        return result;
    }
    

    public List<EmisUser> getUsers()
        throws IOException
    {
        List<EmisUser> result = EmisToolboxIO.loadUsersXml();
        if (result == null)
            throw new IllegalArgumentException("Failed loading user file failed (missing file, or invalid validation)."); 

        return result; 
    }
    
    /** - successful login with restriction, return level. 
     *  - successful free login, return null 
     *  - failed login - throw exception
     */
    public AccessLevel getAccessLevelFromToken(String token, String ip)
        throws IOException
    {
        // viewer-127.0.0.1-default-ca4cd4d2a8d5a23a5f340e182aa0908c
        String parts[] = token.split("-"); 
        
        String verifiedToken = ApiUserAuthServlet.getToken(parts[0], parts[1], parts[2]);
        if (!verifiedToken.equals(token))
            throw new IOException("Token validation failed."); 

        if (parts[0].equals("viewer"))
            return AccessLevel.VIEWER;
        if (parts[1].equals("reportAdmin"))
            return AccessLevel.REPORT_ADMIN; 
        if (parts[1].equals("admin"))
            return AccessLevel.SYSTEM_ADMIN;
        
        throw new IOException("Access denied.");  
    }

    public void setUsers(List<EmisUser> users)
        throws IOException
    { EmisToolboxIO.saveUsersXml(users); }
    
    public String getPasswordHash(String password)
    { return DigestUtils.md5Hex(password); }
    
    public EmisReportModuleData loadReportData(String name) throws IOException
    {
        return loadReportData(name, null);
    }

    public EmisReportModuleData loadReportData(String name, EmisMeta meta) throws IOException
    {
        EmisReportModuleData result = new EmisReportModuleData();
        if (meta == null)
            meta = loadAdminData(name).getModel();

        result.setModel(meta);
        result.setReportConfig(EmisToolboxServiceImpl.getReportConfig(name, result.getModel())); 

        return result;
    }
    
    public static EmisReportConfig getReportConfig(String name, EmisMeta meta)
        throws IOException
    { return EmisToolboxIO.loadReportXml(name, meta); }

    public void saveReportData(String name, EmisReportModuleData data) throws IOException
    { 
    	try { EmisToolboxIO.saveReportXml(name, data.getReportConfig()); }
    	catch (RuntimeException err)
    	{
    		err.printStackTrace(System.out); 
    		throw err; 
    	} 
    }

    public void saveAdminData(String name, EmisAdminModuleData data) throws IOException
    {
        EmisToolboxIO.saveModelXml(name, data.getModel());
        EmisToolboxIO.saveMappingXml(name,  data.getMapping());
        EmisToolboxIO.saveValidationXml(name,  data.getValidations());
    }

    private static EmisDataSet getDataSet(String dataset) throws IOException
    { return EmisToolboxIO.loadDataset(dataset); } 

    public EmisReportModuleData loadData(String name) 
        throws IOException
    {
        try
        {
            EmisReportModuleData result = new EmisReportModuleData(); 
            result.setModel(EmisToolboxIO.loadDataset(name).getMetaDataSet());
            result.setReportConfig(EmisToolboxIO.loadReportXml(name, result.getModel())); 
            
            result.setConfig(EmisToolboxIO.loadProperties(name)); 
            
            return result; 
        }
        catch (IOException err)
        {
            err.printStackTrace();
            throw err; 
        }
    }

    private IOException handleException(Throwable ex)
    {
        Throwable t = ex;
        while (t != null)
        {
            t.printStackTrace();
            System.out.println("=====");
            t = t.getCause();
        }

        if ((ex instanceof IOException))
        {
            return (IOException) ex;
        }
        return new IOException(ex.getMessage());
    }

    public Map<String, List<String>> getDataSourceInfo(DbDataSourceConfig dbConfig, String dataset) throws IOException
    {
        try { 
            DbDataSource data = DbUtil.getDataSource(dbConfig, dataset);
            return data.getDataInfo();
        }
        catch (IOException ex)
        {
            Throwable iter = ex; 
            while (iter != null)
            {
                iter.printStackTrace();
                iter = iter.getCause(); 
            }
            
            throw ex; 
        }
    }

    public Set<String> getValues(DbContext context, String dataset, String field) throws IOException
    {
        Set<String> result = new HashSet<String>();
        DbDataSource data = DbUtil.getDataSource(context.getDataSource(), dataset);

        DbResultSet rs = null; 
        try { 
            rs = data.query(context.getQuery());

            int count = 0;
            while ((rs.next()) && (count < 300))
            {
                result.add(rs.get(field));
                count++;
            }

            return result;
        }
        finally
        { 
            if (rs != null)
                rs.close(); 
        } 
    }

    public Map<Integer, String> getHierarchyEntities(String dataset, String hierarchyName, int dateIndex, EmisMetaEntity parentEntity, int parentId) 
        throws IOException
    { return getHierarchyEntities(getDataSet(dataset), hierarchyName, dateIndex, parentEntity, parentId); }

    public List<EmisEntity> findEntityParents(String dataset, String hierarchyName, int dateIndex, EmisEntity entity) 
        	throws IOException
    {
    	List<EmisEntity> result = new ArrayList<EmisEntity>();

    	EmisDataSet emis = getDataSet(dataset);
    	EmisHierarchy hierarchy = emis.getHierarchy(hierarchyName);

    	NamedIndexList<EmisMetaEntity> entityTypes = hierarchy.getMetaHierarchy().getEntityOrder(); 
    	int entityIndex = NamedUtil.findIndex(entity.getEntityType(), entityTypes); 
    	if (entityIndex == -1)
    		return null; 

    	while (entityIndex > 0)
    	{
    		int id = hierarchy.findParentId(entity, dateIndex); 
    		if (id == -1)
    			return null; 

    		entityIndex--; 

    		entity = new Entity(entityTypes.get(entityIndex), id); 
    		result.add(0, entity); 
    	}
    	
    	getEntityNames(emis, hierarchy, result);

    	return result; 
    }
    
    public Map<String, String> findEntityNames(String dataset, List<String> entityIds)
    	throws IOException
    {
    	Map<String, String> result = new HashMap<String, String>();
    	
    	// Find hierarchy in which entities exist. 
    	// 
    	EmisDataSet emis = getDataSet(dataset); 

    	for (EmisMetaHierarchy hierarchy : emis.getMetaDataSet().getHierarchies())
    	{
    		List<EmisEntity> entities = new ArrayList<EmisEntity>(); 

    		Iterator<String> iter = entityIds.iterator(); 
    		while (iter.hasNext())
    		{
    			String id = iter.next();  
    			int pos = id.indexOf(":");
    			if (pos == -1)
    				continue; 
    			
    			EmisMetaEntity entityType = NamedUtil.find(id.substring(0, pos), hierarchy.getEntityOrder());
    			if (entityType == null)
    				continue; 

    			iter.remove(); 
    			try { 
    				EmisEntity entity = new Entity(); 
    				entity.setEntityType(entityType); 
    				entity.setId(Integer.parseInt(id.substring(pos + 1)));
    				entities.add(entity); 
    			}
    			catch (NumberFormatException ex) 
    			{}
    		}
    		
    		for (EmisEntity entity : getEntityNames(dataset, hierarchy.getName(), entities))
    			result.put(entity.getEntityType().getName() + ":" + entity.getId(), entity.getEntityType().getName() + ":" + entity.getName());
    	}

    	return result;
    }
    
    public List<EmisEntity> getEntityNames(String dataset, String hierarchyName, List<EmisEntity> entities)
    	throws IOException
    {
    	EmisDataSet emis = getDataSet(dataset); 
    	EmisHierarchy hierarchy = emis.getHierarchy(hierarchyName);
    	return getEntityNames(emis, hierarchy, entities); 
    }
    
    private List<EmisEntity> getEntityNames(EmisDataSet emis, EmisHierarchy hierarchy, List<EmisEntity> entities)
    {
    	for (EmisMetaEntity entityType : getEntityTypes(entities))
    		updateNames(emis, hierarchy, getEntities(entities, entityType));  
    	
    	return entities; 
    }
    
    private List<EmisMetaEntity> getEntityTypes(List<EmisEntity> entities)
    {
    	List<EmisMetaEntity> result = new ArrayList<EmisMetaEntity>(); 
    	for (EmisEntity entity : entities)
    	{
    		if (null == NamedUtil.find(entity.getEntityType().getName(), result))
    			result.add(entity.getEntityType()); 
    	}
    	
    	return result; 
    }
    
    private List<EmisEntity> getEntities(List<EmisEntity> entities, EmisMetaEntity entityType)
    {
    	List<EmisEntity> result = new ArrayList<EmisEntity>(); 
    	for (EmisEntity entity : entities)
    	{
    		if (NamedUtil.sameName(entity.getEntityType(), entityType))
    			result.add(entity); 
    	}

    	return result; 
    }
    
    /** Updates names for a single entity type */ 
    private void updateNames(EmisDataSet emis, EmisHierarchy hierarchy, List<EmisEntity> entities)
    {
    	if (entities == null || entities.size() == 0)
    		return; 

        EmisMetaEntity entityType = entities.get(0).getEntityType(); 

    	int[] ids = new int[entities.size()]; 
    	for (int i = 0; i < ids.length; i++) 
    	{
    		if (!NamedUtil.sameName(entities.get(i).getEntityType(), entityType))
    			throw new IllegalArgumentException("Not same type"); 
    		
    		ids[i] = entities.get(i).getId(); 
    	}
    	
        EmisEntityDataSet dataSet = emis.getEntityDataSet(entityType, (EmisMetaDateEnum) hierarchy.getDateType());

        Map<Integer, String> names = dataSet.getAllValues(0, "name", ids); 
        for (EmisEntity entity : entities)
        	entity.setName(names.get(entity.getId()));  
    }
    
    public static Map<Integer, String> getHierarchyEntities(EmisDataSet emis, String hierarchyName, int dateIndex, EmisMetaEntity parentEntity, int parentId) 
        throws IOException
    {
        EmisHierarchy hierarchy = emis.getHierarchy(hierarchyName);
        if (parentEntity == null)
            return getRootElements(emis, hierarchy, dateIndex);

        int[] childrenIds = hierarchy.getChildren(dateIndex, parentEntity, parentId);
        if ((childrenIds == null) || (childrenIds.length == 0))
            return new HashMap<Integer, String>();

        int parentEntityIndex = hierarchy.getMetaHierarchy().getEntityOrder().getIndex(parentEntity);
        EmisEntityDataSet dataSet = emis.getEntityDataSet((EmisMetaEntity) hierarchy.getMetaHierarchy().getEntityOrder().get(parentEntityIndex + 1), (EmisMetaDateEnum) hierarchy.getDateType());

        return dataSet.getAllValues(dateIndex, "name", childrenIds);
    }

    private static Map<Integer, String> getRootElements(EmisDataSet emis, EmisHierarchy hierarchy, int dateIndex) throws IOException
    {
        int[] ids = hierarchy.getRootElements(dateIndex);
        if (ids == null)
            return new HashMap<Integer, String>();

        EmisMetaEntity entityType = (EmisMetaEntity) hierarchy.getMetaHierarchy().getEntityOrder().get(0);
        EmisEntityDataSet dataSet = emis.getEntityDataSet(entityType, (EmisMetaDateEnum) hierarchy.getDateType());

        Map<Integer, String> result;
        if (dataSet == null)
            result = new HashMap<Integer, String>();
        else
            result = dataSet.getAllValues(dateIndex, "name", ids);
        for (int id : ids)
        {
            if (result.get(Integer.valueOf(id)) == null)
                result.put(Integer.valueOf(id), entityType.getName() + " " + id);
        }
        return result;
    }

    public String[] getRenderedResult(String dataset, TableMetaResult metaResult, int chartType) throws IOException
    {
        try
        {
            Result[] result = getResult(dataset, metaResult);

            File chartOutputFile = ServerUtil.getNewFile("charts", "bar", ".png");
            if (renderer instanceof HighchartChartRenderer)
            	chartOutputFile = ServerUtil.getNewFile("charts", "bar",  ".json"); 
            
            String chartResult = chartOutputFile.getName();
            if (((chartType == 1) || (chartType == 3)) && (result[0].getDimensions() == 1))
                chartType = 0;

            ChartConfig chartConfig = new ChartConfigImpl();
            ChartUtil.setMetaResultValueConfiguration(metaResult.getMetaResultValue(0), chartConfig);
            ChartUtil.scaleChartWidth(chartConfig, result[0], chartType == 0 ? 2 : 1);

            if (chartType == EmisToolboxService.CHART_BAR)
                this.renderer.renderBar(result[0], chartConfig, chartOutputFile);
            else if (chartType == EmisToolboxService.CHART_STACKED_BAR)
                this.renderer.renderStackedBar(result[0], chartConfig, chartOutputFile);
            else if (chartType == EmisToolboxService.CHART_PIE)
                this.renderer.renderPie(result[0], chartConfig, chartOutputFile);
            else if (chartType == EmisToolboxService.CHART_STACKED_BAR_NORMALIZED)
            {
                chartConfig.setMaxValue(100.0D);
                this.renderer.renderNormalizedStackedBar(result[0], chartConfig, chartOutputFile);
            }
            else if (chartType == EmisToolboxService.CHART_LINES)
                renderer.renderLines(result[0], chartConfig, chartOutputFile); 
            else
                chartResult = null;

            String tableResult = null;
            try
            { tableResult = this.tableGenerator.getHtmlTableAsString(result[1]); }
            catch (Throwable err)
            { err.printStackTrace(); }

            File xlsOutputFile = ServerUtil.getNewFile("reports", "xls", ".xls");
            String xlsResult = xlsOutputFile.getName();
            tableGenerator.getFileTable(result[1], new ExcelTableWriter(xlsOutputFile));

            return new String[] { chartResult, tableResult, xlsResult };
        }
        catch (Throwable err)
        {
            throw handleException(err);
        }
    }

    public String[] getRenderedGisResult(String dataset, GisMetaResult metaResult, String mapType) throws IOException
    {
        try 
        { return GisUtil.getGisResult(metaResult, getDataSet(dataset), mapType); }
        catch (Throwable err)
        { throw handleException(err); }
    }

    public String getRenderedReportResult(String dataset, ReportMetaResult metaResult) throws IOException
    { return getRenderedReportResultInternal(dataset, metaResult); }
    
    public static String getRenderedReportResultInternal(String dataset, ReportMetaResult metaResult) 
        throws IOException
    {
        File outputFile = ServerUtil.getNewFile("reports", "report", ".pdf");

        PdfReportWriter writer = getPdfRenderer(); 
        writer.setDateInfo(metaResult); 
        try
        { writer.writeReport(PdfUtil.getPdfReport(metaResult, getDataSet(dataset)), outputFile); }
        catch (PdfReportWriterException ex)
        { throw new IOException("Failed to write report to '" + outputFile.getName() + "'", ex); }
        
        return outputFile.getName();
    }

    // APITODO (name change)
    public String[] getExcelReportTemplates(String dataset) 
        throws IOException 
    {
        File templateDirectory = ServerUtil.getFile(dataset, "excel", false); 
        if (templateDirectory.exists() && templateDirectory.isDirectory())
        {
            File[] files = templateDirectory.listFiles(new FileFilter() {
                public boolean accept(File pathname)
                {
                    if (pathname.isDirectory() && pathname.getName().equals("excel"))
                        return true; 
                    
                    return pathname.getName().endsWith(".xls") || pathname.getName().endsWith(".xlsx");
                }
            }); 
            
            String[] result = new String[files.length]; 
            for (int i = 0; i < result.length; i++) 
                result[i] = files[i].getName(); 
            
            return result; 
        }
        
        return new String[] {}; 
    }

    public String[] getExcelReportSheets(String dataset, String excelFileName) 
        throws IOException
    { return ExcelFileTemplateImpl.getSheets(ServerUtil.getFile(dataset + "/excel", excelFileName, false)); }

    
    // APITODO - param change
    public String getRenderedExcelReportResult(String dataset, ExcelReportMetaResult metaResult, List<EmisIndicator> indicators) 
        throws IOException
    {
        File outputFile = ServerUtil.getNewFile("reports", "excelReport", ".xls"); 

        List<MetaResultValue> values = new ArrayList<MetaResultValue>(); 
        List<String> headers = new ArrayList<String>(); 
        ExcelReportUtil.addMetaResultValues(values,  headers, metaResult.getReportConfig(), indicators); 
        metaResult.setMetaResultValues(values); 
        metaResult.setHeaders(headers); 
        
        ExcelTemplate excelTemplate = new ExcelFileTemplateImpl(ServerUtil.getFile(dataset + "/excel", metaResult.getReportConfig().getTemplateFile(), false)); 
        excelTemplate.setMergeConfigs(metaResult.getReportConfig().getMergeConfigs()); 

        OutputStream out = null; 
        try { 
            ExcelResultCollector collector = new ExcelResultCollector(getDataSet(dataset), metaResult); 

            out = new FileOutputStream(outputFile); 
            excelTemplate.saveMergedDocument(out, new ExcelMergeDataSourceImpl(collector.getHeadings(), collector.getResults())); 
        }
        catch (Throwable err)
        {
            log("Failed to create Excel report.", err); 
            throw new IOException("Failed to create Excel report. " + err.getMessage()); 
        } 
        finally {
            IOUtils.closeQuietly(out); 
        }
        return outputFile.getName(); 
    }
    
    // APITODO 
    public List<PriorityListItem> getPriorityList(String dataset, PriorityMetaResult metaResult) throws IOException
    {
    	try { 
    		PriorityResultCollector collector = new PriorityResultCollector(getDataSet(dataset), metaResult);
    		return collector.getResults();
    	}
    	catch (IOException ex)
    	{ 
    		throw ex; 
    	} 
    	catch (Throwable err)
    	{ 
    		err.printStackTrace();  
    		return null; 
    	}
    }

    public Result[] getResult(String dataset, TableMetaResult metaResult) throws IOException
    { return TableResultCollector.getMultiResult(getDataSet(dataset), metaResult); }

    public MetaResult loadMetaResult(String tmpFile, final List<EmisIndicator> indicators) throws IOException
    {
        Document doc = EmisToolboxIO.readDocument(ServerUtil.getFile("temp", tmpFile, false));
        return new XmlReader().getMetaResult(doc.getDocumentElement(), indicators); 
    }

    public String saveMetaResult(MetaResult metaResult) throws IOException
    {
        File output = ServerUtil.getNewFile("temp", "metaResult", ".xml");
        EmisToolboxIO.writeDocument(new XmlWriter().getXml(metaResult), output);

        return "content?temp=" + output.getName();
    }

    public String savePriorityList(PriorityMetaResult metaResult, List<PriorityListItem> prioList, String ext) throws IOException
    {
        if ((!ext.equals(".csv")) && (!ext.equals(".xls")))
            throw new IllegalArgumentException("Invalid target file format.");

        File output = ServerUtil.getNewFile("reports", "prio", ext);
        TableWriter out = ext.equals(".csv") ? new CsvTableWriter(output) : new ExcelTableWriter(output);

        out.nextCell("ID");
        
        for (String field : metaResult.getAdditionalFields())
        {
        	if (field.equals("name"))
        		out.nextCell("Name");
        	else
        		out.nextCell(field);
        }

        for (MetaResultValue value : metaResult.getMetaResultValues())
            out.nextCell(value.getName(false));

        for (PriorityListItem item : prioList)
        {
            out.nextRow();
            out.nextCell("" + item.getId());
            for (String value : item.getEntityValues())
            	out.nextCell(value == null ? "" : value);
            
            double[] values = item.getValues();
            for (int i = 0; i < values.length; i++)
                out.nextCell(ServerUtil.getFormattedValue(metaResult.getMetaResultValue(i).getFormat(), values[i]));
        }

        out.close();

        return "content?report=" + output.getName();
    }

    public String getEntityData(String dataset, EmisEntity entity, EmisEnumTupleValue date) throws IOException
    {
    	try { 
        StringBuffer result = new StringBuffer(); 

        // Single value fields
        result.append("<table class='emisResult'>\n"); 
        result.append("<thead><tr><th>Field</th><th>Value</th></tr></thead>\n"); 
        result.append("<tbody><tr><th>ID</th><td>" + entity.getEntityType().getName() + ":" + entity.getId() + "</td></tr>\n"); 

        StringBuffer arrayResult = new StringBuffer(); 
        for (EmisMetaEnum targetDateType : date.getEnumTuple().getEnums())
        {
        	EmisEnumTupleValue dateValue = date.get((EmisMetaDateEnum) targetDateType); 
        	if (dateValue == null)
        		continue; 
        	
            EmisEntityDataSet entityDataSet = getDataSet(dataset).getEntityDataSet(entity.getEntityType(), (EmisMetaDateEnum) targetDateType);
            EmisEntityData entityData = entityDataSet.getData(dateValue.getIndex(), entity.getId());  
            if (entityData == null)
            {
            	append(result, "(" + targetDateType.getName() + ")", "No data"); 
            	continue; 
            } 
            
            for (EmisMetaData field : entity.getEntityType().getData())
            {
            	if (!NamedUtil.sameName(field.getDateType(), targetDateType))
            		continue; 

            	String info = field.getName() + " (" + targetDateType.getName() + ")"; 
            	if (field.getArrayDimensions() == null) 
            	{
                	// Add simple fields straight into the result. 
                	EntityDataAccess access = entityDataSet.getDataAccess(field.getName()); 
                	append(result, info, access.getAsString(entityData.getMasterArray())); 
            	}
            	else
            	{
            		// We want to display array results at the end - store them for now in a separate StringBuffer. 
                	arrayResult.append("<p>"); 
                	arrayResult.append(EmisToolbox.div(EmisToolbox.CSS_SECTION_BLUE, info + ":"));
                	EntityDataAccess access = entityDataSet.getDataAccess(field.getName());
                	if (access != null)
                	{
                		HtmlFieldRenderer renderer = new HtmlFieldRenderer(field, access, entityData); 
                		renderer.renderHtml(arrayResult); 
                	}
            	}
            }
        }
        
        result.append("</tbody></table>"); 
        result.append(arrayResult); 
        
        return result.toString(); 
    	}
    	catch (IOException ex)
    	{
    		ex.printStackTrace(); 
    		throw ex; 
    	}
    	catch (RuntimeException ex)
    	{ 
    		ex.printStackTrace(); 
    		throw ex; 
    	} 
    }
       
    private void append(StringBuffer result, String name, String value)
    {
        result.append("<tr><th>");
        result.append(name);
        result.append("</th><td>");
        if (value != null)
            result.append(value);
        else
            result.append("&nbsp;");
        
        result.append("</td></tr>\n");
    }

    public List<EmisEntity> getEntityChildren(String dataset, EmisMetaHierarchy hierarchy, int dateIndex, EmisEntity parent) throws IOException
    {
        EmisDataSet emis = getDataSet(dataset); 
        EmisHierarchy h = emis.getHierarchy(hierarchy.getName());
        if (parent == null)
            return getEntityList(getRootElements(emis, h, dateIndex), hierarchy.getEntityOrder().get(0));

        int[] childrenIds = h.getChildren(dateIndex, parent.getEntityType(), parent.getId());
        if ((childrenIds == null) || (childrenIds.length == 0))
            return new ArrayList<EmisEntity>();

        int parentEntityIndex = hierarchy.getEntityOrder().getIndex(parent.getEntityType());
        EmisMetaEntity entityType = hierarchy.getEntityOrder().get(parentEntityIndex + 1); 

        EmisEntityDataSet dataSet = getDataSet(dataset).getEntityDataSet(entityType, (EmisMetaDateEnum) h.getDateType());
        return getEntityList(dataSet.getAllValues(dateIndex, "name", childrenIds), entityType);
    }
    
    private List<EmisEntity> getEntityList(Map<Integer, String> idNameMap, EmisMetaEntity entityType)
    {
        List<EmisEntity> result = new ArrayList<EmisEntity>(); 
        for (Integer key : idNameMap.keySet())
        {
            EmisEntity entity = new Entity(entityType, key); 
            entity.setName(idNameMap.get(key)); 
            result.add(entity); 
        }

        NamedUtil.sort(result);
        
        return result; 
    }

	public FileDirectoryInfo fileDialogListDir(String path, String protocol, String dataset, String filter) 
	{
		if (dataset == null)
			return null; 
		
		String prefix = "[" + dataset + "]";

		// Path is relative to main directory. 
		if (StringUtils.isEmpty(path))
			path = "/";  

		if (!isValidDirectory(path))
			throw new IllegalArgumentException("Invalid directory:" + path); 
		
		String localPath = EmisToolboxIO.getPath(protocol + path, dataset);
		File files = new File(localPath);
		if (!files.isDirectory())
			files = files.getParentFile(); 

		if (!files.isDirectory())
			throw new IllegalArgumentException("Invalid directory");
		
		FileDirectoryInfo result = new FileDirectoryInfo(path, prefix);
		getFileFilter(filter); 
		for (File f : files.listFiles(getFileFilter(filter)))
		{
			if (f.isDirectory())
				result.addDirectory(f.getName());
			else
				result.addFile(f.getName());
		}

		return result;
	}

	public List<EmisValidation> loadValidations(String datasetName)
		throws IOException
	{
		try { 
			EmisMeta meta = loadAdminData(datasetName).getModel();
			return EmisToolboxIO.loadValidationXml(datasetName, meta); 
		}
		catch (IOException ex)
		{ ex.printStackTrace(); throw ex; } 
		catch (RuntimeException ex)
		{ ex.printStackTrace(); throw ex; }
	}
	
	public void saveValidations(String dataset, List<EmisValidation> validations)
		throws IOException
	{ EmisToolboxIO.saveValidationXml(dataset, validations); } 
	
	@Override
	public EmisValidationErrorHierarchy runValidation(String datasetName, ValidationMetaResult metaResult)
			throws IOException 
	{
		ValidationTask task = new ValidationTask(metaResult, loadValidations(datasetName), EmisToolboxIO.loadDataset(datasetName)); 
		try { return task.validate(); }
		catch (RuntimeException err)
		{
			err.printStackTrace(System.out); 
			throw err; 
		}
		
		// TODO - need to save errors and return an error reference (for future access). 
	}
	
	@Override
	public EmisValidationResult runValidationList(String dataset, ValidationMetaResult metaResult)
		throws IOException 
	{
		ValidationTaskList task = new ValidationTaskList(metaResult, loadValidations(dataset), EmisToolboxIO.loadDataset(dataset)); 
		try { return task.validate(); }
		catch (RuntimeException err)
		{
			err.printStackTrace(System.out); 
			throw err; 
		}
	}

	public String saveValidationResult(EmisValidationResult validationResult, String ext)
		throws IOException
	{
        if ((!ext.equals(".csv")) && (!ext.equals(".xls")))
            throw new IllegalArgumentException("Invalid target file format.");

        File output = ServerUtil.getNewFile("reports", "validation", ext);
        TableWriter out = ext.equals(".csv") ? new CsvTableWriter(output) : new ExcelTableWriter(output);
        ValidationTaskList.write(out, validationResult);
        out.close();

        return "content?report=" + output.getName();
	}
	
	private FileFilter getFileFilter(String filter)
	{
		if (StringUtils.isEmpty(filter) || filter.equals("*.*") || filter.equals("*"))
			return new FileFilter() { public boolean accept(File pathname) { return true; } }; 

		int pos = filter.indexOf("*"); 
		final String startWithFilter = pos > 0 ? filter.substring(0, pos) : null; 
		final String endWithFilter = pos < filter.length() - 1 ? filter.substring(pos + 1) : null; 

		return new FileFilter() {
			@Override
			public boolean accept(File f) 
			{
				if (f.isDirectory())
					return true; 

				if (startWithFilter != null && !f.getName().startsWith(startWithFilter))
					return false; 
				
				if (endWithFilter != null && !f.getName().endsWith(endWithFilter))
					return false; 

				return true; 
			} 
		}; 
	}
	
	public static boolean isValidDirectory(String dir)
	{ return dir.matches("^[a-zA-Z0-9/.///-]*") && !dir.contains(".."); }
}
