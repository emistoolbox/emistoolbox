package com.emistoolbox.client.admin;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.ManageAdminDatasetDialog;
import com.emistoolbox.client.ui.validation.ValidationListEditor;
import com.emistoolbox.common.EmisAdminModuleData;
import com.emistoolbox.common.model.mapping.MappingValidation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class AdminModule
{
    private EmisToolbox toolbox;
    private EmisAdminModuleData config = new EmisAdminModuleData();

    private HTML uiDataset = new HTML(Message.messageAdmin().none());
    private String fixedDataset; 
    private String currentDataset; 
    
    private int manageButtonIndex = 3; 

    public AdminModule(EmisToolbox toolbox, String fixedDataset) 
    {
        this.toolbox = toolbox;
        this.fixedDataset = fixedDataset; 
        this.currentDataset = fixedDataset; 

        createButtons();
        toolbox.setMessage(Message.messageAdmin().welcomeMessage());

        if (fixedDataset != null)
        	ManageAdminDatasetDialog.doLoad(fixedDataset, this, toolbox, null);
    }

    public String getFixedDataset()
    { return fixedDataset; }
    
    public String getCurrentDataset()
    { return currentDataset; } 
    
    private void createButtons()
    {
    	boolean hasDataset = !(config == null || config.getModel() == null || config.getModel().getDatasetName() == null);
    	
        List<Widget> menuButtons = new ArrayList<Widget>();
        if (hasDataset)
        {
	        menuButtons.add(EmisToolbox.getMenuItem(Message.messageAdmin().btnDataModel(), new ClickHandler() {
	            public void onClick(ClickEvent event)
	            {                
	                if (!commit(AdminModule.this.toolbox.getSelectedButton()))
	                	return; 
	                
	                toolbox.selectButton(0);
	
	                ModelEditor modelEditor = new ModelEditor(AdminModule.this.toolbox);
	                modelEditor.set(AdminModule.this.config.getModel());
	                toolbox.setWidget(Message.messageAdmin().editorModel(), modelEditor);
	            }
	        }));

	        menuButtons.add(EmisToolbox.getMenuItem(Message.messageAdmin().btnDataMapping(), new ClickHandler() {
	            public void onClick(ClickEvent event)
	            {
	                if (!commit(AdminModule.this.toolbox.getSelectedButton()))
	                	return; 
	                
	                toolbox.selectButton(1);
	                
	                MappingValidation validation = new MappingValidation(config.getModel());
	                validation.validateMappings(config.getMapping()); 
	
	                DbMapEditor editor = new DbMapEditor(AdminModule.this.toolbox);
	                editor.set(config.getMapping());
	                toolbox.setWidget(Message.messageAdmin().dataMappingConfig(), editor);
	            }
	        }));
        
	        menuButtons.add(EmisToolbox.getMenuItem("Validation", new ClickHandler() {
	        	public void onClick(ClickEvent event)
	        	{
	        		if (!commit(AdminModule.this.toolbox.getSelectedButton()))
	        			return; 
	        		
	        		toolbox.selectButton(2); 

	        		ValidationListEditor editor = new ValidationListEditor(config.getModel()); 
	        		editor.setStylePrimaryName("adminEdit");
	        		editor.setWidth("100%");
	        		editor.setHeight("450px");
	        		editor.set(config.getValidations());
	        		toolbox.setWidget("Validation Editor", editor);
	        	}
	        }));
	        
	        manageButtonIndex = 3; 
	    }
	    else
	    	manageButtonIndex = 0; 
        
        menuButtons.add(EmisToolbox.getMenuItem(Message.messageAdmin().btnManageData(), new ClickHandler() {
            public void onClick(ClickEvent event)
            { showDatasetManager(); }  
        }));
        
    	uiDataset.setStyleName("value");
        uiDataset.addStyleName("spacing");
        uiDataset.addStyleName("pointer"); 
        uiDataset.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { showDatasetManager(); } 
        }); 

        this.toolbox.setMenuItems(menuButtons, uiDataset);
    }

    private void showDatasetManager()
    {
    	if (!commit(AdminModule.this.toolbox.getSelectedButton()))
    		return; 
    	
    	toolbox.selectButton(manageButtonIndex);
    	toolbox.setWidget(Message.messageAdmin().btnManageData(), new ManageAdminDatasetDialog(AdminModule.this, toolbox, config)); 
    }
    
    /** Callback to be used after loading dataset. */
    public void setNewDataset(String dataset, EmisAdminModuleData config)
    {
		uiDataset.setHTML(dataset);
		this.currentDataset = dataset; 
		this.config = config; 
		createButtons(); 
    }

    public boolean isEmpty(EmisAdminModuleData config)
    {
        if ((config.getModel() == null) || (isEmpty(config.getModel().getEntities())))
            return true;

        return (config.getMapping() == null) && (isEmpty(config.getMapping().getEntityMappings()));
    }

    private <T> boolean isEmpty(List<T> list)
    { return (list == null) || (list.size() == 0); }

    private boolean commit(int index)
    {
        Widget w = this.toolbox.getWidget();
        if (w instanceof EmisEditor)
            ((EmisEditor) w).commit();

        if (w instanceof ModelEditor)
        {
            config.setModel(((ModelEditor) w).get());
            config.getMapping().updateDimensions(); 
        }
        else if (w instanceof DbMapEditor)
            config.setMapping(((DbMapEditor) w).get());
        else if (w instanceof ManageAdminDatasetDialog)
        	((ManageAdminDatasetDialog) w).commitUsers(); 
        else if (w instanceof ValidationListEditor)
        {
        	ValidationListEditor editor = (ValidationListEditor) w; 
        	config.setValidations(editor.get());
        	if (!editor.validate())
        		return false; 
        }
        
        return true; 
    }
}
