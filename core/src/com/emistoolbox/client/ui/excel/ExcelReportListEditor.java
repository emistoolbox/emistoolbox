package com.emistoolbox.client.ui.excel;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.excelMerge.impl.ExcelReportConfigImpl;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ExcelReportListEditor extends FlexTable implements EmisEditor<List<ExcelReportConfig>>
{
    private ListBoxWithUserObjects<ExcelReportConfig> uiReports = new ListBoxWithUserObjects<ExcelReportConfig>();
    private ExcelReportConfigEditor uiEditor;
    private PushButton uiAddReportButton = new PushButton(Message.messageAdmin().prcleAdd());
    private PushButton uiDelReportButton = new PushButton(Message.messageAdmin().prcleDel());
    private PushButton uiViewReportButton = new PushButton(Message.messageAdmin().prcleView());
    
    public ExcelReportListEditor(EmisToolbox toolbox, String dataSetName, EmisMeta meta, EmisReportConfig reports)
    {
        this.uiEditor = new ExcelReportConfigEditor(toolbox, dataSetName, meta, reports);
        this.uiEditor.setVisible(false);
        
        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(2);
        EmisUtils.init(this.uiAddReportButton, 60);
        buttons.add(this.uiAddReportButton);
        EmisUtils.init(this.uiDelReportButton, 60);
        buttons.add(this.uiDelReportButton);
        EmisUtils.init(this.uiViewReportButton, 60);
        buttons.add(this.uiViewReportButton);

        uiReports.setVisibleItemCount(25);
        uiReports.setStyleName("fullWidth");

        getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_TOP);
        setCellSpacing(5);
        setWidget(0, 0, buttons);
        setWidget(1, 0, EmisToolbox.metaResultEditFrame(uiReports));
        setWidget(1, 1, uiEditor); 

        this.uiAddReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String newId = EmisUtils.getUniqueId(ExcelReportListEditor.this.uiReports, Message.messageAdmin().prcleNewReportId());
                if (newId == null)
                    return;

                ExcelReportConfig report = new ExcelReportConfigImpl();
                report.setName(newId);
                ExcelReportListEditor.this.uiReports.addItem(newId, report);
                ExcelReportListEditor.this.select(ExcelReportListEditor.this.uiReports.getItemCount() - 1, true);
            }
        });

        this.uiDelReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int index = ExcelReportListEditor.this.uiReports.getSelectedIndex();
                if (index != -1)
                    ExcelReportListEditor.this.uiReports.removeItem(index);

                index = Math.min(index, ExcelReportListEditor.this.uiReports.getItemCount() - 1);
                if (index == -1)
                    ExcelReportListEditor.this.show(null);
                else
                    ExcelReportListEditor.this.select(index, true);
            }
        });
        
        this.uiViewReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int index = ExcelReportListEditor.this.uiReports.getSelectedIndex();
                if (index == -1)
                    return;

//                module.showReport((PdfReportConfig) PdfReportConfigListEditor.this.uiReports.getUserObject(index));
            }
        });

        this.uiReports.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { ExcelReportListEditor.this.select(ExcelReportListEditor.this.uiReports.getSelectedIndex(), false); }
        });
    }
    
    public void commit()
    { 
        ExcelReportConfig config = uiEditor.get();
        for (int i = 0; i < uiReports.getItemCount(); i++) 
        {
            if (uiReports.getUserObject(i) == config)
                uiReports.setItemText(i, config.toString()); 
        }
    }
    

    public List<ExcelReportConfig> get()
    {
        commit(); 
        List<ExcelReportConfig> result = new ArrayList<ExcelReportConfig>();
        for (int i = 0; i < uiReports.getItemCount(); i++)
            result.add(uiReports.getUserObject(i)); 
            
        return result;
    }

    public void set(List<ExcelReportConfig> configs)
    {
        uiReports.clear(); 
        if (configs == null)
            return; 
    
        for (ExcelReportConfig config : configs)
            uiReports.addItem(config.toString(), config);  
                
        if (configs.size() > 0)
            select(0, true); 
    }

    private void select(int index, boolean updateUi)
    {
        if (this.uiEditor.isVisible())
            commit(); 

        if (updateUi)
            this.uiReports.setSelectedIndex(index);

        if (index == -1)
            show(null);
        else
            show(this.uiReports.getUserObject(index));
    }
    
    private void show(ExcelReportConfig config)
    {
        uiEditor.commit();
        uiEditor.set(config); 
        uiEditor.setVisible(config != null); 
    }
}
