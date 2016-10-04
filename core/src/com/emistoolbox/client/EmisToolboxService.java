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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RemoteServiceRelativePath("service")
public abstract interface EmisToolboxService extends RemoteService
{
    public static final int RESULT_CHART = 0;
    public static final int RESULT_TABLE = 1;
    public static final int CHART_BAR = 0;
    public static final int CHART_STACKED_BAR = 1;
    public static final int CHART_PIE = 2;
    public static final int CHART_STACKED_BAR_NORMALIZED = 3;
    public static final int CHART_LINES = 4; 

    public abstract String[] getDataSets(final boolean forAdmin) throws IOException; 

    public abstract EmisAdminModuleData createDataSet(String dataset) throws IOException; 
    
    public abstract boolean changeDataSet(String dataset) throws IOException; 
    
    public abstract EmisAdminModuleData loadAdminData(String dataset) throws IOException;

    public abstract void saveAdminData(String dataset, EmisAdminModuleData paramEmisAdminModuleData) throws IOException;

    public abstract EmisReportModuleData loadReportData(String dataset) throws IOException;

    public abstract void saveReportData(String dataset, EmisReportModuleData paramEmisReportModuleData) throws IOException;

    public abstract ImportStatus getImportStatus(int paramInt1, int paramInt2) throws IOException;

    public abstract int importData(String dataset, EmisAdminModuleData paramEmisAdminModuleData) throws IOException;

    public abstract int testImportData(String dataset, EmisAdminModuleData paramEmisAdminModuleData, EmisEntityDbMap paramEmisEntityDbMap, boolean paramBoolean1, boolean paramBoolean2) throws IOException;

    public abstract EmisReportModuleData loadData(String dataset) throws IOException;

    public abstract Map<String, List<String>> getDataSourceInfo(DbDataSourceConfig dataSourceConfig, String dataset) throws IOException;

    public abstract Set<String> getValues(DbContext context, String dataset, String field) throws IOException;

    public abstract Map<Integer, String> getHierarchyEntities(String dataset, String hierarchyName, int dateIndex, EmisMetaEntity paramEmisMetaEntity, int paramInt2) throws IOException;

    public abstract Map<String, String> findEntityNames(String dataset, List<String> entityId) 
    	throws IOException;

    public abstract List<EmisEntity> findEntityParents(String dataset, String hierarchyName, int dateIndex, EmisEntity entities) 
    	throws IOException;

    public abstract List<EmisEntity> getEntityNames(String dataset, String hierarchyName, List<EmisEntity> entities) throws IOException; 

    public abstract Result[] getResult(String dataset, TableMetaResult paramTableMetaResult) throws IOException;

    public abstract String[] getRenderedResult(String dataset, TableMetaResult paramTableMetaResult, int paramInt) throws IOException;

    public abstract String[] getRenderedGisResult(String dataset, GisMetaResult paramGisMetaResult, String format) throws IOException;

    public abstract String getRenderedReportResult(String dataset, ReportMetaResult paramReportMetaResult) throws IOException;

    public abstract String getRenderedReportAsHtmlResult(String dataset, ReportMetaResult metaResult) throws IOException;

    public abstract String getRenderedExcelReportResult(String dataset, ExcelReportMetaResult paramReportMetaResult, List<EmisIndicator> indicators) throws IOException;

    public abstract String[] getExcelReportTemplates(String dataset) throws IOException; 

    public abstract String[] getExcelReportSheets(String dataset, String excelFile) throws IOException;
    
    public abstract List<PriorityListItem> getPriorityList(String dataset, PriorityMetaResult paramPriorityMetaResult) throws IOException;

    public abstract String saveMetaResult(MetaResult paramMetaResult) throws IOException;

    public abstract MetaResult loadMetaResult(String paramString, List<EmisIndicator> paramList) throws IOException;

    public abstract String savePriorityList(PriorityMetaResult paramPriorityMetaResult, List<PriorityListItem> paramList, String paramString) throws IOException;
    
    public List<EmisUser> getUsers() throws IOException;
    
    public AccessLevel getAccessLevelFromToken(String token, String ip) throws IOException;
    
    public void setUsers(List<EmisUser> users) throws IOException;

    public String getPasswordHash(String password);
    
    public String getEntityData(String dataset, EmisEntity entity, EmisEnumTupleValue date) throws IOException; 
    public List<EmisEntity> getEntityChildren(String dataset, EmisMetaHierarchy hierarchy, int dateIndex, EmisEntity parent) throws IOException; 

	public FileDirectoryInfo fileDialogListDir(String path, String protocol, String dataset, String filter) 
		throws IllegalArgumentException;
	
	public List<EmisValidation> loadValidations(String datasetName)
		throws IOException; 
	
	public EmisValidationErrorHierarchy runValidation(String dataset, ValidationMetaResult metaResult)
		throws IOException;

	public EmisValidationResult runValidationList(String dataset, ValidationMetaResult metaResult)
		throws IOException;
	
	public String saveValidationResult(EmisValidationResult validationResult, String ext)
		throws IOException; 
}
