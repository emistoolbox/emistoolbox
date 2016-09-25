package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

public class PDFLayout {
	private PDFLayoutFrameElement outerFrame;

	public PDFLayoutFrameElement getOuterFrame () {
		return outerFrame;
	}

	public void setOuterFrame (PDFLayoutFrameElement outerFrame) {
		this.outerFrame = outerFrame;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit(this);
	}
}
