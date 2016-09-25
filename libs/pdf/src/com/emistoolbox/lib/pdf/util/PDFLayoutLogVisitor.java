package com.emistoolbox.lib.pdf.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVisitor;

import es.jbauer.lib.io.IOOutput;

public class PDFLayoutLogVisitor implements PDFLayoutVisitor<Void> 
{
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(PDFLayoutComponent component) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(PDFLayoutFrame frame) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(PDFLayoutPDFContent pdfContent) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(PDFLayoutTextContent textContent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void close()
	{ 
		if (out != null)
			os.close(); 
	}
}
