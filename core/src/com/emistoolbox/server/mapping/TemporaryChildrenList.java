package com.emistoolbox.server.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class TemporaryChildrenList extends HashMap<Integer, Set<Integer>>
{
    private static final long serialVersionUID = 1L;

    public boolean addChild(int parentId, int childId)
    {
        if ((parentId == -1) || (childId == -1))
        {
            return false;
        }
        Set<Integer> children = get(Integer.valueOf(parentId));
        if (children == null)
        {
            children = new HashSet();
            put(Integer.valueOf(parentId), children);
        }

        children.add(Integer.valueOf(childId));
        return true;
    }

    public int[] getChildren(int parentId)
    {
        Set<Integer> children = get(Integer.valueOf(parentId));
        if (children == null)
        {
            return null;
        }
        int[] result = new int[children.size()];
        int i = 0;
        for (Integer child : children)
        {
            result[i] = child.intValue();
            i++;
        }

        return result;
    }
}
