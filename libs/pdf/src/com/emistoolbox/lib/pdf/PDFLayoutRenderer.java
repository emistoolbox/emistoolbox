package com.emistoolbox.lib.pdf;

import info.joriki.adobe.GlyphList;
import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.Util;
import info.joriki.pdf.ConstructiblePDFDocument;
import info.joriki.pdf.ContentStreamBounder;
import info.joriki.pdf.PDFArray;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFFile;
import info.joriki.pdf.PDFFont;
import info.joriki.pdf.PDFIndirectObject;
import info.joriki.pdf.PDFInteger;
import info.joriki.pdf.PDFName;
import info.joriki.pdf.PDFNull;
import info.joriki.pdf.PDFNumber;
import info.joriki.pdf.PDFObject;
import info.joriki.pdf.PDFReal;
import info.joriki.pdf.PDFStream;
import info.joriki.pdf.PDFString;
import info.joriki.pdf.PDFWriter;
import info.joriki.pdf.TextState;
import info.joriki.util.Range;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutCoordinatePlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElementLink;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHighchartElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutImageElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLink;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPageLink;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutShadowStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableFormat;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutURILink;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVisitor;
import com.emistoolbox.lib.pdf.util.CMYKColor;	
import com.emistoolbox.lib.pdf.util.RangeFinder;

import es.jbauer.lib.io.IOOutput;

public class PDFLayoutRenderer implements PDFLayoutVisitor<Void> {
	final static Color debugElementBoxColor = Color.LIGHT_GRAY;
	final static Color debugPaddingBoxColor = Color.CYAN.brighter ();
	final static Color debugBorderBoxColor = Color.MAGENTA.brighter ();
	final static Color debugTableBoxColor = Color.BLUE.brighter ();

	final static int ndigits = 6;

	private ResourceRenamer resourceRenamer;
	private PrintStream ps;
	private boolean debugging;
	
	private PDFDictionary currentPage;

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
		
		for (PDFLayout layout : layouts)
			render (layout,document);
		
		for (PDFLayoutElement element : positionMap.keySet ())
			addLink (element.getLink (),positionMap.get (element));
		
		PDFWriter writer = new PDFWriter (output.getOutputStream ());
		writer.write (document);
		writer.close ();
	}

	private void render (PDFLayout layout,ConstructiblePDFDocument document) throws IOException {
		currentPage = new PDFDictionary ("Page");
		transformStack.clear ();
		tableTransformStack.clear ();
		transformStack.push (new Transformation ());
		tableTransformStack.push (new Transformation ());
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		ps = new PrintStream (baos);
		resourceRenamer = new ResourceRenamer ("R");
		PDFLayoutFrameElement outerFrame = layout.getOuterFrame ();
		Rectangle boundingBox = getBoundingBox (outerFrame);
		flip (boundingBox);
		outerFrame.accept (this);
		ps.close ();
		applyPadding (boundingBox,outerFrame,1);
		currentPage.put ("MediaBox",new PDFArray (boundingBox));
		currentPage.putIndirect ("Contents",new PDFStream (baos.toByteArray ()));
		PDFDictionary resources = resourceRenamer.getResources ();
		fontLabeler.addResources (resources);
		imageLabeler.addResources (resources);
		graphicsStateLabeler.addResources (resources);
		shadingLabeler.addResources (resources);
		currentPage.putIndirect ("Resources",resources);
		document.addPage (currentPage);
		layoutMap.put (layout,currentPage);
	}
	
	private void addLink (PDFLayoutLink link,Position position) {
		if (link != null) {
			PDFArray annotations = (PDFArray) position.page.get ("Annots");
			if (annotations == null) {
				annotations = new PDFArray ();
				position.page.put ("Annots",annotations);
			}
			PDFDictionary annotation = new PDFDictionary ("Annot");

			if (link.getBorderWidth () != 1 || link.getBorderRadius () != 0)
				annotation.put ("Border",new PDFArray (new double [] {link.getBorderRadius (),link.getBorderRadius (),link.getBorderWidth ()}));
			annotation.put ("Rect",new PDFArray (position.getBoundingBox ()));
			if (position.boxes.size () > 1) {
				PDFArray quadPoints = new PDFArray ();
				for (Rectangle box : position.boxes) {
					quadPoints.add (new PDFReal (box.xmin));
					quadPoints.add (new PDFReal (box.ymin));
					quadPoints.add (new PDFReal (box.xmax));
					quadPoints.add (new PDFReal (box.ymin));
					quadPoints.add (new PDFReal (box.xmax));
					quadPoints.add (new PDFReal (box.ymax));
					quadPoints.add (new PDFReal (box.xmin));
					quadPoints.add (new PDFReal (box.ymax));
				}
				annotation.put ("QuadPoints",quadPoints);
			}

			if (link instanceof PDFLayoutURILink) {
				// this isn't tested
				annotation.put ("Subtype","URI");
				annotation.put ("URI",new PDFString (((PDFLayoutURILink) link).getURI ()));
			}
			else {
				PDFArray destination;

				if (link instanceof PDFLayoutPageLink)
					destination = Position.getPageDestination (layoutMap.get (((PDFLayoutPageLink) link).getTargetPage ()));
				else if (link instanceof PDFLayoutElementLink) {
					PDFLayoutElementLink elementLink = (PDFLayoutElementLink) link;
					destination = positionMap.get (elementLink.getTargetElement ()).getDestination (elementLink.isZooming ());
				}
				else
					throw new Error ("unknown link type " + link.getClass ());
				annotation.put ("Subtype","Link");
				annotation.put ("Dest",destination);
			}
	
			annotations.add (new PDFIndirectObject (annotation));
		}
	}

	final private static Set<String> standardFontNames = new HashSet<String> ();
	static {
		standardFontNames.add (PDFLayoutFont.FONT_TIMES);
		standardFontNames.add (PDFLayoutFont.FONT_HELVETICA);
		standardFontNames.add (PDFLayoutFont.FONT_COURIER);
	}

	private PDFFont getPDFFont (PDFLayoutFont layoutFont) {
		return PDFFont.getInstance (fontLabeler.getResource (layoutFont),null);
	}

	private void debugRectangle (Rectangle r,Color c) {
		if (debugging) {
			pushGraphicsState ();
			ps.print ("0 w\n");
			setStrokeColor (c);
			outputRectangle (r);
			ps.print ("s\n");
			popGraphicsState ();
		}
	}

	private void fillRectangle (Rectangle r) {
		outputRectangle (r);
		ps.print ("f\n");
	}

	private void fillRectangle (Rectangle r,Color c) {
		if (c != null) {
			pushGraphicsState ();
			setFillColor (c);
			outputRectangle (r);
			ps.print ("f\n");
			popGraphicsState ();
		}
	}

	private void setColor (Color color,boolean stroke) {
		if (color.getAlpha () != 0xff)
			setAlpha (color.getAlpha () / (double) 0xff,stroke);
		String command;
		float [] components;
		if (color instanceof CMYKColor) {
			command = "k";
			components = color.getColorComponents (null);
		}
		else {
			command = "rg";
			components = color.getRGBColorComponents (null);
		}
		if (stroke)
			command = command.toUpperCase ();
		coordinateCommand (command,components);
	}

	private void setStrokeColor (Color color) {
		setColor (color,true);
	}

	private void setFillColor (Color color) {
		setColor (color,false);
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

	// The transform stack tracks transforms of non-table elements.
	// On entering a table, the current transform (at the top of the transform stack)
	// is concatenated to the current table transform (at the top of the table transform stack)
	// and the identity is pushed onto the table transform stack. Thus the current total transform
	// (which is needed to calculated link coordinates) is always the product of the current transform
	// and the current table transform.
	private Stack<Transformation> transformStack = new Stack<Transformation> ();
	private Stack<Transformation> tableTransformStack = new Stack<Transformation> ();

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

	private void popTableTransform () {
		tableTransformStack.pop ();
	}

	private void pushTableTransform () {
		tableTransformStack.push (getTotalTransform ());
	}
	
	private Transformation getTotalTransform () {
		return new Transformation (getCurrentTransform (),tableTransformStack.peek ());
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

	static class LayoutState {
		Rectangle box;
		Double baseline;
	}
	
	public Void visit (PDFLayoutFrameElement frame) throws IOException {
		PDFLayoutSides<Boolean> alignedWithEdge = new PDFLayoutSides<Boolean> (true);
		Rectangle frameBox = getBoundingBox (frame);
		Rectangle objectFitBox = new Rectangle (frameBox);
		LayoutState layoutState = new LayoutState ();
		for (PDFLayoutElement element : frame.getElements ())
			render (element,objectFitBox,alignedWithEdge,frameBox,layoutState);
		return null;
	}

	private void render (PDFLayoutElement element,Rectangle box) throws IOException {
		render (element,new Rectangle (box),new PDFLayoutSides<Boolean> (true),box,new LayoutState ());
	}
	
	private void render (PDFLayoutElement element,Rectangle objectFitBox,PDFLayoutSides<Boolean> alignedWithEdge,Rectangle frameBox,LayoutState layoutState) throws IOException {
		Rectangle previousElementBox = layoutState.box;
		PDFLayoutPlacement placement = element.getPlacement ();
		PDFLayoutHorizontalPlacement horizontalPlacement = placement.getHorizontalPlacement ();
		PDFLayoutVerticalPlacement verticalPlacement = placement.getVerticalPlacement ();
		PDFLayoutObjectFit objectFit = element.getObjectFit ();
		Rectangle currentObjectFitBox = new Rectangle (objectFitBox);

		// reduce current object fit box in case of fixed coordinates

		if (horizontalPlacement instanceof PDFLayoutCoordinatePlacement)
			currentObjectFitBox.xmin = ((PDFLayoutCoordinatePlacement) horizontalPlacement).getX ();
		else if (horizontalPlacement instanceof PDFLayoutHorizontalAlignment)
			switch ((PDFLayoutHorizontalAlignment) horizontalPlacement) {
			case BEFORE:
				currentObjectFitBox.xmax = previousElementBox != null ? previousElementBox.xmin : frameBox.xmax;
				break;
			case AFTER:
				currentObjectFitBox.xmin = previousElementBox != null ? previousElementBox.xmax : frameBox.xmin;
				break;
			case PREVIOUS_LEFT:
				currentObjectFitBox.xmin = (previousElementBox != null ? previousElementBox : frameBox).xmin;
				break;
			case PREVIOUS_RIGHT:
				currentObjectFitBox.xmax = (previousElementBox != null ? previousElementBox : frameBox).xmax;
				break;
			case LEFT:
			case CENTER:
			case RIGHT:
				break;
			default:
				throw new Error ("horizontal alignment " + horizontalPlacement + " not implemented");
			}
		else
			throw new Error ("horizontal placement " + horizontalPlacement + " not implemented");

		if (verticalPlacement instanceof PDFLayoutCoordinatePlacement)
			currentObjectFitBox.ymin = ((PDFLayoutCoordinatePlacement) verticalPlacement).getX ();
		else if (verticalPlacement instanceof PDFLayoutVerticalAlignment)
			switch ((PDFLayoutVerticalAlignment) verticalPlacement) {
			case ABOVE:
				currentObjectFitBox.ymax = previousElementBox != null ? previousElementBox.ymin : frameBox.ymax;
				break;
			case BELOW:
				currentObjectFitBox.ymin = previousElementBox != null ? previousElementBox.ymax : frameBox.ymin;
				break;
			case PREVIOUS_TOP:
				currentObjectFitBox.ymin = (previousElementBox != null ? previousElementBox : frameBox).ymin;
				break;
			case PREVIOUS_BOTTOM:
				currentObjectFitBox.ymax = (previousElementBox != null ? previousElementBox : frameBox).ymax;
				break;
			case PREVIOUS_CENTER:
			case PREVIOUS_BASELINE:
			case TOP:
			case CENTER:
			case BOTTOM:
				break;
			default:
				throw new Error ("vertical alignment " + verticalPlacement + " not implemented");
			}
		else
			throw new Error ("vertical placement " + verticalPlacement + " not implemented");

		// determine element size, and scale it if applicable

		Rectangle unrotatedElementBox; 
		Rectangle elementBox;
		Rectangle newElementBox;

		{
			// reduce the current object fit box by padding and border in user space
			Rectangle reducedObjectFitBox = new Rectangle (currentObjectFitBox);
			reducedObjectFitBox.transformBy (getCurrentTransform ());
			applyPaddingAndBorder (reducedObjectFitBox,element,-1);
			reducedObjectFitBox.transformBy (getCurrentTransform ().inverse ());

			unrotatedElementBox = objectFit == PDFLayoutObjectFit.NONE ? getBoundingBox (element,(element.getRotation () & 1) == 0 ? reducedObjectFitBox.width () : reducedObjectFitBox.height ()) : getBoundingBox (element);
			elementBox = new Rectangle (unrotatedElementBox);
			elementBox = elementBox.transformBy (Transformation.rotationThroughRightAngles (element.getRotation ()));
			
			switch (objectFit) {
			case FILL:
				newElementBox = new Rectangle (0,0,reducedObjectFitBox.width (),reducedObjectFitBox.height ());
				break;
			case CONTAIN:
			case SCALE_DOWN:
				double scale = Math.min (reducedObjectFitBox.width () / elementBox.width (),reducedObjectFitBox.height () / elementBox.height ());
				if (objectFit == PDFLayoutObjectFit.SCALE_DOWN && scale > 1)
					scale = 1;
				newElementBox = new Rectangle (0,0,scale * elementBox.width (),scale * elementBox.height ());
				break;
			case NONE:
				newElementBox = new Rectangle (elementBox);
				break;
			default:
				throw new Error ("object fit " + objectFit + " not implemented");
			}
		}

		// TODO: Does an object-fitted element cause displacement? Probably not, as it would take up
		// the remaining space in at least one direction, not leaving any space for further object fitting

		// augment the new element box by padding and border in user space
		Rectangle augmentedNewElementBox = new Rectangle (newElementBox);
		augmentedNewElementBox.transformBy (getCurrentTransform ());
		applyPaddingAndBorder (augmentedNewElementBox,element,1);
		augmentedNewElementBox.transformBy (getCurrentTransform ().inverse ());

		// determine element position
		
		double x;
		if (horizontalPlacement instanceof PDFLayoutCoordinatePlacement) {
			x = ((PDFLayoutCoordinatePlacement) horizontalPlacement).getX ();
			alignedWithEdge.setLeft (false);
			alignedWithEdge.setRight (false);
		}
		else if (horizontalPlacement instanceof PDFLayoutHorizontalAlignment)
			switch ((PDFLayoutHorizontalAlignment) horizontalPlacement) {
			case BEFORE:
			case PREVIOUS_RIGHT:
				x = currentObjectFitBox.xmax - augmentedNewElementBox.width ();
				alignedWithEdge.setLeft (false);
				break;
			case AFTER:
			case PREVIOUS_LEFT:
				x = currentObjectFitBox.xmin;
				alignedWithEdge.setRight (false);
				break;
			case LEFT:
				x = frameBox.xmin;
				alignedWithEdge.setLeft (true);
				alignedWithEdge.setRight (false);
				break;
			case CENTER:
				x = currentObjectFitBox.xmin + (currentObjectFitBox.width () - augmentedNewElementBox.width ()) / 2;
				alignedWithEdge.setLeft (false);
				alignedWithEdge.setRight (false);
				break;
			case RIGHT:
				x = frameBox.xmax - augmentedNewElementBox.width ();
				alignedWithEdge.setLeft (false);
				alignedWithEdge.setRight (true);
				break;
			default:
				throw new Error ("horizontal alignment " + horizontalPlacement + " not implemented");
			}
		else
			throw new Error ("horizontal placement " + horizontalPlacement + " not implemented");
			
		double y;
		if (verticalPlacement instanceof PDFLayoutCoordinatePlacement) {
			y = ((PDFLayoutCoordinatePlacement) verticalPlacement).getX ();
			alignedWithEdge.setTop (false);
			alignedWithEdge.setBottom (false);
		}
		else if (verticalPlacement instanceof PDFLayoutVerticalAlignment)
			switch ((PDFLayoutVerticalAlignment) verticalPlacement) {
			case ABOVE:
			case PREVIOUS_BOTTOM:
				y = currentObjectFitBox.ymax - augmentedNewElementBox.height ();
				alignedWithEdge.setTop (false);
				break;
			case PREVIOUS_BASELINE: // doesn't work with scaling or rotation
				if (layoutState.baseline == null)
					throw new Error ("previous baseline placement without previous text element");
				if (!(element instanceof PDFLayoutTextElement))
					throw new Error ("previous baseline placement for non-text element");
				y = layoutState.baseline + augmentedNewElementBox.ymin - newElementBox.ymin - newElementBox.ymax;
				alignedWithEdge.setTop (false);
				alignedWithEdge.setBottom (false);
				break;
			case PREVIOUS_CENTER:
				y = previousElementBox.ymin + (previousElementBox.height () - augmentedNewElementBox.height ()) / 2;
				break;
			case BELOW:
			case PREVIOUS_TOP:
				y = currentObjectFitBox.ymin;
				alignedWithEdge.setBottom (false);
				break;
			case TOP:
				y = frameBox.ymin;
				alignedWithEdge.setTop (true);
				alignedWithEdge.setBottom (false);
				break;
			case CENTER:
				y = currentObjectFitBox.ymin + (currentObjectFitBox.height () - augmentedNewElementBox.height ()) / 2;
				alignedWithEdge.setTop (false);
				alignedWithEdge.setBottom (false);
				break;
			case BOTTOM:
				y = frameBox.ymax - augmentedNewElementBox.height ();
				alignedWithEdge.setTop (false);
				alignedWithEdge.setBottom (true);
				break;
			default:
				throw new Error ("vertical alignment " + verticalPlacement + " not implemented");
			}
		else
			throw new Error ("vertical placement " + verticalPlacement + " not implemented");

		// shift the (augmented) new element box to the determined position

		newElementBox.shiftBy (augmentedNewElementBox.xmin - x,augmentedNewElementBox.ymin - y);
		augmentedNewElementBox.shiftBy (augmentedNewElementBox.xmin - x,augmentedNewElementBox.ymin - y);

		// displace, if applicable

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

		// now comes the actual drawing

		pushTransform ();
//		can either draw new box before transform or old box after transform
//		if (debugging)
//			drawRectangle (newElementBox,debugBoxColor);
		if (!newElementBox.equals (elementBox))
			transform (elementBox,newElementBox);

		Rectangle paddedElementBox = new Rectangle (elementBox);
		paddedElementBox.transformBy (getCurrentTransform ());

		debugRectangle (paddedElementBox,debugElementBoxColor);

		applyPadding (paddedElementBox,element,1);

		debugRectangle (paddedElementBox,debugPaddingBoxColor);

		Rectangle borderElementBox = new Rectangle (paddedElementBox);
		applyBorder (borderElementBox,element,1);

		debugRectangle (borderElementBox,debugBorderBoxColor);

		// TODO: borders with different colours
		// TODO: don't use splines if the radius is zero
		pushGraphicsState ();
		PDFLayoutBorderStyle borderStyle = element.getBorderStyle ();
		Point [] [] paddedPoints = getCornerPoints (paddedElementBox,borderStyle == null ? 0 : borderStyle.getBorderRadius (),null);
		if (borderStyle != null) {
			Color borderColor = null;
			PDFLayoutSides<PDFLayoutLineStyle> rawLineStyles = borderStyle.getLineStyles ();
			// this is allowed to be null so we can have a border radius without a border, for clipping (and perhaps later for general shadows)
			if (rawLineStyles != null) {
				PDFLayoutLineStyle [] lineStyles = rawLineStyles.getValues (new PDFLayoutLineStyle [4]);

				for (PDFLayoutLineStyle style : lineStyles)
					if (style.getWidth () != 0) {
						Color color = style.getColor ();
						if (borderColor != null && !color.equals (borderColor))
							throw new Error ("different border colours not implemented");
						borderColor = color;
					}

				if (borderColor != null) {
					setFillColor (borderColor);

					Point [] [] borderPoints = getCornerPoints (borderElementBox,borderStyle.getBorderRadius (),lineStyles);

					boolean allSegmentsExist = true;
					// check for beginnings of sections of contiguous existing segments and draw each such section
					for (int i = 0;i < 4;i++) {
						if (lineStyles [i] == null && lineStyles [(i + 1) % 4] != null) {
							int j = i + 2;
							while (lineStyles [j % 4] != null)
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
				}
			}
		}
		if (element.getBackgroundColor () != null) {
			setFillColor (element.getBackgroundColor ());
			draw (getPointList (paddedPoints,0,3,false));
			coordinateCommand ("f*");
		}
		
		popGraphicsState ();

		PDFLayoutShadowStyle shadowStyle = element.getShadowStyle ();
		if (shadowStyle != null) {
			double shiftX = shadowStyle.getShiftX ();
			double shiftY = shadowStyle.getShiftY ();
			Transformation shift = Transformation.translationBy (shiftX,-shiftY);
			Transformation boxMatch = Transformation.matchBoxes (new Rectangle (0,0,1,1),paddedElementBox);
			double d = new Point (shiftX,shiftY).transformedBy (boxMatch.linearPart ().inverse ()).distanceFrom (new Point ());

			PDFDictionary shadingDictionary = new PDFDictionary ();
			shadingDictionary.put ("ShadingType",3);
			shadingDictionary.put ("ColorSpace","DeviceGray");
			shadingDictionary.put ("Coords",new PDFArray (new double [] {0.5,0.5,0,0.5,0.5,0.5}));

			PDFDictionary functionDictionary = new PDFDictionary ();
			functionDictionary.put ("FunctionType",2);
			functionDictionary.put ("Domain",new PDFArray (new double [] {0,1}));
			functionDictionary.put ("C0",new PDFArray (new PDFReal (1 - shadowStyle.getStrength () * 0.5 / d)));
			functionDictionary.put ("N",1);
			shadingDictionary.put ("Function",functionDictionary);

			pushGraphicsState ();
			
			setBlendMode ("Multiply");
			
			PDFDictionary patternDictionary = new PDFDictionary ("Pattern");
			patternDictionary.put ("PatternType",2);
			patternDictionary.put ("Shading",shadingDictionary);
			patternDictionary.put ("Matrix",new PDFArray (new Transformation (boxMatch,shift).matrix));
			coordinateCommand ("/Pattern cs /" + shadingLabeler.getLabel (patternDictionary) + " scn");
			
			fillRectangle (new Rectangle (paddedElementBox).transformBy (shift));
			
			popGraphicsState ();
		}
		
		if (element.isClipping ()) {
			pushGraphicsState ();
			draw (getPointList (paddedPoints,0,3,false));
			coordinateCommand ("W* n");
		}
		
		transform (Transformation.rotationThroughRightAngles (element.getRotation ()));

		boolean isLeaf = !(element instanceof PDFLayoutFrameElement);
		if (isLeaf) {
			if (!(element instanceof PDFLayoutTableElement))
				flip (unrotatedElementBox);
			pushGraphicsState ();
			outputTransform (getCurrentTransform ());
		}
		element.accept (this);
		if (isLeaf)
			popGraphicsState ();
		
		Collection<Rectangle> linkBoxes = element instanceof PDFLayoutTextElement ? getContentRectangles ((PDFLayoutTextElement) element) : Collections.singleton (elementBox);
		for (Rectangle linkBox : linkBoxes)
			linkBox.transformBy (getTotalTransform ());

		positionMap.put (element,new Position (currentPage,linkBoxes));
		
		popTransform ();
		
		if (element.isClipping ())
			popGraphicsState ();
		
		layoutState.box = augmentedNewElementBox;
		if (element instanceof PDFLayoutTextElement)
			layoutState.baseline = newElementBox.ymin + newElementBox.height () * elementBox.ymax / elementBox.height ();
	}

	public Void visit (PDFLayoutHighchartElement element) throws IOException {
		throw new Error ("PDF layout highchart element rendering not implemented");
	}

	public Void visit (PDFLayoutImageElement element) throws IOException {
		BufferedImage image = getImage (element);
		pushGraphicsState ();
		coordinateCommand ("cm",image.getWidth (),0,0,image.getHeight (),0,0);
		ps.print ("/" + imageLabeler.getLabel (element) + " Do\n");
		popGraphicsState ();
		return null;
	}

	public Void visit (PDFLayoutPDFElement pdfElement) throws IOException {
		ps.write (resourceRenamer.rename (getPage (pdfElement)));
		return null;
	}
	
	public Void visit (PDFLayoutTableElement tableElement) throws IOException {
		pushTableTransform ();
		transformStack.push (new Transformation ());
		TableLayout tableLayout = new TableLayout (tableElement);
		for (int row = 0;row < tableElement.getRowCount ();row++)
			for (int col = 0;col < tableElement.getColCount ();col++)
				if (!tableLayout.cellSpanned [row] [col]) {
					PDFLayoutElement element = tableElement.getElement (row,col);
					if (element != null) { 
						Rectangle cellBox = tableLayout.getCellBox (row,col,tableElement.getRowSpan (row,col),tableElement.getColSpan (row,col));
						PDFLayoutTableFormat format = tableElement.getFormat (row,col);
						if (format != null && format.getBackgroundColor () != null)
							fillRectangle (cellBox,format.getBackgroundColor ());
						render (element,cellBox);
						debugRectangle (cellBox,debugTableBoxColor);
					}
				}
		
		// render contiguous sections of border segments with the same line style as a single rectangle
		PDFLayoutLineStyle [] horizontalLineStyles = new PDFLayoutLineStyle [tableElement.getColCount ()]; 
		for (int row = 0;row <= tableElement.getRowCount ();row++) {
			for (int col = 0;col < tableElement.getColCount ();col++)
				horizontalLineStyles [col] = tableLayout.horizontalBorderSpanned [col] [row] ? null : tableElement.getHorizontalLineStyle (row,col);
			for (Range range : RangeFinder.findRanges (horizontalLineStyles))
				if (horizontalLineStyles [range.beg] != null)
					fillRectangle (new Rectangle (tableLayout.horizontalLayout.getBorderCenter (range.beg),tableLayout.verticalLayout.getEnd (row - 1),
								                  tableLayout.horizontalLayout.getBorderCenter (range.end),tableLayout.verticalLayout.getStart (row)),
								                  horizontalLineStyles [range.beg].getColor ());
		}
		
		PDFLayoutLineStyle [] verticalLineStyles = new PDFLayoutLineStyle [tableElement.getRowCount ()]; 
		for (int col = 0;col <= tableElement.getColCount ();col++) {
			for (int row = 0;row < tableElement.getRowCount ();row++)
				verticalLineStyles [row] = tableLayout.verticalBorderSpanned [row] [col] ? null : tableElement.getVerticalLineStyle (row,col);
			for (Range range : RangeFinder.findRanges (verticalLineStyles))
				if (verticalLineStyles [range.beg] != null)
					fillRectangle (new Rectangle (tableLayout.horizontalLayout.getEnd (col - 1),tableLayout.verticalLayout.getBorderCenter (range.beg),
								                  tableLayout.horizontalLayout.getStart (col),tableLayout.verticalLayout.getBorderCenter (range.end)),
								                  verticalLineStyles [range.beg].getColor ());
			}
		
//		separate rendering of each border segment
//
//		for (int row = 0;row <= tableElement.getRowCount ();row++)
//			for (int col = 0;col < tableElement.getColCount ();col++) {
//				PDFLayoutLineStyle horizontalLineStyle = tableLayout.horizontalBorderSpanned [row] [col] ? null : tableElement.getHorizontalLineStyle (row,col);
//				if (horizontalLineStyle != null)
//					fillRectangle (new Rectangle (tableLayout.horizontalLayout.getBorderCenter (col),tableLayout.verticalLayout.getEnd (row - 1),
//								                  tableLayout.horizontalLayout.getBorderCenter (col + 1),tableLayout.verticalLayout.getStart (row)),
//												  horizontalLineStyle.getColor ());
//			}
//		
//		for (int row = 0;row < tableElement.getRowCount ();row++)
//			for (int col = 0;col <= tableElement.getColCount ();col++) {
//				PDFLayoutLineStyle verticalLineStyle = tableLayout.verticalBorderSpanned [row] [col] ? null : tableElement.getVerticalLineStyle (row,col);
//				if (verticalLineStyle != null)
//					fillRectangle (new Rectangle (tableLayout.horizontalLayout.getEnd (col - 1),tableLayout.verticalLayout.getBorderCenter (row),
//								                  tableLayout.horizontalLayout.getStart (col),tableLayout.verticalLayout.getBorderCenter (row + 1)),
//												  verticalLineStyle.getColor ());
//			}
		
		popTableTransform ();
		popTransform ();
		return null;
	}

	public Void visit (PDFLayoutTextElement textElement) throws IOException {
		PDFLayoutFont layoutFont = textElement.getFont ();
		double fontSize = layoutFont.getFontSize ();
		double textAlignmentFactor = getTextAlignmentFactor (textElement);
		setFillColor (layoutFont.getColor ());
		ps.print ("BT /" + fontLabeler.getLabel (layoutFont) + " " + fontSize + " Tf\n");
		double ty = 0;
		TextState textState = getTextState (layoutFont);
		for (String piece : pieceMap.get (textElement)) {
			byte [] bytes = getBytes (piece,layoutFont);
			double tx = -textAlignmentFactor * textState.getAdvance (bytes);
			coordinateCommand ("Tm",1,0,0,1,tx,ty);
			ps.print ("(");
			ps.write (bytes);
			ps.print (") Tj T*\n");
			ty -= layoutFont.getLineSpacing () * fontSize;
		}
		ps.print ("ET\n");
		return null;
	}

	private List<Rectangle> getContentRectangles (PDFLayoutTextElement textElement) {
		PDFLayoutFont layoutFont = textElement.getFont ();
		PDFFont pdfFont = getPDFFont (layoutFont);
		double fontSize = layoutFont.getFontSize ();
		double descent = fontSize * pdfFont.getDescent ();
		double ascent = fontSize * pdfFont.getAscent ();
		double textAlignmentFactor = getTextAlignmentFactor (textElement);
		double ty = 0;
		TextState textState = getTextState (layoutFont);
		
		List<Rectangle> contentRectangles = new ArrayList<Rectangle> ();
		for (String piece : pieceMap.get (textElement)) {
			double width = textState.getAdvance (getBytes (piece,layoutFont));
			contentRectangles.add (new Rectangle (-textAlignmentFactor * width,ty + descent,(1 - textAlignmentFactor) * width,ty + ascent));
			ty -= layoutFont.getLineSpacing () * fontSize;
		}
		return contentRectangles;
	}

	public double getTextAlignmentFactor (PDFLayoutTextElement textElement) {
		PDFLayoutHorizontalPlacement horizontalPlacement = textElement.getPlacement ().getHorizontalPlacement ();
		if (!(horizontalPlacement instanceof PDFLayoutHorizontalAlignment))
			return 0;
		switch ((PDFLayoutHorizontalAlignment) horizontalPlacement) {
		case LEFT:
		case AFTER:
			return 0;
		case CENTER:
			return 0.5;
		case BEFORE:
		case RIGHT:
			return 1;
		default:
			throw new Error ("horizontal alignment " + horizontalPlacement + " not implemented");
		}
	}
	
	private static class Position {
		PDFDictionary page;
		Collection<Rectangle> boxes;

		public Position (PDFDictionary page,Collection<Rectangle> boxes) {
			this.page = page;
			this.boxes = boxes;
		}
		
		Rectangle getBoundingBox () {
			Rectangle boundingBox = new Rectangle ();
			for (Rectangle box : boxes)
				boundingBox.add (box);
			return boundingBox;
		}
		
		PDFArray getDestination (boolean zooming) {
			Rectangle boundingBox = getBoundingBox ();
			return zooming ? getDestination (page,"FitR",boundingBox.toDoubleArray ()) : getXYZDestination (page,boundingBox.ymax);
		}
		
		static PDFArray getPageDestination (PDFDictionary page) {
			return getXYZDestination (page,((PDFNumber) page.getMediaBox ().get (3)).doubleValue ());
		}
		
		private static PDFArray getXYZDestination (PDFDictionary page,double top) {
			return getDestination (page,"XYZ",null,top,null);
		}
		
		private static PDFArray getDestination (PDFDictionary page,String name,double ... parameters) {
			PDFArray destination = getDestination (page,name);
			for (double parameter : parameters)
				destination.add (new PDFReal (parameter));
			return destination;
		}

		private static PDFArray getDestination (PDFDictionary page,String name,Double ... parameters) {
			PDFArray destination = getDestination (page,name);
			for (Double parameter : parameters)
				destination.add (parameter == null ? PDFNull.nullObject : new PDFReal (parameter));
			return destination;
		}

		private static PDFArray getDestination (PDFDictionary page,String name) {
			return new PDFArray (new PDFIndirectObject (page),new PDFName (name));
		}
	}
	
	private Map<PDFLayoutElement,Position> positionMap = new HashMap<PDFLayoutElement,Position> ();
	private Map<PDFLayout,PDFDictionary> layoutMap = new HashMap<PDFLayout,PDFDictionary> ();
	
	private static abstract class ResourceLabeler<T> {
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

	private Map<PDFLayoutFont,PDFDictionary> fontMap = new HashMap<PDFLayoutFont,PDFDictionary> ();
	
	private ResourceLabeler<PDFLayoutFont> fontLabeler = new ResourceLabeler<PDFLayoutFont> ("F","Font") {
		PDFDictionary getResource (PDFLayoutFont layoutFont) {
			PDFDictionary dictionary = fontMap.get (layoutFont);
			if (dictionary == null) {
				if (!standardFontNames.contains (layoutFont.getFontName ()))
					throw new Error ("only standard fonts implemented");

				dictionary = new PDFDictionary ("Font");
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

				fontMap.put (layoutFont,dictionary);
			}
			return dictionary;
		}
	};
	
	private void setAlpha (double alpha,boolean stroke) {
		setGraphicsState (stroke ? "CA" : "ca",new PDFReal (alpha));
	}
	
	private void setBlendMode (String blendMode) {
		setGraphicsState ("BM",new PDFName (blendMode));
	}
	
	private void setGraphicsState (String key,PDFObject value) {
		ps.print ('/');
		ps.print (graphicsStateLabeler.getLabel (Collections.singletonMap (key,value)));
		ps.print (" gs\n");
	}
	
	private ResourceLabeler<Map<String,PDFObject>> graphicsStateLabeler = new ResourceLabeler<Map<String,PDFObject>> ("AGS","ExtGState") {
		PDFDictionary getResource (Map<String,PDFObject> entries) {
			PDFDictionary dictionary = new PDFDictionary ("ExtGState");
			for (Map.Entry<String,? extends PDFObject> entry : entries.entrySet ())
				dictionary.put (entry.getKey (),entry.getValue ());
			return dictionary;
		}
	};
	
	private ResourceLabeler<PDFDictionary> shadingLabeler = new ResourceLabeler<PDFDictionary> ("P","Pattern") {
		PDFDictionary getResource (PDFDictionary shadingDictionary) {
			return shadingDictionary;
		}
	};

	private ResourceLabeler<PDFLayoutImageElement> imageLabeler = new ResourceLabeler<PDFLayoutImageElement> ("Im","XObject") {
		PDFDictionary getResource (PDFLayoutImageElement imageElement) {
			BufferedImage image = getImage (imageElement);
			ColorModel colorModel = image.getColorModel ();
			int numColorComponents = colorModel.getNumColorComponents ();
			int numComponents = colorModel.getNumComponents ();
			if (numColorComponents != 3)
				throw new Error ("non-RGB color model not implemented");
			if (numComponents != numColorComponents && numComponents != numColorComponents + 1)
				throw new Error ("can only handle 0 or 1 alpha components");
			boolean hasAlpha = numComponents > numColorComponents;

			PDFStream imageStream = new PDFStream ();
			imageStream.put ("Type","XObject");
			imageStream.put ("Subtype","Image");
			imageStream.put ("Width",image.getWidth ());
			imageStream.put ("Height",image.getHeight ());
			imageStream.put ("ColorSpace","DeviceRGB");
			imageStream.put ("BitsPerComponent",8);
			byte [] data = ((DataBufferByte) image.getRaster ().getDataBuffer ()).getData ();
			if ("image/jpeg".equals (imageElement.getInput ().getContentType ())) {
				if (hasAlpha)
					throw new Error ("can't handle JPEG alpha"); // might be easy to handle; PDF allows JPEG data to contain alpha
				imageStream.setData (getImageData (imageElement));
				imageStream.put ("Filter","DCTDecode");
			}
			else {
				int npixels = image.getWidth () * image.getHeight ();
				if (data.length != numComponents * npixels)
					throw new Error ("unexpected image data length");
				if (numComponents < 3)
					throw new Error ("non-RGB data not implemented");
				byte [] rgb = new byte [3 * npixels];
				byte [] alpha = hasAlpha ? new byte [npixels] : null;
				boolean actuallyHasAlpha = false;

				for (int row = 0,j = 0,k = 0,l = 0;row < image.getHeight ();row++)
					for (int col = 0;col < image.getWidth ();col++,j += numColorComponents,k += numComponents,l++) {
						rgb [j + 0] = data [k + numComponents - 1];
						rgb [j + 1] = data [k + numComponents - 2];
						rgb [j + 2] = data [k + numComponents - 3];
						if (hasAlpha) {
							alpha [l] = data [k];
							actuallyHasAlpha |= alpha [l] != -1;
						}
					}
				imageStream.setAndCompressData (rgb);
				if (actuallyHasAlpha) {
					PDFStream maskStream = new PDFStream ();
					maskStream.put ("Type","XObject");
					maskStream.put ("Subtype","Image");
					maskStream.put ("Width",image.getWidth ());
					maskStream.put ("Height",image.getHeight ());
					maskStream.put ("ColorSpace","DeviceGray");
					maskStream.put ("BitsPerComponent",8);
					maskStream.setAndCompressData (alpha);
					imageStream.putIndirect ("SMask",maskStream);
				}
//				This would be for using a predictor, but our images aren't likely to be improved by predictors
//				PDFDictionary decodingParameters = new PDFDictionary ();
//				decodingParameters.put ("Columns",image.getWidth ());
//				decodingParameters.put ("Colors",3);
//				decodingParameters.put ("Predictor",2);
//				imageStream.put ("DecodeParms",decodingParameters);
			}
			return imageStream;
		}
	};

	private Rectangle getBoundingBox (PDFLayoutElement element) throws IOException {
		return getBoundingBox (element,Double.POSITIVE_INFINITY);
	}

	private Rectangle getBoundingBox (PDFLayoutElement element,final double maxWidth) throws IOException {
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
				BufferedImage image = getImage (element);
				return new Rectangle (0,0,image.getWidth (),image.getHeight ());
			}
			
			public Rectangle visit (PDFLayoutPDFElement element) throws IOException {
				PDFDictionary page = getPage (element);
				return element.isCropping () ?
						new ContentStreamBounder ().getBoundingBox (page) :
						page.getMediaBox ().toRectangle ();
			}

			public Rectangle visit (PDFLayoutTextElement textElement) {
				PDFLayoutFont layoutFont = textElement.getFont ();
				double fontSize = layoutFont.getFontSize ();
				getBytes (textElement.getText (),layoutFont); // this is just to update the encoding so the text state advance uses the right widths for non-ASCII characters 
				PDFFont pdfFont = getPDFFont (layoutFont);
				double descent = fontSize * pdfFont.getDescent ();
				double ascent = fontSize * pdfFont.getAscent ();
				TextState textState = getTextState (layoutFont);
				double width = 0;
				List<String> pieces = wrap (textElement,maxWidth);
				for (String piece : pieces)
					width = Math.max (width,textState.getAdvance (getBytes (piece,layoutFont)));
				double textAlignmentFactor = getTextAlignmentFactor (textElement);
				return new Rectangle (-textAlignmentFactor * width,descent - fontSize * layoutFont.getLineSpacing () * (pieces.size () - 1),(1 - textAlignmentFactor) * width,ascent);
			}
			
			public Rectangle visit (PDFLayoutTableElement tableElement) throws IOException {
				return new TableLayout (tableElement).getBoundingBox ();
			}
		});
	}
	
	private class TableLayout {
		LinearLayout horizontalLayout;
		LinearLayout verticalLayout;
		
		boolean [] [] horizontalBorderSpanned;
		boolean [] [] verticalBorderSpanned;
		boolean [] [] cellSpanned;
		
		TableLayout (PDFLayoutTableElement tableElement) throws IOException {
			horizontalBorderSpanned = new boolean [tableElement.getColCount ()] [tableElement.getRowCount () + 1];
			verticalBorderSpanned   = new boolean [tableElement.getRowCount ()] [tableElement.getColCount () + 1];
			cellSpanned             = new boolean [tableElement.getRowCount ()] [tableElement.getColCount ()];
			
			for (int row = 0;row < tableElement.getRowCount ();row++)
				for (int col = 0;col < tableElement.getColCount ();col++)
					for (int i = 0;i < tableElement.getRowSpan (row,col);i++)
						for (int j = 0;j < tableElement.getColSpan (row,col);j++) {
							if (i != 0)
								horizontalBorderSpanned [col + j] [row + i] = true;
							if (j != 0)
								verticalBorderSpanned [row + i] [col + j] = true;
							if (i != 0 ||j != 0)
								cellSpanned [row + i] [col + j] = true;
						}

			double [] widths = tableElement.getWidths ();
			double [] heights = tableElement.getHeights ();
			int [] [] rowSpans = new int [tableElement.getColCount ()] [tableElement.getRowCount ()];
			int [] [] colSpans = new int [tableElement.getRowCount ()] [tableElement.getColCount ()];
			double [] [] cellWidths  = new double [tableElement.getRowCount ()] [tableElement.getColCount ()];
			double [] [] cellHeights = new double [tableElement.getColCount ()] [tableElement.getRowCount ()];
			
			for (int row = 0;row < tableElement.getRowCount ();row++)
				for (int col = 0;col < tableElement.getColCount ();col++)
					if (!cellSpanned [row] [col]) {
						rowSpans [col] [row] = tableElement.getRowSpan (row,col);
						colSpans [row] [col] = tableElement.getColSpan (row,col);
						PDFLayoutElement element = tableElement.getElement (row,col);
						if (element != null) {
							PDFLayoutObjectFit objectFit = element.getObjectFit ();
							
							boolean fixedWidth = widths != null;
							boolean scaling = objectFit != PDFLayoutObjectFit.NONE;

							double width = 0;
							if (fixedWidth) {
								for (int i = 0;i < colSpans [row] [col];i++)
									width += widths [i];
								if (!scaling) {
									Rectangle widthBox = new Rectangle (0,0,width,0);
									applyPaddingAndBorder (widthBox,element,-1);
									width = widthBox.width ();
								}
							}
							
							Rectangle boundingBox = PDFLayoutRenderer.this.getBoundingBox (element,fixedWidth && !scaling ? width : Double.POSITIVE_INFINITY);
							applyPaddingAndBorder (boundingBox,element,1);
							double scale = fixedWidth && scaling ? width / boundingBox.width () : 1;
							cellWidths  [row] [col] = scale * boundingBox.width ();
							cellHeights [col] [row] = scale * boundingBox.height ();
						}
					}

			double [] [] horizontalBorderWidths = new double [tableElement.getColCount ()] [tableElement.getRowCount () + 1];
			double [] [] verticalBorderWidths   = new double [tableElement.getRowCount ()] [tableElement.getColCount () + 1];

			for (int row = 0;row < tableElement.getRowCount ();row++)
				for (int col = 0;col <= tableElement.getColCount ();col++)
					if (!verticalBorderSpanned [row] [col])
						verticalBorderWidths [row] [col] = getWidth (tableElement.getVerticalLineStyle (row,col));

			for (int row = 0;row <= tableElement.getRowCount ();row++)
				for (int col = 0;col < tableElement.getColCount ();col++)
					if (!horizontalBorderSpanned [col] [row])
						horizontalBorderWidths [col] [row] = getWidth (tableElement.getHorizontalLineStyle (row,col));

			horizontalLayout = new LinearLayout (cellWidths,colSpans,verticalBorderWidths,widths);
			verticalLayout   = new LinearLayout (cellHeights,rowSpans,horizontalBorderWidths,heights);
		}	
		
		Rectangle getBoundingBox () {
			return new Rectangle (0,0,horizontalLayout.getDimension (),verticalLayout.getDimension ());
		}
		
		Rectangle getCellBox (int row,int col,int rowSpan,int colSpan) {
			return new Rectangle (horizontalLayout.getStart (col),verticalLayout.getStart (row),horizontalLayout.getEnd (col + colSpan - 1),verticalLayout.getEnd (row + rowSpan - 1));
		}
	}
	
	private static class LinearLayout {
		double [] cellDimensions;
		double [] borderDimensions;
		
		LinearLayout (double [] [] cellWidths,int [] [] spans,double [] [] borderWidths,double [] fixedWidths) {
			int count = cellWidths.length == 0 ? 0 : cellWidths [0].length;
			borderDimensions = new double [count + 1];

			// first find border dimensions; they're independent of spanning
			for (double [] b : borderWidths)
				for (int i = 0;i < b.length;i++)
					borderDimensions [i] = Math.max (borderDimensions [i],b [i]);

			// if fixed widths are given, use them
			if (fixedWidths != null) {
				cellDimensions = fixedWidths;
				return;
			}
			
			cellDimensions = new double [count];

			// first use the non-spanning widths
			for (int i = 0;i < cellWidths.length;i++)
				for (int j = 0;j < count;j++)
					if (spans [i] [j] == 1)
						cellDimensions [j] = Math.max (cellDimensions [j],cellWidths [i] [j]);
			
			// now grow cells if necessary to satisfy the spanning widths
			// in each step, satisfy the remaining unsatisfied width that requires the largest increase per spanned cell
			for (;;) {
				double maxRequiredIncrease = 0;
				int maxIndex = 0;
				int maxSpan = 0;
				
				for (int i = 0;i < cellWidths.length;i++)
					for (int j = 0;j < count;j++) {
						int span = spans [i] [j];
						if (span > 1) {
							double requiredIncrease = (cellWidths [i] [j] - getEnd (j + span - 1) + getStart (j)) / span;
							if (requiredIncrease > maxRequiredIncrease) {
								maxRequiredIncrease = requiredIncrease;
								maxIndex = j;
								maxSpan = span;
							}
						}
					}
				
				if (maxRequiredIncrease <= 1e-10)
					break;

				for (int k = 0;k < maxSpan;k++)
					cellDimensions [maxIndex + k] += maxRequiredIncrease;
			}
			
			// add infinitesimal extra space to avoid text wrapping through rounding
			for (int i = 0;i < cellDimensions.length;i++)
				cellDimensions [i] *= 1 + 1e-10;
		}
		
		public String toString () {
			return Arrays.toString (cellDimensions) + ", " + Arrays.toString (borderDimensions);
		}
		
		double getDimension () {
			double dimension = 0;
			for (double cellDimension : cellDimensions)
				dimension += cellDimension;
			for (double borderDimension : borderDimensions)
				dimension += borderDimension;
			return dimension;
		}
		
		double getStart (int index) {
			double position = 0;
			for (int i = 0;i < index;i++)
				position += cellDimensions [i];
			for (int i = 0;i <= index;i++)
				position += borderDimensions [i];
			return position;
		}
		
		// index may be -1
		public double getEnd (int index) {
			double position = 0;
			for (int i = 0;i <= index;i++)
				position += cellDimensions [i];
			for (int i = 0;i <= index;i++)
				position += borderDimensions [i];
			return position;
		}
		
		public double getBorderCenter (int index) {
			return index == 0 ? 0 : index == cellDimensions.length ? getDimension () : (getEnd (index - 1) + getStart (index)) / 2;
		}
	}
	
	private double getWidth (PDFLayoutLineStyle lineStyle) {
		return lineStyle == null ? 0 : lineStyle.getWidth (); 
	}

	private void applyPaddingAndBorder (Rectangle r,PDFLayoutElement element,double sign) {
		applyPadding (r,element,sign);
		applyBorder (r,element,sign);
	}

	private void applyPadding (Rectangle r,PDFLayoutElement element,double sign) {
		PDFLayoutSides<Double> padding = element.getPadding ();
		if (padding != null) {
			// top and bottom are switched because of the sign convention for the y coordinate
			if (padding.getLeft() != null)
				r.xmin -= sign * padding.getLeft ();
			if (padding.getBottom() != null)
				r.ymin -= sign * padding.getBottom ();
			if (padding.getRight() != null)
				r.xmax += sign * padding.getRight ();
			if (padding.getTop() != null)
				r.ymax += sign * padding.getTop ();
		}
	}

	private void applyBorder (Rectangle r,PDFLayoutElement element,double sign) {
		PDFLayoutBorderStyle borderStyle = element.getBorderStyle ();
		if (borderStyle != null) {
			PDFLayoutSides<PDFLayoutLineStyle> lineStyles = borderStyle.getLineStyles ();
			if (lineStyles != null) {
				// top and bottom are switched because of the sign convention for the y coordinate
				r.xmin -= sign * lineStyles.getLeft ().getWidth ();
				r.ymin -= sign * lineStyles.getBottom ().getWidth ();
				r.xmax += sign * lineStyles.getRight ().getWidth ();
				r.ymax += sign * lineStyles.getTop ().getWidth ();
			}
		}
	}

	private byte [] getBytes (String string,PDFLayoutFont layoutFont) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		outer:
		for (char c : string.toCharArray ())
			if (c < 0x80)
				// ASCII character
				baos.write (c);
			else {
				// non-ASCII character
				String glyph = GlyphList.getGlyphName (c);
				// retrieve encoding differences array
				PDFDictionary fontDictionary = fontLabeler.getResource (layoutFont);
				PDFDictionary encodingDictionary = fontDictionary.getOrCreateDictionary ("Encoding");
				PDFArray differences = (PDFArray) encodingDictionary.get ("Differences");
				if (differences == null) {
					differences = new PDFArray (new PDFInteger (0x80));
					encodingDictionary.put ("Differences",differences);
				}
				// check if it already contains this glyph
				for (int i = 1;i < differences.size ();i++)
					if (((PDFName) differences.get (i)).getName ().equals (glyph)) {
						baos.write (0x80 + i - 1);
						continue outer;
					}
				// it doesn't -- add the glyph 
				differences.add (new PDFName (glyph));
			}
		return baos.toByteArray ();
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

	private Map<PDFLayoutTextElement,List<String>> pieceMap = new HashMap<PDFLayoutTextElement,List<String>> ();

	// TODO: proper handling of multiple spaces
	private List<String> wrap (PDFLayoutTextElement element,double maxWidth) {
		List<String> pieces = new ArrayList<String> ();
		PDFLayoutFont layoutFont = element.getFont ();
		TextState textState = getTextState (layoutFont);
		String text = element.getText ().trim () + ' ';
		do {
			int space = -1;
			do {
				int nextSpace = text.indexOf (' ',space + 1);
				if (textState.getAdvance (getBytes (text.substring (0,nextSpace),layoutFont)) > maxWidth) {
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
//	final static double k = 1 - 4 * (Math.sqrt (2) - 1) / 3;
	final private static double [] ds = {1,(7 - 4 * Math.sqrt (2)) / 3};
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

	private Map<PDFLayoutImageElement,byte []> imageDataMap = new HashMap<PDFLayoutImageElement,byte[]> ();

	private byte [] getImageData (PDFLayoutImageElement imageElement) {
		byte [] imageData = imageDataMap.get (imageElement);
		if (imageData == null) {
			try {
				imageData = Util.toByteArray (imageElement.getInput ().getInputStream ());
			} catch (IOException ioe) {
				ioe.printStackTrace ();
				throw new Error ("can't read image");
			}
			imageDataMap.put (imageElement,imageData);
		}
		return imageData;
	}

	private Map<PDFLayoutImageElement,BufferedImage> imageMap = new HashMap<PDFLayoutImageElement,BufferedImage> ();

	private BufferedImage getImage (PDFLayoutImageElement imageElement) {
		BufferedImage image = imageMap.get (imageElement);
		if (image == null) {
			try {
				image = ImageIO.read (new ByteArrayInputStream (getImageData (imageElement)));
			} catch (IOException e) {
				e.printStackTrace ();
				throw new Error ("couldn't read image");
			}
			imageMap.put (imageElement,image);
		}
		return image;
	}
}
