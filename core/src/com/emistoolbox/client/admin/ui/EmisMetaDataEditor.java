package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class EmisMetaDataEditor extends FlexTable
{
    private EmisMeta emis;
    private EmisMetaData data;
    private ListBox uiDateType = new ListBox();
    private ListBox uiType = new ListBox();
    private Label uiEnumTypeLabel = new Label(Message.messageAdmin().emdeLabelClassification());
    private ListBox uiEnumType = new ListBox();
    private MetaEnumTupleUi uiDimensions = null;
    private CheckBox uiPlanningResource = new CheckBox(Message.messageAdmin().emdePlanningResource());

    public EmisMetaDataEditor(EmisMeta emis) {
        this.emis = emis;
        int row = 0;

        refresh();

        getCellFormatter().setStyleName(0, 0, "sectionBar");
        row++; 

        // Planning resource
        row++;

        setText(row, 0, Message.messageAdmin().emdeLabelDateType());
        row++;

        setText(row, 0, Message.messageAdmin().emdeLabelDataType());
        this.uiType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            {
                EmisMetaDataEditor.this.showEnum();
            }
        });
        row++;

        setWidget(row, 0, this.uiEnumTypeLabel);
        row++;

        this.uiDimensions = new MetaEnumTupleUi(null, true);
        this.uiDimensions.setAllEnums(emis.getEnums());
        getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        setText(row, 0, Message.messageAdmin().emdeLabelAsArray());
        row++;
        
        for (int i = 1; i < row; i++) 
        {
        	getCellFormatter().addStyleName(i, 0, "section");
        	getCellFormatter().addStyleName(i, 1, "value");
        }
    }

    public void refresh()
    {
    	boolean readOnly = data != null && "name".equals(data.getName()); 
    	int row = 1; 

    	if (readOnly)
    		setHTML(row, 1, ""); 
    	else
    		setWidget(row, 1, this.uiPlanningResource);
        row++; 
        
        if (readOnly)
        	setHTML(row, 1, data.getDateType() != null ? data.getDateType().getName() : "");
        else
        	setWidget(row, 1, this.uiDateType);
        row++; 

        if (readOnly)
        	setHTML(row, 1, data.getType().toString());
        else
        	setWidget(row, 1, this.uiType);
        row++; 

        if (readOnly)
        	setHTML(row, 1, data.getEnumType() != null ? data.getEnumType().getName() : ""); 
        else
        	setWidget(row, 1, this.uiEnumType);
        row++; 

        if (readOnly)
        	setHTML(row, 1, "<nobr>(no array)</nobr>"); 
        else
        	setWidget(row, 1, this.uiDimensions);
        row++; 

        this.uiDateType.clear();
        for (EmisMetaDateEnum dateEnum : this.emis.getDateEnums())
            this.uiDateType.addItem(dateEnum.getName());

        this.uiType.clear();
        for (EmisMetaData.EmisDataType type : EmisMetaData.EmisDataType.values())
            this.uiType.addItem("" + type);

        this.uiEnumType.clear();
        this.uiEnumType.addItem("");
        for (EmisMetaEnum item : this.emis.getEnums())
            this.uiEnumType.addItem(item.getName());
    }

    public void set(EmisMetaData data)
    {
        this.data = data;
        refresh();

        if (data == null)
            return;
        
        setHTML(0, 0, "Variable '" + data.getName() + "'");
        getFlexCellFormatter().setColSpan(0, 0, 2);
        
        this.uiPlanningResource.setValue(Boolean.valueOf(data.isPlanningResource()));
        setListBox(this.uiType, data.getType());
        setListBox(this.uiDateType, data.getDateType());
        setListBox(this.uiEnumType, data.getEnumType());

        this.uiDimensions.set(data.getArrayDimensions());

        showEnum();
    }

    public EmisMetaData get()
    {
        if (this.data == null)
        {
            return null;
        }
        String type = getListBoxValue(this.uiType);
        if (type != null)
            this.data.setType(EmisMetaData.EmisDataType.valueOf(type));
        else
        {
            this.data.setType(null);
        }
        this.data.setPlanningResource(this.uiPlanningResource.getValue().booleanValue());
        this.data.setDateType((EmisMetaDateEnum) getListBoxValue(this.uiDateType, this.emis.getDateEnums()));
        this.data.setEnumType((EmisMetaEnum) getListBoxValue(this.uiEnumType, this.emis.getEnums()));
        this.data.setArrayDimentsions(this.uiDimensions.get());

        return this.data;
    }

    private String getListBoxValue(ListBox lb)
    {
        int index = lb.getSelectedIndex();
        if (index == -1)
        {
            return null;
        }
        String value = lb.getItemText(index);
        if ((value == null) || (value.equals("")))
        {
            return null;
        }
        return value;
    }

    private <T extends Named> T getListBoxValue(ListBox lb, NamedIndexList<T> indexList)
    {
        String value = getListBoxValue(lb);
        if (value == null)
        {
            return null;
        }
        int resultIndex = indexList.getIndex(value);
        if (resultIndex == -1)
        {
            return null;
        }
        return indexList.get(resultIndex);
    }

    private void showEnum()
    {
    	int index = uiType.getSelectedIndex(); 
    	String value = index == -1 ? null : uiType.getValue(index); 
    	
        boolean visible = value.equals(EmisMetaData.EmisDataType.ENUM.toString()) || value.equals(EmisMetaData.EmisDataType.ENUM_SET.toString());
        this.uiEnumType.setVisible(visible);
        this.uiEnumTypeLabel.setVisible(visible);
    }

    private void setListBox(ListBox lb, EmisMetaData.EmisDataType type)
    {
        if (type == null)
            setListBox(lb, "");
        else
            setListBox(lb, type.toString());
    }

    private void setListBox(ListBox lb, Named obj)
    {
        if (obj == null)
            setListBox(lb, "");
        else
            setListBox(lb, obj.getName());
    }

    private void setListBox(ListBox lb, String value)
    {
        if (value == null)
        {
            if (lb.getItemCount() > 0)
                lb.setSelectedIndex(0);
            return;
        }

        for (int i = 0; i < lb.getItemCount(); i++)
        {
            if (!lb.getItemText(i).equals(value))
                continue;
            lb.setSelectedIndex(i);
            return;
        }

        if (lb.getItemCount() > 0)
            lb.setSelectedIndex(0);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.EmisMetaDataEditor JD-Core
 * Version: 0.6.0
 */