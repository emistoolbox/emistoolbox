package com.emistoolbox.server.renderer.pdfreport.html;

import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;
import com.emistoolbox.server.util.TableWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResultToTableGeneratorImpl implements ResultToTableGenerator
{
    private final String TABLE = "table";
    private final String TABLE_BODY = "tbody";
    private final String TABLE_HEADER = "th";
    private final String TABLE_ROW = "tr";
    private final String TABLE_DATA = "td";

    private boolean indent = false;

    public void setIndent(boolean indentOn)
    {
        this.indent = indentOn;
    }

    public String getHtmlTableAsString(Result result) 
        throws TransformerConfigurationException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getHtmlTable(result, stream);
        return stream.toString();
    }

    public void getHtmlTable(Result result, OutputStream output) 
        throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element table = doc.createElement("table");
        table.setAttribute("class", "emisResult");

        doc.appendChild(table);

        Element thead = doc.createElement("thead");
        Element tbody = doc.createElement("tbody");

        if (result.getDimensions() == 1)
        {
            thead.appendChild(getColumnHeadingRow(new String[] { result.getValueLabel() }, doc, 1));
            create1DTable(result, doc, tbody);
            table.appendChild(thead);
            table.appendChild(tbody);
        }
        else
        {
            String[] headings = result.getHeadings(1);

            if (result.getDimensions() == 2)
                create2DTable(result, doc, table, headings, thead, tbody);
            else if (result.getDimensions() == 3)
                create3DTable(result, doc, table, headings, thead, tbody);
        }
        documentToStream(output, doc);
    }

    private void create3DTable(Result result, Document doc, Element table, String[] headings, Element thead, Element tbody)
    {
        String[] subHeadings = result.getHeadings(2);

        thead.appendChild(getColumnHeadingRow(headings, doc, subHeadings.length));
        thead.appendChild(getSubColumnHeadingRow(headings.length, subHeadings, doc));
        table.appendChild(thead);

        String[] rowHeadings = result.getHeadings(0);

        int[] indexes = new int[3];
        for (int i = 0; i < rowHeadings.length; i++)
        {
            Element tr = doc.createElement("tr");

            Element th = doc.createElement("th");
            th.appendChild(doc.createTextNode(rowHeadings[i]));
            tr.appendChild(th);

            for (int j = 0; j < headings.length; j++)
            {
                for (int k = 0; k < subHeadings.length; k++)
                {
                    indexes[0] = i;
                    indexes[1] = j;
                    indexes[2] = k;
                    appendTaggedData(doc, tr, "td", ServerUtil.getFormattedValue(result, indexes), headings[j], rowHeadings[i]);
                }
            }

            tbody.appendChild(tr);
        }
        table.appendChild(tbody);
    }

    private void create2DTable(Result result, Document doc, Element table, String[] headings, Element thead, Element tbody)
    {
        thead.appendChild(getColumnHeadingRow(headings, doc, 1));
        table.appendChild(thead); 
        
        String[] rowHeadings = result.getHeadings(0);

        int[] indexes = new int[2];
        for (int i = 0; i < rowHeadings.length; i++)
        {
            Element tr = doc.createElement("tr");

            Element th = doc.createElement("th");
            th.appendChild(doc.createTextNode(rowHeadings[i]));
            tr.appendChild(th);

            for (int j = 0; j < headings.length; j++)
            {
                indexes[0] = i;
                indexes[1] = j;

                appendTaggedData(doc, tr, "td", ServerUtil.getFormattedValue(result, indexes), headings[j], rowHeadings[i]);
            }
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);
    }

    private void create1DTable(Result result, Document doc, Element table)
    {
        String[] headings = result.getHeadings(0);

        int[] indexes = new int[1];
        for (int i = 0; i < result.getDimensionSize(0); i++)
        {
            Element tr = doc.createElement("tr");
            indexes[0] = i;
            appendData(doc, tr, "th", headings[i]);

            appendTaggedData(doc, tr, "td", ServerUtil.getFormattedValue(result, indexes), "default", headings[i]);
            table.appendChild(tr);
        }
    }

    private Element getColumnHeadingRow(String[] headings, Document doc, int colspan)
    {
        Element tr = doc.createElement("tr");

        appendData(doc, tr, "th", "");

        for (int i = 0; i < headings.length; i++)
        {
            Element element = appendData(doc, tr, "th", headings[i]);
            if (colspan <= 1)
                continue;
            element.setAttribute("colspan", Integer.toString(colspan));
        }
        return tr;
    }

    private Element getSubColumnHeadingRow(int headingLength, String[] subHeadings, Document doc)
    {
        Element tr = doc.createElement("tr");

        appendData(doc, tr, "th", "");

        for (int i = 0; i < headingLength; i++)
        {
            for (int j = 0; j < subHeadings.length; j++)
                appendData(doc, tr, "th", subHeadings[j]);
        }
        return tr;
    }

    private Element appendData(Document doc, Element rootElement, String elementType, String data)
    { return appendTaggedData(doc, rootElement, elementType, data, null, null); } 
    
    private Element appendTaggedData(Document doc, Element rootElement, String elementType, String data, String seriesName, String xName)
    {
        Element element = doc.createElement(elementType);
        if (seriesName != null)
        	element.setAttribute("data-series", seriesName);

        if (seriesName != null)
        	element.setAttribute("data-x", xName);
        
        element.appendChild(doc.createTextNode(data));
        rootElement.appendChild(element);
        return element;
    }

    private void documentToStream(OutputStream output, Document doc) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty("omit-xml-declaration", "yes");
        tf.setOutputProperty("indent", this.indent ? "yes" : "no");
        tf.setOutputProperty("encoding", "utf-8");
        tf.setOutputProperty("method", "html");

        tf.transform(new DOMSource(doc), new StreamResult(output));
    }

    public void getFileTable(Result result, TableWriter writer)
    {
        if (result.getDimensions() == 1)
        {
            writer.nextCell("");
            writer.nextCell(result.getValueLabel());
            writer.nextRow();

            render1DFileTable(result, writer);
        }
        else
        {
            if (result.getDimensions() == 2)
                render2DFileTable(result, writer);
            else
                render3DFileTable(result, writer);
        }

        writer.close();
    }

    private void render1DFileTable(Result result, TableWriter writer)
    {
        String[] headings = result.getHeadings(0);

        int[] indexes = new int[1];
        for (int i = 0; i < result.getDimensionSize(0); i++)
        {
            indexes[0] = i;
            writer.nextCell(headings[i]);
            writer.nextCell(ServerUtil.getFormattedValue(result, indexes));
            writer.nextRow();
        }
    }

    private void render2DFileTable(Result result, TableWriter writer)
    {
        String[] headings = result.getHeadings(1);
        writer.nextCell("");
        for (String heading : headings)
            writer.nextCell(heading);
        writer.nextRow();

        String[] rowHeadings = result.getHeadings(0);

        int[] indexes = new int[2];
        for (int i = 0; i < rowHeadings.length; i++)
        {
            writer.nextCell(rowHeadings[i]);
            for (int j = 0; j < headings.length; j++)
            {
                indexes[0] = i;
                indexes[1] = j;

                writer.nextCell(ServerUtil.getFormattedValue(result, indexes));
            }

            writer.nextRow();
        }
    }

    private void render3DFileTable(Result result, TableWriter writer)
    {
        String[] headings = result.getHeadings(1);
        String[] subHeadings = result.getHeadings(2);

        writer.nextCell("");
        for (String heading : headings)
        {
            writer.nextCell(heading);
            for (int i = 1; i < subHeadings.length; i++)
                writer.nextCell(heading);

        }
        writer.nextRow();

        writer.nextCell("");
        for (int i = 0; i < headings.length; i++)
            for (String subHeading : subHeadings)
                writer.nextCell(subHeading);

        writer.nextRow();

        String[] rowHeadings = result.getHeadings(0);

        int[] indexes = new int[3];
        for (int i = 0; i < rowHeadings.length; i++)
        {
            writer.nextCell(rowHeadings[i]);
            for (int j = 0; j < headings.length; j++)
            {
                for (int k = 0; k < subHeadings.length; k++)
                {
                    indexes[0] = i;
                    indexes[1] = j;
                    indexes[2] = k;
                    writer.nextCell(ServerUtil.getFormattedValue(result, indexes));
                }
            }

            writer.nextRow();
        }
    }

    @Override
    public String getHtmlTableAsString(GisFeatureSet features, String format)
        throws TransformerConfigurationException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getHtmlTable(features, format, stream);
        return stream.toString();
    }

    private void getHtmlTable(GisFeatureSet features, String format, OutputStream out)
        throws TransformerConfigurationException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element table = doc.createElement(TABLE);
        table.setAttribute("class", "emisResult");
        doc.appendChild(table);

        Element tbody = doc.createElement(TABLE_BODY);
        table.appendChild(tbody);

        Element row = doc.createElement(TABLE_ROW);
        tbody.appendChild(row);

        Element tag = doc.createElement(TABLE_HEADER);
        row.appendChild(tag);
        
        tag =doc.createElement(TABLE_HEADER);
        tag.setTextContent(features.getIndicator().getName());
        row.appendChild(tag);

        for (int i = 0; i < features.getCount(); i++)
        {
            row = doc.createElement(TABLE_ROW);
            table.appendChild(row);
            
            tag = doc.createElement(TABLE_HEADER);
            tag.setTextContent(features.getTitle(i));
            row.appendChild(tag);
            
            tag = doc.createElement(TABLE_DATA);
            tag.setTextContent(ServerUtil.getFormattedValue(format, features.getValue(i)));
            row.appendChild(tag);
        }
        
        documentToStream(out, doc);
    }

}
