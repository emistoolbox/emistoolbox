/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.cff;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import info.joriki.io.SeekableFile;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.FullySeekableDataInput;

import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.OptionHandler;
import info.joriki.util.ArgumentIterator;
import info.joriki.util.NotImplementedException;

import info.joriki.charstring.CharStringReader;
import info.joriki.charstring.CharStringPrinter;

public class CFFFontSet implements CFFSpeaker
{
  final static byte major = 1;
  final static byte minor = 0;
  int offSize = 1;
  final static byte hdrSize = 4;

  final static Map standardMap = new HashMap ();
  static {
    for (int i = 0;i < standardStrings.length;i++)
      standardMap.put (standardStrings [i],new Integer (i));
  }

  CFFIndex nameIndex   = new CFFIndex ();
  CFFIndex dictIndex   = new CFFIndex ();
  CFFIndex stringIndex = new CFFIndex ();
  CFFIndex subrIndex   = new CFFIndex ();

  public void writeTo (String file) throws IOException
  {
    FileOutputStream fos = new FileOutputStream (file);
    try {
      writeTo (fos);
      fos.flush ();
    } finally {
      fos.close ();
    }
  }

  public void writeTo (OutputStream out) throws IOException
  {
    out.write (toByteArray ());
  }
  
  public byte [] toByteArray ()
  {
    stringIndex.clear ();
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int oldSize,newSize = 0;
    do
      {
        oldSize = newSize;
        baos.reset ();
        writeTentativelyTo (baos);
        newSize = baos.size ();
      }
    while (newSize != oldSize);
    
    return baos.toByteArray ();
  }

  private void writeTentativelyTo (ByteArrayOutputStream out)
  {
    out.write (major);
    out.write (minor);
    out.write (hdrSize);
    out.write (offSize);
    nameIndex.writeTo (out);
    dictIndex.writeTo (out);
    stringIndex.writeTo (out);
    subrIndex.writeTo (out);

    for (int i = 0;i < dictIndex.size ();i++)
      ((CFFFont) dictIndex.get (i)).data.writeTo (out);
  }

  int getSID (String str)
  {
    Integer standardID = (Integer) standardMap.get (str);
    if (standardID != null)
      return standardID.intValue ();

    int SID = stringIndex.indexOf (str);
    if (SID == -1)
      {
        SID = stringIndex.size ();
        stringIndex.add (str);
      }
    return SID + standardStrings.length;
  }

  String getString (int SID)
  {
    if (SID < standardStrings.length)
      {
        if (standardStrings [SID] == null)
          throw new Error ("uninitialized standard string " + SID);
        return standardStrings [SID];
      }
    Object o = stringIndex.get (SID - standardStrings.length);
    if (o instanceof String)
      return (String) o;
    if (o instanceof byte [])
      return new String ((byte []) o);
    throw new Error ("unknown object type in string index : " + o.getClass ());
  }

  // no sorting is performed; add fonts in lexical order.
  public void addFont (CFFFont font,String name)
  {
    dictIndex.add (font);
    nameIndex.add (name);
  }

  public CFFFont getFont (int index)
  {
    return (CFFFont) dictIndex.get (index);
  }
  
  public CFFFont getFont (String name)
  {
    int index = nameIndex.indexOf (name);
    return index == -1 ? null : getFont (index);
  }

  public String getName (CFFFont font)
  {
    int index = dictIndex.indexOf (font);
    return index == -1 ? null : (String) nameIndex.get (index);
  }

  public CFFFont getOnlyFont ()
  {
    if (dictIndex.size () != 1)
      throw new Error ("expected only one font");
    return (CFFFont) dictIndex.get (0);
  }

  public boolean isEmpty ()
  {
    return nameIndex.isEmpty ();
  }

  public CFFFontSet () {}
  
  public CFFFontSet (String file) throws IOException
  {
    this (new File (file));
  }

  public CFFFontSet (File file) throws IOException
  {
    this (new SeekableFile (file,"r"));
  }

  public CFFFontSet (byte [] data) throws IOException
  {
    this (new SeekableByteArray (data));
  }

  public CFFFontSet (FullySeekableDataInput in) throws IOException
  {
    try {
      if (in.read () != major | in.read () != minor)
        throw new NotImplementedException ("CFF version other than 1.0");
      
      int offset = in.read ();
      offSize = in.read ();
      in.seek (offset);
      
      nameIndex = new CFFIndex (in);
      dictIndex = new CFFIndex (in);
      stringIndex = new CFFIndex (in);
      subrIndex = new CFFIndex (in);
      
      Assertions.expect (nameIndex.size (),dictIndex.size ());
      
      for (int i = 0;i < dictIndex.size ();i++)
        {
          CFFFont font = new CFFFont (this);
          byte [] dict = (byte []) dictIndex.get (i);
          font.readFrom (new ByteArrayInputStream (dict));
          font.data.readFrom (in);
          dictIndex.set (i,font);
          // turn byte arrays into Strings in name index
          nameIndex.set (i,new String ((byte []) nameIndex.get (i)));
        }
    } finally { in.close (); }
  }
    
  public static void main (String [] args) throws IOException
  {
    final File inputFile = Options.getInputFile (args);
    final CFFFontSet fontSet = inputFile == null ? null :
      new CFFFontSet (inputFile);
      
      new Options (CFFFontSet.class,"<CFF file>",null).parse (args,new OptionHandler () {
        CFFFont font = inputFile == null ? null : fontSet.getOnlyFont ();
        public boolean handle (char action,ArgumentIterator args)
        {
          switch (action)
            {
            case 'g' : // list glyphs
              if (font.glyphs == null)
                System.out.println ("font has no charset");
              else
              {
                Iterator iterator = font.glyphs.iterator ();
                for (int i = 0;iterator.hasNext ();i++)
                  System.out.println (i + " : " + iterator.next ());
              }
              break;
            case 'e' : // list encoding
              String [] fontglyphs = font.getDefaultEncoding ().glyphs;
              if (font.encoding == null)
                System.out.println ("predefined encoding : ");
              for (int i = 0;i < fontglyphs.length;i++)
                System.out.println (i + " : " + fontglyphs [i]);
              break;
            case 'c' : // list charstrings
              CFFIndex charStrings = font.charStrings;
              for (int i = 0;i < charStrings.size ();i++)
              {
                System.out.println (i + " :");
                byte [] arr = (byte []) charStrings.get (i);
                for (int j = 0;j < arr.length;j++)
                  System.out.print ((arr [j] & 0xff) + " ");
                System.out.println ();
              }
              break;
            case 'o' : // output font set
              try {
                fontSet.writeTo (args.nextString ());
              } catch (IOException e) {
                e.printStackTrace();
              }
              break;
            case 'p' : // print charstring
              new CharStringReader (2).read ((byte []) font.charStrings.get (args.nextInt ()),new CharStringPrinter (System.out));
              break;
            default :
              return false;
            }
          return true;
        }
      });
  }
}

/* data in arial.cff (as extracted unchanged from arial.cef
   name index : 1 entry
   ArialMT
   dict index : 1 entry (i.e. one font)
   dict entries : (bytes 0x15 to 0x49)
   notice : only entry in string index
   weight : Regular
   FontBBox : [-455 -665 2195 2124]
   FontMatrix : [4.8828125E-4 0 0 4.8828125E-4 0 0]
   charset : 181
   Encoding : 206
   CharStrings : 208
   Private : [8 1898]
   string index : 1 entry
   copyright notice
   subroutine index : 0 entries

   charset : format 1
   ranges :
   (.notdef omitted)
   6 0 percent
   e 0 hyphen
   22 4 A B C D E
   2a 0 I
   2d 3 L M N O
   33 2 R S T
   37 0 V
   3a 0 Y
   Encoding : 0 0
   CharStrings : OK

   Private DICT:
   default width : 1366
   nominal width : 1586
*/

