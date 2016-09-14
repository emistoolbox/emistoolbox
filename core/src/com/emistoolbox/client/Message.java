package com.emistoolbox.client;

import com.google.gwt.core.client.GWT;

public class Message
{
    private static MessageAdmin messageAdmin = (MessageAdmin) GWT.create(MessageAdmin.class);
    private static MessageReport messageReport = (MessageReport) GWT.create(MessageReport.class);

    public static MessageAdmin messageAdmin()
    { return messageAdmin; }

    public static MessageReport messageReport()
    { return messageReport; }
}

