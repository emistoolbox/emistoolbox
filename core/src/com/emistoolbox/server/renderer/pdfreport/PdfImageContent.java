package com.emistoolbox.server.renderer.pdfreport;

import es.jbauer.lib.io.IOInput;

public abstract interface PdfImageContent extends PdfContent
{
    public abstract IOInput getFile();
}
