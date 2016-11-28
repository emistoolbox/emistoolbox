package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.util.Point;
import com.emistoolbox.common.util.Rectangle;

public class PdfText 
{
	public static final String TEXT_TITLE = "title";
	public static final String TEXT_SUBTITLE = "subtitle";
	public static final String TEXT_BODY = "body";
	public static final String TEXT_FOOTER = "footer";
	public static final String TEXT_GROUP = "group";
	
	public static final String ALIGN_LEFT = "left"; 
	public static final String ALIGN_CENTER = "center"; 
	public static final String ALIGN_RIGHT = "right"; 
	

	public final static ChartFont DEFAULT_FONT_HACK = new ChartFont(ChartFont.FONT_HELVETICA, ChartFont.PLAIN, 12);  
	public final static ChartFont DEFAULT_TITLE_FONT_HACK = new ChartFont(ChartFont.FONT_HELVETICA, ChartFont.BOLD, 14);

	public static Point getPageSize(PageSize size, PageOrientation orientation)
	{
		Point result = null; 
		switch (size)
		{
		case A4: 
			result = new Point(595, 842);
			break; 
			
		case A5: 
			result = new Point(420, 595);
			break; 
	
		case LETTER: 
			result = new Point(612, 792);
			break; 
			
		}
	
		if (result != null && orientation == PageOrientation.LANDSCAPE)
			result = new Point(result.y, result.x); 
		
		return result; 
	}
	public static Rectangle getMargins(PageSize pageSize)
	{
		int margin = pageSize == PageSize.A5 ? 24 : 36; 
		return new Rectangle(margin, margin, margin, margin); 
	}  
}
