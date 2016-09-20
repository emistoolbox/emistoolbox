package com.emistoolbox.client.admin;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.OpenLayersUtil;
import com.emistoolbox.client.ui.ActionPanel;
import com.emistoolbox.client.ui.BlockingScreen;
import com.emistoolbox.client.ui.GlobalFilterUi;
import com.emistoolbox.client.ui.ManageReportDatasetDialog;
import com.emistoolbox.client.ui.DataMetaResultEditor;
import com.emistoolbox.client.ui.DownloadPanel;
import com.emistoolbox.client.ui.PriorityListTable;
import com.emistoolbox.client.ui.ValidationMetaResultEditor;
import com.emistoolbox.client.ui.analysis.IndicatorConfigurationEditor;
import com.emistoolbox.client.ui.excel.ExcelReportListEditor;
import com.emistoolbox.client.ui.pdf.PdfReportConfigListEditor;
import com.emistoolbox.client.ui.results.ExcelReportMetaResultEditor;
import com.emistoolbox.client.ui.results.GisMetaResultEditor;
import com.emistoolbox.client.ui.results.MetaResultEditor;
import com.emistoolbox.client.ui.results.PriorityMetaResultEditor;
import com.emistoolbox.client.ui.results.ReportMetaResultEditor;
import com.emistoolbox.client.ui.results.TableMetaResultEditor;
import com.emistoolbox.client.ui.validation.ValidationResultBrowser;
import com.emistoolbox.common.EmisReportModuleData;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.model.validation.EmisValidationResult;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.results.ExcelReportMetaResult;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.common.results.impl.ExcelReportMetaResultImpl;
import com.emistoolbox.common.results.impl.GisMetaResultImpl;
import com.emistoolbox.common.results.impl.MetaResultImpl;
import com.emistoolbox.common.results.impl.PriorityMetaResultImpl;
import com.emistoolbox.common.results.impl.ReportMetaResultImpl;
import com.emistoolbox.common.results.impl.TableMetaResultImpl;
import com.emistoolbox.common.results.impl.ValidationMetaResultImpl;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class ReportModule
{
	private String fixedDataset; 
    private String datasetName = "default"; 
    private TabPanel uiMetaResultTabs;
    private EmisToolbox toolbox;
    private EmisReportModuleData config = new EmisReportModuleData();

    private SimplePanel uiChartContainer = new SimplePanel();
    private SimplePanel uiDataContainer = new SimplePanel();
    private HTML uiDataSetName = new HTML("default");  

    private int dataBrowserButtonIndex; 
    private GlobalFilterUi uiGlobalFilter = new GlobalFilterUi();
    
    public ReportModule(final EmisToolbox toolbox, String dataset, String fixedDataset, final String showUi) 
    {
        this.toolbox = toolbox;

        this.fixedDataset = fixedDataset; 
        datasetName = dataset; 
        uiDataSetName.setHTML(dataset); 
        
        final ActionPanel actions = new ActionPanel();
        BlockingScreen screen = new BlockingScreen(actions.detachStatus()) {
            public void cleanup()
            { actions.attachStatus(); }
        };
        actions.startProgress();
        actions.setMessage(Message.messageReport().loadingDataTitle(), Message.messageReport().loadingDataMessage());

        toolbox.getService().loadData(datasetName, screen.getCallback(actions.getCallback(Message.messageReport().loadingData(), new StatusAsyncCallback<EmisReportModuleData>("Fetch data") {
            public void onSuccess(EmisReportModuleData result)
            {
            	uiGlobalFilter.setMeta(result.getModel()); 
                config = result;
                config.getModel().setDatasetName(datasetName); 
                
                ReportModule.this.init(showUi);
            }
        })));
        toolbox.setWidget(actions);
    }

    private void init(String showUi)
    {
        updateButtons();
        this.toolbox.setMessage(Message.messageReport().welcomeMessage());
        
        if ("analysis-reports".equals(showUi))
        	showReport((PdfReportConfig) null);  
        else
        	showCharts();
    }

    private void updateButtons()
    {
        final AccessLevel access = toolbox.getAccessLevel();
        
        List<Widget> menuItems = new ArrayList<Widget>();
        menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnCharts(), new ClickHandler() {
            public void onClick(ClickEvent event)
            { showCharts(); }
        }));
        
        menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnIndicators(), new ClickHandler() {
        	public void onClick(ClickEvent event)
            { showIndicators(); }
        }));
            
        if (access != AccessLevel.VIEWER)
        {
            menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnReports(), new ClickHandler() {
                public void onClick(ClickEvent event)
                { showReports(null); }
            }));
        
            menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnExcelReports(), new ClickHandler() {
                public void onClick(ClickEvent event)
                { showExcelReports(); }
            }));
        
            menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnManageData(), new ClickHandler() {
                public void onClick(ClickEvent event)
                { showLoadSave(true); }
            }));
        }
        
        dataBrowserButtonIndex = menuItems.size(); 
        menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnBrowseData(), new ClickHandler() {
            public void onClick(ClickEvent evt)
            { showDataBrowser(); } 
        })); 
        
        menuItems.add(EmisToolbox.getMenuItem(Message.messageReport().btnValidateData(), new ClickHandler() {
        	public void onClick(ClickEvent evt)
        	{ showValidateData(); } 
        }));
        
        uiDataSetName.setStyleName("spacing");
        uiDataSetName.addStyleName("value");
        uiDataSetName.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { showLoadSave(false); }
        }); 
        
        this.toolbox.setMenuItems(menuItems, uiDataSetName);
    }

    private void showIndicators()
    {
        selectButton(1);
        IndicatorConfigurationEditor editor = new IndicatorConfigurationEditor(this.config.getModel());
        if ((this.config.getReportConfig() == null) || (this.config.getReportConfig().getIndicators() == null))
            editor.set(new ArrayList<EmisIndicator>());
        else
            editor.set(this.config.getReportConfig().getIndicators());
        this.toolbox.setWidget(Message.messageReport().btnIndicators(), editor);
    }

    public void showReport(PdfReportConfig reportConfig)
    {
        showCharts();
        this.uiMetaResultTabs.selectTab(hasAnyGis() ? 3 : 2); 
        if (reportConfig != null)
        {
	        ReportMetaResultEditor reportEditor = (ReportMetaResultEditor) this.uiMetaResultTabs.getWidget(hasAnyGis() ? 3 : 2);
	        ReportMetaResult metaResult = new ReportMetaResultImpl();
	        metaResult.setReportConfig(reportConfig);
	        reportEditor.set(metaResult);
        }
    }

    private void showValidateData()
    {
    	selectButton(dataBrowserButtonIndex + 1);
    	
    	final ValidationMetaResultEditor editor = new ValidationMetaResultEditor(toolbox, config.getModel(), this.config.getReportConfig(), getRootEntities()); 
    	editor.setStyleName("fullWidth");
    	editor.set(new ValidationMetaResultImpl());; 
    	editor.addValueChangeHandler(new ValueChangeHandler<ValidationMetaResult>() {
    		public void onValueChange(final ValueChangeEvent<ValidationMetaResult> event)
    		{
    			uiDataContainer.setWidget(new HTML("Running validation ...")); 
    			toolbox.getService().runValidationList(config.getModel().getDatasetName(), event.getValue(), new StatusAsyncCallback<EmisValidationResult>("Run Validation") {
    				
					@Override
					public void onFailure(Throwable caught) 
					{
						super.onFailure(caught);
						uiDataContainer.setWidget(new HTML("Failed to run validation. <hr> " + caught.getMessage()));
					}

					@Override
					public void onSuccess(EmisValidationResult validationResult) 
					{
						super.onSuccess(validationResult);
						ValidationResultBrowser ui = new ValidationResultBrowser(toolbox); 
						ui.set(validationResult);
						
						uiDataContainer.setWidget(ui);
					}
    			}); 
    		}
    	}); 
    	
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(EmisToolbox.metaResultEditFrame(editor));
        hp.setSpacing(5);
        uiDataContainer.clear();
        hp.add(uiDataContainer);
        toolbox.setWidget(Message.messageReport().btnValidateData(), hp); 
    }
    
    
    private void showDataBrowser()
    {
        selectButton(dataBrowserButtonIndex); 

        final DataMetaResultEditor editor = new DataMetaResultEditor(toolbox, config.getModel(), this.config.getReportConfig(), getRootEntities());
        editor.setStyleName("fullWidth");
        editor.set(new MetaResultImpl());
        editor.addValueChangeHandler(new ValueChangeHandler<MetaResult>() {
            public void onValueChange(ValueChangeEvent<MetaResult> event)
            {
                EmisContext context = event.getValue().getContext(); 

                uiDataContainer.setWidget(null); 
                List<EmisEntity> entities = context.getEntities();
                if (entities == null || entities.size() == 0)
                    return; 
                
                List<EmisEnumTupleValue> dates = context.getDates();
                if (dates == null || dates.size() == 0)
                    return; 

                EmisEnumTupleValue targetDate = dates.get(0); 
                
                VerticalPanel vp = new VerticalPanel();

                StringBuffer title = new StringBuffer(); 
                for (String item : editor.getEntityPathNames())
                {
                    if (item == null)
                        break; 
                    
                    title.append(" &raquo; ").append(item); 
                }
                
                vp.add(new HTML("<b>" + (title.length() > 0 ? title.substring(9) : title.toString()) + "</b><p>")); 
                final HTML html = new HTML(Message.messageReport().dataLoading()); 
                vp.add(html); 
                uiDataContainer.setWidget(vp);
                toolbox.getService().getEntityData(datasetName, entities.get(0), targetDate,
                    new StatusAsyncCallback<String>("") {
                        public void onFailure(Throwable caught)
                        {
                            super.onFailure(caught);
                            html.setHTML(Message.messageReport().dataLoadingFailed());  
                        }

                        public void onSuccess(String result)
                        {
                            super.onSuccess(result);
                            html.setHTML(result); 
                        }
                    }
                ); 

            }
        }); 
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(EmisToolbox.metaResultEditFrame(editor));
        hp.setSpacing(5);
        uiDataContainer.clear();
        hp.add(uiDataContainer);
        toolbox.setWidget(Message.messageReport().btnBrowseData(), hp); 
    }

    private List<EmisEntity> getRootEntities()
    { 
    	EmisUser user = toolbox.getCurrentUser();  
    	if (user == null)
    		return null;
    
    	synchronized(user) { 
	    	List<String> rootEntityTypes = user.getRootEntityTypes(); 
	    	List<Integer> rootEntityIds = user.getRootEntityIds(); 
	    	if (user.getRootEntities() == null && !isEmpty(rootEntityTypes) && !isEmpty(rootEntityIds))
	    	{
	    		List<EmisEntity> result = new ArrayList<EmisEntity>();
	    		for (int i = 0; i < Math.min(rootEntityTypes.size(), rootEntityIds.size()); i++)
	    		{
	    			EmisMetaEntity entityType = NamedUtil.find(rootEntityTypes.get(i), config.getModel().getEntities()); 
	    			if (entityType == null)
	    				continue; 
	    					
	    			EmisEntity entity = new Entity();
	    			entity.setEntityType(entityType);
	    			entity.setId(rootEntityIds.get(i)); 
	    			
	    			result.add(entity); 
	    		}
	    			
	    		user.setRootEntities(result);
	    	}
	
	    	return user.getRootEntities();
    	}
    }    

    private boolean isEmpty(List items)
    { return items == null || items.size() == 0; }
    
    private void showCharts()
    {
        selectButton(0);
        uiGlobalFilter.resetValueChangeHandlers();

        if ((this.config.getReportConfig().getIndicators() == null) || (this.config.getReportConfig().getIndicators().size() == 0))
        {
            this.toolbox.setWidget(new HTML(Message.messageReport().errorNoIndicators()));
            return;
        }

        ValueChangeHandler<PdfReportConfig> showReportHandler = new ValueChangeHandler<PdfReportConfig>() {
            public void onValueChange(ValueChangeEvent<PdfReportConfig> event)
            { ReportModule.this.showReports((PdfReportConfig) event.getValue()); }
        };

        final TableMetaResultEditor editor = new TableMetaResultEditor(this.toolbox, this.config.getModel(), this.config.getReportConfig(), getRootEntities());
        uiGlobalFilter.addValueChangeHandler(new GlobalFilterValueChangeHandler<TableMetaResult>(editor));
        editor.addShowReportHandler(showReportHandler);
        editor.addValueChangeHandler(new ValueChangeHandler<TableMetaResult>() {
            public void onValueChange(ValueChangeEvent<TableMetaResult> event)
            {
                ReportModule.this.loadChart((TableMetaResult) event.getValue(), editor.getChartType(), ReportModule.this.uiChartContainer);
            }
        });

        editor.set(new TableMetaResultImpl());

        GisMetaResultEditor gisEditor = null; 
        if (hasAnyGis())
        {
            gisEditor = new GisMetaResultEditor(this.toolbox, this.config.getModel(), this.config.getReportConfig(), getRootEntities());
            uiGlobalFilter.addValueChangeHandler(new GlobalFilterValueChangeHandler<GisMetaResult>(gisEditor));
            final GisMetaResultEditor gisEditor2 = gisEditor; 
            gisEditor.addShowReportHandler(showReportHandler);
            gisEditor.addValueChangeHandler(new ValueChangeHandler<GisMetaResult>() {
                public void onValueChange(ValueChangeEvent<GisMetaResult> event)
                {
                    GisMetaResult meta = event.getValue(); 
                    ReportModule.this.loadGis(meta, gisEditor2.getMapType(), ReportModule.this.uiChartContainer); 
                }
            });
            gisEditor.setStyleName("fullWidth");
            gisEditor.set(new GisMetaResultImpl());
        }

        PriorityMetaResultEditor prioEditor = new PriorityMetaResultEditor(this.toolbox, this.config.getModel(), this.config.getReportConfig(), getRootEntities());
        uiGlobalFilter.addValueChangeHandler(new GlobalFilterValueChangeHandler<PriorityMetaResult>(prioEditor));
        prioEditor.addValueChangeHandler(new ValueChangeHandler<PriorityMetaResult>() {
            public void onValueChange(ValueChangeEvent<PriorityMetaResult> event)
            {
                ReportModule.this.loadPriorityList((PriorityMetaResult) event.getValue(), ReportModule.this.uiChartContainer);
            }
        });
        prioEditor.setStyleName("fullWidth");
        prioEditor.set(new PriorityMetaResultImpl());

        ReportMetaResultEditor reportEditor = new ReportMetaResultEditor(this.toolbox, this.config.getModel(), this.config.getReportConfig(), getRootEntities());
        uiGlobalFilter.addValueChangeHandler(new GlobalFilterValueChangeHandler<ReportMetaResult>(reportEditor));
        reportEditor.addValueChangeHandler(new ValueChangeHandler<ReportMetaResult>() {
            public void onValueChange(ValueChangeEvent<ReportMetaResult> event)
            { ReportModule.this.loadReport(event.getValue(), uiChartContainer); }
        });
        reportEditor.setStyleName("fullWidth");
        reportEditor.set(new ReportMetaResultImpl());

        ExcelReportMetaResultEditor excelReportEditor = new ExcelReportMetaResultEditor(toolbox, config.getModel(), config.getReportConfig(), getRootEntities()); 
        uiGlobalFilter.addValueChangeHandler(new GlobalFilterValueChangeHandler<ExcelReportMetaResult>(excelReportEditor));
        excelReportEditor.addValueChangeHandler(new ValueChangeHandler<ExcelReportMetaResult>() {
            public void onValueChange(ValueChangeEvent<ExcelReportMetaResult> event)
            { loadExcelReport(event.getValue(), uiChartContainer); }        
        }); 
        excelReportEditor.setStyleName("fullWidth"); 
        excelReportEditor.set(new ExcelReportMetaResultImpl()); 
        
        this.uiMetaResultTabs = new TabPanel();
        this.uiMetaResultTabs.add(editor, Message.messageReport().chartsTab());
        if (gisEditor != null)
            this.uiMetaResultTabs.add(gisEditor, Message.messageReport().mapsTab());

        this.uiMetaResultTabs.add(prioEditor, Message.messageReport().prioTab());
        this.uiMetaResultTabs.add(reportEditor, Message.messageReport().reportsTab());
        this.uiMetaResultTabs.add(excelReportEditor, Message.messageReport().excelReportsTab());
        this.uiMetaResultTabs.selectTab(0);

        VerticalPanel vp = new VerticalPanel();
        vp.add(uiGlobalFilter); 
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        vp.add(this.uiMetaResultTabs);

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(2);

        HTML btn = new HTML("[Save]");
        btn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ReportModule.this.saveMetaResult(); }
        });
        btn.addStyleName("pointer");

        buttons.add(btn);

        btn = new HTML("[Load]");
        btn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                ReportModule.this.loadMetaResult();
            }
        });
        btn.addStyleName("pointer");

        buttons.add(btn);

//        LOAD/SAVE MetaResult. 
//        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
//        vp.add(buttons); 
        
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(vp);
        hp.setSpacing(5);
        
        this.uiChartContainer.clear();
        hp.add(uiChartContainer);
//        hp.setCellWidth(editor, "200px");

        this.toolbox.setWidget(hp);
    }

    private void loadMetaResult()
    {
        final FormPanel form = new FormPanel();
        form.setAction("/emistoolbox/upload");
        form.setEncoding("multipart/form-data");
        form.setMethod("post");
        form.setWidth("300px");

        FileUpload upload = new FileUpload();
        upload.setName("file");

        PushButton btnSubmit = new PushButton("Upload");
        EmisUtils.init(btnSubmit, 80);

        PushButton btnCancel = new PushButton("Cancel");
        EmisUtils.init(btnCancel, 80);

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.add(btnSubmit);
        buttons.add(btnCancel);

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(5);
        vp.add(new Label("Please select report setting file to upload:"));
        vp.add(upload);
        vp.add(buttons);

        form.setWidget(vp);
        final BlockingScreen popup = new BlockingScreen(form);

        btnSubmit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                form.submit();
                popup.hide();
                popup.cleanup();
            }
        });
        btnCancel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                popup.hide();
                popup.cleanup();
            }
        });
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event)
            {
                String html = event.getResults();
                int pos = html.indexOf("url=");
                if (pos == -1)
                {
                    popup.hide();
                }
                String filename = html.substring(pos + 4).trim();
                Window.alert(filename);

                ReportModule.this.toolbox.getService().loadMetaResult(filename, null, new AsyncCallback<MetaResult>() {
                    public void onFailure(Throwable caught)
                    {
                        popup.hide();
                    }

                    public void onSuccess(MetaResult result)
                    {
                        ReportModule.this.set(result);
                        popup.hide();
                    }
                });
            }
        });
    }

    public MetaResult get()
    {
        int tabIndex = this.uiMetaResultTabs.getTabBar().getSelectedTab();
        if (tabIndex == -1)
        {
            return null;
        }
        Widget w = this.uiMetaResultTabs.getWidget(tabIndex);
        if ((w instanceof MetaResultEditor))
            return ((MetaResultEditor) w).get();

        return null;
    }

    public void set(MetaResult metaResult)
    {
        if (metaResult == null)
        {
            return;
        }
        if ((metaResult instanceof TableMetaResult))
            selectTab((TableMetaResult) metaResult);
        else if ((metaResult instanceof GisMetaResult))
            selectTab((GisMetaResult) metaResult);
        else if ((metaResult instanceof PriorityMetaResult))
            selectTab((PriorityMetaResult) metaResult);
        else if ((metaResult instanceof ReportMetaResult))
            selectTab((ReportMetaResult) metaResult);
    }

    private void selectTab(TableMetaResult metaResult)
    {
        this.uiMetaResultTabs.selectTab(0);
    }

    private void selectTab(GisMetaResult metaResult)
    {
        this.uiMetaResultTabs.selectTab(1);
    }

    private void selectTab(PriorityMetaResult metaResult)
    {
        if (hasAnyGis())
            this.uiMetaResultTabs.selectTab(2);
        else
            this.uiMetaResultTabs.selectTab(1);
    }

    private void selectTab(ReportMetaResult metaResult)
    {
        if (hasAnyGis())
            this.uiMetaResultTabs.selectTab(3);
        else
            this.uiMetaResultTabs.selectTab(2);
    }

    private void saveMetaResult()
    {
        DownloadPanel download = new DownloadPanel(new HTML("Preparing report settings for download"), "Report Settings");
        this.toolbox.getService().saveMetaResult(get(), download.getDownloadCallback(null));
    }

    private boolean hasAnyGis()
    {
        for (EmisMetaEntity entity : this.config.getModel().getEntities())
        {
            if (entity.getGisType() != EmisMetaEntity.EmisGisType.NONE)
            {
                return true;
            }
        }
        return false;
    }

    public void showReports(PdfReportConfig report)
    {
        selectButton(2);
        if ((this.config.getReportConfig().getIndicators() == null) || (this.config.getReportConfig().getIndicators().size() == 0))
        {
            this.toolbox.setWidget(Message.messageReport().btnReports(), new HTML(Message.messageReport().errorNoIndicators()));
            return;
        }

        PdfReportConfigListEditor editor = new PdfReportConfigListEditor(this.config.getModel(), this);
        this.toolbox.setWidget(Message.messageReport().btnReports(), editor);
        editor.set(this.config.getReportConfig().getReports());
        if (report != null)
            editor.selectReport(report);
    }
    
    public void showExcelReports()
    {
        selectButton(3); 
        
        ExcelReportListEditor editor = new ExcelReportListEditor(toolbox, datasetName, config.getModel(), config.getReportConfig()); 
        editor.set(this.config.getReportConfig().getExcelReports()); 
        this.toolbox.setWidget(Message.messageReport().btnExcelReports(), editor);
    }

    private void showLoadSave(boolean withSave)
    {
        selectButton(4);

        ManageReportDatasetDialog ui = new ManageReportDatasetDialog(withSave ? datasetName : null, fixedDataset, toolbox, config); 
        ui.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				datasetName = event.getValue(); 
				uiDataSetName.setHTML(event.getValue()); 

				ActionPanel actions = new ActionPanel(); 
		        toolbox.setWidget(Message.messageReport().btnManageData(), actions);
				loadReportData(actions); 
			}
        }); 
        
        this.toolbox.setWidget(Message.messageReport().btnManageData(), ui);
    }
    
    private void loadReportData(final ActionPanel actions)
    {
        BlockingScreen screen = new BlockingScreen(actions.detachStatus()) {
            public void cleanup()
            {
                actions.attachStatus();
            }
        };
        actions.startProgress();
        actions.setMessage(Message.messageReport().loadingConfigTitle(), Message.messageReport().loadingConfigMessage());

        ReportModule.this.toolbox.getService().loadReportData(datasetName, screen.getCallback(actions.getCallback(Message.messageReport().loadingConfig(), new AsyncCallback<EmisReportModuleData>() {
            public void onFailure(Throwable caught)
            {}

            public void onSuccess(EmisReportModuleData result)
            {
            	uiGlobalFilter.setMeta(result.getModel());
                config = result;
                config.getModel().setDatasetName(datasetName);  
                actions.setMessage(Message.messageReport().dataLoadedTitle(), Message.messageReport().dataLoadedMessage(), true);
            }
        })));
    }
    
    private void selectButton(int index)
    {
        Widget w = this.toolbox.getWidget();
        if (w instanceof IndicatorConfigurationEditor)
            config.getReportConfig().setIndicators(((IndicatorConfigurationEditor) toolbox.getWidget()).get());
        else if (w instanceof PdfReportConfigListEditor)
            config.getReportConfig().setReports(((PdfReportConfigListEditor) toolbox.getWidget()).get());
        else if (w instanceof ExcelReportListEditor)
            config.getReportConfig().setExcelReports(((ExcelReportListEditor) toolbox.getWidget()).get()); 
        
        this.toolbox.selectButton(index);
    }

    private void loadChart(final TableMetaResult metaResult, int chartStyle, final SimplePanel uiChartContainer)
    {
        uiChartContainer.setWidget(new HTML(Message.messageReport().chartLoading()));
        metaResult.setGlobalFilter(uiGlobalFilter.createCopy());
        this.toolbox.getService().getRenderedResult(datasetName, metaResult, chartStyle, new StatusAsyncCallback<String[]>(Message.messageReport().collectingResult()) {
            public void onSuccess(String[] result)
            {
                super.onSuccess(result);
                
        		String highchartConfigUrl = null; 
                String html = MetaResultDimensionUtil.getTitle(metaResult);
                if (result[0] != null)
                {
                	if (result[0].endsWith(".json"))
                	{
                		highchartConfigUrl = result[0]; 
                		html = html + "<div id='highchartChart'></div>";
                	}
                	else
                		html = html + "<img src='/emistoolbox/content?chart=" + result[0] + "'>";
                }
                else
                    html = html + "<b>" + Message.messageReport().errorFailedToRenderChart() + "</b>";
                html = html + "<p>";
                if (result[1] != null)
                    html = html + result[1];
                else
                    html = html + "<b>" + Message.messageReport().errorFailedToRenderTable() + "</b>";

                html = html + "<p>";
                if (result.length > 2 && result[2] != null)
                    html = html + "<a href='/emistoolbox/content?report=" + result[2] + "'><img src='css/icon_xls.gif'></a>";

                uiChartContainer.setWidget(new HTML(html));
                if (highchartConfigUrl != null)
                	initHighcharts(highchartConfigUrl); 
            }
        });
    }

    private native void initHighcharts(String highchartConfigUrl) /*-{
		$wnd.jQuery.ajax("/emistoolbox/content?chart=" + highchartConfigUrl).done(function(data) { 
		console.log("JSON", data); 
		if (!data.tooltip)
			data.tooltip = {}; 
			
		data.tooltip.formatter = function() {
			$wnd.jQuery("table.emisResult td").removeClass("selected");
			var selector = "table.emisResult td[data-series='" + this.series.name + "'][data-x='" + this.x + "']"; 
			console.log(this, selector); 
			$wnd.jQuery(selector).addClass("selected"); 
			
			if (this.series.name == "default")
				return this.x + ": " + this.y; 
			else
				return this.series.name + "<br />" + this.x + ": " + this.y; 
		};

		$wnd.jQuery("#highchartChart").highcharts(data); 
		}); 
	}-*/;
    
    private void appendGisGmlHtml(StringBuffer html, String[] result)
    {
        html.append("-- gml --\n\n");  
        appendFileNames(html, result); 
    }
    
    private void appendGisGeoJsonHtml(StringBuffer html, String[] result)
    {
        OpenLayersUtil.showGeojsonMap(result[0]); 
        html.append("-- geojson --\n\n"); 
    }
    
    private void appendGisShapeHtml(StringBuffer html, String[] result)
    { 
        StringBuffer url = new StringBuffer("./map/openLayers?base=image&dataset=" + datasetName);
        if (result[result.length - 1] != null)
            url.append(result[result.length - 2]);
        
        int offset = url.length(); 
        for (String file : result)
        {
            if (file.endsWith(".shp"))
                url.insert(offset, "&layer=" + file);
        }

        html.append("<iframe width='750' height='500' id='openLayers' src='" + url.toString() + "'></iframe>");
        html.append("<br><a href='" + url.toString() + "' target='_blank'>Full Screen</a>"); 
    }
    
    private void appendFileNames(StringBuffer html, String[] result)
    {
        for (String r : result)
        {
            html.append(r); 
            html.append("\n\n"); 
        }
    }
    
    private void appendGisImageHtml(StringBuffer html, String[] result)
    {
        if (result.length != 5)
            return; 

        boolean usesImageMap = result[1] != null; 
        html.append("<img src='/emistoolbox/content?chart="); 
        html.append(result[0]); 
        html.append("' border='0'"); 
        if (usesImageMap)
        {
            html.append(" usemap='#gismap'><map name='gismap'>");
            html.append(result[1]); 
            html.append("</map>"); 
        }
        else
            html.append(">");
    }

    private void appendGisHtml(StringBuffer html, String[] result, String format)
    {
        if (GisMetaResultEditor.MAP_TYPE_INTERACTIVE.equals(format))
            appendGisShapeHtml(html, result); 
        else
            appendGisImageHtml(html, result); 
    }
    
    private void loadGis(final GisMetaResult gisMetaResult, final String mapType, final SimplePanel uiContainer)
    {
        uiContainer.setWidget(new HTML(Message.messageReport().chartLoading()));
        AsyncCallback<String[]> callback = new StatusAsyncCallback<String[]>(Message.messageReport().collectingResult()) {

            public void onSuccess(String[] result)
            {
                super.onSuccess(result);
                StringBuffer html = new StringBuffer(); 
                html.append("<center>"); 
                html.append(MetaResultDimensionUtil.getTitle(gisMetaResult)); 
                html.append("</center>");
                
                if (result != null && result.length > 2)
                {
                    html.append("<table cellspacing='3'><tr valign='top'><td>"); 
                    appendGisHtml(html, result, mapType);
                    html.append("</td><td>"); 
                    html.append(result[result.length - 2]); 
                    html.append("</td></tr></table><p><center>");
                    html.append(result[result.length - 1]);
                    html.append("</center>");
                }
                else
                {
                    html.append("<b>"); 
                    html.append(Message.messageReport().errorFailedToRenderGis()); 
                    html.append("</b>");
                }
                
                uiContainer.setWidget(new HTML(html.toString()));
            }
        };
        
        gisMetaResult.setGlobalFilter(uiGlobalFilter.createCopy());
        this.toolbox.getService().getRenderedGisResult(datasetName, gisMetaResult, mapType, callback); 
    }

    private void loadPriorityList(final PriorityMetaResult prioMetaResult, final SimplePanel uiContainer)
    {
        uiContainer.setWidget(new HTML(Message.messageReport().chartLoading()));
        prioMetaResult.setGlobalFilter(uiGlobalFilter.createCopy());
        this.toolbox.getService().getPriorityList(datasetName, prioMetaResult, new StatusAsyncCallback<List<PriorityListItem>>(Message.messageReport().collectingResult()) {
            public void onSuccess(List<PriorityListItem> result)
            {
                super.onSuccess(result);
                uiContainer.setWidget(new PriorityListTable(ReportModule.this.toolbox, prioMetaResult, result));
            }
        });
    }

    private void loadReport(ReportMetaResult reportMetaResult, final SimplePanel uiContainer)
    {
        uiContainer.setWidget(new HTML(Message.messageReport().reportLoading()));
        reportMetaResult.setGlobalFilter(uiGlobalFilter.createCopy()); 
        this.toolbox.getService().getRenderedReportResult(datasetName, reportMetaResult, new StatusAsyncCallback<String>(Message.messageReport().collectingResult()) {
            public void onSuccess(String result)
            {
                super.onSuccess(result);
                String html = null;
                if (result != null)
                    html = "<a href='/emistoolbox/content?report=" + result + "' class='pdf' target='_blank'>" + Message.messageReport().download() + "</a>";
                else
                {
                    html = "<b>" + Message.messageReport().errorFailedToRenderReport() + "</b>";
                }
                uiContainer.setWidget(new HTML(html));
            }
        });
    }
    
    private void loadExcelReport(ExcelReportMetaResult metaResult, final SimplePanel uiContainer)
    {
        uiContainer.setWidget(new HTML(Message.messageReport().reportLoading())); 
        metaResult.setGlobalFilter(uiGlobalFilter.createCopy());
        toolbox.getService().getRenderedExcelReportResult(datasetName, metaResult, config.getReportConfig().getIndicators(), new StatusAsyncCallback<String>(Message.messageReport().collectingResult()) {
            public void onSuccess(String result)
            {
                super.onSuccess(result);
                String html = null;
                if (result != null)
                    html = "<a href='/emistoolbox/content?report=" + result + "' class='xls' target='_blank'>" + Message.messageReport().download() + "</a>";
                else
                    html = "<b>" + Message.messageReport().errorFailedToRenderReport() + "</b>";

                uiContainer.setWidget(new HTML(html));
            }
        });
    }
}

class GlobalFilterValueChangeHandler<T extends MetaResult> implements ValueChangeHandler<EmisContext>
{
	private MetaResultEditor<T> editor; 
	
	public GlobalFilterValueChangeHandler(MetaResultEditor<T> editor)
	{ this.editor = editor; } 
	
	@Override
	public void onValueChange(ValueChangeEvent<EmisContext> event) 
	{ editor.setGlobalFilter(event.getValue()); }
}
