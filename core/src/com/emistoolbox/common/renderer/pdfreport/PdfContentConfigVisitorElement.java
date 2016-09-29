package com.emistoolbox.common.renderer.pdfreport;

public interface PdfContentConfigVisitorElement 
{
	public <T> T accept(PdfContentConfigVisitor<T> visitor); 
}
