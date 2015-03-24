package com.emistoolbox.client.ui;

import java.util.List;

import com.emistoolbox.common.model.EmisEntity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class EntitySelect extends FlexTable implements HasValueChangeHandlers<EmisEntity>
{
    private int columns; 

    public EntitySelect(int columns)
    { this.columns = columns; }

    public void set(List<EmisEntity> entities)
    {
        removeAllRows(); 
        
        int rows = (entities.size() + columns - 1) / columns;
        int entityIndex = 0; 
        
        int col = 0; 
        while (true)
        {
            for (int row = 0; row < rows; row++) 
            {
                if (entityIndex >= entities.size())
                    return; 

                final EmisEntity entity = entities.get(entityIndex); 
                Label lbl = new Label(entity.getName());
                lbl.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event)
                    { ValueChangeEvent.fire(EntitySelect.this, entity); }
                }); 
                setWidget(row, col, lbl); 

                entityIndex++; 
            }
            
            col++; 
        }
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EmisEntity> handler)
    { return addHandler(handler, ValueChangeEvent.getType()); }
}
