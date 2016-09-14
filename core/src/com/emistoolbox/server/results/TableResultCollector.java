package com.emistoolbox.server.results;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.impl.MultipleContext;
import com.emistoolbox.common.model.impl.EmisEnumUtils;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.ExpandedTableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityAncestors;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityGrandChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.common.results.impl.ResultImpl;
import com.emistoolbox.common.results.impl.ResultSorted;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.results.impl.ResultDimensionDateEnum;
import com.emistoolbox.server.results.impl.ResultDimensionEntity;
import com.emistoolbox.server.results.impl.ResultDimensionEntityFilter;
import com.emistoolbox.server.results.impl.ResultDimensionEnum;
import com.emistoolbox.server.results.impl.ResultDimensionMetaResultValues;
import com.emistoolbox.server.util.IntIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableResultCollector extends ResultCollector
{
    private TableMetaResult metaResult;

    public TableResultCollector(EmisDataSet emisDataSet, TableMetaResult metaResult) {
        super(emisDataSet, metaResult);
        this.metaResult = metaResult;
    }

    public Result getResult()
    {
        List<MetaResultValue> metaValues = this.metaResult.getMetaResultValues();

        int dimensionCount = this.metaResult.getDimensionCount();
        int metaValueDimension = metaValues.size() > 1 ? 1 : 0;

        int[] sizes = new int[dimensionCount + metaValueDimension];
        ResultDimension[] dimensions = new ResultDimension[sizes.length];
        for (int i = 0; i < sizes.length; i++)
        {
            if (i == dimensionCount)
                dimensions[i] = new ResultDimensionMetaResultValues(metaValues);
            else
                dimensions[i] = getResultDimension(metaResult.getDimension(i), metaResult.getGlobalFilter());
            sizes[i] = dimensions[i].getItemCount();
        }

        Result result = new ResultImpl(sizes);

        result.setDefaultFormat(((MetaResultValue) metaValues.get(0)).getFormat());
        for (int i = 1; i < metaValues.size(); i++)
            result.setFormat(i, ((MetaResultValue) metaValues.get(i)).getFormat());

        for (int i = 0; i < sizes.length; i++)
        {
            for (int y = 0; y < sizes[i]; y++)
                result.setHeading(i, y, dimensions[i].getItemName(y));
        }

        EmisIndicator indicator = null; 
        if (metaValues.size() > 0)
        {
            indicator = metaValues.get(0).getIndicator(); 
        	result.setValueLabel(indicator.getName());
        }

        for (int[] indexes : new IntIndex(sizes))
        {
            if ((metaValueDimension == 1) && (indexes[dimensionCount] != 0))
                continue;

            int globalFilterCount = metaResult.getGlobalFilter() == null ? 0 : 1; 
            EmisContext[] contexts = new EmisContext[dimensionCount + 1 + globalFilterCount];
            contexts[contexts.length - 1] = metaResult.getContext();
            if (globalFilterCount > 0)
            	contexts[contexts.length - 2] = metaResult.getGlobalFilter(); 
            for (int i = 0; i < dimensionCount; i++)
                contexts[i] = dimensions[i].getContext(indexes[i]);

            double[] values = getResultValues(new MultipleContext(contexts, EmisEnumUtils.findLowestEnum(indicator.getUsedDateTypes())));
            for (int i = 0; i < values.length; i++)
            {
                if (metaValueDimension == 1)
                    indexes[dimensionCount] = i;
                result.set(indexes, values[i]);
            }
        }

        return result;
    }
    
    public static Result[] getMultiResult(EmisDataSet emisDataSet, TableMetaResult metaResult)
    {
        TableResultCollector collector = new TableResultCollector(emisDataSet, metaResult);
        TableResultCollector collectorExpanded = new TableResultCollector(emisDataSet, new ExpandedTableMetaResult(metaResult));

        if (metaResult.getSortOrder() == 0)
            return new Result[] { collector.getResult(), collectorExpanded.getResult() };
        if (metaResult.getSortOrder() == 2)
            return new Result[] { new ResultSorted(collector.getResult(), 0), new ResultSorted(collectorExpanded.getResult(), 0) };

        return new Result[] { new ResultSorted(collector.getResult(), 0, metaResult.getSortOrder() == 1), new ResultSorted(collectorExpanded.getResult(), 0, metaResult.getSortOrder() == 1) };
    }

    private ResultDimension getResultDimension(MetaResultDimension metaDimension, EmisContext globalFilter)
    {
        ResultDimension result = null;
        if ((metaDimension instanceof MetaResultDimensionDate))
            result = new ResultDimensionDateEnum((MetaResultDimensionDate) metaDimension, metaResult.getGlobalFilter());
        else if ((metaDimension instanceof MetaResultDimensionEntityAncestors))
        {
            MetaResultDimensionEntityAncestors ancestorDimension = (MetaResultDimensionEntityAncestors) metaDimension;
            int[] ids = ancestorDimension.getEntityPath();
            String[] names = ancestorDimension.getEntityPathNames();
            List entityOrder = this.metaResult.getHierarchy().getEntityOrder();

            int startIndex = Math.max(0, ids.length - MetaResultDimensionUtil.MAX_VERTICAL_DIMENSIONS); 
            EmisEntity[] entities = new EmisEntity[ids.length - startIndex];
            for (int i = 0; i < entities.length; i++)
            {
                entities[i] = new Entity((EmisMetaEntity) entityOrder.get(i + startIndex), ids[i + startIndex]);
                entities[i].setName(names[i + startIndex]);
            }

            result = new ResultDimensionEntity(entities, ancestorDimension.getHierarchyDateIndex());
        }
        else if (metaDimension instanceof MetaResultDimensionEntityGrandChildren)
        	result = getEntityDimension((MetaResultDimensionEntity) metaDimension, 2);  
        else if ((metaDimension instanceof MetaResultDimensionEntityChildren))
        	result = getEntityDimension((MetaResultDimensionEntity) metaDimension, 1);  
        else if ((metaDimension instanceof MetaResultDimensionEnum))
            result = new ResultDimensionEnum((MetaResultDimensionEnum) metaDimension, metaResult.getGlobalFilter());
        else if ((metaDimension instanceof MetaResultDimensionEntityFilter))
            result = new ResultDimensionEntityFilter((MetaResultDimensionEntityFilter) metaDimension);

        return result;
    }
    
    private ResultDimensionEntity getEntityDimension(MetaResultDimensionEntity entityDimension, int descendantLevel)
    {
    	int dateIndex = entityDimension.getHierarchyDateIndex(); 
    	EmisEntity[] entities = getDescendants(entityDimension, descendantLevel); 

    	if (entities.length > 0)
    	{
	    	EmisMetaEntity entityType = entities[0].getEntityType(); 
	        int entityTypeIndex = NamedUtil.findIndex(entityType, getDataSet().getMetaDataSet().getEntities());
	
	        EmisEntityDataSet dataset = getDataSet().getEntityDataSet(entityTypeIndex, getDataSet().getMetaDataSet().getDefaultDateTypeIndex());
	        Map<Integer, String> names = dataset.getAllValues(dateIndex, "name", getIds(entities));
	        for (int i = 0; i < entities.length; i++)
	        {
	            String name = names.get(entities[i].getId());
	            if (name == null)
	                name = entityType.getName() + " " + entities[i].getId();
	
	            entities[i].setName(name);
	        }
    	}
    	
        return new ResultDimensionEntity(entities, dateIndex); 
    }
    
    private int[] getIds(EmisEntity[] entities)
    {
    	int[] result = new int[entities.length]; 
    	for (int i = 0; i < result.length; i++)
    		result[i] = entities[i].getId(); 
    	
    	return result; 
    }
    
    private EmisEntity[] getDescendants(MetaResultDimensionEntity entityDimension, int descendantLevel)
    {
        int dateIndex = entityDimension.getHierarchyDateIndex(); 
        List<EmisMetaEntity> entityOrder = this.metaResult.getHierarchy().getEntityOrder();

        int entityTypeIndex = 0; 
        List<Integer> entityIds = new ArrayList<Integer>();
        
        int[] selectedEntityPath = entityDimension.getEntityPath();
        if (selectedEntityPath == null || selectedEntityPath.length == 0)
        {
        	for (int id : getHierarchy().getRootElements(dateIndex))
        		entityIds.add(id); 

        	descendantLevel--; 
        }
        else
        {
        	entityTypeIndex = selectedEntityPath.length - 1; 
        	entityIds.add(selectedEntityPath[entityTypeIndex]); 
        }

        while (descendantLevel > 0)
        {
        	entityIds = getChildrenIds(dateIndex, entityOrder.get(entityTypeIndex), entityIds); 
        	entityTypeIndex++; 
        	descendantLevel--; 
        }

        EmisEntity[] result = new EmisEntity[entityIds.size()]; 
        EmisMetaEntity entityType = entityOrder.get(entityTypeIndex); 
        for (int i = 0; i < entityIds.size(); i++) 
        	result[i] = new Entity(entityType, entityIds.get(i)); 
        
        return result; 
    }
    
    private List<Integer> getChildrenIds(int hierarchyIndex, EmisMetaEntity entityType, List<Integer> parentIds)
    {
    	List<Integer> result = new ArrayList<Integer>();
    	for (int parentId : parentIds)
    	{
        	int[] ids = getHierarchy().getChildren(hierarchyIndex, entityType, parentId); 
        	if (ids != null)
        	{
        		for (int id : ids)
            		result.add(id); 
        	}
    	}
    	
    	return result; 
    }
}
