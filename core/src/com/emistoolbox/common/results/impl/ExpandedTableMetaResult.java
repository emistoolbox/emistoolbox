package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.TableMetaResult;
import java.util.ArrayList;
import java.util.List;

public class ExpandedTableMetaResult extends TableMetaResultAdaptor implements TableMetaResult
{
    private List<MetaResultValue> expandedValues = new ArrayList();
    private TableMetaResult metaResult = null;

    public ExpandedTableMetaResult(TableMetaResult metaResult) 
    {
        super(metaResult);
        this.metaResult = metaResult;
        updateExpandedValues();
    }

    public void addMetaResultValue(MetaResultValue value)
    {
        this.metaResult.addMetaResultValue(value);
        updateExpandedValues();
    }

    public MetaResultValue getMetaResultValue(int index)
    { return (MetaResultValue) this.expandedValues.get(index); }

    public int getMetaResultValueCount()
    { return this.expandedValues.size(); }

    public List<MetaResultValue> getMetaResultValues()
    { return this.expandedValues; }

    public void setMetaResultValues(List<MetaResultValue> values)
    {
        this.metaResult.setMetaResultValues(values);
        updateExpandedValues();
    }

    private void updateExpandedValues()
    {
        this.expandedValues.clear();
        for (MetaResultValue value : this.metaResult.getMetaResultValues())
        {
            this.expandedValues.add(value);
            if (value.getAggregatorKey() != null)
                continue;

            for (String name : value.getIndicator().getAggregatorNames())
            {
                EmisAggregatorDef aggr = value.getIndicator().getAggregator(name);
                if ((aggr.getName() == null) || (aggr.getName().equals("")))
                {
                    continue;
                }
                MetaResultValue newValue = new MetaResultValueImpl();
                newValue.setIndicator(value.getIndicator());
                newValue.setAggregatorKey(name);
                newValue.setAggregatorName(aggr.getName());

                this.expandedValues.add(newValue);
            }
        }
    }
}

