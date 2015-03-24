package com.emistoolbox.server.results;

import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.model.priolist.impl.PriorityListItemImpl;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.server.model.EmisDataSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriorityResultCollector extends ResultCollector
{
    private PriorityMetaResult prioMetaResult;

    public PriorityResultCollector(EmisDataSet emisDataSet, PriorityMetaResult metaResult) {
        super(emisDataSet, metaResult);
        this.prioMetaResult = metaResult;
    }

    public List<PriorityListItem> getResults()
    {
        final List<PriorityListItem> result = new ArrayList<PriorityListItem>();

        calculateAllResults(prioMetaResult, prioMetaResult.getListEntity(), prioMetaResult.getAdditionalFields(), new ResultCollectorCallback() {
            public void setResult(int id, Map<String, String> entityValues, double[] values)
            {
                PriorityListItem tmp = new PriorityListItemImpl();
                tmp.setId(id);
                tmp.setName(entityValues.get("name"));
                tmp.setValues(values);
                
                String[] additionalFields = prioMetaResult.getAdditionalFields();
                String[] fieldValues = new String[additionalFields.length]; 
                for (int i = 0; i < additionalFields.length; i++)
                	fieldValues[i] = entityValues.get(additionalFields[i]);
                tmp.setEntityValues(fieldValues);
                
                result.add(tmp);
            }
        });
        
        return result;
    }
}
