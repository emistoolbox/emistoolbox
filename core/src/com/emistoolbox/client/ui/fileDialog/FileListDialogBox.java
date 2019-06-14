package com.emistoolbox.client.ui.fileDialog; 

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.common.fileDialog.*; 
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class FileListDialogBox extends PopupPanel implements HasValueChangeHandlers<String> 
{
    private final HTML serverResponseTitle = new HTML();
    private final HTML serverResponseLabel = new HTML();
    private final HTML currentDirectoryLabel = new HTML();
    private final PushButton cancelButton = new PushButton(Message.messageAdmin().btnCancel());
    private final PushButton actionButton = new PushButton(Message.messageAdmin().btnOpen());
    private final ListBox fileListBox = new ListBox(false);
    private final ListBox filterListBox = new ListBox();
    private final Tree dirTree = new Tree(); 
    private final TextBox fullPath = new TextBox();
    
    private EmisToolboxServiceAsync service; 
    private String protocol; 
    private String dataset; 

    public FileListDialogBox(String protocol, EmisToolboxServiceAsync service, String dataset, String[] filters) 
    {
        super();

        setFilters(filters);

        fullPath.setWidth("450px");
        currentDirectoryLabel.setWidth("250px"); 
        this.service = service; 
        this.dataset = dataset; 
        this.protocol = protocol; 
        fullPath.setText("");
        
        fileListBox.setVisibleItemCount(10);

        setWidth("540px"); 

        filterListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) 
            { loadChildren(dirTree.getSelectedItem()); }
        });

        dirTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) 
            { changeDirectory(event.getSelectedItem()); }
        });

        fileListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) 
            {
            	int index = fileListBox.getSelectedIndex();
            	if (index != -1)
            	{
                	String value = fileListBox.getItemText(index); 
                	if (value.equals(".."))
                	{
                		TreeItem item = dirTree.getSelectedItem();
                		if (item != null && item.getParentItem() != null)
                			changeDirectory(item.getParentItem()); 
                	}
                	else if (value.startsWith("[") && value.endsWith("]"))
                		changeDirectory(value.substring(1, value.length() - 1)); 
                	else 
                	{
                		String path = currentDirectoryLabel.getText(); 
                		if (!path.endsWith("/"))
                			path += "/"; 
                		path += value; 

                		while (path.startsWith("//"))
                			path = path.substring(1); 
                		
                		fullPath.setText(path); 
                	}
            	}	
            }
        });

        // Add a handler to save the file
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) 
            {
                FileListDialogBox.this.hide();
                if (!fullPath.getText().equals(""))
                	ValueChangeEvent.fire(FileListDialogBox.this, fullPath.getText());
            }
        });
        
        // Add a handler to close the DialogBox
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) 
            { FileListDialogBox.this.hide(); }
        });

        initUi();
    }
    
    private void changeDirectory(String directory)
    {
    	TreeItem item = dirTree.getSelectedItem();
    	changeDirectory(findChildItem(item, directory)); 
    }
    
    private TreeItem findChildItem(TreeItem item, String directory)
    {
    	if (item == null)
    		return null; 
    	for (int i = 0; i < item.getChildCount(); i++)
    		if (item.getChild(i).getText().equals(directory))
    			return item.getChild(i); 
    	
    	return null; 
    }
    
    
    private void changeDirectory(TreeItem item)
    {
    	if (item == null)
    		return; 
    	
    	currentDirectoryLabel.setText(getPath(item)); 
    	fileListBox.clear(); 
    	loadChildren(item); 
    }
    
    
    private void initUi() 
    {
        this.setAnimationEnabled(true);
        
        // We can set the id of a widget by accessing its Element
        cancelButton.getElement().setId("cancelButton");
        
        FlexTable panel = new FlexTable(); 
        panel.setCellSpacing(2);  
        setWidget(panel);

        int row = 0; 
        panel.setHTML(row, 0, "<h2>" + Message.messageAdmin().openFile() + "</h2>"); 
        row++; 

        panel.setHTML(row, 0, "<b><small>Directory:</small></b>");
        panel.setHTML(row, 1, "<b><small>File:</small></b>"); 
        row++; 

        panel.setWidget(row, 0, currentDirectoryLabel);
        panel.getFlexCellFormatter().setColSpan(row, 0, 2);
        row++; 
        
        panel.setWidget(row, 0, prepare(dirTree, "200px", "230px")); 
        panel.setWidget(row, 1, prepare(fileListBox, "200px", "230px")); 
        row++; 

        panel.setWidget(row, 1, serverResponseTitle);
        row++; 

        panel.setWidget(row, 1, serverResponseLabel); 
        row++; 
        
        serverResponseLabel.getElement().getStyle().setProperty("color", "red");

        panel.setHTML(row, 0, "<b><small>Selection:</small></b>");
        row++;
        
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(fullPath);
        hp.add(filterListBox);
        hp.setSpacing(2);
        panel.getFlexCellFormatter().setColSpan(row, 0, 2);
        panel.setWidget(row, 0, hp);
        row++;

        hp = new HorizontalPanel();
        hp.setSpacing(2); 
        EmisUtils.init(actionButton, 90); 
        EmisUtils.init(cancelButton, 90); 
        hp.add(actionButton);
        hp.add(cancelButton);
        panel.setWidget(row, 1, hp);
        panel.getCellFormatter().setHorizontalAlignment(row,  1,  HasHorizontalAlignment.ALIGN_RIGHT);
        row++;
        
        load(""); 
    }
    
    private Widget prepare(Widget w, String width, String height)
    {
        ScrollPanel scroll = new ScrollPanel(w);
        scroll.setSize(width, height); 
        w.setSize(width, height);
        
        return scroll; 
    }
        
    private String getPath(TreeItem item)
    {
    	StringBuffer result = new StringBuffer(); 
    	while (item != null)
    	{
    		if (result.length() > 0)
    			result.insert(0, "/"); 
    		result.insert(0, item.getText()); 
    		
    		item = item.getParentItem(); 
    	}
    	
    	while (result.length() >= 2 && result.charAt(0) == '/' && result.charAt(1) == '/')
    		result.deleteCharAt(0); 
    	
    	return result.toString(); 
    }
    
    private void load(final String path)
    {
    	dirTree.clear(); 
    	TreeItem item = GwtUtils.getTreeItem(path.equals("") ? "/" : ""); 
    	dirTree.addItem(item);   
    	loadChildren(item); 
    }
    
    private void loadChildren(final TreeItem parent)
    {
    	if (parent == null)
    		return; 

    	if (dataset == null)
    	{
    		serverResponseTitle.setText("Error"); 
    		serverResponseTitle.setText("No dataset specified."); 
    		return; 
    	}

    	String filter = getFilter(); 
        service.fileDialogListDir(getPath(parent), protocol, dataset, filter, new AsyncCallback<FileDirectoryInfo>() {
            public void onFailure(Throwable caught) 
            {
                // Show the RPC error message to the user
                serverResponseTitle.setText("Remote Procedure Call - Failure");
                serverResponseLabel.setHTML(Message.messageAdmin().errorServerError());
                
                cancelButton.setFocus(true);
            }
            public void onSuccess(FileDirectoryInfo result) 
            {
                serverResponseTitle.setText("");
                serverResponseLabel.setText("");
                updateItems(parent, result); 

                actionButton.setFocus(true);
            }
        });
    }

    private void updateItems(TreeItem parent, FileDirectoryInfo result)
    {
    	// Remove all children. 
    	if (parent.getParentItem() == null)
    		currentDirectoryLabel.setText(result.getPath()); 
    	
        fileListBox.clear(); 
        if (parent.getParentItem() != null)
        	fileListBox.addItem(".."); 
        
        for (String directory : result.getDirectories())
        {
        	updateDirectory(parent, directory); 
        	fileListBox.addItem("[" + directory + "]"); 
        }
        
        for (String file : result.getFiles())
        	fileListBox.addItem(file); 
        
        parent.setState(true);
        dirTree.setSelectedItem(parent, false);
    }    

    private void updateDirectory(TreeItem parent, String name)
    {
    	for (int i = 0; i < parent.getChildCount(); i++)
    		if (parent.getChild(i).getText().equals(name))
    			return; 
    	
    	parent.addItem(GwtUtils.getTreeItem(name)); 
    }
    
	public String getFilter()
	{
		if (filterListBox.isVisible() && filterListBox.getSelectedIndex() != -1)
			return filterListBox.getItemText(filterListBox.getSelectedIndex()); 
		
		return filterListBox.getItemCount() > 0 ? filterListBox.getItemText(0) : null;  
	}
	
    private void setFilters(String[] filters) 
    {
        filterListBox.clear();
        
        for (String filter : filters)
        	filterListBox.addItem(filter); 

        filterListBox.setSelectedIndex(0);
        
//        if (update)
//        {
//        	TreeItem item = dirTree.getSelectedItem(); 
//        	if (item == null)
//        		load(""); 
//        	else        		
//        		loadChildren(item);
//        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) 
    { return addHandler(handler, ValueChangeEvent.getType()); }
}
