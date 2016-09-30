package com.emistoolbox.server.renderer.pdfreport.html;

import info.joriki.io.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.results.ReportMetaResult;
import com.emistoolbox.common.util.Rectangle;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPriorityListContent;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PDFAdvancedReportWriter;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContent;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutFrame;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPage;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOInputStreamInput;
import es.jbauer.lib.io.impl.IOOutputStreamOutput;

public class HTMLReportWriter extends PDFAdvancedReportWriter {
	static interface HTMLNode {
		void print (PrintStream ps);
	}
	
	static class TextNode implements HTMLNode {
		String text;

		public TextNode (String text) {
			this.text = text;
		}
		
		public void print (PrintStream ps) {
			ps.print (text);
		}
	}
	
	static class HTMLTag implements HTMLNode {
		String name;
		Map<String,String> attributes = new HashMap<String,String> ();
		List<HTMLNode> children = new ArrayList<HTMLNode> ();
		
		public HTMLTag (String name) {
			this.name = name;
		}
		
		public HTMLTag (String name,String content) {
			this (name);
			add (new TextNode (content));
		}

		public void print (PrintStream ps) {
			ps.print ('<' + name);
			for (Entry<String,String> entry : attributes.entrySet ())
				ps.print (' ' + entry.getKey () + "='" + entry.getValue () + "'");
			ps.print ('>');
			for (HTMLNode child : children)
				child.print (ps);
			if (!children.isEmpty ())
				ps.print ("</" + name + '>');
		}
		
		public void add (HTMLNode child) {
			children.add (child);
		}
	}
	
	static class HTMLDocument {
		HTMLTag html = new HTMLTag ("html");
		HTMLTag head = new HTMLTag ("head");
		HTMLTag body = new HTMLTag ("body");

		public HTMLDocument () {
			html.add (head);
			html.add (body);
		}

		public void print (File file) throws FileNotFoundException {
			PrintStream ps = new PrintStream (file);
			ps.println ("<!DOCTYPE HTML>");
			html.print (ps);
			ps.close ();
		}
		
		public void add (HTMLNode node) {
			body.add (node);
		}
	}
	
	HTMLDocument groupDocument;
	File groupDirectory;
	int groupCount;
	int imageCount;
	int labelCount;
	
	List<HTMLTag> groupContents; 

	void printGroup () throws FileNotFoundException {
		if (groupDocument != null) {
			for (HTMLTag tag : groupContents)
				groupDocument.add (tag);
			groupDocument.print (new File (groupDirectory,"index.html"));
		}
	}
	
	public void writeReport (PdfReport report,File out) throws IOException {
		out.delete ();
		out.mkdir ();
		HTMLDocument indexDocument = new HTMLDocument ();
		groupDirectory = null;
		groupDocument = null;
		for (EmisPdfPage page : report.getPages ()) {
			if (!(page instanceof LayoutPage))
				throw new Error ("can only handle layout pages");
			String groupText = page.getText (PdfText.TEXT_GROUP);
			if (groupText != null) {
				printGroup ();
				String groupDirectoryName = ++groupCount + "-" + groupText;
				HTMLTag a = new HTMLTag ("a");
				a.attributes.put ("href",groupDirectoryName + "/index.html");
				a.add (new TextNode (groupText));
				indexDocument.add (a);
				groupDirectory = new File (out,groupDirectoryName);
				groupDirectory.mkdir ();
				imageCount = 0;
				groupDocument = new HTMLDocument ();
				groupDocument.body.add (new HTMLTag ("h1",groupText));
				groupContents = new ArrayList<HTMLTag> ();
			}
			show (page);
			LayoutPage layoutPage = (LayoutPage) page;
			for (final LayoutFrame frame : layoutPage.getFrames ()) {
				show (frame);
				frame.getContent ().accept (new PdfContentVisitor<Void> () {
					public Void visit (PdfChartContent content) {
						PdfChartContent chartContent = (PdfChartContent) content; 
						updateFrameTitle (frame, chartContent.getTitle());
						Rectangle position = frame.getFrameConfig ().getPosition ();
						try {
							IOInput chart = renderChart ((PdfChartContent) content, position.getWidth (), position.getHeight ());
							String filename = "image-" + ++imageCount + ".png";
							Util.copy (chart.getInputStream (),new File (groupDirectory,filename));
							String label = "a" + ++labelCount;
							String title = content.getTitle ();
							HTMLTag link = new HTMLTag ("a",title);
							link.attributes.put ("href",'#' + label);
							groupDocument.add (link);
							HTMLTag h2 = new HTMLTag ("h2",title);
							h2.attributes.put ("id",label);
							groupContents.add (h2);
							HTMLTag img = new HTMLTag ("img");
							img.attributes.put ("src",filename);
							groupContents.add (img);
						} catch (IOException ioe) {
							ioe.printStackTrace ();
							throw new Error ();
						}
						return null;
					}

					public Void visit (PdfImageContent content) {
						throw new Error ("html rendering for image content not implemented");
					}

					public Void visit (PdfPriorityListContent content) {
						throw new Error ("html rendering for priority list content not implemented");
					}

					public Void visit (PdfTableContent content) {
						int rows = content.getRows ();
						int cols = content.getColumns ();
						HTMLTag table = new HTMLTag ("table");
						for (int row = 0;row < rows;row++) {
							HTMLTag tableRow = new HTMLTag ("tr");
							for (int col = 0;col < cols;col++) {
								HTMLTag tableItem = new HTMLTag ("td");
								tableItem.add (new TextNode (content.getText (row,col)));
								tableRow.add (tableItem);
							}
							table.add (tableRow);
						}
						groupContents.add (table);
						return null;
					}

					public Void visit (PdfTextContent content) {
						if (content.getTitle () != null)
							groupContents.add (new HTMLTag ("h3",content.getTitle ()));
						if (content.getText () != null)
							groupContents.add (new HTMLTag ("p",content.getText ()));
						return null;
					}

					public Void visit (PdfVariableContent content) {
						throw new Error ("html rendering for variable content not implemented");
					}
				});
			}
		}
		printGroup ();
		indexDocument.print (new File (out,"index.html"));
	}

	private IOInput renderChart(PdfChartContent content, double width, double height) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

		IOOutput out = new IOOutputStreamOutput(buffer, "chart.png", "image/png", null); 

		ChartConfig chartConfig = content.getChartConfig ();
		chartConfig.setChartSize ((int) Math.round (2 * width),(int) Math.round (2 * height));
		getChartRenderer ().render (content.getType (),content.getResult (),chartConfig, out);
	
		IOInput in = new IOInputStreamInput (new ByteArrayInputStream (buffer.toByteArray ()),out.getName (),out.getContentType (),null); 
		if (in.getContentType ().equals ("image/png"))
			return in;
			
		throw new IllegalArgumentException("Unsupported chart output format"); 
	}

	void show (TextSet textSet) {
		for (String textKey : textSet.getTextKeys ())
			System.out.println (textKey + " : " + textSet.getText (textKey));
		System.out.println ();
	}

	public void setDateInfo (ReportMetaResult metaInfo) {
	}
}
