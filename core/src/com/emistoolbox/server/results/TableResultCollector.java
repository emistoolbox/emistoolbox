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
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityAncestors;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityChildren;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
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
                dimensions[i] = getResultDimension(this.metaResult.getDimension(i));
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

            EmisContext[] contexts = new EmisContext[dimensionCount + 1];
            contexts[dimensionCount] = this.metaResult.getContext();
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

    private ResultDimension getResultDimension(MetaResultDimension metaDimension)
    {
        ResultDimension result = null;
        if ((metaDimension instanceof MetaResultDimensionDate))
            result = new ResultDimensionDateEnum((MetaResultDimensionDate) metaDimension);
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
        else if ((metaDimension instanceof MetaResultDimensionEntityChildren))
        {
            MetaResultDimensionEntityChildren childrenDimension = (MetaResultDimensionEntityChildren) metaDimension;
            List entityOrder = this.metaResult.getHierarchy().getEntityOrder();

            int[] ancestorIds = childrenDimension.getEntityPath();
            if (ancestorIds == null)
                ancestorIds = new int[0]; 
            
            int[] ids = null;
            if (ancestorIds.length == 0)
                ids = getHierarchy().getRootElements(childrenDimension.getHierarchyDateIndex());
            else
                ids = getHierarchy().getChildren(childrenDimension.getHierarchyDateIndex(), (EmisMetaEntity) entityOrder.get(ancestorIds.length - 1), ancestorIds[(ancestorIds.length - 1)]);

            EmisMetaEntity entityType = (EmisMetaEntity) entityOrder.get(ancestorIds.length);
            EmisEntity[] entities = new EmisEntity[ids == null ? 0 : ids.length];

            int entityTypeIndex = NamedUtil.findIndex(entityType, getDataSet().getMetaDataSet().getEntities());
            EmisEntityDataSet dataset = getDataSet().getEntityDataSet(entityTypeIndex, getDataSet().getMetaDataSet().getDefaultDateTypeIndex());
            Map names = dataset.getAllValues(childrenDimension.getHierarchyDateIndex(), "name", ids);
            for (int i = 0; i < (ids == null ? 0 : ids.length); i++)
            {
                entities[i] = new Entity(entityType, ids[i]);
                String name = (String) names.get(Integer.valueOf(ids[i]));
                if (name == null)
                    name = entityType.getName() + " " + ids[i];
                entities[i].setName(name);
            }

            result = new ResultDimensionEntity(entities, childrenDimension.getHierarchyDateIndex());
        }
        else if ((metaDimension instanceof MetaResultDimensionEnum))
            result = new ResultDimensionEnum((MetaResultDimensionEnum) metaDimension);
        else if ((metaDimension instanceof MetaResultDimensionEntityFilter))
            result = new ResultDimensionEntityFilter((MetaResultDimensionEntityFilter) metaDimension);

        return result;
    }
}
