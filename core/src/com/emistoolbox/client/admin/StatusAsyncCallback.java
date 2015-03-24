package com.emistoolbox.client.admin;

import com.emistoolbox.client.Message;
import com.emistoolbox.client.MessageReport;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StatusAsyncCallback<T> implements AsyncCallback<T>
{
    private String message;
    private boolean alertOnSuccess = false;

    public StatusAsyncCallback(String message) {
        this.message = message;
    }

    public StatusAsyncCallback(String message, boolean alertOnSuccess) {
        this.message = message;
        this.alertOnSuccess = alertOnSuccess;
    }

    public void onFailure(Throwable caught)
    {
        Window.alert(Message.messageReport().onFailure(this.message, caught.toString(), caught.getMessage()));
    }

    public void onSuccess(T result)
    {
        if (this.alertOnSuccess)
            Window.alert(Message.messageReport().onSuccess(this.message));
    }
}

