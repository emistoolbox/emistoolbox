package com.emistoolbox.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

public class ApiUserAuthServlet extends ApiBaseServlet
{
    private static final String QS_SECRET = "secret"; 
    private static final String QS_IP = "ip"; 
    private static final String QS_CREDENTIALS = "credentials"; 
    private static final String QS_DATASET = "dataset"; 

    private static final String SYSTEM_AUTH_WITH_IP = "emistoolbox.use_ip"; 
    
    private static final String useIp = System.getProperty(SYSTEM_AUTH_WITH_IP); 

    public static boolean useIp()
    { return useIp == null || new Boolean(useIp); }
    
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        if (!hasAccess(req, resp))
            return; 

        if (useIp() && !hasParameter(req, resp, QS_IP))
            return;

        if (!hasParameter(req, resp, QS_CREDENTIALS, new String[] { "admin", "reportAdmin", "viewer"}))
            return;
        
        if (!hasParameter(req, resp, QS_DATASET))
            return; 
        
        success(req, resp, getToken(req.getParameter(QS_CREDENTIALS), req.getParameter(QS_IP), req.getParameter(QS_DATASET)));  
    }
    
    public static String getToken(String credentials, String ip, String dataset)
    {
        String values = credentials + "-" + (useIp() ? ip : "")+ "-" + dataset; 
        return values + "-" + DigestUtils.md5Hex(values);
    }
    
    public static String[] getTokenValues(String token)
    {
        if (token == null)
            return null; 
        
        String[] values = token.split("-");
        if (values == null || values.length != 4)
            return null; 
            
        if (!token.equals(getToken(values[0], values[1], values[2])))
            return null; 
        
        return new String[] { values[0], values[1], values[2] }; 
    }
    
}
