package com.emistoolbox.client.admin;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageAdmin;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataFileSource;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigFile;
import com.emistoolbox.common.model.mapping.DbDataSourceConfigHsqldb;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.DbRowConstAccess;
import com.emistoolbox.common.model.mapping.DbRowContextAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.mapping.DbRowFieldAccess;
import com.emistoolbox.common.model.mapping.DbRowMultipleAccess;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigJdbc;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigMultiple;
import com.emistoolbox.common.model.mapping.impl.DbRowAccessMultipleAccessIndex;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowByColumnIndexAccess;
import com.emistoolbox.common.model.mapping.impl.DbRowEnumAccess;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapValidator
{
    private static final String CSS_ERROR = "error";
    private static final String CSS_WARN = "warn";
    private EmisMeta meta;
    private Map<Object, String> messages = new HashMap();

    public MapValidator(EmisMeta meta) {
        this.meta = meta;
    }

    public void validate(EmisDbMap dbMap)
    {
        for (DbDataSourceConfig config : dbMap.getDataSources())
        {
            validate(config, config);
        }
        for (EmisEntityDbMap entityMap : dbMap.getEntityMappings())
        {
            validate(entityMap);
        }
        for (EmisHierarchyDbMap hierarchyMap : dbMap.getHierarchyMappings())
        {
            for (EmisHierarchyDbMapEntry entry : hierarchyMap.getMappings())
                validate(entry);
        }
        for (Iterator i$ = this.meta.getEntities().iterator(); i$.hasNext();)
        {
            EmisMetaEntity entity = (EmisMetaEntity) i$.next();
            for (EmisMetaData data : entity.getData())
            {
                if (!hasMap(dbMap.getEntityMappings(), data))
                    addMessage(entity, Message.messageAdmin().mvErrorNoFieldMap(data.getName()), "error");
            }
        }
    }

    private boolean hasMap(List<EmisEntityDbMap> maps, EmisMetaData data)
    {
        for (EmisEntityDbMap map : maps)
        {
            if (!NamedUtil.sameName(map.getEntity(), data.getEntity()))
            {
                continue;
            }
            for (String field : map.getFieldAccess().keySet())
            {
                if (field.equals(data.getName()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void validate(Object context, DbDataSourceConfig config)
    {
        int index;
        if ((config instanceof DbDataSourceConfigFile))
        {
            DbDataSourceConfigFile fileConfig = (DbDataSourceConfigFile) config;
            List<DbDataFileSource> fileSources = fileConfig.getFileSources();
            if (fileSources.size() == 0)
                addMessage(config, Message.messageAdmin().mvErrorNoFiles(), "error");

            index = 0;
            boolean isHsql = config instanceof DbDataSourceConfigHsqldb; 
            for (DbDataFileSource fileSource : fileSources)
            {
            	String url = fileSource.getUrl(); 
                if (isEmpty(url))
                    addMessage(config, Message.messageAdmin().mvErrorNoPathForEntry(index), "error");
                else if (isHsql && (url.startsWith(DbDataFileSource.PREFIX_HTTP) || url.startsWith(DbDataFileSource.PREFIX_HTTPS) || url.startsWith(DbDataFileSource.PREFIX_MAGPI)))
                {
            		if (isEmpty(fileSource.getCacheFilename()))
            			addMessage(config, Message.messageAdmin().mvErrorNoCachePath(index), "error"); 
                }
                
                index++; 
            }
        }
        else if ((config instanceof DbDataSourceConfigJdbc))
        {
            DbDataSourceConfigJdbc jdbcConfig = (DbDataSourceConfigJdbc) config;
            if (isEmpty(jdbcConfig.getHost()))
                addMessage(config, Message.messageAdmin().mvErrorNoHost(), "error");
            if (isEmpty(jdbcConfig.getDbName()))
                addMessage(config, Message.messageAdmin().mvErrorNoDatabaseName(), "error");
        }
        else if ((config instanceof DbDataSourceConfigMultiple))
        {
            addMessage(config, Message.messageAdmin().mvErrorUnsupportedConfigType(), "warn");
        }
    }

    private void validate(EmisEntityDbMap entityMap)
    {
        validate(entityMap, entityMap.getDbContext());
        validate(entityMap, "id", entityMap.getIdAccess());
        validate(entityMap, "date", entityMap.getDateAccess());
        for (Map.Entry entry : entityMap.getFieldAccess().entrySet())
            validate(entityMap, "field " + (String) entry.getKey(), (DbRowAccess) entry.getValue());
    }

    private void validate(EmisHierarchyDbMapEntry hierarchyEntry)
    {
        validate(hierarchyEntry, hierarchyEntry.getDbContext());
        validate(hierarchyEntry, "date", hierarchyEntry.getDateAccess());
        validate(hierarchyEntry, "parent id", hierarchyEntry.getParentAccess());
        validate(hierarchyEntry, "child id", hierarchyEntry.getChildAccess());
    }

    private boolean isEmpty(String str)
    {
        return (str == null) || (str.equals(""));
    }

    private void addMessage(Object context, String newMessage, String unusedCss)
    {
        String message = (String) this.messages.get(context);
        if (message == null)
            message = newMessage;
        else
        {
            message = message + "<br>" + newMessage;
        }
        this.messages.put(context, message);
    }

    public Map<Object, String> getMessages()
    {
        return this.messages;
    }

    private void validate(Object context, DbContext dbContext)
    {
        if (dbContext == null)
        {
            addMessage(context, Message.messageAdmin().mvErrorNoDatasource(), "error");
            return;
        }

        if (isEmpty(dbContext.getQuery()))
            addMessage(context, Message.messageAdmin().mvErrorNoQuery(), "error");
        validate(context, dbContext.getDataSource());
    }

    private void validate(Object context, String name, DbRowAccess access)
    {
        if (access == null)
        {
            addMessage(context, Message.messageAdmin().mvErrorNotDefined(name), "error");
        }
        else if ((access instanceof DbRowArrayAccess))
        {
            DbRowArrayAccess arrayAccess = (DbRowArrayAccess) access;
            validate(context, name + " ", arrayAccess.getValueAccess());
            validate(context, Message.messageAdmin().mvIndex(name), arrayAccess.getIndexAccess());
        }
        else if ((access instanceof DbRowEnumAccess))
        {
            validate(context, Message.messageAdmin().mvEnumAccess(name), ((DbRowEnumAccess) access).getAccess());
        }
        else if ((access instanceof DbRowDateAccess))
        {
            EmisMetaDateEnum dateType = ((DbRowDateAccess) access).getDateType();
            DbRowAccess[] accesses = ((DbRowDateAccess) access).getAccesses();
            EmisMetaEnum[] enums = dateType.getEnums();
            for (int i = 0; i < accesses.length; i++)
                validate(context, name + "." + enums[i].getName(), accesses[i]);
        }
        else if ((access instanceof DbRowConstAccess))
        {
            if (isEmpty(((DbRowConstAccess) access).getConstValue()))
                addMessage(context, Message.messageAdmin().mvErrorNoConstValue(name), "error");
        }
        else if ((access instanceof DbRowContextAccess))
        {
            if (isEmpty(((DbRowContextAccess) access).getContextParameter()))
                addMessage(context, Message.messageAdmin().mvErrorNoContextVariable(name), "error");
        }
        else if ((access instanceof DbRowFieldAccess))
        {
            if (isEmpty(((DbRowFieldAccess) access).getFieldName()))
                addMessage(context, Message.messageAdmin().mvErrorNoFieldName(name), "error");
        }
        else if ((access instanceof DbRowByColumnAccess))
            ; 
        else if (access instanceof DbRowByColumnIndexAccess)
            ; 
        else
            addMessage(context, Message.messageAdmin().mvWarnUnknownAccess(access.toString()), "warn");
    }

    private void validate(Object context, String name, DbRowAccess[] accesses)
    {
        for (int i = 0; i < accesses.length; i++)
            validate(context, name + " " + i, accesses[i]);
    }

    public String getContextName(Object context)
    {
        if (context == null)
        {
            return Message.messageAdmin().mvContextNone();
        }
        if ((context instanceof EmisMetaEntity))
        {
            return Message.messageAdmin().mvContextMetaEntity(((EmisMetaEntity) context).getName());
        }
        if ((context instanceof DbDataSourceConfig))
        {
            return Message.messageAdmin().mvContextDbConfig(((DbDataSourceConfig) context).getName());
        }
        if (((context instanceof EmisEntityDbMap)) || ((context instanceof EmisHierarchyDbMapEntry)))
        {
            return context.toString();
        }
        return Message.messageAdmin().mvContextUnknown();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.MapValidator JD-Core Version:
 * 0.6.0
 */