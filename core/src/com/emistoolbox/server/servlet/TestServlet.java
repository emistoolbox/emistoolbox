package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.results.impl.MetaResultUtil;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.model.EmisDataSet;

public class TestServlet extends ApiBaseServlet 
{
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException
    {
    	// Simple testing
    	if ("ping".equals(req.getParameter("action")))
    	{
    		PrintWriter out = resp.getWriter(); 
    		out.println("pong"); 
    		out.flush(); 
    		return; 
    	}
    	
        String dataset = req.getParameter(QS_DATASET); 
        if (dataset == null)
        	dataset = "default"; 
        
        EmisDataSet emis = EmisToolboxIO.loadDataset(dataset); 
        if (emis == null)
        {
            error(resp, 400, "No EMIS data loaded.", asJson(req)); 
            return; 
        }
        
        String reportId = req.getParameter(QS_REPORT); 
        EmisReportConfig reportConfig = EmisToolboxIO.loadReportXml(dataset, emis.getMetaDataSet());
        for (EmisPdfReportConfig pdfConfig : reportConfig.getPdfReports())
        {
        	if (reportId == null || reportId.equals(pdfConfig.getName()))
        	{
        		Set<EmisMetaDateEnum> enums = MetaResultUtil.getUsedDateTypes(pdfConfig);
        		enums = enums;
        	}
        }
    }
}
