package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;

public class PdfHtmlContent extends PdfContentBase<PdfContentConfig>
{
    public String html;
    
    public String getHtml()
    { return html; }


    public void setHtml(String html)
    { this.html = html; }

	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return null; }

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