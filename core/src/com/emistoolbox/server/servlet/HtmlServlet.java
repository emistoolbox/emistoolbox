package com.emistoolbox.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.emistoolbox.server.EmisConfig;
import com.emistoolbox.server.ServerUtil;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOFileInput;

public class HtmlServlet extends HttpServlet 
{
	private String emisRoot = EmisConfig.get(EmisConfig.EMISTOOLBOX_PATH, ServerUtil.ROOT_PATH); 

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException 
	{
		IOInput in = getInput(req.getPathInfo()); 
		if (in == null)
			throw new IllegalArgumentException("Path not found."); 

		resp.setContentType(in.getContentType());
		OutputStream os = null; 
		InputStream is = null;
		try { 
			os = resp.getOutputStream();
			is = in.getInputStream(); 
			IOUtils.copy(is, os); 
			os.flush(); 
		}
		finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}
	
	public IOInput getInput(String path)
	{
		int offset = 0; 
		if (path.startsWith("/") || path.startsWith("\\"))
			offset = 1;  
		
		int pos = path.indexOf("/", offset); 
		if (pos == -1)
			return null; 

		if (path.indexOf("..") != -1)
			throw new IllegalArgumentException();  
		
		String dataset = path.substring(offset, pos); 
		if (dataset.equals("bin"))
			throw new IllegalArgumentException("Not allowed."); 
		if (dataset.equals("highcharts") || dataset.equals("reports"))
			return new IOFileInput(new File(emisRoot, path.substring(offset)), getContentType(path), null);  

		File datasetDir = new File(emisRoot, dataset);
		if (!datasetDir.exists() || !datasetDir.isDirectory())
			return null;
		
		return new IOFileInput(new File(datasetDir, path.substring(pos + 1)), getContentType(path), null); 
	}
	
	private static String[] exts = new String[] {
		"js",  "json", 
		"html", "txt", "css",
		"png", "jpg", "jpeg",
		"zip",
	}; 
	
	private static String[] mimes = new String[] { 
		"text/javascript", 
		"application/json", 

		"text/html",  
		"text/plain", 
		"text/css", 
		
		"image/png", 
		"image/jpeg", 
		"image/jpeg", 
		
		"application/zip"
	};
	
	private static Map<String, String> contentTypes = null; 
	
	private String getContentType(String path)
	{
		if (contentTypes == null)
		{
			contentTypes = new HashMap<String, String>(); 
			for (int i = 0; i < exts.length; i++)
				contentTypes.put(exts[i], mimes[i]); 
		}
		
		int pos = path.lastIndexOf(".");
		if (pos == -1)
			return null; 
		
		return contentTypes.get(path.substring(pos + 1)); 
	}
}
