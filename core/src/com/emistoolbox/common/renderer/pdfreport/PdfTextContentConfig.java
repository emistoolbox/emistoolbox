package com.emistoolbox.common.renderer.pdfreport;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface PdfTextContentConfig extends IsSerializable, PdfContentConfig
{
    public String getText(); 
    public void setText(String text); 
}
