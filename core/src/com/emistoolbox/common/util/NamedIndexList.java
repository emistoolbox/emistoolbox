package com.emistoolbox.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NamedIndexList<T extends Named> extends ArrayList<T> implements Serializable
{
    private static final long serialVersionUID = 1L;
    private transient Map<String, Integer> indexes = new HashMap();

    public int getIndex(T item)
    {
        return getIndex(item.getName());
    }

    public int getIndex(String name)
    {
        if ((this.indexes == null) || ((this.indexes.size() == 0) && (size() > 0)))
        {
            reindex();
        }
        Integer index = (Integer) this.indexes.get(name);
        if (index == null)
        {
            return -1;
        }
        return index.intValue();
    }

    public boolean add(T item)
    {
        int index = getIndex(item);
        if (index == -1)
        {
            this.indexes.put(item.getName(), Integer.valueOf(size()));
            return super.add(item);
        }

        remove(index);
        add(index, item);

        return true;
    }

    public boolean addAll(Collection<? extends T> c)
    {
        for (T item : c)
        {
            add(item);
        }
        return true;
    }

    public T remove(int index)
    {
        T result = (T) super.remove(index);
        reindex();
        return result;
    }

    public boolean remove(Object o)
    {
        boolean result = super.remove(o);
        reindex();
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex)
    {
        super.removeRange(fromIndex, toIndex);
        reindex();
    }

    public void clear()
    {
        super.clear();
        this.indexes.clear();
    }

    private void reindex()
    {
        if (this.indexes == null)
            this.indexes = new HashMap();
        else
        {
            this.indexes.clear();
        }
        int index = 0;
        for (Named item : this)
        {
            this.indexes.put(item.getName(), Integer.valueOf(index));
            index++;
        }
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.util.NamedIndexList JD-Core Version:
 * 0.6.0
 */