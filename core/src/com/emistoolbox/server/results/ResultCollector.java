package com.emistoolbox.server.results;

import com.emistoolbox.common.FlatArrayUtil;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisAggregatorList;
import com.emistoolbox.common.model.analysis.EmisSampleAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisIndicatorTimeRatio;
import com.emistoolbox.common.model.analysis.EmisSampleCollector;
import com.emistoolbox.common.model.analysis.impl.ContextEntityOverride;
import com.emistoolbox.common.model.analysis.impl.ContextTimeOffset;
import com.emistoolbox.common.model.analysis.impl.IgnoreFilterContext;
import com.emistoolbox.common.model.analysis.impl.MultipleContext;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.ResultUtil;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultCollector
{
    private EmisDataSet emisDataSet;
    private EmisHierarchy hierarchy;
    private MetaResult metaResult;

    public ResultCollector(EmisDataSet emisDataSet, MetaResult metaResult) 
    {
        this.emisDataSet = emisDataSet;
        this.metaResult = metaResult;
        this.hierarchy = emisDataSet.getHierarchy(metaResult.getHierarchy().getName());
    }

    public EmisDataSet getDataSet()
    {
        return this.emisDataSet;
    }

    public EmisHierarchy getHierarchy()
    {
        return this.hierarchy;
    }

    public MetaResult getMetaResult()
    {
        return this.metaResult;
    }

    public List<int[]> filter(EmisContext context, EmisMetaEntity metaEntity, List<int[]> idsList)
    {
        idsList = copyIds(idsList);
        for (String filter : context.getEntityFilterNames(metaEntity))
        {
            int entityTypeIndex = NamedUtil.findIndex(metaEntity, getDataSet().getMetaDataSet().getEntities());
            EmisMetaData field = (EmisMetaData) NamedUtil.find(filter, metaEntity.getData());
            if (field == null)
                continue;

            int dateIndex = getDateIndex(context, field);
            if (dateIndex == -1)
                continue;

            EmisMetaDateEnum dateEnum = field.getDateType();
            int dateTypeIndex = NamedUtil.findIndex(dateEnum, getDataSet().getMetaDataSet().getDateEnums());

            EmisEntityDataSet dataset = getDataSet().getEntityDataSet(entityTypeIndex, dateTypeIndex);
            if (dataset == null)
                continue;

            for (int[] ids : idsList)
                filter(context, field, dataset, dateIndex, ids);
        }

        return idsList;
    }

    public void filter(EmisContext context, EmisMetaData field, EmisEntityDataSet dataset, int dateIndex, int[] ids)
    {
        EntityDataAccess access = dataset.getDataAccess(field.getName());
        if (access == null)
        {
            return;
        }
        for (int i = 0; i < ids.length; i++)
        {
            if (ids[i] == -1)
            {
                continue;
            }
            EmisEntityData data = dataset.getData(dateIndex, ids[i]);
            if (data == null)
            {
                continue;
            }
            int value = access.getAsInt(data.getMasterArray(), 0);
            if (!context.allowEntityWithValue(field, value))
                ids[i] = -1;
        }
    }

    private List<int[]> copyIds(List<int[]> idsList)
    {
        List<int[]> result = new ArrayList<int[]>();
        for (int[] ids : idsList)
            result.add(ids.clone());
        return result;
    }

    private int getDateIndex(EmisContext context, EmisMetaData field)
    {
        Collection<EmisEnumTupleValue> values = context.getDates(field.getDateType());
        if (values == null)
        {
            return -1;
        }
        Iterator<EmisEnumTupleValue> i$ = values.iterator();
        if (i$.hasNext())
        {
            EmisEnumTupleValue value = i$.next();
            return value.getEnumTuple().getIndex(value.getIndex());
        }
        return -1;
    }

    protected double[] getResultValues(EmisContext context)
    {
        List<MetaResultValue> metaValues = metaResult.getMetaResultValues();
        double[] result = new double[metaValues.size()];
        boolean[] skipResult = new boolean[metaValues.size()];

        int index = 0;
        for (MetaResultValue value : metaValues)
        {
            if (skipResult[index])
            {
                index++;
                continue;
            }
            
            EmisIndicator indicator = value.getIndicator(); 
            Integer denominatorTimeOffset = (indicator instanceof EmisIndicatorTimeRatio) ? ((EmisIndicatorTimeRatio) indicator).getTimeOffset() : null;
            Map<String, Double> values = getResultValues(context, indicator, getUsedAggregatorKeys(indicator, metaValues), denominatorTimeOffset);

            // Set results for all metaResults that use the current indicator. 
            //
            for (int i = index; i < metaValues.size(); i++)
            {
                if (skipResult[i])
                    continue;

                MetaResultValue tmpValue = (MetaResultValue) metaValues.get(i);
                if (!NamedUtil.sameName(indicator, tmpValue.getIndicator()))
                    continue;

                if (tmpValue.getAggregatorKey() == null)
                    result[i] = IndicatorUtil.calculate(tmpValue.getIndicator(), values);
                else if (tmpValue.getAggregatorKey().startsWith("?"))
                    result[i] = IndicatorUtil.calculateRequired(indicator, values, tmpValue.getAggregatorKey().substring(1), tmpValue.getTarget());
                else if (tmpValue.getAggregatorKey().startsWith("+"))
                    result[i] = IndicatorUtil.calculateSurplus(indicator, values, tmpValue.getAggregatorKey().substring(1), tmpValue.getTarget());
                else
                    result[i] = ((Double) values.get(tmpValue.getAggregatorKey())).doubleValue();

                skipResult[i] = true;
            }

            index++;
        }

        return result;
    }
    
    private Set<String> getUsedAggregatorKeys(EmisIndicator indicator, List<MetaResultValue> metaResults)
    {
    	Set<String> result = new HashSet<String>(); 
    	
    	for (MetaResultValue metaResult : metaResults)
    	{
    		if (!NamedUtil.sameName(indicator, metaResult.getIndicator()))
    			continue; 
    		
    		if (metaResult.getAggregatorKey() != null)
    			result.add(metaResult.getAggregatorKey()); 
    		else
    		{
    			for (String key : indicator.getAggregatorNames())
    				result.add(key);
    		}
    	}
    	
    	return result; 
    }

    private Map<String, Double> getResultValues(EmisContext context, EmisAggregatorList aggrList, Set<String> aggrKeys, Integer denominatorTimeOffset)
    {
        Map<String, Double> values = new HashMap<String, Double>();
        for (String aggrKey : aggrKeys)
        {
            EmisAggregatorDef aggr = aggrList.getAggregator(aggrKey); 
            int timeOffset = 0;
            if (denominatorTimeOffset != null && aggrKey.equals("denominator"))
                timeOffset = denominatorTimeOffset; 
            values.put(aggrKey, Double.valueOf(getAggregatorValue(context, aggr, timeOffset)));
        }

        return values;
    }

    private double getAggregatorValue(EmisContext context, EmisAggregatorDef aggr, int timeOffset)
    {
    	if (!(aggr instanceof EmisSampleAggregatorDef))
    		return getPlainAggregatorValue(context, aggr, timeOffset); 

    	// For commpound aggregator we need to loop through all entities, find values 
    	// and then add those.  
    	EmisSampleAggregatorDef compoundAggr = (EmisSampleAggregatorDef) aggr; 
    	EmisMetaEntity compoundEntityType = compoundAggr.getSampleEntityType(hierarchy.getMetaHierarchy());

    	EmisSampleCollector result = compoundAggr.getSampleCollector(); 
    	EmisContext localContext = getLocalContext(context, compoundAggr, timeOffset); 
        for (EmisEntity entity : localContext.getEntities())
        {
            List<int[]> children = getHierarchy().getDescendants(localContext.getHierarchyDateIndex(), entity.getEntityType(), entity.getId(), compoundEntityType);
            children = filter(localContext, compoundEntityType, children);
            for (int[] ids : children)
            {
            	for (int i = 0; i < ids.length; i++)
            	{
            		Set<String> keys = new HashSet<String>(); 
            		keys.addAll(Arrays.asList(compoundAggr.getAggregatorNames())); 

            		EmisContext compoundContext = getCompoundContext(localContext, compoundEntityType, ids[i]); 
            		Map<String, Double> values = getResultValues(compoundContext, compoundAggr, keys, null);
                    result.addSample(values);
            	}
            }
        }

        return result.getFinalValue(); 
    }

    private EmisContext getCompoundContext(EmisContext context, EmisMetaEntity entityType, int entityId)
    { return new ContextEntityOverride(new Entity(entityType, entityId), context); }
 
    private EmisContext getLocalContext(EmisContext context, EmisAggregatorDef aggr, Integer timeOffset)
    {
        EmisContext localContext = context;
        if (aggr.getContext() != null)
            localContext = new MultipleContext(new EmisContext[] { context, aggr.getContext() }, context.getDateType());

        if (timeOffset != null && timeOffset != 0)
            localContext = new ContextTimeOffset(localContext, timeOffset);

        if (IgnoreFilterContext.hasIgnoreFilters(aggr))
        	localContext = new IgnoreFilterContext(aggr, localContext);
        
        return localContext; 
    }
    
    private double getPlainAggregatorValue(EmisContext context, EmisAggregatorDef aggr, int timeOffset)
    {
    	EmisContext localContext = getLocalContext(context, aggr, timeOffset); 
    	
        EmisMetaData field = aggr.getMetaData();

        EmisMetaEntity entityType = aggr.getEntity();
        int entityTypeIndex = NamedUtil.findIndex(entityType, getDataSet().getMetaDataSet().getEntities());

        int dateTypeIndex = -1;
        EmisEntityDataSet dataset = null;
        EntityDataAccess access = null;
        if (field != null)
        {
            dateTypeIndex = NamedUtil.findIndex(field.getDateType(), getDataSet().getMetaDataSet().getDateEnums());
            dataset = getDataSet().getEntityDataSet(entityTypeIndex, dateTypeIndex);
            if (dataset == null)
                return Double.NaN;

            access = dataset.getDataAccess(aggr.getMetaData().getName());
        }
        else if (aggr.getCountDateType() != null)
        {
            dateTypeIndex = NamedUtil.findIndex(aggr.getCountDateType(), getDataSet().getMetaDataSet().getDateEnums());
            dataset = getDataSet().getEntityDataSet(entityTypeIndex, dateTypeIndex);
        }
        
        ResultValueAdaptor valueAdaptor = new PassThroughAdaptor(); 
        if (field != null && field.getType().equals(EmisMetaData.EmisDataType.ENUM))
        	valueAdaptor = new EnumBitCountAdaptor(field, context, aggr); 
        else if (field != null && field.getType().equals(EmisMetaData.EmisDataType.ENUM_SET))
        	valueAdaptor = new EnumSetBitCountAdaptor(field, context, aggr); 

        double total = (0.0D / 0.0D);
        for (EmisEntity entity : localContext.getEntities())
        {
            List<int[]> children = getHierarchy().getDescendants(localContext.getHierarchyDateIndex(), entity.getEntityType(), entity.getId(), entityType);
            children = filter(localContext, entityType, children);
            for (int[] ids : children)
                total = ResultUtil.add(total, getValueForEntities(dataset, field, aggr.getCountDateType(), access, ids, localContext, valueAdaptor));
        }
        
        return total;
    }

    private double getValueForEntities(EmisEntityDataSet dataset, EmisMetaData field, EmisMetaDateEnum countDateEnum, EntityDataAccess access, int[] entityIds, EmisContext context, ResultValueAdaptor valueAdaptor)
    {
        if (field == null)
        {
            int count = 0;
            for (int id : entityIds)
            {
                if (id == -1)
                	continue; 
                
                // Aggregators with no count date type - we just count entities found in list.
                // (unlike that it is used in future). 
            	if (countDateEnum == null)
            		count++;
            	else 
        		{
            		// Aggregators with count date type - we count number of entities that have data for the 
            		// specified dates.
            		boolean hasData = false; 
            		for (EmisEnumTupleValue dateValue : context.getDates(countDateEnum))
            			hasData |= null != dataset.getData(dateValue.getIndex(), id); 
            		
            		if (hasData)
            			count++; 
        		}
            }
            return count;
        }

        double result = (0.0D / 0.0D);

        EmisMetaDateEnum dateType = field.getDateType();
        for (int id : entityIds)
        {
            if (id == -1)
                continue;

            for (EmisEnumTupleValue date : context.getDates(dateType))
            {
                EmisEntityData data = dataset.getData(dateType.getIndex(date.getIndex()), id);
                if (data != null)
                    result = ResultUtil.add(result, getValueFromEntity(field, access, context, valueAdaptor, data.getMasterArray()));
            }
        }
        return result;
    }

    private double getValueFromEntity(EmisMetaData field, EntityDataAccess access, EmisContext context, ResultValueAdaptor valueAdaptor, Object[] masterArray)
    {
        double result = (0.0D / 0.0D);
        if (field.getArrayDimensions() == null)
            result = add(result, valueAdaptor.get(access.getAsInt(masterArray, 0)));
        else
        {
            EmisMetaEnumTuple dimensions = field.getArrayDimensions();
            Iterator<Integer> i$;
            if (hasFilters(dimensions, context))
            {
                for (i$ = FlatArrayUtil.getIndexes(field.getArrayDimensions(), context).iterator(); i$.hasNext();)
                {
                    int index = ((Integer) i$.next()).intValue();
                    result = add(result, valueAdaptor.get(access.getAsInt(masterArray, index)));
                }
            }
            else
            {
                int size = dimensions.getCombinations();
                for (int index = 0; index < size; index++)
                    result = add(result, valueAdaptor.get(access.getAsInt(masterArray, index)));
            }
        }

        return result;
    }

    private boolean hasFilters(EmisMetaEnumTuple dimensions, EmisContext context)
    {
        for (EmisMetaEnum e : dimensions.getEnums())
        {
            if (context.getEnumFilter(e.getName()) != null)
                return true;
        }
        return false;
    }

    private double add(double value1, int value2)
    {
        if (value2 == -1)
            return value1;

        if (Double.isNaN(value1))
            return value2;

        return value1 + value2;
    }

    protected void calculateAllResults(MetaResult metaResult, EmisMetaEntity entityType, String[] entityFields, ResultCollectorCallback callback)
    {
        int entityTypeIndex = NamedUtil.findIndex(entityType, getDataSet().getMetaDataSet().getEntities());

        EmisContext context = metaResult.getContext();
        for (EmisEntity entity : context.getEntities())
        {
            List<int[]> children = getHierarchy().getDescendants(context.getHierarchyDateIndex(), entity.getEntityType(), entity.getId(), entityType);
            children = filter(context, entityType, children);

            for (int[] ids : children)
            {
                EmisEntityDataSet dataset = getDataSet().getEntityDataSet(entityTypeIndex, getDataSet().getMetaDataSet().getDefaultDateTypeIndex());
                Map<Integer, String>[] entityValues = new Map[entityFields.length]; 
                for (int i = 0; i < entityFields.length; i++)
                    entityValues[i] = dataset.getAllValues(context.getHierarchyDateIndex(), entityFields[i], ids);

                for (int i = 0; i < ids.length; i++)
                {
                    List<EmisEntity> entities = new ArrayList<EmisEntity>();
                    entities.add(new Entity(entityType, ids[i]));
                    context.setEntities(entities);
                    
                    Map<String, String> values = new HashMap<String, String>();
                    for (int y = 0; y < entityFields.length; y++) 
                        values.put(entityFields[y], entityValues[y].get(ids[i])); 
                    
                    callback.setResult(ids[i], values, getResultValues(context));  
                }
            }
        }
    }
}

interface ResultValueAdaptor
{
	public int get(int value); 
}

class PassThroughAdaptor implements ResultValueAdaptor
{
	public int get(int value)
	{ return value; } 
}

class EnumBitCountAdaptor implements ResultValueAdaptor
{
	private EmisEnumSet enumFilter; 
	
	public EnumBitCountAdaptor(EmisMetaData field, EmisContext context, EmisAggregatorDef aggr)
	{
		EmisMetaEnum enumType = field.getEnumType(); 
		if (aggr.ignoreFilter(field))
			return; 
		
		enumFilter = context.getEnumFilter(enumType.getName()); 
	}
	
	public int get(int value)
	{ 
		if (value == -1)
			return -1; 
		
		if (enumFilter == null)
			// No filter - just return the value. 
			return 1;

		return enumFilter.hasValue((byte) value) ? 1 : -1;  
	}
}

class EnumSetBitCountAdaptor implements ResultValueAdaptor
{
	// Array of number of bits for value of each index. 
	private static byte[] bitCount = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 }; 

	protected int mask; 

	public EnumSetBitCountAdaptor(EmisMetaData field, EmisContext context, EmisAggregatorDef aggr)
	{
		EmisMetaEnum enumType = field.getEnumType(); 

		// Default mask is for all values. 
		mask = pow(2, enumType.getSize()) - 1;  

		// Filter is overwritten by ignoreFilterEnums. 
		if (aggr.ignoreFilter(field))
			return; 
		
		EmisEnumSet enumFilter = context.getEnumFilter(enumType.getName());
		if (enumFilter == null)
			// No enum filter is set. 
			return; 
		
		mask = 0; 
		for (byte index : enumFilter.getAllIndexes())
			mask |= pow(2, index); 
	}
	
	private int pow(int base, int power)
	{
		int result = base; 
		for (int i = 1; i < power; i++)
			result *= base; 
		
		return result; 
	}
	
	public int get(int value)
	{
		if (value == -1)
			return -1; 
		
		value &= mask;
		
		int result = 0; 
		while (value > 0)
		{
			result += bitCount[value & 0xF]; 
			value = value >> 4; 
		}

		return result; 
	}
}
