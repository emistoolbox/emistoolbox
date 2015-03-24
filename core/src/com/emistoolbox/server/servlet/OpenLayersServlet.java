package com.emistoolbox.server.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.model.impl.GisContextImpl;
import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.model.meta.GisLayer;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.model.EmisDataSet;

/** 
 * Servlet that provides HTML and supporting files for an OpenLayer display of data. 
 * 
 * Note that this servlet only displays files already created in the UI. 
 * 
 * /map/openlayers?layer=&base=image&dataset=mali&layer=&layer=&layer=
localhost:8888/map/openLayers?layer=gis6164915820568939865_layer_Region.shp&base=image&dataset=mali

 * { type: 'range', min: 0, max: 90, colours: [-4467971,-8344835,-10250243,-13667075,-16295171,-16756751,-16758565,-16760121]}
 * { type: 'const', colour: 9009 }
 * { type: 'classification', thresholds: [], colours: []} 
 */
public class OpenLayersServlet extends HttpServlet
{
    private static final String QS_TYPE = "type"; 
    private static final String QS_LAYER = "layer"; 
    private static final String QS_BASE = "base"; 
    private static final String QS_DATASET = "dataset"; 
    private static final String QS_COLOURS = "colours"; 
    private static final String QS_THRESHOLDS = "thresholds"; 
    private static final String QS_POINTS = "points"; 
    private static final String QS_PATH = "path"; 
    
    private static final String BASE_IMAGE = "image"; 
    private static final String BASE_GOOGLE = "google"; 
    
    private static final String TYPE_BACKGROUND = "background"; 
    private static final String TYPE_FILE = "file"; 

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (TYPE_BACKGROUND.equals(req.getParameter(QS_TYPE)))
        {
            writeBackground(resp, req.getParameter(QS_DATASET)); 
            return; 
        }
        
        if (TYPE_FILE.equals(req.getParameter(QS_TYPE)))
        {
            output(req.getParameter(QS_DATASET), req.getParameter(QS_PATH), resp); 
            return; 
        }
        
        PrintWriter out = resp.getWriter(); 
        out.print(getResource("openLayersHeader.html")); 
        writeBaseLayers(out, req.getParameter(QS_BASE), req.getParameter(QS_DATASET)); 

        out.println("    var thresholds = [" + req.getParameter(QS_THRESHOLDS) + "];");
        out.println("    var colours = [" + getQuotedList(req.getParameter(QS_COLOURS)) + "];");
        out.println("    var asPoints = " + req.getParameter(QS_POINTS) + ";"); 
        out.println("    toolbox.setColourThresholds(colours, thresholds, asPoints);"); 
        
        writeLayers(out, req.getParameterValues(QS_LAYER)); 
        
        out.println("   toolbox.loadAllLayers();"); 
        out.println("   toolbox.zoomToBest();"); 
        
        out.print(getResource("openLayersFooter.html")); 
        out.flush(); 
        out.close(); 
    }

    private String getQuotedList(String list)
    {
        StringBuffer result = new StringBuffer(); 
        
        String delim = ""; 
        for (String item : list.split(","))
        {
            result.append(delim); 
            result.append("'"); 
            result.append(item); 
            result.append("'"); 

            delim = ","; 
        }
        
        return result.toString(); 
    }
    
    private void writeLayers(PrintWriter out, String[] layers)
    {
        for (String layer : layers)
        {
            out.println("   toolbox.addShapefileLayer(\"../content?chart=" + layer + "\", \"" + getLayerName(layer) + "\");"); 
        }
    }
    
    private String getLayerName(String layer)
    {
        int end = layer.lastIndexOf(".shp");
        int start = layer.lastIndexOf("_", end);
        
        return layer.substring(start + 1, end); 
    }
    
    private void writeBaseLayers(PrintWriter out, String base, String dataset)
        throws IOException
    {
        if (dataset == null)
            throw new IllegalArgumentException("Missing parameter " + QS_DATASET);

        if (base == null)
            throw new IllegalArgumentException("Missing parameter " + QS_BASE);

        GisContext gis = getGisContext(dataset); 
        out.println(); 
        out.print("   var bounds = ["); 
        String delim = ""; 
        for (double value : gis.getBaseLayerBoundary())
        {
            out.print(delim); 
            out.print(value); 
            delim = ","; 
        }
        out.println("];"); 
        
        if (BASE_IMAGE.equals(base))
        {
            if (gis.getBaseLayerImage() != null)
            {
                out.print("   toolbox.setBackgroundImage('" + getBaseImageUrl(dataset) + "', bounds, ["); 
                delim = ""; 
                for (int value : gis.getBaseLayerImageSize())
                {
                    out.print(delim); 
                    out.print(value); 
                    delim = ","; 
                }
                out.println("]);");
            }
        }
        else if (BASE_GOOGLE.equals(base))
            out.println("// insert google code here");

        for (GisLayer layer : gis.getGisLayers())
        {
            out.println("   var layerIndex = toolbox.addShapefileLayer(\"openLayers?type=file&path=" + layer.getPath() + "&dataset=" + dataset + "\", \"" + layer.getName() + "\");"); 
            if (!StringUtils.isEmpty(layer.getStyle()))
                out.println("   toolbox.setStyle(layerIndex, " + layer.getStyle() + ");");
        }
    }
    
    private String getBaseImageUrl(String dataset)
    { return "openLayers?" + QS_TYPE + "=background&" + QS_DATASET + "=" + dataset; }
    
    private void writeBackground(HttpServletResponse resp, String dataset)
        throws IOException
    {
        GisContext gis = getGisContext(dataset);
        if (gis.getBaseLayerImage() == null)
        {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND, "Dataset '" + dataset + "' does not have background image.");
            return; 
        }

        output(dataset, gis.getBaseLayerImage(), resp); 
    }
    
    private void output(String dataset, String path, HttpServletResponse resp)
        throws IOException 
    {
        File src = ServerUtil.getFile(dataset, path, false); 

        resp.setContentType(EmisToolboxIO.getContentType(path)); 
        OutputStream os = null; 
        InputStream is = null;  
        try { 
            os = resp.getOutputStream();
            is = new FileInputStream(src);
            IOUtils.copy(is, os); 
        }
        finally {
            IOUtils.closeQuietly(is); 
            IOUtils.closeQuietly(os); 
        }
    }
    
    private GisContext getGisContext(String dataset)
        throws IOException 
    {
        EmisDataSet emis = EmisToolboxIO.loadDataset(dataset);
        return emis.getMetaDataSet().getGisContext(); 
    }
    
    private String getResource(String resourceName)
        throws IOException
    {
        InputStream is = null; 
        try { 
            is = this.getClass().getResourceAsStream(resourceName); 
            if (is == null)
                return null; 
            
            List<String> lines = IOUtils.readLines(is); 
            return StringUtils.join(lines, "\n");
        }
        finally {
            IOUtils.closeQuietly(is); 
        }
    }
}
