/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;

import java.awt.Label;
import java.awt.Container;
import java.awt.GridLayout;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import info.joriki.io.Util;
import info.joriki.io.Readable;
import info.joriki.io.EmptyInputStream;
import info.joriki.io.InputStreamConcatenation;

import info.joriki.util.Cinderella;
import info.joriki.util.Count;
import info.joriki.util.Handler;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;

public class PDFDictionary extends PDFContainer
{
  // These abbreviations are meant for inline images, but
  // Acrobat handles them everywhere.
  // See Implementation Note 9 in Appendix H for Section 3.3.
  final static Map<String,String> abbreviations = new HashMap<String,String> ();
  static {
    abbreviations.put ("AHx","ASCIIHex");
    abbreviations.put ("A85","ASCII85");
    abbreviations.put ("CCF","CCITTFax");
    abbreviations.put ("DCT","DCT");
    abbreviations.put ("Fl","Flate");
    abbreviations.put ("LZW","LZW");
    abbreviations.put ("RL","RunLength");
  }

  HashMap<String,PDFObject> map;
  Set<String> unusedKeys;

  public PDFDictionary ()
  {
    map = new HashMap<String,PDFObject> ();
  }

  public PDFDictionary (PDFDictionary dictionary)
  {
    this.map = (HashMap<String,PDFObject>) dictionary.map.clone ();
  }

  public PDFDictionary (String type)
  {
    this ();
    put ("Type",type);
  }

  public PDFObject put (String key,PDFObject value)
  {
    return map.put (key,value);
  }

  void put (PDFName key,PDFObject value)
  {
    put (key.getName (),value);
  }

  public void put (String key,String value)
  {
    put (key,new PDFName (value));
  }

  public void putIndirect (String key,PDFObject value)
  {
    map.put (key,new PDFIndirectObject (value));
  }

  public void put (String key,int value)
  {
    put (key,new PDFInteger (value));
  }

  // does *not* replace PDFNull by null (see get below)
  // this is only for low-level use when indirect and null
  // objects need to be preserved, typically if you want
  // to put them back into a dictionary to be constructed
  public PDFObject getUnresolved (String key)
  {
    use (key);
    return map.get (key);
  }

  // same as above, if you know this is an indirect object
  public PDFIndirectObject getIndirect (String key)
  {
    return (PDFIndirectObject) getUnresolved (key);
  }

  // the spec says null objects are as if the entry were omitted
  public PDFObject get (String key)
  {
    PDFObject value = getUnresolved (key);
    return value == null ? null : value.resolve ();
  }

  public PDFObject get (PDFName name)
  {
    return get (name.getName ());
  }

  public PDFObject get (String key,PDFObject defaultValue)
  {
    PDFObject value = get (key);
    return value == null ? defaultValue : value;
  }

  public boolean getBoolean (String key)
  {
    return ((PDFBoolean) get (key)).val;
  }

  public boolean getBoolean (String key,boolean defaultValue)
  {
    PDFBoolean bool = (PDFBoolean) get (key);
    return bool == null ? defaultValue : bool.val;
  }

  public boolean [] getBooleanArray (String key,boolean [] defaultValue)
  {
    PDFArray array = (PDFArray) get (key);
    return array == null ? defaultValue : array.toBooleanArray ();
  }

  private PDFInteger getInteger (String key)
  {
    try {
      return (PDFInteger) get (key);
    } catch (ClassCastException cce) {
      double value = getDouble (key);
      if (value == (int) value)
	return new PDFInteger ((int) value);
      throw cce;
    }
  }

  public int getInt (String key)
  {
    return getInteger (key).val;
  }

  public int getInt (String key,int defaultValue)
  {
    PDFInteger integer = getInteger (key);
    return integer == null ? defaultValue : integer.val;
  }

  public int [] getIntArray (String key)
  {
    PDFArray array = (PDFArray) get (key);
    return array == null ? null : array.toIntArray ();
  }

  public double getDouble (String key)
  {
    return ((PDFNumber) get (key)).doubleValue ();
  }

  public double getDouble (String key,double defaultValue)
  {
    PDFNumber number = (PDFNumber) get (key);
    return number == null ? defaultValue : number.doubleValue ();
  }

  public float getFloat (String key)
  {
    return ((PDFNumber) get (key)).floatValue ();
  }

  public float getFloat (String key,float defaultValue)
  {
    PDFNumber number = (PDFNumber) get (key);
    return number == null ? defaultValue : number.floatValue ();
  }

  public double [] getDoubleArray (String key)
  {
    PDFArray array = (PDFArray) get (key);
    return array == null ? null : array.toDoubleArray ();
  }

  public double [] getDoubleArray (String key,double [] defaultValue)
  {
    PDFArray array = (PDFArray) get (key);
    return array == null ? defaultValue : array.toDoubleArray ();
  }

  public double [] getRectangleArray (String key)
  {
    double [] rect = getDoubleArray (key);
    if (rect == null)
      return null;
    Assertions.expect (rect.length,4);
    for (int i = 0;i < 2;i++)
      if (rect [i] > rect [i+2])
        {
          double z = rect [i];
          rect [i] = rect [i+2];
          rect [i+2] = z;
        }
    return rect;
  }

  public Rectangle getRectangle (String key)
  {
    double [] rect = getRectangleArray (key);
    return rect == null ? null : new Rectangle (rect);
  }

  public Rectangle getRectangle (String key,Rectangle defaultValue)
  {
    double [] rect = getRectangleArray (key);
    return rect == null ? defaultValue : new Rectangle (rect);
  }

  public float [] getFloatArray (String key)
  {
    PDFArray array = (PDFArray) get (key);
    return array == null ? null : array.toFloatArray ();
  }

  public float [] getFloatArray (String key,float [] defaultValue)
  {
    PDFArray array = (PDFArray) get (key);
    return array == null ? defaultValue : array.toFloatArray ();
  }

  public Transformation getTransformation (String key)
  {
    double [] matrix = getDoubleArray (key);
    return matrix == null ? null : new Transformation (matrix);
  }

  public Transformation getTransformation
    (String key,Transformation defaultValue)
  {
    double [] matrix = getDoubleArray (key);
    return matrix == null ? defaultValue : new Transformation (matrix);
  }

  public byte [] getBytes (String key)
  {
    PDFString string = (PDFString) get (key);
    return string == null ? null : string.getBytes ();
  }

  public String getName (String key)
  {
    PDFName name = (PDFName) get (key);
    return name == null ? null : name.getName ();
  }

  public String getName (String key,String defaultValue)
  {
    PDFName name = (PDFName) get (key);
    return name == null ? defaultValue : name.getName ();
  }

  public String getUTFName (String key)
  {
    PDFName name = (PDFName) get (key);
    return name == null ? null : name.getUTFString ();
  }

  public String getAsciiString (String key)
  {
    PDFString string = (PDFString) get (key);
    return string == null ? null : string.toAsciiString ();
  }

  public String getAsciiString (String key,String defaultValue)
  {
    PDFString string = (PDFString) get (key);
    return string == null ? defaultValue : string.toAsciiString ();
  }

  public String getTextString (String key)
  {
    PDFString string = (PDFString) get (key);
    return string == null ? null : string.toTextString ();
  }

  public String getTextString (String key,boolean allowEOL)
  {
    PDFString string = (PDFString) get (key);
    return string == null ? null : string.toTextString (allowEOL);
  }

  public PDFDate getDate (String key)
  {
    PDFString string = (PDFString) get (key);
    return string == null ? null : new PDFDate (string.str);
  }

  public PDFObject remove (String key)
  {
    return map.remove (key);
  }

  public Set<String> keys ()
  {
    return map.keySet ();
  }

  public Collection<PDFObject> elements ()
  {
    return map.values ();
  }

  public int size () {
    return map.size ();
  }

  // checks whether the dictionary contains this key
  public boolean contains (String key)
  {
    use (key);
    return map.containsKey (key);
  }

  // checks whether the specified entry has the specified name value
  public boolean matches (String key,String value)
  {
    PDFObject entry = get (key);
    return entry instanceof PDFName &&
      ((PDFName) entry).getName ().equals (value);
  }

  // checks whether the specified entry is an array and
  // contains the specified name value.
  public boolean contains (String key,String value)
  {
    PDFObject entry = get (key);
    if (!(entry instanceof PDFArray))
      return false;
    PDFArray array = (PDFArray) entry;
    for (int i = 0;i < array.size ();i++)
      {
        PDFObject element = array.get (i);
        if (element instanceof PDFName &&
            ((PDFName) element).getName ().equals (value))
          return true;
      }
    return false;
  }

  public boolean matchesOrContains (String key,String value)
  {
    return matches (key,value) || contains (key,value);
  }

  public boolean isOfType (String type)
  {
    return matches ("Type",type);
  }

  public boolean isOfSubtype (String subtype)
  {
    return matches ("Subtype",subtype);
  }

  public boolean isOptionallyOfType (String type)
  {
    return isOfType (type) || !contains ("Type");
  }

  public boolean isOptionallyOfSubtype (String type)
  {
    return isOfSubtype (type) || !contains ("Subtype");
  }

  protected boolean write (PDFObjectWriter writer) throws IOException
  {
    writer.write ('<');
    writer.write ('<');
    Iterator<String> keyIterator = keys ().iterator ();
    Iterator<PDFObject> valueIterator = unresolvedIterator ();
    while (keyIterator.hasNext ()) {
      PDFName.write (keyIterator.next (),writer);
      writer.isDelimited = false;
      writer.write (valueIterator.next ());
    }
    writer.write ('>');
    writer.write ('>');
    return true;
  }

  public String toString ()
  {
    return toString (true);
  }

  public String toString (boolean addDelimiters) {
    StringBuilder stringBuilder = new StringBuilder ();

    if (addDelimiters)
      stringBuilder.append ("<<\n");
    Iterator<String> keyIterator = keys ().iterator ();
    Iterator<PDFObject> valueIterator = unresolvedIterator ();
    while (keyIterator.hasNext ())
      stringBuilder.
        append (PDFName.externalForm (keyIterator.next ())).
        append (' ').
        append (valueIterator.next ()).
        append ('\n');
    if (addDelimiters)
      stringBuilder.append (">>");

    return stringBuilder.toString ();
  }

  public void unimplement (String [] keys)
  {
    for (int i = 0;i < keys.length;i++)
      unimplement (keys [i]);
  }

  public void unimplement (String key)
  {
    if (contains (key))
      throw new NotImplementedException ("dictionary entry " + key);
  }

  public void putAll (PDFDictionary dictionary)
  {
    map.putAll (dictionary.map);
  }

  final static Map<String,StreamFilter> streamFilters = new HashMap<String,StreamFilter> ();

  public InputStream getFilteredInputStream (InputStream raw)
  {
    return getFilteredInputStream (raw,false);
  }

  public InputStream getFilteredInputStream (InputStream raw,boolean treadBytely)
  {
    List<PDFFilter> filters = getFilters ();
    if (filters.size () == 0)
      return raw;
    Readable in = Util.toReadable (raw);

    for (PDFFilter filter : filters)
    {
      if (abbreviations.containsKey (filter.name))
        filter.name = abbreviations.get (filter.name) + "Decode";
        // For the benefit of code that gives special treatment to
        // FlateDecode (ContentStreamParser) and CCITTFaxDecode
        // (PDFImageDecoder and ImageReconstructor), we replace
        // the filter name. This only works because that code
        // calls getFilteredInputStream before looking at the
        // filter names.
      if (treadBytely && filter.name.equals ("FlateDecode"))
      {
        if (filter.decodeParameters == null)
          filter.decodeParameters = new PDFDictionary ();
        filter.decodeParameters.put ("TreadBytely",new PDFBoolean (true));
      }

      in = getFilteredReadable (in,filter);

      if (filter.decodeParameters != null)
      {
        // Business200409.pdf
        // copy color transform from parameter dictionary to image dictionary where ImageHandler can find it
        if (filter.name.equals ("DCTDecode") && filter.decodeParameters.contains ("ColorTransform"))
          put ("ColorTransform",filter.decodeParameters.get ("ColorTransform"));

        filter.decodeParameters.checkUnused ("3.7-3.12");
        filter.decodeParameters.remove ("TreadBytely");
      }

      treadBytely = false;
    }
    setFilters (filters);
    return Util.toInputStream (in);
  }

  public Readable getFilteredReadable (Readable in,PDFFilter filter) {
    StreamFilter streamFilter = streamFilters.get (filter.name);
    if (streamFilter == null)
    {
      try {
        streamFilter = (StreamFilter) Class.forName ("info.joriki.pdf.filter." + filter.name + 'r').newInstance ();
      } catch (Exception e) {
        throw new NotImplementedException ("filter " + filter.name);
      }
      streamFilters.put (filter.name,streamFilter);
    }
    return streamFilter.getReadable (in,filter.decodeParameters);
  }

  public List<PDFFilter> getFilters () {
    PDFArray filterNames = PDFArray.toArray (get ("Filter"));
    if (filterNames == null)
      filterNames = new PDFArray ();
    PDFArray decodeParameters = PDFArray.toArray (get ("DecodeParms"));
    if (decodeParameters != null)
      Assertions.expect (decodeParameters.size (),filterNames.size ());
    List<PDFFilter> filters = new ArrayList<PDFFilter> ();
    for (int i = 0;i < filterNames.size ();i++)
      filters.add (new PDFFilter (((PDFName) filterNames.get (i)).getName (),
          decodeParameters == null ? null : (PDFDictionary) decodeParameters.get (i)));
    return filters;
  }

  public void setFilters (List<PDFFilter> filters) {
    remove ("Filter");
    remove ("DecodeParms");
    switch (filters.size ()) {
    case 0 : return;
    case 1 :
      PDFFilter onlyFilter = filters.get (0);
      put ("Filter",new PDFName (onlyFilter.name));
      if (onlyFilter.decodeParameters != null)
        put ("DecodeParms",onlyFilter.decodeParameters);
      break;
    default :
      PDFArray filterNames = new PDFArray ();
      for (PDFFilter filter : filters)
        filterNames.add (new PDFName (filter.name));
      put ("Filter",filterNames);
      PDFArray decodeParameters = new PDFArray ();
      boolean needDecodeParameters = false;
      for (PDFFilter filter : filters)
        if (filter.decodeParameters == null)
          decodeParameters.add (PDFNull.nullObject);
        else {
          decodeParameters.add (filter.decodeParameters);
          needDecodeParameters = true;
        }
      if (needDecodeParameters)
        put ("DecodeParms",decodeParameters);
    }
  }

  public InputStream getContentStream () throws IOException
  {
    Assertions.expect (isOfType ("Page"));

    PDFObject contents = get ("Contents");

    // might be better to return null?
    if (contents == null)
      return new EmptyInputStream ();

    if (contents instanceof PDFStream)
      return ((PDFStream) contents).getInputStream ("3.4");

    final Iterator streams = ((PDFArray) contents).iterator ();

    return new InputStreamConcatenation (new Iterator () {
        public boolean hasNext ()
        {
          return streams.hasNext ();
        }

        public Object next ()
        {
          try {
            return ((PDFStream) streams.next ()).getInputStream ("3.4");
          } catch (IOException ioe) {
            ioe.printStackTrace ();
            return null;
          }
        }

        public void remove ()
        {
          streams.remove ();
        }
      },' '); // insert spaces as delimiters; required for PSSVG.pdf
  }

  public PDFObject treeLookup (Comparable key)
  {
    String valueKey;
    if (key instanceof PDFInteger)
      valueKey = "Nums";
    else if (key instanceof PDFString)
      valueKey = "Names";
    else
      throw new Error ("Key of " + key.getClass () + " not allowed in tree");

    PDFArray values = (PDFArray) get (valueKey);
    PDFArray limits = (PDFArray) get ("Limits");
    PDFArray kids = (PDFArray) get ("Kids");
    checkUnused ("3.32,3.33");
    Assertions.expect (values != null ^ kids != null);

    if (limits != null)
      if (key.compareTo (limits.get (0)) < 0 ||
          key.compareTo (limits.get (1)) > 0)
        return null;

    if (values != null)
      {
        for (int i = 0;i < values.size ();i += 2)
          if (key.equals (values.get (i)))
            return values.get (i + 1);
      }
    else // kids != null
      for (int i = 0;i < kids.size ();i++)
      {
        PDFObject result =
          ((PDFDictionary) kids.get (i)).treeLookup (key);
        if (result != null)
          return result;
      }

    return null;
  }

  public void traverseTree (PDFTreeHandler handler)
  {
    PDFArray values = (PDFArray) get ("Nums");
    if (values == null)
      values = (PDFArray) get ("Names");
    PDFArray kids = (PDFArray) get ("Kids");
    Assertions.expect (contains ("Limits"));
    checkUnused ("3.32,3.33");
    Assertions.expect (values != null ^ kids != null);
    if (values != null)
      for (int i = 0;i < values.size ();i += 2)
        handler.handle (values.get (i),values.indirectAt (i+1));
    else // kids != null
      for (Object kid : kids)
        ((PDFDictionary) kid).traverseTree (handler);
  }

  public boolean pruneTree (Cinderella cinderella) {
    return pruneTreeNode (cinderella,true);
  }

  private boolean pruneTreeNode (Cinderella cinderella,boolean isRoot) {
    PDFArray values = (PDFArray) get ("Nums");
    if (values == null)
      values = (PDFArray) get ("Names");
    PDFArray kids = (PDFArray) get ("Kids");
    use ("Limits");
    remove ("Limits");
    checkUnused ("3.32,3.33");
    Assertions.expect (values != null ^ kids != null);
    PDFObject first;
    PDFObject last;
    if (values != null) {
      Iterator iterator = values.iterator ();
      while (iterator.hasNext ())
        if (cinderella.isGood (iterator.next ()))
          iterator.next ();
        else {
          iterator.remove ();
          iterator.next ();
          iterator.remove ();
        }
      if (values.isEmpty ())
        return true;
      first = values.get (0);
      last = values.get (values.size () - 2);
    }
    else { // kids != null
      Iterator iterator = kids.iterator ();
      while (iterator.hasNext ())
        if (((PDFDictionary) iterator.next ()).pruneTreeNode (cinderella,false))
          iterator.remove ();
      if (kids.isEmpty ())
        return true;
      first = ((PDFArray) ((PDFDictionary) kids.get (0)).get ("Limits")).get (0);
      last = ((PDFArray) ((PDFDictionary) kids.get (kids.size () - 1)).get ("Limits")).get (1);
    }
    if (!isRoot)
      put ("Limits",new PDFArray (new PDFObject [] {first,last}));
    return false;
  }

  private static void unlink (PDFDictionary sibling,String siblingKey,
      PDFDictionary parent,String parentKey,PDFDictionary value) {
    PDFDictionary node = sibling != null ? sibling : parent;
    String key = sibling != null ? siblingKey : parentKey;
    if (value == null)
      node.remove (key);
    else
      node.putIndirect (key,value);
  }

  int traverseOutline (Cinderella<PDFDictionary> cinderella){
    PDFDictionary first = (PDFDictionary) get ("First");
    if (first == null)
      return 0;
    // remove prepended [Link Elements] entry (rotorwing200701-dl.pdf)
    first.remove ("Prev");
    Assertions.unexpect (first.contains ("Prev"));
    Assertions.unexpect (((PDFDictionary) get ("Last")).contains ("Next"));
    int count = 0;
    PDFDictionary next;
    for (PDFDictionary item = first;item != null;item = next) {
      next = (PDFDictionary) item.get ("Next");
      if (cinderella.isGood (item)) {
        count += item.traverseOutline (cinderella) + 1;
	// ensure Last entry points to last remaining item, and
	// remove appended [Link Elements] entry (worldtrademag200610-ae.pdf)
        putIndirect ("Last",item);
      }
      else {
        PDFDictionary previous = (PDFDictionary) item.get ("Prev");
        unlink (previous,"Next",this,"First",next);
        unlink (next,"Prev",this,"Last",previous);
      }
    }
    boolean open = getInt ("Count") > 0;
    put ("Count",open ? count : -count);
    return open ? count : 0;
  }

  void traversePageObjects (Handler<PDFDictionary> handler) {
    traversePageTree (handler,true);
  }

  void traversePageNodes (Handler<PDFDictionary> handler) {
    traversePageTree (handler,false);
  }

  void traversePageTree (Handler<PDFDictionary> handler,boolean pageObjectsOnly) {
    boolean isBranch = isOfType ("Pages");
    if (isBranch)
      for (Object kid : (PDFArray) get ("Kids"))
        ((PDFDictionary) kid).traversePageTree (handler,pageObjectsOnly);
    if (!(isBranch && pageObjectsOnly))
      handler.handle (this);
  }

  void handleContentStreams (final Handler<? super PDFStream> handler) {
    PDFDictionary xobjects = (PDFDictionary) get ("XObject");
    if (xobjects != null)
      for (Object object : xobjects) {
        PDFStream xobject = (PDFStream) object;
        Assertions.expect (xobject.isOptionallyOfType ("XObject"));
        if (xobject.isOfSubtype ("Form"))
          handler.handle (xobject);
      }

    PDFDictionary fonts = (PDFDictionary) get ("Font");
    if (fonts != null)
      for (Object object : fonts) {
        PDFDictionary font = (PDFDictionary) object;
        Assertions.expect (font.isOfType ("Font"));
        if (font.isOfSubtype ("Type3"))
          for (Object charProc : (PDFDictionary) font.get ("CharProcs"))
            handler.handle ((PDFStream) charProc);
      }

    PDFDictionary patterns = (PDFDictionary) get ("Pattern");
    if (patterns != null)
      for (Object pattern : patterns)
	if (pattern instanceof PDFStream)
	  handler.handle ((PDFStream) pattern);
  }

  public PDFDictionary getPage (Count pageNumber)
  {
    if (isOfType ("Page"))
      return --pageNumber.count == 0 ? this : null;
    Assertions.expect (isOfType ("Pages"));
    int count = getInt ("Count");

    if (pageNumber.count < 1)
      throw new IndexOutOfBoundsException (pageNumber + " < 1");
    if (pageNumber.count > count)
      {
        pageNumber.count -= count;
        return null;
      }

    PDFArray kids = (PDFArray) get ("Kids");
    for (int i = 0;i < kids.size ();i++)
      {
        PDFDictionary kid = (PDFDictionary) kids.get (i);
        PDFDictionary result = kid.getPage (pageNumber);
        if (result != null)
          return result;
      }
    throw new Error ("Wrong count in page node");
  }

  public PDFDictionary getOrCreateDictionary (String key)
  {
    PDFDictionary dict = (PDFDictionary) get (key);
    if (dict == null)
      {
        dict = new PDFDictionary ();
        put (key,dict);
      }
    return dict;
  }

  public void inherit (BasicPageInfo info)
  {
    put ("MediaBox",new PDFArray (info.mediaBox));
    put ("CropBox",new PDFArray (info.cropBox));
    if (info.resources != get ("Resources"))
      putIndirect ("Resources",info.resources);
    PDFInteger rotate = (PDFInteger) get ("Rotate");
    if (info.rotation != (rotate == null ? 0 : rotate.rotationValue ()))
      put ("Rotate",new PDFInteger (info.rotation * 90));
  }

  public void clear ()
  {
    map.clear ();
  }

  protected String displayString ()
  {
    return "dictionary";
  }

  void buildEditor (Container container,final EditHandler editor)
  {
    Set keys = keys ();
    container.setLayout (new GridLayout (keys.size (),2));
    Iterator iterator = keys.iterator ();
    while (iterator.hasNext ())
      {
        String key = (String) iterator.next ();
        final Label keyLabel = new Label (key);
        Label valueLabel = editor.createLabel
          (getUnresolved (key),'.' + key,
           new Handler<PDFObject> () {
               public void handle (PDFObject o)
               {
                 String key = keyLabel.getText ();
                 if (o == null)
                   remove (key);
                 else
                   put (key,o);
               }
             });
        keyLabel.addMouseListener (new MouseAdapter () {
            public void mouseClicked (MouseEvent me)
            {
              final String key = keyLabel.getText ();
              editor.edit (new PDFName (key) {
                  public void setValue (String value)
                  {
                    put (value,remove (key));
                    keyLabel.setText (value);
                  }
                },"key " + editor.getTitle () + "." + key);
            }
          });
        container.add (keyLabel);
        container.add (valueLabel);
      }
  }

  void add (PDFObject entry)
  {
    put ("",entry);
  }

  public PDFArray getMediaBox ()
  {
    return (PDFArray) getInherited ("MediaBox");
  }

  public PDFArray getCropBox ()
  {
    return (PDFArray) getInherited ("CropBox");
  }

  public PDFObject getInherited (String key)
  {
    PDFObject value = get (key);
    if (value != null)
      return value;
    PDFDictionary parent = (PDFDictionary) get ("Parent");
    return parent == null ? null : parent.getInherited (key);
  }

  public boolean isEmpty ()
  {
    return map.isEmpty ();
  }

  PDFDictionary cloneResources ()
  {
    PDFDictionary clone = new PDFDictionary ((PDFDictionary) get ("Resources"));
    put ("Resources",clone);
    return clone;
  }

  public Map<String,PDFDictionary> inversion ()
  {
    Iterator outerKeys = keys ().iterator ();
    Map<String,PDFDictionary> inversion = new HashMap<String,PDFDictionary> ();
    while (outerKeys.hasNext ())
      {
        String outerKey = (String) outerKeys.next ();
        PDFDictionary innerDictionary = (PDFDictionary) get (outerKey);
        Iterator innerKeys = innerDictionary.keys ().iterator ();
        while (innerKeys.hasNext ())
          {
            String innerKey = (String) innerKeys.next ();
            PDFDictionary newInnerDictionary = inversion.get (innerKey);
            if (newInnerDictionary == null)
              {
                newInnerDictionary = new PDFDictionary ();
                inversion.put (innerKey,newInnerDictionary);
              }
            newInnerDictionary.put
              (outerKey,innerDictionary.getUnresolved (innerKey));
          }
      }
    return inversion;
  }

  void diff (PDFContainer container,String name) throws IOException
  {
    PDFDictionary dictionary = (PDFDictionary) container;
    Set keySetA = keys ();
    Set keySetB = dictionary.keys ();

    Iterator keys = keySetA.iterator ();
    while (keys.hasNext ())
      {
        String key = (String) keys.next ();
        String newName = name + '.' + key;
        PDFObject valueA = get (key);
        PDFObject valueB = dictionary.get (key);

        if (valueB == null)
          System.out.println ("key A diff : " + newName);
        else
          diff (valueA,valueB,newName);
      }

    keys = keySetB.iterator ();
    while (keys.hasNext ())
      {
        String key = (String) keys.next ();
        if (!keySetA.contains (key))
          System.out.println ("key B diff : " + name + '.' + key);
      }
  }

  boolean iterateInParallel (PDFContainer container,PairHandler handler) {
    for (String key : keys ())
      if (!handler.handle (get (key),((PDFDictionary) container).get (key)))
        return false;
    return true;
  }

  // traces back through the page hierarchy to extract the page number
  // returns raw page number beginning with 1
  public int getPageNumber ()
  {
    try {
      if (!isOfType ("Page"))
        throw new RuntimeException ("invalid page reference");
      return ((PDFDictionary) get ("Parent")).getPageOffset (this);
    } catch (Exception e) {
      Options.warn (e.getMessage ());
      return -1;
    }
  }

  private int getPageOffset (PDFDictionary pageTreeNode)
  {
    if (isOfType ("Pages"))
      {
        PDFDictionary parent = (PDFDictionary) get ("Parent");
        int offset = parent == null ? 1 : parent.getPageOffset (this);
        PDFArray kids = (PDFArray) get ("Kids");
        for (int i = 0;i < kids.size ();i++)
          {
            PDFDictionary kid = (PDFDictionary) kids.get (i);
            if (kid == pageTreeNode)
              return offset;
            else if (kid.isOfType ("Page"))
              offset++;
            else if (kid.isOfType ("Pages"))
              offset += kid.getInt ("Count");
            else
              break;
          }
      }
    throw new RuntimeException ("page tree corrupted");
  }

  // form field support

  public String getFullyQualifiedFieldName ()
  {
    String partialName = getTextString ("T");
    PDFDictionary parent = (PDFDictionary) get ("Parent");
    if (parent == null)
      {
        Assertions.unexpect (partialName,null);
        return partialName;
      }
    String parentName = parent.getFullyQualifiedFieldName ();
    return partialName == null ? parentName : parentName + '.' + partialName;
  }

  public GroupAttributes getGroupAttributes ()
  {
    PDFDictionary groupAttributesDictionary = (PDFDictionary) get ("Group");
    if (groupAttributesDictionary == null)
      return null;
    Assertions.expect (groupAttributesDictionary.isOptionallyOfType ("Group"));
    String subtype = groupAttributesDictionary.getName("S");
    if (subtype.equals ("Transparency"))
      return new TransparencyGroupAttributes (groupAttributesDictionary);
    throw new NotImplementedException ("group attributes dictionary of subtype " + subtype);
  }

  void begin ()
  {
    unusedKeys = new HashSet<String> (map.keySet ());
  }

  public Set<String> getUnusedKeys () {
    return new HashSet<String> (unusedKeys);
  }

  public void checkUnused (String table)
  {
    checkUnused (unusedKeys,table);
  }

  private void checkUnused (Set keys,String table)
  {
    if (keys != null && !keys.isEmpty ()) {
        String msg = "dictionary entr" + (keys.size () == 1 ?
                "y " + keys.iterator ().next () : "ies " + keys) + " in Table " + table;
        if (!PDFOptions.ignoreUnknownEntries.isSet ())
          throw new NotImplementedException (msg);
        else
          Options.warn (msg + " not implemented");
    }
  }

  private Set<String> getUnusedInheritedKeys ()
  {
    PDFDictionary parent = (PDFDictionary) get ("Parent");
    Set<String> parentKeys = parent != null ? parent.getUnusedInheritedKeys () : new HashSet<String> ();
    parentKeys.removeAll (keys ());
    parentKeys.addAll (unusedKeys);
    parentKeys.remove ("Kids");
    return parentKeys;
  }

  public void checkUnusedInherited (String table)
  {
    checkUnused (getUnusedInheritedKeys (),table);
  }

  public void use (String key) {
    if (unusedKeys != null)
      unusedKeys.remove (key);
  }

  public void ignore (String key) {
    if (contains (key))
    {
      use (key);
      Options.warn ("dictionary entry " + key + " ignored");
    }
  }

  public void checkOPI () {
    PDFDictionary versionDictionary = (PDFDictionary) get ("OPI");
    if (versionDictionary == null)
      return;
    Iterator keys = versionDictionary.keys ().iterator();
    if (!keys.hasNext ())
    {
      // sourcebook2005-ae_33.pdf
      Options.warn ("empty OPI version dictionary");
      return;
    }
    String key = (String) keys.next ();
    Assertions.unexpect (keys.hasNext ());
    if (!(key.equals ("1.3") || key.equals ("2.0")))
      throw new NotImplementedException ("OPI version " + key);
    PDFDictionary dictionary = (PDFDictionary) versionDictionary.get (key);
    Assertions.expect (dictionary.isOptionallyOfType("OPI"));
    double dictionaryVersion = dictionary.getDouble ("Version");
    double keyVersion = Double.parseDouble (key);
    if (dictionaryVersion != keyVersion)
    {
      String message = "OPI version number mismatch : " + dictionaryVersion + "/" + key;
      // /tcprojects/hearst/smartmoney/inbox/24717/smartmoney200407-ae.pdf has 1.300003 instead of 1.3
      if (Math.abs (dictionaryVersion - keyVersion) > 1e-5)
        throw new Error (message);
      Options.warn (message);
    }
    // just to check it's a simple file name, not an embedded file stream that we could use
    new PDFFileSpecification (dictionary.get ("F"));
  }

  public void handleVariableText ()
  {
    getInherited ("DA");
    getInherited ("Q");
    // This occurs in veroi_fi.pdf but doesn't belong here;
    // see implementation note 105 for Section 8.6.2.
    getInherited ("DR");
  }

  public PDFDictionary clone () {
    return new PDFDictionary (this);
  }

  public boolean assimilate (PDFContainer container) throws IOException {
    PDFDictionary dictionary = (PDFDictionary) container;
    for (String key : keys ())
      if (!dictionary.contains (key)) {
        remove (key);
        return true;
      }

    for (String key : dictionary.keys ())
      if (!dictionary.get (key).equals (get (key))) {
        put (key,dictionary.get (key));
        return true;
      }

    return false;
  }

  // workaround for reverse /OrigRect entry in Phys-16.pdf
  final static PDFName origRectName = new PDFName ("OrigRect");
  public void ignoreReverseEntries() {
    if (map.containsValue (origRectName)) {
      Options.warn ("ignoring invalid reverse OrigRect entry");
      Iterator keys = map.keySet().iterator ();
      while (keys.hasNext ()) {
        String key = (String) keys.next ();
        if (map.get (key).equals (origRectName))
          ignore (key);
      }
    }
  }

  public void makeIndirect (String key) {
    PDFObject value = map.get (key);
    if (value != null && !(value instanceof PDFIndirectObject))
      putIndirect (key,value);
  }
  
  public boolean equalsDictionary (PDFDictionary dictionary) {
	  return contains (this,dictionary) && contains (dictionary,this);
  }
  
  public static boolean contains (PDFDictionary a,PDFDictionary b) {
	  for (String key : a.map.keySet ())
		  if (!a.get (key).equals (b.get (key)))
			  return false;
	  return true;
  }
}
