package com.emistoolbox.client;

import com.emistoolbox.common.EmisAdminModuleData;
import com.emistoolbox.common.EmisReportModuleData;
import com.emistoolbox.common.fileDialog.FileDirectoryInfo;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.DbDataSourceConfig;
import com.emistoolbox.common.model.mapping.EmisEntityDbMap;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationErrorHierarchy;
import com.emistoolbox.common.model.validation.EmisValidationResult;
import com.emistoolbox.common.results.ExcelReportMetaResult;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.common.user.EmisUser;
import com.emistoolbox.common.user.EmisUser.AccessLevel;
import com.emistoolbox.common.util.ImportStatus;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract interface EmisToolboxServiceAsync
{
    public abstract void getDataSets(boolean forAdmin, AsyncCallback<String[]> callback); 

    public abstract void createDataSet(String dataset, AsyncCallback<EmisAdminModuleData> callback); 
    
    public abstract void changeDataSet(String dataset, AsyncCallback<Boolean> callback); 
    
    public abstract void loadAdminData(String dataset, AsyncCallback<EmisAdminModuleData> paramAsyncCallback);

    public abstract void saveAdminData(String dataset, EmisAdminModuleData paramEmisAdminModuleData, AsyncCallback<Void> paramAsyncCallback);

    public abstract void loadReportData(String dataset, AsyncCallback<EmisReportModuleData> paramAsyncCallback);

    public abstract void saveReportData(String dataset, EmisReportModuleData paramEmisReportModuleData, AsyncCallback<Void> paramAsyncCallback);

    public abstract void getImportStatus(int id, int fromMessage, AsyncCallback<ImportStatus> paramAsyncCallback);

    public abstract void importData(String dataset, EmisAdminModuleData paramEmisAdminModuleData, AsyncCallback<Integer> paramAsyncCallback);

    public abstract void testImportData(String dataset, EmisAdminModuleData paramEmisAdminModuleData, EmisEntityDbMap paramEmisEntityDbMap, boolean paramBoolean1, boolean paramBoolean2, AsyncCallback<Integer> paramAsyncCallback);

    public abstract void loadData(String dataset, AsyncCallback<EmisReportModuleData> paramAsyncCallback);

    public abstract void getDataSourceInfo(DbDataSourceConfig dataSourceConfig, String dataset, AsyncCallback<Map<String, List<String>>> paramAsyncCallback);

    public abstract void getValues(DbContext context, String dataset, String field, AsyncCallback<List<String>> paramAsyncCallback);

    public abstract void getHierarchyEntities(String datasetName, String paramString, int paramInt1, EmisMetaEntity paramEmisMetaEntity, int paramInt2, AsyncCallback<Map<Integer, String>> paramAsyncCallback);

    public abstract void findEntityParents(String dataset, String hierarchyName, int dateIndex, EmisEntity entities, AsyncCallback<List<EmisEntity>> callback); 
    
    public abstract void findEntityNames(String dataset, List<String> entityId, AsyncCallback<Map<String, String>> asyncCallback);  

    public abstract void getEntityNames(String dataset, String hierarchyName, List<EmisEntity> entities, AsyncCallback<List<EmisEntity>> asyncCallback); 

    public abstract void getResult(String datasetName, TableMetaResult paramTableMetaResult, AsyncCallback<Result[]> paramAsyncCallback);

    public abstract void getRenderedResult(String datasetName, TableMetaResult paramTableMetaResult, int paramInt, AsyncCallback<String[]> paramAsyncCallback);

    public abstract void getRenderedGisResult(String datasetName, GisMetaResult paramGisMetaResult, String format, AsyncCallback<String[]> paramAsyncCallback);

    public abstract void getRenderedReportResult(String datasetName, ReportMetaResult paramReportMetaResult, AsyncCallback<String> paramAsyncCallback);

    public abstract void getRenderedExcelReportResult(String datasetName, ExcelReportMetaResult paramReportMetaResult, List<EmisIndicator> indicators, AsyncCallback<String> paramAsyncCallback);

    public abstract void getExcelReportTemplates(String datasetName, AsyncCallback<String[]> asyncCallback) throws IOException; 

    public abstract void getExcelReportSheets(String datasetName, String excelFile, AsyncCallback<String[]> asyncCallback) throws IOException; 

    public abstract void getPriorityList(String datasetName, PriorityMetaResult paramPriorityMetaResult, AsyncCallback<List<PriorityListItem>> paramAsyncCallback);

    public abstract void saveMetaResult(MetaResult paramMetaResult, AsyncCallback<String> paramAsyncCallback);

    public abstract void loadMetaResult(String paramString, List<EmisIndicator> paramList, AsyncCallback<MetaResult> paramAsyncCallback);

    public abstract void savePriorityList(PriorityMetaResult paramPriorityMetaResult, List<PriorityListItem> paramList, String paramString, AsyncCallback<String> paramAsyncCallback);

    public void getUsers(AsyncCallback<List<EmisUser>> callback); 

    public void getAccessLevelFromToken(String token, String ip, AsyncCallback<AccessLevel> callback); 
    
    public void setUsers(List<EmisUser> users, AsyncCallback<Void> callback);  

    public void getPasswordHash(String password, AsyncCallback<String> callback); 

    public void getEntityData(String datasetName, EmisEntity entity, EmisEnumTupleValue date, AsyncCallback<String> callback); 
    
    public void getEntityChildren(String datasetName, EmisMetaHierarchy hierarchy, int dateIndex, EmisEntity parent, AsyncCallback<List<EmisEntity>> callback); 
    
	public void fileDialogListDir(String path, String protocol, String dataset, String filter, AsyncCallback<FileDirectoryInfo> callback); 
	
	public void loadValidations(String datasetName, AsyncCallback<List<EmisValidation>> callback);

	public void runValidation(String dataset, ValidationMetaResult metaResult, AsyncCallback<EmisValidationErrorHierarchy> callback); 

	public void runValidationList(String dataset, ValidationMetaResult metaResult, AsyncCallback<EmisValidationResult> callback);

	public void saveValidationResult(EmisValidationResult validationResult, String ext, AsyncCallback<String> callback);
}
