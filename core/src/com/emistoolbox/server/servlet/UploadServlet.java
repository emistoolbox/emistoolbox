package com.emistoolbox.server.servlet;

import com.emistoolbox.server.ServerUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadServlet extends HttpServlet
{
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (!ServletFileUpload.isMultipartContent(req))
        {
            return;
        }
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        PrintWriter out = resp.getWriter();
        try
        {
            for (FileItem item : (List<FileItem>) upload.parseRequest(req))
            {
                if ((item.isFormField()) || (!"text/xml".equals(item.getContentType())))
                {
                    continue;
                }
                File output = ServerUtil.getNewFile("temp", "metaResultUpload", ".xml");
                item.write(output);

                out.println("url=content?temp=" + output.getName());
                return;
            }

            out.println("error=No uploaded file provided.");
        }
        catch (Exception ex)
        {
            out.println("error=Failed to upload file. " + ex.getMessage());
        }
        out.flush();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.servlet.UploadServlet JD-Core Version:
 * 0.6.0
 */