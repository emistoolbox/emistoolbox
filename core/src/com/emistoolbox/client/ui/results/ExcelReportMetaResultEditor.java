package com.emistoolbox.client.ui.results;

import java.util.List;
import java.util.Set;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.results.ExcelReportMetaResult;
import com.emistoolbox.common.results.impl.MetaResultUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

public class ExcelReportMetaResultEditor extends MetaResultEditor<ExcelReportMetaResult>
{
    private ExcelReportConfig currentReport = null; 
    
    public ExcelReportMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities) 
    {
        super(toolbox, emisMeta, reportConfig, rootEntities); 
        updateUi(); 
    }

    protected void updateUi()
    {
        clear(true);

        if (showCurrentHierarchy(0))
            return;

        if (showCurrentReport(2))
            return;

        if (showEntityEditor(4))
            return;

        if (showDateEditor(8, getUsedDateTypes()))
            return;

        ValueChangeEvent.fire(this, get());
    }
    
    @Override
	protected Set<EmisMetaDateEnum> getUsedDateTypes() 
	{ return MetaResultUtil.getUsedDateTypes(currentReport); }
    
	private boolean showCurrentReport(final int row)
    {
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlReport());
        if (this.currentReport == null)
        {
            editReport(row);
            return true;
        }

        show(row + 1, currentReport, new ClickHandler() {
            public void onClick(ClickEvent event)
            { ExcelReportMetaResultEditor.this.editReport(row); }
        });
        return false;
    }

    private void editReport(int row)
    {
        final ListBoxWithUserObjects<ExcelReportConfig> uiReports = new ListBoxWithUserObjects<ExcelReportConfig>();
        uiReports.add("", null);
        for (ExcelReportConfig reportConfig : getReportConfig().getExcelReports())
        {
            if (reportConfig.isReady())
                uiReports.add(reportConfig.toString(), reportConfig);  
        }

        uiReports.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                currentReport = uiReports.getValue();
                ExcelReportMetaResultEditor.this.updateUi();
            }
        });
        
        setWidget(row + 1, 0, uiReports);
        removeFromRow(row + 2);
    }

    public void commit()
    {
        super.commit();

        metaResult.setContext(getContext(true, true));
        metaResult.setReportConfig(currentReport);
    }

    protected PdfContentConfig getContentConfig(int paramInt)
    { return null; }
}
