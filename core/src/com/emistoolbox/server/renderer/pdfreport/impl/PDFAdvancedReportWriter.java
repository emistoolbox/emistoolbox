package com.emistoolbox.server.renderer.pdfreport.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutFrame;

public abstract class PDFAdvancedReportWriter extends PdfBaseReportWriter {
	protected static void updateFrameTitle (LayoutFrame frame,String title) {
		if (!StringUtils.isEmpty(title) && StringUtils.isEmpty(frame.getText(PdfText.TEXT_TITLE)))
			frame.putText(PdfText.TEXT_TITLE, title); 
	}
	
	protected static String newPrefix (String oldPrefix,String newSegment) {
		return oldPrefix == null ? newSegment : oldPrefix + " / " + newSegment;
	}

	protected static <T> int getListSize (List<T> list) {
		return list == null ? 0 : list.size ();
	}
}
