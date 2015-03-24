package com.emistoolbox.client.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public interface StatusFeedback 
{
    public void startProgress();
    public void setMessage(String title, String message, boolean success);
    public void setMessage(String title, String message);
    
    public <T> AsyncCallback<T> getCallback(final String message, final AsyncCallback<T> callback); 

    public void setWidget(Widget w); 
}
