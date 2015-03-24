package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emistoolbox.common.util.ImportStatus;
import com.emistoolbox.server.ImportProcessList;
import com.emistoolbox.server.mapping.MapProcess;

public class ImportStatusServlet extends HttpServlet
{
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException
    {
        if (null != req.getParameter("stop"))
        {
            ImportProcessList.get().get(new Integer(req.getParameter("stop"))).stop();
            resp.sendRedirect("importStatus"); 
            return; 
        }
        
        PrintWriter out = resp.getWriter(); 
        
        out.println("<html><body>"); 
        out.println("<h2>Import Process Status</h2>"); 
        
        boolean rendered = false; 
        try { 
            show(out, ImportProcessList.get().get(new Integer(req.getParameter("processId"))), true); 
            rendered = true; 
        } 
        catch (Throwable err)
        {}

        if (!rendered)
        {
            boolean showAll = ImportProcessList.get().getAll().size() == 1; 
            out.println("<ul>"); 
            for (MapProcess process : ImportProcessList.get().getAll())
            {
                out.println("  <li>"); 
                show(out, process, showAll); 
                out.println("  </li>"); 
            }
            out.println("</ul>"); 
        }

        out.println("</body></html>"); 
    }

    private void show(PrintWriter out, MapProcess process, boolean showAll)
    {
        out.println("<b>" + process.getId() + " - " + process.getDataset() + "</b><br>");
        if (!showAll)
            out.println("<a href='importStatus?processId=" + process.getId() + "'>show all</a>"); 
        out.println("    <table border='1' cellpadding='3px' cellspacing='0'>"); 
        ImportStatus status = process.getImportStatus(0); 
        out(out, "Created", "" + process.getCreatedDate()); 
        out(out, "Status", getStatus(process, status));   
        out(out, "Tasks", status.getDoneTaskCount() + "/" + status.getTaskCount()); 
        out(out, "Errors", "" + status.getErrorCount());

        if (showAll) 
        {
            if (status.getException() != null)
            out(out, "Exception", status.getException().getMessage()); 
       
            out.println("  <tr valign='top'><td>Messages</td><td><ul>"); 
            List<String> messages = status.getMessages(); 
            int start = Math.max(0, messages.size() - 25);
            for (int i = start; i < messages.size(); i++) 
                out.println("<li>" + messages.get(i) + "</li>"); 
            out.println("</ul></td></tr>"); 
        }        

        out.println("    </table>"); 
    }
    private String getStatus(MapProcess process, ImportStatus status)
    {
        String stopLink = "<a href='importStatus?stop=" + process.getId() + "'>stop now</a>"; 
        if (status.getException() != null)
            return "error"; 
        
        if (process.getThread() != null && process.getThread().isInterrupted())
            return "interrupted"; 
        
        return status.isFinished() ? "finished" : "running " + stopLink;
    }
    private void out(PrintWriter out, String col1, String col2)
    {
        out.print("<tr valign='top'><td>"); 
        out.print(col1); 
        out.print("</td><td>"); 
        out.print(col2); 
        out.print("</td></tr>"); 
    }
}
