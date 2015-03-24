package com.emistoolbox.server.mapping;

import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.DbRowDateAccess;
import com.emistoolbox.common.model.mapping.EmisAccessException;
import com.emistoolbox.common.model.mapping.EmisDbMap;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMap;
import com.emistoolbox.common.model.mapping.EmisHierarchyDbMapEntry;
import com.emistoolbox.common.model.mapping.EntityBaseDbMap;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.ImportStatus;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.EmisGisEntityDataSet;
import com.emistoolbox.server.model.impl.DataSetImpl;
import com.emistoolbox.server.model.impl.EntityBooleanAccess;
import com.emistoolbox.server.model.impl.EntityByteAccess;
import com.emistoolbox.server.model.impl.EntityDataAccess;
import com.emistoolbox.server.model.impl.EntityEnumAccess;
import com.emistoolbox.server.model.impl.EntityEnumSetAccess;
import com.emistoolbox.server.model.impl.EntityIntegerAccess;
import com.emistoolbox.server.model.impl.EntityStringAccess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class MapProcess
{
    private int id = -1; 
    private String dataset; 
    private boolean finished = false;
    private Throwable err;
    private int majorSteps = 0;
    private int majorStepsDone = 0;
    private int subSteps = 0;
    private EmisDataSet data;
    private List<String> statusMessages = new ArrayList<String>();
    private List<String> errorMessages = new ArrayList<String>();
    
    private int errorCountMajorStep = -1; 
    private int errorCount = 0; 
    private int totalErrorCount = 0; 
    private int maxErrorCount = -1; 

    private Date created = new Date(); 
    private Thread thread; 
    
    public MapProcess(EmisMeta metaData) 
    { this(metaData, -1); }
    
    public void stop()
    {
        if (thread != null && !thread.isInterrupted())
        {
            thread.interrupt(); 
            finished = true; 
        }
    }
    
    public MapProcess(EmisMeta metaData, int maxErrorCount) 
    {
        this.maxErrorCount = maxErrorCount; 
        this.data = new DataSetImpl();
        ((DataSetImpl) this.data).setMetaDataSet(metaData);
    }

    public int getId()
    { return id; } 
    
    public void setId(int id)
    { this.id = id; } 
    
    public String getDataset()
    { return dataset; } 
    
    public void setDataset(String dataset)
    { this.dataset = dataset; } 

    public Thread getThread()
    { return thread; } 
    
    public void setThread(Thread thread)
    { this.thread = thread; } 
    
    public EmisDataSet getData()
    {
        return this.data;
    }
    
    public Date getCreatedDate()
    { return created; } 
    
    private synchronized void logStatus(String message)
    {
        System.out.println(message);
        this.statusMessages.add(message);
    }

    private synchronized void logWarn(String message)
    {
        if (errorCountMajorStep != majorStepsDone)
        {
            logErrorCount(); 
            
            errorCountMajorStep = majorStepsDone; 
            errorCount = 0; 
        }

        errorCount++; 
        totalErrorCount++; 
        
        if (errorCount == maxErrorCount)
            errorMessages.add("Reached maximum of errors");  

        if (maxErrorCount != -1 && errorCount >= maxErrorCount)
            return; 

        this.errorMessages.add(message);
    }

    public List<String> getStatusMessages()
    {
        return this.statusMessages;
    }

    public List<String> getErrorMessages()
    {
        return this.errorMessages;
    }

    public void logErrorCount()
    {
        if (errorCount >= maxErrorCount && maxErrorCount != -1)
            errorMessages.add("Total errors for step: " + errorCount); 
    }
    
    public void clearErrors()
    { errorMessages.clear(); }
    
    public synchronized ImportStatus getImportStatus(int fromMessage)
    { return new ImportStatus(this.finished, this.majorStepsDone, this.majorSteps, this.subSteps, totalErrorCount, this.statusMessages, this.err); }

    public void importData(EmisDbMap map) throws IOException
    {
        try
        {
            this.majorSteps = map.getEntityMappings().size();
            this.majorSteps += map.getGisEntityMappings().size();

            for (EmisHierarchyDbMap hierarchyMap : map.getHierarchyMappings())
                this.majorSteps += hierarchyMap.getMappings().size();
            
            for (GisEntityDbMap gisEntityMap : map.getGisEntityMappings())
            {
                importData(gisEntityMap);
                this.majorStepsDone += 1;
                if (isInterrupted())
                    return; 
            }

            for (EmisEntityDbMap entityMap : map.getEntityMappings())
            {
                importData(entityMap);
                this.majorStepsDone += 1;
                if (isInterrupted())
                    return; 
            }

            for (EmisHierarchyDbMap hierarchyMap : map.getHierarchyMappings())
            {
                importData(hierarchyMap);
                if (isInterrupted())
                    return; 
            }

            this.finished = true;
        }
        catch (Throwable err)
        { this.err = getGwtException(err); }
    }

    /** GWT doesn't allow serialization of all exceptions. We transform those that don't work (and we know off) here. 
     *  (TODO can we check programmatically which is valid). 
     */
    private Throwable getGwtException(Throwable err)
    {
    	err.printStackTrace(System.out);
    	if (err instanceof FileNotFoundException)
    		err = new IOException(err.getMessage() + " not found.");

    	return err; 
    }
    
    
    private boolean isInterrupted()
    {
        if (thread == null)
            return false; 
        
        return thread.isInterrupted(); 
    }

    private String getInfo(EmisEntityDbMap entityMap)
    {
        StringBuffer result = new StringBuffer();
        result.append(entityMap.getEntity().getName());
        result.append(", ");
        result.append(entityMap.getDateEnum().getName());
        result.append(" with ");
        for (String key : entityMap.getFieldAccess().keySet())
        {
            result.append(key).append(" ");
        }
        return result.toString();
    }

    private String getInfo(GisEntityDbMap gisEntityMap)
    {
        StringBuffer result = new StringBuffer();
        result.append("GIS for ");
        result.append(gisEntityMap.getEntity().getName());
        return result.toString();
    }

    public void importData(GisEntityDbMap gisEntityMap) throws IOException
    {
        this.subSteps = 0;
        logStatus("importing " + getInfo(gisEntityMap));

        EmisGisEntityDataSet gisData = this.data.getGisEntityDataSetWithCreate(gisEntityMap.getEntity());

        EmisMetaEntity.EmisGisType gisType = gisEntityMap.getEntity().getGisType();
        if (gisType == EmisMetaEntity.EmisGisType.NONE)
        {
            return;
        }
        String info = gisEntityMap.getEntity().getName();
        if (gisEntityMap.getIdAccess() == null)
        {
            logWarn("No ID access specified. Skipping import for " + info);
            return;
        }

        if (gisEntityMap.getDbContext() == null)
        {
            logWarn("No database specified. Skipping import for " + info);
            return;
        }

        DbDataSource dbSource = DbUtil.getDataSource(gisEntityMap.getDbContext().getDataSource(), dataset);

        if (dbSource == null)
        {
            return;
        }
        
        DbResultSet rs = null; 
        try { 
            rs = dbSource.query(gisEntityMap.getDbContext().getQuery());
            rs.setAccessColumns(getColumns(gisEntityMap));

            DbRowAccess idAccess = gisEntityMap.getIdAccess();
            DbRowAccess primaryAccess = gisEntityMap.getPrimaryAccess();
            DbRowAccess secondaryAccess = gisEntityMap.getSecondaryAccess();

            int count = 0;
            while (rs.next())
            {
                this.subSteps += 1;
                Map<String, String> dbRow = rs.getAllValues();
                int entityId = idAccess.getValueAsInt(0, -1, dbRow);
                String primary = primaryAccess.getValue(0, dbRow);
                if (entityId == -1)
                {
                    logWarn("No entity id given. Skipping row " + count + ". " + dbRow + info(idAccess));
    
                    count++;
                    continue;
                }
    
                if (StringUtils.isEmpty(primary))
                {
                    logWarn("No primary coordinate given. Skipping row " + count + ". " + dbRow + info(primaryAccess));
    
                    count++;
                    continue;
                }
    
                String secondary = null;
                if (secondaryAccess != null)
                {
                    secondary = secondaryAccess.getValue(0, dbRow);
                }
                if ((secondaryAccess != null) && (StringUtils.isEmpty(secondary)))
                {
                    logWarn("No secondary coordinate given. Skipping row " + count + ". " + dbRow + info(secondaryAccess));
    
                    count++;
                    continue;
                }
    
                double[] result = null;
                if ((primary != null) && (secondary != null))
                {
                    result = new double[] { parseDouble(primary), parseDouble(secondary) };
                }
                else
                {
                    result = parseDoubles(primary);
                }
                if ((gisType == EmisMetaEntity.EmisGisType.COORDINATE) && (result.length != 2))
                {
                    result = Arrays.copyOf(result, 2);
                }
                gisData.setGisData(entityId, result);
                count++;
            }
    
            logStatus(" => imported " + count + " records.");
        }
        finally 
        { 
            if (rs != null)
                rs.close(); 
        } 
    }
    
    private double[] parseDoubles(String doublesValue)
    {
        if (StringUtils.isEmpty(doublesValue))
        {
            return new double[0];
        }
        String[] values = doublesValue.split(",");
        double[] result = new double[values.length];

        for (int i = 0; i < values.length; i++)
        {
            result[i] = parseDouble(values[i]);
        }
        return result;
    }

    private double parseDouble(String doubleValue)
    {
        if (StringUtils.isEmpty(doubleValue))
        {
            return (0.0D / 0.0D);
        }
        boolean parsedAsDouble = false;
        if (doubleValue.indexOf(".") != -1)
        {
            try
            {
                return Double.parseDouble(doubleValue);
            }
            catch (Throwable err)
            {
                parsedAsDouble = true;
            }
        }
        try
        {
            return Double.longBitsToDouble(Long.parseLong(doubleValue, 16));
        }
        catch (Throwable err)
        {
            if (!parsedAsDouble)
                try
                {
                    return Double.parseDouble(doubleValue);
                }
                catch (Throwable ex)
                {
                }
        }
        return (0.0D / 0.0D);
    }

    private void importData(EmisEntityDbMap entityMap) throws IOException
    {
        this.subSteps = 0;
        logStatus("importing " + getInfo(entityMap));

        EmisMetaEntity entity = entityMap.getEntity();
        EmisMetaDateEnum dateEnum = entityMap.getDateEnum();

        EmisEntityDataSet entityDataSet = this.data.getEntityDataSetWithCreate(entity, dateEnum);

        String info = entityMap.getEntity().getName() + " for " + entityMap.getDateEnum().getName();

        if (entityMap.getIdAccess() == null)
        {
            logWarn("No ID access specified. Skipping import for " + info);
            return;
        }
        if (entityMap.getDateAccess() == null)
        {
            logWarn("No date access specified. Skipping import for " + info);
            return;
        }
        if (entityMap.getDbContext() == null)
        {
            logWarn("No DB context specified. Skipping import for " + info);
            return;
        }

        DbDataSource dbSource = DbUtil.getDataSource(entityMap.getDbContext().getDataSource(), dataset);
        if (dbSource == null)
            return;

        DbContext dbContext = entityMap.getDbContext();

        DbResultSet rs = null; 

        errorCount = 0;  
        try { 
            rs = dbSource.query(dbContext.getQuery()); 
            int count = processQuery(dbContext, entityMap, entityDataSet, rs, 0); 
            logStatus(" => imported " + count + " records.");
            if (errorCount > 0)
                logStatus("    (with " + errorCount + " errors)"); 
        }
        finally 
        {
            if (rs != null)
                rs.close(); 
        }
    }
    
    private int processQuery(DbContext dbContext, EmisEntityDbMap entityMap, EmisEntityDataSet entityDataSet, DbResultSet rs, int countOffset)
        throws IOException
    {
        int count = 0;  
        
        if (dbContext.getLoopVariable() != null && dbContext.getLoopEnum() != null && dbContext.getLoopValues() != null && dbContext.getLoopValues().length > 0)
            rs = new DbResultSetLoop(dbContext.getLoopVariable(), dbContext.getLoopValues(), rs);
        rs.setAccessColumns(getColumns(entityMap));

        Map<String, DbRowAccess> fieldAccess = entityMap.getFieldAccess();

        String[] fields = new String[fieldAccess.size()];
        EntityDataAccess[] dataAccess = new EntityDataAccess[fields.length];
        DbRowAccess[] dbAccess = new DbRowAccess[fields.length];

        int index = 0;
        for (String field : fieldAccess.keySet())
        {
            fields[index] = field;
            dataAccess[index] = entityDataSet.getDataAccess(field);
            dbAccess[index] = fieldAccess.get(field);
            index++;
        }

        while (rs.next())
        {
            this.subSteps += 1;
            Map<String, String> dbRow = rs.getAllValues();
            int entityId = entityMap.getIdAccess().getValueAsInt(0, -1, dbRow);
            int dateId = entityMap.getDateAccess().getValueAsInt(0, -1, dbRow);
            if (entityId == -1 || dateId == -1)
            {
            	if (entityId == -1)
            		logWarn("No entity id given. Skipping row " + (count + countOffset) + ". " + dbRow + info(entityMap.getIdAccess()));

            	if (dateId == -1)
            		logWarn("No date id given. Skipping row " + (count + countOffset) + ". " + dbRow + info(entityMap.getDateAccess()));

            	count++;
                continue;
            }

            EmisEntityData data = entityDataSet.getWithCreate(dateId, entityId);
            importData(data.getMasterArray(), dbRow, count + countOffset, fields, dataAccess, dbAccess);

            count++;
        }
        
        return count; 
    }
    
    private String info(DbRowAccess access)
    { return info(access, null); }
    
    private String info(DbRowAccess access, Integer index)
    { 
    	if (access == null)
    		return " [No access defined]"; 
    	
    	return " [" + access.getInfo() + (index == null ? "" : "(" + index + ")") + "]";  
    }

    private Set<String> getColumnsBase(EntityBaseDbMap entityMap)
    {
        Set<String> columns = new HashSet<String>();
        addColumns(columns, entityMap.getIdAccess());
        return columns;
    }

    private Set<String> getColumns(GisEntityDbMap gisEntityMap)
    {
        Set<String> columns = getColumnsBase(gisEntityMap);
        addColumns(columns, gisEntityMap.getPrimaryAccess());
        addColumns(columns, gisEntityMap.getSecondaryAccess());
        return columns;
    }

    private Set<String> getColumns(EmisEntityDbMap entityMap)
    {
        Set<String> columns = getColumnsBase(entityMap);
        addColumns(columns, entityMap.getDateAccess());
        for (Map.Entry<String, DbRowAccess> entry : entityMap.getFieldAccess().entrySet())
        {
            addColumns(columns, (DbRowAccess) entry.getValue());
        }
        return columns;
    }

    private Set<String> getColumns(EmisHierarchyDbMapEntry hierarchyEntry)
    {
        Set<String> columns = new HashSet<String>();
        addColumns(columns, hierarchyEntry.getChildAccess());
        addColumns(columns, hierarchyEntry.getParentAccess());
        addColumns(columns, hierarchyEntry.getDateAccess());

        return columns;
    }

    private void addColumns(Set<String> columns, DbRowAccess access)
    {
        if (access != null)
            access.addColumns(columns);
    }

    private void importData(Object[] masterArray, Map<String, String> dbRow, int row, String[] fields, EntityDataAccess[] dataAccess, DbRowAccess[] dbAccess)
    {
        for (int i = 0; i < fields.length; i++)
            if (dataAccess[i] == null)
            {
                logWarn("Failed to find dataAccess for field '" + fields[i] + "' (" + i + "). ");
            }
            else if (dbAccess[i] == null)
            {
                if (i == 0)
                {
                    logWarn("Failed to find dbAccess for field '" + fields[i] + "'. ");
                }

            }
            else if (dataAccess[i].getSize() == 1)
                setValue(masterArray, dataAccess[i], 0, dbRow, row, dbAccess[i], 0);
            else
                setArray(masterArray, dataAccess[i], dbRow, row, dbAccess[i]);
    }

    private void setValue(Object[] masterArray, EntityDataAccess dataAccess, int index, Map<String, String> dbRow, int row, DbRowAccess dbAccess, int valueIndex)
    {
        if ((dataAccess instanceof EntityBooleanAccess))
        {
            int val = dbAccess.getValueAsInt(valueIndex, -1, dbRow);
            if (val == -1)
            {
                String tmp = dbAccess.getValue(valueIndex, dbRow);
                ((EntityBooleanAccess) dataAccess).set(masterArray, index, Boolean.parseBoolean(tmp));
            }
            else
            {
                ((EntityBooleanAccess) dataAccess).set(masterArray, index, val != 0);
            }
        }
        else if ((dataAccess instanceof EntityByteAccess))
        {
            ((EntityByteAccess) dataAccess).set(masterArray, index, (byte) dbAccess.getValueAsInt(valueIndex, -1, dbRow));
        }
        else if ((dataAccess instanceof EntityIntegerAccess))
        {
            ((EntityIntegerAccess) dataAccess).set(masterArray, index, dbAccess.getValueAsInt(valueIndex, -1, dbRow));
        }
        else if ((dataAccess instanceof EntityStringAccess))
        {
            ((EntityStringAccess) dataAccess).set(masterArray, index, dbAccess.getValue(valueIndex, dbRow));
        }
        else if ((dataAccess instanceof EntityEnumAccess))
        {
            EmisMetaEnum enumType = ((EntityEnumAccess) dataAccess).getEnumType();
            int value = dbAccess.getValueAsInt(valueIndex, -1, dbRow);
            if ((value >= 0) && (value < enumType.getSize()))
                ((EntityEnumAccess) dataAccess).set(masterArray, index, (byte) value);
            else
                ((EntityEnumAccess) dataAccess).set(masterArray, index, dbAccess.getValue(valueIndex, dbRow));
        }
        else if (dataAccess instanceof EntityEnumSetAccess)
        {
        	EmisMetaEnum enumType = ((EntityEnumSetAccess) dataAccess).getEnumType(); 
        	int value = dbAccess.getValueAsInt(valueIndex, -1, dbRow); 
        	if (value >= 0 && value < 1 << (enumType.getSize()))
        		((EntityEnumSetAccess) dataAccess).set(masterArray, index, value); 
        	else
        		((EntityEnumSetAccess) dataAccess).set(masterArray,  index, dbAccess.getValue(valueIndex, dbRow)); 
        }
    }

    private void setArray(Object[] masterArray, EntityDataAccess dataAccess, Map<String, String> dbRow, int row, DbRowAccess dbAccess)
    {
        if (!(dbAccess instanceof DbRowArrayAccess))
        {
            logWarn("Expected access for array to be DbRowArrayAccess.");
            return;
        }

        DbRowAccess dbValueAccess = ((DbRowArrayAccess) dbAccess).getValueAccess();
        for (int valueIndex = 0; valueIndex < dbAccess.getValuesPerRow(); valueIndex++)
        {
            try { 
            	int index = ((DbRowArrayAccess) dbAccess).getIndex(valueIndex, dbRow);
            	if (index == -1)
                    logWarn("Failed to find enum for row " + row + ". "  + dbRow + info(dbAccess, valueIndex));
            	else
            		setValue(masterArray, dataAccess, index, dbRow, row, dbValueAccess, valueIndex);
            }
            catch (EmisAccessException ex)
            {
                logWarn("Failed to find enum. " + ex.getMessage() + ". " + info(ex.getAccess()) + " for row " + row + ". "  + dbRow + info(dbAccess, valueIndex));
                return;
            }

        }
    }

    public void importData(EmisHierarchyDbMap hierarchyMap) throws IOException
    {
        for (EmisHierarchyDbMapEntry mapEntry : hierarchyMap.getMappings())
        {
            importData(hierarchyMap, mapEntry);
            this.majorStepsDone += 1;
        }
    }

    private String getInfo(EmisHierarchyDbMap hierarchyMap, EmisHierarchyDbMapEntry hierarchyEntry)
    {
        StringBuffer result = new StringBuffer();
        result.append(hierarchyMap.getHierarchy().getName());
        result.append(": ");
        result.append(hierarchyEntry.getParentEntity().getName());
        result.append(" -> ");
        result.append(hierarchyEntry.getChildEntity().getName());

        return result.toString();
    }

    public void importData(EmisHierarchyDbMap hierarchyMap, EmisHierarchyDbMapEntry hierarchyEntry) throws IOException
    {
        this.subSteps = 0;
        logStatus("importing " + getInfo(hierarchyMap, hierarchyEntry));


        DbRowDateAccess dateAccess = hierarchyEntry.getDateAccess();
        DbRowAccess parentAccess = hierarchyEntry.getParentAccess();
        DbRowAccess childAccess = hierarchyEntry.getChildAccess();

        if ((parentAccess == null) || (childAccess == null) || (hierarchyEntry.getDbContext() == null))
        {
            logWarn("Skipped import: Missing mapping for hierarchy entry " + hierarchyEntry.getParentEntity().getName() + " -> " + hierarchyEntry.getChildEntity().getName());

            return;
        }

        DbContext dbContext = hierarchyEntry.getDbContext();
        DbDataSource dbSource = DbUtil.getDataSource(dbContext.getDataSource(), dataset);
        
        int errorCountOffset = errorCount; 
        int count = processImport(hierarchyMap, dbSource, dbContext, hierarchyEntry, dbContext.getQuery()); 
        logStatus("=> imported " + count + " records with " + (errorCount - errorCountOffset) + " errors.");
    }
    
    private int processImport(EmisHierarchyDbMap hierarchyMap, DbDataSource dbSource, DbContext dbContext, EmisHierarchyDbMapEntry hierarchyEntry, String query)
        throws IOException
    {
        TemporaryChildrenList[] results = new TemporaryChildrenList[hierarchyEntry.getDateType().getSize()];
        DbRowDateAccess dateAccess = hierarchyEntry.getDateAccess();
        DbRowAccess parentAccess = hierarchyEntry.getParentAccess();
        DbRowAccess childAccess = hierarchyEntry.getChildAccess();

        DbResultSet rs = dbSource.query(query);
        int row = 0;
        try {
            if (dbContext.getLoopVariable() != null && (dbContext.getLoopEnum() != null || (dbContext.getLoopValues() != null && dbContext.getLoopValues().length > 0)))
                rs = new DbResultSetLoop(dbContext.getLoopVariable(), dbContext.getLoopValues(), rs);
            rs.setAccessColumns(getColumns(hierarchyEntry));
            while (rs.next())
            {
                this.subSteps += 1;
                row++;
                Map<String, String> dbRow = rs.getAllValues();
                int dateIndex = dateAccess.getValueAsInt(0, -1, dbRow);
                if (dateIndex == -1)
                {
                    logWarn("No date information found - skipping row " + row + ". " + dbRow + info(dateAccess));
                    continue;
                }
    
                if (results[dateIndex] == null)
                    results[dateIndex] = new TemporaryChildrenList();
    
                int parentId = parentAccess.getValueAsInt(0, -1, dbRow); 
                int childId = childAccess.getValueAsInt(0, -1, dbRow); 
                if (!results[dateIndex].addChild(parentId, childId))
                {
                	if (parentId == -1)
                		logWarn("Failed to find parentId " + row + ". " + dbRow + info(parentAccess));

                	if (childId == -1)
                		logWarn("Failed to find childId in row " + row + ". " + dbRow + info(childAccess));
                    continue;
                }
            }
        }
        finally 
        { 
            if (rs !=  null)
                rs.close(); 
        }

        EmisHierarchy hierarchy = this.data.getHierarchyWithCreate(hierarchyMap.getHierarchy());
        int parentEntityIndex = hierarchyMap.getHierarchy().getEntityOrder().indexOf(hierarchyEntry.getParentEntity());

        for (int i = 0; i < results.length; i++)
        {
            if (results[i] == null)
            {
                continue;
            }
            for (Integer parentId : results[i].keySet())
                hierarchy.setChildren(i, parentEntityIndex, parentId.intValue(), results[i].getChildren(parentId.intValue()));
        }

        return row; 
    }
}
