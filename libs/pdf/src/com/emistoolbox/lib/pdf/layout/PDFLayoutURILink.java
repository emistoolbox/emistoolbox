package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutURILink extends PDFLayoutLink {
	private String URI;

	public PDFLayoutURILink (String uRI) {
		URI = uRI;
	}

	public String getURI () {
		return URI;
	}

	public void setURI (String uRI) {
		URI = uRI;
	}
}
