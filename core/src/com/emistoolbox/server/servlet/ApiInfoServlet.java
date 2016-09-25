package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.EmisToolboxServiceImpl;
import com.emistoolbox.server.model.EmisDataSet;

public class ApiInfoServlet extends ApiBaseServlet
{
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        if (!hasAccess(req, resp))
            return; 
        
        if (!hasParameter(req, resp, QS_TYPE))
            return;
        String type = req.getParameter(QS_TYPE);

        if (!hasParameter(req, resp, QS_DATASET))
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
        if (type.equals(TYPE_HIERARCHIES))
            success(req, resp, hierarchyList);  
        else if (type.equals(TYPE_INDICATORS))
            success(req, resp, indicatorList); 
        else if (type.equals(TYPE_XAXIS))
        {
            if (!hasParameters(req, resp, hierarchyList, indicatorList, null, null))
                return; 

            success(req, resp, NamedUtil.getNames(getDimensions(req, null, dataset, emis))); 
        }
        else if (type.equals(TYPE_SPLITBY))
        {
            if (!hasParameters(req, resp, hierarchyList, indicatorList, NamedUtil.getNames(getDimensions(req, null, dataset, emis)), null))
                return; 
            
            success(req, resp, NamedUtil.getNames(getDimensions(req, QS_XAXIS, dataset, emis))); 
        }
        else if (type.equals(TYPE_LOCATION))
        {
            List<String> xaxises = NamedUtil.getNames(getDimensions(req, null, dataset, emis)); 
            List<String> splitBys = NamedUtil.getNames(getDimensions(req, QS_XAXIS, dataset, emis)); 
            if (!hasParameters(req, resp, hierarchyList, indicatorList, xaxises, splitBys))
                return; 

            EmisMetaHierarchy hierarchy = getHierarchy(req.getParameter(QS_HIERARCHY), emis.getMetaDataSet()); 
            EmisIndicator indicator = getIndicator(req.getParameter(QS_INDICATOR), emis.getMetaDataSet()); 
            MetaResultDimension dim = getMetaResultDimension(req, QS_XAXIS, hierarchy, indicator, emis.getMetaDataSet()); 
            MetaResultDimension dim2 = getMetaResultDimension(req, QS_SPLITBY, hierarchy, indicator, emis.getMetaDataSet()); 
            
            EmisMetaEntity selectEntity = null; 
            boolean hasEntityDimension = false; 
            if (dim instanceof MetaResultDimensionEntity)
            {
                selectEntity = ((MetaResultDimensionEntity) dim).getEntityType();
                hasEntityDimension = true; 
            }
            
            if (dim2 instanceof MetaResultDimensionEntity)
            {
                selectEntity = ((MetaResultDimensionEntity) dim).getEntityType(); 
                hasEntityDimension = true; 
            }
           
            int targetEntity = selectEntity == null ? -1 : NamedUtil.findIndex(selectEntity, hierarchy.getEntityOrder());

            List<EmisEntity> entities = null; 
            if (!hasEntityDimension || selectEntity != null)
                entities = getLocations(req, hierarchy, emis);
            if (entities == null || entities.size() == 0)
                success(req, resp, new ArrayList<String>()); 
            else
            {
                int currentEntity = NamedUtil.findIndex(entities.get(0).getEntityType(), hierarchy.getEntityOrder()); 
                boolean canBeParent = (currentEntity + 1 < hierarchy.getEntityOrder().size()) && (targetEntity == -1 || currentEntity < targetEntity); 
                boolean canSelect = targetEntity == -1 || currentEntity == targetEntity; 
                
                if (canSelect || canBeParent)
                    success(req, resp, entities, canSelect, canBeParent); 
                else
                    success(req, resp, new ArrayList<String>()); 
            }
        }
        else if (type.equals(TYPE_DATE))
        {
            List<String> xaxises = NamedUtil.getNames(getDimensions(req, null, dataset, emis)); 
            List<String> splitBys = NamedUtil.getNames(getDimensions(req, QS_XAXIS, dataset, emis)); 
            if (!hasParameters(req, resp, hierarchyList, indicatorList, xaxises, splitBys))
                return; 

            EmisMetaHierarchy hierarchy = getHierarchy(req.getParameter(QS_HIERARCHY), emis.getMetaDataSet()); 
            EmisIndicator indicator = getIndicator(req.getParameter(QS_INDICATOR), emis.getMetaDataSet()); 
            MetaResultDimension dim = getMetaResultDimension(req, QS_XAXIS, hierarchy, indicator, emis.getMetaDataSet()); 
            MetaResultDimension dim2 = getMetaResultDimension(req, QS_SPLITBY, hierarchy, indicator, emis.getMetaDataSet()); 

            if (dim instanceof MetaResultDimensionDate || dim2 instanceof MetaResultDimensionDate)
                success(req, resp, new ArrayList<String>()); 
            else
                success(req, resp, getDates(req, emis)); 
        }
        else
            error(resp,  400, "Unknown type '" + req.getParameter(QS_TYPE) + "'", asJson(req)); 
    }
    
    private List<EmisEntity> getLocations(HttpServletRequest req, EmisMetaHierarchy hierarchy, EmisDataSet emis)
    {
        EmisMetaEntity parentEntity = null; 
        int parentId = -1; 

        List<EmisMetaEntity> entities = hierarchy.getEntityOrder(); 
        int index = NamedUtil.findIndex(req.getParameter(QS_PARENT_TYPE), entities);
        if (index != -1)
        {
            parentEntity = entities.get(index); 
            parentId = Integer.parseInt(req.getParameter(QS_PARENT_ID)); 
        }
        
        List<EmisEntity> result = new ArrayList<EmisEntity>(); 
        if (index + 1 >= entities.size())
            return result; 
        
        EmisMetaEntity entityType = entities.get(index + 1); 
        try { 
            int dateIndex = emis.getMetaDataSet().getDefaultDateTypeIndex(); 
            for (Map.Entry<Integer, String> entry : EmisToolboxServiceImpl.getHierarchyEntities(emis, hierarchy.getName(), dateIndex, parentEntity, parentId).entrySet())
            {
                EmisEntity entity = new Entity(entityType, entry.getKey()); 
                entity.setName(entry.getValue());
                result.add(entity); 
            }
        }
        catch (IOException ex)
        {}
        
        return result; 
    }
    
    private List<String> getDates(HttpServletRequest req, EmisDataSet emis)
    {
        List<String> result = new ArrayList<String>();
        NamedIndexList<EmisMetaDateEnum> dates = emis.getMetaDataSet().getDateEnums();
        for (String date : dates.get(0).getValues())
            result.add(date); 

        return result; 
    }
}
