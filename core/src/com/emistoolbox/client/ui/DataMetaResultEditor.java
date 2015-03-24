package com.emistoolbox.client.ui;

import java.util.List;
import java.util.Set;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.ui.results.MetaResultEditor;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

public class DataMetaResultEditor extends MetaResultEditor<MetaResult>
{
    public DataMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities)
    {
        super(toolbox, emisMeta, reportConfig, rootEntities); 
        updateUi(); 
    }

    protected void updateUi()
    {
        clear(true);

        if (showCurrentHierarchy(0))
            return;
        
        if (showDateEditor(2, getDateTypes()))
            return;

        if (showEntityEditor(4))
            return;

        ValueChangeEvent.fire(this, get());
    }

    public void commit()
    {
        super.commit();
        metaResult.setContext(getContext(true, true)); 
    }

    protected PdfContentConfig getContentConfig(int paramInt)
    { return null; }
    
    private Set<EmisMetaDateEnum> getDateTypes()
    { 
    	EmisEntity entity = getCurrentEntity(); 
    	return entity == null ? null : entity.getEntityType().getUsedDateTypes(); 
    }
}
