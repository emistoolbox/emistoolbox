package com.emistoolbox.lib.pdf;

import java.io.IOException;

import info.joriki.io.Util;
import info.joriki.pdf.PDFDictionary;

public class ResourceRenamer {
	int index;
	PDFDictionary allResources = new PDFDictionary ();
	public byte [] rename (PDFDictionary page) throws IOException {
		String content = new String (Util.toByteArray (page.getContentStream ()),"US-ASCII");
		PDFDictionary resources = (PDFDictionary) page.get ("Resources");
		for (String type : resources.keys ()) {
			PDFDictionary typeResources = (PDFDictionary) resources.get (type);
			PDFDictionary allTypeResources = allResources.getOrCreateDictionary (type);
			for (String key : typeResources.keys ()) {
				String newKey = "R" + ++index;
				allTypeResources.put (newKey,typeResources.getUnresolved (key));
				content = content.replaceAll ("/" + key + " ","/" + newKey + " ");
			}
		}
		return content.getBytes ("US-ASCII");
	}
	
	public PDFDictionary getResources () {
		return allResources;
	}
}
