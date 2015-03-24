package com.emistoolbox.common.util;

import java.io.Serializable;
import java.util.List;

public class ImportStatus implements Serializable
{
    private static final long serialVersionUID = 1L;
    private boolean finished;
    private int taskCount = 0;
    private int doneTaskCount = 0;
    private int subTaskCount = 0;
    private int errorCount = 0;
    private List<String> messages;
    private Throwable exception;

    public ImportStatus() {
    }

    public ImportStatus(boolean finished, int doneTaskCount, int taskCount, int subTaskCount, int errorCount, List<String> messages, Throwable err) {
        this.finished = finished;
        this.taskCount = taskCount;
        this.doneTaskCount = doneTaskCount;
        this.errorCount = errorCount;
        this.messages = messages;
        this.subTaskCount = subTaskCount;
        this.exception = err;
    }

    public int getErrorCount()
    {
        return this.errorCount;
    }

    public int getTaskCount()
    {
        return this.taskCount;
    }

    public void setTaskCount(int taskCount)
    {
        this.taskCount = taskCount;
    }

    public int getDoneTaskCount()
    {
        return this.doneTaskCount;
    }

    public void setDoneTaskCount(int doneTaskCount)
    {
        this.doneTaskCount = doneTaskCount;
    }

    public List<String> getMessages()
    {
        return this.messages;
    }

    public void setMessages(List<String> messages)
    {
        this.messages = messages;
    }

    public int getSubTaskCount()
    {
        return this.subTaskCount;
    }

    public void setSubTaskCount(int subTaskCount)
    {
        this.subTaskCount = subTaskCount;
    }

    public boolean isFinished()
    {
        return this.finished;
    }

    public void setFinished(boolean finished)
    {
        this.finished = finished;
    }

    public Throwable getException()
    {
        return this.exception;
    }

    public void setException(Throwable exception)
    {
        this.exception = exception;
    }
}
