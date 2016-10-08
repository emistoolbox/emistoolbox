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
import com.emistoolbox.server.renderer.gis.GisUtil;
import com.emistoolbox.server.renderer.pdfreport.EmisPageGroup;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;
import com.emistoolbox.server.renderer.pdfreport.PdfGisContent;
import com.emistoolbox.server.renderer.pdfreport.PdfImageContent;
import com.emistoolbox.server.renderer.pdfreport.PdfPriorityListContent;
import com.emistoolbox.server.renderer.pdfreport.PdfReport;
import com.emistoolbox.server.renderer.pdfreport.PdfTableContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PDFAdvancedReportWriter;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfTextContent;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfVariableContentImpl;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutFrame;
import com.emistoolbox.server.renderer.pdfreport.layout.LayoutPage;
import com.emistoolbox.server.util.ZipArchiver;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileInput;
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
			int dot = name.indexOf ('.');
			if (dot != -1) {
				attributes.put ("class",name.substring (dot + 1));
				name = name.substring (0,dot);
			}
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
		
		public boolean isEmpty () {
			return children.isEmpty ();
		}
		
		public void removeLastChild () {
			children.remove (children.size () - 1);
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
	
	private File indexDirectory;
	private int imageCount;
	private int labelCount;
	private String reportName;
	private HTMLTag linkTag;

	private HTMLTag getReportNameTag () {
		return new HTMLTag ("div.report-name",reportName);
	}

	public void writeReport (PdfReport report,File out) throws IOException {
		reportName = report.getReportConfig ().getName ();
		String filename = out.getName ();
		if (!filename.endsWith (zipSuffix))
			throw new Error ("trying to write HTML ZIP archive to file without " + zipSuffix + " suffix");
		indexDirectory = new File (out.getParentFile (),filename.substring (0,filename.length () - zipSuffix.length ()));
		indexDirectory.mkdir ();
		HTMLDocument indexDocument = new HTMLDocument ();
		indexDocument.add (getReportNameTag ());
		linkTag = new HTMLTag ("div.emis-links"); // this gets reused on every page, and is added to each page twice
		renderPageGroup (report.getPageGroup (),indexDocument.body,1,null,null,true);
		indexDocument.head.add (new HTMLTag ("style",".smaller{font-size:20px}.hierarchy-lowest{margin:0px}"));
		indexDocument.print (new File (indexDirectory,"index.html"));
		ZipArchiver.archive (indexDirectory,out,false);
	}

	private void renderPageGroup (EmisPageGroup pageGroup,HTMLTag container,int headingLevel,String linkPrefix,String titlePrefix,boolean condensing) throws IOException {
		List<EmisPageGroup> pageGroups = pageGroup.getPageGroups ();
		List<EmisPdfPage> pages = pageGroup.getPages ();

		if (getListSize (pageGroups) != 0 && getListSize (pages) != 0)
			throw new Error ("HTML rendering for page group containing both subgroups and pages not implemented");

		String level = pageGroup.getLevel ();
		String name = pageGroup.getName ();
		Integer id = pageGroup.getId ();
		boolean isLeaf = pageGroups.isEmpty ();
		String fullName = level + " " + name;
		if (isLeaf && isShowIds ())
			fullName += " (" + id + ")";
		
		linkPrefix = newPrefix (linkPrefix,fullName);
		
		if (condensing && getListSize (pageGroups) == 1)
			renderPageGroup (pageGroups.get (0),container,headingLevel,linkPrefix,newPrefix (titlePrefix,fullName),true);
		else {
			HTMLTag heading = new HTMLTag ("h" + Math.min (headingLevel,4));
			HTMLNode nameNode = new TextNode (fullName);

			HTMLTag link = new HTMLTag ("a",linkPrefix);
			String anchor = level + id;
			link.attributes.put ("href","../index.html#" + anchor);
			heading.attributes.put ("id",anchor);
			if (!linkTag.isEmpty ())
				linkTag.add (new TextNode (" / "));
			linkTag.add (link);
			
			if (!pages.isEmpty ()) {
				HTMLTag a = new HTMLTag ("a",nameNode);
				String groupDirectoryName = id + "-" + sanitize (name);
				a.attributes.put ("href",groupDirectoryName + "/index.html");
				nameNode = a;

				final File groupDirectory = new File (indexDirectory,groupDirectoryName);
				groupDirectory.mkdir ();
				imageCount = 0;
				HTMLDocument groupDocument = new HTMLDocument ();
				groupDocument.add (linkTag);
				groupDocument.add (getReportNameTag ());
				groupDocument.add (new HTMLTag ("h1",fullName));
				heading.attributes.put ("class","hierarchy-lowest");
				final HTMLTag toc = new HTMLTag ("div.emis-toc");
				groupDocument.add (toc);
				groupDocument.add (new HTMLTag ("hr"));

				for (EmisPdfPage page : pages) {
					lastTitle = null;
					HTMLTag pageTag = new HTMLTag ("div.emis-page");
					groupDocument.add (pageTag);
					if (!(page instanceof LayoutPage))
						throw new Error ("can only handle layout pages");
					renderTitles (page,pageTag,false);
					for (final LayoutFrame frame : ((LayoutPage) page).getFrames ()) {
						final HTMLTag frameTag = new HTMLTag ("div.emis-frame");
						pageTag.add (frameTag);
						PdfContent content = frame.getContent ();
						final String title = content.getTitle ();
						if (content instanceof PdfTextContent || content instanceof PdfChartContent)
							updateFrameTitle (frame,title);
						renderTitles (frame,frameTag,true);
						content.accept (new PdfContentVisitor<Void> () {
							public Void visit (PdfChartContent content) {
								Rectangle position = frame.getFrameConfig ().getPosition ();
								try {
									renderImage (renderChart (content, position.getWidth (), position.getHeight ()));
								} catch (IOException e) {
									e.printStackTrace ();
									throw new Error ();
								}
								return null;
							}

							public Void visit (PdfImageContent content) {
								try {
									renderImage (content.getFile ());
								} catch (IOException e) {
									e.printStackTrace ();
									throw new Error ();
								}
								return null;
							}
							
							public Void visit (PdfGisContent content) {
								try { 
									String[] result = GisUtil.renderGisResult(content); 
									renderImage(new IOFileInput(new File(result[0]), "image/png", null));
								} catch (IOException e) {
									e.printStackTrace(); 
									throw new Error (); 
								}
								
								return null; 
							}

							public Void visit (PdfPriorityListContent content) {
								return visit((PdfTableContent) content);
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
								frameTag.add (table);
								return null;
							}

							public Void visit (PdfTextContent content) {
								if (content.getText () != null)
									frameTag.add (new HTMLTag ("p",content.getText ()));
								return null;
							}
							
							private void renderImage (IOInput input) throws IOException {
								String filename = "image-" + ++imageCount + ".png";
								Util.copy (input.getInputStream (),new File (groupDirectory,filename));
								String label = "a" + ++labelCount;
								HTMLTag link = new HTMLTag ("a",title);
								link.attributes.put ("href",'#' + label);
								toc.add (link);
								HTMLTag h2 = new HTMLTag ("h2",title);
								h2.attributes.put ("id",label);
								frameTag.add (h2);
								HTMLTag img = new HTMLTag ("img");
								img.attributes.put ("src",filename);
								frameTag.add (img);
							}
						});
						renderFooter (frame,frameTag);
					}
					renderFooter (page,pageTag);
				}
				groupDocument.add (linkTag);
				groupDocument.print (new File (groupDirectory,"index.html"));
			}

			if (titlePrefix == null)
				heading.add (nameNode);
			else {
				HTMLTag div1 = new HTMLTag ("div.smaller",titlePrefix);
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
			
			if (!isLeaf) {
				HTMLTag ul = new HTMLTag ("ul");
				for (EmisPageGroup childGroup : pageGroups)
					renderPageGroup (childGroup,ul,headingLevel + 1,null,null,false);
				target.add (ul);
			}

			linkTag.removeLastChild ();
			if (!linkTag.isEmpty ())
				linkTag.removeLastChild ();
		}
	}
	
	private String lastTitle;
	private void renderTitles (TextSet textSet,HTMLTag target,boolean dontRepeat) {
		String title = textSet.getText (PdfText.TEXT_TITLE);
		if (title != null && !(dontRepeat && title.equals (lastTitle)))
			target.add (new HTMLTag ("h1.emis-title",title));
		lastTitle = title;
		
		String subtitle = textSet.getText (PdfText.TEXT_SUBTITLE);
		if (subtitle != null)
			target.add (new HTMLTag ("h2.emis-subtitle",subtitle));
	}
	
	private void renderFooter (TextSet textSet,HTMLTag target) {
		String footer = textSet.getText (PdfText.TEXT_FOOTER);
		if (footer != null)
			target.add (new HTMLTag ("h2.emis-footer",footer));
	}

	private IOInput renderChart(PdfChartContent content, double width, double height) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

		IOOutput out = new IOOutputStreamOutput(buffer, "chart.png", "image/png", null); 

		ChartConfig chartConfig = content.getChartConfig ();
		chartConfig.setChartSize (800,400);
		getChartRenderer ().render (content.getType (),content.getResult (),chartConfig, out);
	
		IOInput in = new IOInputStreamInput (new ByteArrayInputStream (buffer.toByteArray ()),out.getName (),out.getContentType (),null); 
		if (in.getContentType ().equals ("image/png"))
			return in;
			
		throw new IllegalArgumentException("Unsupported chart output format"); 
	}

	private static String sanitize (String s) {
		StringBuilder builder = new StringBuilder ();
		for (char c : s.toCharArray ())
			if (Character.isLetter (c))
				builder.append (c);
		return builder.toString ();
	}

	public void setDateInfo (ReportMetaResult metaInfo) {
	}

	@Override
	public String getExtension() 
	{ return zipSuffix; }
}
