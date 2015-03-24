package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity.EmisGisType;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;

public class EmisMetaEntityEditor extends FlexTable
{
    private EmisMetaEntity entity;
    private ListBox uiGisType = new ListBox();

    public EmisMetaEntityEditor() {
        for (EmisMetaEntity.EmisGisType t : EmisMetaEntity.EmisGisType.values())
            this.uiGisType.addItem(t.toString());

        getCellFormatter().setStyleName(0, 0, "sectionBar"); 
        getFlexCellFormatter().setColSpan(0, 0, 2);
        
        setText(1, 0, Message.messageAdmin().promptGisType());
        setWidget(1, 1, this.uiGisType);
    }

    public void set(EmisMetaEntity entity)
    {
        this.entity = entity;
        if (entity == null)
        {
        	setHTML(0, 0, ""); 
        	return;
        }
        
        setHTML(0, 0, "Location '" + entity.getName() + "'"); 
        
        int index = 0;
        int foundIndex = 0;
        for (EmisMetaEntity.EmisGisType t : EmisMetaEntity.EmisGisType.values())
        {
            if (entity.getGisType() == t)
                foundIndex = index;

            index++;
        }

        this.uiGisType.setSelectedIndex(foundIndex);
    }

    public EmisMetaEntity get()
    {
        if (this.entity == null)
        {
            return null;
        }
        int index = this.uiGisType.getSelectedIndex();
        if (index == -1)
            this.entity.setGisType(EmisMetaEntity.EmisGisType.NONE);
        else
        {
            this.entity.setGisType(EmisMetaEntity.EmisGisType.valueOf(this.uiGisType.getItemText(index)));
        }
        return this.entity;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.EmisMetaEntityEditor JD-Core
 * Version: 0.6.0
 */