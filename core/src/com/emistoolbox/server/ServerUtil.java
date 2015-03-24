package com.emistoolbox.server;

import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.EmisGisEntityDataSet;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServerUtil
{
    public static String ROOT_PATH = "c:/emistoolbox/";

    public static File getNewFile(String path, String prefix, String ext) throws IOException
    {
        File parent = new File(EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH_WRITABLE, ROOT_PATH), path);
        parent.mkdirs();
        return File.createTempFile(prefix, ext, parent);
    }

    public static File getFile(String id, String file, boolean writable)
    { return new File(EmisConfig.get(writable ? EmisConfig.EMISTOOLBOX_PATH_WRITABLE : EmisConfig.EMISTOOLBOX_PATH, ROOT_PATH), id == null ? file : id + "/" + file); }

    public static void trace(PrintStream out, EmisDataSet dataSet)
    {
        for (EmisMetaHierarchy h : dataSet.getMetaDataSet().getHierarchies())
            trace(out, dataSet.getHierarchy(h.getName()));

        for (Iterator i$ = dataSet.getMetaDataSet().getEntities().iterator(); i$.hasNext();)
        {
            EmisMetaEntity entity = (EmisMetaEntity) i$.next();

            trace(out, dataSet, entity, dataSet.getGisEntityDataSetWithCreate(entity));
            for (EmisMetaDateEnum date : dataSet.getMetaDataSet().getDateEnums())
                trace(out, dataSet, entity, date, dataSet.getEntityDataSet(entity, date));
        }
    }

    public static void trace(PrintStream out, EmisDataSet dataSet, EmisMetaEntity entity, EmisMetaDateEnum date, EmisEntityDataSet data)
    {
        out.println("== entity=" + entity.getName() + ", date=" + date.getName() + " ==");
        Map<Integer, String> values;
        for (int dateIndex = 0; dateIndex < date.getCombinations(); dateIndex++)
        {
            out.println("  -- date=" + dateIndex + "--");
            if (data == null)
            {
                continue;
            }
            for (String field : data.getFields())
            {
                out.println("    -- field=" + field + " --");
                values = data.getAllValues(dateIndex, field);
                StringBuffer msg = new StringBuffer();
                for (Integer id : values.keySet())
                    out.println("      " + id + "=" + (String) values.get(id));
            }
        }
    }

    public static void trace(PrintStream out, EmisDataSet dataSet, EmisMetaEntity entity, EmisGisEntityDataSet gisData)
    {
        out.println("== entity=" + entity.getName() + ", gisType=" + entity.getGisType() + " ==");
        for (Iterator i$ = gisData.getAllIds().iterator(); i$.hasNext();)
        {
            int entityId = ((Integer) i$.next()).intValue();
            out.println("  " + entityId + "=" + toString(gisData.getGisData(entityId)));
        }
    }

    private static String toString(double[] values)
    {
        if ((values == null) || (values.length == 0))
        {
            return "";
        }
        StringBuffer result = new StringBuffer("[");
        for (int i = 0; i < Math.min(4, values.length); i++)
        {
            if (i > 0)
                result.append(",");
            result.append(values[i]);
        }

        if (values.length > 4)
            result.append("..] size=" + values.length);
        else
        {
            result.append("]");
        }
        return result.toString();
    }

    public static void trace(PrintStream out, EmisHierarchy hierarchy)
    {
        if (hierarchy == null)
        {
            return;
        }
        out.println("== HIERARCHY [" + hierarchy.getMetaHierarchy().getName() + ", " + hierarchy.getDateType().getName() + "] ==");

        List order = hierarchy.getMetaHierarchy().getEntityOrder();
        for (int dateIndex = 0; dateIndex < hierarchy.getDateType().getSize(); dateIndex++)
        {
            out.println("-- dateIndex = " + dateIndex + " --");
            trace(out, hierarchy, order, dateIndex, 0, hierarchy.getRootElements(dateIndex), "  ");
        }
    }

    public static void trace(PrintStream out, EmisHierarchy hierarchy, List<EmisMetaEntity> order, int dateIndex, int entityIndex, int[] ids, String spacer)
    {
        if (ids == null)
        {
            return;
        }
        String newspacer = "  " + spacer;
        EmisMetaEntity entity = (EmisMetaEntity) order.get(entityIndex);
        for (int i = 0; i < ids.length; i++)
        {
            out.println(spacer + entity.getName() + " " + ids[i]);
            trace(out, hierarchy, order, dateIndex, entityIndex + 1, hierarchy.getChildren(dateIndex, entity, ids[i]), newspacer);
        }
    }

    public static String getMemoryUsage()
    {
        String result = "free: " + format(Runtime.getRuntime().freeMemory()); 
        result += ", max: " + format(Runtime.getRuntime().maxMemory());  
        return result; 
    }
    
    private static String format(long mem)
    {
        double dMem = mem; 
        
        String[] units = new String[] {"", "KB", "MB", "GB"}; 
        for (String unit : units)
        {
            if (dMem < 1024)
                return String.format("%.1f %s", dMem, unit);
            
            dMem /= 1024; 
        }
        
        return String.format("%.1f %s", dMem * 1024, units[units.length - 1]); 
    }


    public static String getFormattedValue(Result result, int[] indexes)
    { return ServerUtil.getFormattedValue(result.getFormat(indexes[indexes.length - 1]), result.get(indexes)); }
    
    public static String getFormattedValue(String format, double value)
    {
        if (Double.isNaN(value) || value < 0)
            return "";

        if (format.endsWith("%"))
            return new DecimalFormat(format.substring(0, format.length() - 1)).format(value) + "%";
        
        return new DecimalFormat(format).format(value);
    }

    public static String formatRemovePercent(String format)
    { 
        if (format == null || !format.endsWith("%"))
            return format;

        return format.substring(0, format.length() - 1);
    }
}
