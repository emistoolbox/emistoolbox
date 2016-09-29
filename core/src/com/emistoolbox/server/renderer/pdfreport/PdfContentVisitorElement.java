package com.emistoolbox.server.renderer.pdfreport;

public interface PdfContentVisitorElement 
{
    public <T> T accept(PdfContentVisitor<T> visitor); 
}
