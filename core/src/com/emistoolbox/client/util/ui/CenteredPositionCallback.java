package com.emistoolbox.client.util.ui;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/** Callback for popups. Positions popup right in the center of the browsers client area. */  
public class CenteredPositionCallback implements PositionCallback
{
    private PopupPanel panel; 
    
    public CenteredPositionCallback(PopupPanel panel)
    { this.panel = panel; }
    
    public void setPosition(int width, int height)
    {
        int maxWidth = Window.getClientWidth() * 10 / 9; 
        if (width > maxWidth)
        {
            panel.setWidth(maxWidth + "px");
            width = maxWidth; 
        }
        
        int maxHeight = Window.getClientHeight() * 10 / 9; 
        if (height > maxHeight)
        {
            panel.setHeight(maxHeight + "px"); 
            height = maxHeight; 
        }
        
        int x = Math.max(0, (Window.getClientWidth() - width) / 2); 
        x += Window.getScrollLeft(); // offset scroll
        
        int y = Math.max(0, (Window.getClientHeight() - height) / 3);
        y += Window.getScrollTop(); // offset scroll
        
        panel.setPopupPosition(x, y);  
    }
}
