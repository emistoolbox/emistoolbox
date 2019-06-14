package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ReportModule;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfReportConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutPdfReportConfigImpl;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.EmisConfig;
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

import java.util.ArrayList;
import java.util.List;

public class PdfReportConfigListEditor extends FlexTable implements EmisEditor<List<EmisPdfReportConfig>>
{
    private ListBoxWithUserObjects<EmisPdfReportConfig> uiReports = new ListBoxWithUserObjects<EmisPdfReportConfig>();
    private EmisPdfReportEditor uiEditor; 
    private PushButton uiAddReportButton = new PushButton(Message.messageAdmin().prcleAdd());
    private PushButton uiAddAdvancedReportButton = new PushButton(Message.messageAdmin().prcleAdd() + " Adv");
    private PushButton uiDelReportButton = new PushButton(Message.messageAdmin().prcleDel());
    private PushButton uiViewReportButton = new PushButton(Message.messageAdmin().prcleView());

    public PdfReportConfigListEditor(EmisMeta meta, final ReportModule module) 
    {
        this.uiEditor = new EmisPdfReportEditor(meta);
        this.uiReports.setVisibleItemCount(20);

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(2);
        EmisUtils.init(this.uiAddReportButton, 60);
        buttons.add(this.uiAddReportButton);
        EmisUtils.init(this.uiAddAdvancedReportButton, 60);
        if (EmisConfig.ADVANCED_REPORTS)
        	buttons.add(this.uiAddAdvancedReportButton);
        EmisUtils.init(this.uiDelReportButton, 60);
        buttons.add(this.uiDelReportButton);
        EmisUtils.init(this.uiViewReportButton, 60);
        buttons.add(this.uiViewReportButton);

        setWidget(0, 0, buttons); 
        getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_TOP);
        setWidget(1, 0, EmisToolbox.metaResultEditFrame(uiReports)); 
        setWidget(1, 1, uiEditor);
        setCellSpacing(5);

        this.uiReports.setWidth("350px");

        this.uiAddReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String newId = EmisUtils.getUniqueId(PdfReportConfigListEditor.this.uiReports, Message.messageAdmin().prcleNewReportId());
                if (newId == null)
                    return;

                PdfReportConfig report = new PdfReportConfigImpl();
                report.setName(newId);
                PdfReportConfigListEditor.this.uiReports.addItem(newId, report);
                PdfReportConfigListEditor.this.selectReport(PdfReportConfigListEditor.this.uiReports.getItemCount() - 1, true);
            }
        });
        
        this.uiAddAdvancedReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                String newId = EmisUtils.getUniqueId(PdfReportConfigListEditor.this.uiReports, Message.messageAdmin().prcleNewReportId());
                if (newId == null)
                    return;

                LayoutPdfReportConfig report = new LayoutPdfReportConfigImpl();
                report.setName(newId);
                PdfReportConfigListEditor.this.uiReports.addItem(newId, report);
                PdfReportConfigListEditor.this.selectReport(PdfReportConfigListEditor.this.uiReports.getItemCount() - 1, true);
            }
        });
        
        this.uiDelReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int index = PdfReportConfigListEditor.this.uiReports.getSelectedIndex();
                if (index != -1)
                {
                    PdfReportConfigListEditor.this.uiReports.removeItem(index);
                }
                index = Math.min(index, PdfReportConfigListEditor.this.uiReports.getItemCount() - 1);
                if (index == -1)
                    uiEditor.set(null);
                else
                    PdfReportConfigListEditor.this.selectReport(index, true);
            }
        });
        this.uiViewReportButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int index = PdfReportConfigListEditor.this.uiReports.getSelectedIndex();
                if (index == -1)
                {
                    return;
                }
                module.showReport((PdfReportConfig) PdfReportConfigListEditor.this.uiReports.getUserObject(index));
            }
        });
        this.uiReports.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                PdfReportConfigListEditor.this.selectReport(PdfReportConfigListEditor.this.uiReports.getSelectedIndex(), false);
            }
        });
    }

    public void selectReport(EmisPdfReportConfig report)
    {
        for (int i = 0; i < this.uiReports.getItemCount(); i++)
        {
            if (!NamedUtil.sameName(report, (Named) this.uiReports.getUserObject(i)))
                continue;
            selectReport(i, true);
            return;
        }
    }

    private void selectReport(int index, boolean updateUi)
    {
    	uiEditor.commit();

        if (updateUi)
            this.uiReports.setSelectedIndex(index);

        if (index == -1)
        	uiEditor.set(null);
        else
        	uiEditor.set((EmisPdfReportConfig) this.uiReports.getUserObject(index));
    }

    public void set(List<EmisPdfReportConfig> configs)
    {
        this.uiReports.clear();
        if (configs == null)
            return;

        for (EmisPdfReportConfig config : configs)
            this.uiReports.addItem(config.getName(), config);

        if (configs.size() > 0)
            selectReport(0, true);
    }

    public void commit()
    { uiEditor.commit(); }

    public List<EmisPdfReportConfig> get()
    {
        commit();
        List<EmisPdfReportConfig> result = new ArrayList<EmisPdfReportConfig>();
        for (int i = 0; i < this.uiReports.getItemCount(); i++)
            result.add(this.uiReports.getUserObject(i));

        return result;
    }
}
