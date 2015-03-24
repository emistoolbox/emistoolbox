package com.emistoolbox.server.renderer.pdfreport.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class PdfHtmlContent extends AbstractPdfContent
{
    public String html;
    
    public String getHtml()
    { return html; }


    public void setHtml(String html)
    { this.html = html; }

/*
    public void htmlWorker() throws DocumentException, IOException
    {
        Document document = new Document(PageSize.A4);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File("htmltest.pdf")));
        document.open();

        HTMLWorker worker = new HTMLWorker(document);
        StyleSheet style = new StyleSheet();

        worker.setStyleSheet(style);
        worker.parse(new FileReader("SomeHtml.htm"));

        worker.close();
        document.close();
    }
    */ 
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.pdfreport.impl.PdfHtmlContent
 * JD-Core Version: 0.6.0
 */