package com.emistoolbox.lib.pdf;

import java.io.IOException;

import info.joriki.io.Util;
import info.joriki.pdf.PDFArray;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFObject;

public class ResourceRenamer {
	String prefix;
	int index;
	PDFDictionary allResources = new PDFDictionary ();

	public ResourceRenamer () {
		this ("R");
	}

	public ResourceRenamer (String prefix) {
		this.prefix = prefix;
	}

	public byte [] rename (PDFDictionary page) throws IOException {
		String content = new String (Util.toByteArray (page.getContentStream ()),"US-ASCII");
		PDFDictionary resources = (PDFDictionary) page.get ("Resources");
		for (String type : resources.keys ()) {
			PDFObject resourceObject = resources.get (type);
			if (resourceObject instanceof PDFDictionary) {
				PDFDictionary typeResources = (PDFDictionary) resourceObject;
				PDFDictionary allTypeResources = allResources.getOrCreateDictionary (type);
				for (String key : typeResources.keys ()) {
					String newKey = prefix + ++index;
					allTypeResources.put (newKey,typeResources.getUnresolved (key));
					content = content.replaceAll ("/" + key + " ","/" + newKey + " ");
				}
			}
			else if (resourceObject instanceof PDFArray) {
				PDFArray typeResources = (PDFArray) resourceObject;
				PDFArray allTypeResources = (PDFArray) allResources.get (type);
				if (allTypeResources == null) {
					allTypeResources = new PDFArray ();
					allResources.put (type,allTypeResources);
				}
				for (PDFObject resource : typeResources)
					if (!allTypeResources.contains (resource))
						allTypeResources.add (resource);
			}
			else throw new Error ();
		}
		return content.getBytes ("US-ASCII");
	}
	
	public PDFDictionary getResources () {
		return allResources;
	}
}
