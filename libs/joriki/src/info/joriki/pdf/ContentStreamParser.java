/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.EmptyStackException;
import java.util.Map;
import java.util.List;
import java.util.Stack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import info.joriki.io.InputStreamConcatenation;
import info.joriki.io.IncorrectChecksumException;
import info.joriki.util.Count;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;
import info.joriki.adobe.FillRules;

public class ContentStreamParser implements PaintTypes, FillRules, DeviceColorSpaces, ObjectTypes, ContentStreamTypes, PDFOptions
{
  // Operator categories (Table 4.1)
  final static int GENERAL_GRAPHICS_STATE = 0x0001;
  final static int SPECIAL_GRAPHICS_STATE = 0x0002;
  final static int PATH_CONSTRUCTION      = 0x0004;
  final static int PATH_PAINTING          = 0x0008;
  final static int CLIPPING_PATHS         = 0x0010;
  final static int TEXT_OBJECTS           = 0x0020;
  final static int TEXT_STATE             = 0x0040;
  final static int TEXT_POSITIONING       = 0x0080;
  final static int TEXT_SHOWING           = 0x0100;
  final static int TYPE_3_FONTS           = 0x0200;
  final static int COLOR                  = 0x0400;
  final static int SHADING_PATTERNS       = 0x0800;
  final static int INLINE_IMAGES          = 0x1000;
  final static int XOBJECTS               = 0x2000;
  final static int MARKED_CONTENT         = 0x4000;
  final static int COMPATIBILITY          = 0x8000;

  // States (Figure 4.1)
  final static int NONE = -1;
  final static int PAGE = 0;
  final static int CLIP = 1;
  final static int PATH = 2;
  final static int TEXT = 3;
  final static int BBOX = 4;

  final static int [] validOperators = {
    GENERAL_GRAPHICS_STATE |
    SPECIAL_GRAPHICS_STATE |
    COLOR | TEXT_STATE | MARKED_CONTENT |
    SHADING_PATTERNS | XOBJECTS | INLINE_IMAGES | COMPATIBILITY,
    PATH_PAINTING | COMPATIBILITY,
    PATH_PAINTING | PATH_CONSTRUCTION | COMPATIBILITY,
    GENERAL_GRAPHICS_STATE | COLOR |
    TEXT_STATE | TEXT_SHOWING | TEXT_POSITIONING |
    MARKED_CONTENT | COMPATIBILITY,
    0
  };

  Map<String, Operator> operators = new HashMap<String, Operator> ();

  abstract class Operator implements CommandHandler
  {
    int category;
    int nargs;
    int initial;
    int terminal;

    Operator (String command,int nargs,int category)
    {
      this (command,nargs,category,NONE,NONE);
    }

    Operator (String command,int nargs,int category,int initial,int terminal)
    {
      this.category = category;
      this.nargs = nargs;
      this.initial = initial;
      this.terminal = terminal;
      operators.put (command,this);
    }

    public void handle (String command) throws IOException
    {
      if (nargs != -1 && args.size () != nargs)
        throw new IllegalArgumentException (command + " takes " + nargs + " arguments");
      
      if (validating && (validOperators [state.state] & category) == 0)
      {
        if (state.state == PATH &&
            (category == GENERAL_GRAPHICS_STATE ||
             category == SPECIAL_GRAPHICS_STATE ||
             category == COLOR))
          // 'w' in Antrag_59.pdf
          // 'q' in UMNEastBank2002.pdf
          // 'G' in IEPGoals.fp5.pdf
          Options.warn ("invalid graphics state operator in path object");
        else if (state.state == PATH && command.equals ("BT"))
        {
          // "BT" after "re" in annotation on page 498 of 0060084944.pdf
          dropPath ();
          state.state = TEXT;
        }
        else if (state.state == PAGE && category == PATH_PAINTING)
          // 'n' in 0768655544_51.pdf
          // 'B' in All.pdf
        {
          Options.warn ("empty path");
          return;
        }
        else if (state.state == PAGE && category == CLIPPING_PATHS)
          // 'W' in giformat.pdf
          // We follow ghostscript here in that we allow the
          // clip rule to be set via W or W* at any time; it
          // stays in effect up to the next path painting operator.
          // Note also that this bypasses the state change below.
          Options.warn ("invalid clipping operator");
        else if (state.state == TEXT && command.equals ("cm"))
          // It seems that Adobe Reader accepts cm anywhere within text objects;
          // it takes immediate effect and remains in effect beyond the text
          // object. The content stream handler should be able to handle this
          // correctly; PDFtoCSV does and PDFtoSVG probably does but hasn't been
          // properly tested -- see PDFtoSVG.concatenateMatrix. So far we've
          // only seen cm directly after BT, in premierguitar200712_124.pdf.
          Options.warn ("current transform matrix modified within text object");
        else if (state.state == initial)
          state.state = terminal;
        else
          throw new Error ("invalid operator " + command + " in content stream");
      }
      
      execute ();
    }

    abstract void execute () throws IOException;
  }

  boolean wrapping;
  boolean validating = true;
  ContentStreamHandler handler;
  ResourceResolver resourceResolver;

  static class State
  {
    int state;
    InputStream in;
    int inlineImageCount;
    PDFObjectReader reader;
    int clipRule = NOFILL; // remembers clipping operators
    int compatibilityNest; // number of pending BX operators
    Stack<Object> nestingStack = new Stack<Object> (); // to check nesting
    List<Count> commandPatternCounts = new ArrayList<Count> ();
  }

  String [] commandPattern;
  
  String command;
  Stack<Object> args = new Stack<Object> ();
  State state;
  Stack<State> stateStack = new Stack<State> ();

  private final static Object textNest = new Object ();
  private final static Object markNest = new Object ();
  
  public ContentStreamParser (ContentStreamHandler handler,
			      ResourceResolver resourceResolver)
  {
    this (handler,resourceResolver,false);
  }

  public ContentStreamParser (ContentStreamHandler handler,
			      ResourceResolver resourceResolver,
                              boolean wrapping)
  {
    this.handler = handler;
    this.resourceResolver = resourceResolver;
    this.wrapping = wrapping;

    createOperators ();
  }

  void createOperators ()
  {
    new Operator ("b",0,PATH_PAINTING) { public void execute () {
      usePath (true,true,NONZERO);
    }};
    new Operator ("B",0,PATH_PAINTING) { public void execute () {
      usePath (false,true,NONZERO);
    }};
    new Operator ("b*",0,PATH_PAINTING) { public void execute () {
      usePath (true,true,EVENODD);
    }};
    new Operator ("B*",0,PATH_PAINTING) { public void execute () {
      usePath (false,true,EVENODD);
    }};
    new Operator ("BDC",2,MARKED_CONTENT) { public void execute () {
      handler.beginMarkedContent (getContentMark ());
      state.nestingStack.push (markNest);
    }};
    new Operator ("BI",0,INLINE_IMAGES) { public void execute () throws IOException {
      // begin inline image data
      PDFDictionary inlineDictionary = state.reader.readInlineImageDictionary ();
      // they made "I" an abbreviation for both "Indexed" and "Interpolate"
      // so we must replace any key "Indexed" by "Interpolate".
      // I think "unresolved" is unnecessary here, but I don't
      // dare to change it :-)
      PDFObject value = inlineDictionary.getUnresolved ("Indexed");
      if (value != null)
      {
        inlineDictionary.remove ("Indexed");
        inlineDictionary.put ("Interpolate",value);
      }

      // if the first filter is FlateDecode, we need to communicate
      // with it for two reasons: a) we need to tell it to go a byte
      // at a time since we don't know where the stream ends, and
      // b) some people forgot to write the checksum after the ZLIB
      // stream, so we need to read it by hand.
      InputStream filteredInputStream =
        inlineDictionary.getFilteredInputStream (state.in,true);

      inlineImageStream = filteredInputStream != state.in ? filteredInputStream : null;

      handler.drawImage (new PDFImage
          (filteredInputStream,--state.inlineImageCount,inlineDictionary,resourceResolver.map
          (resourceResolver.resolveColorSpace (inlineDictionary.get ("ColorSpace")))));

      exhaustInlineImageStream ();

      inlineDictionary.checkUnused ("4.40"); // XObject images are checked in PDFImage constructor

      // We used to read "EI" using the object reader, but
      // Sample-Statement.pdf has a spurious ">\r" before "EI",
      // which would be treated as an illegal token.
      // Thus we now read up to and including "EI" by hand.
      // This also prevents any bytes read ahead by the
      // stream tokenizer to be copied to the output if
      // we are rewriting the stream.
      // The downside is that we'll accept "EI" even if it's
      // not properly delimited e.g. from the next operator,
      // which Adobe Reader and AGS don't.
      // Some inline images on page 1 of vista_wp2007.pdf contain incorrect zlib checksums.
      // Since we can't tell these from missing checksums, we need to check them for "EI".
      // Since they may contain single 'E's, we need to check for the entire "EI" sequence.
      // If an incorrect checksum should contain "EI", there's not much we can do about it.
    outer:
      for (;;)
      {
        int b = state.reader.tok.nextRawByte ();
        while (b == 'E')
          if ((b = state.reader.tok.nextRawByte ()) == 'I')
            break outer;
        if (b < 0)
          throw new Error ("missing end of inline image");
      }
    }};
    new Operator ("BMC",1,MARKED_CONTENT) { public void execute () {
      handler.beginMarkedContent (new ContentMark (getName ()));
      state.nestingStack.push (markNest);
    }};
    new Operator ("BT",0,TEXT_OBJECTS,PAGE,TEXT) { public void execute () {
      handler.beginTextObject ();
      state.nestingStack.push (textNest);
    }};
    new Operator ("BX",0,COMPATIBILITY) { public void execute () {
      state.compatibilityNest++;
    }};
    new Operator ("c",6,PATH_CONSTRUCTION) { public void execute () {
      double [] points = getDoubleArray ();
      handler.curveTo (points);
      currentX = points [4];
      currentY = points [5];
    }};
    new Operator ("cm",6,SPECIAL_GRAPHICS_STATE) { public void execute () {
      handler.concatenateMatrix (getDoubleArray ());
    }};
    new Operator ("cs",1,COLOR) { public void execute () {
      setColorSpace (NONSTROKING);
    }};
    new Operator ("CS",1,COLOR) { public void execute () {
      setColorSpace (STROKING);
    }};
    new Operator ("d",2,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setDash (getDoubleArray (0),getDouble (1));
    }};
    new Operator ("d0",2,TYPE_3_FONTS,BBOX,PAGE) { public void execute () {
      double wx = getDouble (0);
      double wy = getDouble (1);
      Assertions.expect (wy,0);
      handler.setGlyphWidth (wx,wy);
    }};
    new Operator ("d1",6,TYPE_3_FONTS,BBOX,PAGE) { public void execute () {
      double [] metrics = getDoubleArray ();
      Assertions.expect (metrics [1],0);
      handler.setGlyphMetrics (metrics);
    }};
    new Operator ("Do",1,XOBJECTS) { public void execute () throws IOException {
      PDFObject xobject = getResource ("XObject",0);
      if (xobject == null)
        return;
      PDFStream stream = (PDFStream) xobject;
      Assertions.expect (stream.isOptionallyOfType ("XObject"));
      if (stream.isOfSubtype ("Image"))
        handler.drawImage (new PDFImage (stream,resourceResolver));
      else if (stream.isOfSubtype ("Form"))
      {
        Assertions.expect (stream.getInt ("FormType",1),1);
        handler.drawForm (stream);
      }
      else
        throw new NotImplementedException ("XObject subtype " + stream.get ("Subtype"));
    }};
    new Operator ("DP",2,MARKED_CONTENT) { public void execute () {
      handler.markContent (getContentMark());
    }};
    new Operator ("EMC",0,MARKED_CONTENT) { public void execute () {
      handler.endMarkedContent ();
      nestCheck (markNest);
    }};
    new Operator ("ET",0,TEXT_OBJECTS,TEXT,PAGE) { public void execute () {
      handler.endTextObject ();
      nestCheck (textNest);
    }};
    new Operator ("EX",0,COMPATIBILITY) { public void execute () {
      if (--state.compatibilityNest < 0)
        throw new Error ("unbalanced EX operator");
    }};
    new Operator ("f",0,PATH_PAINTING) { public void execute () { 
      usePath (false,false,NONZERO);
    }};
    new Operator ("F",0,PATH_PAINTING) { public void execute () {
      usePath (false,false,NONZERO);
    }};
    new Operator ("f*",0,PATH_PAINTING) { public void execute () {
      usePath (false,false,EVENODD);
    }};
    new Operator ("g",1,COLOR) { public void execute () {
      setDeviceColor (NONSTROKING,GRAY);
    }};
    new Operator ("G",1,COLOR) { public void execute () { 
      setDeviceColor (STROKING,GRAY);
    }};
    new Operator ("gs",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setGraphicsState ((PDFDictionary) getResource ("ExtGState",0));
    }};
    new Operator ("h",0,PATH_CONSTRUCTION) { public void execute () {
      currentX = firstX;
      currentY = firstY;
      handler.closePath ();
    }};
    new Operator ("i",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setFlatness (getDouble (0));
    }};
    new Operator ("j",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setLineJoin (getInt (0));
    }};
    new Operator ("J",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setLineCap (getInt (0));
    }};
    new Operator ("k",4,COLOR) { public void execute () {
      setDeviceColor (NONSTROKING,CMYK);
    }};
    new Operator ("K",4,COLOR) { public void execute () {
      setDeviceColor (STROKING,CMYK);
    }};
    new Operator ("l",2,PATH_CONSTRUCTION) { public void execute () {
      currentX = getDouble (0);
      currentY = getDouble (1);
      handler.lineTo (currentX,currentY);
    }};
    new Operator ("m",2,PATH_CONSTRUCTION,PAGE,PATH) { public void execute () {
      firstX = currentX = getDouble (0);
      firstY = currentY = getDouble (1);
      handler.moveTo (firstX,firstY);
    }};
    new Operator ("M",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setMiterLimit (getDouble (0));
    }};
    new Operator ("MP",1,MARKED_CONTENT) { public void execute () {
      handler.markContent (new ContentMark (getName ()));
    }};
    new Operator ("n",0,PATH_PAINTING) { public void execute () {
      usePath (false,false,NOFILL);
    }};
    new Operator ("q",0,SPECIAL_GRAPHICS_STATE) { public void execute () {
      handler.gsave ();
    }};
    new Operator ("Q",0,SPECIAL_GRAPHICS_STATE) { public void execute () {
      handler.grestore ();
    }};
    new Operator ("re",4,PATH_CONSTRUCTION,PAGE,PATH) { public void execute () {
      double x = getDouble (0);
      double y = getDouble (1);
      double width = getDouble (2);
      double height = getDouble (3);
      handler.drawRectangle (x,y,width,height);
      firstX = currentX = x;
      firstY = currentY = y;
    }};
    new Operator ("rg",3,COLOR) { public void execute () {
      setDeviceColor (NONSTROKING,RGB);
    }};
    new Operator ("RG",3,COLOR) { public void execute () {
      setDeviceColor (STROKING,RGB);
    }};
    new Operator ("ri",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setRenderingIntent (getName ());
    }};
    new Operator ("s",0,PATH_PAINTING) { public void execute () {
      usePath (true,true,NOFILL);
    }};
    new Operator ("S",0,PATH_PAINTING) { public void execute () {
      usePath (false,true,NOFILL);
    }};
    // we'd have to remember the current color space to
    // check the number of arguments; if it turns out we
    // have to do that anyway, change this; for now,
    // bypass the check
    new Operator ("sc",-1,COLOR) { public void execute () {
      handler.setColor (NONSTROKING,getFloatArray ());
    }};
    new Operator ("SC",-1,COLOR) { public void execute () {
      handler.setColor (STROKING,getFloatArray ());
    }};
    new Operator ("scn",-1,COLOR) { public void execute () {
      setSpecialColor (NONSTROKING);
    }};
    new Operator ("SCN",-1,COLOR) { public void execute () {
      setSpecialColor (STROKING);
    }};
    new Operator ("sh",1,SHADING_PATTERNS) { public void execute () {
      PDFShading shading = (PDFShading) resourceResolver.getCachedResource (SHADING,getName ());
      shading.baseColorSpace.map (resourceResolver);
      handler.shade (shading);
    }};
    new Operator ("Tc",1,TEXT_STATE) { public void execute () {
      handler.setCharacterSpacing (getDouble (0));
    }};
    new Operator ("Td",2,TEXT_POSITIONING) { public void execute () {
      double dx = getDouble (0);
      double dy = getDouble (1);
      handler.moveToNextLine (dx,dy);
    }};
    new Operator ("TD",2,TEXT_POSITIONING) { public void execute () {
      double tx = getDouble (0);
      double ty = getDouble (1);
      handler.moveToNextLineAndSetTextLeading (tx,ty);
    }};
    new Operator ("Tf",2,TEXT_STATE) { public void execute () {
      handler.setTextFont ((PDFFont) resourceResolver.getCachedResource
			   (FONT,getName ()),
			   getDouble (1));
    }};
    new Operator ("Tj",1,TEXT_SHOWING) { public void execute () {
      handler.show (getByteArray (0));
    }};
    new Operator ("TJ",1,TEXT_SHOWING) { public void execute () {
      handler.show (getList (0));
    }};
    new Operator ("TL",1,TEXT_STATE) { public void execute () {
      handler.setTextLeading (getDouble (0));
    }};
    new Operator ("Tm",6,TEXT_POSITIONING) { public void execute () {
      handler.setTextMatrix (getDoubleArray ());
    }};
    new Operator ("Tr",1,TEXT_STATE) { public void execute () {
      handler.setTextRenderingMode (new TextRenderingMode (getInt (0)));
    }};
    new Operator ("Ts",1,TEXT_STATE) { public void execute () {
      handler.setTextRise (getDouble (0));
    }};
    new Operator ("Tw",1,TEXT_STATE) { public void execute () {
      handler.setWordSpacing (getDouble (0));
    }};
    new Operator ("Tz",1,TEXT_STATE) { public void execute () {
      handler.setHorizontalScaling (getDouble (0) / 100);
    }};
    new Operator ("T*",0,TEXT_POSITIONING) { public void execute () {
      handler.moveToNextLine ();
    }};
    new Operator ("v",4,PATH_CONSTRUCTION) { public void execute () {
      double [] points = getDoubleArray ();
      handler.curveTo (new double [] {
	currentX,currentY,points [0],points [1],points [2],points [3]
      });
      currentX = points [2];
      currentY = points [3];
    }};
    new Operator ("y",4,PATH_CONSTRUCTION) { public void execute () {
      double [] points = getDoubleArray ();
      handler.curveTo (new double [] {
	points [0],points [1],points [2],points [3],points [2],points [3]
      });
      currentX = points [2];
      currentY = points [3];
    }};
    new Operator ("w",1,GENERAL_GRAPHICS_STATE) { public void execute () {
      handler.setLineWidth (getDouble (0));
    }};
    new Operator ("W",0,CLIPPING_PATHS,PATH,CLIP) { public void execute () {
      state.clipRule = NONZERO;
    }};
    new Operator ("W*",0,CLIPPING_PATHS,PATH,CLIP) { public void execute () {
      state.clipRule = EVENODD;
    }};
    new Operator ("'",1,TEXT_SHOWING) { public void execute () {
      handler.moveToNextLineAndShow (getByteArray (0));
    }};
    new Operator ("\"",3,TEXT_SHOWING) { public void execute () {
      handler.setWordSpacing (getDouble (0));
      handler.setCharacterSpacing (getDouble (1));
      handler.moveToNextLineAndShow (getByteArray (2));
    }};
  }

  final PDFObject getResource (String resourceType,int arg)
  {
    return getResource (resourceType,(PDFObject) args.get (arg));
  }

  final PDFObject getResource (String resourceType,PDFObject key)
  {
    PDFObject resource = resourceResolver.resolveResource (resourceType,key);
    if (resource instanceof PDFName) {
      Options.warn ("key " + key + " is not defined for resource type " + resourceType);
      return null;
    }
    return resource;
  }

  final PDFName getName ()
  {
    return (PDFName) args.firstElement ();
  }

  final ContentMark getContentMark()
  {
    return new ContentMark (getName (),((PDFDictionary) getResource ("Properties",1)));
  }
  
  final byte [] getByteArray (int arg)
  {
    Object argument = args.get (arg);
    return wrapping ? ((PDFString) argument).getBytes () : (byte []) argument;
  }

  final List getList (int arg)
  {
    Object argument = args.get (arg);
    return wrapping ? ((PDFArray) argument).contents : (List) argument;
  }

  final PDFArray getArray (int arg)
  {
    Object argument = args.get (arg);
    return wrapping ? (PDFArray) argument : new PDFArray ((List<PDFObject>) argument);
  }

  final double [] getDoubleArray (int arg)
  {
    return getArray (arg).toDoubleArray ();
  }

  final double getDouble (int arg)
  {
    return ((PDFNumber) args.get (arg)).doubleValue ();
  }

  final float getFloat (int arg)
  {
    return (float) getDouble (arg);
  }

  final int getInt (int arg)
  {
    return ((PDFInteger) args.get (arg)).val;
  }

  double [] getDoubleArray ()
  {
    double [] arr = new double [args.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = getDouble (i);
    return arr;
  }

  float [] getFloatArray ()
  {
    float [] arr = new float [args.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = getFloat (i);
    return arr;
  }

  int [] getIntArray ()
  {
    int [] arr = new int [args.size ()];
    for (int i = 0;i < arr.length;i++)
      arr [i] = getInt (i);
    return arr;
  }

  void setDeviceColor (int paintType,int which)
  {
    setColorSpaceAndColor
      (paintType,deviceColorSpaces [which],getFloatArray ());
  }

  void setSpecialColor (int paintType)
  {
    if (args.peek () instanceof PDFName)
      handler.setPattern (paintType,
                          (PDFPattern) resourceResolver.getCachedResource
                          (PATTERN,(PDFName) args.pop ()));
    handler.setColor (paintType,getFloatArray ());
  }

  void setColorSpace (int paintType)
  {
    PDFColorSpace colorSpace =
      resourceResolver.resolveColorSpace (getName ());
    if (colorSpace instanceof PatternColorSpace)
      {
        handler.setColorSpace (paintType,colorSpace);
        handler.setPattern (paintType,null);
      }
    else
      setColorSpaceAndColor (paintType,colorSpace,colorSpace.defaultColor);
  }

  void setColorSpaceAndColor
    (int paintType,PDFColorSpace colorSpace,float [] color)
  {
    handler.setColorSpace (paintType,colorSpace);
    handler.setColor (paintType,color);
  }

  double firstX;
  double firstY;
  double currentX;
  double currentY;

  void usePath (boolean close,boolean stroke,int fillRule)
  {
    if (close)
      handler.closeAndUsePath (stroke,fillRule,state.clipRule);
    else
      handler.usePath (stroke,fillRule,state.clipRule);

    state.clipRule = NOFILL;
    state.state = PAGE;
  }
  
  private void nestCheck (Object nest)
  {
    try {
      if (state.nestingStack.pop () != nest)
        Options.warn ("invalid nesting of operators in content stream");
    } catch (EmptyStackException ese) {
      Options.warn ("unmatched closing operator in content stream");
    }
  }

  protected void processCommand () throws IOException
  {
    operators.get (command).handle (command);
  }

  // pattern strings can include wildcard '*'s and/or an initial
  // '?' to indicate that the pattern is optional (i.e. there
  // should be a match if it is missing). The first and last
  // patterns must not be optional.
  public void setCommandPattern (String [] commandPattern) {
    this.commandPattern = commandPattern;
    Assertions.unexpect (commandPattern [0].charAt (0),'?');
    Assertions.unexpect (commandPattern [commandPattern.length - 1].charAt (0),'?');
  }
  
  public boolean commandPatternOccurred () {
    return !state.commandPatternCounts.isEmpty () && state.commandPatternCounts.get (0).count == commandPattern.length; 
  }
  
  public void parse (InputStream in,PDFDictionary resources,int type) throws IOException
  {
    stateStack.push (state);
    state = new State ();
    state.in = in;
    if (resources == null)
      Assertions.expect (type == CHAR || type == FORM);
    if (type == ROOT) {
      handler.beginPage ();
      resourceResolver.setPageResourceDictionary (resources);
    }
    resourceResolver.pushResourceDictionary (resources);
    state.state = type == CHAR ? BBOX : PAGE;
    handler.beginContentStream ();
    state.reader = new PDFObjectReader (state.in,wrapping);
    args.removeAllElements ();
    Object object;
    while ((object = state.reader.readObject ()) != null)
      if (object instanceof String)
      {
        command = (String) object;
        if (Options.tracing)
          System.out.println ("processing " + command);
        if (commandPattern != null) {
          state.commandPatternCounts.add (new Count ());
          Iterator<Count> counts = state.commandPatternCounts.iterator ();
          while (counts.hasNext ()) {
            Count count = counts.next ();
            if (count.count == commandPattern.length) {
              System.err.println ("content stream pattern match");
              counts.remove ();
              continue;
            }
            StringBuilder commandLineBuilder = new StringBuilder ();
            for (int i = 0;i < args.size ();i++)
              commandLineBuilder.append (args.get (i)).append (' ');
            commandLineBuilder.append (command);
            boolean optional,matches;
            do {
              String pattern = commandPattern [count.count];
              optional = pattern.charAt (0) == '?';
              if (optional)
                pattern = pattern.substring (1);
              pattern = pattern.replaceAll ("\\*",".*");
              matches = commandLineBuilder.toString ().matches (pattern);
              if (matches || optional)
                count.increment ();
              else
                counts.remove ();
            } while (optional && !matches);
          }
        }
        processCommand ();
        args.removeAllElements ();
      }
      else
        args.addElement (object);
    if (state.state == PATH)
      dropPath();
    
    handler.endContentStream ();
    if (type == ROOT)
      handler.finishPage ();
    
    resourceResolver.popResourceDictionary ();
    state = stateStack.pop ();
  }

  void dropPath()
  {
    Options.warn ("illegal unused path");
    usePath (false,false,NOFILL);
  }

  InputStream inlineImageStream;

  public void exhaustInlineImageStream () throws IOException {
    if (inlineImageStream != null) {
      try {
        while (inlineImageStream.read () != -1)
          ;
      } catch (IncorrectChecksumException ice) {
        // someone probably forgot the checksum
        Options.warn ("incorrect or missing checksum in zlib stream");
        if (!ignoreChecksums.isSet ())
          state.reader.setInputStream (new InputStreamConcatenation
              (new InputStream [] {ice.getInputStream (),state.in}));
      } finally {
        inlineImageStream = null;
      }
    }
  }
} 
