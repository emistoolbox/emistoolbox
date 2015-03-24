package com.emistoolbox.client.ui.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.admin.ui.TreeHierarchyBrowser;
import com.emistoolbox.client.util.ui.CenteredPositionCallback;
import com.emistoolbox.common.EmisReportModuleData;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EmisUserEditor extends FlexTable implements EmisEditor<EmisUser>
{
	private static Map<String, String> entityNames = new HashMap<String, String>(); 
	
    private TextBox uiUsername = new TextBox(); 
    private PasswordTextBox uiPassword = new PasswordTextBox();
    private ListBox uiAccessLevel = new ListBox(); 
    private ListBoxWithUserObjects<EmisMeta> uiDatasets = new ListBoxWithUserObjects<EmisMeta>(); 

    private VerticalPanel uiRootEntityPanel = new VerticalPanel(); 
    private ListBoxWithUserObjects<String> uiRootEntities = new ListBoxWithUserObjects<String>(); 
    private PushButton btnAddRootEntity = new PushButton("Add"); 
    private PushButton btnDelRootEntity = new PushButton("Del"); 

    private EmisUser user = null; 
    
    private EmisToolbox toolbox; 
    
    public EmisUserEditor(EmisToolbox toolbox)
    {
    	this.toolbox = toolbox; 
    	
        uiAccessLevel.addItem(Message.messageAdmin().usrViewer(), AccessLevel.VIEWER.toString());  
        uiAccessLevel.addItem(Message.messageAdmin().usrReportAdmin(), AccessLevel.REPORT_ADMIN.toString()); 
        uiAccessLevel.addItem(Message.messageAdmin().usrSystemAdmin(), AccessLevel.SYSTEM_ADMIN.toString()); 

        int row = 0; 
        getFlexCellFormatter().setColSpan(row, 0, 2); 
        getCellFormatter().setStyleName(row, 0, "sectionBar");
        row++; 
        
        setHTML(row, 0, Message.messageAdmin().usrUsername() + ":"); 
        getCellFormatter().setStyleName(row, 0, "sectionBlue");
        setWidget(row, 1, uiUsername); 
        row++; 
        
        setHTML(row, 0, Message.messageAdmin().usrPassword() + ":"); 
        getCellFormatter().setStyleName(row, 0, "sectionBlue");
        setWidget(row, 1, uiPassword); 
        row++; 
        
        setHTML(row, 0, Message.messageAdmin().usrAccess() + ":"); 
        getCellFormatter().setStyleName(row, 0, "sectionBlue");
        setWidget(row, 1, uiAccessLevel); 
        row++; 
        
        setHTML(row, 0, "Dataset:"); 
        getCellFormatter().setStyleName(row, 0, "sectionBlue");
        setWidget(row, 1, uiDatasets); 
        row++; 

        Label title = new Label(""); 
        title.setStyleName("sectionBlue");
        
        HorizontalPanel hp = new HorizontalPanel(); 
        hp.setSpacing(1);
        hp.add(btnAddRootEntity);
        hp.add(btnDelRootEntity);
        
        uiRootEntityPanel.add(title);
        uiRootEntityPanel.add(uiRootEntities);
        uiRootEntityPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        uiRootEntityPanel.add(hp);
        
        uiRootEntities.setVisibleItemCount(10); 
        uiRootEntities.setWidth("150px");
        
        getCellFormatter().setStyleName(row, 0, "sectionBlue"); 
        setWidget(row, 1, uiRootEntityPanel); 
        row++; 
        
        uiDatasets.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) 
			{
				uiRootEntities.clear(); 
				updateRootEntitiesUi(); 
			}
        }); 
        
        btnAddRootEntity.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addRootEntity(); 
			}
        }); 
        
        btnDelRootEntity.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int item = uiRootEntities.getSelectedIndex(); 
				if (item != -1)
					uiRootEntities.removeItem(item);
			}
        }); 
    }
    
    private void updateRootEntitiesUi()
    { uiRootEntityPanel.setVisible(uiDatasets.getSelectedIndex() > 0); }
    
    public void setDatasets(String[] datasets)
    {
		uiDatasets.addItem("(all)", (EmisMeta) null);
    	for (String dataset : datasets)
    		uiDatasets.addItem(dataset, (EmisMeta) null);
    	
    	uiRootEntities.clear(); 
    	
    	updateRootEntitiesUi(); 
    }    	

    public void commit()
    {
        if (user == null)
            user = new EmisUser(); 
        
        user.setUsername(uiUsername.getText()); 
        user.setPassword(uiPassword.getText()); 
        user.setDataset(getCurrentDataset());   

        String level = GwtUtils.getListValue(uiAccessLevel); 
        if (level == null)
            user.setAccessLevel(null); 
        else
            user.setAccessLevel(AccessLevel.valueOf(level));  
        
        user.clearRootEntities(); 
        if (user.getDataset() != null && !user.getDataset().equals(""))
        {
        	for (int i = 0; i < uiRootEntities.getItemCount(); i++) 
        	{
        		String item = uiRootEntities.getUserObject(i);
        		int pos = item.indexOf(":"); 
        		if (pos == -1)
        			continue; 
        		
        		user.addRootEntity(item.substring(0, pos), Integer.parseInt(item.substring(pos + 1)));
        	}
        	
        }
    }

    private String getCurrentDataset()
    {
    	int i = uiDatasets.getSelectedIndex();
    	if (i < 1)
    		return null; 
    	
    	return uiDatasets.getValue(i); 
    }
    
    public EmisUser get()
    {
        commit(); 
        return user; 
    }

    public void set(EmisUser user)
    {
        this.user = user; 
        uiRootEntityPanel.setVisible(false);

        if (user != null)
        {
        	setHTML(0, 0, "User '" + user.getUsername() + "':"); 
            uiUsername.setText(user.getUsername()); 
            uiPassword.setText(user.getPassword());
            GwtUtils.setListValue(uiAccessLevel, user.getAccessLevel().toString()); 
            
            if (user.getDataset() == null)
            	uiDatasets.setSelectedIndex(0); 
            else
            {
            	for (int i = 1; i < uiDatasets.getItemCount(); i++) 
            	{
            		if (uiDatasets.getValue(i).equals(user.getDataset()))
            			uiDatasets.setSelectedIndex(i);
            	}
            	
            	uiRootEntityPanel.setVisible(true);
            	uiRootEntities.clear(); 

            	List<String> entityTypes = user.getRootEntityTypes(); 
            	List<Integer> entityIds = user.getRootEntityIds(); 
            	if (entityTypes != null && entityIds != null)
            	{
            		List<String> ids = new ArrayList<String>(); 
            		for (int i = 0; i < Math.min(entityTypes.size(), entityIds.size()); i++)
            		{
            			String id = entityTypes.get(i) + ":" + entityIds.get(i); 
            			uiRootEntities.add(id, id);
            			ids.add(id); 
            		}
            		
            		updateEntityNames(); 
            	}
            }
        }
        else
        {
            uiUsername.setText(""); 
            uiPassword.setText(""); 
            uiAccessLevel.setSelectedIndex(-1); 
            uiRootEntities.clear(); 
        }
        
        updateRootEntitiesUi(); 
    }
    
    private void addRootEntity()
    {
    	final int datasetIndex = uiDatasets.getSelectedIndex();
    	if (datasetIndex < 1)
    		return; 
    	
    	final String dataset = getCurrentDataset();
    	
    	// Show in popup menu.
    	//
    	final PopupPanel popup = new PopupPanel();
    	popup.setWidget(new Label("Loading..."));
    	popup.setModal(true);
    	popup.setWidth("400px");
    	popup.setHeight("250px");
    	popup.setAutoHideEnabled(true);
    	
     	popup.setPopupPositionAndShow(new CenteredPositionCallback(popup)); 

     	if (uiDatasets.getUserObject(datasetIndex) != null)
     		showBrowser(uiDatasets.getUserObject(datasetIndex), popup); 
     	else 
     	{
	     	toolbox.getService().loadData(dataset, new StatusAsyncCallback<EmisReportModuleData>("Loading model for '" + dataset + "'") {
	     		@Override
				public void onSuccess(EmisReportModuleData result) 
				{
					super.onSuccess(result);
					result.getModel().setDatasetName(dataset);

					uiDatasets.setUserObject(datasetIndex, result.getModel());
					showBrowser(result.getModel(), popup); 
				}
	     	}); 
     	}
    }
    
    private void showBrowser(EmisMeta model, final PopupPanel popup)
    {
    	final String dataset = getCurrentDataset(); 
    	
    	final TreeHierarchyBrowser uiTreeBrowser = new TreeHierarchyBrowser(toolbox, null); 
    	final ListBoxWithUserObjects<EmisMetaHierarchy> uiHierarchies = new ListBoxWithUserObjects<EmisMetaHierarchy>(); 
    	
    	uiHierarchies.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				EmisMetaHierarchy hierarchy = uiHierarchies.getUserObject(); 
				if (hierarchy == null)
					; 
				else 
					uiTreeBrowser.setHierarchy(hierarchy); 
			}
    	}); 

    	uiTreeBrowser.addValueChangeHandler(new ValueChangeHandler<EmisEntity>() {
			@Override
			public void onValueChange(ValueChangeEvent<EmisEntity> event)
			{
				EmisEntity entity = event.getValue();
				String id = entity.getEntityType().getName() + ":" + entity.getId(); 
				String name = entity.getEntityType().getName() + ":" + entity.getName(); 
				((ListBoxWithUserObjects) uiRootEntities).add(name, id); 
				entityNames.put(dataset + ":" + id, name);
				
				popup.hide(true);
			}
    	});
    	
    	uiHierarchies.clear(); 
		for (EmisMetaHierarchy hierarchy : model.getHierarchies())
			uiHierarchies.addItem(hierarchy.getName(), hierarchy);
		uiHierarchies.setSelectedIndex(0); 
		
		uiTreeBrowser.setEmisMeta(model, uiHierarchies.getUserObject(0));
		
		VerticalPanel vp = new VerticalPanel(); 
		vp.add(new Label("Please pick the location that the user has access to.")); 
    	vp.add(uiHierarchies);
    	vp.add(uiTreeBrowser.getWidget());

    	popup.setWidget(vp);
    }
    
    private void updateEntityNames()
    {
    	final String dataset = getCurrentDataset(); 
    	if (dataset == null)
    		return; 
    	
    	List<String> lookupIds = new ArrayList<String>(); 
    	for (int i = 0; i < uiRootEntities.getItemCount(); i++)
    	{
    		String id = uiRootEntities.getUserObject(i);
    		if (id.equals(uiRootEntities.getItemText(i)))
    		{
    			String name = entityNames.get(dataset + ":" + id);
    			if (name != null)
    				// We know the name already - update the UI. 
    				uiRootEntities.setItemText(i, name);
    			else
    				// Name not know - add to list of ids to resolve. 
    				lookupIds.add(id); 
    		}
    	}

    	if (lookupIds.size() > 0)
    	{
    		toolbox.getService().findEntityNames(dataset, lookupIds, new StatusAsyncCallback<Map<String, String>>("Updating entity names.") {
				@Override
				public void onSuccess(Map<String, String> result) 
				{
					super.onSuccess(result);
					
					// Add all to names lookup, then update again. 
					for (Map.Entry<String, String> entry : result.entrySet())
						entityNames.put(dataset + ":" + entry.getKey(), entry.getValue());
					
					updateEntityNames(); 
				}
    		});
    	}
    }
}
