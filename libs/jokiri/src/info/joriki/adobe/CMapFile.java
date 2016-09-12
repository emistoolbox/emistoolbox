/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import info.joriki.io.ByteArrayCharacterSource;
import info.joriki.io.Util;

import info.joriki.util.NotTestedException;
import info.joriki.util.Range;
import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class CMapFile extends PostScriptFile
{
  Map cmapDictionary;

  Map [] singleCodes = new Map [4];
  List [] codeDeltas = new List [4];
  List [] codeRanges = new List [4];
  List [] codeSpaceRanges = new List [4];
  List [] notDefRanges = new List [4];
  List [] notDefValues = new List [4];

  boolean pushedDictionaries;

  public CMapFile (InputStream in) throws IOException
  {
    super (in);

    for (int i = 0;i < 4;i++)
      {
        singleCodes [i] = new HashMap ();
        codeDeltas [i] = new ArrayList ();
        codeRanges [i] = new ArrayList ();
        codeSpaceRanges [i] = new ArrayList ();
        notDefRanges [i] = new ArrayList ();
        notDefValues [i] = new ArrayList ();
      }

    // remove entries so they don't hang around and waste space
    List codeSpaceRangeArray = (List) cmapDictionary.remove ("CodeSpaceRanges");
    for (int i = 0;i < codeSpaceRangeArray.size ();i++)
      {
        Iterator rangeIterator = ((List) codeSpaceRangeArray.get (i)).iterator ();
        while (rangeIterator.hasNext ())
          {
            byte [] begarr = (byte []) rangeIterator.next ();
            byte [] endarr = (byte []) rangeIterator.next ();
            Assertions.expect (endarr.length,begarr.length);
            Range range = new Range (General.toInteger (begarr),
                                     General.toInteger (endarr));
            codeSpaceRanges [begarr.length - 1].add (range);
          }
      }

    List codeRangeArray = (List) cmapDictionary.remove ("CodeRanges");
    for (int i = 0;i < codeRangeArray.size ();i++)
    {
      Iterator rangeIterator = ((List) codeRangeArray.get (i)).iterator ();
    outer:
      while (rangeIterator.hasNext ())
      {
        byte [] begarr = (byte []) rangeIterator.next ();
        byte [] endarr = (byte []) rangeIterator.next ();

        int beg = General.toInteger (begarr);
        int end = General.toInteger (endarr);
        int dst;

        Assertions.expect (endarr.length,begarr.length);

        int index = begarr.length - 1;

        for (int c = beg;c <= end;c++)
          Assertions.expect (isValidCode (c,index));

        Object destination = rangeIterator.next ();
        if (destination instanceof byte [])
          dst = toInteger ((byte []) destination);
        else if (destination instanceof List)
        {
          List list = (List) destination;
          Object first = list.get (0);
          if (first instanceof byte [])
          {
            byte [] string = (byte []) first;
            if (string.length != 2) {
              if (beg != end)
                throw new NotImplementedException ("multi-character ranges in CMap files");
              Assertions.expect (list.size (),1);
              mapSingleCode (beg,index,string);
              continue outer;
            }
            // ToUnicode map for Arial in 0795306520.pdf
            Options.warn ("invalid code range specification in CMap");
            dst = toInteger (string);
            for (int j = 0;j < list.size ();j++)
              if (toInteger ((byte []) list.get (j)) != dst + j) {
                for (j = 0;j < list.size ();j++)
                  mapSingleCode (beg + j,index,(byte []) list.get (j));
                continue outer;
              }
          }
          else
            throw new NotImplementedException ("non-numeric codes");
        }
        else
          throw new Error ("invalid code range specification in CMap");

        codeRanges [index].add (new Range (beg,end));
        codeDeltas [index].add (new Integer (dst - beg));
      }
    }
    
    List cidRangeArray = (List) cmapDictionary.remove ("CIDRanges");
    for (int i = 0;i < cidRangeArray.size ();i++)
      {
        Iterator rangeIterator = ((List) cidRangeArray.get (i)).iterator ();
        while (rangeIterator.hasNext ())
          {
            byte [] begarr = (byte []) rangeIterator.next ();
            byte [] endarr = (byte []) rangeIterator.next ();
            int dst = ((Integer) rangeIterator.next ()).intValue ();
            int beg = General.toInteger (begarr);
            int end = General.toInteger (endarr);

            Assertions.expect (endarr.length,begarr.length);

            int index = begarr.length - 1;

            for (int c = beg;c <= end;c++)
              Assertions.expect (isValidCode (c,index));

            codeRanges [index].add (new Range (beg,end));
            codeDeltas [index].add (new Integer (dst - beg));
          }
      }

    List codeArray = (List) cmapDictionary.remove ("Codes");
    for (int i = 0;i < codeArray.size ();i++)
      {
        Iterator codeIterator = ((List) codeArray.get (i)).iterator ();
        while (codeIterator.hasNext ())
          {
            byte [] codearr = (byte []) codeIterator.next ();
            byte [] string  = (byte []) codeIterator.next ();

            mapSingleCode (codearr,string);
          }
      }

    List notDefArray = (List) cmapDictionary.remove ("NotDefRanges");
    for (int i = 0;i < notDefArray.size ();i++)
      {
        Iterator notDefIterator = ((List) notDefArray.get (i)).iterator ();
        while (notDefIterator.hasNext ())
          {
            byte [] begarr = (byte []) notDefIterator.next ();
            byte [] endarr = (byte []) notDefIterator.next ();
            Integer value  = (Integer) notDefIterator.next ();

            int beg = General.toInteger (begarr);
            int end = General.toInteger (endarr);

            Assertions.expect (endarr.length,begarr.length);

            int index = begarr.length - 1;

            for (int c = beg;c <= end;c++)
              Assertions.expect (isValidCode (c,index));

            notDefRanges [index].add (new Range (beg,end));
            notDefValues [index].add (value);
          }
      }
  }

  private void mapSingleCode (byte [] codearr,byte [] string) {
    mapSingleCode (General.toInteger (codearr),codearr.length - 1,string);
  }

  private void mapSingleCode (int code,int index,byte [] string) {
    singleCodes [index].put (new Integer (code),string);
  }

  // These are actually UTF-16BE-encoded strings,
  // but we've only encountered two bytes so far.
  private static int toInteger (byte [] bytes) {
	  Assertions.expect (bytes.length,2);
//    Util.checkUTF16 (bytes);
    return General.toInteger (bytes);
  }

  protected boolean handleSpecial (String operator) throws IOException
  {
    if (operator.equals ("defineresource"))
    {
      Name category = (Name) operandStack.pop ();
      if (category.name.equals ("CMap"))
        cmapDictionary = (Map) operandStack.peek ();
      operandStack.push (category);
      if (pushedDictionaries) {
        // end end
        dictionaryStack.pop ();
        dictionaryStack.pop ();
      }
    }
    else if (lookup (operator) == null)
    {
      if (operator.equals ("CIDSystemInfo"))
      {
        // Bug in ToUnicode map for UTHTNP+Bookman-SwashM in 0765611856.pdf
        // Additionally, the map for Arial in 0795306520.pdf is missing
        // the closing ">> def"
        // This workaround is a bit lengthy, but the advantage is that it's
        // all done here and we don't need to modify any of the proper code
        Options.warn ("bug in CMap file: CIDSystemInfo should be /CIDSystemInfo");
        Map map = new HashMap ();
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_OPEN);
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_NAME);
        Assertions.expect (new String (tok.bval),"Registry");
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_STRING);
        map.put ("Registry",tok.bval);
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_NAME);
        Assertions.expect (new String (tok.bval),"Ordering");
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_STRING);
        map.put ("Ordering",tok.bval);
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_NAME);
        Assertions.expect (new String (tok.bval),"Supplement");
        Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_INTEGER);
        map.put ("Supplement",new Integer ((int) tok.nval));
        if (tok.nextToken () == AdobeStreamTokenizer.TT_CLOSE)
        {
          // ">>" is there; expect "def"
          Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_WORD);
          Assertions.expect (new String (tok.bval),"def");
        }
        else
        {
          // ">> def" missing
          Options.warn ("bug in CMap file: missing >> def");
          tok.backUp ();
        }
        define ("CIDSystemInfo",map);
        return true;
      }
      // A bug in the ToUnicode map for font T1_27
      // (MWILCU+HighwayGothic-E) in realsimple200504_4.pdf
      if (operator.equals ("CMapName")) {
        Options.warn ("bug in CMap file: missing CMapName");
        operandStack.push (new Name ("MissingName"));
        return true;
      }
      // A bug in the ToUnicode maps for Times-Roman and
      // Times-Bold in 1410606538_2.pdf
      if (operator.equals ("New"))
        return appendName ("Times","New") || appendName ("Courier","New");
      if (operator.equals ("Roman"))
        return appendName ("Times New","Roman");
      // A bug in the ToUnicode map for font C2_0
      // (PZXIOA+Verdana) in oraclemaking201_1.pdf
      if (operator.equals ("begincmap")) {
        Options.warn ("bug in CMap file: missing preface");
        // /CIDInit /ProcSet findresource begin
        dictionaryStack.push (findResource ("CIDInit","ProcSet"));
        // 12 dict begin
        dictionaryStack.push (new HashMap ());
        pushedDictionaries = true;
      }
    }
    return false;
  }

  boolean appendName (String base,String operator)
  {
    if (lookup (operator) != null || operandStack.isEmpty ())
      return false;
    
    Object previous = operandStack.peek ();
    if (!(previous instanceof Name && ((Name) previous).name.equals (base)))
      return false;

    Options.warn ("bug in CMap file: unescaped space");
    operandStack.pop ();
    operandStack.push (new Name (base + ' ' + operator));
    return true;
  }

  void expectOperator (String operator) throws IOException
  {
    Assertions.expect (tok.nextToken (),AdobeStreamTokenizer.TT_WORD);
    Assertions.expect (new String (tok.bval),operator);
  }

  public boolean isValidCode (int c,int sizeIndex)
  {
    List codeSpaceRange = codeSpaceRanges [sizeIndex];
    for (int i = 0;i < codeSpaceRange.size ();i++)
      if (((Range) codeSpaceRange.get (i)).contains (c))
        return true;
    return false;
  }

  public ByteArrayCharacterSource getCharacterSource (byte [] text)
  {
    return new ByteArrayCharacterSource (text) {
        public int read ()
        {
          int code = 0;
          int sizeIndex;
          int position = pos;

          for (sizeIndex = 0;sizeIndex < 4 && pos < arr.length;sizeIndex++)
            {
              code |= arr [pos++] & 0xff;
              List codeSpaceRange = codeSpaceRanges [sizeIndex];
              for (int i = 0;i < codeSpaceRange.size ();i++)
                if (((Range) codeSpaceRange.get (i)).contains (code)) {
                  byte [] string = (byte []) singleCodes [sizeIndex].get (new Integer (code));
                  Assertions.expect (string == null || string.length == 2);
                  return string != null ? (char) General.toInteger (string) : rangeMap (code,sizeIndex);
                }
              code <<= 8;
            }
  
          // the code isn't in any code space range.
          // the PDF spec prescribes a complicated procedure on p. 329 (345)
          // for determining how many bytes to advance. Since this
          // is only used for illegal fonts, it hasn't been tested.

          if (true) {
            if (position == arr.length) // remove when tested 
              return -1;
            throw new NotTestedException ();
          }
          
          int longestMatch = -1; // so match = 0 gives us item 1 in the spec 
          int longestIndex = -1; // this keeps n from increasing after EOF
          pos = position;
          code = 0;
          for (sizeIndex = 0;sizeIndex < 4 && pos < arr.length;sizeIndex++)
            {
              code |= arr [pos++] & 0xff;
              List codeSpaceRange = codeSpaceRanges [sizeIndex];
              for (int i = 0;i < codeSpaceRange.size ();i++)
                {
                  Range range = (Range) codeSpaceRange.get (i);
                  int beg = range.beg;
                  int end = range.end;
                  int part = code;
                  int match = sizeIndex + 1;
                  do {
                    part >>= 8;
                    beg >>= 8;
                    end >>= 8;
                    match--;
                  } while (match > 0 && !(beg <= code && code < end));
  
                  if (match > longestMatch)
                    {
                      longestMatch = match;
                      longestIndex = sizeIndex;
                    }
                }
              code <<= 8;
            }

          return (pos = position + longestIndex + 1) < arr.length ? 0 : -1;
        }
      };
  }
  
  char rangeMap (int code,int sizeIndex)
  {
    List codeRange = codeRanges [sizeIndex];
    for (int j = 0;j < codeRange.size ();j++)
      if (((Range) codeRange.get (j)).contains (code))
        return (char) (code + ((Integer) codeDeltas [sizeIndex].get (j)).intValue ());

    List notDefRange = notDefRanges [sizeIndex];
    for (int j = 0;j < notDefRange.size ();j++)
      if (((Range) notDefRange.get (j)).contains (code))
        return (char) ((Integer) notDefValues [sizeIndex].get (j)).intValue ();

    return 0;
  }
  
  static byte [] charToBytes (int c)
  {
    return new byte [] {(byte) (c >> 8),(byte) c};
  }

  public CIDSystemInfo getCIDSystemInfo ()
  {
    Map cidSystemInfo = (Map) cmapDictionary.get ("CIDSystemInfo");
    return new CIDSystemInfo
      (new String ((byte []) cidSystemInfo.get ("Registry")),
       new String ((byte []) cidSystemInfo.get ("Ordering")),
       ((Integer) cidSystemInfo.get ("Supplement")).intValue ());
  }

  public boolean isVertical () {
    int writingMode = ((Integer) cmapDictionary.get ("WMode")).intValue ();
    Assertions.limit (writingMode,0,1);
    return writingMode == 1;
  }
}
