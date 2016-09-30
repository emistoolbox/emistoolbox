package com.emistoolbox.lib.pdf;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.Util;
import info.joriki.pdf.ConstructiblePDFDocument;
import info.joriki.pdf.PDFArray;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFFile;
import info.joriki.pdf.PDFFont;
import info.joriki.pdf.PDFReal;
import info.joriki.pdf.PDFStream;
import info.joriki.pdf.PDFWriter;
import info.joriki.pdf.TextState;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutCoordinatePlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHighchartElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutImageElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVisitor;

import es.jbauer.lib.io.IOOutput;

public class PDFLayoutRenderer implements PDFLayoutVisitor<Void> {
	final static Color debugElementBoxColor = Color.LIGHT_GRAY;
	final static Color debugPaddingBoxColor = Color.CYAN.brighter ();
	final static Color debugBorderBoxColor = Color.MAGENTA.brighter ();

	final static int ndigits = 6;

	private ResourceRenamer resourceRenamer;
	private PrintStream ps;
	private boolean debugging;

	public PDFLayoutRenderer () {
		this (false);
	}

	public PDFLayoutRenderer (boolean debugging) {
		setDebugging (debugging);
	}

	public void setDebugging (boolean debugging) {
		this.debugging = debugging;
	}

	public void render (List<PDFLayout> layouts,IOOutput output) throws IOException {
		ConstructiblePDFDocument document = new ConstructiblePDFDocument ();
		
		// TODO: test document with more than one page
		for (PDFLayout layout : layouts)
			render (layout,document);
		
		PDFWriter writer = new PDFWriter (output.getOutputStream ());
		writer.write (document);
		writer.close ();
	}

	private void render (PDFLayout layout,ConstructiblePDFDocument document) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		ps = new PrintStream (baos);
		resourceRenamer = new ResourceRenamer ("R");
		PDFLayoutFrameElement outerFrame = layout.getOuterFrame ();
		flip (getBoundingBox (outerFrame));
		outerFrame.accept (this);
		ps.close ();
		PDFDictionary page = new PDFDictionary ("Page");
		page.put ("MediaBox",new PDFArray (getBoundingBox (outerFrame)));
		page.putIndirect ("Contents",new PDFStream (baos.toByteArray ()));
		PDFDictionary resources = resourceRenamer.getResources ();
		fontLabeler.addResources (resources);
		alphaStateLabeler.addResources (resources);
		page.putIndirect ("Resources",resources);
		document.addPage (page);
	}

	final static Set<String> standardFontNames = new HashSet<String> ();
	static {
		standardFontNames.add (PDFLayoutFont.FONT_TIMES);
		standardFontNames.add (PDFLayoutFont.FONT_HELVETICA);
		standardFontNames.add (PDFLayoutFont.FONT_COURIER);
	}

	private PDFFont getPDFFont (PDFLayoutFont layoutFont) {
		return PDFFont.getInstance (fontLabeler.getResource (layoutFont),null);
	}

	private void strokeRectangle (Rectangle r) {
		outputRectangle (r);
		ps.print ("s\n");
	}

	private void strokeRectangle (Rectangle r,Color color) {
		pushGraphicsState ();
		setStrokeColor (color);
		strokeRectangle (r);
		popGraphicsState ();
	}

	private void setColor (Color color,String rgbCommand) {
		if (color.getAlpha () != 0xff) {
			ps.print ('/');
			ps.print (alphaStateLabeler.getLabel (color.getAlpha () / (double) 0xff));
			ps.print (" gs\n");
		}
		coordinateCommand (rgbCommand,color.getRGBColorComponents (null));
	}

	private void setStrokeColor (Color color) {
		setColor (color,"RG");
	}

	private void setFillColor (Color color) {
		setColor (color,"rg");
	}

	private void outputRectangle (Rectangle r) {
		coordinateCommand ("re",r.xmin,r.ymin,r.width (),r.height ());
	}

	private void flip (Rectangle r) {
		transform (new Transformation (1,0,0,-1,0,r.ymin + r.ymax));
	}

	private void transform (Rectangle from,Rectangle to) {
		transform (Transformation.matchBoxes (from,to));
	}

	private Stack<Transformation> transformStack = new Stack<Transformation> ();
	{
		transformStack.push (new Transformation ());
	}

	private void pushTransform () {
		transformStack.push (getCurrentTransform ());
	}

	public Transformation getCurrentTransform () {
		return transformStack.peek ();
	}

	private void popTransform () {
		transformStack.pop ();
	}

	private void transform (Transformation transform) {
		transformStack.push (new Transformation (transform,transformStack.pop ()));
	}

	private void outputTransform (Transformation transform) {
		coordinateCommand ("cm",transform.matrix);
	}

	private void coordinateCommand (String command,float ... m) {
		double [] d = new double [m.length];
		for (int i = 0;i < m.length;i++)
			d [i] = m [i];
		coordinateCommand (command,d);
	}

	private void coordinateCommand (String command,double ... m) {
		for (double v : m)
			print (v);
		ps.print (command);
		ps.print ('\n');
	}

	private void pushGraphicsState () {
		ps.print ("q\n");
	}

	private void popGraphicsState () {
		ps.print ("Q\n");
	}

	private void print (double x) {
		if (x < 0) {
			ps.print ('-');
			print (-x);
		}
		else {
			long l = (long) x;
			ps.print (l);
			StringBuilder builder = new StringBuilder ();
			builder.append ('.');
			for (int i = 0;i < ndigits;i++) {
				x -= l;
				x *= 10;
				l = (long) x;
				builder.append (l);
				if (l != 0) {
					ps.print (builder);
					builder.setLength (0);
				}
			}
		}
		ps.print (' ');
	}

	public Void visit(PDFLayout page) throws IOException {
		return null;
	}

	public Void visit (PDFLayoutFrameElement frame) throws IOException {
		PDFLayoutSides<Boolean> alignedWithEdge = new PDFLayoutSides<Boolean> (true);
		Rectangle frameBox = getBoundingBox (frame);
		Rectangle objectFitBox = new Rectangle (frameBox);
		Rectangle previousElementBox = new Rectangle (frameBox.xmax,frameBox.ymax,frameBox.xmin,frameBox.ymin);
		for (PDFLayoutElement element : frame.getElements ()) {
			PDFLayoutPlacement placement = element.getPlacement ();
			PDFLayoutObjectFit objectFit = element.getObjectFit ();
			Rectangle reducedObjectFitBox = new Rectangle (objectFitBox);
			reducedObjectFitBox.transformBy (getCurrentTransform ());
			applyPaddingAndBorder (reducedObjectFitBox,element,-1);
			Rectangle elementBox = getBoundingBox (element,reducedObjectFitBox.width ());
			Rectangle transformedElementBox = new Rectangle (elementBox);
			transformedElementBox.transformBy (getCurrentTransform ());
			// TODO: This is all not tested properly since the tests so far don't have scaled frames so it doesn't really matter which transforms we apply at this point
			double width = 0;
			double height = 0;
			switch (objectFit) {
			case CONTAIN:
				double scale = Math.min (reducedObjectFitBox.width () / transformedElementBox.width (),reducedObjectFitBox.height () / transformedElementBox.height ());
				width = scale * elementBox.width ();
				height = scale * elementBox.height ();
				break;
			case NONE:
				break;
				default:
					throw new Error ("object fit " + objectFit + " not implemented");
			}
			// TODO: Does an object-fitted element cause displacement? Probably not, as it would take up
			// the remaining space in at least one direction, not leaving any space for further object fitting
			Rectangle newElementBox = objectFit == PDFLayoutObjectFit.NONE ? new Rectangle (elementBox) : new Rectangle (0,0,width,height);
			Rectangle augmentedNewElementBox = new Rectangle (newElementBox);
			augmentedNewElementBox.transformBy (getCurrentTransform ());
			applyPaddingAndBorder (augmentedNewElementBox,element,1);
			augmentedNewElementBox.transformBy (getCurrentTransform ().inverse ());
			double x;
			double y;
			if (placement instanceof PDFLayoutCoordinatePlacement) {
				PDFLayoutCoordinatePlacement coordinatePlacement = (PDFLayoutCoordinatePlacement) placement;
				x = coordinatePlacement.getX ();
				y = coordinatePlacement.getY ();
				alignedWithEdge = new PDFLayoutSides<Boolean> (false);
			}
			else if (placement instanceof PDFLayoutAlignmentPlacement) {
				PDFLayoutAlignmentPlacement alignmentPlacement = (PDFLayoutAlignmentPlacement) placement;
				switch (alignmentPlacement.getHorizontalAlignment ()) {
				case BEFORE:
					x = previousElementBox.xmin - augmentedNewElementBox.width ();
					alignedWithEdge.setLeft (false);
					break;
				case AFTER:
					x = previousElementBox.xmax;
					alignedWithEdge.setRight (false);
					break;
				case LEFT:
					x = frameBox.xmin;
					alignedWithEdge.setLeft (true);
					alignedWithEdge.setRight (false);
					break;
				case CENTER:
					x = objectFitBox.xmin + (objectFitBox.width () - augmentedNewElementBox.width ()) / 2;
					alignedWithEdge.setLeft (false);
					alignedWithEdge.setRight (false);
					break;
				case RIGHT:
					x = frameBox.xmax - augmentedNewElementBox.width ();
					alignedWithEdge.setLeft (false);
					alignedWithEdge.setRight (true);
					break;
				default:
					throw new Error ("horizontal placement " + alignmentPlacement.getHorizontalAlignment () + " not implemented");
				}

				switch (alignmentPlacement.getVerticalAlignment ()) {
				case ABOVE:
					y = previousElementBox.ymin - augmentedNewElementBox.height ();
					alignedWithEdge.setTop (false);
					break;
				case BELOW:
					y = previousElementBox.ymax;
					alignedWithEdge.setBottom (false);
					break;
				case TOP:
					y = frameBox.ymin;
					alignedWithEdge.setTop (true);
					alignedWithEdge.setBottom (false);
					break;
				case CENTER:
					y = objectFitBox.ymin + (objectFitBox.height () - augmentedNewElementBox.height ()) / 2;
					alignedWithEdge.setTop (false);
					alignedWithEdge.setBottom (false);
					break;
				case BOTTOM:
					y = frameBox.ymax - augmentedNewElementBox.height ();
					alignedWithEdge.setTop (false);
					alignedWithEdge.setBottom (true);
				default:
					throw new Error ("vertical placement " + alignmentPlacement.getVerticalAlignment () + " not implemented");
				}
			}
			else
				throw new Error ("placement " + placement.getClass () + " not implemented");

			newElementBox.shiftBy (augmentedNewElementBox.xmin - x,augmentedNewElementBox.ymin - y);
			augmentedNewElementBox.shiftBy (augmentedNewElementBox.xmin - x,augmentedNewElementBox.ymin - y);

			if (element.getDisplacement ().getVertical ()) {
				if (alignedWithEdge.getTop ())
					objectFitBox.ymin = augmentedNewElementBox.ymax;
				if (alignedWithEdge.getBottom ())
					objectFitBox.ymax = augmentedNewElementBox.ymin;
			}

			if (element.getDisplacement ().getHorizontal ()) {
				if (alignedWithEdge.getLeft ())
					objectFitBox.xmin = augmentedNewElementBox.xmax;
				if (alignedWithEdge.getRight ())
					objectFitBox.xmax = augmentedNewElementBox.xmin;
			}

			pushTransform ();
//			can either draw new box before transform or old box after transform
//			if (debugging)
//				drawRectangle (newElementBox,debugBoxColor);
			if (!newElementBox.equals (elementBox))
				transform (elementBox,newElementBox);

			Rectangle paddedElementBox = new Rectangle (elementBox);
			paddedElementBox.transformBy (getCurrentTransform ());

			if (debugging)
				strokeRectangle (paddedElementBox,debugElementBoxColor);

			applyPadding (paddedElementBox,element,1);

			if (debugging)
				strokeRectangle (paddedElementBox,debugPaddingBoxColor);

			Rectangle borderElementBox = new Rectangle (paddedElementBox);
			applyBorder (borderElementBox,element,1);

			if (debugging)
				strokeRectangle (borderElementBox,debugBorderBoxColor);

			// TODO: borders with different colours
			PDFLayoutBorderStyle borderStyle = element.getBorderStyle ();
			if (borderStyle != null) {
				Color borderColor = null;
				PDFLayoutLineStyle [] lineStyles = borderStyle.getLineStyles ().getValues (new PDFLayoutLineStyle [4]);

				for (PDFLayoutLineStyle style : lineStyles)
					if (style.getWidth () != 0) {
						Color color = style.getColor ();
						if (borderColor != null && !color.equals (borderColor))
							throw new Error ("different border colours not implemented");
						borderColor = color;
					}

				if (borderColor != null) {
					pushGraphicsState ();
					setFillColor (borderColor);

					Point [] [] paddedPoints = getCornerPoints (paddedElementBox,borderStyle.getBorderRadius (),null);
					Point [] [] borderPoints = getCornerPoints (borderElementBox,borderStyle.getBorderRadius (),lineStyles);

					boolean allSegmentsExist = true;
					// check for beginnings of sections of contiguous existing segments and draw each such section
					for (int i = 0;i < 4;i++) {
						if (lineStyles [i].getWidth () == null && lineStyles [(i + 1) % 4].getWidth () != null) {
							int j = i + 2;
							while (lineStyles [j % 4].getWidth () != null)
								j++;
							List<Point []> section = new ArrayList<Point []> ();
							section.addAll (getPointList (paddedPoints,i + 1,j - 1,false));
							section.addAll (getPointList (borderPoints,i + 1,j - 1,true));
							draw (section);
							coordinateCommand ("f*");
							allSegmentsExist = false;
						}
					}
					// if there was no beginning, all four segments exist; in this case the inner and outer paths are separate
					if (allSegmentsExist) {
						draw (getPointList (paddedPoints,0,3,false));
						draw (getPointList (borderPoints,0,3,true));
						coordinateCommand ("f*");
					}

					if (element.getBackgroundColor () != null) {
						setFillColor (element.getBackgroundColor ());
						draw (getPointList (paddedPoints,0,3,false));
						coordinateCommand ("f*");
					}

					popGraphicsState ();
				}
			}

			boolean isLeaf = !(element instanceof PDFLayoutFrameElement);
			if (isLeaf) {
				flip (elementBox);
				pushGraphicsState ();
				outputTransform (getCurrentTransform ());
			}
			element.accept (this);
			if (isLeaf)
				popGraphicsState ();
			popTransform ();
			previousElementBox = augmentedNewElementBox;
		}
		return null;
	}

	public Void visit (PDFLayoutHighchartElement element) throws IOException {
		throw new Error ("PDF layout highchart element rendering not implemented");
	}

	public Void visit (PDFLayoutImageElement element) throws IOException {
		throw new Error ("PDF layout image element rendering not implemented");
	}

	public Void visit (PDFLayoutPDFElement pdfElement) throws IOException {
		ps.write (resourceRenamer.rename (getPage (pdfElement)));
		return null;
	}

	public Void visit (PDFLayoutTextElement textElement) {
		PDFLayoutFont layoutFont = textElement.getFont ();
		double fontSize = layoutFont.getFontSize ();
		double textAlignmentFactor = getTextAlignmentFactor (textElement);
		ps.print ("BT /" + fontLabeler.getLabel (layoutFont) + " " + fontSize + " Tf\n");
		double ty = 0;
		TextState textState = getTextState (layoutFont);
		for (String piece : pieceMap.get (textElement)) {
			double tx = -textAlignmentFactor * textState.getAdvance (getBytes (piece));
			coordinateCommand ("Tm",1,0,0,1,tx,ty);
			ps.print ("(" + piece + ") Tj T*\n");
			ty -= layoutFont.getLineSpacing () * fontSize;
		}
		ps.print ("ET\n");
		return null;
	}

	public double getTextAlignmentFactor (PDFLayoutTextElement textElement) {
		PDFLayoutPlacement placement = textElement.getPlacement ();
		if (!(placement instanceof PDFLayoutAlignmentPlacement))
			return 0;
		PDFLayoutHorizontalAlignment horizontalAlignment = ((PDFLayoutAlignmentPlacement) placement).getHorizontalAlignment ();
		switch (horizontalAlignment) {
		case LEFT:
		case AFTER:
			return 0;
		case CENTER:
			return 0.5;
		case BEFORE:
		case RIGHT:
			return 1;
		default:
			throw new Error ("horizontal alignment " + horizontalAlignment + " not implemented");
		}
	}

	static abstract class ResourceLabeler<T> {
		private int index;
		private Map<T,String> labels = new HashMap<T,String> ();
		private String prefix;
		private String resourceName;

		public ResourceLabeler (String prefix,String resourceName) {
			this.prefix = prefix;
			this.resourceName = resourceName;
		}

		String getLabel (T t) {
			String label = labels.get (t);
			if (label == null) {
				label = prefix + ++index;
				labels.put (t,label);
			}
			return label;
		}

		void addResources (PDFDictionary resources) {
			if (!labels.isEmpty ()) {
				PDFDictionary dictionary = resources.getOrCreateDictionary (resourceName);
				for (Entry<T,String> entry : labels.entrySet ()) {
					if (dictionary.contains (entry.getValue ()))
						throw new Error ("duplicate key");
					dictionary.putIndirect (entry.getValue (),getResource (entry.getKey ()));
				}
			}
		}

		abstract PDFDictionary getResource (T t);
	}

	ResourceLabeler<PDFLayoutFont> fontLabeler = new ResourceLabeler<PDFLayoutFont> ("F","Font") {
		PDFDictionary getResource (PDFLayoutFont layoutFont) {
			if (!standardFontNames.contains (layoutFont.getFontName ()))
				throw new Error ("only standard fonts implemented");

			PDFDictionary dictionary = new PDFDictionary ("Font");
			dictionary.put ("Subtype","Type1");

			String baseFont = layoutFont.getFontName ();
			boolean isBold = layoutFont.getFontStyle ().isBold ();
			boolean isItalic = layoutFont.getFontStyle ().isItalic ();
			boolean isTimes = baseFont.equals ("Times");
			if (isBold || isItalic) {
				baseFont += '-';
				if (isBold)
					baseFont += "Bold";
				if (isItalic)
					baseFont += isTimes ? "Italic" : "Oblique";
			}
			else if (isTimes)
				baseFont += "-Roman";

			dictionary.put ("BaseFont",baseFont);
			// TODO: using standard encoding for now -- do proper encoding

			return dictionary;
		}
	};

	ResourceLabeler<Double> alphaStateLabeler = new ResourceLabeler<Double> ("AGS","ExtGState") {
		PDFDictionary getResource (Double alpha) {
			PDFDictionary dictionary = new PDFDictionary ("ExtGState");
			dictionary.put ("CA",new PDFReal (alpha));
			dictionary.put ("ca",new PDFReal (alpha));
			return dictionary;
		}
	};

	private Rectangle getBoundingBox (PDFLayoutElement element) throws IOException {
		return getBoundingBox (element,Double.POSITIVE_INFINITY);
	}

	private Rectangle getBoundingBox (PDFLayoutElement element,double maxWidth) throws IOException {
		return element.accept (new PDFLayoutVisitor<Rectangle> () {
			public Rectangle visit(PDFLayout page) throws IOException {
				return null;
			}
			public Rectangle visit (PDFLayoutFrameElement frame) throws IOException {
				return new Rectangle (0,0,frame.getWidth (),frame.getHeight ());
			}

			public Rectangle visit (PDFLayoutHighchartElement element) throws IOException {
				throw new Error ("PDF layout highchart element bounding box not implemented");
			}
			public Rectangle visit (PDFLayoutImageElement element) throws IOException {
				throw new Error ("PDF layout image element bounding box not implemented");
			}
			public Rectangle visit (PDFLayoutPDFElement element) throws IOException {
				return getPage (element).getMediaBox ().toRectangle ();
			}

			public Rectangle visit (PDFLayoutTextElement textElement) {
				PDFLayoutFont layoutFont = textElement.getFont ();
				double fontSize = layoutFont.getFontSize ();
				PDFFont pdfFont = getPDFFont (layoutFont);
				double descent = fontSize * pdfFont.getDescent ();
				double ascent = fontSize * pdfFont.getAscent ();
				TextState textState = getTextState (layoutFont);
				double width = 0;
				List<String> pieces = wrap (textElement,maxWidth);
				for (String piece : pieces)
					width = Math.max (width,textState.getAdvance (getBytes (piece)));
				double textAlignmentFactor = getTextAlignmentFactor (textElement);
				return new Rectangle (-textAlignmentFactor * width,descent - fontSize * layoutFont.getLineSpacing () * (pieces.size () - 1),(1 - textAlignmentFactor) * width,ascent);
			}
		});
	}

	private void applyPaddingAndBorder (Rectangle r,PDFLayoutElement element,double sign) {
		applyPadding (r,element,sign);
		applyBorder (r,element,sign);
	}

	private void applyPadding (Rectangle r,PDFLayoutElement element,double sign) {
		PDFLayoutSides<Double> padding = element.getPadding ();
		if (padding != null) {
			// top and bottom are switched because of the sign convention for the y coordinate
			r.xmin -= sign * padding.getLeft ();
			r.ymin -= sign * padding.getBottom ();
			r.xmax += sign * padding.getRight ();
			r.ymax += sign * padding.getTop ();
		}
	}

	private void applyBorder (Rectangle r,PDFLayoutElement element,double sign) {
		PDFLayoutBorderStyle borderStyle = element.getBorderStyle ();
		if (borderStyle != null) {
			PDFLayoutSides<PDFLayoutLineStyle> lineStyles = borderStyle.getLineStyles ();
			if (lineStyles != null) {
				// top and bottom are switched because of the sign convention for the y coordinate
				r.xmin -= lineStyles.getLeft ().getWidth ();
				r.ymin -= lineStyles.getBottom ().getWidth ();
				r.xmax += lineStyles.getRight ().getWidth ();
				r.ymax += lineStyles.getTop ().getWidth ();
			}
		}
	}

	private byte [] getBytes (String string) {  // TODO: proper encoding
		return string.getBytes ();
	}

	private TextState getTextState (PDFLayoutFont layoutFont) {
		TextState textState = new TextState ();
		textState.setTextFont (getPDFFont (layoutFont),layoutFont.getFontSize ());
		return textState;
	}

	private Map<PDFLayoutPDFElement,PDFDictionary> pageMap = new HashMap<PDFLayoutPDFElement,PDFDictionary> ();

	private PDFDictionary getPage (PDFLayoutPDFElement pdfElement) throws IOException {
		PDFDictionary page = pageMap.get (pdfElement);
		if (page == null) {
			page = new PDFFile (new SeekableByteArray (Util.toByteArray (pdfElement.getInput ().getInputStream ()))).getDocument ().getPage (1);
			pageMap.put (pdfElement,page);
		}
		return page;
	}

	Map<PDFLayoutTextElement,List<String>> pieceMap = new HashMap<PDFLayoutTextElement,List<String>> ();

	// TODO: proper handling of multiple spaces
	private List<String> wrap (PDFLayoutTextElement element,double maxWidth) {
		List<String> pieces = new ArrayList<String> ();
		TextState textState = getTextState (element.getFont ());
		String text = element.getText ().trim () + ' ';
		do {
			int space = -1;
			do {
				int nextSpace = text.indexOf (' ',space + 1);
				if (textState.getAdvance (getBytes (text.substring (0,nextSpace))) > maxWidth) {
					if (space == -1) // if a single word doesn't fit into the width, use it as a piece anyway
						space = nextSpace;
					break;
				}
				space = nextSpace;
			} while (space != text.length () - 1);
			pieces.add (text.substring (0,space));
			text = text.substring (space + 1);
		} while (text.length () > 0);

		pieceMap.put (element,pieces); // for use in rendering

		return pieces;
	}

//  http://math.stackexchange.com/a/221194
//  http://en.wikipedia.org/wiki/Composite_B%C3%A9zier_curve#Approximating_circular_arcs
	final static double k = 1 - 4 * (Math.sqrt (2) - 1) / 3;
	final static double [] ds = {1,(7 - 4 * Math.sqrt (2)) / 3};
	private Point [] [] getCornerPoints (Rectangle r,double radius,PDFLayoutLineStyle [] lineStyles) {
		Point [] dirs = {new Point (0,1),new Point (1,0)};
		Point [] [] cornerPoints = new Point [4] [4];
		Point [] corners = r.corners ();

		// Add four spline points for each of the four corners.
		// For the outer rectangle of the border, line styles are passed in to increase the radius by
		// the line width, so that inner and outer rectangle start bending at the same point along the edge.
		for (int i = 0;i < 4 ;i++)
			for (int j = 0;j < 2;j++) {
				for (int d = 0;d < 2;d++) {
					double s = (radius + getWidth (lineStyles,3 + j - i)) * ds [d];
					cornerPoints [i] [(j << 1) + (j ^ d)] = new Point (corners [i].x + s * dirs [j].x,corners [i].y + s * dirs [j].y);
				}
				dirs [j] = new Point (-dirs [j].y,dirs [j].x);
			}
		return cornerPoints;
	}

	private static double getWidth (PDFLayoutLineStyle [] lineStyles,int index) {
		if (lineStyles == null)
			return 0;
		PDFLayoutLineStyle lineStyle = lineStyles [index % 4];
		if (lineStyle == null)
			return 0;
		Double width = lineStyle.getWidth ();
		return width == null ? 0 : width;
	}

	private List<Point []> getPointList (Point [] [] corners,int from,int to,boolean reverse) {
		List<Point []> pointList = new ArrayList<Point []> ();
		for (int i = from;i <= to;i++)
			pointList.add (corners [i % 4]);
		if (reverse) {
			Collections.reverse (pointList);
			for (Point [] points : pointList)
				for (int i = 0;i < 2;i++) {
					Point p = points [i];
					points [i] = points [3 - i];
					points [3 - i] = p;
				}
		}
		return pointList;
	}

	private void draw (List<Point []> pointList) {
		boolean first = true;
		for (Point [] points : pointList) {
			print (points [0]);
			ps.print (first ? 'm' : 'l');
			ps.print (' ');
			for (int i = 1;i <= 3;i++)
				print (points [i]);
			ps.print ("c ");
			first = false;
		}
		ps.print ("h ");
	}

	private void print (Point p) {
		print (p.x);
		print (p.y);
	}
}
