package com.emistoolbox.client.ui.validation;

import java.util.HashSet;
import java.util.Set;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.ui.analysis.EnumSetEditor;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.impl.ValidationFilter;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationFilterEditor extends FlexTable implements EmisEditor<EmisValidationFilter>
{
    Set<EmisMetaEnum> enums; 
    
    private PushButton btnAdd = new PushButton("Add Filter"); 
    
    public ValidationFilterEditor(EmisMetaEntity entity)
    {
        // List of enums we allow. 
        for (EmisMetaData field : entity.getData())
            enums = addEnums(enums, field); 
                
        init(); 
    }
    
    public ValidationFilterEditor(EmisMetaData field)
    {
        enums = addEnums(null, field); 
        init(); 
    } 
    
    private void init()
    {
        EmisUtils.init(btnAdd, 80);
        getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT); 
        setWidget(0, 0, btnAdd); 
        btnAdd.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)
            {
                VerticalPanel vp = new VerticalPanel(); 

                final PopupPanel popup = new PopupPanel();
                popup.setModal(true); 
                popup.setAutoHideEnabled(true); 
                popup.add(vp); 
                
                vp.add(new Label("Which classificiation do you want to add? ")); 
                for (final EmisMetaEnum enumType : enums)
                {
                    if (isUsed(enumType))
                        continue; 
                    
                    HTML html = new HTML("<li>" + enumType.getName() + "</li>"); 
                    html.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event)
                        {
                            popup.hide();
                            setEditor(getRowCount(), enumType, null); 
                        }
                    });
                    vp.add(html);
                }

                popup.showRelativeTo(btnAdd); 
            }
        }); 
    }

    private Set<EmisMetaEnum> addEnums(Set<EmisMetaEnum> enums, EmisMetaData field)
    {
        if (enums == null)
            enums = new HashSet<EmisMetaEnum>(); 
    
        if (field.getType() == EmisDataType.ENUM || field.getType() == EmisDataType.ENUM_SET)
            enums.add(field.getEnumType()); 
        
        EmisMetaEnumTuple dims = field.getArrayDimensions();
        if (dims != null)
        {
            for (EmisMetaEnum enumType : dims.getEnums())
                enums.add(enumType); 
        }
        
        return enums; 
    }

    private boolean isUsed(EmisMetaEnum enumType)
    {
        for (int row = 1; row < getRowCount(); row++)
        {
            EnumSetEditor editor = getEditor(row);
            if (editor == null)
                continue; 

            if (NamedUtil.sameName(editor.get().getEnum(), enumType))
                return true; 
        }
        
        return false; 
    }
        
    @Override
    public void commit()
    {}

    private int findRow(EmisMetaEnum enumType)
    {
        for (int row = 1; row < getRowCount(); row++)
        {
            EnumSetEditor editor = getEditor(row); 
            if (editor == null)
                continue; 
            
            EmisEnumSet enumSet = editor.get(); 
            if (enumSet == null)
                continue; 
            
            if (NamedUtil.sameName(enumSet.getEnum(), enumType))
                return row; 
        }
        
        return -1;
    }
    
    private void setEditor(int row, final EmisMetaEnum enumType, EmisEnumSet item)
    {
        if (item == null)
        {
            item = new EnumSetImpl(); 
            item.setEnum(enumType); 
            item.setAll(); 
        }
        
        EnumSetEditor editor = new EnumSetEditor(); 
        editor.set(item); 
        
        HTML btnDel = new HTML("<small>[DEL]</small>");
        btnDel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)
            {
                int i = findRow(enumType); 
                if (i != -1)
                    removeRow(i); 
            }
        }); 

        getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP); 
        setText(row, 0, enumType.getName());
        getFlexCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP); 
        setWidget(row, 1, btnDel); 
        setWidget(row, 2, editor); 
    }
    
    private EnumSetEditor getEditor(int row)
    {
        Widget w = getWidget(row, 2); 
        if (w instanceof EnumSetEditor)
            return (EnumSetEditor) w; 
        
        return null; 
    }

    @Override
    public EmisValidationFilter get()
    {
        commit(); 

        EmisValidationFilter result = new ValidationFilter(); 
        for (int row = 1; row < getRowCount(); row++)
        {
            EnumSetEditor editor = getEditor(row); 
            if (editor != null)
            {
                EmisEnumSet filter = editor.get(); 
                if (!filter.hasAllValues() && filter.getAllIndexes().size() != 0)
                    result.addFilter(filter);  
            }
        }
        
        return result;
    }

    @Override
    public void set(EmisValidationFilter validationFilter)
    {
        while (getRowCount() > 1)
            removeRow(1); 
        
        if (validationFilter == null)
            return; 
        
        int row = 1; 
        for (EmisEnumSet e : validationFilter.getFilters().values())
        {
            setEditor(row, e.getEnum(), e); 
            row++; 
        }
    }
}
