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
import com.emistoolbox.server.renderer.pdfreport.EmisPageGroup;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
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
import com.emistoolbox.server.util.ZipArchiver;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOInputStreamInput;
import es.jbauer.lib.io.impl.IOOutputStreamOutput;

public class HTMLReportWriter extends PDFAdvancedReportWriter {
	final static String zipSuffix = ".zip";
	
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
			this (name,new TextNode (content));
		}
		
		public HTMLTag (String name,HTMLNode node) {
			this (name);
			add (node);
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
	
	HTMLDocument indexDocument;
	HTMLDocument groupDocument;
	File indexDirectory;
	File groupDirectory;
	int groupCount;
	int imageCount;
	int labelCount;
	
	List<HTMLNode> groupContents;

	public void writeReport (PdfReport report,File out) throws IOException {
		String filename = out.getName ();
		if (!filename.endsWith (zipSuffix))
			throw new Error ("trying to write HTML ZIP archive to file without " + zipSuffix + " suffix");
		indexDirectory = new File (out.getParentFile (),filename.substring (0,filename.length () - zipSuffix.length ()));
		indexDirectory.mkdir ();
		indexDocument = new HTMLDocument ();
		groupDirectory = null;
		groupDocument = null;
		renderPageGroup (report.getPageGroup (),indexDocument.body,1,null,true);
		indexDocument.head.add (new HTMLTag ("style",".smaller{font-size:20px}.hierarchy-lowest{margin:0px}"));
		indexDocument.print (new File (indexDirectory,"index.html"));
		ZipArchiver.archive (indexDirectory,out,false);
	}

//  debugging output
//	private void showPageGroup (EmisPageGroup pageGroup,String prefix) {
//		System.out.println (prefix + "page group: " + pageGroup.getName () + " : " + pageGroup.getLevel () + " / " + pageGroup.getId ());
//		prefix += "  ";
//		for (EmisPageGroup childGroup : pageGroup.getPageGroups ())
//			showPageGroup (childGroup,prefix);
//		for (EmisPdfPage page : pageGroup.getPages ())
//			System.out.println (prefix + "page: " + page.getText (PdfText.TEXT_GROUP));
//	}
	
	private <T> int getListSize (List<T> list) {
		return list == null ? 0 : list.size ();
	}
	
	private void renderPageGroup (EmisPageGroup pageGroup,HTMLTag container,int headingLevel,String titlePrefix,boolean condensing) throws IOException {
		List<EmisPageGroup> pageGroups = pageGroup.getPageGroups ();
		List<EmisPdfPage> pages = pageGroup.getPages ();
		
		if (getListSize (pageGroups) != 0 && getListSize (pages) != 0)
			throw new Error ("HTML rendering for page group containing both subgroups and pages not implemented");

		String name = pageGroup.getName ();
		
		if (condensing && getListSize (pageGroups) == 1)
			renderPageGroup (pageGroups.get (0),container,headingLevel,titlePrefix == null ? name : titlePrefix + " / " + name,true);
		else {
			HTMLTag heading = new HTMLTag ("h" + Math.min (headingLevel,4));
			HTMLNode nameNode = new TextNode (name);
			if (!pages.isEmpty ()) {
				String groupDirectoryName = ++groupCount + "-" + name.replace ("'","");
				HTMLTag a = new HTMLTag ("a",nameNode);
				a.attributes.put ("href",groupDirectoryName + "/index.html");
				nameNode = a;
				groupDirectory = new File (indexDirectory,groupDirectoryName);
				groupDirectory.mkdir ();
				imageCount = 0;
				groupDocument = new HTMLDocument ();
				groupDocument.body.add (new HTMLTag ("h1",name));
			}
			if (titlePrefix == null)
				heading.add (nameNode);
			else {
				HTMLTag div1 = new HTMLTag ("div",titlePrefix);
				div1.attributes.put ("class","smaller");
				HTMLTag div2 = new HTMLTag ("div");
				div2.add (nameNode);
				heading.add (div1);
				heading.add (div2);
			}
			
			HTMLTag target;
			if (container.name.equals ("ul")) {
				target = new HTMLTag ("li");
				container.add (target);
			}
			else
				target = container;

			target.add (heading);
			
			if (!pageGroups.isEmpty ()) {
				HTMLTag ul = new HTMLTag ("ul");
				for (EmisPageGroup childGroup : pageGroups)
					renderPageGroup (childGroup,ul,headingLevel + 1,null,false);
				target.add (ul);
			}

			if (!pages.isEmpty ()) {
				heading.attributes.put ("class","hierarchy-lowest");
				groupContents = new ArrayList<HTMLNode> ();

				for (EmisPdfPage page : pages) {
					if (!(page instanceof LayoutPage))
						throw new Error ("can only handle layout pages");
					show (page);
					LayoutPage layoutPage = (LayoutPage) page;
					for (final LayoutFrame frame : layoutPage.getFrames ()) {
						show (frame);
						frame.getContent ().accept (new PdfContentVisitor<Void> () {
							public Void visit (PdfChartContent content) {
								Rectangle position = frame.getFrameConfig ().getPosition ();
								try {
									renderImage (renderChart (content, position.getWidth (), position.getHeight ()),frame,content);
								} catch (IOException e) {
									e.printStackTrace ();
									throw new Error ();
								}
								return null;
							}

							public Void visit (PdfImageContent content) {
								try {
									renderImage (content.getFile (),frame,content);
								} catch (IOException e) {
									e.printStackTrace ();
									throw new Error ();
								}
								return null;
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

				for (HTMLNode node : groupContents)
					groupDocument.add (node);
				groupDocument.print (new File (groupDirectory,"index.html"));
			}
		}
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

	private void renderImage (IOInput input,LayoutFrame frame,PdfContent content) throws IOException {
		String title = content.getTitle ();
		updateFrameTitle (frame,title);
		String filename = "image-" + ++imageCount + ".png";
		Util.copy (input.getInputStream (),new File (groupDirectory,filename));
		String label = "a" + ++labelCount;
		HTMLTag link = new HTMLTag ("a",title);
		link.attributes.put ("href",'#' + label);
		groupDocument.add (link);
		HTMLTag h2 = new HTMLTag ("h2",title);
		h2.attributes.put ("id",label);
		groupContents.add (h2);
		HTMLTag img = new HTMLTag ("img");
		img.attributes.put ("src",filename);
		groupContents.add (img);
	}

	void show (TextSet textSet) {
		for (String textKey : textSet.getTextKeys ())
			System.out.println (textKey + " : " + textSet.getText (textKey));
		System.out.println ();
	}

	public void setDateInfo (ReportMetaResult metaInfo) {
	}

	@Override
	public String getExtension() 
	{ return zipSuffix; }
}
