package com.emistoolbox.lib.pdf.util;

import java.io.IOException;
import java.io.PrintStream;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
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
		output(frame);
		for (PDFLayoutElement e : frame.getElements())
			e.accept(this); 
		
		return null;
	}
	
	@Override
	public Void visit(PDFLayoutPDFElement pdfContent) throws IOException {
		os.println(indent + "Element - PDF"); 
		output(pdfContent); 
		return null; 
	}

	@Override
	public Void visit(PDFLayoutTextElement textContent) {
		os.println(indent + "Element - Text: " + textContent.getText());
		output(textContent); 
		return null; 
	}
	
	private void output(PDFLayoutElement item)
	{
		PDFLayoutBorderStyle border = item.getBorderStyle(); 
		os.println(indent + "Border Style"); 

		indent(); 
		try { 
			os.println(indent + "radius=" + border.getBorderRadius()); 
			for (PDFLayoutLineStyle line : border.getLineStyles().getValues(new PDFLayoutLineStyle[0])) 
				os.println(indent + "Line: color=" + line.getColor() + ", width=" + line.getWidth()); 
		}
		finally { unindent(); }
		
		os.println(indent + "fit = " + item.getObjectFit());
		os.println(indent + "padding = " + item.getPadding().getValues(new Double[0]));  
		os.println(indent + "placement = " + item.getPlacement()); 
	}
	

	public void close()
	{ 
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
