/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

/*
  About UNDOCUMENTED features:
  In quite a number of places the freetype source contains code for what
  they call UNDOCUMENTED features. These fall into four categories:
  -- some are just that: UNDOCUMENTED features that I've seen nowhere
     else and that I've taken over from them. These are: allowing the
     zero vector to be "normalized" and using cvt [-1] = 0 in MIRP
  -- one is irrelevant for us, since it pertains to setting the
     single width value.
  -- one is actually documented: setting rp2 to point after MIRP
  -- the rest are more like "VAGUE" features, although the freetype
     authors seem to take a different view on this.
     a) The touching behaviour of the various shifting operations.
        I had it wrong before I looked at the freetype source, but
it makes perfect sense: touching all points on a contour
would be less than helpful, since it would only render IUP
useless. So SHP should touch since it moves a single point,
but SHC and SHZ shouldn't because they move contours.
The spec never explicitly says what "touching" is, the freetype
authors simply assumed that any move results in a touch; it
seems a good explication of "touching" would be that it occurs
when a point is moved relative to the other points on its contour.
     b) The spec speaks about "creating" points in the twilight zones.
        This diction correlates exactly with the code marked as
"UNDOCUMENTED" that sets original positions in the twilight
zone -- I'm now doing this like freetype does because,
as the freetype source mentions, Times.ttf expects this behavior.
The initial (0,0) coordinates of twilight points are only used
as initial values for SCFS and MIAP moves -- the coordinates
of a twilight point are otherwise considered to be undefined,
and are defined by one of the instructions that are allowed
to "create" them: MIAP, MSIRP, MIRP and SCFS. The "original"
coordinates of the point are set to the initial coordinates
upon creation, not to (0,0) -- in fact I don't even allocate
their array until creation, to be sure they're not
inadvertently used. MSIRP and MIRP first set the point
to the reference point and then move it along the
freedom vector. Cut-in tests are not performed for increate
twilight points, since there are no original coordinates
to perform them with and they wouldn't make sense (which
is sort of the same thing).

  There are also some things marked BULLSHIT in the freetype source.
  Most of these are wrong -- that the dual projection vector is to
  be used is well-documented, as is the fact that MD uses zp0 - zp1.
  The remaining one, that the flag on MD is reversed in the spec,
  seemed unlikely to me, but Garamond,BoldItalic in 1589660447.pdf
  from Texterity contains an MD that would use an increate twilight
  point if we don't go along with this.
*/

package info.joriki.truetype;

import java.io.PrintStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import info.joriki.font.GlyphProvider;
import info.joriki.font.GlyphInterpreter;

import info.joriki.sfnt.SFNTFile;
import info.joriki.sfnt.SFNTTable;
import info.joriki.sfnt.SFNTSpeaker;
import info.joriki.sfnt.MaximumProfile;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.SimpleNumber;
import info.joriki.util.NotImplementedException;

import info.joriki.graphics.Transformation;

public class ByteCodeInterpreter
  extends ByteCodeSpeaker
  implements SFNTSpeaker, GlyphProvider, TrueTypeOptions
{
  public int xoff;

  class EnginePoint
  {
    double [] x;
    double [] x0;
    boolean [] touched;
    boolean onCurve;
    boolean twilight;
    
    // original twilight point coordinates are (0,0)
    // they aren't reset for each glyph.
    // x0 will be defined when the point is "created" 
    EnginePoint ()
    {
      x = new double [2];
      x0 = null;
      touched = new boolean [2];
      twilight = true;
    }
    
    EnginePoint (double [] x,boolean onCurve)
    {
      this.x = x;
      this.x0 = x.clone ();
      this.touched = new boolean [2];
      this.onCurve = onCurve;
      this.twilight = false;
    }
    
    public String toString ()
    {
      StringBuilder stringBuilder = new StringBuilder ();
      stringBuilder.append ('(');
      if (x0 == null)
        stringBuilder.append ("nil,nil");
      else
        {
          General.append (stringBuilder,x0 [0],6);
          stringBuilder.append (',');
          General.append (stringBuilder,x0 [1],6);
        }
      stringBuilder.append (") -> (");
      if (x == null)
        stringBuilder.append ("nil,nil");
      else
        {
          General.append (stringBuilder,x [0],6);
          stringBuilder.append (',');
          General.append (stringBuilder,x [1],6);
        }
      stringBuilder.append (") [").append (touched [0]).
        append (",").append (touched [1]).append ("]");
      return stringBuilder.toString ();
    }

    void flip ()
    {
      onCurve = !onCurve;
    }

    void touch (boolean touching)
    {
      if (!inhibitGridFit)
        for (int i = 0;i < 2;i++)
          if (freedomVector [i] != 0)
            touched [i] = touching;
    }
    
    void shiftParallel (double shift,boolean touch)
    {
      if (!inhibitGridFit)
        {
          for (int i = 0;i < 2;i++)
            x [i] += shift * freedomVector [i];
          if (touch)
            touch (true);
        }
    }

    final void shift (double shift,boolean touch)
    {
      shiftParallel (shift / dot,touch);
    }

    final void create ()
    {
      x0 = x.clone ();
    }

    final void move (double coordinate)
    {
      shift (coordinate - coordinate (),true);
    }

    final EnginePoint referencePoint ()
    {
      return points [0] [rp [0]];
    }

    final void moveRelative (double delta)
    {
      move (referencePoint ().coordinate () + delta);
    }

    final void setToReference ()
    {
      System.arraycopy (referencePoint ().x0,0,x,0,2);
    }

    final double coordinate ()
    {
      return dotProduct (x,projectionVector);
    }

    final double originalCoordinate ()
    {
      return dotProduct (x0,dualProjectionVector);
    }

    final double nonDualOriginalCoordinate ()
    {
      return dotProduct (x0,projectionVector);
    }
  }

  List indexList = new ArrayList ();
  List pointList = new ArrayList ();

  public void addIndex (int [] index)
  {
    indexList.add (index);
  }

  int npoint = 0;

  public void addPoint (double [] x,boolean onCurve)
  {
    pointList.add (new EnginePoint (x,onCurve));
  }

  public double [] popPhantomPoint ()
  {
    return ((EnginePoint) pointList.remove (pointList.size () - 1)).x;
  }

  List pointHarvest;
  List indexHarvest;

  public void plow ()
  {
    pointHarvest = new ArrayList ();
    indexHarvest = new ArrayList ();
  }

  public void harvest (Transformation transformation)
  {
    Iterator pointIterator = pointList.iterator ();
    while (pointIterator.hasNext ())
      {
        EnginePoint p = (EnginePoint) pointIterator.next ();
        transformation.transform (p.x);
        transformation.transform (p.x0);
      }
    pointHarvest.addAll (pointList);
    indexHarvest.addAll (indexList);
    pointList.clear ();
    indexList.clear ();
  }

  public void sow ()
  {
    pointList = pointHarvest;
    indexList = indexHarvest;
  }

  public double [] getPoint (int i)
  {
    return ((EnginePoint) pointList.get (i)).x;
  }

  public double [] getHarvest (int i)
  {
    return ((EnginePoint) pointHarvest.get (i)).x;
  }

  EnginePoint [] [] zpoints = new EnginePoint [2] [];
  EnginePoint [] [] points = new EnginePoint [3] [];

  // graphics state
  double [] dualProjectionVector;
  double [] projectionVector;
  double [] freedomVector;
  double dot; // cached scalar product of projection vector and freedom vector
  int [] rp;
  boolean autoFlip;
  boolean inhibitGridFit;
  int loop;

  final static double [] [] axisVectors = {{1,0},{0,1}};

  void setVectors (int axis)
  {
    freedomVector = projectionVector = dualProjectionVector = 
      axisVectors [axis];
    dot = 1;
  }

  void setProjectionVector (double [] projectionVector)
  {
    double norm = Math.sqrt
      (projectionVector [0] * projectionVector [0] +
       projectionVector [1] * projectionVector [1]);
    // UNDOCUMENTED feature from freetype source:
    // possible to attempt normalization of zero vector
    if (norm != 0)
      {
        projectionVector [0] /= norm;
        projectionVector [1] /= norm;
      }
    this.dualProjectionVector = this.projectionVector = projectionVector;
    calculateDot ();
  }

  void setProjectionVector (int axis)
  {
    setProjectionVector (axisVectors [axis]);
  }

  void setFreedomVector (double [] freedomVector)
  {
    this.freedomVector = freedomVector;
    calculateDot ();
  }

  void setFreedomVector (int axis)
  {
    setFreedomVector (axisVectors [axis]);
  }

  double [] getVectorFromLine (int opcode,boolean setDual)
  {
    EnginePoint p2 = popPoint (2);
    EnginePoint p1 = popPoint (1);
    boolean perpendicular = (opcode & 1) == 1;
    if (setDual)
      dualProjectionVector = vectorFromLine (perpendicular,p1.x0,p2.x0);
    return vectorFromLine (perpendicular,p1.x,p2.x);
  }

  double [] vectorFromLine (boolean perpendicular,double [] x1,double [] x2)
  {
    double [] vector = vectorFromLine (x1,x2);
    if (perpendicular)
      {
        double tmp = vector [0];
        vector [0] = -vector [1];
        vector [1] = tmp;
      }
    return vector;
  }

  double [] vectorFromLine (double [] x1,double [] x2)
  {
    return new double [] {x1 [0] - x2 [0],x1 [1] - x2 [1]};
  }

  void setZones (int zone)
  {
    setZone (0,zone);
    setZone (1,zone);
    setZone (2,zone);
  }

  void setZone (int zindex,int zone)
  {
    points [zindex] = zpoints [zone];
  }

  void initializeGraphicsState ()
  {
    setVectors (0);
    setZones (1);
    rp = new int [3];
    autoFlip = true;
    loop = 1;
    // don't reset inhibitGridFit; this is globally set in the CVT program
  }

  Subroutine [] functions;
  Subroutine [] instructions;
  Number [] storage;
  Number [] stack;
  int nstack;
  double [] cvt;
  double [] cvtTemplate;

  static class Argument extends SimpleNumber
  {
    int arg;

    Argument (int arg)
    {
      this.arg = arg;
    }

    public double doubleValue ()
    {
      return arg / 64.;
    }

    public int intValue ()
    {
      return arg;
    }

    public String toString ()
    {
      return Integer.toString (arg);
    }
  }

  static class VectorComponent extends SimpleNumber
  {
    int arg;

    VectorComponent (int arg)
    {
      this.arg = arg;
    }

    public double doubleValue ()
    {
      return arg / (double) 0x4000;
    }

    public int intValue ()
    {
      return arg;
    }

    public String toString ()
    {
      return Integer.toString (arg);
    }
  }

  String classString (Number n)
  {
    return n + " (" + n.getClass () + ")";
  }

  void push (Number n)
  {
    if (Options.tracing)
    {
      System.out.println ("nstack = " + nstack);
      System.out.println ("pushing " + classString (n));
    }
    stack [nstack++] = n;
  }

  Number pop ()
  {
    if (Options.tracing)
    {
      System.out.println ("nstack = " + nstack);
      System.out.println ("popping " + classString (stack [nstack - 1]));
    }
    return stack [--nstack];
  }

  Number peek ()
  {
    return stack [nstack - 1];
  }

  void push (int i)
  {
    push (new Argument (i));
  }

  void pushDouble (double d)
  {
    push (new Double (d));
  }

  double popDouble ()
  {
    Assertions.expect (!(peek () instanceof Integer));
    return pop ().doubleValue ();
  }

  void pushBoolean (boolean b)
  {
    push (new Integer (b ? 1 : 0));
  }
  
  boolean popBoolean ()
  {
    // Impact.ttf (see below, popInt) forces us to allow VectorComponents here.
    // I don't think the people who wrote that could spell "type safety".
    // A strange font called WPMathA forces us to allow doubles.
    return pop ().doubleValue () != 0;
  }

  int popInt ()
  {
    Number n = pop ();
    // I'd thought this case wouldn't occur --
    // but Impact.ttf contains a case where a number
    // is read from the CVT and some arithmetic is
    // performed on it, ending in a DIV by 4096.
    // It doesn't use any measurements provided by
    // us, so the font program knows exactly what it's
    // doing and expects a well-defined result independent
    // of scaling. In principle, we would have to simulate
    // any truncation and rounding that would have occured
    // in a "proper" byte code interpreter along the way,
    // but it would be rather devious of them to rely on
    // that. At least in this example, it's enough to make
    // sure that we truncate the final result and faithfully
    // reproduce any exact 64ths; since our binary doubles
    // can do that, we should be OK.
    if (n instanceof Double)
      return (int) (64 * n.doubleValue ());
    Assertions.expect (!(n instanceof VectorComponent));
    return n.intValue ();
  }

  EnginePoint popPoint (int zindex)
  {
    return points [zindex] [popInt ()];
  }

  void pushInt (int i)
  {
    push (new Integer (i));
  }

  void pushVectorComponent (double c)
  {
    push (new VectorComponent ((int) (0x4000 * c)));
  }

  double popVectorComponent ()
  {
    Number number = pop ();
    if (number instanceof Argument)
      number = new VectorComponent (number.intValue ());
    else if (number instanceof Double) // occurs in Contractor.pdf
      {
        Assertions.expect (number.doubleValue (),0);
        number = new VectorComponent (0);
      }
    Assertions.expect (number instanceof VectorComponent);
    return number.doubleValue ();
  }

  void pushVector (double [] v)
  {
    pushVectorComponent (v [0]);
    pushVectorComponent (v [1]);
  }

  double [] popVector ()
  {
    double y = popVectorComponent ();
    double x = popVectorComponent ();
    return new double [] {x,y};
  }

  double compare ()
  {
    Number n2 = pop ();
    Number n1 = pop ();
    if (n1 instanceof Double || n2 instanceof Double)
      {
        Assertions.expect (!(n1 instanceof Integer || n2 instanceof Integer));
        return n1.doubleValue () - n2.doubleValue ();
      }
    return n1.intValue () - n2.intValue ();
  }

  private static final boolean unscathed (Number n)
  {
    return n instanceof Integer || n instanceof Argument;
  }

  boolean even ()
  {
    Number n = pop ();
    if (!unscathed (n))
      // this test is meaningless in our case, so we just make sure
      // we get the only meaningful result right, namely that 0 is even.
      return true;
    int i;
    if (n instanceof Argument)
      {
        i = ((Argument) n).arg;
        if ((i & 0x1f) != 0)
          throw new NotImplementedException ("odd/even test for non-integers");
        i >>= 6;
      }
    else if (n instanceof Integer)
      i = ((Integer) n).intValue ();
    else
      throw new InternalError ();
    return (i & 1) == 0;
  }

  double unaryOperation (int opcode,double op)
  {
    switch (opcode)
      {
      case 0x64 : return Math.abs (op);
      case 0x65 : return -op;
      default   : throw new InternalError ();
      }
  }

  double binaryOperation (int opcode,double op1,double op2)
  {
    return (opcode & 0x60) == 0x60 ? // ADD/SUB
      (opcode == 0x60 ? op1 + op2 : op1 - op2) :
      (opcode == 0x8b ? Math.max (op1,op2) : Math.min (op1,op2));
  }

  void binaryOperation (int opcode)
  {
    Number n2 = pop ();
    Number n1 = pop ();

    if (n1 instanceof Double || n2 instanceof Double)
      {
        Assertions.expect (!(n1 instanceof Integer || n2 instanceof Integer));
        pushDouble (binaryOperation (opcode,n1.doubleValue (),n2.doubleValue ()));
      }
    else
      {
        int result = (int) binaryOperation (opcode,n1.intValue (),n2.intValue ());
        if (n1 instanceof VectorComponent && n2 instanceof VectorComponent)
          push (new VectorComponent (result));
        else
          {
            Assertions.expect (!(n1 instanceof VectorComponent ||
                                 n2 instanceof VectorComponent));
            if (n1 instanceof Integer || n2 instanceof Integer)
              pushInt (result);
            else
              push (result);
          }
      }
  }

  EnginePoint getShiftPoint (int opcode)
  {
    boolean which = (opcode & 1) == 0;
    return points [which ? 1 : 0] [rp [which ? 2 : 1]];
  }

  public ByteCodeInterpreter (SFNTFile fontFile)
  {
    this (fontFile,System.err);
  }

  public ByteCodeInterpreter (SFNTFile fontFile,PrintStream log)
  {
    super (log);

    if (!enableHinting.isSet ())
      return;

    MaximumProfile maximumProfile = (MaximumProfile) fontFile.getTable (MAXP);
    ControlValueTable controlValueTable =
      (ControlValueTable) fontFile.getTable (CVT);
    if (controlValueTable != null)
      cvt = controlValueTable.toDoubleArray ();
    stack = new Number [maximumProfile.extension.maxStackElements];
    storage = new Number [maximumProfile.extension.maxStorage];
    // The spec says that reading storage that you haven't written to
    // yields undefined results. Freetype, however, initializes the
    // storage area to zero on size change. We have no size change.
    for (int i = 0;i < storage.length;i++)
      storage [i] = new Argument (0);
    functions = new Subroutine [maximumProfile.extension.maxFunctionDefs];
    zpoints [0] = new EnginePoint [maximumProfile.extension.maxTwilightPoints];
    instructions = new Subroutine [256];
    for (int i = 0;i < zpoints [0].length;i++)
      zpoints [0] [i] = new EnginePoint ();

    initializeGraphicsState ();
    run (fontFile.getTable (FPGM));
    run (fontFile.getTable (PREP));
    cvtTemplate = cvt;
  }

  public void interpret (GlyphInterpreter interpreter)
  {
    interpret ((OutlineInterpreter) interpreter);
  }

  public void interpret (OutlineInterpreter interpreter)
  {
    EnginePoint [] points = getPointArray ();

    double x  = 0,y  = 0;
    double ax = 0,ay = 0;
    double cx = 0,cy = 0;

    /* marks rogue glyphs for debugging
       interpreter.newpath ();
       interpreter.moveto (0,0);
       interpreter.lineto (0,2048);
       interpreter.lineto (2048,2048);
       interpreter.lineto (2048,0);
       interpreter.closepath ();
    */

    int k = 0;
    Iterator indexIterator = indexList.iterator ();
    while (indexIterator.hasNext ())
      {
        int [] index = (int []) indexIterator.next ();
        int offset = k;
        for (int i = 0;i < index.length;i++)
          {
            int lastk = offset + index [i];
            int nextk = lastk + 1;
    
            if (k == lastk)
              {
                EnginePoint onlyPoint = points [k++];
                x = onlyPoint.x [0];
                y = onlyPoint.x [1];
                continue;
              }
    
            interpreter.newpath ();
    
            EnginePoint firstPoint = points [k++];
            ax = x = firstPoint.x [0];
            ay = y = firstPoint.x [1];
            boolean wasOnCurve = firstPoint.onCurve;
    
            if (!wasOnCurve)
              {
                // We want to begin with a point on the curve.
                EnginePoint lastPoint = points [lastk];
                ax = lastPoint.x [0];
                ay = lastPoint.x [1];

                if (!lastPoint.onCurve)
                  {
                    // the last point is also off curve; start
                    // from the midpoint between first and last
                    ax += (x - ax) / 2;
                    ay += (y - ay) / 2;
                  }

                // remember
                cx = x - ax;
                cy = y - ay;
              }
    
            interpreter.moveto (ax - xoff,ay);

            for (;k <= nextk;k++)
              {
                double dx,dy;
                boolean isOnCurve;

                if (k == nextk)
                  {
                    if (wasOnCurve)
                      continue;
                    dx = ax - x;
                    dy = ay - y;
                    isOnCurve = true; // we ensured this above
                  }
                else
                  {
                    EnginePoint p = points [k];
                    dx = p.x [0] - x;
                    dy = p.x [1] - y;
                    x += dx;
                    y += dy;
                    isOnCurve = p.onCurve;
                  }

                if (wasOnCurve)
                  {
                    if (isOnCurve)
                      interpreter.lineto (x - xoff,y);
                  }
                else
                  {
                    if (!isOnCurve)
                      {
                        dx /= 2;
                        dy /= 2; 
                      }

                    interpreter.rrcurveto (cx,cy,dx,dy);
                  }

                if (!isOnCurve)
                  {
                    cx = dx;
                    cy = dy;
                  }

                wasOnCurve = isOnCurve;
              }
            k--;
            interpreter.closepath ();
          }
      }
    
    interpreter.finish ();

    pointList.clear ();
    indexList.clear ();
    zpoints [1] = null;
  }

  EnginePoint [] getPointArray ()
  {
    return (EnginePoint []) pointList.toArray (new EnginePoint [pointList.size ()]);
  }

  public void interpret (byte [] instructions)
  {
    if (instructions == null || instructions.length == 0 ||
        !enableHinting.isSet () || inhibitGridFit)
      return;
    if (Options.tracing)
      System.out.println ("hinting !!!!");
    zpoints [1] = getPointArray ();
    initializeGraphicsState ();
    System.arraycopy (cvtTemplate,0,cvt,0,cvt.length);
    run (instructions);
  }

  void run (SFNTTable table)
  {
    if (table != null)
      run (table.toByteArray ());
    Assertions.expect (nstack,0);
  }

  void run (byte [] b)
  {
    interpret (new ByteCodeStream (b,0));
    nstack = 0; // tried to assert this, but some programs leave garbage
  }

  void call (int index)
  {
    interpret (functions [index].getCodeStream ());
  }

  final static double dotProduct (double [] x,double [] y)
  {
    return x [0] * y [0] + x [1] * y [1];
  }

  void calculateDot ()
  {
    dot = dotProduct (freedomVector,projectionVector);
  }

  void warn (ArrayIndexOutOfBoundsException aioobe)
  {
    Options.warn ("Index " + aioobe.getMessage () + " out of range");
  }

  void show (EnginePoint [] parr)
  {
    System.out.println ("showing " + parr);
    if (parr != null)
      for (int i = 0;i < parr.length;i++)
        System.out.println (parr [i]);
  }

  int opcount = 0;

  void interpret (ByteCodeStream codeStream)
  {
    ByteCodeStream toBeDissed = (ByteCodeStream) codeStream.clone ();
    try {
      int index,cvtIndex = 0;
      double value = 0;
      double op1,op2;
      Number number;
      EnginePoint point;
      EnginePoint p1,p2;

      int opcode;
      while ((opcode = codeStream.read ()) != -1)
        {
          if (Options.tracing)
            {
              System.out.println ("opcount = " + opcount++);
              show (zpoints [0]);
              show (zpoints [1]);
              System.out.println ("executing " + (opcode < mnemonic.length ? mnemonic [opcode] + " " : "") + Integer.toHexString (opcode));
            }
          if ((opcode & 0xf8) == 0x68) // (N)ROUND[ab]
            ;
          else if ((opcode & 0xf0) == 0xb0) // PUSH{B,W}[abc]
            {
              boolean words = (opcode & 8) == 8;
              opcode &= 7;
              while (opcode-- >= 0)
                push (words ? codeStream.readWord () : codeStream.read ());
            }
          else if ((opcode & 0xc0) == 0xc0) //M{D,I}RP[abcde]
            {
              // Two UNDOCUMENTED features taken from the freetype source;
              // a third one looks akin to the one in MSIRP, and like that
              // one I haven't implemented it.
              // The fourth one, to set rp2, is actually documented
              // somewhere (I think in the apple specs); I had that
              // before I looked at freetype.
              boolean indirect = (opcode & 0x20) == 0x20;
              if (indirect)
                cvtIndex = popInt ();
              index = popInt ();
              try {
                if (indirect)
                  // UNDOCUMENTED feature from freetype source:
                  // cvt [-1] is always 0
                  // Don't know why they have this only in MIRP.
                  value = cvtIndex == -1 ? 0 : cvt [cvtIndex];
                point = points [1] [index];
                // The freetype source has this special case
                // only for MIRP, i.e. indirect. They have an
                // XXX comment asking whether the twilight
                // zone should get a special treatment in MDRP.
                // We follow their example. The only case I found
                // where (point.twilight && !indirect) is in
                // 1589660285.pdf from Texterity. It didn't seem
                // to make a difference (at infinite point size)
                // which branch of the following conditional was
                // used in this case.
                boolean specialCase = point.twilight && indirect;
                if (specialCase)
                  point.setToReference ();
                else
                  {
                    double original =
                      point.originalCoordinate () -
                      points [0] [rp [0]].originalCoordinate ();
                    // UNDOCUMENTED feature in freetype source:
                    // says to only perform cut-in test when both
                    // points refer to the same zone -- not using that.
                    if (indirect && (opcode & 4) == 0)
                      {
                        if (autoFlip && original * value < 0)
                          value = -value;
                      }
                    else
                      value = original;
                  }

                point.moveRelative (value);
                if (specialCase)
                  point.create ();
                rp [1] = rp [0];
                rp [2] = index;
                if ((opcode & 0x10) == 0x10)
                  rp [0] = index;
              } catch (ArrayIndexOutOfBoundsException aioobe) {
                warn (aioobe);
              }
            }
          else
            switch (opcode)
              {
              case 0x00 : // SVTCA[0]
              case 0x01 : // SVTCA[1]
                setVectors (1 - opcode);
                break;
              case 0x02 : // SPVTCA[0]
              case 0x03 : // SPVTCA[1]
                setProjectionVector (3 - opcode);
                break;
              case 0x04 : // SPVTCA[0]
              case 0x05 : // SPVTCA[1]
                setFreedomVector (5 - opcode);
                break;
              case 0x06 : // SPVTL[0]
              case 0x07 : // SPVTL[1]
                setProjectionVector (getVectorFromLine (opcode,false));
                break;
              case 0x08 : // SFVTL[0]
              case 0x09 : // SFVTL[1]
                setFreedomVector (getVectorFromLine (opcode,false));
                break;
              case 0x0a : // SPVFS[]
                setProjectionVector (popVector ());
                break;
              case 0x0b : // SFVFS[]
                setFreedomVector (popVector ());
                break;
              case 0x0c :  // GPV[]
                pushVector (projectionVector);
                break;
              case 0x0d :  // GFV[]
                pushVector (freedomVector);
                break;
              case 0x0e : // SFVTPV[]
                setFreedomVector (projectionVector);
                break;
              case 0x0f : // ISECT[]
                double [] a1 = popPoint (0).x;
                double [] b1 = popPoint (0).x;
                double [] a2 = popPoint (1).x;
                double [] b2 = popPoint (1).x;
                point = popPoint (2);
                double [] line1 = vectorFromLine (a1,b1);
                double [] line2 = vectorFromLine (a2,b2);
                double det = line1 [0] * line2 [1] - line1 [1] * line2 [0];
                // if parallel, choose average of endpoints
                // (spec isn't entirely clear on what should happen)
                if (det == 0)
                  for (int i = 0;i < 2;i++)
                    point.x [i] = (a1 [i] + b1 [i] + a2 [i] + b2 [i]) / 4;
                else
                  {
                    double lambda = (line2 [1] * (a2 [0] - a1 [0]) -
                                     line2 [0] * (a2 [1] - a1 [1])) / det;
                    for (int i = 0;i < 2;i++)
                      point.x [i] = a1 [i] + lambda * line1 [i];
                  }
                point.touch (true);
                break;
              case 0x10 : // SRP0[]
              case 0x11 : // SRP1[]
              case 0x12 : // SRP2[]
                rp [opcode - 0x10] = popInt ();
                break;
              case 0x13 : // SZP0[]
              case 0x14 : // SZP1[]
              case 0x15 : // SZP2[]
                setZone (opcode - 0x13,popInt ());
                break;
              case 0x16 : // SZPS
                setZones (popInt ());
                break;
              case 0x17 : // SLOOP[]
                loop = popInt ();
                // the spec says 0 is an error, but I encountered
                // a font that uses 0, and freetype allows it.
                Assertions.expect (loop >= 0);
                break;
              case 0x18 : // RTG[]
                break;
              case 0x19 : // RTHG[]
                break;
              case 0x1a : // SMD[]
                popDouble ();
                break;
              case 0x1b : // ELSE[]
                codeStream.skipConditional ();
                break;
              case 0x1c : // JMPR[]
                codeStream.jumpRelative (popInt ());
                break;
              case 0x1d : // SCVTCI[]
              case 0x1e : // SSWCI[]
              case 0x1f : // SSW[]
                pop ();
                break;
              case 0x20 : // DUP[]
                number = pop ();
                push (number);
                push (number);
                break;
              case 0x21 : // POP[]
                pop ();
                break;
              case 0x22 : // CLEAR[]
                nstack = 0;
                break;
              case 0x23 : // SWAP[]
                Number arg1 = pop ();
                Number arg2 = pop ();
                push (arg1);
                push (arg2);
                break;
              case 0x24 : // DEPTH[]
                pushInt (nstack);
                break;
              case 0x25 : // CINDEX[]
                index = popInt ();
                push (stack [nstack - index]);
                break;
              case 0x26 : // MINDEX[]
                index = popInt ();
                number = stack [nstack - index];
                for (int i = nstack - index;i < nstack - 1;i++)
                  stack [i] = stack [i+1];
                stack [nstack - 1] = number;
                break;
              case 0x27 : // ALIGNPTS[]
                p1 = popPoint (0);
                p2 = popPoint (1);
                value = (p2.coordinate () - p1.coordinate ()) / 2;
                p1.shift (value,true);
                p2.shift (-value,true);
                break;
              case 0x29 : // UTP[]
                popPoint (0).touch (false);
                break;
              case 0x2a : // LOOPCALL[]
                index = popInt ();
                int count = popInt ();
                while (count-- > 0)
                  call (index);
                break;
              case 0x2b : // CALL[]
                call (popInt ());
                break;
              case 0x2c : // FDEF[]
                functions [popInt ()] = new Subroutine (codeStream);
                while (codeStream.skip () != 0x2d) // ENDF[]
                  ;
                break;
              case 0x2d : // ENDF[]
                return;
              case 0x2f : // MDAP[1]
              case 0x2e : // MDAP[0]
                points [0] [rp [0] = rp [1] = popInt ()].touch (true);
                break;
              case 0x30 : // IUP[0]
              case 0x31 : // IUP[1]
                if (inhibitGridFit)
                  break;
                EnginePoint [] points2 = points [2];
                int direction = ~opcode & 1;
                int offset = 0;
                Iterator indexIterator = indexList.iterator ();
                while (indexIterator.hasNext ())
                  {
                    int [] lasts = (int []) indexIterator.next ();
                    int first = 0;
                    for (int j = 0;j < lasts.length;j++) // loop over contours
                      {
                        int last = lasts [j];
                        int length = last - first + 1;
                        // contour from first to last
                        // find a touched point
                        int beg = 0;
                        while (beg < length && !points2 [offset + beg].touched [direction])
                          beg++;
                        if (beg != length) // found one?
                          {
                            int wrap = beg;
                            int end = beg;
                            do
                              {
                                while (!points2 [offset + (end = (end + 1) % length)].touched [direction])
                                  ;
                                // reference points
                                EnginePoint a = points2 [offset + beg];
                                EnginePoint b = points2 [offset + end];
                                if (a.x0 [direction] > b.x0 [direction])
                                  {
                                    EnginePoint swap = a;
                                    a = b;
                                    b = swap;
                                  }
                                // original coordinates of reference points
                                double oa = a.x0 [direction];
                                double ob = b.x0 [direction];
                                // moved coordinates of reference points
                                double xa = a.x  [direction];
                                double xb = b.x  [direction];
                                // oa == ob case doesn't use scale
                                double scale = oa == ob ? 0 :
                                  (xb - xa) / (ob - oa);
                                for (int p = beg + 1;(p %= length) != end;p++)
                                  {
                                    EnginePoint c = points2 [offset + p];
                                    double oc = c.x0 [direction];
                                    c.x [direction] =
                                      oc <= oa ?
                                      xa + oc - oa :
                                      oc >= ob ?
                                      xb + oc - ob :
                                      // oa == ob can't get here : oa < oc < ob
                                      xa + scale * (oc - oa);
                                  }
                                beg = end;
                              }
                            while (end != wrap);
                          }
                        first = last + 1;
                        offset += length;
                      }
                  }
                break;
              case 0x32 : // SHP[0]
              case 0x33 : // SHP[1]
                point = getShiftPoint (opcode);
                value = point.coordinate () - point.nonDualOriginalCoordinate ();
                while (loop-- > 0)
                  // UNDOCUMENTED feature from freetype source:
                  // SHP touches points
                  popPoint (2).shift (value,true);
                loop = 1;
                break;
              case 0x34 : // SHC[0]
              case 0x35 : // SHC[1]
                point = getShiftPoint (opcode);
                value = point.coordinate () - point.nonDualOriginalCoordinate ();
                int contour = popInt ();
                int pointOffset = 0;
                int firstPoint;
                int lastPoint;
                Iterator indicesIterator = indexList.iterator ();
                try {
                  for (;;)
                    {
                      // will have next if contour index is valid, else see below
                      int [] indices = (int []) indicesIterator.next ();
                      if (contour < indices.length)
                        {
                          firstPoint = pointOffset +
                            (contour == 0 ? 0 : indices [contour - 1]);
                          lastPoint = pointOffset + indices [contour];
                          break;
                        }
                      contour -= indices.length;
                      pointOffset += indices [indices.length - 1] + 1;
                    }
                } catch (NoSuchElementException nsee) {
                  // occurs in 0821352741.pdf
                  Options.warn ("invalid TrueType contour index");
                  break;
                }

                for (int i = firstPoint;i <= lastPoint;i++)
                  {
                    // don't know what contours would mean in twilight zone
                    Assertions.expect (points [2],zpoints [1]);
                    EnginePoint p = points [2] [i];
                    if (p != point)
                      // UNDOCUMENTED feature from freetype source:
                      // SHC doesn't touch points
                      p.shift (value,false);
                  }
                break;
              case 0x36 : // SHZ[0]
              case 0x37 : // SHZ[1]
                point = getShiftPoint (opcode);
                value = point.coordinate () - point.nonDualOriginalCoordinate ();
                EnginePoint [] zonePoints = zpoints [popInt ()];
                for (int i = 0;i < zonePoints.length;i++)
                  if (zonePoints [i] != point)
                    // UNDOCUMENTED feature from freetype source:
                    // SHC doesn't touch points
                    zonePoints [i].shift (value,false);
                break;
              case 0x38 : // SHPIX
                value = popDouble ();
                while (loop-- > 0)
                  popPoint (2).shiftParallel (value,true);
                loop = 1;
                break;
              case 0x39 : // IP[]
                double o1,x1;
                double o2,x2;
                try {
                  p1 = points [0] [rp [1]];
                  p2 = points [1] [rp [2]];
                  o1 = p1.originalCoordinate ();
                  o2 = p2.originalCoordinate ();
                  x1 = p1.coordinate ();
                  x2 = p2.coordinate ();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                  // This is what freetype does. With our handling
                  // of o1 == o2 it means doing nothing, except that
                  // the points get touched. Also we don't need an
                  // extra loop to pop the points off the stack.
                  o1 = x1 = o2 = x2 = 0;
                  warn (aioobe);
                }
                // freetype treats IP like IUP in that it shifts points
                // outside of the reference interval by a constant. The
                // spec says to do this for IUP, but not for IP.
                // Here we currently follow the spec and interpolate
                // everything linearly.
                // Another, related problem is that the spec says that
                // o1 == o2 is illegal. However, the 'p' in Garamond Italic
                // has such a case. Here we can't interpolate linearly.
                // Freetype arbitrarily chooses one of the points for its
                // constant interpolation, but it's more in keeping with
                // our approach to shift by the average shift of the
                // reference points.
                if (o1 == o2)
                  {
                    double del = ((x1 - o1) + (x2 - o2)) / 2;
                    while (loop-- > 0)
                      popPoint (2).shift (del,true);
                  }
                else
                  {
                    double off = o1 * x2 - o2 * x1;
                    double dif = x1 - x2;
                    double den = 1 / (o1 - o2);
                    while (loop-- > 0)
                      {
                        point = popPoint (2);
                        point.move ((off + dif * point.originalCoordinate ()) * den);
                      }
                  }
                loop = 1;
                break;
              case 0x3a : // MSIRP[0]
              case 0x3b : // MSIRP[1]
                double distance = popDouble ();
                index = popInt ();
                point = points [1] [index];
                if (point.twilight)
                  point.setToReference ();
                point.moveRelative (distance);
                if (point.twilight)
                  point.create ();
                rp [1] = rp [0];
                rp [2] = index;
                if ((opcode & 1) == 1)
                  rp [0] = index;
                break;
              case 0x3c : // ALIGNRP[]
                while (loop-- > 0)
                  try {
                    popPoint (1).moveRelative (0);
                  } catch (ArrayIndexOutOfBoundsException aioobe) {
                    warn (aioobe);
                  }
                loop = 1;
                break;
              case 0x3d : // RTDG[]
                break;
              case 0x3e : // MIAP[0]
              case 0x3f : // MIAP[1]
                value = cvt [popInt ()];
                index = popInt ();
                point = points [0] [index];
                /*
                  The control value cut-in is effectively zero,
                  so if we're told to look at it, the test always fails.
                  The spec is very unclear about what happens in MIAP when
                  the control value isn't used. It speaks of the point's
                  "original position". Usually this means the position
                  in the original unhinted outline. This would also seem
                  consistent with how MIRP works. But for several reasons
                  it seems to me that they mean the position prior to this
                  MIAP instruction:
                  a) It says "the original position will be rounded".
                  In the other interpretation, one would expect
                  something like "the original position is restored
                  and then rounded."
                  b) The examples in ttinst2.doc seem to support this
                  interpretation -- in the other interpretation, they
                  would only make sense if it were implied that this
                  point hasn't beed moved before -- it is probably this
                  hidden assumption that kept them from clarifying this
                  point.
                  c) MIAP would be one of few instructions that don't
                  use the dual projection vector when measuring original
                  coordinates. The others are SHP and SHC, and there it
                  makes sense not to do so.
                  d) Quite generally, it seems daft to move the point
                  back to its original position if it has already
                  intentionally been moved somewhere else.
                  e) After elaborating the previous four reasons, I found
                  a final, compelling one: this is how freetype does it.
       
                  For the other interpretation, taking "original" literally,
                  the following code would need to be used :
                  point.move (((opcode & 1) == 0 ?
                  value : point.nonDualOriginalCoordinate ()));

                  It's also not clear whether MIAP uses autoFlip.
                  Some parts of the spec say so, others don't.
                  It doesn't seem to make too much sense, so let's not use it.
                  Again, freetype agrees, so I guessed right on both accounts.

                  The following special case for the twilight zone
                  is taken from freetype, but the factor 1/dot seems
                  to be missing there.
                */
      
                if (point.twilight)
                  {
                    double lambda = value / dot;
                    for (int i = 0;i < 2;i++)
                      point.x [i] = lambda * freedomVector [i];
                    point.create ();
                  }

                if ((opcode & 1) == 0)
                  point.move (value);

                rp [0] = rp [1] = index;
                break;
              case 0x40 : // NPUSHB[]
                int nbyte = codeStream.read ();
                while (nbyte-- > 0)
                  push (codeStream.read ());
                break;
              case 0x41 : // NPUSHW[]
                int nword = codeStream.read ();
                while (nword-- > 0)
                  push (codeStream.readWord ());
                break;
              case 0x42 : // WS[]
                number = pop ();
                storage [popInt ()] = number;
                break;
              case 0x43 : // RS[]
                push (storage [popInt ()]);
                break;
              case 0x44 : // WCVTP[]
                value = popDouble ();
                cvt [popInt ()] = value;
                break;
              case 0x45 : // RCVT[]
		// font F4 in 0870817035_16.pdf uses invalid indices
		// freetype pushes a zero in this case
		// (unless "pedantic hinting" is turned on)
                pushDouble (cvt [popInt ()]);
                break;
              case 0x46 : // GC [0]
                pushDouble (popPoint (2).coordinate ());
                break;
              case 0x47 : // GC [1]
                pushDouble (popPoint (2).originalCoordinate ());
                break;
              case 0x48 : // SCFS[]
                value = popDouble ();
                point = popPoint (2);
                point.move (value);
                if (point.twilight)
                  point.create ();
                break;
              case 0x49 : // MD[0]
              case 0x4a : // MD[1]
                p2 = popPoint (1);
                p1 = popPoint (0);
                // The freetype source says that this flag is reversed
                // with respect to the spec. I didn't believe this at
                // first, but Garamond,BoldItalic in 1589660447.pdf
                // from Texterity contains an MD that would otherwise
                // use an increate twilight point, so we reverse it.
                pushDouble
                  ((opcode & 1) == 1 ?
                   p1.coordinate () - p2.coordinate () :
                   p1.originalCoordinate () - p2.originalCoordinate ());
                break;
              case 0x4b : // MPPEM[]
              case 0x4c : // MPS[]
                push (0x1000000); // huge value
                break;
              case 0x4d : // FLIPON[]
                autoFlip = true;
                break;
              case 0x4e : // FLIPOFF[]
                autoFlip = false;
                break;
              case 0x4f : // DEBUG[]
                pop ();
                break;
              case 0x50 : // LT[]
                pushBoolean (compare () < 0);
                break;
              case 0x51 : // LTEQ[]
                pushBoolean (compare () <= 0);
                break;
              case 0x52 : // GT[]
                pushBoolean (compare () > 0);
                break;
              case 0x53 : // GTEQ[]
                pushBoolean (compare () >= 0);
                break;
              case 0x54 : // EQ[]
                pushBoolean (compare () == 0);
                break;
              case 0x55 : // NEQ[]
                pushBoolean (compare () != 0);
                break;
              case 0x56 : // ODD
                pushBoolean (!even ());
                break;
              case 0x57 : // EVEN
                pushBoolean (even ());
                break;
              case 0x58 : // IF[]
                if (!popBoolean ())
                  codeStream.skipConditional ();
                break;
              case 0x59 : // EIF[]
                break;
              case 0x5a : // AND[]
                pushBoolean (popBoolean () & popBoolean ());
                break;
              case 0x5b : // OR[]
                pushBoolean (popBoolean () | popBoolean ());
                break;
              case 0x5c : // NOT[]
                pushBoolean (!popBoolean ());
                break;
                // 0x5d is before 0x71
              case 0x5e : // SDB[]
              case 0x5f : // SDS[]
                popInt ();
                break;
              case 0x60 : // ADD[]
              case 0x61 : // SUB[]
                binaryOperation (opcode);
                break;
              case 0x62 : // DIV[]
              case 0x63 : // MUL[]
                Number n2 = pop ();
                Number n1 = pop ();
                op2 = n2.doubleValue ();
                op1 = n1.doubleValue ();
                double res = opcode == 0x62 ? op1 / op2 : op1 * op2;
                push (unscathed (n1) && unscathed (n2) ?
                      (Number) new Argument ((int) (res * 64)) :
                      (Number) new Double (res));
                break;
              case 0x64 : // ABS[]
              case 0x65 : // NEG[]
                number = pop ();
                if (number instanceof Double)
                  pushDouble (unaryOperation (opcode,number.doubleValue ()));
                else
                  {
                    int result = (int) unaryOperation (opcode,number.intValue ());
                    if (number instanceof Integer)
                      pushInt (result);
                    else if (number instanceof Argument)
                      push (result);
                    else if (number instanceof VectorComponent)
                      push (new VectorComponent (result));
                    else
                      throw new InternalError ();
                  }
                break;
              case 0x66 : // FLOOR[]
              case 0x67 : // CEILING[]
                Number n = pop ();
                if (n instanceof Integer)
                  push (n);
                else if (n instanceof Argument)
                  push (new Integer ((int) (opcode == 0x66 ?
                                            Math.floor (n.doubleValue ()) :
                                            Math.ceil (n.doubleValue ()))));
                else if (n instanceof Double)
                  // there's no special meaning to integer values --
                  // we could view this as rendering at the resolution
                  // of floating point precision :-)
                  push (n);
                else
                  throw new NotImplementedException ("floor/ceiling for " + n.getClass ());
                break;
              case 0x70 : // WCVTF[]
                // take a value in FUnits, convert to pixels and store it in
                // the cvt; in our case, conversion just means int -> double :-)
                int pixels = popInt ();
                cvt [popInt ()] = pixels;
                break;
              case 0x5d : // DELTAP1[]
              case 0x71 : // DELTAP2[]
              case 0x72 : // DELTAP3[]
              case 0x73 : // DELTAC1[]
              case 0x74 : // DELTAC2[]
              case 0x75 : // DELTAC3[]
                for (int i = popInt ();i > 0;i--)
                  {
                    popInt ();
                    popInt ();
                  }
                break;
              case 0x76 : // SROUND[]
              case 0x77 : // S45ROUND[]
                popInt ();
                break;
              case 0x78 : // JROT[]
              case 0x79 : // JROF[]
                boolean jump = popBoolean () == (opcode == 0x78);
                index = popInt ();
                if (jump)
                  codeStream.jumpRelative (index);
                break;
              case 0x7a : // ROFF[]
              case 0x7c : // RUTG[]
              case 0x7d : // RDTG[]
                break;
              case 0x7e : // SANGW[]
              case 0x7f : // AA[]
                popInt ();
                break;
              case 0x80 : // FLIPPT[]
                while (loop-- > 0)
                  popPoint (0).flip ();
                loop = 1;
                break;
              case 0x81 : // FLIPRGON[]
              case 0x82 : // FLIPRGOFF[]
                int hi = popInt ();
                int lo = popInt ();
                for (int i = lo;i <= hi;i++)
                  points [0] [i].onCurve = opcode == 0x81;
                break;
              case 0x85 : // SCANCTRL[]
                pop ();
                break;
              case 0x86 : // SDPVTL[0]
              case 0x87 : // SDPVTL[1]
                setProjectionVector (getVectorFromLine (opcode,true));
                break;
              case 0x88 : // GETINFO
                popInt ();
                pushInt (0); // no version, no rotation, no stretching 
                break;
              case 0x89 : // IDEF[]
                instructions [popInt ()] = new Subroutine (codeStream);
                while (codeStream.skip () != 0x2d) // ENDF[]
                  ;
                break;
              case 0x8a : // ROLL[]
                Number a = pop ();
                Number b = pop ();
                Number c = pop ();
                push (b);
                push (a);
                push (c);
                break;
              case 0x8b : // MAX[]
              case 0x8c : // MIN[]
                binaryOperation (opcode);
                break;
              case 0x8d : // SCANTYPE[]
                pop ();
                break;
              case 0x8e : // INSTCTRL
                int selector = popInt ();
                int flags = popInt ();
                if ((selector & 1) == 1)
                  inhibitGridFit = (flags & 1) == 1;
                // ignore flag 2, since we ignore parameters like cut-in anyway
                break;
              default : 
                if (instructions [opcode] != null)
                  interpret (instructions [opcode].getCodeStream ());
                else
                  throw new NotImplementedException
                    ("byte code " + Integer.toHexString (opcode) + " " + codeStream.positionString ());
              }
        }
    } catch (Exception e) {
      e.printStackTrace ();
      new ByteCodeDisassembler (log).disassemble (toBeDissed);
      Assertions.expect (false);
    }
  }
}
