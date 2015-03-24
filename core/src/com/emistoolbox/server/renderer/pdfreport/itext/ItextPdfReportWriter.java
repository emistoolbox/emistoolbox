package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportWriterException;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfReportWriter;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfNullContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContent;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;

public class ItextPdfReportWriter implements PdfReportWriter
{
    private static final String TITLE_SEPARATOR = " - ";
    private static final float HEADERFOOTER_CELL_HEIGHT = 15.0F;
    private static final Font DEFAULT_TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 12.0F, 1);
    private static final Font DEFAULT_REPEAT_TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 8.0F, 1);
    private static final Font DEFAULT_SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 12.0F, 1);
    private static final Font DEFAULT_FOOTER_FONT = new Font(Font.FontFamily.HELVETICA, 10.0F);
    
    private int[] getCellHeights(PdfPage page, float totalHeight)
    {
        int[] result = new int[page.getRows()]; 

        for (int row = 0; row < page.getRows(); row++) 
        {
            result[row] = 1; 
            for (int col = 0; col < page.getColumns(); col++) 
            {
                PdfContent content = page.getContent(row, col); 
                if (content instanceof PdfChartContent || content instanceof PdfTableContent || content instanceof ItextPdfImageContent)
                    result[row] = 2; 
                else if (content instanceof PdfVariableContent && ((PdfVariableContent) content).getSize() > 4)
                    result[row] = 2; 
            }
        }
        
        int totalCount = 0; 
        for (int i = 0; i < result.length; i++) 
            totalCount += result[i]; 
        
        float unitHeight = totalHeight * 0.95f / totalCount; 
        for (int i = 0; i < result.length; i++) 
            result[i] = (int) (result[i] * unitHeight);  

        return result; 
    }
    
    public void writeReport(PdfReport report, File outputFile) throws IOException, PdfReportWriterException
    {
        if (report == null)
            throw new PdfReportWriterException("Report cannot be null");

        Document document = new Document(getItextPageSize(report));
        try
        {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));

            document.open();
            document.setMargins(10.0F, 10.0F, 10.0F, 10.0F);

            String currentPageTitle = null;

            for (PdfPage page : report.getPages())
            {
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
                                ItextPdfChartContent chartContent = (ItextPdfChartContent) content;
                                contentTable.addCell(chartContent.getChart(writer, new DefaultFontMapper(), maxCellWidth * content.getSpanCols(),
                                        (int) (maxCellHeight[row] - contentTable.getHeaderHeight() - tablePageLayout.getHeaderHeight() - 5.0F) * content.getSpanRows()));
                            }
                            else if ((content instanceof PdfTableContent))
                                contentTable.addCell(((ItextPdfTableContent) content).getTable());
                            else if ((content instanceof PdfTextContent))
                            {
                                Phrase p = new Phrase(((PdfTextContent) content).getText());
                                p.setFont(new Font(FontFamily.HELVETICA, 9)); 
                                contentTable.addCell(p);
                            }
                            else if ((content instanceof PdfVariableContent))
                                contentTable.addCell(((PdfVariableContent) content).getTable()); 
                            else if ((content instanceof ItextPdfImageContent))
                                contentTable.addCell(getSizedImage(content, maxCellWidth * content.getSpanCols(), (int) (maxCellHeight[row] - contentTable.getHeaderHeight() - tablePageLayout.getHeaderHeight())));

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

    private PdfPCell getSizedImage(PdfContent content, float width, float height) throws BadElementException, MalformedURLException, IOException
    {
        width -= Math.ceil(width * 0.1);
        height -= Math.ceil(height * 0.1);

        Image image = Image.getInstance(ServerUtil.getFile("charts", ((ItextPdfImageContent) content).getImagePath(), true).getAbsolutePath());
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
        return isNullOrEmpty(page.getFooter()) ? report.getReportConfig().getFooter() : page.getFooter();
    }

    private static boolean isNullOrEmpty(String check)
    {
        return (check == null) || (check.equals(""));
    }

    private static String setTitleRow(PdfPTable table, PdfPage page, PdfReport report, String previousPageTitle)
    {
        String title = report.getReportConfig().getTitle();
        String subtitle = report.getReportConfig().getSubtitle();

        if (isNullOrEmpty(page.getTitle()))
        {
            if (isNullOrEmpty(report.getReportConfig().getTitle()))
                return previousPageTitle;
        }
        else
        {
            title = page.getTitle();
            subtitle = page.getSubtitle();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(title);
        if (!isNullOrEmpty(subtitle))
        {
            builder.append(" - ").append(subtitle);
        }
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
}
