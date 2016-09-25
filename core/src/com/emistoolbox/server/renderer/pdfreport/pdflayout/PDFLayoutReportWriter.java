package com.emistoolbox.server.renderer.pdfreport.pdflayout;

import java.awt.Color;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;

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
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;
import com.emistoolbox.lib.pdf.util.PDFLayoutLogVisitor;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContent;
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
		OutputStream os = null; 
		try 
		{
			os = ioo.getOutputStream(); 
			ObjectOutputStream oos = new ObjectOutputStream(os); 
			oos.writeObject(pages);
			oos.flush(); 
		}
		finally 
		{ IOUtils.closeQuietly(os); }
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
		
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement(size.x - margins.xmin - margins.xmax, size.y - margins.ymin - margins.ymax);
		for (LayoutFrame frame : page.getFrames())
			outerFrame.addElement(createFrame(frame)); 
		
		outerFrame.addElement(createAlignedText(page.getText(PdfText.TEXT_FOOTER), PDFLayoutHorizontalAlignment.CENTER, PDFLayoutVerticalAlignment.BOTTOM));
		
		layout.setOuterFrame(outerFrame); 
		return layout; 
	}

	private PDFLayoutElement createAlignedText(String title, PDFLayoutHorizontalAlignment horizontal, PDFLayoutVerticalAlignment vertical)
	{
		if (StringUtils.isEmpty(title))
			return null; 
		return new PDFLayoutTextElement(title, null).align(horizontal, vertical); 
	}
		
	private PDFLayoutElement createFrame(LayoutFrame frame)
	{
		PDFLayoutFrameElement result = new PDFLayoutFrameElement(); 
		LayoutFrameConfig config = frame.getFrameConfig(); 

		setFramePlacement(result, config); 
		setFrameBorder(result, config); 
		setFramePadding(result, config); 
		setFrameBackground(result, config);
		
		
		PDFLayoutElement contentElement = null; 
		PdfContent content = frame.getContent();
		if (content instanceof PdfImageContent)
			contentElement = new PDFLayoutTextElement("IMAGE - to be implemented", null);  
		else if (content instanceof PdfChartContent)
			contentElement = new PDFLayoutTextElement("CHART - to be implemented", null);  
		else if (content instanceof PdfTextContent)
			contentElement = new PDFLayoutTextElement(((PdfTextContent) content).getText(), null); 
		else if (content instanceof PdfVariableContent)
			contentElement = new PDFLayoutTextElement("VARIABLES - to be implemented", null);  
		else if (content instanceof PdfTableContent)
			contentElement = new PDFLayoutTextElement("TABLE - to be implemented", null);  
		
		return result; 
	}
	
	private void setFramePlacement(PDFLayoutFrameElement el, LayoutFrameConfig config)
	{
		el.position(config.getPosition().getLeft(), config.getPosition().getRight()); 
		el.setWidth(config.getPosition().getWidth() - config.getPadding().getLeft() - config.getPadding().getRight());
		el.setHeight(config.getPosition().getHeight() - config.getPadding().getTop() - config.getPadding().getBottom());
	}

	private void setFrameBorder(PDFLayoutElement el, LayoutFrameConfig config)
	{
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
	}
	
	private void setFrameBackground(PDFLayoutElement el, LayoutFrameConfig config)
	{}
	
	private void setFrameObjectFit(PDFLayoutElement el, LayoutFrameConfig config)
	{}

	private void setFramePadding(PDFLayoutElement el, LayoutFrameConfig config)
	{ el.setPadding(new PDFLayoutSides<Double>(config.getPadding().getValues(new Double[4]))); }
	
	private Color getColor(ChartColor color)
	{ return new Color(color.getRed(), color.getGreen(), color.getBlue()); }
	
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
