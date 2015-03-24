package com.emistoolbox.client.ui.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.common.excelMerge.CellPosition;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.excelMerge.ExcelReportUtil;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.MergeDirection;
import com.emistoolbox.common.excelMerge.impl.ExcelMergeConfigImpl;
import com.emistoolbox.common.excelMerge.impl.ExcelReportConfigImpl;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ExcelReportConfigEditor extends FlexTable implements EmisEditor<ExcelReportConfig>
{
    private EmisMeta meta; 
    private EmisReportConfig reports; 

    private int rowFields; 
    private PushButton btnAddField = new PushButton(Message.messageAdmin().xlsAddField()); 

    private ExcelReportConfig config = null; 
    private TextBox uiName = new TextBox(); 
    private ListBox uiTemplateFile = new ListBox(); 
    private ListBox uiEntities = new ListBox(); 
    private ListBox uiSheets = new ListBox(); 
    private TextBox uiPosition = new TextBox(); 
    
    private RadioButton uiDirectionRows = new RadioButton("direction", Message.messageAdmin().xlsRows()); 
    private RadioButton uiDirectionColumns = new RadioButton("direction", Message.messageAdmin().xlsColumns()); 
    
    private List<String> variables = new ArrayList<String>(); 
    private List<String> variableLabels = new ArrayList<String>(); 
    private EmisToolbox toolbox; 
    private String datasetName; 
    
    // Helper values to manage asynchronous loading of templates and sheets. 
    //
    private int templatesRow; 
    private int sheetsRow; 
    private boolean loadingTemplates = false; 
    private String pendingTemplateValue = null; 
    private String pendingSheetValue = null; 
    
    public ExcelReportConfigEditor(EmisToolbox toolbox, String dataSetName, EmisMeta meta, final EmisReportConfig reports)
    {
        this.meta = meta; 
        this.reports = reports; 
        this.toolbox = toolbox; 
        this.datasetName = dataSetName; 
        
        int row = 0; 
        
        uiName.setStyleName(EmisToolbox.CSS_FULL_TEXT);
        uiTemplateFile.setStyleName(EmisToolbox.CSS_THIRD_TEXT);
        uiEntities.setStyleName(EmisToolbox.CSS_THIRD_TEXT);
        uiSheets.setStyleName(EmisToolbox.CSS_THIRD_TEXT);
        uiPosition.setStyleName(EmisToolbox.CSS_THIRD_TEXT); 
        
        setHTML(row, 0, Message.messageAdmin().xlsName() + ":");
        getFlexCellFormatter().setColSpan(row, 1, 2);
        setWidget(row, 1, uiName); 
        row++; 
        
        setHTML(row, 0, Message.messageAdmin().xlsTemplateFile() + ":"); 
        templatesRow = row; 
        loadTemplates(); 
        uiTemplateFile.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { onTemplateChange(null); }
        }); 
        row++;
        
        setHTML(row, 1, "<small>Directory: " + dataSetName + "\\excel</small>"); 
        row++; 
        
        for (EmisMetaEntity entityType : meta.getEntities())
            uiEntities.addItem(entityType.getName()); 

        setHTML(row, 0, Message.messageAdmin().preHtmlForEntity() + ":"); 
        setWidget(row, 1, uiEntities); 
        uiEntities.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                onEntityChange(); 
                showAddButton(); 
            }
        }); 
        row++; 
        
        sheetsRow = row; 
        setHTML(row, 0, Message.messageAdmin().xlsDataPosition() + ":"); 
        setWidget(row, 1, uiSheets);
        setWidget(row, 2, uiPosition);
        uiPosition.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { onPositionChange(); }
        }); 
        row++; 
        
        setHTML(row, 1, "<small>Sheet</small>"); 
        setHTML(row, 2, "<small>Top Left Cell (e. g. E4)</small>");
        row++; 
        
        setHTML(row, 0, Message.messageAdmin().xlsDirection() + ":"); 
        setWidget(row, 1, uiDirectionRows); 
        setWidget(row, 2, uiDirectionColumns); 

        ClickHandler positionClickHandler = new ClickHandler() {
            public void onClick(ClickEvent event)
            { onPositionChange(); } 
        };
        uiDirectionRows.addClickHandler(positionClickHandler); 
        uiDirectionColumns.addClickHandler(positionClickHandler); 
        
        row++; 
        
        rowFields = row;
        
        btnAddField.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                int row = getNextFieldEditor(); 
                while (row < getRowCount())
                    removeRow(row); 

                setWidget(row, 1, new UpDownDeleteControl(row, row > rowFields, false, getPositionText(getTopLeft(), row - rowFields))); 
                setWidget(row, 2, new ExcelReportFieldEditor(variables, variableLabels, reports)); 
                updateUpDown(row); 

                showAddButton(); 
            }
        }); 

        showAddButton(); 
        
        for (int i = 0; i < rowFields; i++)
        	getCellFormatter().setStyleName(i, 0, EmisToolbox.CSS_SECTION); 
    } 

    private void loadTemplates()
    {
        uiTemplateFile.clear(); 
        setHTML(templatesRow, 1, Message.messageAdmin().xlsLoading()); 
        
        synchronized(this) 
        { loadingTemplates = true; }
        
        AsyncCallback<String[]> callback = new StatusAsyncCallback<String[]>("Loading Excel templates") {
            public void onSuccess(String[] result)
            {
                super.onSuccess(result); 
                
                for (String template : result)
                    uiTemplateFile.addItem(template); 
                
                setWidget(templatesRow, 1, uiTemplateFile); 
                
                synchronized(ExcelReportConfigEditor.this) 
                {
                    loadingTemplates = false; 
                    setTemplateAndSheet(pendingTemplateValue,  pendingSheetValue); 
                } 
            }
        }; 
        
        try { toolbox.getService().getExcelReportTemplates(datasetName, callback); }
        catch (IOException ex)
        {}
    }
    
    private void onPositionChange()
    {
        CellPosition topLeft = getTopLeft();
        for (int row = rowFields; row < getRowCount(); row++) 
        {
            if (getCheckedWidget(row, 1) instanceof UpDownDeleteControl)
                ((UpDownDeleteControl) getWidget(row, 1)).setText(getPositionText(topLeft, row - rowFields)); 
        }
    }
    
    private void onTemplateChange(final String sheetValue)
    {
        uiSheets.clear(); 
        if (-1 != uiTemplateFile.getSelectedIndex())
        {
            try { 
                toolbox.getService().getExcelReportSheets(datasetName, GwtUtils.getListValue(uiTemplateFile), new StatusAsyncCallback<String[]>("Loading excel sheet data.") {
                public void onSuccess(String[] result)
                {
                    super.onSuccess(result);
                    for (String sheet : result)
                        uiSheets.addItem(sheet); 

                    GwtUtils.setListValue(uiSheets, sheetValue); 
                    setWidget(sheetsRow, 1, uiSheets); 
                }
            }); 
            }
            catch (IOException ex)
            {}
        }
    }
    
    private void onEntityChange()
    {
        variables.clear(); 
        variableLabels.clear();

        EmisMetaEntity entityType = getEntity();
        if (entityType == null)
            return; 

        for (EmisIndicator indicator : reports.getIndicators())
        {
            if (!canUseIndicator(entityType, indicator, meta.getHierarchies()))
                continue;
            
            addIndicatorToUi(indicator); 
        }
        
        for (EmisMetaData field : entityType.getData())
        {
            if (field.getArrayDimensions() != null)
                continue;
            
            variables.add(ExcelReportUtil.getFieldId(field)); 
            variableLabels.add(entityType.getName() + "." + field.getName()); 
        }
        
        for (int row = rowFields; row < getRowCount(); row++)
        {
            Widget w = getCheckedWidget(row, 2); 
            if (w instanceof ExcelReportFieldEditor)
                ((ExcelReportFieldEditor) w).updateVariables(variables, variableLabels); 
        }
    }
    
    private void addIndicatorToUi(EmisIndicator indicator)
    {
        variables.add(ExcelReportUtil.getExcelIndicatorId(indicator));  
        variableLabels.add(indicator.getName());  
        
        for (Map.Entry<String, EmisAggregatorDef> entry : indicator.getAggregators().entrySet())
        {
            EmisAggregatorDef aggr = entry.getValue(); 
            if (aggr == null || aggr.getName() == null || aggr.getName().equals(""))
                continue; 
            
            variables.add(ExcelReportUtil.getExcelAggregatorId(indicator, entry.getKey()));  
            variableLabels.add("=== " + aggr.getName() + " (" + indicator.getName() + ")"); 
        }
    }
    
    private boolean canUseIndicator(EmisMetaEntity reportEntity, EmisIndicator indicator, List<EmisMetaHierarchy> hierarchies)
    {
        for (EmisMetaHierarchy h : hierarchies)
        {
            if (canUseIndicator(reportEntity, indicator, h))
                return true; 
        }
        
        return false; 
    }
    
    private boolean canUseIndicator(EmisMetaEntity reportEntity, EmisIndicator indicator, EmisMetaHierarchy hierarchy)
    {
        EmisMetaEntity entityIndicator = indicator.getSeniorEntity(hierarchy); 
        if (entityIndicator == null)
            return false;
        return NamedUtil.sameName(entityIndicator, reportEntity) || entityIndicator.isChildOf(reportEntity, hierarchy); 
    }
    
    private boolean canAddFields()
    { return uiEntities.getItemCount() > 0 && uiEntities.getSelectedIndex() != -1; }
    
    private void showAddButton()
    {
        if (!canAddFields())
        {
            while (rowFields < getRowCount())
                removeRow(rowFields); 
            
            setHTML(rowFields, 0, Message.messageAdmin().xlsFields() + ":");
            setHTML(rowFields, 1, Message.messageAdmin().xlsSelectEntity()); 
        }
        else
        {
            int row = getNextFieldEditor(); 
            while (row < getRowCount())
                removeRow(row); 
            
            setHTML(rowFields, 0, Message.messageAdmin().xlsFields() + ":"); 
            setWidget(row, 1, btnAddField);  
        }
        
    	getCellFormatter().setStyleName(rowFields, 0, EmisToolbox.CSS_SECTION); 
    }
    
    private int getNextFieldEditor()
    {
        int row = rowFields; 
        while (getCheckedWidget(row, 1) instanceof UpDownDeleteControl)
            row++; 
        
        return row; 
    }
    
    private CellPosition getTopLeft()
    { 
        try { return new CellPosition(uiPosition.getText()); }
        catch (Throwable err)
        { return null; }
    }
    
    private String getPositionText(CellPosition topLeft, int offset)
    { 
        if (topLeft == null || (!uiDirectionRows.getValue() && !uiDirectionColumns.getValue()))
            return "" + (offset + 1); 
        
        return new CellPosition(topLeft, uiDirectionRows.getValue() ? offset : 0, uiDirectionColumns.getValue() ? offset : 0).toString(); 
   }
    
    private EmisMetaEntity getEntity()
    {
        if (uiEntities.getSelectedIndex() == -1)
            return  null; 
        
        return NamedUtil.find(uiEntities.getValue(uiEntities.getSelectedIndex()), meta.getEntities()); 
    }

    public void commit()
    {
        if (config == null)
            return; 
        
        config.setName(uiName.getText()); 
        config.setTemplateFile(GwtUtils.getListValue(uiTemplateFile));
        config.setEntityType(getEntity()); 
        
        ExcelMergeConfig mergeConfig = new ExcelMergeConfigImpl(); 
        mergeConfig.setSheetName(GwtUtils.getListValue(uiSheets)); 
        try { mergeConfig.setTopLeft(new CellPosition(uiPosition.getText())); }
        catch (Throwable err)
        {}
        
        mergeConfig.setDirection(uiDirectionRows.getValue() ? MergeDirection.ROWS : (uiDirectionColumns.getValue() ? MergeDirection.COLUMNS : null)); 

        List<CellType> cellTypes = new ArrayList<CellType>(); 
        List<String> cellValues = new ArrayList<String>(); 
        List<EmisIndicator> cellIndicators = new ArrayList<EmisIndicator>(); 
        List<EmisAggregatorDef> cellAggregators = new ArrayList<EmisAggregatorDef>(); 
        
        for (int row = rowFields; row < getRowCount(); row++) 
        {
            Widget w = getCheckedWidget(row, 2);  
            if (w instanceof ExcelReportFieldEditor)
            {
                ExcelReportFieldEditor editor = (ExcelReportFieldEditor) w; 
                if (editor.getCellType() != null)
                {
                	CellType cellType = editor.getCellType(); 
                	String value = editor.getValue(); 
                	
                    cellTypes.add(cellType); 
                    cellValues.add(value); 

                    EmisIndicator indicator = ExcelReportConfigImpl.getIndicator(cellType, value, reports.getIndicators()); 
                    cellIndicators.add(indicator);  
                    cellAggregators.add(ExcelReportConfigImpl.getAggregator(indicator, value)); 
                }
            }
        }
        
        mergeConfig.setCells(cellTypes.toArray(new CellType[] {}), cellValues.toArray(new String[] {}), cellIndicators.toArray(new EmisIndicator[0]), cellAggregators.toArray(new EmisAggregatorDef[0])); 
        
        List<ExcelMergeConfig> mergeConfigs = new ArrayList<ExcelMergeConfig>(); 
        mergeConfigs.add(mergeConfig); 
        config.setMergeConfigs(mergeConfigs); 
    }

    public ExcelReportConfig get()
    {
        commit(); 
        return config;
    }

    public void set(ExcelReportConfig config)
    {
        this.config = config; 
        if (config == null)
        {
            setVisible(false);
            return; 
        }

        setVisible(true); 
        
        uiName.setText(config.getName());
        
        uiEntities.setSelectedIndex(NamedUtil.findIndex(config.getEntityType(), meta.getEntities()));
        
        String sheetValue = null; 
        List<ExcelMergeConfig> mergeConfigs = config.getMergeConfigs(); 
        if (mergeConfigs != null && mergeConfigs.size() > 0)
        {
            ExcelMergeConfig mergeConfig = mergeConfigs.get(0); 
            sheetValue = mergeConfig.getSheetName();  
            CellPosition topLeft = mergeConfig.getTopLeft(); 
            uiPosition.setValue(topLeft == null ? "" : "" + mergeConfig.getTopLeft()); 
            uiDirectionRows.setValue(mergeConfig.getDirection() == MergeDirection.ROWS); 
            uiDirectionColumns.setValue(mergeConfig.getDirection() == MergeDirection.COLUMNS); 
            
            onEntityChange(); 
            
            int row; 
            for (row = 0; row < mergeConfig.getCellCount(); row++) 
            {
                setWidget(rowFields + row, 1, new UpDownDeleteControl(rowFields + row, row > 0, row + 1 < mergeConfig.getCellCount(), getPositionText(topLeft,  row)));  

                ExcelReportFieldEditor editor = new ExcelReportFieldEditor(variables, variableLabels, reports); 
                editor.set(mergeConfig.getCellType(row), mergeConfig.getCellValue(row)); 
                setWidget(rowFields + row, 2, editor); 
            }
            
            while (rowFields + row < getRowCount())
                removeRow(rowFields + row); 
        }
        else
        {
            uiPosition.setValue(""); 
            uiDirectionRows.setValue(false); 
            uiDirectionColumns.setValue(false); 
            while (rowFields < getRowCount())
                removeRow(rowFields); 
        }
        
        setTemplateAndSheet(config.getTemplateFile(), sheetValue); 
        
        showAddButton(); 
    }
    
    private Widget getCheckedWidget(int row, int col)
    {
        if (row < getRowCount() && col < getCellCount(row))
            return getWidget(row, col); 
        
        return null; 
    }
    
    private void setTemplateAndSheet(String templateValue, String sheetValue)
    {
        synchronized(this) 
        {
            if (loadingTemplates)
            {
                pendingTemplateValue = templateValue; 
                pendingSheetValue = sheetValue; 
            }
            else
            {
                GwtUtils.setListValue(uiTemplateFile, templateValue); 
                onTemplateChange(sheetValue); 
                
                pendingTemplateValue = null; 
                pendingSheetValue = null; 
            }
        }
    }
    
    private void move(int row, int offset)
    {
        int lastRow = getNextFieldEditor() - 1; 
        int targetRow = row + offset; 
        if (targetRow < rowFields || targetRow > lastRow)
            return; 

        ExcelReportFieldEditor editor = (ExcelReportFieldEditor) getCheckedWidget(row, 2); 
        setWidget(row, 2, getCheckedWidget(targetRow, 2)); 
        setWidget(targetRow, 2, editor); 
        
        updateUpDown(lastRow); 
    }
    
    private void delete(int row)
    {
        int endRow = getNextFieldEditor() - 1; 
        
        if (row < rowFields || row > endRow)
            return; 
        
        removeRow(row);
        updateUpDown(endRow); 
    }
    
    private void updateUpDown(int endRow)
    {
        int row = rowFields; 
        while (row <= endRow)
        {
            UpDownDeleteControl ctrl = (UpDownDeleteControl) getWidget(row, 1);
            ctrl.setShowUp(row > rowFields); 
            ctrl.setShowDown(row < endRow); 
            ctrl.setRow(row);
            
            row++; 
        }
    }
    

    class UpDownDeleteControl extends HorizontalPanel
    {
        private int row = -1;
        private Label uiText = new Label(); 
        private Image uiUp = new Image(); 
        private Image uiDown = new Image(); 
        private Image uiDel = new Image("css/del.png"); 
        
        public UpDownDeleteControl(int row, boolean showUp, boolean showDown, String text)
        {
            this.row = row; 
            
            uiText.setText(text); 
            setCellSpacing(2); 
            setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE); 

            VerticalPanel vp = new VerticalPanel(); 
            vp.add(uiUp); 
            vp.add(uiDown); 

            add(uiText); 
            uiText.setWidth("50px"); 
            uiText.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT); 
            
            add(vp); 
            add(uiDel); 

         
            uiUp.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    if (uiUp.getUrl().indexOf("Gray") == -1)
                        move(UpDownDeleteControl.this.row, -1); 
                }
            }); 
            
            uiDown.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    if (uiDown.getUrl().indexOf("Gray") == -1)
                        move(UpDownDeleteControl.this.row, 1); 
                }
            }); 

            uiDel.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event)
                { delete(UpDownDeleteControl.this.row); }
            }); 

            
            setShowUp(showUp); 
            setShowDown(showDown); 
        }
        
        private void setText(String text)
        { uiText.setText(text); } 
        
        private void setRow(int row)
        { this.row = row; } 
        
        private void setShowUp(boolean show)
        { uiUp.setUrl(show ? "css/up.png" : "css/upGray.png"); } 
        
        private void setShowDown(boolean show)
        { uiDown.setUrl(show ? "css/down.png" : "css/downGray.png"); } 
    }
}

