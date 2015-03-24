package com.emistoolbox.server.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class RedirectServlet extends HttpServlet 
{
	private static final String INIT_DATASETS = "datasets"; 
	private static final String INIT_ROLES = "roles"; 
	private static final String QS_DATASET = "dataset"; 
	private static final String QS_ROLE = "role"; 
	private static final String QS_SHOW = "show"; 
	
	private String[] roles;
	private String[] allRoles = new String[] { "admin", "report", "viewer"} ;
	private String[] datasets; 

	@Override
	public void init() 
		throws ServletException 
	{
		ServletConfig config = getServletConfig(); 
		if (!StringUtils.isEmpty(config.getInitParameter(INIT_DATASETS)))
			datasets = config.getInitParameter(INIT_DATASETS).split(","); 
		
		if (!StringUtils.isEmpty(config.getInitParameter(INIT_ROLES)))
			roles = config.getInitParameter(INIT_ROLES).split(","); 
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException 
	{
		String role = get(req, QS_ROLE, roles); 
		validate(QS_ROLE, role, allRoles); 
		
		String dataset = get(req, QS_DATASET, datasets); 
		String ip = req.getRemoteAddr();

		String qsShow = ""; 
		if (!StringUtils.isEmpty(req.getParameter(QS_SHOW)))
			qsShow = "&show=" + req.getParameter(QS_SHOW); 
		
		resp.sendRedirect("/EmisToolbox.html?token=" + ApiUserAuthServlet.getToken(role, ip, dataset) + qsShow);
	}
	
	private String get(HttpServletRequest req, String qs, String[] allowed)
	{
		String value = req.getParameter(qs);
		if (StringUtils.isEmpty(value))
			throw new IllegalArgumentException("Expected a value for '" + qs + "'");

		validate(qs, value, allowed); 
		return value; 
	}
	
	private void validate(String name, String value, String[] allowed)
	{
		for (int i = 0; i < allowed.length; i++)
			if (allowed[i].equals(value))
				return;

		throw new IllegalArgumentException("Invalid value for parameter '" + name + "'");
	}
}
