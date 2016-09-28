package com.emistoolbox.lib.pdf.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.layout.PDFLayout;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileInput;
import es.jbauer.lib.io.impl.IOFileOutput;

/** Reads a List of PDFLayout objects from an ObjectStream. */ 
public class ObjectStreamTest 
{
	public static void main (String [] args) throws Exception 
	{
		System.out.println("Usage: ObjectStreamTest inputPath outputPath");
		if (args.length != 2)
			return; 
		
		IOInput in = new IOFileInput(new File(args[0])); 
		IOOutput out = new IOFileOutput(new File(args[1]), "application/pdf", null);

		new PDFLayoutRenderer().render(readLayout(in), out);
	}

	private static List<PDFLayout> readLayout(IOInput in)
		throws IOException, ClassNotFoundException
	{
		InputStream is = null; 
		try {
			is = in.getInputStream(); 
			ObjectInputStream ois = new ObjectInputStream(is); 
			return (List<PDFLayout>) ois.readObject(); 
		}
		finally 
		{ if (is != null) is.close(); }  
	}
}
