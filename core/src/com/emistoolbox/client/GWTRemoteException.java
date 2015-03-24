package com.emistoolbox.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTRemoteException extends Exception implements IsSerializable
{
    private static final long serialVersionUID = 1L;
    String causeInfo;

    public GWTRemoteException() {
    }

    public GWTRemoteException(String causeInfo) {
        this.causeInfo = causeInfo;
    }

    public GWTRemoteException(String message, String causeInfo) {
        super(message);
        this.causeInfo = causeInfo;
    }

    public String getMessage()
    {
        return super.getMessage() + " " + this.causeInfo;
    }

    public void setCauseInfo(String causeInfo)
    {
        this.causeInfo = causeInfo;
    }

    public String getCauseInfo()
    {
        return this.causeInfo;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.GWTRemoteException JD-Core Version:
 * 0.6.0
 */