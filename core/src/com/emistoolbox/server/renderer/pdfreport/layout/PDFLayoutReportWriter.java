package com.emistoolbox.server.renderer.pdfreport.layout;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.TableCellFormat;
import com.emistoolbox.common.TableCellFormat.HorizontalAlign;
import com.emistoolbox.common.TableCellFormat.VerticalAlign;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle.BorderType;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.common.util.Point;
import com.emistoolbox.common.util.Rectangle;
import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHighchartElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutImageElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableFormat;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;
import com.emistoolbox.lib.pdf.util.PDFLayoutLogVisitor;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPriorityListContent;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PDFAdvancedReportWriter;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContent;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileOutput;
import es.jbauer.lib.io.impl.IOInputStreamInput;
import es.jbauer.lib.io.impl.IOOutputStreamOutput;

public class PDFLayoutReportWriter extends PDFAdvancedReportWriter 
{
	@Override
	public void writeReport(PdfReport report, File out) 
		throws IOException, PdfReportWriterException 
	{
		if (!(report.getReportConfig() instanceof LayoutPdfReportConfig))
			throw new IllegalArgumentException("Can only write LayoutPdfReportConfig reports."); 

		LayoutPdfReportConfig config = (LayoutPdfReportConfig) report.getReportConfig(); 
		
		Point pageSize = PdfText.getPageSize(config.getPageSize(), config.getOrientation());
		Rectangle margins = PdfText.getMargins(config.getPageSize()); 

		List<PDFLayout> pages = new ArrayList<PDFLayout>(); 
		for (EmisPdfPage page : report.getPages())
		{
			if (page instanceof LayoutPage)
				pages.add(renderPage((LayoutPage) page, pageSize, margins)); 
		}

		if (isDebug())
		{
			PDFLayoutLogVisitor visitor = new PDFLayoutLogVisitor(); 
			for (PDFLayout page : pages)
				page.accept(visitor);

			visitor.close();
			
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
		result.setLeft(values.getLeft());
		result.setTop(values.getTop());
		result.setRight(values.getRight());
		result.setBottom(values.getBottom());
		
		return result; 
	}
	
	private PDFLayout renderPage(LayoutPage page, Point size, Rectangle margins)
		throws IOException
	{
		PDFLayout layout = new PDFLayout();  
		
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement(size.x - margins.getLeft() - margins.getRight(), size.y - margins.getTop() - margins.getBottom());
		outerFrame.setPadding(getSides(margins)); 

		// TODO - handle titles of page (maybe defined in frame already as part of the LayoutPdfReportCreator)
		for (LayoutFrame frame : page.getFrames())
			outerFrame.addElement(createFrame(frame)); 
		
		outerFrame.addElement(createAlignedText(page.getText(PdfText.TEXT_FOOTER), page.getFont(PdfText.TEXT_FOOTER), PDFLayoutHorizontalAlignment.CENTER, PDFLayoutVerticalAlignment.BOTTOM));
		
		layout.setOuterFrame(outerFrame); 
		return layout; 
	}

	private PDFLayoutElement createAlignedText(String title, ChartFont font, PDFLayoutHorizontalAlignment horizontal, PDFLayoutVerticalAlignment vertical) 
	{
		if (StringUtils.isEmpty(title))
			return null; 
		return new PDFLayoutTextElement(title, getFont(font)).align(horizontal, vertical); 
	}
		
	private PDFLayoutElement createFrame(LayoutFrame frame)
		throws IOException
	{
		PDFLayoutFrameElement result = new PDFLayoutFrameElement(); 
		LayoutFrameConfig config = frame.getFrameConfig(); 
		setFramePlacement(result, config); 
		setFrameBorder(result, config); 
		setFramePadding(result, config); 
		setFrameBackground(result, config);
		
		PDFLayoutElement item = null; 
		PdfContent content = frame.getContent();
		if (content instanceof PdfImageContent)
			item = new PDFLayoutImageElement(((PdfImageContent) content).getFile());  
		else if (content instanceof PdfChartContent)
		{
			PdfChartContent chartContent = (PdfChartContent) content; 
			updateFrameTitle(frame, chartContent.getTitle());
			item = renderChart((PdfChartContent) content, config.getPosition().getWidth(), config.getPosition().getHeight());   
			item.fit(PDFLayoutObjectFit.COVER); 
		}
		else if (content instanceof PdfTextContent)
		{
			PdfTextContent textContent = (PdfTextContent) content;
			updateFrameTitle(frame, textContent.getTitle()); 
			item = new PDFLayoutTextElement(textContent.getText(), getFont(textContent.getTextFont()));  
		}
		else if (content instanceof PdfVariableContent)
			item = new PDFLayoutTextElement("VARIABLES - to be implemented", null);  
		else if (content instanceof PdfPriorityListContent)
			item = new PDFLayoutTextElement("PRIORITY LIST - to be implemented", null);  
		else if (content instanceof PdfTableContent)
			item = renderTable((PdfTableContent) content); 

		if (item != null)
		{
			// Wrap content with title, subtitle and footer.  
			item.fit(PDFLayoutObjectFit.CONTAIN);
			addTitlesAndContent(result, item, frame);
			
			// style box
			addStyles(item, frame); 
		}
		
		return result; 
	}
	
	private void addStyles(PDFLayoutElement content, LayoutFrame frame)
	{
		LayoutFrameConfig config = frame.getFrameConfig(); 
		
		content.setBackgroundColor(getColor(config.getBackgroundColour())); 
		content.setBorderStyle(getBorder(config.getBorderRadius(), config.getBorders()));
		content.setPadding(getLayoutSides(config.getPadding(), null));
	}
		
	private void addTitlesAndContent(PDFLayoutFrameElement frame, PDFLayoutElement content, TextSet text)
	{
		PDFLayoutElement title = getTextElement(text, PdfText.TEXT_TITLE, PDFLayoutHorizontalAlignment.LEFT, PDFLayoutVerticalAlignment.BELOW); 
		if (title != null)
			frame.addElement(title.pad(0, 0, 0, 6)); 
		frame.addElement(getTextElement(text, PdfText.TEXT_SUBTITLE, PDFLayoutHorizontalAlignment.LEFT, PDFLayoutVerticalAlignment.BELOW)); 
		frame.addElement(content);
		content.align(PDFLayoutHorizontalAlignment.CENTER, PDFLayoutVerticalAlignment.BELOW);
		frame.addElement(getTextElement(text, PdfText.TEXT_FOOTER, PDFLayoutHorizontalAlignment.LEFT, PDFLayoutVerticalAlignment.BOTTOM));
	}

	private PDFLayoutElement getTextElement(TextSet text, String key, PDFLayoutHorizontalAlignment horz, PDFLayoutVerticalAlignment vert)
	{
		if (text.getText(key) == null)
			return null; 
		
		PDFLayoutTextElement result = new PDFLayoutTextElement(); 
		result.setText(text.getText(key));
		result.setFont(getFont(text.getFont(key)));
		result.align(horz, vert);
//		result.setDisplacement(null); // TODO 
		
		return result;
	}
	
	private PDFLayoutElement renderTable(PdfTableContent content)
	{
		EmisTableStyle style = content.getTableStyle(); 
		PDFLayoutTableElement table = new PDFLayoutTableElement();
		table.setDimensions(content.getRows(), content.getColumns());
		for (int col = 0; col < content.getColumns(); col++)
			for (int row = 0; row < content.getRows(); row++)
			{
				table.setText(row, col, content.getText(row, col));
				if (col == 0 && style.getTopHeaderFormat() != null)
				{
					table.setCellFormat(row, col, getTableFormat(style.getTopHeaderFormat()));
				}
				else if (row == 0 && style.getLeftHeaderFormat() != null)
				{
					table.setCellFormat(row, col, getTableFormat(style.getLeftHeaderFormat()));
				}
				else 
				{
					table.setCellFormat(row, col, getTableFormat(style.getDataCellFormat(col)));
				}
			}

		table.setTableBorderStyle(getLineStyle(style.getBorder(BorderType.TABLE_HORIZONTAL))); 

		return table; 
	}
	
	private PDFLayoutTableFormat getTableFormat(TableCellFormat format)
	{
		PDFLayoutTableFormat result = new PDFLayoutTableFormat();  
		result.setBackgroundColor(getColor(format.getBackgroundColor()));
		result.setFont(getFont(format.getFont()));
		result.setObjectFit(PDFLayoutObjectFit.CONTAIN);
		result.setPlacement(getAlignment(format.getHorizontalAlign(), format.getVerticalAlign()));
		result.setPadding(getLayoutSides(format.getPadding(), null)); 
		
		return result; 
	}

	private PDFLayoutPlacement getAlignment(HorizontalAlign hAlign, VerticalAlign vAlign)
	{ return new PDFLayoutAlignmentPlacement(getHAlign(hAlign), getVAlign(vAlign)); }
	
	private PDFLayoutVerticalAlignment getVAlign(VerticalAlign vAlign)
	{
		switch (vAlign)
		{
		case CENTER: 
			return PDFLayoutVerticalAlignment.CENTER;
		case BOTTOM:
			return PDFLayoutVerticalAlignment.BOTTOM;
		default: 
			return PDFLayoutVerticalAlignment.TOP;
		}
	}
	
	private PDFLayoutHorizontalAlignment getHAlign(HorizontalAlign hAlign)
	{
		switch (hAlign)
		{
		case CENTER: 
			return PDFLayoutHorizontalAlignment.CENTER;
		case RIGHT:
			return PDFLayoutHorizontalAlignment.RIGHT;
		default: 
			return PDFLayoutHorizontalAlignment.LEFT;
		}
	}
	
	private PDFLayoutElement renderChart(PdfChartContent content, double width, double height)
		throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

		IOOutput out = null; 
		if (getChartRenderer().canCreateContentType("application/pdf"))
			out = new IOOutputStreamOutput(buffer, "chart.pdf", "application/pdf", null); 
		else
			out = new IOOutputStreamOutput(buffer, "chart.png", "image/png", null); 

		ChartConfig chartConfig = content.getChartConfig();  
		chartConfig.setChartSize((int) Math.round(2 * width), (int) Math.round(2 * height));
		getChartRenderer().render(content.getType(), content.getResult(), chartConfig, out);
		
		IOInput in = new IOInputStreamInput(new ByteArrayInputStream(buffer.toByteArray()), out.getName(), out.getContentType(), null); 
		if (in.getContentType().equals("application/json"))
			return new PDFLayoutHighchartElement(in); 
		else if (in.getContentType().equals("image/png"))
			return new PDFLayoutImageElement(in);
		else if (in.getContentType().equals("application/pdf"))
			return new PDFLayoutPDFElement(in);
		
		throw new IllegalArgumentException("Unsupported chart output format"); 
	}
	
	private void setFramePlacement(PDFLayoutFrameElement el, LayoutFrameConfig config)
	{
		if (config.getPosition() == null)
			return; 
		
		el.position(config.getPosition().getLeft(), config.getPosition().getTop()); 
		el.setWidth(config.getPosition().getWidth() - config.getPadding().getLeft() - config.getPadding().getRight());
		el.setHeight(config.getPosition().getHeight() - config.getPadding().getTop() - config.getPadding().getBottom());
	}

	private void setFrameBorder(PDFLayoutElement el, LayoutFrameConfig config)
	{
		PDFLayoutBorderStyle  border = new PDFLayoutBorderStyle();
		border.setBorderRadius((double) config.getBorderRadius());
		PDFLayoutLineStyle[] lineStyles = new PDFLayoutLineStyle[4]; 
		int i = 0; 
		
		for (BorderStyle borderConfig : config.getBorders().getValues(new BorderStyle[4]))
		{
			lineStyles[i] = getLineStyle(borderConfig);  
			i++; 
		}

		border.setLineStyles(new PDFLayoutSides<PDFLayoutLineStyle>(lineStyles));
	}
	
	private PDFLayoutLineStyle getLineStyle(BorderStyle style)
	{
		if (style == null)
			return null; 
		 return new PDFLayoutLineStyle((double) style.getWidth(), getColor(style.getColour())); 
	}
	
	private void setFrameBackground(PDFLayoutElement el, LayoutFrameConfig config)
	{}
	
	private void setFrameObjectFit(PDFLayoutElement el, LayoutFrameConfig config)
	{}

	private void setFramePadding(PDFLayoutElement el, LayoutFrameConfig config)
	{ el.setPadding(getLayoutSides(config.getPadding(), null)); }
	
	private PDFLayoutBorderStyle getBorder(int radius, LayoutSides<BorderStyle> borders)
	{
		BorderStyle[] layoutLines = borders.getValues(new BorderStyle[4]); 
		PDFLayoutLineStyle[] lines = new PDFLayoutLineStyle[layoutLines.length]; 
		for (int i = 0; i < lines.length; i++)
		{
			if (layoutLines[i] == null)
				lines[i] = new PDFLayoutLineStyle(0.0, null);  
			else
				lines[i] = new PDFLayoutLineStyle(1.0 * layoutLines[i].getWidth(), getColor(layoutLines[i].getColour())); 
		}
		
		return new PDFLayoutBorderStyle(new PDFLayoutSides<PDFLayoutLineStyle>(lines), 1.0 * radius); 
	}
	
	private <T> PDFLayoutSides<T> getLayoutSides(LayoutSides<T> values, T[] defaultValues) 
	{
		if (values == null)
		{
			if (defaultValues != null)
				return new PDFLayoutSides<T>(defaultValues); 
			else
				return null; 
		}
		else
		{
			PDFLayoutSides<T> result = new PDFLayoutSides<T>();
			result.setLeft(values.getLeft());
			result.setBottom(values.getBottom());
			result.setTop(values.getTop());
			result.setRight(values.getRight());
			
			return result; 
		}
	}
	
	private Color getColor(ChartColor color)
	{
		if (color == null)
			return null; 
		
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()); 
	}
	
	@Override
	public void setDateInfo(ReportMetaResult metaInfo) 
	{}
	
	private PDFLayoutFont getFont(ChartFont font)
	{
		if (font == null)
			font = PdfText.DEFAULT_FONT_HACK;
			
		PDFLayoutFont result = new PDFLayoutFont();
		result.setFontName(font.getName());
		result.setFontSize(font.getSize());
		switch (font.getStyle())
		{
		case ChartFont.BOLD: 
			result.setFontStyle(PDFLayoutFontStyle.BOLD);
			break; 
			
		case ChartFont.ITALIC: 
			result.setFontStyle(PDFLayoutFontStyle.ITALIC);
			break; 
			
		case ChartFont.ITALIC + ChartFont.BOLD: 
			result.setFontStyle(PDFLayoutFontStyle.BOLD_ITALIC);
			break; 

		default: 
			result.setFontStyle(PDFLayoutFontStyle.PLAIN);
			break; 
		}
		
		return result; 
	}
}
