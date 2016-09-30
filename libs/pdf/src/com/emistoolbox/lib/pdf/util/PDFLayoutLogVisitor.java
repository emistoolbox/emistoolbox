package com.emistoolbox.lib.pdf.util;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHighchartElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutImageElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVisitor;

import es.jbauer.lib.io.IOOutput;

public class PDFLayoutLogVisitor implements PDFLayoutVisitor<Void> 
{
	private static final String INDENT = "  "; 
	
	private int indentCount = 0;
	private String indent = ""; 
	
	private IOOutput out; 
	private PrintStream os; 
	
	public PDFLayoutLogVisitor()
	{ os = System.out; }
	
	public PDFLayoutLogVisitor(IOOutput out)
		throws IOException
	{
		this.out = out; 
		if (out.getEncoding() == null)
			this.os = new PrintStream(out.getOutputStream(), true);
		else
			this.os = new PrintStream(out.getOutputStream(), true, out.getEncoding());
	}

	@Override
	public Void visit(PDFLayout page) 
		throws IOException 
	{
		if (page == null)
			return null; 
		
		os.println("Page:");
		indent(); 
		try { page.getOuterFrame().accept(this); }
		finally { unindent(); } 
		
		return null; 
	}

	@Override
	public Void visit(PDFLayoutFrameElement frame) throws IOException 
	{
		os.println(indent + "Element - Frame: " + frame.getWidth() + " x " + frame.getHeight()); 
		indent(); 
		try { 
			output(frame);
			for (PDFLayoutElement e : frame.getElements())
				e.accept(this); 
		}
		finally { unindent(); } 
		
		return null;
	}
	
	@Override
	public Void visit(PDFLayoutHighchartElement highchartContent) throws IOException {
		os.println(indent + "Element - Highchart: " + highchartContent.getInput().getName()); 
		output(highchartContent); 
		return null; 
	}

	@Override
	public Void visit(PDFLayoutImageElement imageContent) throws IOException {
		os.println(indent + "Element - Image: " + imageContent.getInput().getName()); 
		output(imageContent); 
		return null; 
	}

	@Override
	public Void visit(PDFLayoutPDFElement pdfContent) throws IOException {
		os.println(indent + "Element - PDF: " + pdfContent.getInput().getName()); 
		output(pdfContent); 
		return null; 
	}

	public Void visit(PDFLayoutTableElement tableContent) throws IOException {
		os.println(indent + "Element - Table: " + tableContent.getRowCount() + " x " + tableContent.getColCount());
		try {
			indent();
			output (tableContent);
			for (int row = 0;row < tableContent.getRowCount ();row++)
				for (int col = 0;col < tableContent.getColCount ();col++) {
					os.println (indent + "Table element in row " + row + ", column " + col + ":");
					PDFLayoutElement element = tableContent.getElement (row,col);
					if (element == null)
						os.println (indent + "  null");
					else
						element.accept (this);
				}
		}
		finally { unindent(); }
		return null;
	}


	public Void visit(PDFLayoutFont font)
	{
		os.println(indent + "Font: " + font.getFontName() + ", size: " + font.getFontSize() + ", style: " + font.getFontStyle() + ", col: " + getColorName(font.getColor())); 
		return null; 
	}
	
	private String getColorName(Color color)
	{
		if (color == null)
			return null; 
		
		String result = color.toString(); 
		int start = result.indexOf("["); 
		int end = result.indexOf("]"); 
		if (start == -1 || end == -1)
			return result; 
		
		return result.substring(start + 1, end); 
	}

	@Override
	public Void visit(PDFLayoutTextElement textContent) {
		os.println(indent + "Element - Text: " + textContent.getText());
		try { 
			indent(); 
			visit(textContent.getFont());  
			output(textContent); 
		}
		finally { unindent(); }
		
		return null; 
	}
	
	private void output(PDFLayoutElement item)
	{
		PDFLayoutBorderStyle border = item.getBorderStyle(); 
		if (border != null)
		{
			os.println(indent + "Border Style"); 
	
			indent(); 
			try { 
				os.println(indent + "radius=" + border.getBorderRadius()); 
				os.println(indent + "borders: " + border.getLineStyles()); 
			}
			finally { unindent(); }
		}
		
		os.println(indent + "fit = " + item.getObjectFit());
		
		if (item.getPadding() != null)
			os.println(indent + "padding = " + item.getPadding());  
		
		if (item.getPlacement() != null)
			os.println(indent + "placement = " + item.getPlacement());

		if (item.getBackgroundColor() != null)
			os.println(indent + "background = " + item.getBackgroundColor ());
	}
	

	public void close()
	{ 
		os.flush(); 
		if (out != null)
			os.close(); 
	}
	
	private void indent()
	{ 
		indentCount++; 
		updateIndent(); 
	} 
	
	private void unindent()
	{ 
		if (indentCount > 0)
			indentCount--; 
		updateIndent(); 
	}
	
	private void updateIndent()
	{ indent = new String(new char[indentCount]).replace("\0", INDENT); }
}
