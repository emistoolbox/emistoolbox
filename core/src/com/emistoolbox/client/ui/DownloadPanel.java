package com.emistoolbox.client.ui;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.util.ui.CenteredPositionCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DownloadPanel
{
    private String html;
    private PushButton btnClose = new PushButton("Close");
    private SimplePanel simplePanel = new SimplePanel();
    private PopupPanel popup = null;

    private DownloadPanel(String html) {
        this(html, null);
    }

    private DownloadPanel(String html, Widget anchor) {
        if (anchor == null)
        {
            this.popup = new BlockingScreen(this.simplePanel);
        }
        else
        {
            this.popup = new PopupPanel();
            this.popup.setWidget(this.simplePanel);
            this.popup.setModal(true);
            this.popup.setAutoHideEnabled(true);
            this.popup.setPopupPositionAndShow(new CenteredPositionCallback(popup)); 
//            this.popup.showRelativeTo(anchor);
        }

        EmisUtils.init(this.btnClose, 60);
        this.html = html;

        this.btnClose.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                DownloadPanel.this.popup.hide();
            }
        });
    }

    public DownloadPanel(Widget prepareMessage, String html) {
        this(prepareMessage, html, null);
    }

    public DownloadPanel(Widget prepareMessage, String html, Widget anchor) {
        this(html, anchor);
        this.simplePanel.setWidget(prepareMessage);
    }

    public AsyncCallback<String> getDownloadCallback(final AsyncCallback<String> callback)
    {
        return new AsyncCallback<String>() {
            public void onFailure(Throwable caught)
            {
                if (callback != null)
                    callback.onFailure(caught);
                DownloadPanel.this.showError(caught.toString());
            }

            public void onSuccess(String result)
            {
                if (callback != null)
                    callback.onSuccess(result);
                DownloadPanel.this.showDownload(result);
            }
        };
    }

    public void showError(String message)
    {
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(2);
        vp.add(new Label(message));
        vp.add(this.btnClose);
        this.simplePanel.setWidget(vp);
    }

    public void showDownload(String url)
    {
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(5);

        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        vp.add(new Label("Click on the link to download the document:"));
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vp.add(new HTML("<a href='" + url + "' target='_blank'>" + this.html + "</a>"));

        vp.add(this.btnClose);
        this.simplePanel.setWidget(vp);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.ui.DownloadPanel JD-Core Version:
 * 0.6.0
 */