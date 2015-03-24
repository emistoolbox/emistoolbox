package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.google.gwt.user.client.ui.FlexTable;

public abstract class MapUiEditorBase<T> extends FlexTable implements EmisEditor<T>
{
    private T data;
    private EmisToolboxServiceAsync service;
    private String dataset; 
    
    public MapUiEditorBase(EmisToolboxServiceAsync service, String dataset) 
    {
    	setCellSpacing(4);
        this.service = service;
        this.dataset = dataset; 
    }
    
    public String getDataset()
    { return dataset; } 

    public EmisToolboxServiceAsync getService()
    { return this.service; }

    public void set(T data)
    { this.data = data; }

    public void commit()
    {}

    public T get()
    { return this.data; }
}
