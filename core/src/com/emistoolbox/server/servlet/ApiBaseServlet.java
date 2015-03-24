package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;

import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultDimensionBuilder;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.EmisToolboxServiceImpl;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;

public abstract class ApiBaseServlet extends HttpServlet
{
    public static final String SYSTEM_API_SECRET = "emistoolbox.api.secret"; 
    public static final String QS_SECRET = "secret"; 
    public static final String QS_FORMAT = "format"; 
    public static final String QS_DATASET = "dataset"; 
    
    public static final String QS_INDICATOR = "indicator"; 
    public static final String QS_HIERARCHY = "hierarchy"; 
    public static final String QS_XAXIS = "xaxis"; 
    public static final String QS_SPLITBY = "splitBy"; 
    public static final String QS_DATE = "date"; 
    public static final String QS_LOCATION = "location"; 
    public static final String QS_PARENT_TYPE = "parentType"; 
    public static final String QS_PARENT_ID = "parentId"; 
    
    public static final String QS_TYPE = "type"; 
    public static final String QS_REPORT = "report";
    public static final String QS_REPORT_COLOURS = "colours"; 
    
    public static final String TYPE_HIERARCHIES = "hierarchies"; 
    public static final String TYPE_INDICATORS = "indicators"; 
    public static final String TYPE_XAXIS = "xaxis"; 
    public static final String TYPE_SPLITBY = "splitBy"; 
    public static final String TYPE_LOCATION = "locations"; 
    public static final String TYPE_DATE = "dates"; 

    private String secret = System.getProperty(SYSTEM_API_SECRET);  

    protected boolean hasParameter(HttpServletRequest req, HttpServletResponse resp, String param)
        throws IOException
    { return hasParameter(req, resp, param, (List<String>) null); }
    
    protected boolean hasParameter(HttpServletRequest req, HttpServletResponse resp, String param, String[] values)
        throws IOException
    {
        List<String> valuesAsList = new ArrayList<String>(); 
        for (String value : values)
            valuesAsList.add(value); 
        return hasParameter(req, resp, param, valuesAsList); 
    }
    
    protected boolean hasParameter(HttpServletRequest req, HttpServletResponse resp, String param, List<String> values)
        throws IOException
    {
        String value = req.getParameter(param); 
        if (StringUtils.isEmpty(value))
        {
            error(resp, 400, "Missing parameter '" + param + "'", asJson(req)); 
            return false; 
        }
        
        if (values != null)
        {
            StringBuffer allValues = new StringBuffer(); 
            for (String v : values)
            {
                if (v.equals(value))
                    return true; 
                
                if (allValues.length() > 0)
                    allValues.append(","); 
                allValues.append(v); 
            }
            
            String message = "Invalid parameter '" + param + "'"; 
            if (values != null)
                message += ". Should be one of: " + allValues; 
            
            error(resp, 400, message, asJson(req));
            return false; 
        }
        
        return true; 
    }
    
    protected boolean hasAccess(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        String qsSecret = req.getParameter(QS_SECRET); 
        if (StringUtils.isEmpty(qsSecret))
        {
            error(resp, 400, "Missing parameter 'secret'", asJson(req));
            return false; 
        }
        
        if (!qsSecret.equals(secret))
        {
        	System.out.println("Secret: " + qsSecret + " = " + secret); 
            error(resp, 403, "Invalid parameter 'secret'", asJson(req)); 
            return false; 
        }
        
        return true; 
    }
    
    protected boolean asJson(HttpServletRequest req)
    {
        String format = req.getParameter(QS_FORMAT); 
        return !StringUtils.isEmpty(format) && format.equals("json"); 
    }
    
    protected void error(HttpServletResponse resp, int status, String message, boolean asJson)
        throws IOException
    {
        if (asJson)
            out(resp, status, "\"" + message + "\"");
        else
            out(resp, status, "<error>" + message + "</error>");
    }
    
    protected void success(HttpServletRequest req, HttpServletResponse resp, String message)
        throws IOException
    { out(resp, 200, asJson(req) ? ("\"" + message + "\"") : ("<ok>" + message + "</ok>\n")); }

    protected void success(HttpServletRequest req, HttpServletResponse resp, String[] items)
        throws IOException
    {
        List<String> list = new ArrayList<String>(); 
        for (String item : items)
            list.add(item); 
                
        success(req, resp, list); 
    }
    
    protected void success(HttpServletRequest req, HttpServletResponse resp, Collection<String> items)
        throws IOException
    {
        StringBuffer content = new StringBuffer(); 
        if (asJson(req)) 
            addJsonContent(content, items); 
        else
            addXmlContent(content, items); 

        out(resp, 200, content.toString());
    }
    
    protected void success(HttpServletRequest req, HttpServletResponse resp, Result result, MetaResult metaResult)
        throws IOException
    {
        StringBuffer content = new StringBuffer(); 
        if (asJson(req))
            addJsonContent(content, result, metaResult); 
        else
            addXmlContent(content, result, metaResult); 
        
        out(resp, 200, content.toString()); 
    }

    protected void success(HttpServletRequest req, HttpServletResponse resp, List<EmisEntity> entities, boolean canSelect, boolean canDecent)
        throws IOException
    {
        Map<String, String> items = new HashMap<String, String>(); 
        for (EmisEntity entity : entities)
            items.put("" + entity.getId(), entity.getName()); 

        String type = entities.size() == 0 ? null : entities.get(0).getEntityType().getName();

        StringBuffer content = new StringBuffer(); 
        Map<String, String> attrs = new HashMap<String, String>(); 
        attrs.put("type", type); 
        if (canSelect)
            attrs.put("canSelect", "true"); 
        if (canDecent)
            attrs.put("canDecent", "true"); 
        if (asJson(req)) 
            addJsonContent(content, items, attrs);
        else
            addXmlContent(content, items, attrs);

        out(resp, 200, content.toString());
    }
    
    protected void success(HttpServletRequest req, HttpServletResponse resp, Map<String, String> items)
        throws IOException
    {
        StringBuffer content = new StringBuffer(); 
        if (asJson(req)) 
            addJsonContent(content, items, null); 
        else
            addXmlContent(content, items, null); 

        out(resp, 200, content.toString());
    }

    private void addJsonContent(StringBuffer content, Collection<String> items)
    {
        content.append("[\n"); 

        String delim = ""; 
        for (String item : items)
        {
            content.append(delim); 
            delim = ",\n"; 
            content.append("  \"" + StringEscapeUtils.escapeEcmaScript(item) + "\""); 
        }

        content.append("\n]\n"); 
    }
    
    private void addJsonContent(StringBuffer content, Map<String, String> items, Map<String, String> attrs)
    {
        content.append("{\n");
        String delim = ""; 
        if (attrs != null)
        {
	        for (Map.Entry<String, String> attr : attrs.entrySet())
	        {
	            content.append(delim); 
	            content.append("\"" + attr.getKey() + "\": \"" + attr.getValue() + "\"");  
	            delim = ",\n"; 
	        }
        }
        
        for (Map.Entry<String, String> entry : items.entrySet())
        {
            content.append(delim); 
            delim = ",\n"; 
            content.append("\"" + entry.getKey() + "\": \"" + StringEscapeUtils.escapeEcmaScript(entry.getValue()) + "\""); 
        }
                
        content.append("\n}\n"); 
    }
    
    private void addJsonContent(StringBuffer content, Result result, MetaResult metaResult)
    {
        content.append("{\n"); 
        content.append("  \"title\": \"" + StringEscapeUtils.escapeEcmaScript(MetaResultDimensionUtil.getTitle(metaResult)) + "\",\n"); 

        if (result.getDimensions() == 2)
        {
            content.append("  \"headers\": ["); 
            // Output headers. 
            for (int i = 0; i < result.getDimensionSize(1); i++) 
            {
                if (i > 0)
                    content.append(", "); 
                content.append("\"" + result.getHeading(1, i) + "\""); 
            }
            content.append("],"); 
        }
        
        content.append("  \"data\": [\n"); 
        for (int i = 0; i < result.getDimensionSize(0); i++) 
        {
            if (i > 0)
                content.append(",\n"); 
            content.append("    [\"" + result.getHeading(0,  i) + "\""); 
            if (result.getDimensions() == 1)
                content.append(", " + result.get(new int[] { i })); 
            else if (result.getDimensions() == 2)
            {
                for (int j = 0; j < result.getDimensionSize(1); j++)
                {
                    content.append(","); 
                    content.append(result.get(new int[] { i, j })); 
                }
            }
            content.append("]"); 
        }

        content.append("\n  ]\n}\n"); 
    }
    
    private void addXmlContent(StringBuffer content, Collection<String> items)
    {
        content.append("<items>\n"); 
        for (String item : items)
            content.append("  <item>" + StringEscapeUtils.escapeXml(item) + "</item>\n"); 

        content.append("</items>\n"); 
    }

    private void addXmlContent(StringBuffer content, Map<String, String> items, Map<String, String> attrs)
    {
        content.append("<items"); 
        if (attrs != null)
        {
        	for (Map.Entry<String, String> attr : attrs.entrySet())
        		content.append(" " + attr.getKey() + "=\"" + attr.getValue() + "\"");
        }
        content.append(">\n"); 

        for (Map.Entry<String, String> entry : items.entrySet())
            content.append("  <item id=\""+ entry.getKey() + "\">" + StringEscapeUtils.escapeEcmaScript(entry.getValue()) + "</item>\n"); 

        content.append("</items>\n"); 
    }
    
    private void addXmlContent(StringBuffer content, Result result, MetaResult metaResult)
    {
        content.append("<data title=\"" + StringEscapeUtils.escapeXml(MetaResultDimensionUtil.getTitle(metaResult)) + "\">\n"); 

        if (result.getDimensions() == 2)
        {
            // Output headers. 
            for (String header : result.getHeadings(1))
                content.append("  <header>" + header + "</header>\n"); 
        }
        
        for (int i = 0; i < result.getDimensionSize(0); i++) 
        {
            content.append("  <row header=\"" + result.getHeading(0,  i) + "\">"); 
            if (result.getDimensions() == 1)
                content.append(result.get(new int[] { i })); 
            else if (result.getDimensions() == 2)
            {
                for (int j = 0; j < result.getDimensionSize(1); j++)
                {
                    if (j > 0)
                        content.append(","); 
                    content.append(result.get(new int[] { i, j })); 
                }
            }

            content.append("</row>\n"); 
        }

        content.append("</data>\n"); 
    }
    
    private void out(HttpServletResponse resp, int status, String content)
        throws IOException
    {
        resp.setStatus(status); 
        PrintWriter out = null; 
        try { 
            out = new PrintWriter(new OutputStreamWriter(resp.getOutputStream()));  
            out.print(content); 
        }
        finally
        {
            if (out != null)
                out.flush(); 
            IOUtils.closeQuietly(out); 
        }
    }
    
    protected List<EmisIndicator> getIndicators(EmisMeta meta)
        throws IOException
    {
        EmisReportConfig config = EmisToolboxServiceImpl.getReportConfig(meta.getDatasetName(), meta);    
        return config.getIndicators(); 
    }

    protected EmisMetaHierarchy getHierarchy(HttpServletRequest req, String qs, EmisMeta meta)
    {
        String value = req.getParameter(qs);
        if (StringUtils.isEmpty(value))
            return null; 
        
        return NamedUtil.find(value,  meta.getHierarchies()); 
    }
    
    protected EmisIndicator getIndicator(String value, EmisMeta meta)
        throws IOException
    {
        if (StringUtils.isEmpty(value))
            return null; 

        return NamedUtil.find(value, getIndicators(meta));  
    }

    protected MessageAdmin getMessageAdmin(HttpServletRequest req)
    {
        try { return (MessageAdmin) Proxy.newProxyInstance(MessageAdmin.class.getClassLoader(), new Class[] { MessageAdmin.class }, new MessageHandler(MessageAdmin.class, null)); }
        catch (Exception ex) 
        {
            // No properties found - return default implementation with just the key name. 
            return (MessageAdmin) Proxy.newProxyInstance(MessageAdmin.class.getClassLoader(), new Class[] { MessageAdmin.class }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                { return "(" + method.getName() + ")"; }
            });
        }
    }

    protected boolean setMetaResult(MetaResult metaResult, HttpServletRequest req, HttpServletResponse resp, EmisDataSet emis)
        throws IOException
    {
        EmisMeta meta = emis.getMetaDataSet(); 
        EmisMetaHierarchy hierarchy = getHierarchy(req, QS_HIERARCHY, meta);

        List<EmisMetaEntity> entityTypes = hierarchy.getEntityOrder(); 
        int[] pathIds = getEntityPathIds(emis, hierarchy, req.getParameter(QS_LOCATION), 0); 

        int entityIndex = pathIds.length -1; 
        EmisContext context = getContext(req, resp, pathIds == null || pathIds.length == 0 ? null : new Entity(entityTypes.get(entityIndex), pathIds[entityIndex]), meta); 
        if (context == null)
            return false; 

        metaResult.setContext(context); 
        metaResult.setHierarchy(hierarchy); 
        if (req.getParameter(QS_INDICATOR) != null)
            metaResult.setIndicator(getIndicator(req.getParameter(QS_INDICATOR), meta)); 
        
        return true; 
    }
    
    protected MetaResultDimension getMetaResultDimension(HttpServletRequest req, String qs, EmisMetaHierarchy hierarchy, EmisIndicator indicator, EmisMeta meta)
    {
        String value = req.getParameter(qs); 
        if (StringUtils.isEmpty(qs))
            return null; 
        
        final List<MetaResultDimension> dims = new ArrayList<MetaResultDimension>(); 
        MetaResultDimensionUtil.addDimensions(getMessageAdmin(req), new MetaResultDimensionBuilder() {
            public void add(String sectionTitle)
            {}

            public void add(MetaResultDimension dim)
            { dims.add(dim); } 
        }, hierarchy, indicator, null, meta); 
        
        return NamedUtil.find(value,  dims); 
    }

    protected boolean hasParameters(HttpServletRequest req, HttpServletResponse resp, List<String> hieraries, List<String> indicators, List<String> xaxis, List<String> splitBy) throws IOException
    {
        if (hieraries != null && !hasParameter(req, resp, QS_HIERARCHY, hieraries))
            return false; 
        
        if (indicators != null && !hasParameter(req, resp, QS_INDICATOR, indicators))
            return false; 
        
        if (xaxis != null && !hasParameter(req, resp, QS_XAXIS, xaxis))
            return false; 
        
        if (splitBy != null && !StringUtils.isEmpty(req.getParameter(QS_SPLITBY)))
        {
            if (!hasParameter(req, resp, QS_SPLITBY, splitBy))
                return false; 
        }
        
        return true; 
    }

    protected List<MetaResultDimension> getDimensions(HttpServletRequest req, String qsIgnoreDimension, String dataName, EmisDataSet emis) throws IOException
    {
        final List<MetaResultDimension> result = new ArrayList<MetaResultDimension>(); 
        
        MetaResultDimensionBuilder builder = new MetaResultDimensionBuilder() {
            public void add(String sectionTitle)
            {}
    
            public void add(MetaResultDimension dim)
            { result.add(dim); }
        }; 
        
        EmisMetaHierarchy hierarchy = getHierarchy(req, QS_HIERARCHY, emis.getMetaDataSet()); 
        EmisIndicator indicator = getIndicator(req.getParameter(QS_INDICATOR), emis.getMetaDataSet()); 
        MetaResultDimension ignoreDimension = getMetaResultDimension(req, qsIgnoreDimension, hierarchy, indicator, emis.getMetaDataSet()); 
        
        MetaResultDimensionUtil.addDimensions(getMessageAdmin(req), builder, hierarchy, indicator, ignoreDimension, emis.getMetaDataSet()); 
        
        return result; 
    }

    protected int[] getEntityPathIds(EmisDataSet emis, EmisMetaHierarchy metaHierarchy, String path, int dateIndex)
    {
        if (StringUtils.isEmpty(path))
            return null; 
        
        int pos = path.indexOf(":");  
        if (pos != -1)
            return findIdsFromEntity(emis, metaHierarchy, path.substring(0, pos), Integer.parseInt(path.substring(pos + 1)), dateIndex);
        
        String[] ids = path.split(",");
        if (metaHierarchy.getEntityOrder().size() < ids.length)
            return null; 
        
        int[] result = new int[ids.length];
        for (int i = 0; i < result.length; i++)
        {
            try { result[i] = Integer.parseInt(ids[i]); }
            catch (NumberFormatException ex)
            { return null; } 
        }
        
        return result; 
    }
    
    private int[] findIdsFromEntity(EmisDataSet emis, EmisMetaHierarchy metaHierarchy, String entityTypeName, int entityId, int dateIndex)
    {
        EmisHierarchy hierarchy = emis.getHierarchy(metaHierarchy.getName());
        NamedIndexList<EmisMetaEntity> entityOrder = metaHierarchy.getEntityOrder();         
        
        int entityTypeIndex = NamedUtil.findIndex(entityTypeName, entityOrder);
        if (entityTypeIndex == -1)
            throw new IllegalArgumentException("The entity '" + entityTypeName + "' is not part of the hierarchy '" + metaHierarchy.getName() + "'.");
        
        int[] ids = new int[entityTypeIndex + 1];
        ids[entityTypeIndex] = entityId; 
        
        while (entityTypeIndex > 0 && entityTypeIndex < ids.length)
        {
            EmisEntity child = new Entity(entityOrder.get(entityTypeIndex), ids[entityTypeIndex]); 
            ids[entityTypeIndex - 1] = hierarchy.findParentId(child, dateIndex);
            
            if (ids[entityTypeIndex -1] == -1)
                throw new IllegalArgumentException("" + child.toString() + " does not exist, or cannot be reached from the root element.");
            entityTypeIndex--; 
        }
        
        return ids; 
    }
    
    protected String[] getEntityPathNames(EmisDataSet emis, int[] entityIds, List<EmisMetaEntity> entityOrder, int dateIndex)
    { 
        String[] entityNames = new String[entityIds.length]; 
        for (int i = 0; i < entityNames.length; i++)
        {
            EmisMetaEntity entityType= entityOrder.get(i); 
            EmisEntityDataSet entityDataSet = emis.getEntityDataSet(entityType, emis.getMetaDataSet().getDefaultDateType()); 
            EmisEntityData data = entityDataSet.getData(dateIndex, entityIds[i]);
            
            EntityDataAccess access = entityDataSet.getDataAccess("name"); 
            entityNames[i] = access.getAsString(data.getMasterArray());  
        }
        
        return entityNames; 
    }
    
    protected void adjustContext(Context context, boolean withEntity, boolean withDate)
    {
        if (!withEntity)
        {
            context.setEntityType(null); 
            context.setEntities(null); 
            context.setHierarchyDateIndex(-1); 
        }
        
        if (!withDate)
        {
            context.setDates(null); 
            context.setDateType(null); 
        }
    }
    
    protected EmisContext getContext(HttpServletRequest req, HttpServletResponse resp, EmisEntity entity, EmisMeta meta)
    {
        Context result = new Context();

        if (entity != null)
        {
            List<EmisEntity> entities = new ArrayList<EmisEntity>();
            result.setEntityType(entity.getEntityType());
            entities.add(entity);
            result.setEntities(entities);
            result.setHierarchyDateIndex(meta.getDefaultDateTypeIndex());
        }

        String date = req.getParameter(QS_DATE); 
        if (!StringUtils.isEmpty(date))
        {
            List<EmisEnumTupleValue> dates = new ArrayList<EmisEnumTupleValue>();
            dates.add(getDateEnumValue(meta.getDateEnums().get(0), date));
            result.setDates(dates);
            result.setDateType((EmisMetaDateEnum) dates.get(0).getEnumTuple());
        }

        return result; 
    }

    protected EmisEnumTupleValue getDateEnumValue(EmisMetaDateEnum dateType, String date)
    {
        EnumTupleValueImpl value = new EnumTupleValueImpl(); 
        value.setEnumTuple(dateType); 
        value.setValue(new String[] { date }); 

        return value; 
    }
}

class MessageHandler implements InvocationHandler
{
    private Properties messages = new Properties();
    private Method formatMethod; 
    
    public MessageHandler(Class clazz, String lang)
        throws Exception
    {
        String name = clazz.getName(); 
        int pos = name.lastIndexOf("."); 
        if (pos != -1)
            name = name.substring(pos + 1); 

        messages.load(clazz.getResourceAsStream(name + (lang == null ? "" : "_" + lang) + ".properties"));
        
        formatMethod = MessageFormat.class.getMethod("format", new Class[] { String.class, Object[].class });
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) 
        throws Throwable
    { 
        String message = messages.getProperty(method.getName()); 
        if (message == null)
            return ""; 

        if (args == null || args.length == 0)
            return message; 
        
        return formatMethod.invoke(null, new Object[] {message, args}); 
    }
}

