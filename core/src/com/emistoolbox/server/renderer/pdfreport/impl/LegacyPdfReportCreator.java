package com.emistoolbox.server.renderer.pdfreport.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.results.MetaResultDimensionUtil;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.server.renderer.charts.impl.ChartUtil;
import com.emistoolbox.server.renderer.pdfreport.EmisPageGroup;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.results.TableResultCollector;
import com.emistoolbox.server.util.TemplateEngine;

public class LegacyPdfReportCreator extends BasePdfReportCreator<PdfReportConfig> 
{
	@Override
	protected int getPageCountPerEntity()
	{
		List<PdfContentConfig> contents = config.getContentConfigs();
        int totalPages = contents.size() / (config.getRows() * config.getColumns());
        if (contents.size() % (config.getRows() * config.getColumns()) != 0)
            totalPages++;

        return totalPages; 
	}
	
	protected void addEntityPages(EmisEntity entity, int[] ids, String[] names, int totalPages, EmisPageGroup group, TemplateEngine templates)
	{
		init(entity, ids, names);
		
    	String subtitle = MetaResultDimensionUtil.getTitle(metaResult, MetaResultDimensionUtil.ENTITY_DATE_LEVEL.NAMES, false, true);

    	List<PdfContentConfig> contents = new ArrayList<PdfContentConfig>(); 
    	contents.addAll(config.getContentConfigs());
    	
        int i = 0; 
        while (i < contents.size())
        {
            PdfPage page = getNewPage(subtitle, "Page " + (reportResult.getPages().size() + 1) + "/" + totalPages);
    		reportResult.addPage(page);
    		group.addPage(page);

            for (int row = 0; row < config.getRows(); row++)
            {
                for (int col = 0; col < config.getColumns(); col++)
                {
                	if (i >= contents.size())
                        return;

                	page.addContent(row, col, createContent(contents.get(i), templates));
                	i++; 
                }
            }
        }
    }

	private PdfPage getNewPage(String subtitle, String footer)
    {
        PdfPage page = new PdfPageImpl();
        page.setLayout(config.getRows(), config.getColumns());
        page.putText(PdfText.TEXT_TITLE, config.getText(PdfText.TEXT_TITLE)); 
        page.putText(PdfText.TEXT_SUBTITLE, subtitle);

        if (StringUtils.isEmpty(config.getText(PdfText.TEXT_FOOTER)))
            page.putText(PdfText.TEXT_FOOTER, footer);
        else if (StringUtils.isEmpty(footer))
            page.putText(PdfText.TEXT_FOOTER, config.getText(PdfText.TEXT_FOOTER));
        else
            page.putText(PdfText.TEXT_FOOTER, config.getText(PdfText.TEXT_FOOTER) + " - " + footer);

        return page;
    }
}
