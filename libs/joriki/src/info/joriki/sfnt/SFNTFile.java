/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.File;
import java.io.DataInput;
import java.io.IOException;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import info.joriki.io.Util;
import info.joriki.io.SeekableFile;
import info.joriki.io.FullySeekableDataInput;

import info.joriki.cff.CFFFont;
import info.joriki.cff.CFFFontSet;

import info.joriki.font.DescribedFont;
import info.joriki.font.GlyphProvider;

import info.joriki.util.Assertions;
import info.joriki.util.General;

import info.joriki.graphics.Transformation;

import info.joriki.opentype.GlyphPositioningTable;
import info.joriki.opentype.GlyphSubstitutionTable;
import info.joriki.truetype.GlyphTable;
import info.joriki.truetype.LocationTable;
import info.joriki.truetype.ControlValueTable;
import info.joriki.truetype.ByteCodeInterpreter;

public class SFNTFile extends SFNTDocument implements DescribedFont
{
  class HeaderEntry
  {
    String id;
    int checksum;
    int offset;
    int length;

    HeaderEntry (DataInput in) throws IOException
    {
      id = Util.readString (in,4);
      checksum = in.readInt ();
      offset = in.readInt ();
      length = in.readInt ();
    }
  }

  HashMap entries = new HashMap ();

  FullySeekableDataInput in;

  public SFNTFile (String filename) throws IOException
  {
    this (new File (filename));
  }

  public SFNTFile (File file) throws IOException
  {
    this (new SeekableFile (file,"r"));
  }

  public SFNTFile (FullySeekableDataInput in) throws IOException
  {
    super (in.readInt ());

    this.in = in;

    int ntable = in.readShort ();             // numTables
    new BinarySearchData (ntable,unitSize).assertFrom (in);

    for (int i = 0;i < ntable;i++)
      {
        HeaderEntry entry = new HeaderEntry (in);
        if (entry.length == 0) {
	  String id = entry.id;
          // accessintel-31134_92.pdf
          if (id.equals (NAME))
            continue;
          // fpgm: viasatellite200607_4.pdf
          // cvt and prep: p. 48 in ticket 44489
	  if (!(id.equals (FPGM) || id.equals (CVT) || id.equals (PREP) || id.equals ("glyf")))
	    throw new Error ("empty SFNT table");
        }
        entries.put (entry.id,entry);
      }
  }

//  FP 08/01/2014 removed method identical to overridden method
//  int checksum (byte [] data)
//  {
//    SFNTChecksumStream checksumStream = new SFNTChecksumStream ();
//    checksumStream.write (data);
//    return checksumStream.checksum;
//  }

  public byte [] getRawData (String id) throws IOException
  {
    HeaderEntry entry = (HeaderEntry) entries.get (id);
    if (entry == null)
      return null;
    in.seek (entry.offset);
    return Util.readBytes (in,entry.length);
  }

  public void removeTable (String id)
  {
    super.removeTable (id);
    entries.remove (id);
  }

  public SFNTTable getTable (String id)
  {
    SFNTTable table = super.getTable (id);
    if (table != null)
      return table;

    HeaderEntry entry = (HeaderEntry) entries.get (id);
    if (entry == null)
      return null;

    table = getTable (entry);
    addTable (table);
    return table;
  }

  private SFNTTable getTable (HeaderEntry entry)
  {
    String id = entry.id;

    SFNTTable aux = null;
    SFNTTable auy = null;
    if (id.equals (GLYF))
      {
        aux = getTable (LOCA);
        auy = getTable (HMTX);
      }
    else if (id.equals (HMTX))
      aux = getTable (HHEA);
    else if (id.equals (LOCA))
      aux = getTable (HEAD);

    int length = entry.length;

    try {
      in.pushOffset (entry.offset);
      try {
        if (id.equals (CMAP))
          return new CMap (in,length);
        else if (id.equals (CVT))
          return new ControlValueTable (in,length);
        else if (id.equals (GLYF))
          return new GlyphTable (in,(LocationTable) aux,(HorizontalMetrics) auy);
        else if (id.equals (HEAD))
          return new SFNTHeader (in);
        else if (id.equals (HHEA))
          return new HorizontalHeader (in);
        else if (id.equals (HMTX))
          return new HorizontalMetrics (in,(HorizontalHeader) aux,length);
        else if (id.equals (LOCA))
          return new LocationTable (in,(SFNTHeader) aux,length);
        else if (id.equals (MAXP))
          return new MaximumProfile (in);
        else if (id.equals (NAME))
          return new NamingTable (in,length);
        else if (id.equals (POST))
          return new PostScriptTable (in);
        else if (id.equals (OS2))
          return new OS2Table (in,length);
        // OpenType tables
        else if (id.equals (GPOS))
          return new GlyphPositioningTable (in);
        else if (id.equals (GSUB))
          return new GlyphSubstitutionTable (in);
        else
          return new GenericTable (in,id,length);
      } finally {
        in.popOffset ();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      return null;
    }
  }

  public Set getIDs ()
  {
    Set ids = super.getIDs ();

    ids.addAll (entries.keySet ());

    return ids;
  }

  public void rewrite ()
  {
    Iterator iterator = entries.keySet ().iterator ();
    while (iterator.hasNext ())
      getTable ((String) iterator.next ());
  }

  public SFNTHeader getHeader ()
  {
    return (SFNTHeader) getTable (HEAD);
  }

  public double [] getFontBBox ()
  {
    return getHeader ().bbox ();
  }

  public double [] getFontMatrix ()
  {
    return Transformation.scalingBy (1. / getHeader ().unitsPerEm).matrix;
  }

  public String getName ()
  {
    NamingTable namingTable = (NamingTable) getTable (NAME);
    return namingTable == null ? null : namingTable.getPostScriptName ();
  }

  public String getNotice ()
  {
    NamingTable namingTable = (NamingTable) getTable (NAME);
    if (namingTable == null)
      return null;
    String notice = namingTable.getTradeMark ();
    if (notice == null)
      notice = namingTable.getCopyrightNotice ();
    return notice == null ? null : General.replace
      (General.replace
       (notice,
        (char) 0xa8,"(R)"),
       (char) 0xa9,"(C)");
  }

  public String getWeight ()
  {
    OS2Table os2Table = (OS2Table) getTable (OS2);
    return os2Table == null ? null : os2Table.getWeight ();
  }

  public static void main (String [] args) throws IOException
  {
    int arg = 0;

    SFNTFile file = new SFNTFile (args [arg++]);
    if (args [arg].charAt (0) == '-')
      {
        CMap cmap = null;
        CMapTable subtable = null;
        CFFFontSet fontSet = null;
        CFFFont font = null;
        while (arg < args.length && args [arg].charAt (0) == '-')
          {
            switch (args [arg++].charAt (1))
              {
              case 'a' : // strip table
                file.removeTable (args [arg++]);
                break;
              case 'b' : // show metric info
                System.out.println ("units per em : " + file.getHeader ().unitsPerEm);
                System.out.println ("bbox : " + General.toString (file.getFontBBox ()));
                HorizontalHeader hhea = (HorizontalHeader) file.getTable (HHEA);
                OS2Table os2 = (OS2Table) file.getTable (OS2);
                System.out.println ("ascent : " + hhea.ascent);
                System.out.println ("descent : " + hhea.descent);
                System.out.println ("lineGap : " + hhea.lineGap);
                System.out.println ("sTypoAscender = " + os2.sTypoAscender);
                System.out.println ("sTypoDescender = " + os2.sTypoDescender);
                System.out.println ("usWinAscent = " + os2.usWinAscent);
                System.out.println ("usWinDescent = " + os2.usWinDescent);
                System.out.println ("sxHeight = " + os2.sxHeight);
                System.out.println ("sCapHeight = " + os2.sCapHeight);
                System.out.println ("yStrikeoutPosition = " + os2.yStrikeoutPosition);
                break;
              case 'c' : // list cmap
                int platform = Integer.parseInt (args [arg++]);
                int encoding = Integer.parseInt (args [arg++]);
                int language = Integer.parseInt (args [arg++]);
                cmap = (CMap) file.getTable (CMAP);
                subtable = cmap.getTable (platform,encoding,language);
                subtable.printTo (System.out);
                break;
              case 'e' : // import charstring from another CFF file
                fontSet = new CFFFontSet
                  (Util.toByteArray (file.getTable (CFF)));
                font = fontSet.getOnlyFont ();
                CFFFontSet other = new CFFFontSet (args [arg++]);
                CFFFont otherFont = other.getOnlyFont ();
                byte [] charstr = otherFont.getCharString (Integer.parseInt (args [arg++]));
                font.setCharString (Integer.parseInt (args [arg++]),charstr);
                file.addTable (new GenericTable (CFF,fontSet.toByteArray ()));
                break;
              case 'i' : // import cff file
                file.addTable (new GenericTable (CFF,Util.undump (args [arg++])));
                break;
              case 'l' : // dump tables
                Iterator idIterator = file.getIDs ().iterator ();
                while (idIterator.hasNext ())
                  {
                    String id = (String) idIterator.next ();
                    if (id.indexOf ('/') == -1)
                      Util.dump (file.getRawData (id),id + ".dmp");
                  }
                break;
              case 'm' : // modify cmap
                char code = args [arg++].charAt (0);
                int index = Integer.parseInt (args [arg++]);
                ByteEncodingTable byteTable = new ByteEncodingTable (subtable);
                byteTable.map (code,index);
                SegmentMappingTable segmentTable = new SegmentMappingTable (byteTable);
                cmap.addEntry (segmentTable);
                break;
              case 'n' : // new cmap with only one entry
                ByteEncodingTable emptyTable = new ByteEncodingTable (0,3,0);
                emptyTable.map ('N',49);
                cmap.addEntry (new SegmentMappingTable (emptyTable));
                break;
              case 'p' : // peek at table
                file.getTable (args [arg++]);
                break;
              case 'q' : // peek and dump
                String which = args [arg++];
                Util.dump (file.getRawData (which),which + ".dmp");
                SFNTTable table = file.getTable (which);
                Util.dump (table,which + ".out");
                break;
              case 'r' : // replace cmap table
                SFNTFile cmapFile = new SFNTFile (args [arg++]);
                file.addTable (cmapFile.getTable (CMAP));
                break;
              case 's' : // byte table morph
                SFNTFile morphFile = new SFNTFile (args [arg++]);
                CMap morphMap = (CMap) morphFile.getTable (CMAP);
                CMapTable morphTable = morphMap.getTable (0,3,0);
                ByteEncodingTable newTable = new ByteEncodingTable (subtable);
                for (char i = 1;i < 256;i++)
                  {
                    newTable.map (i,morphTable.getGlyphIndex (i));
                    cmap.addEntry (new SegmentMappingTable (newTable));
                    Util.dump (file,"morph" + ((int) i) + ".cef");
                  }
                break;
              case 'x' : // extract table
                String id = args [arg++];
                String dump = args [arg++];
                Util.dump (file.getRawData (id),dump);
                break;
              default :
                throw new Error ("option " + args [arg-1].charAt (1));
              }
          }
      }
    if (arg != args.length)
      Util.dump (file,args [arg]);
  }

  ByteCodeInterpreter byteCodeInterpreter;

  public GlyphProvider getGlyphProvider (Object glyphSelector)
  {
    if (byteCodeInterpreter == null)
      byteCodeInterpreter = new ByteCodeInterpreter (this);
    ((GlyphTable) getTable (GLYF)).getOutline (((Integer) glyphSelector).intValue ()).
      interpret (byteCodeInterpreter);
    return byteCodeInterpreter;
  }
}
