package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.impl.AbstractPdfContent;

import es.jbauer.lib.io.IOInput;

public class PdfImageContentImpl extends AbstractPdfContent implements PdfImageContent
{
    private IOInput file;

    public PdfImageContentImpl(IOInput file)
    { this.file = file; } 

    public IOInput getFile()
    { return file; } 
}
