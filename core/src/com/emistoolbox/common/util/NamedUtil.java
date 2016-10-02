package com.emistoolbox.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NamedUtil
{
    public static <T extends Named> List<String> getNames(List<T> items)
    {
        List<String> result = new ArrayList<String>();
        for (T item : items)
        {
            result.add(item.getName());
        }
        return result;
    }

    public static <T extends Named> String[] getNames(T[] items)
    {
        List result = new ArrayList();
        for (Named item : items)
        {
            result.add(item.getName());
        }
        return (String[]) result.toArray(new String[0]);
    }

    public static <T extends Named> List<T> findAll(String[] names, List<T> items)
    {
        List result = new ArrayList();
        for (String name : names)
        {
            Named item = find(name, items);
            if (item != null)
            {
                result.add(item);
            }
        }
        return result;
    }

    public static <T extends Named> int findIndex(T item, List<T> items)
    {
        if (item == null)
        {
            return -1;
        }
        return findIndex(item.getName(), items);
    }

    public static <T extends Named> int findIndex(String name, List<T> items)
    {
        if (name == null)
        {
            return -1;
        }
        int index = 0;
        for (Named item : items)
        {
            if (name.equals(item.getName()))
                return index;
            index++;
        }

        return -1;
    }

    public static <T extends Named> T find(String name, List<T> items)
    {
        if (name == null)
            return null;

        for (T item : items)
        {
            if (name.equals(item.getName()))
                return item;
        }

        return null;
    }

    public static <T extends Named> boolean sameName(T named1, T named2)
    {
        if (named1 == null)
            return named2 == null;

        if (named2 == null)
            return false; 
        
        if (named1.getName() == null)
            return named2.getName() == null; 
        
        return named1.getName().equals(named2.getName());
    }

    public static <T extends Named> List<T> sort(List<T> list)
    {
        Collections.sort(list, new Comparator<T>() {
            public int compare(T o1, T o2)
            {
                if (((o1 == null) || (o1.getName() == null)) && ((o2 == null) || (o2.getName() == null)))
                    return 0;
                if ((o1 == null) || (o1.getName() == null))
                    return -1;
                if ((o2 == null) || (o2.getName() == null))
                {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }

    public static <T extends Named> List<T> listOr(List<T> list1, List<T> list2)
    {
        List<T> result = new ArrayList<T>();
        result.addAll(list1);
        for (T item : list2)
        {
            if (find(item.getName(), result) == null)
            {
                result.add(item);
            }
        }
        return result;
    }

    public static <T extends Named> List<T> listAnd(List<T> list1, List<T> list2)
    {
        List<T> result = new ArrayList<T>();
        for (T item : list1)
        {
            if (find(item.getName(), list2) != null)
            {
                result.add(item);
            }
        }
        return result;
    }
}
