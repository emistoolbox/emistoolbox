/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.List;
import java.util.Stack;

import info.joriki.util.Assertions;

import info.joriki.graphics.Transformation;

abstract public class StateHandler extends EquivalenceHandler
{
  /* The following methods declared in ContentStreamHandler
     remain empty :

     void moveTo (double x,double y);
     void lineTo (double x,double y);
     void curveTo (double [] coors);
     void closePath ();
     void usePath (boolean stroke,int fillRule,int clipRule);
     void markContent (PDFName tag,PDFDictionary properties);
     void beginMarkedContent (PDFName tag,PDFDictionary properties);
     void endMarkedContent ();
     void shade (PDFShading shading);
     void drawImage (PDFImage image); // remains no-op
     void drawForm (PDFStream formStream);
     void setGlyphWidth (double x,double y);
     void setGlyphMetrics (double [] metrics);
     void finishPage ();
  */

  protected GraphicsState graphicsState;
  protected Stack graphicsStateStack = new Stack ();
  protected Stack initialGraphicsStateStack = new Stack ();

  // I didn't want the resourceResolver to sit this
  // far up in the hierarchy, but the fact that device
  // colors spaces are mapped to default color spaces
  // *upon use* requires us to have a resource resolver
  // around whenever we use the current color space
  // or pattern.
  protected ResourceResolver resourceResolver = new ResourceResolver ();

  protected PDFColorSpace getColorSpace (int paintType)
  {
    return resourceResolver.map (graphicsState.colorSpace [paintType]);
  }

  protected PDFPattern getPattern (int paintType)
  {
    PDFPattern pattern = graphicsState.pattern [paintType];
    if (pattern instanceof ShadingPattern)
      ((ShadingPattern) pattern).shading.baseColorSpace.map (resourceResolver);
    return pattern;
  }

  public int getRGBColor (int paintType)
  {
    return getColorSpace (paintType).toRGB (graphicsState.color [paintType]);
  }

  public String getHexColor (int paintType)
  {
    return getColorSpace (paintType).toHexString (graphicsState.color [paintType]);
  }

  public void setGraphicsState (PDFDictionary dictionary)
  {
    graphicsState.set (dictionary);
  }

  public void setColorSpace (int paintType,PDFColorSpace colorSpace)
  {
    graphicsState.colorSpace [paintType] = colorSpace;
  }

  public void setColor (int paintType,float [] color)
  {
    graphicsState.color [paintType] = color;

    // rest ist just for assertion
    PDFColorSpace colorSpace = graphicsState.colorSpace [paintType];
    // this condition shouldn't be necessary, see also below (setPattern)
    // it seems possible to use an uncolored pattern color space
    // with a colored pattern and ignore the base color space.
    if (!(colorSpace instanceof UncoloredPatternColorSpace))
      Assertions.expect (color.length,colorSpace.ncomponents);
  }

  public void setPattern (int paintType,PDFPattern pattern)
  {
    graphicsState.pattern [paintType] = pattern;

    // rest is just for assertion
    PDFColorSpace colorSpace = graphicsState.colorSpace [paintType];
    Assertions.expect (colorSpace instanceof PatternColorSpace);
    /* I had the following assertion. It failed on the PDF for the
       SVG spec (REC-SVG-20010904.pdf). It seems to me that the
       PDF spec says the following: for an uncolored pattern,
       use a pattern color space of the form [/Pattern <base color space>] 
       and then specify the color to be used by the pattern in the
       base color space; use a pattern space of the form /Pattern
       for colored patterns, since you don't need a base color space,
       since you don't need to specify colors for them. But in the
       above file they set [/Pattern /DeviceRGB] as color space and
       then use a colored tiling pattern. The pattern is set using
       /P0 scn, and it uses its own color space (an indexed color space),
       so all this is consistent with how colored patterns should be
       used, just that the /DeviceRGB base color space is superfluous.
       So, Acrobat Reader let's it go through, so I guess we should --
       just ignore the spurious base color space.
       if (pattern != null)
       Assertions.expect (colorSpace instanceof UncoloredPatternColorSpace,
       pattern instanceof TilingPattern &&
       ((TilingPattern) pattern).paintType ==
       TilingPattern.UNCOLORED);
    */
  }

  public void setFlatness (double flatness)
  {
    graphicsState.flatness = flatness;
  }

  public void setSmoothness (double smoothness)
  {
    graphicsState.smoothness = smoothness;
  }

  public void setLineJoin (int join)
  {
    graphicsState.lineJoin = join;
  }

  public void setLineCap (int cap)
  {
    graphicsState.lineCap = cap;
  }

  public void setLineWidth (double width)
  {
    graphicsState.lineWidth = width;
  }

  public void setMiterLimit (double limit)
  {
    graphicsState.miterLimit = limit;
  }

  public void setDash (double [] pattern,double offset)
  {
    graphicsState.dashPattern = pattern;
    graphicsState.dashOffset = offset;
  }

  public void setRenderingIntent (PDFName intent)
  {
    graphicsState.renderingIntent = intent;
  }

  public void concatenateMatrix (double [] matrix)
  {
    graphicsState.ctm.concat (new Transformation (matrix));
  }

  public void gsave ()
  {
    graphicsStateStack.push (graphicsState);
    graphicsState = (GraphicsState) graphicsState.clone ();
  }

  public void grestore ()
  {
    graphicsState = (GraphicsState) graphicsStateStack.pop ();
  }

  public void beginTextObject ()
  {
    graphicsState.textState.textMatrix     = new Transformation (); // identity
    graphicsState.textState.textLineMatrix = new Transformation (); // identity
  }

  public void endTextObject ()
  {
    graphicsState.textState.textMatrix =
      graphicsState.textState.textLineMatrix =
      null;
  }

  public void setTextMatrix (double [] matrix)
  {
    graphicsState.textState.textLineMatrix.copy (matrix);
    graphicsState.textState.textMatrix.copy (matrix);
  }

  public void setTextFont (PDFFont font,double size)
  {
    graphicsState.textState.setTextFont (font,size);
  }
  
  public void setCharacterSpacing (double spacing)
  {
    graphicsState.textState.characterSpacing = spacing;
  }

  public void setWordSpacing (double spacing)
  {
    graphicsState.textState.wordSpacing = spacing;
  }

  public void setTextLeading (double leading)
  {
    graphicsState.textState.leading = leading;
  }

  public void setTextRise (double rise)
  {
    graphicsState.textState.textRise = rise;
    graphicsState.textState.calculateTextPrefix ();
  }

  public void setHorizontalScaling (double scaling)
  {
    graphicsState.textState.horizontalScaling = scaling;
    graphicsState.textState.calculateTextPrefix ();
  }

  public void setTextRenderingMode (TextRenderingMode renderingMode)
  {
    graphicsState.textState.renderingMode = renderingMode;
  }

  public void moveToNextLine ()
  {
    moveToNextLine (0,-graphicsState.textState.leading); // sign error in PDF Reference
  }

  public void moveToNextLine (double tx,double ty)
  {
    graphicsState.textState.textLineMatrix.translateBy (tx,ty);
    graphicsState.textState.textMatrix.copy
      (graphicsState.textState.textLineMatrix);
  }

  public void show (byte [] text)
  {
    graphicsState.textState.advanceBy (text);
  }

  public void show (List text)
  {
    graphicsState.textState.advanceBy (text);
  }

  protected Stack outerTransformStack = new Stack ();

  public void beginPage ()
  {
    graphicsStateStack.clear ();
    initialGraphicsStateStack.clear ();
    outerTransformStack.clear ();
    outerTransformStack.push (Transformation.identity);
    graphicsState = new GraphicsState ();
  }

  public void beginContentStream ()
  {
    initialGraphicsStateStack.push (graphicsState);
    gsave ();
  }

  public void endContentStream ()
  {
    grestore ();
    initialGraphicsStateStack.pop ();
  }

  protected GraphicsState getInitialGraphicsState ()
  {
    return (GraphicsState) initialGraphicsStateStack.peek ();
  }

  protected Transformation totalTransform ()
  {
    Transformation outerTransform = (Transformation) outerTransformStack.peek ();
    return outerTransform == null ? null : 
      new Transformation (outerTransform,graphicsState.ctm);
  }
}
