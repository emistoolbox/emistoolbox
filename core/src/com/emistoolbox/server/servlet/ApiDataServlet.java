package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.TableMetaResultImpl;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.results.TableResultCollector;

public class ApiDataServlet extends ApiBaseServlet
{
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        if (!hasAccess(req, resp))
            return; 

        if (!hasParameter(req, resp, QS_REPORT))
            return;

        String dataset = req.getParameter(QS_DATASET); 
        EmisDataSet emis = EmisToolboxIO.loadDataset(dataset); 
        if (emis == null)
        {
            error(resp, 400, "No EMIS data loaded.", asJson(req)); 
            return; 
        }

        List<String> hierarchyList = NamedUtil.getNames(emis.getMetaDataSet().getHierarchies()); 
        List<String> indicatorList = NamedUtil.getNames(getIndicators(emis.getMetaDataSet())); 
        List<String> xaxises = NamedUtil.getNames(getDimensions(req, null, dataset, emis)); 
        List<String> splitBys = NamedUtil.getNames(getDimensions(req, QS_XAXIS, dataset, emis)); 

        if (!hasParameters(req, resp, hierarchyList, indicatorList, xaxises, splitBys))
            return; 

        // TODO - validate location
        // TODO - validate date
        
        TableMetaResult metaResult = getTableMetaResult(req, resp, dataset, emis); 
        if (metaResult == null)
            return; 
        
        TableResultCollector collector = new TableResultCollector(emis, metaResult); 
        success(req, resp, collector.getResult(), metaResult);
    }
    
    private void setEntity(HttpServletRequest req, HttpServletResponse resp, EmisDataSet emis, EmisContext context, List<EmisMetaEntity> entityOrder, int[] entityIds, MetaResultDimensionEntity dim)
    {
        dim.setEntityType(context.getEntityType());
        dim.setPath(entityIds, getEntityPathNames(emis, entityIds, entityOrder, dim.getHierarchyDateIndex()), dim.getHierarchyDateIndex());
    }
        
    private TableMetaResult getTableMetaResult(HttpServletRequest req, HttpServletResponse resp, String dataset, EmisDataSet emis)
        throws IOException
    {
        EmisMeta meta = emis.getMetaDataSet(); 
        
        TableMetaResultImpl metaResult = new TableMetaResultImpl();
        setMetaResult(metaResult, req, resp, emis); 

        EmisMetaHierarchy metaHierarchy = metaResult.getHierarchy();
        int[] pathIds = getEntityPathIds(emis, metaHierarchy, req.getParameter(QS_LOCATION), 0); 
        MetaResultDimension dim = getMetaResultDimension(req, QS_XAXIS, metaHierarchy, metaResult.getIndicator(), meta); 
        if (dim instanceof MetaResultDimensionEntity)
            setEntity(req, resp, emis, metaResult.getContext(), metaHierarchy.getEntityOrder(), pathIds, (MetaResultDimensionEntity) dim); 
        
        MetaResultDimension dim2 = getMetaResultDimension(req, QS_SPLITBY, metaResult.getHierarchy(), metaResult.getIndicator(), meta); 
        if (dim2 instanceof MetaResultDimensionEntity)
            setEntity(req, resp, emis, metaResult.getContext(), metaHierarchy.getEntityOrder(), pathIds, (MetaResultDimensionEntity) dim2); 

        adjustContext((Context) metaResult.getContext(), contextNeedsEntity(dim, dim2), contextNeedsDate(dim, dim2)); 

        if (dim == null)
            return null; 
        
        if (dim2 == null)
        {
            metaResult.setDimensionCount(1); 
            metaResult.setDimension(0, dim); 
        }
        else
        {
            metaResult.setDimensionCount(2); 
            metaResult.setDimension(0, dim); 
            metaResult.setDimension(1, dim2); 
        }

        return metaResult; 
        //        metaResult.setSortOrder(order); 
    }
    
    private boolean contextNeedsEntity(MetaResultDimension dim1, MetaResultDimension dim2)
    { return (!(dim1 instanceof MetaResultDimensionEntity) && !(dim2 instanceof MetaResultDimensionEntity)); }

    private boolean contextNeedsDate(MetaResultDimension dim1, MetaResultDimension dim2)
    { return (!(dim1 instanceof MetaResultDimensionDate) && !(dim2 instanceof MetaResultDimensionDate)); }
}
