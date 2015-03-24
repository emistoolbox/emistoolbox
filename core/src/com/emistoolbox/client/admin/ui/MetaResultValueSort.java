package com.emistoolbox.client.admin.ui;

import com.emistoolbox.common.results.MetaResultValue;
import java.util.ArrayList;
import java.util.List;

public class MetaResultValueSort
{
    private List<MetaResultValue> values;
    private List<Boolean> sortAscending;

    public MetaResultValueSort() {
        this.values = new ArrayList();
        this.sortAscending = new ArrayList();
    }

    public MetaResultValueSort(List<MetaResultValue> values, List<Boolean> sortAscending) {
        List newValues = new ArrayList();
        List newSortAscending = new ArrayList();

        for (int i = 0; i < values.size(); i++)
        {
            MetaResultValue v = (MetaResultValue) values.get(i);
            if (v.getIndicator() == null)
            {
                continue;
            }
            newValues.add(v);
            newSortAscending.add(sortAscending.get(i));
        }

        this.values = newValues;
        this.sortAscending = newSortAscending;
    }

    public void clear()
    {
        this.values.clear();
        this.sortAscending.clear();
    }

    public MetaResultValue getValue(int index)
    {
        return (MetaResultValue) this.values.get(index);
    }

    public List<MetaResultValue> getValues()
    {
        return this.values;
    }

    public Boolean getSortAscending(int index)
    {
        return (Boolean) this.sortAscending.get(index);
    }

    public List<Boolean> getSortAscending()
    {
        return this.sortAscending;
    }

    public void add(MetaResultValue value, Boolean sortAscending)
    {
        this.values.add(value);
        this.sortAscending.add(sortAscending);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.MetaResultValueSort JD-Core
 * Version: 0.6.0
 */