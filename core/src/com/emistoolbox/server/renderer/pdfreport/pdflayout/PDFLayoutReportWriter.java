package com.emistoolbox.server.renderer.pdfreport.pdflayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.lib.pdf.specification.PDFLayout;
import com.emistoolbox.lib.pdf.specification.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.specification.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.specification.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.specification.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.specification.PDFLayoutSides;
import com.emistoolbox.lib.pdf.specification.PDFLayoutTextContent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutVerticalAlignment;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;

public class PDFLayoutReportWriter implements PdfReportWriter 
{
	private ChartRenderer chartRenderer; 
	
	public PDFLayoutReportWriter(ChartRenderer chartRenderer)
	{ this.chartRenderer = chartRenderer; }
	
	@Override
	public void writeReport(PdfReport report, File out) 
		throws IOException, PdfReportWriterException 
	{
		PdfReportConfig config = report.getReportConfig(); 
		
		Point pageSize = getPageSize(config);
		Rectangle margins = getMargins(config); 
		Point marginCells = getCellMargins(config); 

		List<PDFLayout> pages = new ArrayList<PDFLayout>(); 
		for (PdfPage page : report.getPages())
		{
			page.layout(pageSize, margins, marginCells);
			pages.add(getPage(page, pageSize, margins)); 
		}
		
		render(out, pages); 
	}

	private void render(File out, List<PDFLayout> pages)
	{}
	
	private PDFLayoutSides<Double> getSides(Rectangle values)
	{
		PDFLayoutSides<Double> result = new PDFLayoutSides<Double>(); 
		result.setLeft(values.xmin);
		result.setTop(values.ymin);
		result.setRight(values.xmax);
		result.setBottom(values.ymax);
		
		return result; 
	}
	
	private PDFLayout getPage(PdfPage page, Point size, Rectangle margins)
	{
		PDFLayout layout = new PDFLayout();  
		PDFLayoutFrame frame = new PDFLayoutFrame(); 
		frame.setRectangle(new Rectangle(0, 0, size.x, size.y));
		frame.setMargins(getSides(margins)); 

		List<PDFLayoutComponent> components = new ArrayList<PDFLayoutComponent>();
		PDFLayoutComponent component = createTitles(frame, page.getTitle(), page.getSubtitle()); 
		if (component != null)
			components.add(component); 
		
		component = createFooter(frame, page.getFooter());
		if (component != null)
			components.add(component); 
		
		for (PdfContent content : page.getContents())
		{
		}
		
		frame.setComponents(components);
		layout.setOuterFrame(frame);
		
		return layout; 
	}
	
	private PDFLayoutComponent createTitles(PDFLayoutFrame frame, String title, String subtitle)
	{
		return null; 
	}
	
	private PDFLayoutComponent createFooter(PDFLayoutFrame frame, String footer)
	{ return getText(footer, null, PDFLayoutObjectFit.NONE, PDFLayoutHorizontalAlignment.CENTER, PDFLayoutVerticalAlignment.BOTTOM); }
	
	private PDFLayoutComponent getText(String text, String font, PDFLayoutObjectFit fit, PDFLayoutHorizontalAlignment horizontal, PDFLayoutVerticalAlignment vertical)
	{
		PDFLayoutTextContent content = new PDFLayoutTextContent(); 
		content.setText(text);
		
		PDFLayoutComponent result = new PDFLayoutComponent(); 
		result.setContent(content);
		result.setObjectFit(fit);
		PDFLayoutAlignmentPlacement placement = new PDFLayoutAlignmentPlacement(); 
		placement.setHorizontalAlignment(horizontal);
		placement.setVerticalAlignment(vertical);
		result.setPlacement(placement);
		
		return result; 
	}
	
	private Point getPageSize(PdfReportConfig config)
	{
		Point result = new Point(); 
		switch (config.getPageSize())
		{
		case A4: 
			result = new Point(595, 842);
			break; 
			
		case A5: 
			result = new Point(420, 595);
			break; 

		case LETTER: 
			result = new Point(612, 792);
			break; 
			
		}

		if (config.getOrientation() == PageOrientation.LANDSCAPE)
			result = new Point(result.y, result.x); 
		
		return result; 
	}
	
	private Rectangle getMargins(PdfReportConfig config)
	{
		int margin = config.getPageSize() == PageSize.A5 ? 24 : 36; 
		return new Rectangle(margin, margin, margin, margin); 
	}
	
	private Point getCellMargins(PdfReportConfig config)
	{ return new Point(10, 10); }
	
	@Override
	public void setDateInfo(ReportMetaResult metaInfo) 
	{
	}
}
