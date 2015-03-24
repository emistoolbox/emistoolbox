package com.emistoolbox.server.renderer.pdfreport.impl;

public class PdfTextContent extends AbstractPdfContent
{
    private String text;

    public PdfTextContent(String title, String text) {
        setTitle(title);
        this.text = text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return this.text;
    }
}
