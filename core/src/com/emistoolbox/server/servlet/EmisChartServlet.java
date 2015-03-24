package com.emistoolbox.server.servlet;

import com.emistoolbox.server.EmisConfig;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.ServerUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

public class EmisChartServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String name = req.getParameter("chart");
        if (name != null)
        {
            serve("charts", name, EmisToolboxIO.getContentType(name, "image/png"), resp);
            return;
        }

        name = req.getParameter("log");
        if (name != null)
        {
            serve(req.getParameter("dataSet"), name, "text/plain", resp);
            return;
        }

        name = req.getParameter("report");
        if (name != null)
        {
            serve("reports", name, EmisToolboxIO.getContentType(name), resp); 
            return; 
        }

        name = req.getParameter("temp");
        if ((name != null) && (name.endsWith(".xml")))
        {
            serve("temp", name, EmisToolboxIO.getContentType(name), resp);
            return;
        }
    }

    private void serve(String subdir, String filename, String contentType, HttpServletResponse resp) throws IOException
    {
        if (contentType == null)
            return; 
        
        File parent = null;
        if (subdir == null)
            parent = new File(EmisConfig.get("emistoolbox.path.writable", ServerUtil.ROOT_PATH));
        else
            parent = new File(EmisConfig.get("emistoolbox.path.writable", ServerUtil.ROOT_PATH), subdir);

        File file = new File(parent, filename);
        if (!file.exists())
            return;

        if (!file.getParentFile().getCanonicalPath().equals(parent.getCanonicalPath()))
            return;

        resp.setContentType(contentType);
        resp.setHeader("Content-disposition", "attachment; filename=" + filename);

        InputStream is = null;
        try
        {
            is = new FileInputStream(file);
            IOUtils.copy(is, resp.getOutputStream());
            resp.flushBuffer();
        }
        finally
        { IOUtils.closeQuietly(is); }
    }
}

