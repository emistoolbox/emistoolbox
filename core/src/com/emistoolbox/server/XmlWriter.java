package com.emistoolbox.server;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisAggregatorList;
import com.emistoolbox.common.model.analysis.EmisSampleAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisIndicatorRatio;
import com.emistoolbox.common.model.analysis.EmisIndicatorSimple;
import com.emistoolbox.common.model.analysis.EmisIndicatorTimeRatio;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.analysis.FilterTarget;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.analysis.impl.WeightedAggregatorDef;
import com.emistoolbox.common.model.mapping.DataSourceReplace;
import com.emistoolbox.common.model.mapping.DataSourceReplaceSet;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigAccess;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigCsv;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigExcel;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigFile;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigGeo;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigHsqldb;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowConstAccess;
import com.emistoolbox.common.model.mapping.DbRowContextAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.mapping.DbRowFieldAccess;
import com.emistoolbox.common.model.mapping.DbRowMultipleAccess;
import com.emistoolbox.common.model.mapping.EmisDateInitDbMap;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.mapping.EntityBaseDbMap;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigMultiple;
import com.emistoolbox.common.model.mapping.impl.DbRowAccessMultipleAccessIndex;
import com.emistoolbox.common.model.mapping.impl.DbRowArrayAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaGroupEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.model.meta.GisLayer;
import com.emistoolbox.common.model.priolist.PriorityReportConfig;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.model.validation.impl.ValidationMinMaxRuleImpl;
import com.emistoolbox.common.model.validation.impl.ValidationNotExceedingRule;
import com.emistoolbox.common.model.validation.impl.ValidationRatioRuleImpl;
import com.emistoolbox.common.model.validation.impl.ValidationTimeRatioRuleImpl;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfGisContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfPriorityListContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutBorderConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityAncestors;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityGrandChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.excelMerge.ExcelReportConfigSerializer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlWriter
{
    private Document doc;

    private Element createDocumentAndRoot(String root)
    {
        this.doc = new DocumentImpl();
        return createElementAndAdd(root, this.doc);
    }

    public synchronized Document getXml(EmisMeta meta)
    {
        addXml(createDocumentAndRoot("emisMeta"), meta);
        return this.doc;
    }

    public synchronized Document getXml(EmisDbMap map)
    {
    	map.updateDimensions(); 
        addXml(createDocumentAndRoot("emisMapping"), map);
        return this.doc;
    }

    public synchronized Document getXml(MetaResult metaResult)
    {
        Element root = createDocumentAndRoot("metaResult");
        if ((metaResult instanceof TableMetaResult))
            addXml(root, (TableMetaResult) metaResult);
        else if ((metaResult instanceof GisMetaResult))
            addXml(root, (GisMetaResult) metaResult);
        else if (metaResult instanceof PriorityMetaResult)
        	addXml(root, (PriorityMetaResult) metaResult); 
        else
            return null;

        return this.doc;
    }
    
    public synchronized Document getXmlValidations(List<EmisValidation> validations)
    {
    	Element root = createDocumentAndRoot("emisValidation"); 
    	for (EmisValidation validation : validations)
    		addXml(root, validation); 
    	
    	return this.doc; 
    }
    
    private void addXml(Element parent, EmisValidation validation)
    {
    	Element tag = createElementAndAdd("validation", parent); 
    	tag.setAttribute("name", validation.getName()); 
    	tag.setAttribute("entity", validation.getEntityType().getName());
    	
    	for (EmisMetaData field : validation.getAdditionalFields())
    	{
    		Element fieldTag = createElementAndAdd("displayField", tag); 
    		fieldTag.setAttribute("entity", field.getEntity().getName());
    		fieldTag.setAttribute("field", field.getName());
    	}

    	for (EmisValidationRule rule : validation.getRules())
    	{
    		Element ruleTag = null; 
    		if (rule instanceof ValidationRatioRuleImpl)
    			ruleTag = createElementAndAdd("ratio", tag); 
    		else if (rule instanceof ValidationMinMaxRuleImpl)
    			ruleTag = createElementAndAdd("minMax", tag); 
    		else if (rule instanceof ValidationNotExceedingRule)
    			ruleTag = createElementAndAdd("notExceeding", tag); 
    		else if (rule instanceof ValidationTimeRatioRuleImpl)
    			ruleTag = createElementAndAdd("timeRatio", tag); 

    		ruleTag.setAttribute("name", rule.getName());
    		if (!StringUtils.isEmpty(rule.getDescription()))
    			ruleTag.setAttribute("description", rule.getDescription());

    		if (rule instanceof ValidationMinMaxRuleImpl)
    		{
    			ValidationMinMaxRuleImpl minMaxRule = (ValidationMinMaxRuleImpl) rule; 
	    		if (minMaxRule.getMinValue() != null)
	    			ruleTag.setAttribute("min", "" + minMaxRule.getMinValue()); 
	    		if (minMaxRule.getMaxValue() != null)
	    			ruleTag.setAttribute("max", "" + minMaxRule.getMaxValue()); 
    		}
    		
    		addXml(ruleTag, "groupBy", rule.getGroupBy(), true); 
    		for (int i = 0; i < rule.getFieldCount(); i++)
    			addXml(ruleTag, rule.getField(i), rule.getFilter(i)); 
    	}
    }

    private void addXml(Element tag, String attributeName, EmisValidationFilter filter, boolean useShortAllValues)
    {
    	if (filter == null)
    		return; 
    	
    	StringBuffer result = new StringBuffer(); 
    	for (Map.Entry<String, EmisEnumSet> entry : filter.getFilters().entrySet())
    	{
    		if (result.length() > 0)
    			result.append(";"); 

    		result.append(entry.getKey()); 
    	
    		EmisEnumSet enumValues = entry.getValue(); 
    		if (enumValues.hasAllValues() && useShortAllValues)
    			continue;

    		result.append("="); 
    		
    		String delim = ""; 
    		for (String v : enumValues.getAll())
    		{
    			result.append(delim); 
    			result.append(v); 
    			delim = ","; 
    		}
    	}
    	
    	tag.setAttribute(attributeName, result.toString());
    }
    
    private void addXml(Element tag, EmisMetaData field, EmisValidationFilter filter)
    {
    	Element fieldTag = createElementAndAdd("field", tag); 
    	fieldTag.setAttribute("field", field.getEntity().getName() + "." + field.getName());
    	addXml(fieldTag, "filter", filter, false); 
    }

    public synchronized Document getXml(List<EmisUser> users)
    {
        Element root = createDocumentAndRoot("users");
        
        for (EmisUser user : users)
            addXml(root, user); 

        root.setAttribute("md5", getMD5(users)); 
        return doc; 
    }

    private void addXml(Element parent, EmisUser user)
    {
        Element tag = createElementAndAdd("user", parent); 
        tag.setAttribute("uid", user.getUsername());
        if (!StringUtils.isEmpty(user.getPassword()))
            user.setPasswordHash(DigestUtils.md5Hex(user.getPassword())); 
            
        tag.setAttribute("pwd", user.getPasswordHash()); 
        tag.setAttribute("level", "" + user.getAccessLevel());
        if (user.getDataset() != null)
        {
        	tag.setAttribute("dataset", user.getDataset());
        	
        	List<String> entityTypes = user.getRootEntityTypes(); 
        	List<Integer> entityIds = user.getRootEntityIds(); 
        	if (entityTypes != null && entityIds != null)
        	{
	        	for (int i = 0; i < Math.min(entityTypes.size(), entityIds.size()); i++) 
	        	{
	        		Element childTag = createElementAndAdd("rootEntity", tag); 
	        		childTag.setAttribute("entityType", entityTypes.get(i)); 
	        		childTag.setAttribute("entityId", "" + entityIds.get(i)); 
	        	}
        	}
        }
    }
    
    public static String getMD5(List<EmisUser> users)
    {
        StringBuffer buffer = new StringBuffer(); 
        if (users == null || users.size() == 0)
            buffer.append("(none)"); 
        else
        {
            buffer.append("EMISToolbox"); 
            for (EmisUser user : users)
            {
                buffer.append(user.getUsername());
                buffer.append(user.getPasswordHash());
                buffer.append(user.getAccessLevel()); 
                if (user.getDataset() != null)
                {
                	buffer.append(user.getDataset()); 

                	if (user.getRootEntityTypes() != null)
                		for (String type : user.getRootEntityTypes())
                			buffer.append(type); 

                	if (user.getRootEntityIds() != null)
                		for (Integer id : user.getRootEntityIds())
                			buffer.append(id); 
                }
                
                buffer.append("\n* "); 
            }
            buffer.append("ToolboxEMIS"); 
        }
        
        return DigestUtils.md5Hex(buffer.toString()); 
    }

    private void addXml(Element parent, EmisMeta meta)
    {
        Element tag = createElementAndAdd("enums", parent);
        for (EmisMetaEnum enumType : meta.getEnums())
        {
            addXml(tag, enumType);
        }
        tag = createElementAndAdd("entities", parent);
        for (EmisMetaEntity entity : meta.getEntities())
        {
            addXml(tag, entity);
        }
        tag = createElementAndAdd("dates", parent);
        for (EmisMetaDateEnum date : meta.getDateEnums())
        {
            addXml(tag, date);
        }
        tag = createElementAndAdd("hierarchies", parent);
        for (EmisMetaHierarchy hierarchy : meta.getHierarchies())
            addXml(tag, hierarchy);

        GisContext gisContext = meta.getGisContext(); 
        if (gisContext != null && gisContext.hasAnyValues())
        {
            tag = createElementAndAdd("gisContext", parent);
            setAttr(tag, "projection", gisContext.getProjection());
            setIds(tag, "boundary", gisContext.getBaseLayerBoundary()); 

            if (gisContext.getBaseLayerImage() != null )
            {
                Element imageTag = createElementAndAdd("baseImage", tag); 
                setAttr(imageTag, "image", gisContext.getBaseLayerImage()); 
                setIds(imageTag, "size", gisContext.getBaseLayerImageSize()); 
            }
            
            for (GisLayer layer : gisContext.getGisLayers())
                addXml(tag, layer); 
        }
    }
    
    private void addXml(Element parent, GisLayer layer)
    {
        Element tag = createElementAndAdd("gisLayer", parent); 
        tag.setAttribute("name", layer.getName()); 
        tag.setAttribute("path", layer.getPath());
        if (!StringUtils.isEmpty(layer.getStyle()))
            tag.setTextContent(layer.getStyle()); 
    }

    private Element createElementAndAdd(String name, Node parent)
    {
        Element result = this.doc.createElement(name);
        parent.appendChild(result);
        return result;
    }

    private void addXml(Element parent, EmisMetaEntity entity)
    {
        Element entityTag = createElementAndAdd("entity", parent);
        setAttr(entityTag, "id", entity);
        entityTag.setAttribute("gisType", entity.getGisType().toString());

        for (EmisMetaData data : entity.getData())
            addXml(entityTag, data);
    }

    private void addXml(Element parent, EmisMetaEnum enumType)
    {
        Element enumTag = createElementAndAdd("enum", parent);
        setAttr(enumTag, "id", enumType);
        setIds(enumTag, "values", enumType.getValues());
    }

    private void addXml(Element parent, EmisMetaData data)
    {
        Element dataTag = createElementAndAdd("data", parent);
        setAttr(dataTag, "id", data);
        setAttr(dataTag, "date", data.getDateType());
        setAttr(dataTag, "entity", data.getEntity());
        setAttr(dataTag, "dataType", data.getType());
        setAttr(dataTag, "planningResource", "" + data.isPlanningResource());
        if (data.getType() == EmisMetaData.EmisDataType.ENUM || data.getType() == EmisMetaData.EmisDataType.ENUM_SET)
            setAttr(dataTag, "enum", data.getEnumType());
        setIds(dataTag, "dimensions", data.getArrayDimensions());
    }

    private void addXml(Element parent, EmisMetaDateEnum date)
    {
        Element dateTag = createElementAndAdd("dateEnum", parent);
        setAttr(dateTag, "id", date);
        setAttr(dateTag, "parent", date.getParent());
        setIds(dateTag, "values", date.getValues());
        if (date.hasAllowDynamicInit())
        	setAttr(dateTag, "dateInit", "true"); 
    }

    private void addXml(Element parent, EmisMetaHierarchy hierarchy)
    {
        Element hierarchyTag = createElementAndAdd("hierarchy", parent);
        setAttr(hierarchyTag, "id", hierarchy);
        setIds(hierarchyTag, "entities", hierarchy.getEntityOrder());
    }

    private void addXml(Element mapTag, EmisDbMap map)
    {
        Element tag = createElementAndAdd("dbConfigs", mapTag);
        for (DbDataSourceConfig config : map.getDataSources())
        {
            addXml(tag, config);
        }
        tag = createElementAndAdd("dateInits", mapTag); 
        for (EmisDateInitDbMap dateInitMap : map.getDateInitMappings())
        	addXml(tag, dateInitMap, map); 
        
        tag = createElementAndAdd("entityMaps", mapTag);
        for (EmisEntityDbMap entityMap : map.getEntityMappings())
            addXml(tag, entityMap, map);

        tag = createElementAndAdd("gisEntityMaps", mapTag);
        for (GisEntityDbMap gisMap : map.getGisEntityMappings())
        {
            addXml(tag, gisMap, map);
        }
        tag = createElementAndAdd("hierarchyMaps", mapTag);
        for (EmisHierarchyDbMap hierarchyMap : map.getHierarchyMappings())
        {
            Element hierarchyTag = createElementAndAdd("hierarchyMap", tag);
            setAttr(hierarchyTag, "id", hierarchyMap.getHierarchy());
            for (EmisHierarchyDbMapEntry entry : hierarchyMap.getMappings())
                addXml(hierarchyTag, entry, map);
        }
    }

    private String getFileType(DbDataSourceConfig config)
    {
        if ((config instanceof DbDataSourceConfigAccess))
            return "ACCESS";
        if (config instanceof DbDataSourceConfigCsv)
        	return "CSV"; 
        if ((config instanceof DbDataSourceConfigExcel))
            return "EXCEL";
        if ((config instanceof DbDataSourceConfigGeo))
            return "GIS_SHAPE";
        if ((config instanceof DbDataSourceConfigHsqldb))
        	return "HSQL"; 

        return "";
    }

    private void addXml(Element parent, EmisDateInitDbMap dateInitMap, EmisDbMap dbmap)
    {
    	Element tag = createElementAndAdd("dateInit", parent); 
    	addXml(tag, dateInitMap.getDbContext(), dbmap);
    	addXml(tag, null, dateInitMap.getValueAccess()); 
    	setAttr(tag, "dateType", dateInitMap.getDateType()); 
    }
    
    private void addXml(Element parent, DbDataSourceConfig config)
    {
        Element tag = createElementAndAdd("dbConfig", parent);

        if (config.hasQueries())
        {
            for (String key : config.getQueries().keySet())
            {
                Element queryTag = createElementAndAdd("query", tag);
                setAttr(queryTag, "id", key);
                queryTag.setTextContent(config.getQuery(key));
            }
        }

        if ((config instanceof DbDataSourceConfigFile))
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) config;
            setAttr(tag, "type", getFileType(config));
            setAttr(tag, "contextName", fileConfig.getContextName());

            for (DbDataFileSource fileSource : fileConfig.getFileSources())
            {
                Element fileTag = createElementAndAdd("file", tag);
                setAttr(fileTag, "url", fileSource.getUrl()); 
                setAttr(fileTag, "contextValue", fileSource.getContextValue()); 
                setAttr(fileTag, "cacheFilename", fileSource.getCacheFilename()); 
            }
            
            if (config instanceof DbDataSourceConfigCsv)
            {
            	DataSourceReplaceSet replace = ((DbDataSourceConfigCsv) config).getReplace();
            	if (replace !=  null)
            	{
                	Element child = createElementAndAdd("replace", tag);
                	for (DataSourceReplace item : replace.getAll())
                	{
                		Element entryTag = createElementAndAdd("entry", child); 
                		entryTag.setAttribute("col", item.getColumn()); 
                		entryTag.setAttribute("key", item.getNeedle()); 
                		entryTag.setAttribute("value", item.getReplacement());
                	}
            	}
            }
        }
        else if ((config instanceof DbDataSourceConfigJdbc))
        {
            DbDataSourceConfigJdbc jdbcConfig = (DbDataSourceConfigJdbc) config;
            setAttr(tag, "type", "JDBC");
            setAttr(tag, "host", jdbcConfig.getHost());
            setAttr(tag, "port", Integer.valueOf(jdbcConfig.getPort()));
            setAttr(tag, "db", jdbcConfig.getDbName());
            setAttr(tag, "uid", jdbcConfig.getUserId());
            setAttr(tag, "pwd", jdbcConfig.getPassword());
            setAttr(tag, "driverType", jdbcConfig.getDriverType());
        }
        else if ((config instanceof DbDataSourceConfigMultiple))
        {
            setAttr(tag, "type", "MULTIPLE");
            for (DbDataSourceConfig dbconfig : ((DbDataSourceConfigMultiple) config).getConfigs())
                addXml(tag, dbconfig);
        }
    }

    private Element addXmlEntityBase(Element parent, String tagName, EntityBaseDbMap map, EmisDbMap dbmap)
    {
        Element tag = createElementAndAdd(tagName, parent);
        setAttr(tag, "entity", map.getEntity());
        addXml(tag, map.getDbContext(), dbmap);
        addXml(tag, "id", map.getIdAccess());

        return tag;
    }

    private void addXml(Element parent, GisEntityDbMap map, EmisDbMap dbmap)
    {
        Element tag = addXmlEntityBase(parent, "gisEntityMap", map, dbmap);
        addXml(tag, "primary", map.getPrimaryAccess());
        addXml(tag, "secondary", map.getSecondaryAccess());
    }

    private void addXml(Element parent, EmisEntityDbMap map, EmisDbMap dbmap)
    {
        Element tag = addXmlEntityBase(parent, "entityMap", map, dbmap);
        setAttr(tag, "dateType", map.getDateEnum());
        addXml(tag, "date", map.getDateAccess());
        for (Map.Entry<String, DbRowAccess> entry : map.getFieldAccess().entrySet())
            addXml(tag, "field." + (String) entry.getKey(), (DbRowAccess) entry.getValue());
    }

    private void addXml(Element parent, DbContext dbContext, EmisDbMap dbmap)
    {
        if (dbContext == null)
        {
            return;
        }
        Element tag = createElementAndAdd("dbContext", parent);
        addXml(tag, dbContext.getDataSource(), dbmap);
        setAttr(tag, "query", dbContext.getQuery());
        setAttr(tag, "loopVariable", dbContext.getLoopVariable());
        if (dbContext.getLoopEnum() != null)
            setAttr(tag, "loopEnum", dbContext.getLoopEnum().getName());
        else if (dbContext.getLoopValues() != null && dbContext.getLoopValues().length > 0)
            setIds(tag, "loopValues", dbContext.getLoopValues());
    }

    private void addXml(Element parent, DbDataSourceConfig config, EmisDbMap dbmap)
    {
        if (config == null)
        {
            return;
        }
        int index = dbmap.getDataSources().indexOf(config);
        if (index == -1)
        {
            return;
        }
        Element tag = createElementAndAdd("dbConfigRef", parent);
        setAttr(tag, "config", "" + index);
    }

    private void addXml(Element parent, EmisHierarchyDbMapEntry entry, EmisDbMap dbmap)
    {
        Element tag = createElementAndAdd("hierarchyEntry", parent);
        setAttr(tag, "dateType", entry.getDateType());
        setAttr(tag, "childType", entry.getChildEntity());
        setAttr(tag, "parentType", entry.getParentEntity());
        addXml(tag, entry.getDbContext(), dbmap);
        addXml(tag, "date", entry.getDateAccess());
        addXml(tag, "child", entry.getChildAccess());
        addXml(tag, "parent", entry.getParentAccess());
    }

    private void addXml(Element parent, String key, DbRowAccess access)
    {
        if (access == null)
            return;

        Element tag = createElementAndAdd("access", parent);
        if (key != null)
            setAttr(tag, "key", key);

        if ((access instanceof DbRowArrayAccessImpl))
        {
            setAttr(tag, "type", "array");

            DbRowArrayAccessImpl arrayAccess = (DbRowArrayAccessImpl) access;
            addXml(tag, "value", arrayAccess.getValueAccess());
            for (DbRowAccess subAccess : arrayAccess.getIndexAccess())
                addXml(tag, "index", subAccess);

            StringBuffer dims = new StringBuffer();
            for (int dim : arrayAccess.getDimensions())
            {
                if (dims.length() > 0)
                    dims.append(",");

                dims.append(dim);
            }

            setAttr(tag, "dimensions", dims);
        }
        else if ((access instanceof DbRowFieldAccess))
        {
            setAttr(tag, "type", "field");
            setAttr(tag, "fieldName", ((DbRowFieldAccess) access).getFieldName());
        }
        else if ((access instanceof DbRowEnumAccess))
        {
            DbRowEnumAccess enumAccess = (DbRowEnumAccess) access;
            setAttr(tag, "type", "enum");
            setAttr(tag, "enumType", enumAccess.getEnumType());
            setAttr(tag, "offset", Integer.valueOf(enumAccess.getOffset()));
            setAttr(tag, "asValue", Boolean.valueOf(enumAccess.getAsValue()));
            setAttr(tag, "asSet", Boolean.valueOf(enumAccess.isValueAsSet()));
            if (enumAccess.getMap() != null)
            {
                for (Map.Entry<String, String> entry : enumAccess.getMap().entrySet())
                {
                    Element mapTag = createElementAndAdd("map", tag);
                    setAttr(mapTag, "key", entry.getKey());
                    setAttr(mapTag, "value", entry.getValue());
                }
            }

            addXml(tag, null, enumAccess.getAccess());
        }
        else if ((access instanceof DbRowDateAccess))
        {
            setAttr(tag, "type", "date");
            setAttr(tag, "dateType", ((DbRowDateAccess) access).getDateType());

            EmisMetaEnum[] enums = ((DbRowDateAccess) access).getDateType().getEnums();
            DbRowAccess[] accesses = ((DbRowDateAccess) access).getAccesses();
            for (int i = 0; i < enums.length; i++)
                addXml(tag, enums[i].getName(), accesses[i]);
        }
        else if ((access instanceof DbRowConstAccess))
        {
            setAttr(tag, "type", "const");
            setAttr(tag, "const", ((DbRowConstAccess) access).getConstValue());
        }
        else if ((access instanceof DbRowContextAccess))
        {
            setAttr(tag, "type", "context");
            setAttr(tag, "context", ((DbRowContextAccess) access).getContextParameter());
        }
        else if ((access instanceof DbRowMultipleAccess))
        {
            setAttr(tag, "type", "multiple");
            for (DbRowAccess subAccess : ((DbRowMultipleAccess) access).getAccesses())
            {
                addXml(tag, null, subAccess);
            }
            setIds(tag, "indexes", ((DbRowMultipleAccess) access).getIndexes());
        }
        else if ((access instanceof DbRowAccessMultipleAccessIndex))
        {
            setAttr(tag, "type", "multipleIndex");
            setAttr(tag, "size", Integer.valueOf(((DbRowAccessMultipleAccessIndex) access).getSize()));
        }
        else if ((access instanceof DbRowByColumnAccess))
        {
            setAttr(tag, "type", "byColumn");
            
            DbRowByColumnAccess byColAccess = (DbRowByColumnAccess) access;
            List<String> colNames = byColAccess.getColumnNames(); 
            List<String> enumNames = byColAccess.getEnumNames(); 
            setAttr(tag, "enums", StringUtils.join(enumNames, ",")); 
            
            for (String colName : colNames)
            {
                for (String enumName : enumNames)
                {
                    Element valueTag = createElementAndAdd("value", tag);
                    setAttr(valueTag, "enum", enumName);
                    setAttr(valueTag, "col", colName); 
                    setAttr(valueTag, "value", byColAccess.getValue(colName, enumName)); 
                }
            }
        }
        else if ((access instanceof DbRowByColumnIndexAccess))
        {
            DbRowByColumnIndexAccess byColAccess = (DbRowByColumnIndexAccess) access; 

            setAttr(tag, "type", "byColumnIndex");
            if (byColAccess.getEnumType() != null)
                setAttr(tag, "enum", byColAccess.getEnumType().getName());
        }
    }

    public synchronized Document getXml(EmisReportConfig reportConfig)
    {
        Element tag = createDocumentAndRoot("emisReportModule");
        addXml(tag, reportConfig);
        setAttr(tag, "defaultDateIndex", Integer.valueOf(reportConfig.getDefaultDateIndex()));
        return this.doc;
    }

    private void addXml(Element parent, EmisReportConfig reportConfig)
    {
    	for (EmisMetaGroupEnum groupEnum : reportConfig.getGroupEnums())
    		addXml(parent, groupEnum); 
    	
        for (EmisIndicator indicator : reportConfig.getIndicators())
            addXml(parent, indicator);

        for (EmisPdfReportConfig pdfReport : reportConfig.getPdfReports())
        {
        	if (pdfReport instanceof LayoutPdfReportConfig)
        		addXml(parent, (LayoutPdfReportConfig) pdfReport); 
        	else if (pdfReport instanceof PdfReportConfig)
        		addXml(parent, (PdfReportConfig) pdfReport); 
        }
                        
        for (ExcelReportConfig excelReport : reportConfig.getExcelReports())
            ExcelReportConfigSerializer.addXml(parent, excelReport); 
        
        for (PriorityReportConfig prioReport : reportConfig.getPriorityReports())
        	addXml(parent, prioReport); 
    }

    private Element getEmisPdfReportConfig(Element parent, EmisPdfReportConfig config, String version)
    {
        Element tag = createElementAndAdd("pdfReport", parent);
        setAttr(tag, "name", config.getName());
        setAttr(tag, "entityType", config.getEntityType());
        setAttr(tag, "pageSize", "" + config.getPageSize());
        setAttr(tag, "pageOrientation", "" + config.getOrientation());

        addXml(tag, (TextSet) config); 
        if (config.hasShortTitles())
        	setAttr(tag, "shortTitles", "true"); 
    	
        return tag; 
    }
    
    private void addXml(Element parent, TextSet texts)
    {
    	for (String key : texts.getTextKeys())
    	{
    		String value = texts.getText(key);
    		if (StringUtils.isEmpty(value))
    			continue; 
    		
    		createElementAndAdd(key, parent).setTextContent(value); 
    	}
    }
    
    private void addXml(Element parent, PriorityReportConfig config)
    {
    	if (config == null)
    		return; 
    	
    	Element tag = createElementAndAdd("priorityReport", parent);
    	addXml(tag, config.getMetaResult()); 
    }
    
    private void addXml(Element parent, LayoutPdfReportConfig config)
    {
    	if (config == null)
    		return; 
    	
    	Element tag = getEmisPdfReportConfig(parent, config, LayoutPdfReportConfig.PDF_REPORT_VERSION);
    	for (LayoutPageConfig page : config.getPages())
    	{
    		Element pageTag = createElementAndAdd("page", tag);
    		for (LayoutFrameConfig frame : page.getFrames())
    			addXml(pageTag, frame); 
    		
    		addXml(pageTag, (TextSet) page); 
    	}
    }
    
    private void addXml(Element parent, LayoutFrameConfig frame)
    {
    	Element tag = createElementAndAdd("frame", parent);

    	if (frame.getPosition() != null)
    		setIds(tag, "position", frame.getPosition().toDoubleArray());

    	Element borderTag = createElementAndAdd("borders", tag); 
    	
    	LayoutSides<LayoutBorderConfig> borders = frame.getBorders(); 
    	if (borders != null)
    	{
    		addXml(borderTag, borders.getLeft(), "left");
    		addXml(borderTag, borders.getTop(), "top");
    		addXml(borderTag, borders.getRight(), "right");
    		addXml(borderTag, borders.getBottom(), "bottom");
    	}
    	
    	setAttr(borderTag, "borderRadius", frame.getBorderRadius());

    	Element backgroundTag = createElementAndAdd("background", tag);
    	setAttr(backgroundTag, "image", frame.getBackgroundImagePath()); 
    	setAttr(backgroundTag, "colour", frame.getBackgroundColour()); 
    	setAttr(backgroundTag, "transparency", frame.getBackgroundTransparency());  
    	frame.getBackgroundImagePath(); 
    	frame.getBackgroundTransparency(); 
    	
    	addXml(tag, frame.getContentConfig());
    }
    
    private void addXml(Element parent, LayoutBorderConfig border, String side)
    {
    	if (border == null)
    		return; 
    	
    	Element tag = createElementAndAdd("border", parent); 
    	setAttr(tag, "side", side); 
    	setAttr(tag, "colour", border.getColour()); 
    	setAttr(tag, "width", border.getWidth()); 
    }
    
    private void addXml(Element parent, PdfReportConfig config)
    {
        if (config == null)
            return;

        Element tag = getEmisPdfReportConfig(parent, config, null); 
        
        setAttr(tag, "rows", Integer.valueOf(config.getRows()));
        setAttr(tag, "cols", Integer.valueOf(config.getColumns()));
        
        for (PdfContentConfig contentConfig : config.getContentConfigs())
            addXml(tag, contentConfig);
    }

    private void addXml(Element parent, PdfContentConfig contentConfig)
    {
        if (contentConfig == null)
            return;

        Element tag = createElementAndAdd("pdfContent", parent);
        setAttr(tag, "title", contentConfig.getTitle());

        if ((contentConfig instanceof PdfChartContentConfigImpl))
        {
            setAttr(tag, "type", "chart");
            setAttr(tag, "chartType", Integer.valueOf(((PdfChartContentConfigImpl) contentConfig).getChartType()));
            addXml(tag, (TableMetaResult) ((PdfChartContentConfigImpl) contentConfig).getMetaResult());
        }
        else if ((contentConfig instanceof PdfTableContentConfigImpl))
        {
            setAttr(tag, "type", "table");
            addXml(tag, (TableMetaResult) ((PdfTableContentConfigImpl) contentConfig).getMetaResult());
        }
        else if ((contentConfig instanceof PdfGisContentConfigImpl))
        {
            setAttr(tag, "type", "gis");
            addXml(tag, (GisMetaResult) ((PdfGisContentConfigImpl) contentConfig).getMetaResult());
        }
        else if (contentConfig instanceof PdfTextContentConfigImpl)
        {
            setAttr(tag, "type", "text");
            tag.setTextContent(((PdfTextContentConfigImpl) contentConfig).getText());
        }
        else if (contentConfig instanceof PdfVariableContentConfigImpl)
        {
            PdfVariableContentConfigImpl varContent = (PdfVariableContentConfigImpl) contentConfig; 
            
            setAttr(tag, "type", "vars");
            setAttr(tag, "entityType", varContent.getEntityType().getName()); 
            
            for (int i = 0; i < varContent.getItemCount(); i++)
            {
                Element varTag = createElementAndAdd("entry", tag);
                varTag.setAttribute("variable", varContent.getItemVariable(i));
                varTag.setAttribute("title", varContent.getItemTitle(i));  
            }
        }
        else if (contentConfig instanceof PdfPriorityListContentConfigImpl)
        {
        	PdfPriorityListContentConfigImpl prioConfig= (PdfPriorityListContentConfigImpl) contentConfig; 
        	setAttr(tag, "type", "prio"); 
        	addXml(tag, prioConfig.getMetaResult()); 
        }        
    }

    private void updateXml(Element tag, MetaResult metaResult)
    {
        setAttr(tag, "hierarchy", metaResult.getHierarchy());

        Element metaResultTag = createElementAndAdd("metaResultValues", tag);
        for (MetaResultValue value : metaResult.getMetaResultValues())
            addXml(metaResultTag, value);

        addXml(tag, metaResult.getContext());
        if (null != metaResult.getGlobalFilter())
        {
        	Element globalFilterTag = createElementAndAdd("globalFilter", tag); 
        	addXml(globalFilterTag, metaResult.getGlobalFilter());
        }
    }

    private void addXml(Element parent, MetaResultValue value)
    {
        if (value.getIndicator() == null)
        {
            return;
        }
        Element tag = createElementAndAdd("metaResultValue", parent);
        setAttr(tag, "indicator", value.getIndicator().getName());
        String key = value.getAggregatorKey();
        setAttr(tag, "aggregator", key);
        if ((key != null) && (key.startsWith("?")))
            setAttr(tag, "target", value.getTarget());
    }

    private void addXml(Element parent, GisMetaResult gisMetaResult)
    {
        if (gisMetaResult == null)
            return;

        Element tag = createElementAndAdd("gisMetaResult", parent);
        updateXml(tag, gisMetaResult);
    }
    
    private void addXml(Element parent, PriorityMetaResult metaResult)
    {
    	if (metaResult == null)
    		return; 
    	
    	Element tag = createElementAndAdd("prioMetaResult", parent); 
    	updateXml(tag, metaResult); 
    	
    	setIds(tag, "fields", metaResult.getAdditionalFields());
    	setAttr(tag, "filterEmpty", metaResult.getFilterEmpty());
    	setAttr(tag, "entityType", (Named) metaResult.getListEntity()); 
    }

    private void addXml(Element parent, TableMetaResult tableMetaResult)
    {
        if (tableMetaResult == null)
            return;

        Element tag = createElementAndAdd("tableMetaResult", parent);
        if (tableMetaResult.getSortOrder() == 1)
            tag.setAttribute("sort", "asc");
        else if (tableMetaResult.getSortOrder() == -1)
            tag.setAttribute("sort", "desc");
        else if (tableMetaResult.getSortOrder() == 2)
            tag.setAttribute("sort", "name");

        updateXml(tag, tableMetaResult);
        for (int i = 0; i < tableMetaResult.getDimensionCount(); i++)
            addXml(tag, tableMetaResult.getDimension(i));
    }

    private void addXml(Element parent, MetaResultDimension metaResult)
    {
        if (metaResult == null)
            return;

        Element tag = createElementAndAdd("dimension", parent);
        setAttr(tag, "name", metaResult.getName());

        if ((metaResult instanceof MetaResultDimensionDate))
        {
            setAttr(tag, "type", "date");
            setAttr(tag, "dateType", ((MetaResultDimensionDate) metaResult).getDateEnumType());

            EmisEnumTupleValue value = ((MetaResultDimensionDate) metaResult).getDateEnum();
            if (value != null)
                setIds(tag, "date", value.getIndex());
        }
        else if ((metaResult instanceof MetaResultDimensionEnum))
        {
            setAttr(tag, "type", "enum");
            setAttr(tag, "enum", ((MetaResultDimensionEnum) metaResult).getEnumType());
        }
        else if ((metaResult instanceof MetaResultDimensionEntityFilter))
        {
            setAttr(tag, "type", "entityFilter");
            EmisMetaData field = ((MetaResultDimensionEntityFilter) metaResult).getField();
            if (field != null)
                setAttr(tag, "entity", field.getEntity());
            setAttr(tag, "field", field.getName());
        }
        else if ((metaResult instanceof MetaResultDimensionEntityAncestors))
        {
            setAttr(tag, "type", "entityAncestors");
            addXml(tag, (MetaResultDimensionEntity) metaResult);
        }
        else if ((metaResult instanceof MetaResultDimensionEntityChildren))
        {
            setAttr(tag, "type", "entityChildren");
            addXml(tag, (MetaResultDimensionEntity) metaResult);
        }
        else if (metaResult instanceof MetaResultDimensionEntityGrandChildren)
        {
        	setAttr(tag, "type", "entityGrandChildren"); 
        	addXml(tag, (MetaResultDimensionEntity) metaResult); 
        }
    }

    private void addXml(Element tag, MetaResultDimensionEntity metaResult)
    {
        setAttr(tag, "hierarchy", metaResult.getHierarchy());
        setAttr(tag, "entityType", metaResult.getEntityType());
        setAttr(tag, "dateIndex", Integer.valueOf(metaResult.getHierarchyDateIndex()));

        int[] ids = metaResult.getEntityPath();
        String[] names = metaResult.getEntityPathNames();
        for (int i = 0; i < ids.length; i++)
        {
            Element entityTag = createElementAndAdd("entity", tag);
            setAttr(entityTag, "id", Integer.valueOf(ids[i]));
            setAttr(entityTag, "name", names[i]);
        }
    }

    private void addXml(Element parent, EmisMetaGroupEnum groupEnum)
    {
    	if (groupEnum == null)
    		return; 
    	
    	Element tag = createElementAndAdd("groupEnum", parent); 
    	
    	setAttr(tag, "id", groupEnum.getName()); 
    	setAttr(tag, "enum", groupEnum.getBaseEnum().getName()); 
    	for (String groupName : groupEnum.getValues())
    	{
    		Element childTag = createElementAndAdd("group", tag); 
    		setAttr(childTag, "name", groupName); 
    		setAttr(childTag, "values", StringUtils.join(groupEnum.getGroupValues(groupName)));   
    	}
    }
    
    private void addXml(Element parent, EmisIndicator indicator)
    {
        if (indicator == null)
            return;

        Element tag = createElementAndAdd("indicator", parent);

        setAttr(tag, "bad", Double.valueOf(indicator.getBadThreshold()));
        setAttr(tag, "badText", indicator.getBadThresholdText());
        setAttr(tag, "good", Double.valueOf(indicator.getGoodThreshold()));
        setAttr(tag, "goodText", indicator.getGoodThresholdText());
        setAttr(tag, "bigIsBetter", Boolean.valueOf(indicator.getBiggerIsBetter()));
        setAttr(tag, "max", Double.valueOf(indicator.getMaxValue()));
        setAttr(tag, "name", indicator.getName());
        setAttr(tag, "groupName", indicator.getGroupName());
        setAttr(tag, "yAxis", indicator.getYAxisLabel()); 

        if ((indicator instanceof EmisIndicatorTimeRatio))
        {
            setAttr(tag, "type", "timeRatio");
            setAttr(tag, "factor", Double.valueOf(((EmisIndicatorRatio) indicator).getFactor()));
            setAttr(tag, "offset", Integer.valueOf(((EmisIndicatorTimeRatio) indicator).getTimeOffset()));
        }
        else if ((indicator instanceof EmisIndicatorRatio))
        {
            setAttr(tag, "type", "ratio");
            setAttr(tag, "factor", Double.valueOf(((EmisIndicatorRatio) indicator).getFactor()));
        }
        else if ((indicator instanceof EmisIndicatorSimple))
        {
            setAttr(tag, "type", "simple");
        }

        addXml(tag, (EmisAggregatorList) indicator); 
    }
    
    private void addXml(Element parent, EmisAggregatorList aggrs)
    {
        for (String name : aggrs.getAggregatorNames())
            addXml(parent, aggrs.getAggregator(name), name);
    }
    
    private void addXml(Element parent, EmisAggregatorDef aggregator, String key)
    {
        if (aggregator == null)
            return;

        Element tag = createElementAndAdd("aggregator", parent);
        setAttr(tag, "name", aggregator);
        setAttr(tag, "key", key);
        setAttr(tag, "entity", aggregator.getEntity());
        setAttr(tag, "field", aggregator.getMetaData());
        if (aggregator.getMetaData() == null)
        	setAttr(tag, "countDateType", aggregator.getCountDateType()); 
        
        addXml(tag, aggregator.getContext());
        
        if (aggregator instanceof WeightedAggregatorDef)
        {
        	setAttr(tag, "type", "weighted"); 
        	addXml(tag, (EmisSampleAggregatorDef) aggregator); 
        }

        for (FilterTarget ignoreFilter : aggregator.getIgnoreFilters())
        	addXml(tag, "ignoreFilter", ignoreFilter); 
    }
    
    private void addXml(Element parent, String tagName, FilterTarget filter)
    {
    	Element tag = createElementAndAdd(tagName, parent); 
    	setAttr(tag, "entity", filter.getField().getEntity().getName());
    	setAttr(tag, "field", filter.getField().getName());
    	if (filter.getEnumType() != null)
    		setAttr(tag, "enum", filter.getEnumType().getName());
    }

    private void addXml(Element parent, EmisContext context)
    {
        if (context == null)
        {
            return;
        }
        Element tag = createElementAndAdd("context", parent);
        if ((context instanceof Context))
        {
            Context ctx = (Context) context;

            setAttr(tag, "type", "full");
            if (ctx.getHierarchyDateIndex() != -1)
                setAttr(tag, "dateIndex", Integer.valueOf(ctx.getHierarchyDateIndex()));

            if (ctx.getDateType() != null)
                setAttr(tag, "dateType", ctx.getDateType());

            if (ctx.getDates() != null)
            {
                for (EmisEnumTupleValue value : ctx.getDates())
                {
                    Element dateTag = createElementAndAdd("date", tag);
                    EmisMetaEnum[] enums = value.getEnumTuple().getEnums();
                    setAttr(dateTag, "dateEnum", enums[(enums.length - 1)]);
                    setIds(dateTag, "date", value.getValue());
                }
            }

            if (ctx.getEntities() != null)
            {
                for (EmisEntity entity : ctx.getEntities())
                {
                    Element entityTag = createElementAndAdd("entity", tag);
                    setAttr(entityTag, "entityType", entity.getEntityType());
                    setAttr(entityTag, "id", Integer.valueOf(entity.getId()));
                }
            }

            if (ctx.getEntityType() != null)
            {
                setAttr(tag, "entityType", ctx.getEntityType());
            }

            if (ctx.getEnumFilters() != null)
            {
                for (Map.Entry<String, EmisEnumSet> enumFilter : ctx.getEnumFilters().entrySet())
                {
                    Element enumFilterTag = createElementAndAdd("enumFilter", tag);
                    setAttr(enumFilterTag, "key", enumFilter.getKey());
                    setAttr(enumFilterTag, "enumType", ((EmisEnumSet) enumFilter.getValue()).getEnum());
                    setIds(enumFilterTag, "values", ((EmisEnumSet) enumFilter.getValue()).getAllIndexes());
                }
            }

            if (ctx.getEntityFilters() != null)
            {
                for (Map.Entry<String, byte[]> entityFilter : ctx.getEntityFilters().entrySet())
                {
                    Element entityFilterTag = createElementAndAdd("entityFilter", tag);
                    setAttr(entityFilterTag, "key", entityFilter.getKey());
                    setIds(entityFilterTag, "values", (byte[]) entityFilter.getValue());
                }
            }

            if (ctx.getDateEnumFilters() != null)
            {
            	for (Map.Entry<String, EmisEnumSet> dateFilter : ctx.getDateEnumFilters().entrySet())
            	{
            		Element dateFilterTag = createElementAndAdd("dateFilter", tag); 
            		setAttr(dateFilterTag, "key", dateFilter.getKey());
            		setIds(dateFilterTag, "values", ((EmisEnumSet) dateFilter.getValue()).getAllIndexes()); 
            	}
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown context implementation: " + context.toString());
        }
    }

    private void setAttr(Element tag, String attr, Object obj)
    {
        if (obj != null)
            tag.setAttribute(attr, obj.toString());
    }

    private void setAttr(Element tag, String attr, ChartColor color)
    {
    	if (color == null)
    		return;
    	
    	setAttr(tag, attr, String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue())); 
    }
    
    private void setAttr(Element tag, String attr, Named named)
    {
        if (named != null)
            tag.setAttribute(attr, named.getName());
    }

    private void setIds(Element tag, String attr, EmisMetaEnumTuple tuple)
    {
        if (tuple == null)
            return;

        setIds(tag, attr, NamedUtil.getNames(tuple.getEnums()));
    }

    private void setIds(Element tag, String attr, Collection values)
    {
        StringBuffer ids = new StringBuffer();
        for (Iterator i$ = values.iterator(); i$.hasNext();)
        {
            Object value = i$.next();

            if (ids.length() > 0)
                ids.append(",");
            ids.append(value);
        }

        tag.setAttribute(attr, ids.toString());
    }

    private void setIds(Element tag, String attr, int[] values)
    {
    	if (values == null)
    		return; 
    	
        StringBuffer ids = new StringBuffer(); 
        for (int value : values)
        {
            if (ids.length() > 0)
                ids.append(","); 
            ids.append(value); 
        }
        
        tag.setAttribute(attr, ids.toString()); 
    }
    
    private void setIds(Element tag, String attr, double[] values)
    {
    	if (values == null || values.length == 0)
    		return; 
    	
        StringBuffer ids = new StringBuffer(); 
        for (double value : values)
        {
            if (ids.length() > 0)
                ids.append(","); 
            ids.append(value); 
        }
        
        tag.setAttribute(attr, ids.toString()); 
    }
    
    private void setIds(Element tag, String attr, byte[] values)
    {
        StringBuffer ids = new StringBuffer();
        for (byte value : values)
        {
            if (ids.length() > 0)
                ids.append(",");
            ids.append(value);
        }

        tag.setAttribute(attr, ids.toString());
    }

    private void setIds(Element tag, String attr, String[] values)
    {
        if (values == null)
        {
            return;
        }
        StringBuffer ids = new StringBuffer();
        for (String value : values)
        {
            if (ids.length() > 0)
                ids.append(",");
            ids.append(value);
        }

        tag.setAttribute(attr, ids.toString());
    }

    private <T extends Named> void setIds(Element tag, String attr, List<T> items)
    {
        StringBuffer ids = new StringBuffer();
        for (Named item : items)
        {
            if (ids.length() > 0)
                ids.append(",");
            ids.append(item.getName());
        }

        tag.setAttribute(attr, ids.toString());
    }
}
