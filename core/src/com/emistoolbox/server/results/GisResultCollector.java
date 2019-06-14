package com.emistoolbox.server.results;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstEntity;
import com.emistoolbox.common.model.analysis.impl.MultipleContext;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity.EmisGisType;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.EmisGisEntityDataSet;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;
import com.emistoolbox.server.renderer.gis.impl.GisFeatureSetImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GisResultCollector extends ResultCollector
{
    public GisResultCollector(EmisDataSet emisDataSet, GisMetaResult metaResult) {
        super(emisDataSet, metaResult);

        EmisMetaEntity entityType = metaResult.getContext().getEntityType();
        if (entityType == null)
            throw new IllegalArgumentException("GisMetaResult requires entity type on which we plot.");

        if (entityType.getGisType() == EmisMetaEntity.EmisGisType.NONE)
            throw new IllegalArgumentException("Cannot plot entity of type " + entityType.getName());
    }

    private EmisMetaEntity getTopEntityType(NamedIndexList<EmisMetaEntity> entityOrder, EmisContext context)
    {
        if ((context.getEntities() == null) || (context.getEntities().size() == 0) || (((EmisEntity) context.getEntities().get(0)).getEntityType() == null))
            return (EmisMetaEntity) entityOrder.get(0);

        return ((EmisEntity) context.getEntities().get(0)).getEntityType();
    }

    public List<GisFeatureSet> getResults()
    {
        List result = new ArrayList();

        EmisContext context = getMetaResult().getContextWithGlobalFilter();

        NamedIndexList entityOrder = getHierarchy().getMetaHierarchy().getEntityOrder();
        int start = entityOrder.getIndex(getTopEntityType(entityOrder, context));
        int end = entityOrder.getIndex(context.getEntityType());

        for (int i = start; i <= end; i++)
        {
            EmisMetaEntity tmpEntityType = (EmisMetaEntity) entityOrder.get(i);
            if (tmpEntityType.getGisType() == EmisMetaEntity.EmisGisType.NONE)
                continue;

            int tmpEntityTypeIndex = NamedUtil.findIndex(tmpEntityType, getDataSet().getMetaDataSet().getEntities());

            GisFeatureSet featureSet = null;
            if (i == end)
                featureSet = new GisFeatureSetImpl(tmpEntityType, getMetaResult().getIndicator());
            else
                featureSet = new GisFeatureSetImpl(tmpEntityType);
            
            for (EmisEntity entity : context.getEntities())
            {
                List children = getHierarchy().getDescendants(context.getHierarchyDateIndex(), entity.getEntityType(), entity.getId(), tmpEntityType);
                children = filter(context, tmpEntityType, children);

                addFeatures(featureSet, getDataSet().getGisEntityDataSet(tmpEntityType), tmpEntityType, tmpEntityTypeIndex, context.getHierarchyDateIndex(), children, i == end);
            }

            result.add(featureSet);
        }

        return result;
    }

    private void addFeatures(GisFeatureSet featureSet, EmisGisEntityDataSet gisDataSet, EmisMetaEntity entityType, int entityTypeIndex, int dateIndex, List<int[]> entityIds, boolean withResultValue)
    {
        for (int[] entityIdArray : entityIds)
        {
            EmisEntityDataSet dataset = getDataSet().getEntityDataSet(entityTypeIndex, getDataSet().getMetaDataSet().getDefaultDateTypeIndex());
            Map names = dataset.getAllValues(dateIndex, "name", entityIdArray);

            for (int entityId : entityIdArray)
            {
                String title = (String) names.get(Integer.valueOf(entityId));
                if (title == null)
                    title = "";

                if (withResultValue)
                {
                    double[] results = getResultValues(getContext(new Entity(entityType, entityId)));
                    if ((results != null) && (results.length > 0))
                        featureSet.add(entityId, gisDataSet.getGisData(entityId), results[0], title);
                    else
                        featureSet.add(entityId, gisDataSet.getGisData(entityId), 0.0D, title);
                }
                else
                    featureSet.add(entityId, gisDataSet.getGisData(entityId), 0.0D, title);
            }
        }
    }

    private EmisContext getContext(EmisEntity entity)
    {
        EmisContext metaResultContext = getMetaResult().getContextWithGlobalFilter();
        return new MultipleContext(new EmisContext[] { new ContextConstEntity(entity, metaResultContext.getHierarchyDateIndex()), metaResultContext }, metaResultContext.getDateType());
    }
}
