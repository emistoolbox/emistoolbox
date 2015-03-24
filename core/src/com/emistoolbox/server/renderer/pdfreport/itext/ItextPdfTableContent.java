package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.pdfreport.FontIdentifier;
import com.emistoolbox.server.renderer.pdfreport.PdfContentFontMap;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.fonts.FontUtils;
import com.emistoolbox.server.renderer.pdfreport.impl.AbstractPdfContent;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class ItextPdfTableContent extends AbstractPdfContent implements PdfTableContent
{
    private Result result;
    private PdfContentFontMap fontMap = new PdfContentFontMap();

    private float headerFixedHeight = 20.0F;

    public void setResult(Result result)
    { this.result = result; }

    public void setHeaderFixedHeight(float height)
    {
        this.headerFixedHeight = height;
    }

    public float getHeaderFixedHeight()
    {
        return this.headerFixedHeight;
    }

    public void setFont(FontIdentifier identifier, ChartFont font)
    {
        this.fontMap.setFont(identifier, font);
    }

    public PdfPTable getTable()
    {
        if (this.result.getDimensions() == 1)
            return get1DTable();
        if (this.result.getDimensions() == 2)
            return get2DTable();

        throw new IndexOutOfBoundsException("Dimension index out of bounds");
    }

    private PdfPTable get2DTable()
    {
        int columns = this.result.getDimensionSize(1) + 1;

        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100.0F);

        String[] dimCol = this.result.getHeadings(1);
        String[] dimRow = this.result.getHeadings(0);

        PdfPCell cell = new PdfPCell(table.getDefaultCell());
        table.addCell(cell);

        for (String heading : dimCol)
            table.addCell(getHeaderCell(new Phrase(heading, FontUtils.getItextFont(this.fontMap.getFont(FontIdentifier.TABLE_HEADER)))));

        int[] indexes = new int[2];
        for (int i = 0; i < dimRow.length; i++)
        {
            table.addCell(getHeaderCell(new Phrase(dimRow[i], FontUtils.getItextFont(this.fontMap.getFont(FontIdentifier.TABLE_HEADER)))));

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

    private PdfPTable get1DTable()
    {
        int columns = this.result.getDimensionSize(0);

        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100.0F);

        for (String heading : this.result.getHeadings(0))
        {
            table.addCell(getHeaderCell(new Phrase(heading, FontUtils.getItextFont(this.fontMap.getFont(FontIdentifier.TABLE_HEADER)))));
        }

        int[] indexes = new int[1];
        for (int i = 0; i < columns; i++)
        {
            PdfPCell cell = new PdfPCell(table.getDefaultCell());
            cell.setMinimumHeight(this.headerFixedHeight);
            indexes[0] = i;
            cell.setPhrase(new Phrase(ServerUtil.getFormattedValue(result, indexes), FontUtils.getItextFont(fontMap.getFont(FontIdentifier.DEFAULT))));
            table.addCell(cell);
        }
        return table;
    }

    private PdfPCell getHeaderCell(Phrase phrase)
    {
        PdfPCell cell = new PdfPCell();
        cell.setGrayFill(0.85F);
        cell.setNoWrap(false);
        cell.setMinimumHeight(this.headerFixedHeight);
//        cell.setFixedHeight(this.headerFixedHeight);
        cell.setPhrase(phrase);
        return cell;
    }
}
