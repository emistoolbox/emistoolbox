package com.emistoolbox.server.renderer.pdfreport.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.PriorityMetaResult;
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
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPriorityListContent;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportCreator;
import com.emistoolbox.server.results.PriorityResultCollector;
import com.emistoolbox.server.results.ResultCollector;
import com.emistoolbox.server.results.TableResultCollector;

import es.jbauer.lib.io.impl.IOFileInput;

public abstract class BasePdfReportCreator<T extends EmisPdfReportConfig> implements PdfReportCreator<T> 
{
    public static final ChartFont LABEL_FONT = new ChartFont("SansSerif", 1, 6);

	protected T config; 
	protected ReportMetaResult metaResult; 
	protected EmisDataSet dataSet; 
	
	protected PdfReport reportResult; 
	

	protected abstract int getPageCountPerEntity(); 
	
	public synchronized PdfReport create(ReportMetaResult metaResult, EmisDataSet dataSet) 
	{
    	this.metaResult = metaResult; 
    	this.dataSet = dataSet; 
    	this.config = (T) metaResult.getReportConfig(); 
    	
    	this.reportResult = new PdfReportImpl();

    	EmisContext context = metaResult.getContextWithGlobalFilter();

        int indexEntityType = NamedUtil.findIndex(metaResult.getReportConfig().getEntityType(), metaResult.getHierarchy().getEntityOrder());
        if (indexEntityType == -1)
            return reportResult;

        // Remember current context values.
        EmisMetaEntity oldEntityType = context.getEntityType(); 
        List<EmisEntity> oldEntities = context.getEntities(); 
        int[] oldIds = metaResult.getEntityPathIds(); 
        String[] oldNames = metaResult.getEntityPathNames(); 
        
        addAllPages(metaResult.getEntityPathIds(), metaResult.getEntityPathNames(), indexEntityType, getTotalPageCount(context));

        metaResult.getContext().setEntityType(oldEntityType); 
        metaResult.getContext().setEntities(oldEntities); 
        metaResult.setEntityPath(oldIds, oldNames); 
        
        // Reset context values. 
        return reportResult;
	}

	protected abstract void addEntityPages(EmisEntity entity, int[] ids, String[] names, int totalPages); 
	
	protected void init(EmisEntity entity, int[] ids, String[] names)
	{
		// Prepare ReortMetaResult. 
        List<EmisEntity> result = new ArrayList<EmisEntity>();
        result.add(entity);

        EmisContext context = metaResult.getContext(); 
        context.setEntityType(entity.getEntityType());
        context.setEntities(result);
        metaResult.setEntityPath(ids, names);
	}
	
    private void addAllPages(int[] ids, String[] names, int indexEntityType, int totalPages)
    {
        if (indexEntityType + 1 == ids.length)
        {
            EmisMetaEntity entityType = metaResult.getHierarchy().getEntityOrder().get(indexEntityType);
            
            EmisEntity entity = new Entity(entityType, ids[ids.length - 1]); 
            entity.setName(names[names.length - 1]); 
            
            addEntityPages(entity, ids, names, totalPages); 
            return; 
        }

        int[] newIds = new int[ids.length + 1];
        for (int i = 0; i < ids.length; i++)
            newIds[i] = ids[i];

        String[] newNames = new String[names.length + 1];
        for (int i = 0; i < names.length; i++)
            newNames[i] = names[i];

        EmisHierarchy hierarchy = dataSet.getHierarchy(config.getHierarchy().getName());
        int dateTypeIndex = NamedUtil.findIndex((EmisMetaDateEnum) hierarchy.getDateType(), dataSet.getMetaDataSet().getDateEnums());
        int[] childIds = hierarchy.getChildren(dateTypeIndex, hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length - 1), ids[ids.length - 1]);
        childIds = ResultCollector.filter(metaResult.getContextWithGlobalFilter(), hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length), childIds, dataSet); 

        if (childIds == null || childIds.length == 0)
            return;

        EmisEntityDataSet entityDataSet = dataSet.getEntityDataSet(hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length), (EmisMetaDateEnum) hierarchy.getDateType());
        Map<Integer, String> childNames = entityDataSet.getAllValues(getDateIndex(metaResult.getContext()), "name", childIds);
        for (int i = 0; i < childIds.length; i++)
        {
        	if (childIds[i] == -1)
        		continue; 
        	
            newIds[newIds.length - 1] = childIds[i];
            newNames[newNames.length - 1] = childNames.get(childIds[i]);

            addAllPages(newIds, newNames, indexEntityType, totalPages);
        }
    }
    
    protected int getDateIndex(EmisContext context)
    {
        List<EmisEnumTupleValue> dates = context.getDates();
        if (dates == null || dates.size() == 0)
            return 0;

        byte[] indexes = dates.get(0).getIndex();
        return indexes[indexes.length - 1];
    }

    protected int getEntityCount(EmisContext context, EmisMetaEntity childEntityType, int dateIndex, EmisHierarchy hierarchy, EmisDataSet dataSet)
    {
        List<EmisEntity> entities = context.getEntities(); // global-changed: metaResult.getContext().getEntities();
        if (entities.size() == 0)
        	return 0; 
        
        EmisEntity parent = entities.get(0);

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

    protected boolean adapt(TableMetaResult tableMetaResult)
    {
        adapt(tableMetaResult.getContext());
        for (int i = 0; i < tableMetaResult.getDimensionCount(); i++)
        {
            if (!adapt(tableMetaResult.getDimension(i)))
                return false;
        }
        
        return true;
    }

    protected boolean adapt(MetaResultDimension dim)
    {
        if ((dim instanceof MetaResultDimensionDate))
        {
            MetaResultDimensionDate dateDim = (MetaResultDimensionDate) dim;
            
            List<EmisEnumTupleValue> newDates = metaResult.getContext().getDates();
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

            int[] newIds = metaResult.getEntityPathIds();
            String[] newNames = metaResult.getEntityPathNames();

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

    protected void adapt(EmisContext context)
    {
        EmisContext newContext = metaResult.getContext();
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

        EmisEntity newEntity = getEntity(((EmisEntity) context.getEntities().get(0)).getEntityType());
        if (newEntity == null)
        {
            return;
        }
        List<EmisEntity> entities = new ArrayList<EmisEntity>();
        entities.add(newEntity);
        context.setEntities(entities);
    }

    protected EmisEntity getEntity(EmisMetaEntity entityType)
    {
        if (metaResult.getHierarchy() == null)
        {
            return null;
        }
        int index = NamedUtil.findIndex(entityType, metaResult.getHierarchy().getEntityOrder());
        if (index == -1)
        {
            return null;
        }
        int[] ids = metaResult.getEntityPathIds();
        String[] names = metaResult.getEntityPathNames();

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
    
    protected EmisContext mergeFilters(EmisContext context1, EmisContext context2, EmisMetaHierarchy hierarchy)
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

    private EmisEnumSet merge(EmisEnumSet value1, EmisEnumSet value2)
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

	protected Set<Byte> merge(byte[] value1, byte[] value2)
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

	protected int getTotalPageCount(EmisContext context)
	{
	    // Need to multiple totalPages by number of EmisEntities
	    int totalPages = getPageCountPerEntity(); 
	    totalPages *= getEntityCount(context, metaResult.getReportConfig().getEntityType(), getDateIndex(context), dataSet.getHierarchy(metaResult.getHierarchy().getName()), dataSet); 

	    return totalPages; 
	}
	
    protected PdfContent createContent(PdfContentConfig contentConfig)
    {
        PdfContent result = null;

	    if ((contentConfig instanceof PdfChartContentConfig))
	    {
	        PdfChartContentConfig chartContentConfig = (PdfChartContentConfig) contentConfig;

	        PdfChartContent chartResult = new PdfChartContentImpl(chartContentConfig.getChartType());
	        chartResult.setConfig(chartContentConfig);
	        
	        ChartConfig chartConfig = new ChartConfigImpl();
	
	        chartConfig.setSeriesColours(metaResult.getColourScheme());
	        chartConfig.setSeriesStrokes(metaResult.getStrokes());
	        
	        chartConfig.setLabelFont(LABEL_FONT); 
	        ChartUtil.setMetaResultValueConfiguration(((TableMetaResult) chartContentConfig.getMetaResult()).getMetaResultValue(0), chartConfig);
	        chartResult.setChartConfig(chartConfig);
	
	        TableMetaResult tableMetaResult = (TableMetaResult) chartContentConfig.getMetaResult();
	        adapt(tableMetaResult);
	        
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
	    else if (contentConfig instanceof PdfTextContentConfig)
	    {
	    	PdfTextContentConfig txtConfig = (PdfTextContentConfig) contentConfig; 
	    	PdfTextContent txtContent = new PdfTextContent(contentConfig.getTitle(), ((PdfTextContentConfig) contentConfig).getText());
	    	txtContent.setConfig(txtConfig);
	    	txtContent.setTitleFont(PdfText.DEFAULT_TITLE_FONT_HACK);
	    	txtContent.setTextFont(PdfText.DEFAULT_FONT_HACK);
	    	
	    	result = txtContent; 
	    }
	    else if ((contentConfig instanceof PdfVariableContentConfigImpl))
        {
            PdfVariableContentConfig variableConfig = (PdfVariableContentConfig) contentConfig;
            PdfVariableContent tmp = new PdfVariableContent(contentConfig.getTitle(), variableConfig.getSeniorEntity(), variableConfig.getTitles(), variableConfig.getVariables());  
            if (tmp.setContext(dataSet, metaResult.getContext()))
                result = tmp; 
        }
        else if ((contentConfig instanceof PdfGisContentConfig))
        {
            try
            {
                PdfGisContentConfig gisContentConfig = (PdfGisContentConfig) contentConfig;
                String[] results = GisUtil.getGisResult((GisMetaResult) gisContentConfig.getMetaResult(), dataSet, null);
                result = new PdfImageContentImpl(new IOFileInput(new File(results[0])));
                if (metaResult.getReportConfig().hasShortTitles())
                	result.setTitle(MetaResultDimensionUtil.getSimpleTitle(gisContentConfig.getMetaResult(), false, null).toString()); 
                else
                	result.setTitle(MetaResultDimensionUtil.getTitle(gisContentConfig.getMetaResult(), MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NONE, false));
            }
            catch (IOException ex)
            { return null; }
        }
        else if (contentConfig instanceof PdfPriorityListContentConfig)
        {
        	PdfPriorityListContentConfig prioConfig = (PdfPriorityListContentConfig) contentConfig; 
        	
        	PdfPriorityListContent prioContent =new PdfPriorityListContentImpl(); 
        	prioContent.setConfig(prioConfig); 
        	
        	PriorityMetaResult prioMetaResult = prioConfig.getMetaResult(); 
        	adapt(prioMetaResult.getContext());
        	
        	PriorityResultCollector collector = new PriorityResultCollector(dataSet, prioMetaResult);
        	prioContent.setResults(collector.getResults());
        	        	
        	result = prioContent; 
        }

        return result;
    }
}
