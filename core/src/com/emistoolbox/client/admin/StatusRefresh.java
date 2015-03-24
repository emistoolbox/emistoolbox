package com.emistoolbox.client.admin;

import com.emistoolbox.client.EmisToolboxServiceAsync;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.ui.StatusFeedback;
import com.emistoolbox.common.util.ImportStatus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StatusRefresh
{
    private int resultId;
    private StatusFeedback status;
    private EmisToolboxServiceAsync service;
    private int errors = 0;
    private String dataSet; 

    public StatusRefresh(int resultId, String dataSet, StatusFeedback status, EmisToolboxServiceAsync service) {
        this.resultId = resultId;
        this.status = status;
        this.service = service;
        this.dataSet = dataSet; 
    }

    public void request()
    {
        this.service.getImportStatus(this.resultId, 0, new AsyncCallback<ImportStatus>() {
            public void onFailure(Throwable caught)
            {
                errors++;
                status.setMessage(Message.messageAdmin().errorFailureToRetrieveImportStatusTitle(), Message.messageAdmin().errorFailureToRetrieveImportStatusMessage() + "<p>" + errors);
                request();
            }

            public void onSuccess(ImportStatus result)
            {
                String message = StatusRefresh.this.getStatusMessage(result);
                if (result.getException() != null)
                    StatusRefresh.this.status.setMessage(Message.messageAdmin().importStatusFailure(), result.getException().toString() + "<hr>" + message, false);
                else
                {
                    if (result.isFinished())
                        StatusRefresh.this.status.setMessage(Message.messageAdmin().importStatusSuccess(), message, true);
                    else
                    {
                        StatusRefresh.this.status.setMessage(Message.messageAdmin().importStatusInProgress(), message);
                        StatusRefresh.this.request();
                    }
                }
            }
        });
    }

    private String getStatusMessage(ImportStatus status)
    {
        StringBuffer result = new StringBuffer();
        result.append(Message.messageAdmin().statusMessageProcessed(status.getDoneTaskCount(), status.getTaskCount())).append("<br>");
        result.append(Message.messageAdmin().statusMessageProcessing(status.getSubTaskCount())).append("<br>");
        result.append(Message.messageAdmin().statusMessageErrorCount(status.getErrorCount())).append("<br>");

        if (status.isFinished())
        {
            result.append("Log files: <a target='_blank' href='/emistoolbox/content?log=errors.log&dataSet=" + dataSet + "'>ErrorLog</a>"); 
            result.append(", <a target='_blank' href='/emistoolbox/content?log=data.log&dataSet=" + dataSet + "'>Import Data Summary</a>"); 
        }
        result.append("<hr>");

        for (String message : status.getMessages())
        {
            result.append(message + "<br>");
        }
        return result.toString();
    }
}

