package com.emistoolbox.client.ui;

import com.emistoolbox.client.Message;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class StatusPanel extends HorizontalPanel implements StatusFeedback
{
    private ScrollPanel uiPanel = new ScrollPanel();
    private Image uiStatusImage = new Image();

    public StatusPanel()
    {
        setSpacing(5); 
        setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        add(uiStatusImage); 
        uiStatusImage.setVisible(false); 
        add(uiPanel); 
    }

    public void startProgress()
    {
        uiStatusImage.setUrl("css/progress.gif");
        uiStatusImage.setVisible(true);
    }

    public void setMessage(String title, String message, boolean success)
    {
        this.uiStatusImage.setUrl(success ? "css/ok.png" : "css/error.png");
        this.uiStatusImage.setVisible(true);
        setMessage(title, message); 
    }
    
    public void setMessage(String title, String message)
    { setWidget(new HTML("<div class='subtitle'>" + title + "</div>" + message)); }
    
    public void setWidget(Widget w)
    { uiPanel.setWidget(w); }

    public Widget getWidget()
    { return uiPanel.getWidget(); } 

    public <T> AsyncCallback<T> getCallback(final String message, final AsyncCallback<T> callback)
    {
        return new AsyncCallback<T>() {
            public void onFailure(Throwable caught)
            {
                setMessage(Message.messageAdmin().apFailed(message), caught.toString() + " - " + caught.getMessage(), false);
                if (callback != null)
                    callback.onFailure(caught);
            }

            public void onSuccess(T result)
            {
                setMessage(Message.messageAdmin().apSuccess(), message, true);
                if (callback != null)
                    callback.onSuccess(result);
            }
        };
    }
}
