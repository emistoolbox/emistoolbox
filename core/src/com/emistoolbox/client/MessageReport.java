package com.emistoolbox.client;

import com.google.gwt.i18n.client.Messages;

public abstract interface MessageReport extends Messages
{
    public abstract String btnCharts();

    public abstract String btnIndicators();

    public abstract String btnLoad();

    public abstract String btnManageData();

    public abstract String btnReports();

    public abstract String btnExcelReports();

    public abstract String btnSave();

    public abstract String chartLoading();

    public abstract String collectingResult();

    public abstract String dataLoadedTitle();

    public abstract String dataLoadedMessage();

    public abstract String dataSavedMessage();

    public abstract String dataSavedTitle();

    public abstract String dataSavingAction();

    public abstract String dataSavingMessage();

    public abstract String dataSavingTitle();

    public abstract String errorFailedToRenderChart();

    public abstract String errorFailedToRenderTable();

    public abstract String errorNoIndicators();

    public abstract String loadingConfig();

    public abstract String loadingConfigMessage();

    public abstract String loadingConfigTitle();

    public abstract String loadingData();

    public abstract String loadingDataMessage();

    public abstract String loadingDataTitle();

    public abstract String onFailure(String paramString1, String paramString2, String paramString3);

    public abstract String onSuccess(String paramString);

    public abstract String notYetImplemented();

    public abstract String welcomeMessage();

    public abstract String bfeAll();

    public abstract String bfeWith();

    public abstract String bfeWithout();

    public abstract String errorFailedToRenderGis();

    public abstract String errorFailedToRenderReport();

    public abstract String errorFailedToRenderPrioList();

    public abstract String chartsTab();

    public abstract String mapsTab();

    public abstract String reportsTab();

    public abstract String prioTab();

    public abstract String reportLoading();

    public abstract String download();

    public abstract String reportPage();

    public abstract String excelReportsTab(); 
    public abstract String btnBrowseData();
    public abstract String btnValidateData(); 
    public abstract String dataNoData(); 
    public abstract String dataHierarchy();
    
    public abstract String dataChildren(); 
    public abstract String dataDataValues(); 
    public abstract String dataLoading(); 
    public abstract String dataLoadingFailed();
    public abstract String dataDate(); 
    
    public abstract String dataValue(); 
}
