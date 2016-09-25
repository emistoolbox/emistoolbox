package com.emistoolbox.server.renderer.pdfreport.pdflayout;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutBorderConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;
import com.emistoolbox.lib.pdf.util.PDFLayoutLogVisitor;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutFrame;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPage;

import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileOutput;
import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;

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

		if (debug)
		{
			PDFLayoutLogVisitor visitor = new PDFLayoutLogVisitor(); 
			for (PDFLayout page : pages)
				page.accept(visitor);
			
			try { writeObjectStream(out, pages); }
			catch (Throwable err)
			{}
		}
		
		render(out, pages); 
	}

	private void writeObjectStream(File out, List<PDFLayout> pages)
		throws IOException
	{
		String name = out.getName();
		int pos = name.lastIndexOf("."); 
		if (pos != -1)
			name = name.substring(0, pos); 
		
		File objFile = new File(out.getParentFile(), name + ".layout"); 
		
		IOOutput ioo = new IOFileOutput(objFile); 
		try (OutputStream os = ioo.getOutputStream())
		{
			ObjectOutputStream oos = new ObjectOutputStream(os); 
			oos.writeObject(pages);
			oos.flush(); 
		}
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
		pageFrame.setRectangle(new Rectangle(margins.xmin, margins.ymin, size.x - margins.xmin - margins.xmax, size.y - margins.ymin - margins.ymax));

		List<PDFLayoutComponent> components = new ArrayList<PDFLayoutComponent>();
		components.addAll(createAlignedText(page.getText(PdfText.TEXT_TITLE), PDFLayoutHorizontalAlignment.LEFT, PDFLayoutVerticalAlignment.TOP)); 
		components.addAll(createAlignedText(page.getText(PdfText.TEXT_SUBTITLE), PDFLayoutHorizontalAlignment.LEFT, PDFLayoutVerticalAlignment.TOP)); 

		for (LayoutFrame frame : page.getFrames())
		{
			PDFLayoutComponent component = createFrame(frame);
			if (component != null)
				components.add(component); 
		}
		
		components.addAll(createAlignedText(page.getText(PdfText.TEXT_FOOTER), PDFLayoutHorizontalAlignment.CENTER, PDFLayoutVerticalAlignment.BOTTOM));

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
		PDFLayoutBorderStyle  border = new PDFLayoutBorderStyle();
		border.setBorderRadius((double) config.getBorderRadius());
		
		PDFLayoutLineStyle[] lineStyles = new PDFLayoutLineStyle[4]; 
		int i = 0; 
		
		for (LayoutBorderConfig borderConfig : config.getBorders().getValues(new LayoutBorderConfig[0]))
		{
			lineStyles[i] = new PDFLayoutLineStyle((double) borderConfig.getWidth(), getColor(borderConfig.getColour())); 
			i++; 
		}

		border.setLineStyles(new PDFLayoutSides<PDFLayoutLineStyle>(lineStyles));

// TODO		result.setRectangle();
		
		return null; 
	}
	
	private Color getColor(ChartColor color)
	{ return new Color(color.getRed(), color.getGreen(), color.getBlue()); }
	
	private List<PDFLayoutComponent> createAlignedText(String title, PDFLayoutHorizontalAlignment horizontal, PDFLayoutVerticalAlignment vertical)
	{
		List<PDFLayoutComponent> result = new ArrayList<PDFLayoutComponent>(); 
		if (!StringUtils.isEmpty(title))
			result.add(new PDFLayoutComponent(new PDFLayoutTextContent(title, null), null, horizontal, vertical)); 
		
		return result; 
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
	
	@Override
	public void setDateInfo(ReportMetaResult metaInfo) 
	{}
}
