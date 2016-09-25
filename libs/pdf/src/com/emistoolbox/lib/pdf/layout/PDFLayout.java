package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

public class PDFLayout {
	private PDFLayoutFrame outerFrame;

	public PDFLayoutFrame getOuterFrame () {
		return outerFrame;
	}

	public void setOuterFrame (PDFLayoutFrame outerFrame) {
		this.outerFrame = outerFrame;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit(this);
	}
}
