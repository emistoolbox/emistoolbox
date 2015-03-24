package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hierarchy implements EmisHierarchy, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnum dateType;
    private EmisMetaHierarchy hierarchy;
    private Map<Integer, int[]>[][] children;

    public Hierarchy(EmisMetaEnum dateType, EmisMetaHierarchy hierarchy) {
        this.dateType = dateType;
        this.hierarchy = hierarchy;
        this.children = new Map[dateType.getSize() + 1][];
    }

    public int[] getRootElements(int dateIndex)
    {
        Map<Integer, int[]> map = getMap(dateIndex, 0, false);
        if (map == null)
        {
            return null;
        }
        int[] result = new int[map.size()];
        int index = 0;
        for (Integer id : map.keySet())
        {
            result[index] = id.intValue();
            index++;
        }

        return result;
    }

    private int[] getChildren(int dateIndex, int hierarchyIndex, Integer id)
    {
        if ((hierarchyIndex < 0) || (hierarchyIndex + 1 >= this.hierarchy.getEntityOrder().size()))
        {
            return null;
        }
        Map map = getMap(dateIndex, hierarchyIndex, false);
        if (map == null)
            return null;

        return (int[]) map.get(id);
    }
    
    public void setChildren(int dateIndex, int hierarchyIndex, int id, int[] children)
    {
        getMap(dateIndex, hierarchyIndex, true).put(Integer.valueOf(id), children);
    }

    public Integer findParentId(EmisEntity child, int dateIndex)
    {
        int entityTypeIndex = NamedUtil.findIndex(child.getEntityType().getName(), hierarchy.getEntityOrder());
        if (entityTypeIndex < 1)
            return null; 

        // Find parent to child mappings for appropriate level. 
        Map<Integer, int[]> idMap = getMap(dateIndex, entityTypeIndex - 1, false); 
        for (Map.Entry<Integer, int[]> entry : idMap.entrySet())
        {
            int[] childIds = entry.getValue();
            for (int i = 0; i < childIds.length; i++)
                if (childIds[i] == child.getId())
                    return entry.getKey(); 
        }
        
        return null; 
    }
    
    private Map<Integer, int[]> getMap(int dateIndex, int hierarchyIndex, boolean withCreate)
    {
        Map[] tmp = this.children[(dateIndex + 1)];
        if (tmp == null)
        {
            tmp = new Map[this.hierarchy.getEntityOrder().size() - 1];
            this.children[(dateIndex + 1)] = tmp;
        }

        if ((withCreate) && (tmp[hierarchyIndex] == null))
        {
            tmp[hierarchyIndex] = new HashMap();
        }
        return tmp[hierarchyIndex];
    }

    public List<int[]> getDescendants(int dateIndex, EmisMetaEntity ancestorType, int ancestorId, EmisMetaEntity descendantType)
    {
        List result = new ArrayList();
        if (ancestorType == null)
        {
            EmisMetaEntity rootEntity = (EmisMetaEntity) getMetaHierarchy().getEntityOrder().get(0);
            for (int tmpAncestorId : getRootElements(dateIndex))
            {
                result.addAll(getDescendants(dateIndex, rootEntity, tmpAncestorId, descendantType));
            }
            return result;
        }

        NamedIndexList entities = this.hierarchy.getEntityOrder();
        int startIndex = entities.getIndex(ancestorType);
        int endIndex = entities.getIndex(descendantType);
        if (startIndex > endIndex)
        {
            return result;
        }
        result.add(new int[] { ancestorId });
        for (int i = startIndex; i < endIndex; i++)
        {
            result = getChildren(getMap(dateIndex, i, false), result);
        }
        return result;
    }

    private List<int[]> getChildren(Map<Integer, int[]> relations, List<int[]> items)
    {
        List result = new ArrayList();
        if (relations == null)
        {
            return result;
        }
        for (int[] item : items)
        {
            for (int i = 0; i < item.length; i++)
            {
                int[] children = (int[]) relations.get(Integer.valueOf(item[i]));
                if ((children != null) && (children.length > 0))
                {
                    result.add(children);
                }
            }
        }
        return result;
    }

    public void setChildren(int dateIndex, EmisMetaEntity parentType, int parentId, int[] children)
    {
        setChildren(dateIndex, getHierarchyIndex(parentType), parentId, children);
    }

    public int[] getChildren(int dateIndex, EmisMetaEntity parentType, int parentId)
    {
        return getChildren(dateIndex, getHierarchyIndex(parentType), Integer.valueOf(parentId));
    }
    
    public EmisEntityIterator getChildren(int dateIndex, EmisEntity parentEntity)
    {
    	int index = getHierarchyIndex(parentEntity.getEntityType());
    	return new IntArrayEntityIterator(hierarchy.getEntityOrder().get(index + 1), getChildren(dateIndex, parentEntity.getEntityType(), parentEntity.getId())); 
    }

    private int getHierarchyIndex(EmisMetaEntity entity)
    { return NamedUtil.findIndex(entity, this.hierarchy.getEntityOrder()); }

    public EmisMetaHierarchy getMetaHierarchy()
    {
        return this.hierarchy;
    }

    public EmisMetaEnum getDateType()
    {
        return this.dateType;
    }
}
