package com.emistoolbox.client.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BlockingScreen extends PopupPanel
{
    private int count = 0;
    private int success = 0;
    private int fail = 0;
    private boolean waitForFinish = false;
    private Widget messageWidget;
    private SimplePanel panel; 
    
    public BlockingScreen(Widget message, boolean wait) {
        this(message);
        this.waitForFinish = wait;
    }

    public BlockingScreen(Widget message) {
        this.messageWidget = message;

        setPixelSize(Window.getClientWidth(), Window.getClientHeight());
        setStylePrimaryName("grayPopup");
        setModal(true);
        setAutoHideEnabled(false);

        FlexTable table = new FlexTable();
        table.setCellSpacing(10);
        table.setWidth("100%");
        table.setHeight("100%");
        setWidget(table);

        table.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);

        panel = new SimplePanel();
        panel.setStyleName("whiteBox");
        panel.setWidth("400px");
        panel.setWidget(message);

        table.setWidget(0, 0, panel);

        setPopupPosition(0, 0);
        show();
    }

    public Widget getMessageWidget()
    {
        return this.messageWidget;
    }

    public <T> AsyncCallback<T> getCallback(final AsyncCallback<T> callback)
    {
        this.count += 1;

        return new AsyncCallback<T>() {
            public void onFailure(Throwable caught)
            {
                BlockingScreen.this.tryHide(false);
                callback.onFailure(caught);
            }

            public void onSuccess(T result)
            {
                BlockingScreen.this.tryHide(true);
                callback.onSuccess(result);
            }
        };
    }

    public synchronized void finished()
    {
        this.waitForFinish = false;
        if (this.count == this.success + this.fail)
        {
            hide();
            cleanup();
        }
    }

    public synchronized void tryHide(boolean withSuccess)
    {
        if (withSuccess)
            this.success += 1;
        else
            this.fail += 1;

        if (this.waitForFinish)
            return;

        if (this.count == this.success + this.fail)
        {
            hide();
            cleanup();
        }
    }

    public void cleanup()
    { messageWidget.removeFromParent(); }
}
