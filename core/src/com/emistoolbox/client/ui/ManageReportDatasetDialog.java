package com.emistoolbox.client.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.EmisReportModuleData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageReportDatasetDialog extends FlexTable implements HasValueChangeHandlers<String>
{
    private ListBoxWithUserObjects<String> uiDatasets = new ListBoxWithUserObjects<String>();
    private StatusPanel uiStatus = new StatusPanel(); 
    private PushButton uiLoadButton; 
    
    private String dataset; 
    private EmisToolbox toolbox;
    private EmisReportModuleData config; 
    
    public ManageReportDatasetDialog(String dataset, String fixedDataset, EmisToolbox toolbox, EmisReportModuleData config)
    {
    	this.dataset = dataset; 
    	this.config = config; 
    	this.toolbox = toolbox; 
    	
        uiDatasets.setVisibleItemCount(10);
        uiDatasets.setStyleName("fullWidth");

        HorizontalPanel buttons = new HorizontalPanel(); 
        buttons.setSpacing(5); 

        PushButton saveButton = null; 
        if (dataset != null && !dataset.equals(""))
        {
            saveButton = new PushButton(Message.messageReport().btnSave() + " '" + dataset + "'", new ClickHandler() {
            	@Override
            	public void onClick(ClickEvent event) 
            	{ doSave(); }
            }); 	

            EmisUtils.init(saveButton, 120); 
        }
        
        uiLoadButton = new PushButton(Message.messageReport().btnLoad(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ doLoad(); }
        }); 
        EmisUtils.init(uiLoadButton, 80); 
        uiLoadButton.setEnabled(false);
        
        getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_TOP);
        setCellSpacing(3);

        int row = 0; 
        if (saveButton != null)
        {
        	VerticalPanel savePanel = new VerticalPanel();
        	HTML title = new HTML("Save Dataset");
        	title.setStyleName("sectionBlue");
        	savePanel.add(title); 
        	savePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        	savePanel.add(saveButton); 
        	setWidget(row, 0, EmisToolbox.metaResultEditFrame(savePanel)); 
        	row++; 
        }

    	VerticalPanel loadPanel = new VerticalPanel(); 
        loadPanel.setStyleName("fullWidth");
    	loadPanel.setSpacing(3);
    	HTML loadTitle = new HTML("Load Dataset"); 
    	loadTitle.setStyleName("sectionBlue");
    	loadPanel.add(loadTitle); 
        loadPanel.add(uiDatasets); 
    	loadPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        loadPanel.add(uiLoadButton);
        setWidget(row, 0, EmisToolbox.metaResultEditFrame(loadPanel));

        setWidget(0, 1, uiStatus); 
        getCellFormatter().setVerticalAlignment(0,  1, HasVerticalAlignment.ALIGN_TOP);
        row++; 

        loadDatasets(fixedDataset); 
    }

    @Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) 
    { return addHandler(handler, ValueChangeEvent.getType()); }

	private void doSave()
    {
        BlockingScreen screen = new BlockingScreen(uiStatus) {
            public void cleanup()
            {}
        };

        uiStatus.startProgress();
        uiStatus.setMessage(Message.messageReport().dataSavingTitle(), Message.messageReport().dataSavingMessage());
        toolbox.getService().saveReportData(dataset, config, screen.getCallback(new AsyncCallback<Void>() {
            public void onFailure(Throwable caught)
            { uiStatus.setMessage(Message.messageReport().dataSavedTitle(), Message.messageReport().dataLoadingFailed(), false); }

            public void onSuccess(Void result)
            { uiStatus.setMessage(Message.messageReport().dataSavedTitle(), Message.messageReport().dataSavedMessage(), true); }
        }));
    }

    private void loadDatasets(String fixedDataset)
    {
    	uiDatasets.clear(); 
    	if (fixedDataset != null)
    	{
    		uiDatasets.add(fixedDataset, fixedDataset);
    		return; 
    	}
    	
    	uiStatus.startProgress(); 
    	uiStatus.setMessage("",  "Loading list of datasets.");

    	toolbox.getService().getDataSets(false, new AsyncCallback<String[]>() {
			@Override
			public void onFailure(Throwable caught) 
			{
				uiStatus.setMessage("", "Failed to retrieve list of datasets", false);
				uiLoadButton.setEnabled(false); 
			}

			@Override
			public void onSuccess(String[] result) 
			{
				uiStatus.setMessage("", "", true); 
				uiLoadButton.setEnabled(true);
				
				for (String item : result)
					uiDatasets.add(item, item);
			}
    	});
    }
    
    private void doLoad()
    {
		final String newDataset = uiDatasets.getUserObject(); 
		if (newDataset == null || newDataset.equals(""))
			return; 

    	BlockingScreen screen = new BlockingScreen(uiStatus); 
    	
    	uiStatus.startProgress(); 
    	uiStatus.setMessage("Changing dataset", "Changing dataset");

    	toolbox.getService().changeDataSet(newDataset, screen.getCallback(new AsyncCallback<Boolean>() {
    		public void onFailure(Throwable caught)
            {}

    		public void onSuccess(Boolean result)
    		{
    			if (!result)
    			{
    				Window.alert("Failed to load data for DataSet: " + newDataset); 
    				return; 
    			}

    			ValueChangeEvent.fire(ManageReportDatasetDialog.this, newDataset);
    			
    			uiStatus.setMessage("", "Dataset loaded successfully", true);  
    		}
    	})); 
    }
}
