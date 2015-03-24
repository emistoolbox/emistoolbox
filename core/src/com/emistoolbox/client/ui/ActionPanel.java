package com.emistoolbox.client.ui;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ActionPanel extends VerticalPanel implements StatusFeedback
{
    HorizontalPanel uiButtons = new HorizontalPanel();
    StatusPanel uiStatus = new StatusPanel(); 

    public ActionPanel() {
        this.uiButtons.setSpacing(3);

        add(this.uiButtons);
        add(new HTML("<hr>"));
        add(this.uiStatus);
    }
    

    public Widget detachStatus()
    {
        remove(this.uiStatus);
        return this.uiStatus;
    }

    public void attachStatus()
    {
        add(this.uiStatus);
    }

    public void startProgress()
    { uiStatus.startProgress(); }

    public void add(PushButton button, int size)
    { this.uiButtons.add(EmisUtils.init(button, size)); }

    public void setMessage(String title, String message)
    { uiStatus.setMessage(title, message); }
    
    public void setMessage(String title, String message, boolean success)
    { uiStatus.setMessage(title,  message, success); }

    public void setWidget(Widget w)
    { uiStatus.setWidget(w); }
    
    public Widget getWidget()
    { return uiStatus.getWidget(); }

    public <T> AsyncCallback<T> getCallback(final String message, final AsyncCallback<T> callback)
    { return uiStatus.getCallback(message, callback); } 
}
