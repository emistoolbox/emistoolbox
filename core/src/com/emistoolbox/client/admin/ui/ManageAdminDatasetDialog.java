package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.AdminModule;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusRefresh;
import com.emistoolbox.client.ui.BlockingScreen;
import com.emistoolbox.client.ui.StatusPanel;
import com.emistoolbox.client.ui.user.EmisUserListEditor;
import com.emistoolbox.common.EmisAdminModuleData;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ManageAdminDatasetDialog extends HorizontalPanel 
{
	private ListBoxWithUserObjects<String> uiDatasets = new ListBoxWithUserObjects<String>();
	private TabPanel uiTabs = new TabPanel();
	private EmisUserListEditor uiUsers;
	private StatusPanel uiStatus = new StatusPanel();
	private PushButton btnLoad;
	private PushButton btnSave; 
	private ListBoxWithUserObjects<EmisEntityDbMap> uiTasks; 
	
	private EmisToolbox toolbox;
	private EmisAdminModuleData config;
	private AdminModule adminModule;

	public ManageAdminDatasetDialog(AdminModule adminModule, EmisToolbox toolbox, EmisAdminModuleData config) 
	{
		this.toolbox = toolbox;
		this.config = config;
		this.adminModule = adminModule;

		uiUsers = new EmisUserListEditor(toolbox);
		uiUsers.set(toolbox.getUsers());

		uiTabs.add(getDatasetTab(), "Dataset");
		uiTabs.add(getImportTab(), Message.messageAdmin().btnImport());
		uiTabs.add(uiUsers, Message.messageAdmin().btnUsers());
		uiTabs.selectTab(0);
		uiTabs.addStyleName("wide");

		add(uiTabs);
		add(uiStatus); 
		
		init(config); 
		btnSave.setText(Message.messageAdmin().btnSave() + " '" + adminModule.getCurrentDataset() + "'");
	}

	private void setConfig(EmisAdminModuleData config)
	{ this.config = config; }

	public void init(EmisAdminModuleData config)
	{
		this.config = config;

		String currentDataset = adminModule.getCurrentDataset(); 
		btnSave.setVisible(currentDataset != null && !currentDataset.equals(""));

		uiTasks.clear(); 
		for (EmisEntityDbMap mapping : config.getMapping().getEntityMappings())
			uiTasks.add(mapping.toString(), mapping);
	}
	
	private Widget getDatasetTab() 
	{
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(3);
		PushButton btn = null;

		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(3);

		// Save Dataset
		btn = new PushButton(Message.messageAdmin().btnSave(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ doSave(); }
		});
		btn.setStyleName("button24"); 
		hp.add(btn);
		btnSave = btn; 

		// New dataset
		btn = new PushButton("New", new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ doNew(); }
		});
		EmisUtils.init(btn, 80);
		hp.add(btn);

		// Clear dataset
		btn = new PushButton(Message.messageAdmin().btnClearAll(),
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						doClearAll();
					}
				});
		EmisUtils.init(btn, 80);
		hp.add(btn);

		vp.add(hp);

		// Load dataset
		vp.add(new HTML("<hr>")); 
		
		HTML html = new HTML("Load Dataset"); 
		html.setStyleName("sectionBlue"); 
		vp.add(html);
		uiDatasets.setVisibleItemCount(10);
		uiDatasets.setWidth("200px");
		vp.add(uiDatasets);
		btnLoad = new PushButton("Load", new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ doLoad(uiDatasets.getUserObject()); }
		});
		btnLoad.setEnabled(false);
		updateLoadUi();
		EmisUtils.init(btnLoad, 80);
		vp.add(btnLoad);

		return vp;
	}
	
	public void doLoad(String newDataset)
	{ ManageAdminDatasetDialog.doLoad(newDataset, adminModule, toolbox, this); }
	
	public static void doLoad(final String newDataset, final AdminModule adminModule, final EmisToolbox toolbox, final ManageAdminDatasetDialog thisDialog)
	{
		StatusPanel status = new StatusPanel();
		BlockingScreen screen = new BlockingScreen(status);

		status.startProgress();
		status.setMessage(Message.messageAdmin().loadingDataTitle(), Message.messageAdmin().loadingDataMessage());

		AsyncCallback<EmisAdminModuleData> callback = new AsyncCallback<EmisAdminModuleData>() {
			public void onFailure(Throwable caught)
			{ 
				if (thisDialog != null)
					thisDialog.setMessage("Load Dataset", "Failed to load dataset '" + newDataset + "'", false); 
			}

			public void onSuccess(EmisAdminModuleData result)
			{
				if (thisDialog != null)
					thisDialog.setConfig(result); 

				result.getModel().setDatasetName(newDataset);
				adminModule.setNewDataset(newDataset, result);

				if (thisDialog != null)
					thisDialog.setMessage("Load Dataset", "Dataset '" + newDataset + "' loaded!", true);
			}
		};
		
		if (thisDialog != null)
			toolbox.getService().loadAdminData(newDataset, screen.getCallback(thisDialog.uiStatus.getCallback(Message.messageAdmin().loadingDataCallback(), callback)));
		else
			toolbox.getService().loadAdminData(newDataset, screen.getCallback(callback));
	}

	private void doNew() {
		commitUsers();

		final String newDataset = Window.prompt("Enter name of new data set", "");
		if (newDataset == null || newDataset.equals(""))
			return;

		toolbox.getService().createDataSet(newDataset, new AsyncCallback<EmisAdminModuleData>() {
			public void onFailure(Throwable caught) 
			{ setMessage("New Dataset", "Failed to create new dataset: " + caught.getMessage(), false); }

			public void onSuccess(EmisAdminModuleData result) 
			{
				config = result; 
				
				result.getModel().setDatasetName(newDataset);
				adminModule.setNewDataset(newDataset, result);
				setMessage("New Dataset", "New dataset created.", true);
			}
		});
	}

	private Widget getImportTab() 
	{
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(3);
		
		HTML html = new HTML("Full Import:"); 
		html.setStyleName("sectionBlue"); 
		vp.add(html);

		PushButton btn = new PushButton(Message.messageAdmin().btnImport(), new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ importData(null, true, true); }
		});

		EmisUtils.init(btn, 80);
		vp.add(btn);
		vp.add(new HTML("<hr>"));

		html = new HTML("Test Import:"); 
		html.setStyleName("sectionBlue");
		vp.add(html); 
		
		final CheckBox uiWithGis = new CheckBox("import GIS data.");
		uiWithGis.setValue(Boolean.valueOf(false));
		vp.add(uiWithGis);

		final CheckBox uiWithHierarchy = new CheckBox("import hierarchy data.");
		uiWithHierarchy.setValue(Boolean.valueOf(false));
		vp.add(uiWithHierarchy);

		uiTasks = new ListBoxWithUserObjects<EmisEntityDbMap>();
		uiTasks.setVisibleItemCount(20);
		uiTasks.setWidth("500px");
		vp.add(uiTasks);

		btn = new PushButton("Test Import", new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ importData((EmisEntityDbMap) uiTasks.getUserObject(), uiWithGis.getValue().booleanValue(), uiWithHierarchy.getValue().booleanValue()); }
		});
		EmisUtils.init(btn, 80);
		vp.add(btn);

		return vp;
	}

	public void importData(EmisEntityDbMap testEntityMapping, boolean withGis, boolean withHierarchy) 
	{
		commitUsers();

		StatusPanel status = new StatusPanel(); 
		BlockingScreen screen = new BlockingScreen(new StatusPanel()); 

		status.startProgress();
		status.setMessage(Message.messageAdmin().importingDataTitle(), Message.messageAdmin().importingDataMessage());

		AsyncCallback<Integer> callback = screen.getCallback(new AsyncCallback<Integer>() {
			public void onFailure(Throwable caught) 
			{ setMessage("Data Import", "Import failed: " + caught.getMessage(), false);}
			
			public void onSuccess(Integer resultId) 
			{
				StatusRefresh refresh = new StatusRefresh(resultId.intValue(), adminModule.getCurrentDataset(), uiStatus, toolbox.getService());
				refresh.request();
			}
		});

		if (testEntityMapping == null)
			this.toolbox.getService().importData(adminModule.getCurrentDataset(), config, callback);
		else
			this.toolbox.getService().testImportData(adminModule.getCurrentDataset(), config, testEntityMapping, withGis, withHierarchy, callback);
	}

	public void commitUsers() 
	{ toolbox.setUsers(uiUsers.get()); }

	private void doClearAll() 
	{
		commitUsers();
		if (Window.confirm(Message.messageAdmin().confirmDelete())) 
		{
			config = new EmisAdminModuleData();
			adminModule.setNewDataset(adminModule.getCurrentDataset(), config);
			setMessage(Message.messageAdmin().clearModelTitle(), Message.messageAdmin().clearModelMessage(), true);
		}
	}

	private void doSave() 
	{
		commitUsers();
		if (adminModule.isEmpty(config)) 
		{
			Window.alert("Your model and mapping are empty. No saving possible.");
			return;
		}
		
		StatusPanel status = new StatusPanel();
		BlockingScreen screen = new BlockingScreen(status);

		status.startProgress();
		status.setMessage(Message.messageAdmin().savingDataTitle(), Message.messageAdmin().savingDataMessage());

		toolbox.getService().saveAdminData(adminModule.getCurrentDataset(), config, screen.getCallback(uiStatus.getCallback(Message.messageAdmin().savingDataCallback(), (AsyncCallback<Void>) null)));
	}

	private void updateLoadUi() 
	{
		String fixedDataset = adminModule.getFixedDataset(); 
		if (fixedDataset != null)
		{
			uiDatasets.add(fixedDataset, fixedDataset);
			return; 
		}
		
		toolbox.getService().getDataSets(true, new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) 
			{ setMessage("Dataset List", "Failed to retrieve list of datasets: " + caught.getMessage(), false); }

			public void onSuccess(String[] datasets) 
			{
				for (String dataset : datasets)
					uiDatasets.add(dataset, dataset);
				btnLoad.setEnabled(true);
			}
		});
	}
	
	private void setMessage(String title, String subtitle, boolean success)
	{ uiStatus.setMessage(title,  subtitle, success); }
}
