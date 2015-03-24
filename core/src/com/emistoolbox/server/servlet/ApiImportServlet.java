package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.util.ImportStatus;
import com.emistoolbox.server.EmisToolboxServiceImpl;
import com.emistoolbox.server.ImportProcessList;
import com.emistoolbox.server.mapping.MapProcess;

public class ApiImportServlet extends ApiBaseServlet 
{
    protected void service(HttpServletRequest req, HttpServletResponse resp)
    	throws ServletException, IOException
    {
        if (!hasAccess(req, resp))
            return; 

    	if (req.getParameter("importId") != null)
    	{
    		int id = Integer.parseInt(req.getParameter("importId")); 
    		success(req, resp, getValues(ImportProcessList.get().get(id))); 

    		return; 
    	}

        String dataset = req.getParameter(QS_DATASET); 
        if (StringUtils.isEmpty(dataset))
        {
            error(resp, 400, "No dataset specified.", asJson(req)); 
            return; 
        }
        
        success(req, resp, getValues(EmisToolboxServiceImpl.runImport(dataset, null))); 
    }
    
    private Map<String, String> getValues(MapProcess process)
    {
    	ImportStatus status = process.getImportStatus(0); 
    	
    	Map<String, String> values = new HashMap<String, String>(); 
    	values.put("id", "" + process.getId()); 
    	values.put("finished", "" + status.isFinished()); 
    	values.put("errors", "" + status.getErrorCount()); 
    	values.put("tasks.count", "" + status.getTaskCount()); 
    	values.put("tasks.subTasks.count", "" + status.getSubTaskCount()); 
    	values.put("tasks.done.count", "" + status.getDoneTaskCount()); 
    	
    	return values; 
    }
}
