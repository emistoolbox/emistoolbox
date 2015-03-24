package com.emistoolbox.client.ui.pdf;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VariableEditor extends VerticalPanel implements HasValueChangeHandlers<Boolean>
{
    private FlexTable uiVariables = new FlexTable();
    private TextBox uiTitle = new TextBox();
    private PushButton uiAdd = new PushButton("Add"); 
    private PushButton uiOk = new PushButton("Ok"); 
    private PushButton uiCancel = new PushButton("Cancel"); 
    private List<String> varNames = new ArrayList<String>(); 
    
    public VariableEditor(EmisMetaEntity entity)
    {
        setSpacing(5); 
        
        for (EmisMetaData field : entity.getData())
        {
            if (field.getArrayDimensions() == null)
                varNames.add(field.getName());
        }

        add(new Label("Title:")); 
        add(uiTitle); 
        add(new Label("Variables")); 
        add(uiVariables); 
        
        HorizontalPanel hp = new HorizontalPanel(); 
        hp.setSpacing(2); 
        hp.add(EmisUtils.init(uiAdd,  60));  
        hp.add(EmisUtils.init(uiOk,  60)); 
        hp.add(EmisUtils.init(uiCancel,  60)); 
        add(hp); 
        
        uiAdd.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { setItem(uiVariables.getRowCount(), null, null); }
        }); 

        uiCancel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ValueChangeEvent.fire(VariableEditor.this, false); }
        }); 

        uiOk.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            { ValueChangeEvent.fire(VariableEditor.this, true); }
        }); 
    }
    
    private void setItem(int row, String title, String variable)
    {
        TextBox uiItemTitle = new TextBox(); 
        uiItemTitle.setText(title); 
        
        ListBox uiVariable = new ListBox(); 
        int selectedIndex = (variable == null || !variable.equals("")) ? -1 : uiVariable.getItemCount(); 
        uiVariable.addItem(""); 
        for (String varName : varNames)
        {
            if (variable != null && variable.equals(varName))
                selectedIndex = uiVariable.getItemCount(); 
            uiVariable.addItem(varName); 
        }
        
        uiVariable.setSelectedIndex(selectedIndex); 
        
        uiVariables.setWidget(row, 0, uiItemTitle); 
        uiVariables.setWidget(row, 1, uiVariable); 
    }
    
    public void set(PdfVariableContentConfig config)
    {
        uiTitle.setText(config.getTitle());
        while (uiVariables.getRowCount() > 0)
            uiVariables.removeRow(0);
        
        for (int i = 0; i < config.getItemCount(); i++) 
            setItem(i, config.getItemTitle(i), config.getItemVariable(i)); 
    }
    
    public void update(PdfVariableContentConfig config)
    {
        config.setTitle(uiTitle.getText());
        config.deleteAllItems(); 
        
        for (int row = 0; row < uiVariables.getRowCount(); row++) 
        {
            TextBox tb = (TextBox) uiVariables.getWidget(row, 0); 
            ListBox lb = (ListBox) uiVariables.getWidget(row, 1); 
            
            if (lb.getSelectedIndex() == -1 || lb.getSelectedIndex() == 0)
                continue; 
            
            config.addItem(tb.getText(), lb.getSelectedIndex() == -1 ? "" : lb.getItemText(lb.getSelectedIndex())); 
        }
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }
}
