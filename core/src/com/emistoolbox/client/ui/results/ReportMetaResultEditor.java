package com.emistoolbox.client.ui.results;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

public class ReportMetaResultEditor extends MetaResultEditor<ReportMetaResult>
{
    private EmisPdfReportConfig currentReport;
    private ListBoxWithUserObjects<ChartColor[]> uiColourScheme = new ListBoxWithUserObjects<ChartColor[]>(); 

    public ReportMetaResultEditor(EmisToolbox toolbox, EmisMeta emisMeta, EmisReportConfig reportConfig, List<EmisEntity> rootEntities)
    {
        super(toolbox, emisMeta, reportConfig, rootEntities);
        updateUi();
        
        uiColourScheme.addItem("Colours", ChartConfig.PALLET_VARIED); 
        uiColourScheme.addItem("Grays", ChartConfig.PALLET_GRAYS); 
        uiColourScheme.addItem("Yellows", ChartConfig.PALLET_YELLOW); 
        uiColourScheme.addItem("Shadings", ChartConfig.PALLET_SHADES);
        
        uiColourScheme.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                // Fire if we have all details only.  
                if (getCurrentDate() != null)
                    ValueChangeEvent.fire(ReportMetaResultEditor.this, get()); 
            }
        }); 
    }

    protected EmisEnumTupleValue getDefaultDate()
    {
        return null;
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

        setSectionHTML(10, 0, "Colour Scheme"); // TODO: i18n
        setWidget(11, 0, uiColourScheme); 
        
        ValueChangeEvent.fire(this, get());
    }
    
    @Override
	protected Set<EmisMetaDateEnum> getUsedDateTypes() 
	{ return MetaResultUtil.getUsedDateTypes(currentReport); } 

	protected void configureEntityEditor(IHierarchyBrowser editor)
    {
        editor.setAnySelection(true);
        editor.setEntityType(this.currentReport.getEntityType());
    }

    private boolean showCurrentReport(final int row)
    {
        setSectionHTML(row, 0, Message.messageAdmin().mreHtmlReport());
        if (this.currentReport == null)
        {
            editReport(row);
            return true;
        }

        show(row + 1, this.currentReport, new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                ReportMetaResultEditor.this.editReport(row);
            }
        });
        return false;
    }

    public void commit()
    {
        super.commit();
        EmisContext context = getContext(true, true);
        ((ReportMetaResult) this.metaResult).setContext(context);
        ((ReportMetaResult) this.metaResult).setReportConfig(this.currentReport);

        int size = getPathSize(getEntityPathIds(), getEntityPathNames());
        ((ReportMetaResult) this.metaResult).setEntityPath(resize(getEntityPathIds(), size), resize(getEntityPathNames(), size));
    }

    private int getPathSize(int[] ids, String[] names)
    {
        for (int i = 0; i < Math.min(ids.length, names.length); i++)
        {
            if (ids[i] == -1 || names[i] == null)
                return i;
        }

        return Math.min(ids.length, names.length);
    }

    private int[] resize(int[] values, int size)
    {
        if (values.length == size)
            return values;

        int[] result = new int[size];
        for (int i = 0; i < Math.min(size, values.length); i++)
            result[i] = values[i];

        for (int i = values.length; i < result.length; i++)
            result[i] = -1;

        return result;
    }

    private String[] resize(String[] values, int size)
    {
        if (values.length == size)
            return values;

        String[] result = new String[size];
        for (int i = 0; i < Math.min(size, values.length); i++)
            result[i] = values[i];

        for (int i = values.length; i < result.length; i++)
            result[i] = null;

        return result;
    }

    private void editReport(int row)
    {
        final ListBoxWithUserObjects<EmisPdfReportConfig> uiReports = new ListBoxWithUserObjects<EmisPdfReportConfig>();
        uiReports.add("", null);
        for (EmisPdfReportConfig reportConfig : getReportConfig().getPdfReports())
        {
            if (reportConfig.getEntityType() != null)
                uiReports.add(reportConfig.getName() + " (" + reportConfig.getEntityType().getName() + ")", reportConfig);
            else
            	uiReports.add(reportConfig.getName(), reportConfig);
        }
        
        uiReports.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                currentReport = uiReports.getValue();
                ReportMetaResultEditor.this.updateUi();
            }
        });
        setWidget(row + 1, 0, uiReports);
        removeFromRow(row + 2);
    }

    protected PdfContentConfig getContentConfig(int addButton)
    {
        throw new IllegalArgumentException("Cannot create PdfContentConfig from ReportMetaResult.");
    }

    protected EmisMetaDateEnum getSeniorDateEnum()
    {
        if (this.currentReport == null)
        {
            return null;
        }
        return this.currentReport.getSeniorDateEnum();
    }

    public void set(ReportMetaResult metaResult)
    {
        super.set(metaResult);
        this.currentReport = metaResult.getReportConfig();

        uiColourScheme.setSelectedIndex(0); 
        for (int i = 0; i < uiColourScheme.getItemCount(); i++) 
        { 
            if (isEquals(metaResult.getColourScheme(), uiColourScheme.getUserObject(i)))
                uiColourScheme.setSelectedIndex(i); 
        }
        
        updateUi();
    }
    
    private boolean isEquals(ChartColor[] colorA, ChartColor[] colorB)
    {
        if (colorA == null && colorB == null)
            return true; 
        
        if (colorA == null || colorB == null)
            return false; 
        
        if (colorA.length != colorB.length)
            return false; 
        
        for (int i = 0; i < colorA.length; i++) 
            if (!colorA[i].equals(colorB[i]))
                return false; 
        
        return true; 
    }
    
    public ReportMetaResult get()
    {
        ReportMetaResult result = super.get();
        
        ChartColor[] colours = uiColourScheme.getUserObject(); 
        if (colours == null)
            colours = ChartConfig.PALLET_VARIED; 
        result.setColourScheme(colours);
        
        return result;
    }
}
