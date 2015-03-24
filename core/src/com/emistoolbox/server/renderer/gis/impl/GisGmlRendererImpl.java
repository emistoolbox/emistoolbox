package com.emistoolbox.server.renderer.gis.impl;

import java.io.IOException;
import java.io.PrintWriter;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public class GisGmlRendererImpl extends GisMultiFileRendererImpl
{
    public GisGmlRendererImpl()
    { super(".gml", "application/gml+xml"); } 
    
//    synchronized public void renderMap(List<GisFeatureSet> featureSets, List<ColourScheme> colours, ChartConfig chartConfig, File outputFile)
//        throws IOException
//    {
//        super.renderMap(featureSets, colours, chartConfig, outputFile); 
//        // jb2do - output code to combine all files into one. 
//    }

    protected void renderFeatureSet(GisFeatureSet features, ColourScheme colours, boolean setRange, boolean showLabels, boolean selectable)
        throws IOException
    {
        PrintWriter out = getOut(features); 
        boolean asPoints = hasPoints(features); 
        
        outputGmlHeader(out); 
        outputBoundary(out, features.getBoundary()); 
        
        for (int i = 0; i < features.getCount(); i++)
        {
            out.println("<gml:featureMember>"); 
            if (asPoints)
                out.println("  <ms:point fid=\"" + i + "\">");
            else
                out.println("  <ms:polygon fid=\"" + i + "\">");
            
            double[] coords = features.getFeature(i);
            outputBoundary(out, GisFeatureSetImpl.getBoundary(coords, null));  
            
            out.println("    <ms:msGeometry>"); 
            if (asPoints)
            {
                out.println("      <gml:Point srsName=\"EPSG:4326\">"); 
                outputCoordinates(out, coords); 
                out.println("      </gml:Point>"); 
            }
            else
            {
                out.println("      <gml:MultiPolygon srsName=\"EPSG:4326\">"); 
                out.println("        <gml:polygonMember>"); 
                out.println("          <gml:Polygon>"); 
                out.println("            <gml:outerBoundaryIs><gml:LinearRing>"); 
                outputCoordinates(out, coords); 
                out.println("            </gml:LinearRing></gml:outerBoundaryIs>"); 
                out.println("          </gml:Polygon>"); 
                out.println("        </gml:polygonMember>"); 
                out.println("      </gml:MultiPolygon>"); 
            }
            
            out.println("    </ms:msGeometry>"); 
            // <ms:ogc_fid>1</ms:ogc_fid>
            out.println("    <ms:name>" + features.getTitle(i) + "</ms:name>"); 
            out.println("    <ms:id>" + features.getId(i) + "</ms:id>"); 

            if (asPoints)
                out.println("  </ms:point>"); 
            else
                out.println("  </ms:polygon>");
            
            out.println("</gml:featureMember>"); 
        }
        
        outputGmlFooter(out); 
        
        out.flush(); 
        out.close(); 
        out = null; 
    }
    
    private void outputBoundary(PrintWriter out, double[] coords)
    {
        if (coords.length != 4)
            throw new IllegalArgumentException("Boundary double[] of invalid size: " + coords.length); 
        
        out.println("  <gml:boundedBy>"); 
        out.println("    <gml:Box srsName=\"EPSG:4326\">");
        outputCoordinates(out, coords); 
        out.println("    </gml:Box>"); 
        out.println("  </gml:boundedBy>"); 
    }
    
    private void outputCoordinates(PrintWriter out, double[] coords)
    {
        out.print("<gml:coordinates>"); 
        for (int i = 0; i < coords.length - 1; i += 2)
        {
            if (i != 0)
                out.print(" "); 
            out.print(coords[i]); 
            out.print(","); 
            out.print(coords[i + 1]);
        }
        out.print("</gml:coordinates>"); 
    }
    
    private void outputGmlHeader(PrintWriter out)
    {
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"); 
        out.print("<wfs:FeatureCollection xmlns:ms=\"http://mapserver.gis.umn.edu/mapserver\"");
        out.print(" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\""); 
        out.print(" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""); 
        out.print(" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd "); 
        out.println(" http://mapserver.gis.umn.edu/mapserver http://aneto.oco/cgi-bin/worldwfs?SERVICE=WFS&amp;VERSION=1.0.0&amp;REQUEST=DescribeFeatureType&amp;TYPENAME=polygon&amp;OUTPUTFORMAT=XMLSCHEMA\">"); 
    }
    
    private void outputGmlFooter(PrintWriter out)
    { out.println("</wfs:FeatureCollection>"); 
        
    }
}
