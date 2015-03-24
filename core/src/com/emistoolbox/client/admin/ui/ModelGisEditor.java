package com.emistoolbox.client.admin.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.common.model.impl.GisContextImpl;
import com.emistoolbox.common.model.meta.GisContext;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ModelGisEditor extends FlexTable
{
    private TextArea uiProjection = new TextArea();

    private TextBox uiImage = new TextBox(); 
    private Grid uiBoundary = new Grid(4, 2); 
    private Grid uiSize = new Grid(2, 2); 
    
    public ModelGisEditor() 
    {
        setHTML(0, 0, Message.messageAdmin().mgeProjectionTitle());
        EmisToolbox.css(this, 0, 0, "sectionBlue"); 
        
        setText(1, 0, Message.messageAdmin().mgeProjection());
        setWidget(2, 0, uiProjection);
        this.uiProjection.setPixelSize(300, 100);

        setHTML(3, 0, Message.messageAdmin().mgeBaseImageTitle()); 
        EmisToolbox.css(this, 3, 0, "sectionBlue"); 
        setText(4, 0, Message.messageAdmin().mgeBaseImage()); 
        setWidget(5, 0, uiImage);
        
        String[] labels = new String[] { "west:", "south:", "east:", "north:"}; 
        setText(6, 0, Message.messageAdmin().mgeBaseImageBoundary()); 
        for (int i = 0; i < 4; i++) 
        {
            uiBoundary.setText(i, 0, labels[i]); 
            EmisToolbox.css(uiBoundary, i, 0, "section"); 
            uiBoundary.setWidget(i, 1, new TextBox()); 
        }
        setWidget(7, 0, uiBoundary);

        setText(8, 0, Message.messageAdmin().mgeBaseImageSize()); 
        labels = new String[] {" x:", " y:" };
        for (int i = 0; i < 2; i++)
        {
            uiSize.setText(i, 0, labels[i]); 
            EmisToolbox.css(uiSize, i, 0, "section"); 
            uiSize.setWidget(i, 1, new TextBox()); 
        }
        
        setWidget(9, 0, uiSize);
    }

    public void set(GisContext gis)
    {
        if (gis == null)
        {
            uiProjection.setText("");
            for (int i = 0; i < 4; i++) 
                set(uiBoundary, 1 + i * 2, "");
            
            for (int i = 0; i < 2; i++) 
                set(uiSize, 1 + i * 2, ""); 
        }
        else
        {
            uiProjection.setText(gis.getProjection());
            double[] boundary = gis.getBaseLayerBoundary(); 
            for (int i = 0; i < 4; i++) 
                set(uiBoundary, i, boundary != null && i < boundary.length ? "" + boundary[i] : null); 
            
            int[] size = gis.getBaseLayerImageSize(); 
            for (int i = 0; i < 2; i++) 
                set(uiSize, i, size != null && i < size.length ? "" + size[i] : null); 
        }
    }

    public GisContext get()
    {
        GisContext result = new GisContextImpl(); 

        result.setProjection(uiProjection.getText());
        result.setBaseLayerImage(uiImage.getText()); 
        result.setBaseLayerBoundary(getDoubles(uiBoundary)); 
        result.setBaseLayerImageSize(getInts(uiSize)); 
        
        return result; 
    }

    private void set(Grid g, int index, String value)
    {
        if (index >= g.getRowCount())
            return; 
            
        Widget w = g.getWidget(index, 1);
        if (!(w instanceof TextBox))
            return; 
        
        TextBox tb = (TextBox) w; 
        tb.setText(value); 
    }

    private double[] getDoubles(Grid g)
    {
        double[] result = new double[g.getRowCount()]; 
        for (int i = 0; i < result.length; i++) 
        {
            result[i] = getDouble(g, i); 
            if (Double.isNaN(result[i]))
                return null; 
        }
        
        return result; 
    }
    
    private double getDouble(Grid g, int index)
    {
        TextBox tb = getTextBox(g, index); 
        if (tb == null)
            return Double.NaN; 
        
        try { return Double.parseDouble(tb.getText()); } 
        catch (Throwable err) { return Double.NaN; } 
    }
    
    private int[] getInts(Grid g)
    {
        int[] result = new int[g.getRowCount()]; 
        for (int i = 0; i < result.length; i++) 
        {
            result[i] = getInt(g, i); 
            if (-1 == result[i])
                return null; 
        }
        
        return result; 
    }
    
    private int getInt(Grid g, int index)
    {
        TextBox tb = getTextBox(g, index); 
        if (tb == null)
            return -1; 
        
        try { return Integer.parseInt(tb.getText()); }  
        catch (Throwable err) { return -1; } 
    }
    
    private TextBox getTextBox(Grid g, int index)
    {
        if (index >= g.getRowCount())
            return null; 
        
        Widget w = g.getWidget(index, 1); 
        if (w instanceof TextBox)
            return (TextBox) w; 

        return null; 
    }
}
