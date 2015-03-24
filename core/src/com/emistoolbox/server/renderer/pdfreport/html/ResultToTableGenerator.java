package com.emistoolbox.server.renderer.pdfreport.html;

import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;
import com.emistoolbox.server.util.TableWriter;

import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

public abstract interface ResultToTableGenerator
{
    public abstract void setIndent(boolean paramBoolean);

    public abstract String getHtmlTableAsString(Result paramResult) throws TransformerConfigurationException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException;

    public abstract void getHtmlTable(Result paramResult, OutputStream paramOutputStream) throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException;

    public abstract void getFileTable(Result result, TableWriter writer);
    
    public abstract String getHtmlTableAsString(GisFeatureSet features, String format)
        throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException;
}

