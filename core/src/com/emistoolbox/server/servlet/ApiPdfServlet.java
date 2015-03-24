package com.emistoolbox.server.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.EmisReportConfig;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.impl.ReportMetaResultImpl;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.EmisToolboxIO;
import com.emistoolbox.server.EmisToolboxServiceImpl;
import com.emistoolbox.server.model.EmisDataSet;

public class ApiPdfServlet extends ApiBaseServlet
{
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException
    {
        if (!hasAccess(req, resp))
            return; 
        
        if (!hasParameter(req, resp, QS_DATASET))
            return;

        if (!hasParameter(req, resp, QS_LOCATION))
            return; 
        
        if (!hasParameter(req, resp, QS_DATE))
            return; 
        
        String dataset = req.getParameter(QS_DATASET); 
        EmisDataSet emis = EmisToolboxIO.loadDataset(dataset); 
        if (emis == null)
        {
            error(resp, 400, "No EMIS data loaded.", asJson(req)); 
            return; 
        }
        
        List<String> hierarchyList = NamedUtil.getNames(emis.getMetaDataSet().getHierarchies()); 
        if (!hasParameters(req, resp, hierarchyList, null, null, null))
            return; 

        ReportMetaResult metaResult = getReportMetaResult(req, resp, emis); 
        if (metaResult == null)
            return; 
        
        String filename = EmisToolboxServiceImpl.getRenderedReportResultInternal(dataset, metaResult); 
        success(req, resp, new String[] { "/emistoolbox/content?report=" + filename, MetaResultDimensionUtil.getTitle(metaResult) });  
    }
    
    private ReportMetaResult getReportMetaResult(HttpServletRequest req, HttpServletResponse resp, EmisDataSet emis)
        throws IOException
    {
        // Verify report ID is correct. 
        EmisReportConfig reportConfig = EmisToolboxIO.loadReportXml(emis.getMetaDataSet().getDatasetName(), emis.getMetaDataSet()); 
        if (!hasParameter(req, resp, QS_REPORT, NamedUtil.getNames(reportConfig.getReports())))
            return null;
        
        ReportMetaResultImpl result = new ReportMetaResultImpl();
        if (!setMetaResult(result, req, resp, emis))
            return null; 

        EmisMetaHierarchy metaHierarchy = result.getHierarchy();
        int[] entityIds = getEntityPathIds(emis, metaHierarchy, req.getParameter(QS_LOCATION), 0); 
        result.setEntityPath(entityIds, getEntityPathNames(emis, entityIds, metaHierarchy.getEntityOrder(), getDateIndex(result.getContext())));
        result.setReportConfig(NamedUtil.find(req.getParameter(QS_REPORT), reportConfig.getReports())); 
        result.setColourScheme(getColourScheme(req.getParameter(QS_REPORT_COLOURS))); 
        
        return result; 
    }
    
    private ChartColor[] getColourScheme(String scheme)
    {
        if ("colors".equals(scheme))
            return ChartConfig.PALLET_VARIED; 
        else if ("yellows".equals(scheme))
            return ChartConfig.PALLET_YELLOW;
        else if ("fills".equals(scheme))
        	return ChartConfig.PALLET_SHADES; 
        else
            return ChartConfig.PALLET_GRAYS;
    }
    
    private int getDateIndex(EmisContext context)
    {
        List<EmisEnumTupleValue> dates = context.getDates();
        if (dates == null || dates.size() == 0)
            return 0; 
        
        return dates.get(0).getIndex()[0]; 
    }
}
