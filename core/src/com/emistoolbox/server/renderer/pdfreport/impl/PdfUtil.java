package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.client.admin.ReportModule;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
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
import com.emistoolbox.server.results.TableResultCollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        // Need to multiple totalPages by number of EmisEntities
        List<EmisEntity> entities = metaResult.getContext().getEntities();
        if (entities.size() == 0)
            totalPages = 0;
        else
            totalPages *= getEntityCount(entities.get(0), metaResult.getReportConfig().getEntityType(), getDateIndex(metaResult), dataSet.getHierarchy(metaResult.getHierarchy().getName()), dataSet); 

        int indexEntityType = NamedUtil.findIndex(metaResult.getReportConfig().getEntityType(), metaResult.getHierarchy().getEntityOrder());
        if (indexEntityType == -1)
            return report;

        // Remember current context values.
        EmisMetaEntity oldEntityType = metaResult.getContext().getEntityType(); 
        List<EmisEntity> oldEntities = metaResult.getContext().getEntities(); 
        int[] oldIds = metaResult.getEntityPathIds(); 
        String[] oldNames = metaResult.getEntityPathNames(); 
        
        addAllToPdfReport(report, metaResult.getEntityPathIds(), metaResult.getEntityPathNames(), indexEntityType, contents, 0, totalPages, metaResult, dataSet);

        metaResult.getContext().setEntityType(oldEntityType); 
        metaResult.getContext().setEntities(oldEntities); 
        metaResult.setEntityPath(oldIds, oldNames); 
        
        // Reset context values. 
        return report;
    }

    private static int getEntityCount(EmisEntity parent, EmisMetaEntity childEntityType, int dateIndex, EmisHierarchy hierarchy, EmisDataSet dataSet)
    {
        
        List<int[]> items = hierarchy.getDescendants(dateIndex, parent.getEntityType(), parent.getId(), childEntityType);
        if (items == null)
            return 0; 
        
        int result = 0; 
        for (int[] ints : items)
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

        if (childIds == null || childIds.length == 0)
            return 0;

        EmisEntityDataSet entityDataSet = dataSet.getEntityDataSet((EmisMetaEntity) hierarchy.getMetaHierarchy().getEntityOrder().get(ids.length), (EmisMetaDateEnum) hierarchy.getDateType());
        Map<Integer, String> childNames = entityDataSet.getAllValues(getDateIndex(metaResult), "name", childIds);
        for (int i = 0; i < childIds.length; i++)
        {
            newIds[newIds.length - 1] = childIds[i];
            newNames[newNames.length - 1] = childNames.get(childIds[i]);

            startPage += addAllToPdfReport(report, newIds, newNames, indexEntityType, contents, startPage, totalPages, metaResult, dataSet);
        }

        return startPage;
    }

    private static int getDateIndex(ReportMetaResult metaResult)
    {
        List<EmisEnumTupleValue> dates = metaResult.getContext().getDates();
        if (dates.size() == 0)
            return -1;

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
        {
            page.setFooter(config.getFooter() + " - " + footer);
        }
        return page;
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
            Result[] results = TableResultCollector.getMultiResult(dataSet, tableMetaResult);

            chartResult.setResult(results[0]);
            result = chartResult;
//            result.setTitle(MetaResultDimensionUtil.getTitle(tableMetaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NONE, false));
            result.setTitle(MetaResultDimensionUtil.getTitle(tableMetaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NAMES, false));
        }
        else if ((contentConfig instanceof PdfTableContentConfigImpl))
        {
            PdfTableContentConfigImpl tableContentConfig = (PdfTableContentConfigImpl) contentConfig;
            ItextPdfTableContent tableResult = new ItextPdfTableContent();

            TableMetaResult tableMetaResult = (TableMetaResult) tableContentConfig.getMetaResult();
            adapt(metaResult, tableMetaResult);
            Result[] results = TableResultCollector.getMultiResult(dataSet, tableMetaResult);
            tableResult.setResult(results[1]);

            result = tableResult;
            result.setTitle(MetaResultDimensionUtil.getTitle(tableMetaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NONE, false));
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
            
            List newDates = reportMetaResult.getContext().getDates();
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
        List entities = new ArrayList();
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

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.impl.PdfUtil
 * JD-Core Version: 0.6.0
 */