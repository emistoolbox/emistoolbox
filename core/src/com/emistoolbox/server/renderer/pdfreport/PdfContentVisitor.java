package com.emistoolbox.server.renderer.pdfreport;

import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContentImpl;

public interface PdfContentVisitor<T>
{
	public T visit(PdfChartContent content); 
	public T visit(PdfImageContent content); 
	public T visit(PdfPriorityListContent content); 
	public T visit(PdfTableContent content); 
	public T visit(PdfTextContent content); 
	public T visit(PdfGisContent content); 
}
