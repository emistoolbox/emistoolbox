package com.emistoolbox.server;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
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
import com.emistoolbox.common.model.analysis.EmisIndicatorTimeRatio;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.analysis.FilterTarget;
import com.emistoolbox.common.model.analysis.impl.AggregatorDef;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.analysis.impl.IndicatorRatio;
import com.emistoolbox.common.model.analysis.impl.IndicatorSimple;
import com.emistoolbox.common.model.analysis.impl.IndicatorTimeRatio;
import com.emistoolbox.common.model.analysis.impl.ReportConfig;
import com.emistoolbox.common.model.analysis.impl.WeightedAggregatorDef;
import com.emistoolbox.common.model.impl.EmisMetaImpl;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.impl.GisContextImpl;
import com.emistoolbox.common.model.impl.GisLayerImpl;
import com.emistoolbox.common.model.impl.MetaData;
import com.emistoolbox.common.model.impl.MetaDateEnum;
import com.emistoolbox.common.model.impl.MetaEntity;
import com.emistoolbox.common.model.impl.MetaEnum;
import com.emistoolbox.common.model.impl.MetaEnumTuple;
import com.emistoolbox.common.model.impl.MetaGroupEnum;
import com.emistoolbox.common.model.impl.MetaHierarchy;
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
import com.emistoolbox.common.model.mapping.DbRowAccessFn;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
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
import com.emistoolbox.common.model.mapping.impl.DataSourceReplaceSetImpl;
import com.emistoolbox.common.model.mapping.impl.DbContextImpl;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigMultiple;
import com.emistoolbox.common.model.mapping.impl.DbRowAccessBase;
import com.emistoolbox.common.model.mapping.impl.DbRowAccessMultipleAccessIndex;
import com.emistoolbox.common.model.mapping.impl.DbRowArrayAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowConstAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowContextAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowFieldAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowMultipleAccessImpl;
import com.emistoolbox.common.model.mapping.impl.EmisDateInitDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.EmisDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.EmisEntityDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.EmisHierarchyDbMapEntryImpl;
import com.emistoolbox.common.model.mapping.impl.EmisHierarchyDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.GisEntityDbMapImpl;
import com.emistoolbox.common.model.mapping.impl.UnflattenDbColumn;
import com.emistoolbox.common.model.mapping.impl.UnflattenDbQuery;
import com.emistoolbox.common.model.mapping.impl.UnflattenDbRow;
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
import com.emistoolbox.common.model.priolist.impl.PriorityReportConfigImpl;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.model.validation.EmisValidationTimeRatioRule;
import com.emistoolbox.common.model.validation.impl.ValidationFilter;
import com.emistoolbox.common.model.validation.impl.ValidationImpl;
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
import com.emistoolbox.common.renderer.pdfreport.impl.PdfReportConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutBorderConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutFrameConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutPageConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutPdfReportConfigImpl;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.GisMetaResultImpl;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityAncestors;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityGrandChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.common.results.impl.MetaResultValueImpl;
import com.emistoolbox.common.results.impl.PriorityMetaResultImpl;
import com.emistoolbox.common.results.impl.TableMetaResultImpl;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.common.util.Rectangle;
import com.emistoolbox.server.excelMerge.ExcelReportConfigSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlReader 
{
	private EmisMeta meta;

	public synchronized EmisMeta getEmisMeta(Element tag) 
	{
		if (tag == null)
			return null;

		verifyTagName(tag, "emisMeta");

		this.meta = new EmisMetaImpl();

		NamedIndexList<EmisMetaEnum> enums = new NamedIndexList<EmisMetaEnum>();
		for (Element enumTag : getElements(tag, "enums", "enum"))
			add(enums, getEmisMetaEnum(enumTag));
		this.meta.setEnums(enums);

		NamedIndexList<EmisMetaDateEnum> dateEnums = new NamedIndexList<EmisMetaDateEnum>();
		for (Element dateTag : getElements(tag, "dates", "dateEnum"))
			add(dateEnums, getEmisMetaDateEnum(dateTag, dateEnums));
		this.meta.setDateEnums(dateEnums);

		NamedIndexList<EmisMetaEntity> entities = new NamedIndexList<EmisMetaEntity>();
		for (Element entityTag : getElements(tag, "entities", "entity"))
			add(entities, getEmisMetaEntity(entityTag));
		this.meta.setEntities(entities);

		NamedIndexList<EmisMetaHierarchy> hierarchies = new NamedIndexList<EmisMetaHierarchy>();
		for (Element hierarchyTag : getElements(tag, "hierarchies", "hierarchy"))
			add(hierarchies, getEmisMetaHierarchy(hierarchyTag));
		this.meta.setHierarchies(hierarchies);

		GisContext gis = null;
		Element gisTag = getElement(tag, "gisProjection");
		if (gisTag != null) 
		{
			gis = new GisContextImpl();
			gis.setProjection(gisTag.getTextContent());
		}

		gisTag = getElement(tag, "gisContext");
		if (gisTag != null) 
		{
			gis = new GisContextImpl();
			gis.setProjection(getAttr(gisTag, "projection"));
			gis.setBaseLayerBoundary(getIdsAsDoubleArray(gisTag, "boundary"));

			Element imgTag = getElement(gisTag, "baseImage");
			if (imgTag != null) 
			{
				gis.setBaseLayerImage(getAttr(imgTag, "image"));
				gis.setBaseLayerImageSize(getIdsAsIntArray(imgTag, "size"));
			}

			for (Element gisLayerTag : getElements(gisTag, null, "gisLayer"))
				gis.addGisLayer(getGisLayer(gisLayerTag));

			meta.setGisContext(gis);
		}

		this.meta.setGisContext(gis);

		return this.meta;
	}

	private GisLayer getGisLayer(Element tag) 
	{
		if (tag == null)
			return null;

		GisLayer result = new GisLayerImpl();
		result.setName(getAttr(tag, "name"));
		result.setPath(getAttr(tag, "path"));
		result.setStyle(tag.getTextContent());

		return result;
	}

	private EmisMetaEnum getEmisMetaEnum(Element tag) 
	{
		if (tag == null)
			return null;

		verifyTagName(tag, "enum");
		EmisMetaEnum result = new MetaEnum();
		result.setName(getAttr(tag, "id"));
		result.setValues(getIdsAsArray(tag, "values"));
		return result;
	}

	private EmisMetaDateEnum getEmisMetaDateEnum(Element tag, List<EmisMetaDateEnum> dates) 
	{
		if (tag == null)
			return null;

		verifyTagName(tag, "dateEnum");

		EmisMetaDateEnum result = new MetaDateEnum();
		result.setName(getAttr(tag, "id"));
		result.setValues(getIdsAsArray(tag, "values"));
		result.setParent((EmisMetaDateEnum) find(tag, "parent", dates));
		result.setAllowDynamicInit("true".equalsIgnoreCase(getAttr(tag, "dateInit"))); 

		return result;
	}

	private EmisMetaEntity getEmisMetaEntity(Element tag) 
	{
		if (tag == null)
			return null;

		verifyTagName(tag, "entity");
		EmisMetaEntity result = new MetaEntity();
		result.setName(tag.getAttribute("id"));
		if (!StringUtils.isEmpty(tag.getAttribute("gisType")))
			result.setGisType(EmisMetaEntity.EmisGisType.valueOf(tag.getAttribute("gisType")));
		else
			result.setGisType(EmisMetaEntity.EmisGisType.NONE);

		NamedIndexList<EmisMetaData> datas = new NamedIndexList<EmisMetaData>();
		for (Element dataTag : getElements(tag, null, "data"))
			add(datas, getEmisMetaData(dataTag, result));
		result.setData(datas);
		return result;
	}

	private EmisMetaData getEmisMetaData(Element tag, EmisMetaEntity entity) 
	{
		if (tag == null) 
			return null;

		verifyTagName(tag, "data");

		EmisMetaData result = new MetaData();
		result.setName(getAttr(tag, "id"));
		result.setDateType((EmisMetaDateEnum) find(tag, "date", this.meta.getDateEnums()));
		result.setEntity(entity);
		Boolean b = getAttrAsBoolean(tag, "planningResource");
		if (b != null)
			result.setPlanningResource(b.booleanValue());

		result.setType(EmisMetaData.EmisDataType.valueOf(getAttr(tag, "dataType")));
		if (result.getType() == EmisMetaData.EmisDataType.ENUM || result.getType() == EmisMetaData.EmisDataType.ENUM_SET)
			result.setEnumType((EmisMetaEnum) find(tag, "enum", this.meta.getEnums()));

		List<EmisMetaEnum> enums = findAsList(tag, "dimensions", this.meta.getEnums());
		if ((enums != null) && (enums.size() > 0)) 
		{
			EmisMetaEnumTuple tuple = new MetaEnumTuple();
			tuple.setEnums((EmisMetaEnum[]) enums.toArray(new EmisMetaEnum[0]));
			result.setArrayDimentsions(tuple);
		}

		return result;
	}

	public List<EmisUser> getUsers(Element tag) 
	{
		verifyTagName(tag, "users");

		List<EmisUser> result = new ArrayList<EmisUser>();
		for (Element userTag : getElements(tag, null, "user")) 
		{
			EmisUser user = getUser(userTag);
			if (user != null)
				result.add(user);
		}

		if (XmlWriter.getMD5(result).equals(tag.getAttribute("md5")))
			return result;

		return null;
	}

	private EmisUser getUser(Element tag) 
	{
		EmisUser result = new EmisUser();
		if (StringUtils.isEmpty(tag.getAttribute("uid")))
			return null;
		result.setUsername(tag.getAttribute("uid"));

		if (StringUtils.isEmpty(tag.getAttribute("pwd")))
			return null;
		result.setPasswordHash(tag.getAttribute("pwd"));

		if (StringUtils.isEmpty(tag.getAttribute("level")))
			result.setAccessLevel(EmisUser.AccessLevel.VIEWER);
		else
			result.setAccessLevel(EmisUser.AccessLevel.valueOf(tag.getAttribute("level")));

		if (!StringUtils.isEmpty(tag.getAttribute("dataset"))) 
		{
			result.setDataset(tag.getAttribute("dataset"));
			for (Element entityTag : getElements(tag, null, "rootEntity")) 
			{
				try {
					String entityType = entityTag.getAttribute("entityType");
					if (StringUtils.isEmpty(entityType))
						continue;

					Integer entityId = Integer.parseInt(entityTag.getAttribute("entityId"));
					result.addRootEntity(entityType, entityId);
				} 
				catch (NumberFormatException ex) 
				{}
			}
		}

		return result;
	}

	private EmisMetaHierarchy getEmisMetaHierarchy(Element tag) 
	{
		if (tag == null) 
			return null;

		verifyTagName(tag, "hierarchy");
		EmisMetaHierarchy result = new MetaHierarchy();
		result.setName(getAttr(tag, "id"));
		result.setEntityOrder(findAsList(tag, "entities", this.meta.getEntities()));

		return result;
	}

	public synchronized EmisDbMap getEmisDbMap(Element tag, final EmisMeta meta)
		throws IOException 
	{
		verifyTagName(tag, "emisMapping");
		this.meta = meta;

		String dataset = tag.getAttribute("dataset");
		if (!StringUtils.isEmpty(dataset)) 
		{
			EmisDbMap result = EmisToolboxIO.loadMappingXml(dataset, meta);

			for (Element globalTag : getElements(tag, null, "globalQuery")) 
			{
				String sql = globalTag.getTextContent();
				DbDataSourceConfig config = result.getDataSources().get(Integer.parseInt(globalTag.getAttribute("dbconfig")));
				config.setQuery(DbDataSourceConfig.GLOBAL_QUERY, sql);
			}

			return result;
		}

		EmisDbMap result = new EmisDbMapImpl();
		result.setMetaData(meta);

		List<DbDataSourceConfig> configs = new ArrayList<DbDataSourceConfig>();
		for (Element configTag : getElements(tag, "dbConfigs", "dbConfig"))
			add(configs, getDbDataSourceConfig(configTag, meta.getDatasetName()));
		result.setDataSources(configs);

		List<EmisDateInitDbMap> dateInits = new ArrayList<EmisDateInitDbMap>(); 
		for (Element dateInitTag : getElements(tag, "dateInits", "dateInit"))
			add(dateInits, getEmisDateInitDbMap(dateInitTag, configs)); 
		result.setDateInitMappings(dateInits);
		
		List<EmisEntityDbMap> entities = new ArrayList<EmisEntityDbMap>();
		for (Element entityTag : getElements(tag, "entityMaps", "entityMap"))
			add(entities, getEmisEntityDbMap(entityTag, configs));
		result.setEntityMappings(entities);

		List<GisEntityDbMap> gisEntities = new ArrayList<GisEntityDbMap>();
		for (Element gisEntityTag : getElements(tag, "gisEntityMaps", "gisEntityMap"))
			add(gisEntities, getGisEntityDbMap(gisEntityTag, configs));
		result.setGisEntityMappings(gisEntities);

		List<EmisHierarchyDbMap> hierarchies = new ArrayList<EmisHierarchyDbMap>();
		for (Element hierarchyTag : getElements(tag, "hierarchyMaps", "hierarchyMap"))
			add(hierarchies, getEmisHierarchyDbMap(hierarchyTag, configs));
		result.setHierarchyMappings(hierarchies);

		result.updateDimensions();
		return result;
	}

	private DataSourceReplaceSet parseReplace(Element tag) 
	{
		if (tag == null)
			return null;

		DataSourceReplaceSet result = new DataSourceReplaceSetImpl();
		for (Element child : getElements(tag, null, "entry"))
			result.addReplace(getAttr(child, "col"), getAttr(child, "key"), getAttr(child, "value"));

		return result;
	}

	private void getQueries(Element configTag, DbDataSourceConfig config) 
	{
		if (!config.hasQueries()) 
			return;

		for (Element queryTag : getElements(configTag, null, "query"))
			config.addQuery(queryTag.getAttribute("id"), queryTag.getTextContent());
	}

	private DbDataSourceConfig getDbDataSourceConfig(Element tag, String dataset) 
	{
		verifyTagName(tag, "dbConfig");

		String type = getAttr(tag, "type");
		if (type.equals("ACCESS") || type.equals("EXCEL") || type.equals("GIS_SHAPE") || type.equals("CSV") || type.equals("HSQL")) 
		{
			DbDataSourceConfigFile result = null;
			if (type.equals("EXCEL"))
				result = new DbDataSourceConfigExcel();
			else if (type.equals("CSV")) 
			{
				result = new DbDataSourceConfigCsv();
				((DbDataSourceConfigCsv) result)
						.setReplace(parseReplace(getElement(tag, "replace")));
			}
			else if (type.equals("ACCESS"))
				result = new DbDataSourceConfigAccess();
			else if (type.equals("GIS_SHAPE"))
				result = new DbDataSourceConfigGeo();
			else if (type.equals("HSQL"))
				result = new DbDataSourceConfigHsqldb();

			result.setContextName(getAttr(tag, "contextName"));

			for (Element fileTag : getElements(tag, null, "file")) 
			{
				if (fileTag.hasAttribute("path")) 
				{
					String relativePath = getRelativePath(
							getAttr(fileTag, "path"), dataset);
					if (relativePath == null)
						result.addFileSource(DbDataFileSource.PREFIX_FILESYSTEM + getAttr(fileTag, "path"), getAttr(fileTag, "contextValue"));
					else
						result.addFileSource(DbDataFileSource.PREFIX_DATASET + relativePath, getAttr(fileTag, "contextValue"));
				}
				else 
				{
					DbDataFileSource fileSource = result.addFileSource(
							getAttr(fileTag, "url"),
							getAttr(fileTag, "contextValue"));
					fileSource.setCacheFilename(getAttr(fileTag,
							"cacheFilename"));
				}
			}

			getQueries(tag, result);
			return result;
		}

		if (type.equals("JDBC")) {
			DbDataSourceConfigJdbc result = new DbDataSourceConfigJdbc();
			result.setHost(getAttr(tag, "host"));
			try {
				result.setPort(getAttrAsInt(tag, "port").intValue());
			} catch (Throwable err) {
			}

			result.setDbName(getAttr(tag, "db"));
			result.setUserId(getAttr(tag, "uid"));
			result.setPassword(getAttr(tag, "pwd"));
			try {
				result.setDriverType(DbDataSourceConfigJdbc.JdbcDriver
						.valueOf(getAttr(tag, "driverType")));
			} catch (Throwable err) {
			}

			getQueries(tag, result);
			return result;
		}

		if (type.equals("MULTIPLE")) {
			DbDataSourceConfigMultiple result = new DbDataSourceConfigMultiple();
			List<DbDataSourceConfig> configs = new ArrayList<DbDataSourceConfig>();
			for (Element configTag : getElements(tag, null, "config"))
				add(configs, getDbDataSourceConfig(configTag, dataset));
			result.setConfigs(configs);

			getQueries(tag, result);
			return result;
		}

		throw new IllegalArgumentException("Unknown config type '" + type + "'");
	}

	private String getRelativePath(String path, String dataset) {
		path = path.replace("\\", "/");

		String basePath = EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH,
				ServerUtil.ROOT_PATH) + dataset;
		basePath = basePath.replace("\\", "/");
		if (!basePath.endsWith("/"))
			basePath += "/";

		if (path.startsWith(basePath))
			return path.substring(basePath.length());

		return null;
	}

	private void updateEntityBaseDbMap(EntityBaseDbMap result, Element tag, List<DbDataSourceConfig> dbconfigs) 
	{
		result.setEmisMetaEntity((EmisMetaEntity) find(tag, "entity", this.meta.getEntities()));
		result.setDbContext(getDbContext(getElement(tag, "dbContext"), dbconfigs));
		result.setIdAccess(getDbRowAccess(getElement(tag, "access", "key", "id"), dbconfigs));
	}

	private GisEntityDbMap getGisEntityDbMap(Element tag,
			List<DbDataSourceConfig> dbconfigs) {
		verifyTagName(tag, "gisEntityMap");

		GisEntityDbMap result = new GisEntityDbMapImpl();
		updateEntityBaseDbMap(result, tag, dbconfigs);

		result.setPrimaryAccess(getDbRowAccess(getElement(tag, "access", "key", "primary"), dbconfigs));
		result.setSecondaryAccess(getDbRowAccess(getElement(tag, "access", "key", "secondary"), dbconfigs));

		return result;
	}
	
	private EmisDateInitDbMap getEmisDateInitDbMap(Element tag, List<DbDataSourceConfig> dbconfigs)
	{
		verifyTagName(tag, "dateInit"); 
		
		EmisDateInitDbMap result = new EmisDateInitDbMapImpl(); 
		result.setDbContext(getDbContext(getElement(tag, "dbContext"), dbconfigs));
		result.setValueAccess(getDbRowAccess(getElement(tag, "dateInit"), dbconfigs));
		result.setDateType(find(tag, "dateType", this.meta.getDateEnums())); 

		return result; 
	}
	

	private EmisEntityDbMap getEmisEntityDbMap(Element tag, List<DbDataSourceConfig> dbconfigs) 
	{
		verifyTagName(tag, "entityMap");

		EmisEntityDbMap result = new EmisEntityDbMapImpl();
		updateEntityBaseDbMap(result, tag, dbconfigs);
		result.setDateEnum((EmisMetaDateEnum) find(tag, "dateType", this.meta.getDateEnums()));
		result.setDateAccess((DbRowDateAccess) getDbRowAccess(getElement(tag, "access", "key", "date"), dbconfigs));

		Map<String, DbRowAccess> fields = new HashMap<String, DbRowAccess>();
		for (Element element : getElements(tag, null, "access")) 
		{
			String key = element.getAttribute("key");
			if ((key != null) && (key.startsWith("field."))) 
			{
				DbRowAccess access = getDbRowAccess(element, dbconfigs);
				
				String fieldName = key.substring(6);
				if (access instanceof DbRowArrayAccess)
					adaptArrayAccess((DbRowArrayAccess) access, NamedUtil.find(fieldName, result.getEntity().getData()));

				fields.put(fieldName, access);
			}
		}
		
		result.setFieldAccess(fields);

		return result;
	}

	private DbContext getDbContext(Element tag,
			List<DbDataSourceConfig> dbconfigs) {
		if (tag == null) {
			return null;
		}
		verifyTagName(tag, "dbContext");

		DbContext result = new DbContextImpl();
		result.setDataSource(getDbDataSourceConfigRef(
				getElement(tag, "dbConfigRef"), dbconfigs));
		result.setQuery(getAttr(tag, "query"));

		result.setLoopVariable(getAttr(tag, "loopVariable"));
		if (tag.hasAttribute("loopEnum"))
			result.setLoopEnum((EmisMetaDateEnum) find(tag, "loopEnum", this.meta.getDateEnums()));
		else
			result.setLoopVariable(getAttr(tag, "loopValues"));

		return result;
	}

	private DbDataSourceConfig getDbDataSourceConfigRef(Element tag,
			List<DbDataSourceConfig> dbconfigs) {
		if (tag == null) {
			return null;
		}
		Integer index = getAttrAsInt(tag, "config");
		if (index == null) {
			return null;
		}
		return (DbDataSourceConfig) dbconfigs.get(index.intValue());
	}

	private void adaptArrayAccess(DbRowArrayAccess access, EmisMetaData field) 
	{
		final DbRowAccess valueAccess = access.getValueAccess();
		if (valueAccess == null)
			return;

		if (DbRowByColumnAccess.getByColumnAccess(valueAccess) != null) 
		{
			DbRowAccessBase.map(new DbRowAccessFn() 
			{
				public void fn(DbRowAccess access) 
				{
					if (access instanceof DbRowByColumnIndexAccess)
						((DbRowByColumnIndexAccess) access).setByColumnAccess(DbRowByColumnAccess.getByColumnAccess(valueAccess));
				}
			}, access.getIndexAccess());
		}
		else if (valueAccess instanceof DbRowMultipleAccess) 
		{
			DbRowMultipleAccess multipleAccess = (DbRowMultipleAccess) valueAccess;

			DbRowByColumnAccess byColumnAccess = new DbRowByColumnAccess();
			EmisMetaEnum enumType = replaceIndexAccess(field, access.getIndexAccess(), byColumnAccess);
			if (enumType == null) 
			{
				removeMultipleAccess(access);
				return;
			}

			byColumnAccess.addEnum(enumType);

			String[] enumValues = multipleAccess.getIndexes();
			DbRowAccess[] accesses = multipleAccess.getAccesses();
			for (byte i = 0; i < enumValues.length; i++) 
			{
				if (!(accesses[i] instanceof DbRowFieldAccess))
					continue;

				DbRowFieldAccess fieldAccess = (DbRowFieldAccess) accesses[i];
				String fieldName = fieldAccess == null ? null : fieldAccess.getFieldName();
				if (fieldName != null) 
				{
					byColumnAccess.findColumn(fieldName, true);
					byColumnAccess.setValue(fieldName, enumType.getName(), enumValues[i]);
				}
			}

			access.setValueAccess(byColumnAccess);
		}
	}

	private void removeMultipleAccess(DbRowArrayAccess access) {
		if (access.getValueAccess() instanceof DbRowMultipleAccess)
			access.setValueAccess(null);

		DbRowAccess[] indexAccesses = access.getIndexAccess();
		for (int i = 0; i < indexAccesses.length; i++) {
			if (indexAccesses[i] instanceof DbRowAccessMultipleAccessIndex)
				indexAccesses[i] = null;
		}
	}

	private EmisMetaEnum replaceIndexAccess(EmisMetaData field,
			DbRowAccess[] accesses, DbRowByColumnAccess byColumnAccess) {
		EmisMetaEnum[] enums = field.getArrayDimensions().getEnums();
		for (int i = 0; i < accesses.length; i++) {
			if (accesses[i] instanceof DbRowAccessMultipleAccessIndex) {
				accesses[i] = new DbRowByColumnIndexAccess(byColumnAccess, enums[i]);
				return enums[i];
			}
		}

		return null;
	}

	private DbRowAccess getDbRowAccess(Element tag, List<DbDataSourceConfig> dbconfigs) 
	{
		if (tag == null)
			return null;

		verifyTagName(tag, "access");
		String type = getAttr(tag, "type");
		if (type.equals("array")) 
		{
			DbRowArrayAccessImpl arrayAccess = new DbRowArrayAccessImpl();
			arrayAccess.setValueAccess(getDbRowAccess(getElement(tag, "access", "key", "value"), dbconfigs));
			arrayAccess.setDimensions(getIdsAsIntArray(tag, "dimensions"));

			List<DbRowAccess> indexes = new ArrayList<DbRowAccess>();
			for (Element indexTag : getElements(tag, "access", "key", "index"))
				add(indexes, getDbRowAccess(indexTag, dbconfigs));
		
			arrayAccess.setIndexAccess((DbRowAccess[]) indexes.toArray(new DbRowAccess[0]));

			return arrayAccess;
		}
		
		if (type.equals("field")) {
			DbRowFieldAccess fieldAccess = new DbRowFieldAccessImpl();
			fieldAccess.setFieldName(getAttr(tag, "fieldName"));

			return fieldAccess;
		}
		if (type.equals("enum")) {
			DbRowAccess subAccess = getDbRowAccess(getElement(tag, "access"),
					dbconfigs);
			EmisMetaEnum enumType = (EmisMetaEnum) find(tag, "enumType",
					this.meta.getEnums());
			if (enumType == null)
				enumType = (EmisMetaEnum) find(tag, "enumType",
						this.meta.getDateEnums());
			DbRowEnumAccess enumAccess = new DbRowEnumAccess(subAccess,
					enumType);
			enumAccess.setOffset(getAttrAsInt(tag, "offset").intValue());
			enumAccess.setAsValue(getAttrAsBoolean(tag, "asValue")
					.booleanValue());
			enumAccess.setValueAsSet(getAttrAsBoolean(tag, "asSet")
					.booleanValue());

			Map<String, String> map = null;
			for (Element mapTag : getElements(tag, null, "map")) {
				if (map == null)
					map = new HashMap<String, String>();
				map.put(getAttr(mapTag, "key"), getAttr(mapTag, "value"));
			}
			enumAccess.setMap(map);

			return enumAccess;
		}
		if (type.equals("date")) {
			DbRowDateAccess dateAccess = new DbRowDateAccess();
			dateAccess.setDateType((EmisMetaDateEnum) find(tag, "dateType",
					this.meta.getDateEnums()));

			EmisMetaEnum[] enums = dateAccess.getDateType().getEnums();
			DbRowAccess[] accesses = new DbRowAccess[enums.length];
			for (int i = 0; i < accesses.length; i++)
				accesses[i] = getDbRowAccess(
						getElement(tag, "access", "key", enums[i].getName()),
						dbconfigs);
			dateAccess.setAccesses(accesses);

			return dateAccess;
		}
		if (type.equals("const")) {
			DbRowConstAccess constAccess = new DbRowConstAccessImpl();
			constAccess.setConstValue(getAttr(tag, "const"));
			return constAccess;
		}
		if (type.equals("context")) {
			DbRowContextAccess contextAccess = new DbRowContextAccessImpl();
			contextAccess.setContextParameter(getAttr(tag, "context"));
			return contextAccess;
		}
		if (type.equals("multiple")) {
			DbRowMultipleAccess multiAccess = new DbRowMultipleAccessImpl();

			multiAccess.setIndexes(getIdsAsArray(tag, "indexes"));
			List<DbRowAccess> accesses = new ArrayList<DbRowAccess>();
			for (Element accessTag : getElements(tag, null, "access"))
				add(accesses, getDbRowAccess(accessTag, dbconfigs));
			multiAccess.setAccesses((DbRowAccess[]) accesses
					.toArray(new DbRowAccess[0]));

			return multiAccess;
		}
		if (type.equals("multipleIndex")) {
			DbRowAccessMultipleAccessIndex result = new DbRowAccessMultipleAccessIndex();
			result.setSize(getAttrAsInt(tag, "size").byteValue());
			return result;
		}
		if (type.equals("byColumn")) {
			DbRowByColumnAccess result = new DbRowByColumnAccess();
			for (EmisMetaEnum enumType : NamedUtil.findAll(
					getIdsAsArray(tag, "enums"), meta.getEnums()))
				result.addEnum(enumType);

			Set<String> columns = new HashSet<String>();
			for (Element colTag : getElements(tag, null, "value"))
				columns.add(getAttr(colTag, "col"));

			result.addColumns(columns);

			for (Element colTag : getElements(tag, null, "value")) {
				result.setValue(getAttr(colTag, "col"),
						getAttr(colTag, "enum"), getAttr(colTag, "value"));

				getAttr(colTag, "col");
				getAttr(colTag, "enum");
				getAttr(colTag, "value");
			}

			return result;
		}
		
		if (type.equals("byColumnIndex"))
			return new DbRowByColumnIndexAccess(NamedUtil.find(getAttr(tag, "enum"), meta.getEnums()));

		throw new IllegalArgumentException("Unknow type for <access> tag: '" + type + "'");
	}

	private EmisHierarchyDbMap getEmisHierarchyDbMap(Element tag, List<DbDataSourceConfig> dbconfigs) 
	{
		verifyTagName(tag, "hierarchyMap");

		EmisHierarchyDbMap result = new EmisHierarchyDbMapImpl();
		result.setHierarchy((EmisMetaHierarchy) find(tag, "id",
				this.meta.getHierarchies()));

		List<EmisHierarchyDbMapEntry> entries = new ArrayList<EmisHierarchyDbMapEntry>();
		for (Element hierarchyTag : getElements(tag, null, "hierarchyEntry"))
			add(entries, getEmisHierarchyDbMapEntry(hierarchyTag, dbconfigs));
		result.setMappings(entries);
		return result;
	}

	private EmisHierarchyDbMapEntry getEmisHierarchyDbMapEntry(Element tag, List<DbDataSourceConfig> dbconfigs) 
	{
		verifyTagName(tag, "hierarchyEntry");
		EmisHierarchyDbMapEntry result = new EmisHierarchyDbMapEntryImpl();
		result.setDbContext(getDbContext(getElement(tag, "dbContext"),
				dbconfigs));
		result.setDateType((EmisMetaDateEnum) find(tag, "dateType",
				this.meta.getDateEnums()));
		result.setChildEntity((EmisMetaEntity) find(tag, "childType",
				this.meta.getEntities()));
		result.setChildAccess(getDbRowAccess(
				getElement(tag, "access", "key", "child"), dbconfigs));
		result.setParentEntity((EmisMetaEntity) find(tag, "parentType",
				this.meta.getEntities()));
		result.setParentAccess(getDbRowAccess(
				getElement(tag, "access", "key", "parent"), dbconfigs));
		result.setDateAccess((DbRowDateAccess) getDbRowAccess(
				getElement(tag, "access", "key", "date"), dbconfigs));

		return result;
	}

	public synchronized EmisReportConfig getEmisReportConfig(Element tag,
			EmisMeta meta) {
		this.meta = meta;
		verifyTagName(tag, "emisReportModule");

		EmisReportConfig result = new ReportConfig();
		Integer i = getAttrAsInt(tag, "defaultDateIndex");
		result.setDefaultDateIndex(i == null ? 0 : i.intValue());

		List<EmisMetaGroupEnum> groupEnums = new ArrayList<EmisMetaGroupEnum>();
		for (Element groupEnumTag : getElements(tag, null, "groupEnum"))
			add(groupEnums, getGroupEnum(groupEnumTag));

		result.setGroupEnums(groupEnums);

		List<EmisIndicator> indicators = new ArrayList<EmisIndicator>();
		for (Element indicatorTag : getElements(tag, null, "indicator"))
			add(indicators, getEmisIndicator(indicatorTag));
		result.setIndicators(indicators);

		List<EmisPdfReportConfig> pdfReports = new ArrayList<EmisPdfReportConfig>();
		for (Element reportTag : getElements(tag, null, "pdfReport"))
			add(pdfReports, getPdfReportConfig(reportTag, indicators));
		result.setPdfReports(pdfReports);

		List<PriorityReportConfig> priorityReports = new ArrayList<PriorityReportConfig>(); 
		for (Element prioTag : getElements(tag, null, "priorityReport"))
			add(priorityReports, getPriorityReport(prioTag, indicators)); 
		result.setPriorityReports(priorityReports);
		
		List<ExcelReportConfig> excelReports = new ArrayList<ExcelReportConfig>();
		for (Element reportTag : getElements(tag, null,
				ExcelReportConfigSerializer.TAG_EXCEL_REPORT))
			add(excelReports, ExcelReportConfigSerializer.getExcelReport(meta,
					reportTag, indicators));

		result.setExcelReports(excelReports);

		return result;
	}

	private PriorityReportConfig getPriorityReport(Element tag, List<EmisIndicator> indicators)
	{
		PriorityReportConfig result = new PriorityReportConfigImpl(); 
		result.setName(getAttr(tag, "name")); 
		result.setMetaResult(getPriorityMetaResult(getElement(tag, "prioMetaResult"), indicators));
		
		return result; 
	}
	
	private EmisPdfReportConfig getPdfReportConfig(Element tag, List<EmisIndicator> indicators) 
	{
		verifyTagName(tag, "pdfReport");

		EmisPdfReportConfig result = null; 
		
		// Load correct implementation depending on version
		String version = tag.getAttribute("version");
		if (StringUtils.isEmpty(version))
			result = getLegacyPdfReport(tag, indicators);
		else if (version.equals(LayoutPdfReportConfig.PDF_REPORT_VERSION))
			result = getLayoutPdfReport(tag, indicators); 
		else 
			throw new IllegalArgumentException("Unexpected PDF report version."); 
		
		return result; 
	}
	
	private void readEmisPdfReportConfig(Element tag, EmisPdfReportConfig config)
	{
		config.setName(getAttr(tag, "name"));
		config.setEntityType((EmisMetaEntity) find(tag, "entityType", meta.getEntities()));

		PdfReportConfig.PageOrientation orientation = PdfReportConfig.PageOrientation.PORTRAIT;
		PdfReportConfig.PageSize size = PdfReportConfig.PageSize.A4;
		if (tag.getAttributeNode("pageSize") != null)
			size = PdfReportConfig.PageSize.valueOf(tag.getAttribute("pageSize"));
		if (tag.getAttributeNode("pageOrientation") != null)
			orientation = PdfReportConfig.PageOrientation.valueOf(tag.getAttribute("pageOrientation"));
		config.setPage(size, orientation);

		readTexts(tag, (TextSet) config);
		config.setShortTitles(getAttrAsBoolean(tag, "shortTitles"));
	}
	
	private void readTexts(Element parent, TextSet texts)
	{
		for (String key : texts.getTextKeys())
		{
			Element tag = getElement(parent, key);
			if (tag == null)
				continue; 
			
			texts.putText(key, tag.getTextContent(), getFont(tag)); 
		}
	}
	
	private ChartFont getFont(Element tag)
	{
		ChartColor col = getAttrAsColour(tag, "colour"); 
		return new ChartFont(getAttr(tag, "font", "Helvetica"), getAttrAsInt(tag, "fontSize", 12), getAttrAsInt(tag, "fontStyle", 0)); 
	}
	
	private EmisPdfReportConfig getLayoutPdfReport(Element tag, List<EmisIndicator> indicators)
	{
		LayoutPdfReportConfig config = new LayoutPdfReportConfigImpl(); 
		readEmisPdfReportConfig(tag, config); 
		
		for (Element pageTag : getElements(tag, null, "page"))
		{
			LayoutPageConfig page = new LayoutPageConfigImpl(); 
			for (Element frameTag : getElements(pageTag, null, "frame"))
				page.addFrame(getLayoutFrame(frameTag, indicators));
			
			readTexts(pageTag, page); 
			
			config.addPage(page);
		}
		
		return config; 
	}

	private LayoutFrameConfig getLayoutFrame(Element tag, List<EmisIndicator> indicators)
	{
		LayoutFrameConfig frame = new LayoutFrameConfigImpl(); 
		
		frame.setPosition(new Rectangle(getIdsAsDoubleArray(tag, "position")));
		
		// borders
		// borderRadius
		Element borderTag = getElement(tag, "borders"); 
		if (borderTag != null)
		{
			frame.setBorderRadius(getAttrAsInt(borderTag, "radius", 0)); 
			LayoutSides<LayoutBorderConfig> borders = new LayoutSides<LayoutBorderConfig>(); 
			borders.setLeft(getBorderConfig(borderTag, "left"));   
			borders.setTop(getBorderConfig(borderTag, "top"));  
			borders.setRight(getBorderConfig(borderTag, "right"));  
			borders.setBottom(getBorderConfig(borderTag, "bottom"));  

			frame.setBorders(borders);
		}
		
		Element backgroundTag = getElement(tag, "background");
		if (backgroundTag != null)
		{
			frame.setBackgroundImagePath(getAttr(backgroundTag, "image"));
			frame.setBackgroundColour(getAttrAsColour(backgroundTag, "colour"));
			frame.setBackgroundTransparency(getAttrAsInt(backgroundTag, "transparency", 0));
		}

		frame.setContentConfig(getPdfContentConfig(getElement(tag, "pdfContent"), indicators));

		return frame;
	}
	
	private LayoutBorderConfig getBorderConfig(Element tag, String side)
	{
		Element borderTag = getElement(tag, "border", "side", side);
		if (borderTag == null)
			return null; 
		
		LayoutBorderConfig result = new LayoutBorderConfig();
		result.setColor(getAttrAsColour(borderTag, "colour")); 
		result.setWidth(getAttrAsInt(borderTag, "width", 1));
		
		return result; 
	}
	
	
	private EmisPdfReportConfig getLegacyPdfReport(Element tag, List<EmisIndicator> indicators)
	{
		PdfReportConfig config = new PdfReportConfigImpl();
		readEmisPdfReportConfig(tag, config);
		
		config.setLayout(getAttrAsInt(tag, "rows", 1), getAttrAsInt(tag, "cols", 1));

		List<PdfContentConfig> contents = new ArrayList<PdfContentConfig>();
		for (Element contentTag : getElements(tag, null, "pdfContent"))
			contents.add(getPdfContentConfig(contentTag, indicators));

		config.setContentConfigs(contents);
		return config;
	}

	private PdfContentConfig getPdfContentConfig(Element tag, List<EmisIndicator> indicators) 
	{
		if (tag == null)
			return null; 
		
		PdfContentConfig result = null;
		String type = getAttr(tag, "type");
		if (type.equals("chart")) {
			PdfChartContentConfigImpl tmp = new PdfChartContentConfigImpl();
			tmp.setChartType(getAttrAsInt(tag, "chartType").intValue());
			tmp.setMetaResult(getTableMetaResult(getElement(tag, "tableMetaResult"), indicators));
			result = tmp;
		} else if (type.equals("table")) {
			PdfTableContentConfigImpl tmp = new PdfTableContentConfigImpl();
			tmp.setMetaResult(getTableMetaResult(getElement(tag, "tableMetaResult"), indicators));
			result = tmp;
		} else if (type.equals("gis")) {
			PdfGisContentConfigImpl tmp = new PdfGisContentConfigImpl();
			tmp.setMetaResult(getGisMetaResult(
					getElement(tag, "gisMetaResult"), indicators));
			result = tmp;
		} else if (type.equals("text")) {
			PdfTextContentConfigImpl tmp = new PdfTextContentConfigImpl();
			tmp.setText(tag.getTextContent());
			result = tmp;
		} else if (type.equals("vars")) {
			PdfVariableContentConfigImpl tmp = new PdfVariableContentConfigImpl();
			for (Element entryTag : getElements(tag, null, "entry"))
				tmp.addItem(entryTag.getAttribute("title"),
						entryTag.getAttribute("variable"));
			tmp.setEntity(NamedUtil.find(getAttr(tag, "entityType"),
					this.meta.getEntities()));

			result = tmp;
		} 
		else if (type.equals("prio")) 
		{
			PdfPriorityListContentConfigImpl tmp = new PdfPriorityListContentConfigImpl(); 
			tmp.setMetaResult(getPriorityMetaResult(getElement(tag, "prioMetaResult"), indicators)); 
		}

		result.setTitle(getAttr(tag, "title"));

		return result;
	}

	public MetaResult getMetaResult(Element tag, List<EmisIndicator> indicators) {
		if (!tag.getNodeName().equals("metaResult"))
			throw new IllegalArgumentException("Expected 'metaResult' tag.");

		Element child = getElement(tag, "tableMetaResult");
		if (child != null) 
			return getTableMetaResult(child, indicators);

		child = getElement(tag, "gisMetaResult");
		if (child != null)
			return getGisMetaResult(child, indicators);
		
		child = getElement(tag, "prioMetaResult"); 
		if (child != null)
			return getPriorityMetaResult(child, indicators); 

		return null;
	}

	public PriorityMetaResult getPriorityMetaResult(Element tag, List<EmisIndicator> indicators)
	{
		if (tag == null)
			return null; 
		
		PriorityMetaResult result = new PriorityMetaResultImpl(); 
		updateMetaResult(result, tag, indicators); 
		
		getIdsAsArray(tag, "fields");
		result.setFilterEmpty(getAttrAsBoolean(tag, "filterEmpty", false));
		result.setListEntity((EmisMetaEntity) find(tag, "entityType", this.meta.getEntities()));
		
		return result; 
	}
	
	
	public TableMetaResult getTableMetaResult(Element tag, List<EmisIndicator> indicators) 
	{
		TableMetaResult result = new TableMetaResultImpl();
		updateMetaResult(result, tag, indicators);
		String sort = tag.getAttribute("sort");
		if ("asc".equals(sort))
			result.setSortOrder(1);
		else if ("desc".equals(sort))
			result.setSortOrder(-1);
		else
			result.setSortOrder(0);

		List<MetaResultDimension> dimensions = new ArrayList<MetaResultDimension>();
		for (Element dimTag : getElements(tag, null, "dimension"))
			dimensions.add(getMetaResultDimension(dimTag));

		result.setDimensionCount(dimensions.size());
		int index = 0;
		for (MetaResultDimension dimension : dimensions) {
			result.setDimension(index, dimension);
			index++;
		}

		return result;
	}

	private MetaResultDimension getMetaResultDimension(Element tag) {
		MetaResultDimension result = null;

		String type = getAttr(tag, "type");
		if (type.equals("date")) {
			MetaResultDimensionDate tmp = new MetaResultDimensionDate();
			tmp.setDateEnumType((EmisMetaDateEnum) find(tag, "dateType",
					this.meta.getDateEnums()));

			byte[] values = getBytes(getIdsAsIntArray(tag, "date"));
			if (values != null) {
				EmisEnumTupleValue dateValue = new EnumTupleValueImpl();
				dateValue.setEnumTuple(tmp.getDateEnumType());
				dateValue.setIndex(values);
				tmp.setDateEnum(dateValue);
			} else {
				tmp.setDateEnum(null);
			}
			result = tmp;
		} else if (type.equals("enum")) {
			MetaResultDimensionEnum tmp = new MetaResultDimensionEnum();
			tmp.setEnumType((EmisMetaEnum) find(tag, "enum",
					this.meta.getEnums()));
			result = tmp;
		}
		else if (type.equals("entityFilter")) 
		{
			MetaResultDimensionEntityFilter tmp = new MetaResultDimensionEntityFilter();

			EmisMetaEntity entity = (EmisMetaEntity) find(tag, "entity",
					this.meta.getEntities());
			tmp.setField((EmisMetaData) find(tag, "field", entity.getData()));
			result = tmp;
		}
		else if (type.equals("entityAncestors")) 
		{
			MetaResultDimensionEntityAncestors tmp = new MetaResultDimensionEntityAncestors();
			updateMetaResultDimensionEntity(tag, tmp);
			result = tmp;
		}
		else if (type.equals("entityChildren")) {
			MetaResultDimensionEntityChildren tmp = new MetaResultDimensionEntityChildren();
			updateMetaResultDimensionEntity(tag, tmp);
			result = tmp;
		} else if (type.equals("entityGrandChildren")) {
			MetaResultDimensionEntityGrandChildren tmp = new MetaResultDimensionEntityGrandChildren();
			updateMetaResultDimensionEntity(tag, tmp);
			result = tmp;
		}

		result.setName(getAttr(tag, "name"));

		return result;
	}

	private void updateMetaResultDimensionEntity(Element tag, MetaResultDimensionEntity result) 
	{
		result.setHierarchy((EmisMetaHierarchy) find(tag, "hierarchy",
				this.meta.getHierarchies()));
		result.setEntityType((EmisMetaEntity) find(tag, "entityType",
				this.meta.getEntities()));

		List<Integer> ids = new ArrayList<Integer>();
		List<String> names = new ArrayList<String>();
		for (Element entityTag : getElements(tag, null, "entity")) 
		{
			ids.add(getAttrAsInt(entityTag, "id"));
			names.add(getAttr(entityTag, "name"));
		}

		Integer[] idsArray = (Integer[]) ids.toArray(new Integer[0]);
		String[] namesArray = (String[]) names.toArray(new String[0]);
		Integer dateIndex = getAttrAsInt(tag, "dateIndex");
		if (dateIndex != null)
			result.setPath(ArrayUtils.toPrimitive(idsArray), namesArray,
					dateIndex.intValue());
	}

	public GisMetaResult getGisMetaResult(Element tag,
			List<EmisIndicator> indicators) {
		GisMetaResult result = new GisMetaResultImpl();
		updateMetaResult(result, tag, indicators);
		return result;
	}

	private void updateMetaResult(MetaResult result, Element tag,
			List<EmisIndicator> indicators) {
		result.setHierarchy((EmisMetaHierarchy) find(tag, "hierarchy",
				this.meta.getHierarchies()));

		EmisIndicator indicator = (EmisIndicator) find(tag, "indicator",
				indicators);
		if (indicator != null)
			result.setIndicator(indicator);
		else {
			List<MetaResultValue> values = new ArrayList<MetaResultValue>();
			for (Element metaResultTag : getElements(tag, "metaResultValues", "metaResultValue"))
				values.add(getMetaResultValue(metaResultTag, indicators));
			result.setMetaResultValues(values);
		}

		result.setContext(getEmisContext(getElement(tag, "context")));
		Element globalFilterTag = getElement(tag, "globalFilter"); 
		if (globalFilterTag != null)
			result.setGlobalFilter(getEmisContext(getElement(tag, "context")));  
	}

	private MetaResultValue getMetaResultValue(Element tag,
			List<EmisIndicator> indicators) {
		MetaResultValue result = new MetaResultValueImpl();
		result.setIndicator((EmisIndicator) find(tag, "indicator", indicators));

		String key = getAttr(tag, "aggregator");
		if (!StringUtils.isEmpty(key)) {
			result.setAggregatorKey(getAttr(tag, "aggregator"));
			if (result.getAggregatorKey().startsWith("?")) {
				result.setTarget(getAttrAsDouble(tag, "target"));
			}
		}
		return result;
	}

	private EmisMetaGroupEnum getGroupEnum(Element tag) {
		verifyTagName(tag, "groupEnum");

		EmisMetaEnum baseEnum = NamedUtil.find(getAttr(tag, "enum"),
				meta.getEnums());
		if (baseEnum == null)
			return null;

		EmisMetaGroupEnum result = new MetaGroupEnum();
		result.setName(getAttr(tag, "id"));
		result.setBaseEnum(baseEnum);

		List<String> groupNames = new ArrayList<String>();
		for (Element groupTag : getElements(tag, null, "group")) {
			String groupName = getAttr(groupTag, "name");
			groupNames.add(groupName);
			result.setGroupValues(groupName,
					getAttr(groupTag, "values").split(","));
		}

		result.setValues(groupNames.toArray(new String[0]));

		return result;
	}

	private EmisIndicator getEmisIndicator(Element tag) {
		verifyTagName(tag, "indicator");

		EmisIndicator indicator = null;
		String type = getAttr(tag, "type");
		if (type.equals("timeRatio")) {
			indicator = new IndicatorTimeRatio();
			((EmisIndicatorRatio) indicator).setFactor(getAttrAsDouble(tag,
					"factor").doubleValue());
			((EmisIndicatorTimeRatio) indicator).setTimeOffset(getAttrAsInt(
					tag, "offset").intValue());
		}
		if (type.equals("ratio")) {
			indicator = new IndicatorRatio();
			((EmisIndicatorRatio) indicator).setFactor(getAttrAsDouble(tag,
					"factor").doubleValue());
		} else if (type.equals("simple"))
			indicator = new IndicatorSimple();

		indicator.setName(getAttr(tag, "name"));
		indicator.setGroupName(getAttr(tag, "groupName"));
		indicator.setYAxisLabel(getAttr(tag, "yAxis"));

		indicator.setMaxValue(getAttrAsDouble(tag, "max").doubleValue());

		double good = getAttrAsDouble(tag, "good").doubleValue();
		String goodText = getAttr(tag, "goodText");
		double bad = getAttrAsDouble(tag, "bad").doubleValue();
		String badText = getAttr(tag, "badText");
		if (Double.isNaN(bad))
			indicator.setThreshold(good, goodText,
					getAttrAsBoolean(tag, "bigIsBetter").booleanValue());
		else {
			indicator.setThreshold(good, goodText, bad, badText);
		}

		getAggregators(indicator, tag);

		return indicator;
	}

	private void getAggregators(EmisAggregatorList aggrList, Element tag) 
	{
		for (Element aggrTag : getElements(tag, null, "aggregator"))
			aggrList.setAggregator(getAttr(aggrTag, "key"),
					getEmisAggregatorDef(aggrTag));
	}

	private EmisAggregatorDef getEmisAggregatorDef(Element tag) 
	{
		verifyTagName(tag, "aggregator");

		EmisAggregatorDef result = null;
		if ("weighted".equals(getAttr(tag, "type"))) 
		{
			EmisSampleAggregatorDef compoundAggr = new WeightedAggregatorDef();
			getAggregators(compoundAggr, tag);
			result = compoundAggr;
		} 
		else
			result = new AggregatorDef();

		result.setName(getAttr(tag, "name"));
		EmisMetaEntity entity = (EmisMetaEntity) find(tag, "entity", this.meta.getEntities());
		result.setEntity(entity);
		if (entity != null)
			result.setMetaData((EmisMetaData) find(tag, "field", entity.getData()));
		
		if (result.getMetaData() == null)
		{
			EmisMetaDateEnum dateType = find(tag, "countDateType", this.meta.getDateEnums()); 
			if (dateType == null && meta.getDateEnums().size() > 0)
				dateType = meta.getDateEnums().get(0); 

			result.setCountDateType(dateType); 
		}

		result.setContext(getEmisContext(getElement(tag, "context")));

		for (Element ignoreFilterTag : getElements(tag, null, "ignoreFilter"))
			result.addIgnoreFilter(getFilterTarget(ignoreFilterTag));

		return result;
	}
	
	private FilterTarget getFilterTarget(Element tag)
	{
		FilterTarget result = new FilterTarget(); 
		EmisMetaEntity entityType = NamedUtil.find(getAttr(tag, "entity"), meta.getEntities()); 
		result.setField(NamedUtil.find(getAttr(tag, "field"), entityType.getData()));  
		if (getAttr(tag, "enum") != null)
		{
			EmisMetaEnum foundEnum = NamedUtil.find(getAttr(tag, "enum"), meta.getEnums());
			if (foundEnum != null)
				result.setEnumType(foundEnum);
		}
		
		return result; 
	}

	private byte[] getBytes(int[] ids) {
		if (ids == null) {
			return null;
		}
		byte[] result = new byte[ids.length];
		for (int i = 0; i < ids.length; i++) {
			result[i] = (byte) ids[i];
		}
		return result;
	}

	private EmisContext getEmisContext(Element tag) {
		if (tag == null) {
			return null;
		}
		verifyTagName(tag, "context");

		String type = getAttr(tag, "type");
		if (!type.equals("full")) {
			System.out.println("Unknown context: " + type);
			return null;
		}

		Context result = new Context();
		Integer index = getAttrAsInt(tag, "dateIndex");
		if (index != null) {
			result.setHierarchyDateIndex(index.intValue());
		}
		result.setDateType((EmisMetaDateEnum) find(tag, "dateType",
				this.meta.getDateEnums()));
		result.setEntityType((EmisMetaEntity) find(tag, "entityType",
				this.meta.getEntities()));

		List<EmisEnumTupleValue> enums = null;
		for (Element dateEnumTag : getElements(tag, null, "date")) {
			EmisEnumTupleValue tuple = getEnumTuple(
					dateEnumTag,
					"date",
					(EmisMetaEnumTuple) find(dateEnumTag, "dateEnum",
							this.meta.getDateEnums()));
			if (enums == null)
				enums = new ArrayList<EmisEnumTupleValue>();

			enums.add(tuple);
		}

		result.setDates(enums);

		List<EmisEntity> entities = null;
		for (Element entityTag : getElements(tag, null, "entity")) {
			EmisEntity entity = new Entity();
			entity.setEntityType((EmisMetaEntity) find(entityTag, "entityType",
					this.meta.getEntities()));
			entity.setId(getAttrAsInt(entityTag, "id").intValue());

			if (entities == null)
				entities = new ArrayList<EmisEntity>();
			entities.add(entity);
		}
		result.setEntities(entities);

		for (Element enumFilterTag : getElements(tag, null, "enumFilter")) {
			EmisEnumSet value = new EnumSetImpl();
			value.setEnum((EmisMetaEnum) find(enumFilterTag, "enumType",
					this.meta.getEnums()));
			int[] ids = getIdsAsIntArray(enumFilterTag, "values");
			Set<Byte> idSet = new HashSet<Byte>();
			for (int i = 0; i < ids.length; i++)
				idSet.add(Byte.valueOf((byte) ids[i]));
			value.setAllIndexes(idSet);

			result.addEnumFilter(value);
		}

		for (Element enumFilterTag : getElements(tag, null, "entityFilter")) 
		{
			result.addEntityFilter(getAttr(enumFilterTag, "key"),
					getBytes(getIdsAsIntArray(enumFilterTag, "values")));
		}
		
		for (Element dateFilterTag : getElements(tag, null, "dateFilter")) 
		{
			EmisMetaDateEnum dateEnumType = NamedUtil.find(getAttr(dateFilterTag, "key"), meta.getDateEnums()); 
			
			EmisEnumSet values = new EnumSetImpl(dateEnumType, getBytes(getIdsAsIntArray(dateFilterTag, "values"))); 
			result.addDateEnumFilter(values);
		}
		
		return result;
	}

	private <T> void add(List<T> list, T item) {
		if (item != null)
			list.add(item);
	}

	private void verifyTagName(Element tag, String name) {
		if (!tag.getNodeName().equals(name))
			throw new IllegalArgumentException("Expected tag '" + name
					+ "' but got '" + tag.getNodeName());
	}

	private List<Element> getElements(Element parent, String tagName,
			String itemTagName) {
		List<Element> result = new ArrayList<Element>();
		if (tagName != null)
			parent = getElement(parent, tagName);

		if (parent == null)
			return result;

		NodeList nodes = parent.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (!nodes.item(i).getNodeName().equals(itemTagName))
				continue;

			result.add((Element) nodes.item(i));
		}

		return result;
	}

	private List<Element> getElements(Element parent, String tagName,
			String attr, String value) {
		List<Element> result = new ArrayList<Element>();
		for (Element tmp : getElements(parent, null, tagName)) {
			if (value.equals(tmp.getAttribute(attr)))
				result.add(tmp);
		}

		return result;
	}

	private String getElementText(Element parent, String tagName) {
		Element e = getElement(parent, tagName);
		if (e == null) {
			return null;
		}
		return e.getTextContent();
	}

	private Element getElement(Element parent, String tagName) {
		if (parent == null)
			return null;

		NodeList nodes = parent.getElementsByTagName(tagName);
		if ((nodes == null) || (nodes.getLength() == 0))
			return null;

		return (Element) nodes.item(0);
	}

	private Element getElement(Element parent, String tagName, String attr,
			String value) {
		for (Element element : getElements(parent, null, tagName)) {
			if (value.equals(element.getAttribute(attr)))
				return element;
		}

		return null;
	}

	private String[] getIdsAsArray(Element tag, String attr) {
		String value = tag.getAttribute(attr);
		if (StringUtils.isEmpty(value))
			return new String[0];

		return value.split(",");
	}

	private EmisEnumTupleValue getEnumTuple(Element tag, String attr,
			EmisMetaEnumTuple enumType) {
		String[] values = getIdsAsArray(tag, attr);
		if (values == null) {
			return null;
		}
		EmisEnumTupleValue result = new EnumTupleValueImpl();
		result.setEnumTuple(enumType);
		result.setValue(values);

		return result;
	}

	private double[] getIdsAsDoubleArray(Element tag, String attr) {
		String[] ids = getIdsAsArray(tag, attr);
		if (ids == null)
			return null;

		double[] result = new double[ids.length];
		for (int i = 0; i < ids.length; i++) {
			try {
				if (ids[i] != null)
					result[i] = Double.parseDouble(ids[i]);
			} catch (Throwable err) {
			}
		}

		return result;
	}

	private int[] getIdsAsIntArray(Element tag, String attr) {
		String[] ids = getIdsAsArray(tag, attr);
		if (ids == null || ids.length == 0)
			return null;

		int[] intIds = new int[ids.length];
		for (int i = 0; i < ids.length; i++) {
			try {
				if (ids[i] != null)
					intIds[i] = Integer.parseInt(ids[i]);
			} catch (Throwable err) {
			}
		}

		return intIds;
	}

	private <T extends Named> T find(Element tag, String attr, List<T> list) {
		String name = getAttr(tag, attr);
		if (name == null) {
			return null;
		}
		return NamedUtil.find(name, list);
	}

	private <T extends Named> NamedIndexList<T> findAsList(Element tag, String attr, List<T> list) 
	{
		NamedIndexList<T> result = new NamedIndexList<T>();
		for (String name : getIdsAsArray(tag, attr))
			add(result, NamedUtil.find(name, list));

		return result;
	}

	private String getAttr(Element tag, String attr) {
		if (StringUtils.isEmpty(tag.getAttribute(attr))) {
			return null;
		}
		return tag.getAttribute(attr);
	}

	private String getAttr(Element tag, String attr, String defaultValue) 
	{
		String value = getAttr(tag, attr); 
		if (value == null)
			return defaultValue; 
		
		return value; 
	}

	private ChartColor getAttrAsColour(Element tag, String attr)
	{
		String value = tag.getAttribute(attr); 
		if (StringUtils.isEmpty(value))
			return null; 
		
		if (value.startsWith("#"))
			value = value.substring(1); 
		
		int len = 1; 
		if (value.length() == 3)
			len = 1; 
		else if (value.length() == 6)
			len = 2; 
		else 
			return null; 
		
		int r = Integer.parseInt(value.substring(0, len), 16); 
		int g = Integer.parseInt(value.substring(len, 2 * len), 16); 
		int b = Integer.parseInt(value.substring(2 * len, 3 * len), 16); 
		
		return new ChartColor(r, g, b);  
	}
	
	private Integer getAttrAsInt(Element tag, String attr) {
		String value = tag.getAttribute(attr);
		if (value == null)
			return null;
		try {
			return Integer.valueOf(Integer.parseInt(value));
		} catch (Throwable err) {
		}
		return null;
	}

	private Integer getAttrAsInt(Element tag, String attr, int defaultValue) 
	{ 
		Integer value = getAttrAsInt(tag, attr); 
		return value == null ? defaultValue : value; 
	}
	private boolean getAttrAsBoolean(Element tag, String attr, boolean defaultValue)
	{
		Boolean value = getAttrAsBoolean(tag, attr); 
		if (value == null)
			return defaultValue; 
		return value; 
	}
	
	private Boolean getAttrAsBoolean(Element tag, String attr) {
		String value = tag.getAttribute(attr);
		if (value == null)
			return null;
		try {
			return Boolean.valueOf(Boolean.parseBoolean(value));
		} catch (Throwable err) {
		}
		return null;
	}

	private Double getAttrAsDouble(Element tag, String attr) {
		String value = tag.getAttribute(attr);
		if (value == null)
			return null;
		try {
			return Double.valueOf(Double.parseDouble(value));
		} catch (Throwable err) {
		}
		return null;
	}

	public static double[] getCoordinates(String coords) {
		if (StringUtils.isEmpty(coords)) {
			return new double[0];
		}
		String[] values = coords.split(",");
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(""))
				result[i] = (0.0D / 0.0D);
			else {
				result[i] = Double.longBitsToDouble(Long.parseLong(values[i],
						16));
			}
		}
		return result;
	}

	public List<UnflattenDbQuery> getUnflattenDbQueries(Element tag) {
		List<UnflattenDbQuery> result = new ArrayList<UnflattenDbQuery>();
		for (Element queryTag : getElements(tag, null, "query"))
			result.add(getUnflattenDbQuery(queryTag));

		return result;
	}

	private UnflattenDbQuery getUnflattenDbQuery(Element tag) {
		UnflattenDbQuery result = new UnflattenDbQuery();
		result.setName(tag.getAttribute("name"));
		result.setTable(tag.getAttribute("table"));
		for (Element columnTag : getElements(tag, null, "column"))
			result.addColumn(getUnflattenDbColumn(columnTag));

		for (Element rowTag : getElements(tag, null, "row"))
			result.addRow(getUnflattenDbRow(rowTag));

		return result;
	}

	private UnflattenDbColumn getUnflattenDbColumn(Element tag) {
		UnflattenDbColumn result = new UnflattenDbColumn();
		result.setName(tag.getAttribute("name"));
		if (tag.hasAttribute("column"))
			result.setColumn(tag.getAttribute("column"));
		else if (tag.hasAttribute("value"))
			result.setValue(tag.getAttribute("value"));
		else
			result.setColumn(tag.getAttribute("name"));

		return result;
	}

	private UnflattenDbRow getUnflattenDbRow(Element tag) {
		UnflattenDbRow result = new UnflattenDbRow();
		NamedNodeMap attrs = tag.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			UnflattenDbColumn col = new UnflattenDbColumn();
			col.setName(attrs.item(i).getNodeName());
			col.setValue(attrs.item(i).getNodeValue());

			result.addColumn(col);
		}

		for (Element colTag : getElements(tag, null, "column"))
			result.addColumn(getUnflattenDbColumn(colTag));

		return result;
	}

	public List<EmisValidation> getValidation(Element tag, EmisMeta meta) {
		this.meta = meta;

		verifyTagName(tag, "emisValidation");

		Set<String> validationNames = new HashSet<String>();
		List<EmisValidation> result = new ArrayList<EmisValidation>();
		for (Element validationTag : getElements(tag, null, "validation")) {
			EmisValidation validation = parseValidation(validationTag, meta);
			if (validationNames.contains(validation.getName()))
				throw new IllegalArgumentException(
						"Duplicate name for Validation: "
								+ validation.getName());
			validationNames.add(validation.getName());
			result.add(validation);
		}

		return result;
	}

	private EmisValidation parseValidation(Element tag, EmisMeta meta) {
		verifyTagName(tag, "validation");
		EmisValidation result = new ValidationImpl();
		result.setName(tag.getAttribute("name"));
		result.setEntityType(NamedUtil.find(tag.getAttribute("entity"),
				meta.getEntities()));

		if (StringUtils.isEmpty(result.getName()))
			throw new IllegalArgumentException("Validation without name");

		List<EmisValidationRule> rules = new ArrayList<EmisValidationRule>();

		for (Element fieldTag : getElements(tag, null, "displayField")) {
			EmisMetaEntity entityType = NamedUtil.find(
					fieldTag.getAttribute("entity"), meta.getEntities());
			if (entityType == null)
				continue;

			EmisMetaData field = NamedUtil.find(fieldTag.getAttribute("field"),
					entityType.getData());
			if (field != null)
				result.addAdditionalField(field);
		}

		Set<String> ruleNames = new HashSet<String>();
		NodeList nodes = tag.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			EmisValidationRule rule = parseValidationRule(
					(Element) nodes.item(i), meta);
			if (rule != null) {
				if (ruleNames.contains(rule.getName()))
					throw new IllegalArgumentException(
							"Rules with duplicate name: " + rule.getName());
				ruleNames.add(rule.getName());

				rules.add(rule);
			}
		}

		result.setRules(rules);

		return result;
	}

	private EmisValidationRule parseValidationRule(Element tag, EmisMeta meta) {
		EmisValidationRule result = null;

		if (tag.getNodeName().equals("minMax"))
			result = new ValidationMinMaxRuleImpl();
		else if (tag.getNodeName().equals("ratio"))
			result = new ValidationRatioRuleImpl();
		else if (tag.getNodeName().equals("notExceeding"))
			result = new ValidationNotExceedingRule();
		else if (tag.getNodeName().equals("timeRatio")) {
			EmisValidationTimeRatioRule tmp = new ValidationTimeRatioRuleImpl();
			tmp.setDateType(NamedUtil.find(getAttr(tag, "dateType"),
					meta.getDateEnums()));
			result = tmp;
		} else
			return null;

		parseFields(result, getElements(tag, null, "field"));
		parseGroupBy(result, getAttr(tag, "groupBy"), meta.getEnums());

		if (result instanceof ValidationMinMaxRuleImpl) {
			ValidationMinMaxRuleImpl minMax = (ValidationMinMaxRuleImpl) result;
			if (tag.hasAttribute("max"))
				minMax.setMaxValue(getAttrAsDouble(tag, "max"));
			if (tag.hasAttribute("min"))
				minMax.setMinValue(getAttrAsDouble(tag, "min"));
		}

		result.setName(getAttr(tag, "name"));
		if (StringUtils.isEmpty(result.getName()))
			throw new IllegalArgumentException("Validation rule without name.");

		if (tag.hasAttribute("description"))
			result.setDescription(tag.getAttribute("description"));

		checkValidationRule(result);

		return result;
	}

	private void parseGroupBy(EmisValidationRule rule, String groupBy,
			NamedIndexList<EmisMetaEnum> enums) {
		if (StringUtils.isEmpty(groupBy))
			return;

		rule.setGroupBy(parseFilter(groupBy, true));
	}

	private void parseFields(EmisValidationRule rule, List<Element> tags) {
		EmisMetaData[] fields = new EmisMetaData[tags.size()];
		EmisValidationFilter[] filters = new EmisValidationFilter[tags.size()];

		int index = 0;
		for (Element tag : tags) {
			fields[index] = parseField(getAttr(tag, "field"));
			filters[index] = parseFilter(getAttr(tag, "filter"), false);
			index++;
		}

		rule.setFields(fields, filters);
	}

	private void checkValidationRule(EmisValidationRule rule) {
		// Check that fields are all from the same entity.
		if (rule.getFieldCount() == 0)
			throw new IllegalArgumentException("Validation rule has no fields.");

		EmisMetaEntity entityType = rule.getField(0).getEntity();
		for (int i = 1; i < rule.getFieldCount(); i++) {
			if (!NamedUtil.sameName(entityType, rule.getField(i).getEntity()))
				throw new IllegalArgumentException(
						"Validation rule has fields for two different entities: "
								+ entityType.getName() + " and "
								+ rule.getField(i).getEntity().getName());
		}

		// Make sure all fields have the groupBy enums as array.
		if (rule.getGroupBy() != null) {
			for (EmisEnumSet e : rule.getGroupBy().getFilters().values()) {
				for (int i = 0; i < rule.getFieldCount(); i++) {
					// Ensure that each field has enum of group by.
					EmisMetaData field = rule.getField(i);
					String fieldName = field.getEntity().getName() + "."
							+ field.getName();
					if (!hasDimension(e.getEnum(), field.getArrayDimensions()))
						throw new IllegalArgumentException(
								"Validation rule error: Group by for '"
										+ e.getEnum().getName()
										+ "' cannot be applied to field '"
										+ fieldName + "'.");

					// Ensure that non of field filters uses group by enum
					EmisValidationFilter filter = rule.getFilter(i);
					if (filter != null
							&& null != filter.getFilterFor(e.getEnum()))
						throw new IllegalArgumentException(
								"Validation rule error: Group by for '"
										+ e.getEnum().getName()
										+ "' cannot be used in field '"
										+ fieldName + "' filter.");
				}
			}
		}
	}

	private boolean hasDimension(EmisMetaEnum needle,
			EmisMetaEnumTuple dimensions) {
		if (dimensions == null || dimensions.getDimensions() == 0)
			return false;

		for (EmisMetaEnum item : dimensions.getEnums()) {
			if (NamedUtil.sameName(item, needle))
				return true;
		}

		return false;
	}

	private EmisMetaData parseField(String field) {
		int pos = field.indexOf(".");
		EmisMetaEntity entityType = NamedUtil.find(field.substring(0, pos),
				meta.getEntities());
		EmisMetaData result = NamedUtil.find(field.substring(pos + 1),
				entityType.getData());
		if (result == null)
			throw new IllegalArgumentException("Field '" + field
					+ "' not found.");

		return result;
	}

	private EmisValidationFilter parseFilter(String filterList,
			boolean allowAllValues) {
		if (StringUtils.isEmpty(filterList))
			return null;

		EmisValidationFilter result = new ValidationFilter();

		String[] filters = filterList.split(";");
		for (String filter : filters) {
			int pos = filter.indexOf("=");
			if (pos == -1 && !allowAllValues)
				continue;

			String enumName = pos == -1 ? filter : filter.substring(0, pos);

			EmisMetaEnum enumType = NamedUtil.find(enumName, meta.getEnums());
			if (enumType == null)
				throw new IllegalArgumentException("Unknown enum '" + enumName
						+ "'");

			// Create resulting enum set
			EmisEnumSet enumSet = new EnumSetImpl();
			enumSet.setEnum(enumType);

			if (pos == -1)
				enumSet.setAll();
			else {
				String[] values = filter.substring(pos + 1).split(",");
				for (String value : values)
					enumSet.addValue(value);
			}

			result.addFilter(enumSet);
		}

		if (result.getFilters().size() == 0)
			return null;

		return result;
	}
}
