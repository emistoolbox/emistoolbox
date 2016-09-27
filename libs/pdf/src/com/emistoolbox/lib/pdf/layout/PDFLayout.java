package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;
import java.io.Serializable;

public class PDFLayout implements Serializable {
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
