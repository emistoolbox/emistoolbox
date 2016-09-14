package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfGisContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.renderer.charts.impl.ChartUtil;
import com.emistoolbox.server.renderer.gis.GisUtil;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfTableContent;
import com.emistoolbox.server.results.ResultCollector;
import com.emistoolbox.server.results.TableResultCollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class PdfUtil
{
    public static final ChartFont LABEL_FONT = new ChartFont("SansSerif", 1, 6);

    public static PdfReport getPdfReport(ReportMetaResult metaResult, EmisDataSet dataSet)
    {
        PdfReport report = new PdfReportImpl();
        report.setReportConfig(metaResult.getReportConfig());

        List<PdfContentConfig> contents = metaResult.getReportConfig().getContentConfigs();
        int totalPages = contents.size() / (metaResult.getReportConfig().getRows() * metaResult.getReportConfig().getColumns());
        if (contents.size() % (metaResult.getReportConfig().getRows() * metaResult.getReportConfig().getColumns()) != 0)
            totalPages++;

        EmisContext context = metaResult.getContextWithGlobalFilter();
        
        // Need to multiple totalPages by number of EmisEntities
        List<EmisEntity> entities = context.getEntities(); // global-changed: metaResult.getContext().getEntities();
        if (entities.size() == 0)
            totalPages = 0;
        else
            totalPages *= getEntityCount(context, entities.get(0), metaResult.getReportConfig().getEntityType(), getDateIndex(context), dataSet.getHierarchy(metaResult.getHierarchy().getName()), dataSet); 

        int indexEntityType = NamedUtil.findIndex(metaResult.getReportConfig().getEntityType(), metaResult.getHierarchy().getEntityOrder());
        if (indexEntityType == -1)
            return report;

        // Remember current context values.
        EmisMetaEntity oldEntityType = context.getEntityType(); 
        List<EmisEntity> oldEntities = context.getEntities(); 
        int[] oldIds = metaResult.getEntityPathIds(); 
        String[] oldNames = metaResult.getEntityPathNames(); 
        
        addAllToPdfReport(report, metaResult.getEntityPathIds(), metaResult.getEntityPathNames(), indexEntityType, contents, 0, totalPages, metaResult, dataSet);

        metaResult.getContext().setEntityType(oldEntityType); 
        metaResult.getContext().setEntities(oldEntities); 
        metaResult.setEntityPath(oldIds, oldNames); 
        
        // Reset context values. 
        return report;
    }

    private static int getEntityCount(EmisContext context, EmisEntity parent, EmisMetaEntity childEntityType, int dateIndex, EmisHierarchy hierarchy, EmisDataSet dataSet)
    {
        List<int[]> items = hierarchy.getDescendants(dateIndex, parent.getEntityType(), parent.getId(), childEntityType);
        items = ResultCollector.filter(context, childEntityType, items, dataSet); 
        if (items == null)
            return 0; 
        
        int result = 0; 
        for (int[] ints : items)
        	for(int i : ints)
        		if (i > -1)
        			result += ints.length; 
        
        return result; 
    }

    private static int addAllToPdfReport(PdfReport report, int[] ids, String[] names, int indexEntityType, List<PdfContentConfig> contents, int startPage, int totalPages, ReportMetaResult metaResult, EmisDataSet dataSet)
    {
        if (indexEntityType + 1 == ids.length)
        {
            EmisMetaEntity entityType = metaResult.getHierarchy().getEntityOrder().get(indexEntityType);
            
            EmisContext context = metaResult.getContext(); 
            context.setEntityType(entityType);
            List<EmisEntity> result = new ArrayList<EmisEntity>();
            EmisEntity entity = new Entity(entityType, ids[ids.length - 1]); 
            entity.setName(names[names.length - 1]); 
            result.add(entity);
            context.setEntities(result);
            metaResult.setEntityPath(ids, names);
            
            return addEntityToPdfReport(report, contents, startPage, totalPages, metaResult, dataSet);
        }

        int[] newIds = new int[ids.length + 1];
        for (int i = 0; i < ids.length; i++)
            newIds[i] = ids[i];

        String[] newNames = new String[names.length + 1];
        for (int i = 0; i < names.length; i++)
            newNames[i] = names[i];

        EmisHierarchy hierarchy = dataSet.getHierarchy(report.getReportConfig().getHierarchy().getName());
        int dateTypeIndex = NamedUtil.findIndex((EmisMetaDateEnum) hierarchy.getDateType(), dataSet.getMetaDataSet().getDateEnums());
        int[] childIds = hierarchy.getChildren(dateTypeIndex, hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length - 1), ids[ids.length - 1]);
        childIds = ResultCollector.filter(metaResult.getContextWithGlobalFilter(), hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length), childIds, dataSet); 

        if (childIds == null || childIds.length == 0)
            return 0;

        EmisEntityDataSet entityDataSet = dataSet.getEntityDataSet(hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length), (EmisMetaDateEnum) hierarchy.getDateType());
        Map<Integer, String> childNames = entityDataSet.getAllValues(getDateIndex(metaResult.getContext()), "name", childIds);
        for (int i = 0; i < childIds.length; i++)
        {
        	if (childIds[i] == -1)
        		continue; 
        	
            newIds[newIds.length - 1] = childIds[i];
            newNames[newNames.length - 1] = childNames.get(childIds[i]);

            startPage += addAllToPdfReport(report, newIds, newNames, indexEntityType, contents, startPage, totalPages, metaResult, dataSet);
        }

        return startPage;
    }

    private static int getDateIndex(EmisContext context)
    {
        List<EmisEnumTupleValue> dates = context.getDates();
        if (dates == null || dates.size() == 0)
            return 0;

        byte[] indexes = dates.get(0).getIndex();
        return indexes[indexes.length - 1];
    }

    private static int addEntityToPdfReport(PdfReport report, List<PdfContentConfig> contents, int startPage, int totalPages, ReportMetaResult metaResult, EmisDataSet dataSet)
    {
        String subtitle = MetaResultDimensionUtil.getTitle(metaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NAMES, false, true);

        int currentPage = startPage;

        int i = 0;
        while (i < contents.size())
        {
            PdfPage page = getNewPage(metaResult, metaResult.getReportConfig(), subtitle, "Page " + (currentPage + 1) + "/" + totalPages);
            report.addPage(page);
            currentPage++;

            for (int row = 0; row < metaResult.getReportConfig().getRows(); row++)
            {
                for (int col = 0; col < metaResult.getReportConfig().getColumns(); col++)
                {
                    if (i >= contents.size())
                        return currentPage - startPage;

                    page.addContent(row, col, getNewContent(metaResult, contents.get(i), dataSet));
                    i++;
                }
            }
        }

        return currentPage - startPage;
    }

    private static PdfPage getNewPage(ReportMetaResult metaResult, PdfReportConfig config, String subtitle, String footer)
    {
        PdfPage page = new PdfPageImpl();
        page.setLayout(config.getRows(), config.getColumns());
        page.setTitle(config.getTitle(), subtitle);

        if (StringUtils.isEmpty(config.getFooter()))
            page.setFooter(footer);
        else if (StringUtils.isEmpty(footer))
            page.setFooter(config.getFooter());
        else
            page.setFooter(config.getFooter() + " - " + footer);

        return page;
    }

    private static Set<Byte> merge(byte[] value1, byte[] value2)
    {
    	Set<Byte> result = new HashSet<Byte>(); 
    	if (value1 == null)
    	{
    		for (byte v : value2)
    			result.add(v); 
    		return result; 
    	}
    	
    	if (value2 == null)
    	{
    		for (byte v : value1)
    			result.add(v); 
    		return result; 
    	}

    	for (byte val : value1)
    		result.add(val); 
    	
    	Set<Byte> set2= new HashSet<Byte>(); 
    	for (byte val : value2)
    		set2.add(val); 
    	
    	result.retainAll(set2); 
    	return result; 
    }
    
    private static EmisEnumSet merge(EmisEnumSet value1, EmisEnumSet value2)
    {
    	if (value1 == null)
    		return value2; 
    	
    	if (value2 == null)
    		return value1; 
    	
    	if (!NamedUtil.sameName(value1.getEnum(), value2.getEnum()))
    		throw new IllegalArgumentException("Mismatching EmisMetaEnum."); 
    	
    	EmisEnumSet result = new EnumSetImpl(); 
    	result.setEnum(value1.getEnum());
    	result.setAllIndexes(value1.getAllIndexes());
    	result.opAnd(value2);

    	return result; 
    }
    
    private static EmisContext mergeFilters(EmisContext context1, EmisContext context2, EmisMetaHierarchy hierarchy)
    {
    	if (context1 == null)
    		return context2; 
    	
    	if (context2 == null)
    		return context1; 
    	
    	EmisContext result = new Context();
    	for (EmisEnumSet filter : context1.getDateEnumFilters().values())
    		result.addDateEnumFilter(merge(filter, context2.getDateEnumFilter(filter.getEnum().getName())));
    		
		for (EmisEnumSet filter: context2.getDateEnumFilters().values())
		{
			if (result.getDateEnumFilter(filter.getEnum().getName()) == null)
				result.addDateEnumFilter(filter);
   		}
    		
		if (context1.getEnumFilters() != null)
			for (EmisEnumSet filter : context1.getEnumFilters().values())
				result.addEnumFilter(merge(filter, context2.getDateEnumFilter(filter.getEnum().getName())));
		
		if (context2.getEnumFilters() != null)
			for (EmisEnumSet filter : context2.getEnumFilters().values())
			{
				if (result.getEnumFilter(filter.getEnum().getName()) == null)
					result.addEnumFilter(filter); 
			}
		
		for (EmisMetaEntity entity : hierarchy.getEntityOrder())
		{
			for (String fieldName : context1.getEntityFilterNames(entity))
			{
				EmisMetaData field = NamedUtil.find(fieldName, entity.getData()); 
				Set<Byte> values = merge(context1.getEntityFilterValues(field), context2.getEntityFilterValues(field));  
				if (values != null)
					result.addEnumEntityFilter(field, new EnumSetImpl(field.getEnumType(), values)); 
			}
			
			for (String fieldName : context2.getEntityFilterNames(entity))
			{
				EmisMetaData field = NamedUtil.find(fieldName, entity.getData()); 
				if (result.getEntityFilterValues(field) == null)
				{
					byte[] values =  context2.getEntityFilterValues(field); 
					if (values != null)
						result.addEnumEntityFilter(field, new EnumSetImpl(field.getEnumType(), values)); 
				}
			}
		}
		
    	return result; 
    }
    
    private static PdfContent getNewContent(ReportMetaResult metaResult, PdfContentConfig contentConfig, EmisDataSet dataSet)
    {
        PdfContent result = null;

        if ((contentConfig instanceof PdfChartContentConfigImpl))
        {
            PdfChartContentConfigImpl chartContentConfig = (PdfChartContentConfigImpl) contentConfig;
            ItextPdfChartContent chartResult = new ItextPdfChartContent(chartContentConfig.getChartType());
            
            ChartConfig chartConfig = new ChartConfigImpl();

            chartConfig.setSeriesColours(metaResult.getColourScheme());
            chartConfig.setSeriesStrokes(metaResult.getStrokes());
            
            chartConfig.setLabelFont(LABEL_FONT); 
            ChartUtil.setMetaResultValueConfiguration(((TableMetaResult) chartContentConfig.getMetaResult()).getMetaResultValue(0), chartConfig);
            chartResult.setChartConfig(chartConfig);

            TableMetaResult tableMetaResult = (TableMetaResult) chartContentConfig.getMetaResult();
            adapt(metaResult, tableMetaResult);
            
            EmisContext oldGlobalFilter = tableMetaResult.getGlobalFilter(); 
            tableMetaResult.setGlobalFilter(mergeFilters(oldGlobalFilter, metaResult.getGlobalFilter(), metaResult.getHierarchy())); 
            try {
	            Result[] results = TableResultCollector.getMultiResult(dataSet, tableMetaResult);
	            chartResult.setResult(results[0]);
	            result = chartResult;
	            if (metaResult.getReportConfig().hasShortTitles())
	            	result.setTitle(MetaResultDimensionUtil.getSimpleTitle(tableMetaResult, false, null).toString()); 
	            else
	            	result.setTitle(MetaResultDimensionUtil.getTitle(tableMetaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NAMES, false));
            }
            finally 
            { tableMetaResult.setGlobalFilter(oldGlobalFilter); }
        }
        else if ((contentConfig instanceof PdfTableContentConfigImpl))
        {
            PdfTableContentConfigImpl tableContentConfig = (PdfTableContentConfigImpl) contentConfig;
            ItextPdfTableContent tableResult = new ItextPdfTableContent();

            TableMetaResult tableMetaResult = (TableMetaResult) tableContentConfig.getMetaResult();
            adapt(metaResult, tableMetaResult);
            
            EmisContext oldGlobalFilter = tableMetaResult.getGlobalFilter(); 
            tableMetaResult.setGlobalFilter(mergeFilters(oldGlobalFilter, metaResult.getGlobalFilter(), metaResult.getHierarchy())); 
            try { 
	            Result[] results = TableResultCollector.getMultiResult(dataSet, tableMetaResult);
	            tableResult.setResult(results[1]);
	
	            result = tableResult;
	            
	            if (metaResult.getReportConfig().hasShortTitles())
	            	result.setTitle(MetaResultDimensionUtil.getSimpleTitle(tableMetaResult, false, null).toString()); 
	            else
	            	result.setTitle(MetaResultDimensionUtil.getTitle(tableMetaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NONE, false));
            }
            finally 
            { tableMetaResult.setGlobalFilter(oldGlobalFilter); } 
        }
        else if ((contentConfig instanceof PdfTextContentConfigImpl))
            result = new PdfTextContent(contentConfig.getTitle(), ((PdfTextContentConfigImpl) contentConfig).getText());
        else if ((contentConfig instanceof PdfVariableContentConfigImpl))
        {
            PdfVariableContentConfig variableConfig = (PdfVariableContentConfig) contentConfig;  
            PdfVariableContent tmp = new PdfVariableContent(contentConfig.getTitle(), variableConfig.getSeniorEntity(), variableConfig.getTitles(), variableConfig.getVariables());  
            if (tmp.setContext(dataSet, metaResult.getContext()))
                result = tmp; 
        }
        else if ((contentConfig instanceof PdfGisContentConfigImpl))
        {
            try
            {
                PdfGisContentConfigImpl gisContentConfig = (PdfGisContentConfigImpl) contentConfig;
                String[] results = GisUtil.getGisResult((GisMetaResult) gisContentConfig.getMetaResult(), dataSet, null);

                PdfImageContent gisResult = new ItextPdfImageContent();
                gisResult.setImagePath(results[0]);

                result = gisResult;
                if (metaResult.getReportConfig().hasShortTitles())
                	result.setTitle(MetaResultDimensionUtil.getSimpleTitle(gisContentConfig.getMetaResult(), false, null).toString()); 
                else
                	result.setTitle(MetaResultDimensionUtil.getTitle(gisContentConfig.getMetaResult(), MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NONE, false));
            }
            catch (IOException ex)
            {
                return null;
            }
        }

        return result;
    }

    private static boolean adapt(ReportMetaResult reportMetaResult, TableMetaResult tableMetaResult)
    {
        adapt(reportMetaResult, tableMetaResult.getContext());
        for (int i = 0; i < tableMetaResult.getDimensionCount(); i++)
        {
            if (!adapt(reportMetaResult, tableMetaResult.getDimension(i)))
                return false;
        }
        return true;
    }

    private static boolean adapt(ReportMetaResult reportMetaResult, MetaResultDimension dim)
    {
        if ((dim instanceof MetaResultDimensionDate))
        {
            MetaResultDimensionDate dateDim = (MetaResultDimensionDate) dim;
            
            List<EmisEnumTupleValue> newDates = reportMetaResult.getContext().getDates();
            if ((newDates == null) || (newDates.size() == 0))
            {
                return false;
            }
            dateDim.setDateEnum(((EmisEnumTupleValue) newDates.get(0)).get(dateDim.getDateEnumType().getParent()));
        }

        if ((dim instanceof MetaResultDimensionEntity))
        {
            MetaResultDimensionEntity entityDim = (MetaResultDimensionEntity) dim;

            int[] ids = entityDim.getEntityPath();
            String[] names = entityDim.getEntityPathNames();

            int[] newIds = reportMetaResult.getEntityPathIds();
            String[] newNames = reportMetaResult.getEntityPathNames();

            if (ids.length > newIds.length)
            {
                return false;
            }
            for (int i = 0; i < ids.length; i++)
            {
                ids[i] = newIds[i];
                names[i] = newNames[i];
            }
            entityDim.setPath(ids, names, entityDim.getHierarchyDateIndex());
        }

        return true;
    }

    private static void adapt(ReportMetaResult reportMetaResult, EmisContext context)
    {
        EmisContext newContext = reportMetaResult.getContext();
        if (context.getDateType() != null)
        {
        	if (context.getDateType().getDimensions() > newContext.getDateType().getDimensions())
        	{
        		context.setDateType(newContext.getDateType()); 
        		context.setDates(newContext.getDates()); 
        	
        	}
        	else
        		context.setDates(newContext.getDates(context.getDateType()));
        }
        
        if ((context.getEntities() == null) || (context.getEntities().size() == 0))
        {
            return;
        }

        EmisEntity newEntity = getEntity(reportMetaResult, ((EmisEntity) context.getEntities().get(0)).getEntityType());
        if (newEntity == null)
        {
            return;
        }
        List<EmisEntity> entities = new ArrayList<EmisEntity>();
        entities.add(newEntity);
        context.setEntities(entities);
    }

    private static EmisEntity getEntity(ReportMetaResult reportMetaResult, EmisMetaEntity entityType)
    {
        if (reportMetaResult.getHierarchy() == null)
        {
            return null;
        }
        int index = NamedUtil.findIndex(entityType, reportMetaResult.getHierarchy().getEntityOrder());
        if (index == -1)
        {
            return null;
        }
        int[] ids = reportMetaResult.getEntityPathIds();
        String[] names = reportMetaResult.getEntityPathNames();

        if (index >= ids.length)
        {
            return null;
        }
        EmisEntity result = new Entity();
        result.setId(ids[index]);
        result.setName(names[index]);
        result.setEntityType(entityType);

        return result;
    }
}
