package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.charts.impl.BarChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.LineChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.PieChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.StackedBarChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.FontIdentifier;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.fonts.FontUtils;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfNullContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfResultTableContentImpl;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContent;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.ImgTemplate;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.FontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;

public class ItextPdfReportWriter implements PdfReportWriter
{
    private static final String TITLE_SEPARATOR = " - ";
    private static final float HEADERFOOTER_CELL_HEIGHT = 15.0F;
    private static final Font DEFAULT_TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 12.0F, 1);
    private static final Font DEFAULT_REPEAT_TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 8.0F, 1);
    private static final Font DEFAULT_SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 12.0F, 1);
    private static final Font DEFAULT_FOOTER_FONT = new Font(Font.FontFamily.HELVETICA, 10.0F);
    
    private String dateInfo; 
    
    @Override
	public void setDateInfo(ReportMetaResult metaInfo) 
    {
    	for (EmisEnumTupleValue item : metaInfo.getContext().getDates())
    		dateInfo = StringUtils.join(item.getValue(), ", ");
    }

	private int[] getCellHeights(PdfPage page, float totalHeight)
    {
        int[] result = new int[page.getRows()]; 

        for (int row = 0; row < page.getRows(); row++) 
        {
            result[row] = 1; 
            for (int col = 0; col < page.getColumns(); col++) 
            {
                PdfContent content = page.getContent(row, col); 
                if (content instanceof PdfChartContent || content instanceof PdfTableContent || content instanceof PdfImageContent)
                    result[row] = 2; 
                else if (content instanceof PdfVariableContent && ((PdfVariableContent) content).getSize() > 4)
                    result[row] = 2; 
                else if (content instanceof PdfTextContent && ((PdfTextContent) content).getText().length() > 150)
                	result[row] = 2; 
            }
        }
        
        int totalCount = 0; 
        for (int i = 0; i < result.length; i++) 
            totalCount += result[i]; 
        
        float unitHeight = totalHeight * getHeightFactor(totalCount) / totalCount; 
        for (int i = 0; i < result.length; i++) 
            result[i] = (int) (result[i] * unitHeight);  

        return result; 
    }
    
    /** We adjust the height by the number of rows - the more rows, the more likely some of them don't use the maximum 
     *  space. 
     */
    private float getHeightFactor(int rowCount)
    {
    	if (rowCount <= 4)
    		return 0.95f;
    	else if (rowCount <= 8)
    		return 1.0f; 
		else 
    		return 1.05f; 
    }
    
    public void writeReport(PdfReport report, File outputFile) throws IOException, PdfReportWriterException
    {
        if (report == null)
            throw new PdfReportWriterException("Report cannot be null");
        
        if (!(report.getReportConfig() instanceof PdfReportConfig))
        	throw new PdfReportWriterException("Can only write PdfReportConfig reports."); 

        Document document = new Document(getItextPageSize(report));
        try
        {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));

            document.open();
            document.setMargins(10.0F, 10.0F, 10.0F, 10.0F);

            String currentPageTitle = null;

            for (EmisPdfPage emisPage : report.getPages())
            {
            	if (!(emisPage instanceof PdfPage))
            		continue; 
            	
            	PdfPage page = (PdfPage) emisPage; 
                document.newPage();

                int maxCellWidth = (int) (document.getPageSize().getWidth() - 20.0F) / page.getColumns();
                int[] maxCellHeight = getCellHeights(page, document.getPageSize().getHeight() - 10.0F); 

                PdfPTable tablePageLayout = new PdfPTable(page.getColumns());
                tablePageLayout.setWidthPercentage(100.0F);
                tablePageLayout.setExtendLastRow(true);
                tablePageLayout.setKeepTogether(true);

                tablePageLayout.getDefaultCell().setBorderWidth(0.0F); 
                currentPageTitle = setTitleRow(tablePageLayout, page, report, currentPageTitle);

                for (int row = 0; row < page.getRows(); row++)
                {
                    for (int col = 0; col < page.getColumns(); col++)
                    {
                        PdfPCell cell = new PdfPCell(tablePageLayout.getDefaultCell());
                        cell.setMinimumHeight(0.8f * maxCellHeight[row]); 
                        
                        PdfContent content = page.getContent(row, col);

                        if (content != null)
                        {
                            cell.setColspan(content.getSpanCols());
                            cell.setRowspan(content.getSpanRows());

                            if ((content instanceof PdfNullContent))
                                continue;

                            PdfPTable contentTable = new PdfPTable(1);
                            configureContentHeader(maxCellWidth, content, contentTable);

                            if ((content instanceof PdfChartContent))
                            {
                                PdfChartContent chartContent = (PdfChartContent) content;
                                contentTable.addCell(getChart(chartContent, writer, new DefaultFontMapper(), maxCellWidth * content.getSpanCols(),
                                        (int) (maxCellHeight[row] - contentTable.getHeaderHeight() - tablePageLayout.getHeaderHeight() - 5.0F) * content.getSpanRows()));
                            }
                            else if ((content instanceof PdfResultTableContentImpl))
                                contentTable.addCell(getResultTable((PdfResultTableContentImpl) content));
                            else if ((content instanceof PdfTextContent))
                            {
                                Phrase p = new Phrase(((PdfTextContent) content).getText());
                                p.setFont(new Font(FontFamily.HELVETICA, 9)); 
                                contentTable.addCell(p);
                            }
                            else if ((content instanceof PdfVariableContent))
                                contentTable.addCell(((PdfVariableContent) content).getTable()); 
                            else if (content instanceof PdfImageContent)
                                contentTable.addCell(getSizedImage((PdfImageContent) content, maxCellWidth * content.getSpanCols(), (int) (maxCellHeight[row] - contentTable.getHeaderHeight() - tablePageLayout.getHeaderHeight())));

                            cell.addElement(contentTable);
                        }

                        tablePageLayout.addCell(cell);
                    }
                }
                setFooterRow(tablePageLayout, getCurrentFooter(page, report), page.getColumns());
                document.add(tablePageLayout);
            }
            document.close();
        }
        catch (DocumentException e)
        {
            throw new PdfReportWriterException(e.getMessage());
        }
    }
    
    public PdfPTable getResultTable(PdfResultTableContentImpl content)
    {
    	if (content.getResult().getDimensions() == 1)
    		return get1DTable(content, content.getResult());
    	if (content.getResult().getDimensions() == 2)
    		return get2DTable(content, content.getResult());

        throw new IndexOutOfBoundsException("Dimension index out of bounds");
    }
    
    private PdfPTable get2DTable(PdfTableContent content, Result result)
    {
        int columns = result.getDimensionSize(1) + 1;

        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100.0F);

        String[] dimCol = result.getHeadings(1);
        String[] dimRow = result.getHeadings(0);

        PdfPCell cell = new PdfPCell(table.getDefaultCell());
        table.addCell(cell);

        for (String heading : dimCol)
            table.addCell(getHeaderCell(new Phrase(heading, FontUtils.getItextFont(content.getFont(FontIdentifier.TABLE_HEADER)))));

        int[] indexes = new int[2];
        for (int i = 0; i < dimRow.length; i++)
        {
            table.addCell(getHeaderCell(new Phrase(dimRow[i], FontUtils.getItextFont(content.getFont(FontIdentifier.TABLE_HEADER)))));

            for (int j = 0; j < dimCol.length; j++)
            {
                cell = new PdfPCell(table.getDefaultCell());
                indexes[0] = i;
                indexes[1] = j;
                
                cell.setPhrase(new Phrase(ServerUtil.getFormattedValue(result, indexes)));
                table.addCell(cell);
            }
        }
        return table;
    }

    private PdfPTable get1DTable(PdfTableContent content, Result result)
    {
        int columns = result.getDimensionSize(0);

        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100.0F);

        for (String heading : result.getHeadings(0))
            table.addCell(getHeaderCell(new Phrase(heading, FontUtils.getItextFont(content.getFont(FontIdentifier.TABLE_HEADER)))));

        int[] indexes = new int[1];
        for (int i = 0; i < columns; i++)
        {
            PdfPCell cell = new PdfPCell(table.getDefaultCell());
            cell.setMinimumHeight(20);
            indexes[0] = i;
            cell.setPhrase(new Phrase(ServerUtil.getFormattedValue(result, indexes), FontUtils.getItextFont(content.getFont(FontIdentifier.DEFAULT))));
            table.addCell(cell);
        }
        return table;
    }

    private PdfPCell getHeaderCell(Phrase phrase)
    {
        PdfPCell cell = new PdfPCell();
        cell.setGrayFill(0.85F);
        cell.setNoWrap(false);
        cell.setMinimumHeight(20);
        cell.setPhrase(phrase);
        return cell;
    }

    public Image getChart(PdfChartContent chartContent, PdfWriter writer, FontMapper mapper, int width, int height) throws BadElementException
    {
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(width, height);
        Graphics2D g2 = tp.createGraphics(width, height, mapper);
        Rectangle2D r2D = new Rectangle2D.Double(0.0D, 0.0D, width, height);
        drawChart(chartContent, g2, r2D);
        g2.dispose();
        return new ImgTemplate(tp);
    }


    private void drawChart(PdfChartContent chartContent, Graphics2D g2, Rectangle2D r2d)
    {
    	ChartType type = chartContent.getType(); 
    	Result result = chartContent.getResult(); 
    	ChartConfig config = chartContent.getChartConfig(); 
    	
        JFreeChart chart = null;
        switch (chartContent.getType()) {
        case BAR:
            chart = new BarChartRenderer().render(config, result);
            break;
        case PIE:
            chart = new PieChartRenderer().render(config, result);
            break;
        case STACKED:
            chart = new StackedBarChartRenderer().render(config, result);
            break;
        case STACKED_SCALED: 
        {    
            if (result.getDimensions() == 1)
                chart = new BarChartRenderer().render(config, result);
            else
                chart = new StackedBarChartRenderer().render(config, result);
            break; 
        }
        case LINE: 
            chart = new LineChartRenderer().render(config, result); 
            break; 
        default:
            throw new UnsupportedOperationException("Type not supported:" + type);
        }
        chart.draw(g2, r2d);
    }


    private PdfPCell getSizedImage(PdfImageContent content, float width, float height) throws BadElementException, MalformedURLException, IOException
    {
        width -= Math.ceil(width * 0.02);
        height -= Math.ceil(height * 0.02);

//      Image image = Image.getInstance(ServerUtil.getFile("charts", ((PdfImageContentImpl) content).getImagePath(), true).getAbsolutePath());

        ByteArrayOutputStream os = new ByteArrayOutputStream(); 
        IOUtils.copy(content.getFile().getInputStream(), os); 
        Image image = Image.getInstance(os.toByteArray());
        image.scaleAbsolute(width, height);
        return new PdfPCell(image, false);
    }

    private void configureContentHeader(int maxCellWidth, PdfContent content, PdfPTable contentTable)
    {
        contentTable.setTotalWidth(maxCellWidth * content.getSpanCols());
        contentTable.setLockedWidth(true);

        contentTable.setHorizontalAlignment(0);

        PdfPCell headerCell = new PdfPCell();
        if (!StringUtils.isEmpty(content.getTitle()))
        {
            headerCell.setGrayFill(0.85F);
            headerCell.setPhrase(new Phrase(content.getTitle()));

            if ((content instanceof PdfTextContent))
            {
                contentTable.getDefaultCell().setBorder(0);
                headerCell.setBorder(0);
            }

            contentTable.addCell(headerCell);
            contentTable.setHeaderRows(1);
        }
        else
            contentTable.getDefaultCell().setBorder(0); 
    }

    private String getCurrentFooter(PdfPage page, PdfReport report)
    {
    	String pageFooter = page.getText(PdfText.TEXT_FOOTER); 
        return isNullOrEmpty(pageFooter) ? report.getReportConfig().getText(PdfText.TEXT_FOOTER) : pageFooter;
    }

    private static boolean isNullOrEmpty(String check)
    {
        return (check == null) || (check.equals(""));
    }

    private String setTitleRow(PdfPTable table, PdfPage page, PdfReport report, String previousPageTitle)
    {
    	TextSet txt = report.getReportConfig(); 
        String title = txt.getText(PdfText.TEXT_TITLE);
        String subtitle = txt.getText(PdfText.TEXT_SUBTITLE);

        if (isNullOrEmpty(page.getText(PdfText.TEXT_TITLE)))
        {
            if (isNullOrEmpty(txt.getText(PdfText.TEXT_TITLE)))
                return previousPageTitle;
        }
        else
        {
            title = replaceDate(page.getText(PdfText.TEXT_TITLE));
            subtitle = replaceDate(page.getText(PdfText.TEXT_SUBTITLE));
        }

        StringBuilder builder = new StringBuilder();
        builder.append(title);
        if (!isNullOrEmpty(subtitle))
            builder.append(" - ").append(subtitle);

        String currentPageTitle = builder.toString();

        Font titleFont = DEFAULT_TITLE_FONT;
        Font subtitleFont = DEFAULT_SUBTITLE_FONT;
        int titleAlignment = 0;

        if (currentPageTitle.equals(previousPageTitle))
        {
            titleFont = DEFAULT_REPEAT_TITLE_FONT;
            subtitleFont = DEFAULT_REPEAT_TITLE_FONT;
            titleAlignment = 2;
        }
        Phrase titlePhrase = new Phrase();
        titlePhrase.add(new Chunk(title, titleFont));
        if (!isNullOrEmpty(subtitle))
        {
            Chunk subtitlePhrase = new Chunk(subtitle, subtitleFont);
            titlePhrase.add(new Chunk(" - "));
            titlePhrase.add(subtitlePhrase);
        }

        PdfPCell cell = new PdfPCell(table.getDefaultCell());
        cell.setPhrase(titlePhrase);
        cell.setColspan(page.getColumns());
        cell.setHorizontalAlignment(titleAlignment);
        cell.setVerticalAlignment(4);
        cell.setFixedHeight(20.0F);
        table.addCell(cell);
        table.setHeaderRows(1);

        return currentPageTitle;
    }
    
    private String replaceDate(String title)
    {
    	if (dateInfo == null)
    		return title; 
    	
    	return title.replaceAll("\\{\\$date\\}", dateInfo); 
    }

    private static void setFooterRow(PdfPTable table, String footer, int colspan)
    {
        if ((footer == null) || (footer.equals("")))
            return;
        PdfPCell cell = new PdfPCell(table.getDefaultCell());
        cell.setPhrase(new Phrase(footer, DEFAULT_FOOTER_FONT));
        cell.setColspan(colspan);
        cell.setFixedHeight(20.0F);
        cell.setHorizontalAlignment(1);
        cell.setVerticalAlignment(6);
        table.addCell(cell);
    }

    private static Rectangle getItextPageSize(PdfReport report)
    {
        boolean rotate = report.getReportConfig().getOrientation() == PdfReportConfig.PageOrientation.LANDSCAPE;

        Rectangle size = null;

        switch (report.getReportConfig().getPageSize()) {
        case A4:
            size = PageSize.A4;
            break;
        case A5:
            size = PageSize.A5;
            break;
        case LETTER:
            size = PageSize.LETTER;
            break;
        default:
            throw new IllegalArgumentException("Unmapped Page Size");
        }
        return rotate ? size.rotate() : size;
    }

	@Override
	public String getExtension() 
	{ return ".pdf"; } 
}
