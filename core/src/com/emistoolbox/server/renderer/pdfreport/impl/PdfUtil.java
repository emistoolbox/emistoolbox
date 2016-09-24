package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfGisContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntity;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.renderer.charts.impl.ChartUtil;
import com.emistoolbox.server.renderer.gis.GisUtil;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportCreator;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPdfReportCreator;
import com.emistoolbox.server.results.ResultCollector;
import com.emistoolbox.server.results.TableResultCollector;
import com.gargoylesoftware.htmlunit.javascript.host.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class PdfUtil
{
    public static PdfReport getPdfReport(ReportMetaResult metaResult, EmisDataSet dataSet)
    {
        EmisPdfReportConfig config = metaResult.getReportConfig();
        return getPdfReportCreator(config).create(metaResult, dataSet); 
    }
    
    private static PdfReportCreator getPdfReportCreator(EmisPdfReportConfig config)
    {
    	if (config instanceof LayoutPdfReportConfig)
    		return new LayoutPdfReportCreator(); 
    	else if (config instanceof PdfReportConfig)
    		return new LegacyPdfReportCreator(); 
    	else
    		throw new IllegalArgumentException("Unknown PDF report configuration."); 
    }
}
