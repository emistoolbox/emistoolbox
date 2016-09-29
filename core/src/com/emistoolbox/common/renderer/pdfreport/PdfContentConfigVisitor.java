package com.emistoolbox.common.renderer.pdfreport;

public interface PdfContentConfigVisitor<T> 
{
	public T visit(PdfTextContentConfig config); 
	public T visit(PdfVariableContentConfig config); 
	public T visit(PdfChartContentConfig config); 
	public T visit(PdfGisContentConfig config); 
	public T visit(PdfPriorityListContentConfig config); 
	public T visit(PdfTableContentConfig config); 
}
