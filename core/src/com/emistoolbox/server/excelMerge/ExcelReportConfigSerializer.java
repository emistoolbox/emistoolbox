package com.emistoolbox.server.excelMerge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.emistoolbox.common.excelMerge.CellPosition;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.CellType;
import com.emistoolbox.common.excelMerge.ExcelMergeConfig.MergeDirection;
import com.emistoolbox.common.excelMerge.ExcelReportConfig;
import com.emistoolbox.common.excelMerge.impl.ExcelMergeConfigImpl;
import com.emistoolbox.common.excelMerge.impl.ExcelReportConfigImpl;
import com.emistoolbox.common.model.analysis.EmisAggregatorDef;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.util.NamedUtil;

/** Reads ExcelMergeConfig from an XML file.
 *
 * <excelMerge>
 *     <sheet name='sheetName' direction='rows|columns' topLeft='A1'>
 *         <global key='' value='' />
 *         <cell type='ORIGINAL|COPY|EMPTY|GLOBAL_VARIABLE|LOOP_VARIABLE|CONSTANT' value='' />
 *     </sheet>
 *
 *
 * </excelMerge>
 *
 */
public class ExcelReportConfigSerializer
{
    public  static final String TAG_EXCEL_REPORT = "excelReport";
    private static final String TAG_EXCEL_REPORTS = "excelReports";
    private static final String TAG_SHEET = "sheet";
    private static final String TAG_GLOBAL = "global";
    private static final String TAG_CELL = "cell";

    private static final String ATTRIBUTE_TEMPLATE = "template";
    private static final String ATTRIBUTE_DIRECTION = "direction";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_ENTITY = "entity";
    private static final String ATTRIBUTE_TOP_LEFT = "topLeft";

    public static Document getXml(List<ExcelReportConfig> configs)
    {
        Document doc = new DocumentImpl(); 
        
        Element rootTag = doc.createElement(TAG_EXCEL_REPORTS); 
        doc.appendChild(rootTag);
        
        for (ExcelReportConfig config : configs)
            addXml(rootTag, config); 
                
        return doc; 
    }
    
    public static void addXml(Element parentTag, ExcelReportConfig config)
    {
        Element reportTag = parentTag.getOwnerDocument().createElement(TAG_EXCEL_REPORT); 
        parentTag.appendChild(reportTag);
        
        if (config.getEntityType() != null)
            reportTag.setAttribute(ATTRIBUTE_ENTITY, config.getEntityType().getName()); 
        if (config.getName() != null)
            reportTag.setAttribute(ATTRIBUTE_NAME, config.getName()); 
        if (config.getTemplateFile() != null)
            reportTag.setAttribute(ATTRIBUTE_TEMPLATE, config.getTemplateFile()); 
        
        for (ExcelMergeConfig sheetConfig : config.getMergeConfigs())
            addXml(reportTag, sheetConfig); 
    }

    private static void addXml(Element parentTag, ExcelMergeConfig config)
    {
        Element tag = parentTag.getOwnerDocument().createElement(TAG_SHEET); 
        parentTag.appendChild(tag); 
        
        tag.setAttribute(ATTRIBUTE_NAME,  config.getSheetName()); 
        tag.setAttribute(ATTRIBUTE_DIRECTION, "" + config.getDirection());
        tag.setAttribute(ATTRIBUTE_TOP_LEFT,  "" + config.getTopLeft());

        Map<String, String> context = config.getContext(); 
        for (String key : context.keySet())
        {
            Element t = parentTag.getOwnerDocument().createElement(TAG_GLOBAL);
            tag.appendChild(t); 
            t.setAttribute(ATTRIBUTE_KEY, key); 
            t.setAttribute(ATTRIBUTE_VALUE, context.get(key)); 
        }
        
        for (int i = 0; i < config.getCellCount(); i++) 
        {
            Element t = parentTag.getOwnerDocument().createElement(TAG_CELL); 
            tag.appendChild(t); 
            t.setAttribute(ATTRIBUTE_TYPE, "" + config.getCellType(i)); 
            t.setAttribute(ATTRIBUTE_VALUE, config.getCellValue(i));
        }
    }

    public static List<ExcelReportConfig> getExcelReports(EmisMeta meta, Element rootTag, List<EmisIndicator> indicators)
    {
        List<ExcelReportConfig> result = new ArrayList<ExcelReportConfig>(); 
        
        NodeList nodes = rootTag.getElementsByTagName(TAG_EXCEL_REPORT); 
        for (int i = 0; i < nodes.getLength(); i++)
            result.add(getExcelReport(meta, (Element) nodes.item(i), indicators)); 
        
        return result; 
    }
    
    public static ExcelReportConfig getExcelReport(EmisMeta meta, Element reportTag, List<EmisIndicator> indicators)
    {
        if (!reportTag.getNodeName().equals(TAG_EXCEL_REPORT))
            throw new IllegalArgumentException("Unexpected root tag.");

        ExcelReportConfig result = new ExcelReportConfigImpl(); 
        
        result.setEntityType(NamedUtil.find(reportTag.getAttribute(ATTRIBUTE_ENTITY), meta.getEntities()));  
        result.setName(reportTag.getAttribute(ATTRIBUTE_NAME));  
        result.setTemplateFile(reportTag.getAttribute(ATTRIBUTE_TEMPLATE)); 
        
        NodeList sheets = reportTag.getElementsByTagName(TAG_SHEET);
        for (int i = 0; i < sheets.getLength(); i++)
            result.addMergeConfig(getExcelMergeConfig((Element) sheets.item(i), indicators));

        return result;
    }

    private static ExcelMergeConfig getExcelMergeConfig(Element sheetTag, List<EmisIndicator> indicators)
    {
        ExcelMergeConfig result = new ExcelMergeConfigImpl();

        result.setSheetName(sheetTag.getAttribute(ATTRIBUTE_NAME));
        result.setDirection(sheetTag.getAttribute(ATTRIBUTE_DIRECTION).equalsIgnoreCase(MergeDirection.COLUMNS.toString()) ? MergeDirection.COLUMNS : MergeDirection.ROWS);
        result.setTopLeft(new CellPosition(sheetTag.getAttribute(ATTRIBUTE_TOP_LEFT)));

        NodeList globals = sheetTag.getElementsByTagName(TAG_GLOBAL);
        for (int i = 0; i < globals.getLength(); i++)
        {
            Element globalTag = (Element) globals.item(i);
            result.putContext(globalTag.getAttribute(ATTRIBUTE_KEY), globalTag.getAttribute(ATTRIBUTE_VALUE));
        }

        NodeList cells = sheetTag.getElementsByTagName(TAG_CELL);
        int size = cells.getLength(); 
        result.setCells(new CellType[size], new String[size], new EmisIndicator[size], new EmisAggregatorDef[size]);
        for (int i = 0; i < size; i++)
        {
            Element cellTag = (Element) cells.item(i);
            CellType cellType = CellType.valueOf(CellType.class, cellTag.getAttribute(ATTRIBUTE_TYPE)); 
            result.setCellType(i, cellType);
            
            String value = cellTag.getAttribute(ATTRIBUTE_VALUE); 
            result.setCellValue(i, value);

            EmisIndicator indicator = ExcelReportConfigImpl.getIndicator(cellType, value, indicators); 
            result.setCellIndicator(i, indicator); 
            result.setCellAggregator(i, ExcelReportConfigImpl.getAggregator(indicator, value));
        }

        return result;
    }
}
