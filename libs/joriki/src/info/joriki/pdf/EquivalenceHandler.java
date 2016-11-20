/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

/**
 * implements the equivalencies in the PDF spec, so that handlers don't
 * need to know about them if they don't want to.
 */
abstract public class EquivalenceHandler extends ContentStreamAdapter
{
  public void drawRectangle (double x,double y,double width,double height)
  {
    moveTo (x,y);
    lineTo (x + width,y);
    lineTo (x + width,y + height);
    lineTo (x,y + height);
    closePath ();
  }

  public void moveToNextLineAndShow (byte [] text)
  {
    moveToNextLine ();
    show (text);
  }

  public void moveToNextLineAndSetTextLeading (double tx,double ty)
  {
    moveToNextLine (tx,ty);
    setTextLeading (-ty);
  }

  public void closeAndUsePath (boolean stroke,int fillRule,int clipRule)
  {
    closePath ();
    usePath (stroke,fillRule,clipRule);
  }
}
