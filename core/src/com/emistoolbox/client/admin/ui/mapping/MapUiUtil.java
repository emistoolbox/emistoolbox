package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.DbMapEditor;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowArrayAccessImpl;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class MapUiUtil
{
    public static Widget getMapUiAsWidget(DbRowAccess access, EmisToolboxServiceAsync service, String dataset)
    { return (Widget) getMapUi(access, false, null, service, dataset); }

    public static Widget getMapUiAsWidget(DbRowAccess access, boolean isArray, EmisMetaData field, EmisToolboxServiceAsync service, String dataset)
    { return (Widget) getMapUi(access, isArray, field, service, dataset); }

    public static Widget getMapUiAsWidget(DbRowAccess access, EmisMetaData field, EmisToolboxServiceAsync service, String dataset)
    {
        MapUiWidget result = getMapUi(access, false, field, service, dataset);
        result.setConstraint(getConstraint(field));
        return (Widget) result;
    }

    public static Widget getMapUiAsWidget(DbRowAccess access, EmisMetaEnum targetEnum, boolean asSet, EmisMetaData field, EmisToolboxServiceAsync service, String dataset)
    { return (Widget) getMapUi(access, targetEnum, asSet, field, service, dataset); }

    public static MapUiWidget getMapUi(DbRowAccess access, EmisMetaEnum targetEnum, boolean asSet, EmisMetaData field, EmisToolboxServiceAsync service, String dataset)
    {
    	if (targetEnum == null && field != null)
    	{
    		targetEnum = field.getEnumType(); 
    		asSet = field.getType() == EmisMetaData.EmisDataType.ENUM_SET; 
    	}

    	DbRowAccess fieldAccess = access;
        if ((access instanceof DbRowEnumAccess))
            fieldAccess = ((DbRowEnumAccess) access).getAccess();
        else
            access = new DbRowEnumAccess(fieldAccess, targetEnum);

        MapUiEnumField result = new MapUiEnumField(field, service, asSet, dataset); 
        if (fieldAccess instanceof DbRowByColumnIndexAccess)
            result.addByColumnType(); 
        
        result.set(access);

        return result;
    }

    public static MapUiWidget getMapUi(DbRowAccess access, boolean isArray, EmisMetaData field, EmisToolboxServiceAsync service, String dataset)
    {
        MapUiWidget result = null;
        if (access == null)
        {
            if (isArray)
                result = new MapUiByColumnArray(field, service, dataset);
            else
                result = new MapUiField(service, dataset); 
            result.setMetaContext(field);
        }
        else if (access instanceof DbRowArrayAccess)
        {
            if (field == null)
                throw new IllegalArgumentException(Message.messageAdmin().mapuiuErrorNeedToCreateArray());

            result = new MapUiByColumnArray(field, service, dataset);
            result.setMetaContext(field);
            result.set((DbRowArrayAccess) access);
        }
        else
        {
            if ((access instanceof DbRowDateAccess))
                throw new IllegalArgumentException(Message.messageAdmin().mapuiuErrorNeedToPassDateEnum());

            result = new MapUiField(service, dataset); 
            result.setMetaContext(field);
            result.set(access);
        }

        if (field != null)
            result.setConstraint(getConstraint(field));

        return result;
    }

    public static DbRowArrayAccess createArrayAccess(EmisMetaData field)
    {
        DbRowArrayAccess result = new DbRowArrayAccessImpl();

        EmisMetaEnum[] enums = field.getArrayDimensions().getEnums();
        int[] dimensions = new int[enums.length];
        for (int i = 0; i < enums.length; i++)
        {
            dimensions[i] = enums[i].getSize();
        }
        result.setDimensions(dimensions);
        return result;
    }
    
    public static Widget getMapUiAsWidget(EmisMetaData data, EmisToolboxServiceAsync service, String dataset)
    {
        if (data.getArrayDimensions() != null)
        {
            MapUiByColumnArray result = new MapUiByColumnArray(data, service, dataset);
            
            result.setMetaContext(data);
            return result;
        }

        if (data.getType() == EmisMetaData.EmisDataType.ENUM || data.getType() == EmisMetaData.EmisDataType.ENUM_SET)
        {
            MapUiWidget result = getMapUi(null, null, false, data, service, dataset);
            result.setMetaContext(data);
            return (Widget) result;
        }

        MapUiField result = new MapUiField(service, dataset);
        result.setMetaContext(data);
        result.setConstraint(getConstraint(data));

        return result;
    }

    public static MapUiConstraint getConstraint(EmisMetaData data)
    {
        if (data == null)
        {
            return null;
        }
        switch (data.getType()) {
        case ENUM:
        case ENUM_SET: 
            return new MapUiEnumConstraint(data.getEnumType());

        case STRING:
            return null;
        }

        return new MapUiBasicTypeConstraint(data.getType());
    }

    public static DbRowAccess getAccess(Widget w)
    {
        if ((w instanceof MapUiWidget))
            return ((MapUiWidget) w).get();

        return null;
    }

    public static DbRowDateAccess getDateAccess(Widget w)
    {
        if ((w instanceof MapUiDate))
        {
            return (DbRowDateAccess) ((MapUiDate) w).get();
        }
        return null;
    }

    public static MapUiDbContextEditor getMapUiAsWidget(DbContext context, DbMapEditor dbMapEditor)
    { return getMapUiAsWidget(context, null, dbMapEditor); }
    
    public static MapUiDbContextEditor getMapUiAsWidget(DbContext context, EmisMetaDateEnum loopEnum, DbMapEditor dbMapEditor)
    {
        MapUiDbContextEditor result = new MapUiDbContextEditor(dbMapEditor, loopEnum);
        result.setDataSourceConfigs(dbMapEditor.get().getDataSources());
        result.set(context);

        return result;
    }
    
    public static MapUiDate getMapUiAsWidget(EmisMetaDateEnum dateType, DbRowDateAccess access, EmisToolboxServiceAsync service, String dataset)
    {
        MapUiDate result = new MapUiDate(service, dataset);
        if (access == null)
        {
            access = new DbRowDateAccess();
            access.setDateType(dateType);
        }

        result.set(access);
        return result;
    }

    public static Widget getMapUiAsWidget(DbDataSourceConfig value, List<DbDataSourceConfig> configs)
    {
        if (configs.size() == 0)
        {
            return new Label(Message.messageAdmin().mapuiuLabelNoDataSourceDefined());
        }
        ListBoxWithUserObjects<DbDataSourceConfig> lb = new ListBoxWithUserObjects<DbDataSourceConfig>();
        for (DbDataSourceConfig config : configs)
            lb.add(config.getName(), config);

        if (value != null)
            lb.setValue(value);

        return lb;
    }

    public static DbDataSourceConfig getDataSourceConfig(Widget w)
    {
        if ((w instanceof ListBoxWithUserObjects))
            return ((ListBoxWithUserObjects<DbDataSourceConfig>) w).getUserObject();

        return null;
    }

    public static DbContext getDbContext(Widget w)
    {
        if ((w instanceof MapUiDbContextEditor))
        {
            return ((MapUiDbContextEditor) w).get();
        }
        return null;
    }

    public static String getString(Widget w)
    {
        if ((w instanceof TextBox))
        {
            return ((TextBox) w).getText();
        }
        return null;
    }

    public static TextBox getStringAsWidget(String s)
    {
        TextBox tb = new TextBox();
        tb.setText(s);
        return tb;
    }
}
