package com.emistoolbox.server.cli;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.EmisToolboxServiceImpl;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.renderer.pdfreport.html.HTMLReportWriter;
import com.emistoolbox.server.servlet.ApiBaseServlet;
import com.emistoolbox.server.servlet.ApiPdfServlet;
import com.emistoolbox.server.servlet.ApiPdfServlet.ObjectRef;

public class PdfReportRenderer 
{
	public static void main(String[] args)
		throws Exception
	{
		try { 
			System.out.println("Usage: [dataset] [key=value]* [-html]");

			boolean htmlRenderer = ArrayUtils.contains(args, "-html"); 
			
	        EmisDataSet emis = EmisToolboxIO.loadDataset(args[0]); 
	        if (emis == null)
	        	throw new IllegalArgumentException("Failed to load dataset '" + args[0] + "'");
	
	        Map<String, String> params = getParameters(args, 1); 
	
	        List<String> hierarchyList = NamedUtil.getNames(emis.getMetaDataSet().getHierarchies()); 
	        String error = ApiBaseServlet.hasParameters(params, hierarchyList, null, null, null); 
	        if (error != null)
	        {
	        	System.out.println("ERROR: " + error); 
	        	return; 
	        }
	        
	        ObjectRef<String> outError = new ObjectRef<String>(); 
	        ReportMetaResult metaResult = ApiPdfServlet.getReportMetaResult(params, outError, emis); 
	        if (outError.get() != null)
	        {
	        	System.out.println("ERROR: " + outError.get()); 
	        	return; 	
	        }
	        
	        String filename = null; 
	        if (htmlRenderer)
	        	filename = EmisToolboxServiceImpl.getRenderedReportResultInternal(args[0], metaResult,new HTMLReportWriter().setChartRenderer(EmisToolboxServiceImpl.getChartRenderer()));
	        else
	        	filename = EmisToolboxServiceImpl.getRenderedReportResultInternal(args[0], metaResult);

	        System.out.println("PDF Path: " + filename); 
		}
		finally { System.out.flush(); }
	}

	private static Map<String, String> getParameters(String[] args, int index)
	{
		Map<String, String> result = new HashMap<String, String>();
		while (index < args.length)
		{
			int pos = args[index].indexOf("="); 
			if (pos == -1)
				continue; 
			
			result.put(args[index].substring(0, pos), args[index].substring(pos + 1)); 
			index++; 
		}
		
		return result; 
	}
}
