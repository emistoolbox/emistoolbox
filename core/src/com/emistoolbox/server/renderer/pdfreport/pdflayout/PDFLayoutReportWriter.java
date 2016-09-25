package com.emistoolbox.server.renderer.pdfreport.pdflayout;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutBorderConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVisitor;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutFrame;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPage;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOFileOutput;
import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.io.Util;

public class PDFLayoutReportWriter implements PdfReportWriter 
{
	private ChartRenderer chartRenderer; 
	private boolean debug = false; 
	
	public PDFLayoutReportWriter(ChartRenderer chartRenderer)
	{ this.chartRenderer = chartRenderer; }
	
	public void setDebug(boolean debug)
	{ this.debug = debug; }
	
	@Override
	public void writeReport(PdfReport report, File out) 
		throws IOException, PdfReportWriterException 
	{
		if (!(report.getReportConfig() instanceof LayoutPdfReportConfig))
			throw new IllegalArgumentException("Can only write LayoutPdfReportConfig reports."); 

		LayoutPdfReportConfig config = (LayoutPdfReportConfig) report.getReportConfig(); 
		
		Point pageSize = getPageSize(config);
		Rectangle margins = getMargins(config); 

		List<PDFLayout> pages = new ArrayList<PDFLayout>(); 
		for (EmisPdfPage page : report.getPages())
		{
			if (page instanceof LayoutPage)
				pages.add(renderPage((LayoutPage) page, pageSize, margins)); 
		}

		PDFLayoutVisitor visitor = new PDFLayoutLogVisitor(); 
		for (PDFLayout page : pages)
			visitor.visit(page); 
		
		render(out, pages); 
	}

	private void render(File out, List<PDFLayout> pages)
		throws IOException
	{ new PDFLayoutRenderer().render(pages, new IOFileOutput(out, "application/pdf", null)); }
	
	private PDFLayoutSides<Double> getSides(Rectangle values)
	{
		PDFLayoutSides<Double> result = new PDFLayoutSides<Double>(); 
		result.setLeft(values.xmin);
		result.setTop(values.ymin);
		result.setRight(values.xmax);
		result.setBottom(values.ymax);
		
		return result; 
	}
	
	private PDFLayout renderPage(LayoutPage page, Point size, Rectangle margins)
	{
		PDFLayout layout = new PDFLayout();  
		PDFLayoutFrame pageFrame = new PDFLayoutFrame(); 
		pageFrame.setRectangle(new Rectangle(0, 0, size.x, size.y));
		pageFrame.setMargins(getSides(margins)); 

		List<PDFLayoutComponent> components = new ArrayList<PDFLayoutComponent>();
		PDFLayoutComponent component = createTitles(pageFrame, page.getText(PdfText.TEXT_TITLE), page.getText(PdfText.TEXT_SUBTITLE)); 
		if (component != null)
			components.add(component); 
		
		component = createFooter(pageFrame, page.getText(PdfText.TEXT_FOOTER));
		if (component != null)
			components.add(component); 
		
		for (LayoutFrame frame : page.getFrames())
		{
			component = createFrame(frame);
			if (component != null)
				components.add(component); 
		}
		
		pageFrame.setComponents(components);
		layout.setOuterFrame(pageFrame);
		
		return layout; 
	}
	
	private PDFLayoutComponent createFrame(LayoutFrame frame)
	{
		PDFLayoutComponent component = new PDFLayoutComponent(); 
		setPlacement(component, frame.getFrameConfig().getPosition());

		PDFLayoutFrame pdfFrame = createFrame(frame.getFrameConfig()); 
		component.setContent(pdfFrame);
		
		return component; 
	}
	
	private void setPlacement(PDFLayoutComponent component, com.emistoolbox.common.util.Rectangle rect)
	{
		// TODO
	}
	
	private PDFLayoutFrame createFrame(LayoutFrameConfig config)
	{
		PDFLayoutFrame result = new PDFLayoutFrame(); 
		result.setBorderRadius((double) config.getBorderRadius());
		result.setColors(getColors(config.getBorders())); 
//		result.setLineWidths(lineWidths);
//		result.setMargins();
//		result.setRectangle();
		
		return null; 
	}
	
	private PDFLayoutSides<Color> getColors(LayoutSides<LayoutBorderConfig> borderSides)
	{
		if (borderSides == null)
			return null; 
		
		PDFLayoutSides<Color> result = new PDFLayoutSides<Color>(); 

		LayoutBorderConfig[] borders = borderSides.getValues(new LayoutBorderConfig[4]); 
		Color[] colors = new Color[4]; 
		
		for (int i = 0; i < 4; i++)
		{
			if (borders[i] != null)
				colors[i] = getColor(borders[i].getColour()); 
		}
		
		result.set(colors); 
		return result; 
	}
	
	private Color getColor(ChartColor color)
	{ return new Color(color.getRed(), color.getGreen(), color.getBlue()); }
	
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
	
	private Point getPageSize(LayoutPdfReportConfig config)
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
	
	private Rectangle getMargins(LayoutPdfReportConfig config)
	{
		int margin = config.getPageSize() == PageSize.A5 ? 24 : 36; 
		return new Rectangle(margin, margin, margin, margin); 
	}
	
	private Point getCellMargins(LayoutPdfReportConfig config)
	{ return new Point(10, 10); }
	
	@Override
	public void setDateInfo(ReportMetaResult metaInfo) 
	{
	}
}
