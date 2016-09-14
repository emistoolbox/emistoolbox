package com.emistoolbox.client;

import com.google.gwt.i18n.client.Messages;

public abstract interface MessageAdmin extends Messages
{
    public abstract String apFailed(String paramString);

    public abstract String apSuccess();

    public abstract String aeHtmlAddFilter();

    public abstract String aeHtmlDataField();

    public abstract String aeHtmlName();

    public abstract String aeHtmlOnlyToDisplayInTable();

    public abstract String aeTextSelectDataField();

    public abstract String aeTextEntityNameCount(String paramString);

    public abstract String btnClearAll();

    public abstract String btnDataMapping();

    public abstract String btnDataModel();

    public abstract String btnImport();

    public abstract String btnLoad();

    public abstract String btnManageData();

    public abstract String btnOk();

    public abstract String btnSave();

    public abstract String clearModelMessage();

    public abstract String clearModelTitle();

    public abstract String confirmDelete();

    public abstract String dataMappingConfig();

    public abstract String dbmapAlertFailToConnect(String paramString);

    public abstract String dbmapBtnAddDataSource();

    public abstract String dbmapBtnAddMapping(String paramString);

    public abstract String dbmapBtnRefresh();

    public abstract String dbmapDataSourceConnectingMessage();

    public abstract String dbmapDataSourceConnectingTitle();

    public abstract String dbmapDataSources();

    public abstract String dbmapErrorMissingConfigurations();

    public abstract String dbmapErrorNotParentOfEmisHierarchyDbMap();

    public abstract String dbmapHeirarchies();

    public abstract String dbmapLabelConfigureChildElements();

    public abstract String dbmapLocations();

    public abstract String dbmapNewDataSource();

    public abstract String editorModel();

    public abstract String edpLabelEntityCount(String entityName, String dateName);

    public abstract String emdeLabelAsArray();

    public abstract String emdeLabelClassification();

    public abstract String emdeLabelDataType();

    public abstract String emdeLabelDateType();

    public abstract String emdeLabelName();

    public abstract String emdePlanningResource();

    public abstract String emeInfoUseCommaForMultipleValues();

    public abstract String enumEditBtnAdd();

    public abstract String enumEditBtnDel();

    public abstract String enumEditBtnDown();

    public abstract String enumEditBtnUp();

    public abstract String enumEditPromptEnterClassificationValue();

    public abstract String errorFailureToRetrieveImportStatusMessage();

    public abstract String errorFailureToRetrieveImportStatusTitle();

    public abstract String errorFailureToRetrieveStatusMessage();

    public abstract String errorFailureToRetrieveStatusTitle();

    public abstract String eseHtmlClearAll();

    public abstract String eseHtmlSelectAll();

    public abstract String etAlertEmisOrAdminNotFound();

    public abstract String etErrorIdEmisMenuNotFound();

    public abstract String hbDate();

    public abstract String hbLabelHierarchy();

    public abstract String hbLabelNoEntitiesFound();

    public abstract String hbMessageLoading();

    public abstract String hbNoHeirarchiesDefined();

    public abstract String hbSelectorSelect();

    public abstract String hbSelectorSelectAll();

    public abstract String iceBtnAddCount();

    public abstract String iceBtnAddRatio();

    public abstract String iceBtnDelete();

    public abstract String iceIndicatorNewCount();

    public abstract String iceIndicatorNewRatio();

    public abstract String ieHtmlFactor();

    public abstract String ieHtmlMaxValue();

    public abstract String ieHtmlName();

    public abstract String ieHtmlThresholds();

    public abstract String ieLabel();

    public abstract String ieLabelBad();

    public abstract String ieLabelGood();

    public abstract String ieTextBadHigherThan();

    public abstract String ieTextBadLowerThan();

    public abstract String ieTextBadLowerThanGood();

    public abstract String ieTextGoodLowerThanBad();

    public abstract String ieTextGoodHigherThan();

    public abstract String ieTextGoodLowerThan();

    public abstract String importingDataMessage();

    public abstract String importingDataTitle();

    public abstract String importStatusFailure();

    public abstract String importStatusInProgress();

    public abstract String importStatusSuccess();

    public abstract String loadingDataCallback();

    public abstract String loadingDataMessage();

    public abstract String loadingDataTitle();

    public abstract String mapuiaErrorArrayNeedsToSpecifyEmisMetaDataType();

    public abstract String mapuiaInfoNoEachRowHasOnlyOneValue();

    public abstract String mapuiaInfoYesFor(String paramString);

    public abstract String mapuiaPromptMoreThanOneValueInRow();

    public abstract String mapuiaRowEnumAccess();

    public abstract String mapuiaRowValueAccess();

    public abstract String mapuidsContextValue();

    public abstract String mapuidsContextVariable();

    public abstract String mapuidsDataSourceType();

    public abstract String mapuidsDbName();

    public abstract String mapuidsDbPassword();

    public abstract String mapuidsDbUsername();

    public abstract String mapuidsFile();

    public abstract String mapuidsFileCount(int paramInt);

    public abstract String mapuidsHost();

    public abstract String mapuidsHtmlAdd();

    public abstract String mapuifInfoValueInField();

    public abstract String mapuifInfoValueInVariable();

    public abstract String mapuifInfoValueIsConstant();

    public abstract String mapuifInfoWaitingForFieldNames();

    public abstract String mapuifHtmlSourceType();

    public abstract String mapuifLabelConstantValue();

    public abstract String mapuifLabelContextVariable();

    public abstract String mapuifLabelFieldName();

    public abstract String mapuifLabelNone();

    public abstract String mapuiuErrorNeedToCreateArray();

    public abstract String mapuiuErrorNeedToPassDateEnum();

    public abstract String mapuiuLabelNoDataSourceDefined();

    public abstract String mdeeBtnAddDate();

    public abstract String mdeeBtnDelDate();

    public abstract String mdeeInfoDateAnalysis();

    public abstract String mdeePromptEnterDateEnumName();

    public abstract String mEnumeBtnDeleteClassification();

    public abstract String mEnumeBtnNewClassification();

    public abstract String mEnumePromptInputClassificationName();

    public abstract String mEnteBtnAddLocation();

    public abstract String mEnteBtnAddVariable();

    public abstract String mEnteBtnDelete();

    public abstract String mEnteHtmlEmisMetaData(String paramString);

    public abstract String mEnteInfoLocations();

    public abstract String mEnteInfoNameOfLocation();

    public abstract String mEnteInfoVariableName();

    public abstract String meTabClassifications();

    public abstract String meTabDates();

    public abstract String meTabLocations();

    public abstract String meTabHierarchies();

    public abstract String metadeInfoAllChildrenForParent(String paramString1, String paramString2);

    public abstract String metadeInfoAllEnums(String paramString);

    public abstract String metadeInfoAllEnumsFor(String paramString1, String paramString2);

    public abstract String metadeInfoByEntityVariables();

    public abstract String metadeInfoByEnumName(String paramString);

    public abstract String metadeInfoDateAnalysis();

    public abstract String metadeInfoEntityByName(String paramString1, String paramString2);

    public abstract String metadeInfoEnumAnalysis();

    public abstract String metadeInfoHorizontalAnalysis();

    public abstract String metadeInfoOneChildAndItsParents(String paramString1, String paramString2);

    public abstract String metadeInfoNoSplitDimension();

    public abstract String metadeInfoVerticalAnalysis();

    public abstract String metuDimensional(int paramInt);

    public abstract String metuNoArray();

    public abstract String mheBtnAddHierarchy();

    public abstract String mheBtnAddLocation();

    public abstract String mheBtnDelete();

    public abstract String mheBtnDown();

    public abstract String mheBtnUp();

    public abstract String mheInfoHierarchiesRelationships();

    public abstract String mhePromptInputHierarchyName();

    public abstract String mhePromptChooseTopEntityFirst();

    public abstract String mhePromptSelectEntity();

    public abstract String mreHtmlBarGraphs();

    public abstract String mreHtmlChartStyle();

    public abstract String mreHtmlPieChart();

    public abstract String mreHtmlLineChart();

    public abstract String mreHtmlStackedBarChsrt();

    public abstract String mreHtmlDate();

    public abstract String mreHtmlEntity();

    public abstract String mreHtmlHierarchy();

    public abstract String mreHtmlIndicator();

    public abstract String mreHtmlVariables();

    public abstract String mreHtmlListEntity();

    public abstract String mreHtmlNone();

    public abstract String mreHtmlSplitBy();

    public abstract String mreHtmlSwitch();

    public abstract String mreHtmlXAxis();

    public abstract String mueeBtnAdd();

    public abstract String mueeBtnDeleteMapping();

    public abstract String mueeErrorNoDataTypeSet();

    public abstract String mueeHtmlDataSource();

    public abstract String mueeHtmlDateAccess();

    public abstract String mueeHtmlDelete();

    public abstract String mueeIdName(String paramString);

    public abstract String mueeLabelAddNewFieldMapping();

    public abstract String muefItemEnumIndexes();

    public abstract String muefItemEnumValues();

    public abstract String muefItemMappedEnumValues();

    public abstract String muheeHtmlDataAccess();

    public abstract String muheeHtmlDateAccess();

    public abstract String muheeHtmlIdFor(String paramString);

    public abstract String mvContextDbConfig(String paramString);

    public abstract String mvContextMetaEntity(String paramString);

    public abstract String mvContextNone();

    public abstract String mvContextUnknown();

    public abstract String mvEnumAccess(String paramString);

    public abstract String mvErrorNoContextVariable(String paramString);

    public abstract String mvErrorNoConstValue(String paramString);

    public abstract String mvErrorNoDatabaseName();

    public abstract String mvErrorNoDatasource();

    public abstract String mvErrorNoFieldMap(String paramString);

    public abstract String mvErrorNoFieldName(String paramString);

    public abstract String mvErrorNoFiles();

    public abstract String mvErrorNoHost();

    public abstract String mvErrorNoPathForEntry(int index);
    public abstract String mvErrorNoCachePath(int index); 

    public abstract String mvErrorNoQuery();

    public abstract String mvErrorNotDefined(String paramString);

    public abstract String mvErrorUnsupportedConfigType();

    public abstract String mvIndex(String paramString);

    public abstract String mvMultiAccess(String paramString);

    public abstract String mvMultiIndex(String paramString);

    public abstract String mvWarnUnknownAccess(String paramString);

    public abstract String preHtmlForEntity();

    public abstract String preHtmlPageSetup();

    public abstract String preHtmlContents();

    public abstract String preHtmlSubtitle();

    public abstract String preHtmlTitle();

    public abstract String preItemNewPage();

    public abstract String savingDataCallback();

    public abstract String savingDataMessage();

    public abstract String savingDataTitle();

    public abstract String statusMessageErrorCount(int paramInt);

    public abstract String statusMessageFinished();

    public abstract String statusMessageProcessed(int paramInt1, int paramInt2);

    public abstract String statusMessageProcessing(int paramInt);

    public abstract String welcomeMessage();

    public abstract String ieCurrentNext(String paramString);

    public abstract String ieCurrentPrev(String paramString);

    public abstract String ieTimeOffset();

    public abstract String iceBtnAddTimeRatio();

    public abstract String iceIndicatorNewTimeRatio();

    public abstract String meTabGis();

    public abstract String mgeProjection();
    public abstract String mgeProjectionTitle();
    public abstract String mgeBaseImage();
    public abstract String mgeBaseImageTitle();
    public abstract String mgeBaseImageBoundary();
    public abstract String mgeBaseImageSize();

    public abstract String promptGisType();

    public abstract String gisMapSeperateLongLatFields();

    public abstract String gisMapSingleLongLatField();

    public abstract String gisLongLat();

    public abstract String gisLongitude();

    public abstract String gisLatitude();

    public abstract String gisLongLatList();

    public abstract String gisTypeNone();

    public abstract String gisTypeCoordinate();

    public abstract String gisTypePolygon();

    public abstract String mreHtmlPlotEntity();

    public abstract String mapuidsQueries();

    public abstract String promptNewQueryId();

    public abstract String preHtmlFooter();

    public abstract String preBtnDelContent();

    public abstract String preBtnMoveContentUp();

    public abstract String preBtnMoveContentDown();

    public abstract String preBtnViewContent();

    public abstract String preLayoutFullPage();

    public abstract String preLayoutTwoColumns();

    public abstract String preLayoutTwoRows();

    public abstract String preLayoutTwoByTwo();

    public abstract String preLayoutThreeByTwo();

    public abstract String preLayoutTwoByThree();

    public abstract String preLayoutFourByThree();

    public abstract String preLayoutThreeByFour();

    public abstract String preHtmlLayout();

    public abstract String prcleAdd();

    public abstract String prcleDel();

    public abstract String prcleView();

    public abstract String prcleReports();

    public abstract String prcleNewReportId();

    public abstract String mreHtmlReport();

    public abstract String mreHtmlCreateNewReport();

    public abstract String mreHtmlAddToReport();

    public abstract String preBtnContentToChart();

    public abstract String preBtnContentToTable();

    public abstract String preHtmlMap();

    public abstract String preHtmlTable();

    public abstract String preHtmlAddContentMessage();

    public abstract String mreBtnAddChart();

    public abstract String mreBtnAddTable();

    public abstract String mreBtnAddChartTable();

    public abstract String orientationLandscape();

    public abstract String orientationPortrait();

    public abstract String muefWithOffset();

    public abstract String mreSortNone();

    public abstract String mreSortAscending();

    public abstract String mreSortDescending();

    public abstract String mreSortName();
    
    public abstract String xlsAddField(); 
    
    public abstract String xlsTemplateFile(); 
    
    public abstract String xlsDataPosition(); 
    
    public abstract String xlsDirection(); 
    
    public abstract String xlsRows(); 

    public abstract String xlsColumns(); 

    public abstract String xlsFields(); 

    public abstract String xlsSelectEntity();
    
    public abstract String xlsCellTypeSelect();
    public abstract String xlsCellTypeLoopVariable();
    public abstract String xlsCellTypeConstant();
    public abstract String xlsCellTypeOriginal();
    public abstract String xlsCellTypeEmpty();
    public abstract String xlsCellTypeCopyFormula();
    public abstract String xlsName();
    public abstract String xlsLoading();
    public abstract String btnUsers();
    public abstract String usrViewer();
    public abstract String usrReportAdmin();
    public abstract String usrSystemAdmin();
    public abstract String usrUsername();
    public abstract String usrPassword();
    public abstract String usrAccess();
    public abstract String usrLogin();
    public abstract String usrLoginFailed();
    public abstract String usrFailedToLoad();
    public abstract String usrAccessDenied();
    public abstract String usrAskLogout(); 
    public abstract String usrSaveUsersSuccess(); 
    public abstract String usrSaveUsersFailed(); 
    public abstract String usrSaveUsersTitle(); 
    public abstract String infoUser(); 
    public abstract String infoDataSet();
    public abstract String none(); 

    public String currentDirectoryTxt();
    public String btnCancel();
    public String btnCreateDir();
    public String btnOpen();
    // public String btnSave();
	
    public String openFile();
    public String saveFileAs();
	
    public String errorServerError();
    public String ieHtmlYAxis(); 
   
    public String enumAllowDynamicInit(); 
    public String dbmapDateInit(); 
}
