package com.emistoolbox.client.ui.excel;

import java.util.List;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.ui.DropDownSelect;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class ExcelReportFieldEditor extends HorizontalPanel
{    
    private ListBoxWithUserObjects<CellType> uiCellTypes = new ListBoxWithUserObjects<CellType>();
    private TextBox uiValue = new TextBox(); 
    private ListBox uiVariables = new ListBox(); 
    private EmisReportConfig reports; 
    
    public ExcelReportFieldEditor(List<String> variables, List<String> variableLabels, EmisReportConfig reports)
    {
    	this.reports = reports; 
    	
        uiCellTypes.addItem(Message.messageAdmin().xlsCellTypeSelect(), (CellType) null); 
        uiCellTypes.addItem(Message.messageAdmin().xlsCellTypeLoopVariable(), CellType.LOOP_VARIABLE); 
        uiCellTypes.addItem(Message.messageAdmin().xlsCellTypeConstant(), CellType.CONSTANT); 
        uiCellTypes.addItem(Message.messageAdmin().xlsCellTypeOriginal(), CellType.ORIGINAL); 
        uiCellTypes.addItem(Message.messageAdmin().xlsCellTypeEmpty(), CellType.EMPTY); 
        uiCellTypes.addItem(Message.messageAdmin().xlsCellTypeCopyFormula(), CellType.COPY); 
        uiCellTypes.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { updateValueUi(); }
        }); 
        
        if (variables != null)
        {
            for (int i = 0; i < variables.size(); i++) 
            {
                if (i < variableLabels.size())
                    uiVariables.addItem(variableLabels.get(i), variables.get(i)); 
                else
                    uiVariables.addItem(variables.get(i)); 
            }   
        }
        
        add(uiCellTypes); 
        add(uiValue); 
        add(uiVariables); 
        
        updateValueUi(); 
    }
    
    public void updateVariables(List<String> variables, List<String> variableLabels)
    {
        String oldValue = uiVariables.getSelectedIndex() == -1 ? null : uiVariables.getValue(uiVariables.getSelectedIndex()); 
        
        uiVariables.clear(); 
        for (int i = 0; i < variables.size(); i++) 
        {
            String value = variables.get(i); 
            if (i < variableLabels.size())
                uiVariables.addItem(variableLabels.get(i), value); 
            else
                uiVariables.addItem(value); 
        }
        
        GwtUtils.setListValue(uiVariables, oldValue); 
    }
    
    public void set(CellType type, String value)
    {
        setCellType(type); 
        setValue(value); 
    }
    
    public CellType getCellType()
    { return uiCellTypes.getUserObject(); }
    
    public void setCellType(CellType type)
    {
        for (int i = 0; i < uiCellTypes.getItemCount(); i++) 
        {
            if (uiCellTypes.getUserObject(i) != null && uiCellTypes.getUserObject(i).equals(type))
            {
                uiCellTypes.setSelectedIndex(i); 
                updateValueUi(); 
                return; 
            }
        }
    }
    
    public void updateValueUi()
    {
        CellType ct = getCellType(); 
        uiVariables.setVisible(ct == CellType.LOOP_VARIABLE); 
        uiValue.setVisible(ct == CellType.CONSTANT); 
    }
    
    public String getValue()
    {
        switch (getCellType())
        {
        case LOOP_VARIABLE: 
            int index = uiVariables.getSelectedIndex(); 
            if (index == -1)
                return null; 
            
            return uiVariables.getValue(index);  
            
        case CONSTANT: ; 
            return uiValue.getText();
            
        default:
            return null; 
        }
    }
    
    public void setValue(String value)
    {
        switch (getCellType())
        {
        case LOOP_VARIABLE: 
            GwtUtils.setListValue(uiVariables, value); 
            break; 
            
        case CONSTANT: 
            uiValue.setText(value); 
            break; 
            
        default:
            ;
        }
    }
}
