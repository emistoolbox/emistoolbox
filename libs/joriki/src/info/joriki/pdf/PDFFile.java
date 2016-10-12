/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

/*
  The position in the file is transient.
  It can be changed at any point by an object
  or a stream being read.
  invisible paragraph : PDF spec, p. 554 (570)
*/

package info.joriki.pdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import info.joriki.awt.image.ImageBuffer;

import info.joriki.awt.image.jpeg.JPEGDecoder;

import info.joriki.io.AbstractInputStreamProvider;
import info.joriki.io.ProtocolHandlers;
import info.joriki.io.Util;
import info.joriki.io.SeekableFile;
import info.joriki.io.FullySeekable;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.LimitedInputStream;
import info.joriki.io.PersistentInputStream;
import info.joriki.io.ReadableBufferedInputStream;

import info.joriki.util.Range;
import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Version;
import info.joriki.util.Assertions;
import info.joriki.util.OptionHandler;
import info.joriki.util.ArgumentIterator;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

import info.joriki.crypto.StreamCypher;
import info.joriki.crypto.DecypheringInputStream;

public class PDFFile implements ObjectTypes, PDFOptions
{
  FullySeekable file;
  PDFFileObjectReader reader;

  PDFDocument document;
  
  public PDFFile (String url) throws IOException
  {
    if (url.indexOf ("://") == -1)
      init (new File (url));
    else
      init (ProtocolHandlers.getFullySeekable (url));
  }

  public PDFFile (File file) throws IOException
  {
    init (file);
  }

  public PDFFile (FullySeekable file) throws IOException
  {
    init (file);
  }

  static boolean isLine (int b)
  {
    return b == '\r' || b == '\n';
  }

  boolean at (String str) throws IOException
  {
    byte [] ref = str.getBytes ();
    int i = 0;
    while (i < ref.length && file.read () == (ref [i++] & 0xff))
      ;
    boolean at = i == ref.length;
    if (!at)
      file.seekRelative (-i);
    return at;
  }

  void skip (String str) throws IOException
  {
    skipSpace ();

    byte [] ref = str.getBytes ();
    for (int i = 0;i < ref.length;i++)
      if (file.read () != ref [i])
        throw new StreamCorruptedException ("PDF file structure corrupted");
  }

  void skipSpace () throws IOException
  {
    while (General.isWhiteSpace (file.read ()))
      ;
    backUp ();
  }

  final void backUp () throws IOException
  {
    file.seekRelative (-1);
  }

  final void skip () throws IOException
  {
    file.read ();
  }

  final int peek () throws IOException
  {
    int peek = file.read ();
    if (peek != -1)
      file.seekRelative (-1);
    return peek;
  }

  int readInt () throws IOException
  {
    skipSpace ();

    boolean negative = file.read () == '-';
    if (!negative)
      backUp ();

    int num = 0;
    int digit;

    while (General.isDigit (digit = file.read ()))
      {
        num *= 10;
        num += digit - '0';
      }

    backUp ();

    return negative ? -num : num;
  }

  PDFDictionary potentialStream (PDFDictionary dictionary) throws IOException
  {
    skipSpace ();

    if (!at ("stream"))
      return dictionary;

    if (peek () == '\r')
      skip ();
    if (peek () == '\n')
      skip ();

    final StreamCypher cypher = crypt.getCypher ();
    final PDFStream stream = new PDFStream ();
    stream.map = dictionary.map;
    stream.streamProvider = new AbstractInputStreamProvider () {
      long begin = file.getFilePointer ();
      boolean first = true;
      public InputStream getInputStream ()
      {
        InputStream in = new ReadableBufferedInputStream
        (new LimitedInputStream (new PersistentInputStream (file,begin),stream.getInt ("Length")));
        if (cypher == null)
          return in;
        if (first)
          first = false;
        else
          cypher.reset ();
        return new DecypheringInputStream (in,cypher);
      }
    };
    file.seekRelative (stream.getInt ("Length"));
    skip ("endstream");
    return stream;
  }

  Crypt crypt = new Crypt (null);
  
  abstract class CrossReference
  {
    class Section extends Range
    {
      int position;
      
      Section (int start,int length,int position)
      {
        super (start,start + length - 1);
        this.position = position;
      }
    }

    List sections = new ArrayList ();
    int entryLength;

    PDFObject getObject (PDFObjectIdentifier identifier) throws IOException
    {
      for (int i = 0;i < sections.size ();i++)
      {
        Section section = (Section) sections.get (i);
        if (section.contains (identifier.object))
        {
          PDFObject object = getObject (identifier,section.position + (identifier.object - section.beg) * entryLength);
          /* We can get null for three different reasons here.
          The object could be marked as free, or it could
          have a lower or higher generation number. If I
          understand the spec correctly, all of these mean
          that the object is undefined and the reference
          should be treated as a reference to a null object.
          This also seems to be ghostscript's interpretation. */
          return object != null ? object : PDFNull.nullObject;
        }
      }
      return null;
    }

    abstract protected PDFObject getObject (PDFObjectIdentifier identifier,int pos) throws IOException;
  }

  final static int xrefEntryLength = 20;

  class CrossReferenceTable extends CrossReference
  {
    CrossReferenceTable () throws IOException
    {
      entryLength = xrefEntryLength;
      for (;;)
      {
        skipSpace ();
        if (!General.isDigit (peek ()))
          break;
        int start = readInt ();
        int length = readInt ();
        // shouldn't be necessary, but ICC-1_1998-09.pdf has an extra space
        skipSpace ();
        int position = (int) file.getFilePointer ();
        sections.add (new Section (start,length,position));
        file.seekRelative (entryLength * length);
      }
    }

    protected PDFObject getObject (PDFObjectIdentifier identifier,int pos) throws IOException
    {
      file.seek (pos);
      int offset = readInt ();
      if (readInt () != identifier.generation)
        return null;
      skip ();
      int type = file.read ();
      switch (type)
      {
      case 'f' : return null;
      case 'n' : return readObject (identifier,offset);
      default  : throw new Error ("invalid entry type " + (char) type);
      }
    }
  }

  final static int FREE         = 0;
  final static int UNCOMPRESSED = 1;
  final static int COMPRESSED   = 2;

  final static int [] [] fieldDefaults = {{1,-1,-1},  // FREE
                                          {1,-1,0},   // UNCOMPRESSED
                                          {1,-1,0}};  // COMPRESSED
  // The default value zero for the third field in a compressed entry
  // isn't documented, but used in etech_spring2004.pdf. It's safe to
  // use this because of the assertion in ObjectStream.seek.

  Map objectStreamCache = new HashMap ();

  class CrossReferenceStream extends CrossReference
  {
    class ObjectStream
    {
      SeekableByteArray data;
      PDFIndirectObjectReader reader;
      int [] numbers;
      int [] offsets;
      
      ObjectStream (PDFStream stream) throws IOException
      {
        Assertions.expect (stream.isOfType ("ObjStm"));
        int first = stream.getInt ("First");
        int n = stream.getInt ("N");
        data = new SeekableByteArray (stream.getData ("3.14"));
        reader = new PDFIndirectObjectReader (data,PDFFile.this);
        numbers = new int [n];
        offsets = new int [n];
        for (int i = 0;i < n;i++)
        {
          numbers [i] = reader.tok.nextInt ();
          offsets [i] = reader.tok.nextInt () + first;
        }
      }
      
      void seek (int num,int index)
      {
        Assertions.expect (num,numbers [index]);
        data.seek (offsets [index]);
      }
    }
    
    int [] fieldSizes;
    int [] fields;
    byte [] data;

    CrossReferenceStream (PDFStream stream) throws IOException
    {
      Assertions.expect (stream.isOfType ("XRef"));
      fieldSizes = stream.getIntArray ("W");
      int [] indices = stream.getIntArray ("Index");
      int size = stream.getInt ("Size");
      data = stream.getData ("3.15");
      Assertions.expect (fieldSizes.length,3);
      for (int i = 0;i < fieldSizes.length;i++)
        entryLength += fieldSizes [i];
      fields = new int [fieldSizes.length];
      if (indices == null)
        indices = new int [] {0,size};
      Assertions.expect (indices.length & 1,0);
      for (int i = 0,position = 0;i < indices.length;)
      {
        int start = indices [i++];
        int length = indices [i++];
        sections.add (new Section (start,length,position));
        position += length * entryLength;
      }
    }

    protected PDFObject getObject (PDFObjectIdentifier identifier,int pos) throws IOException
    {
      if (identifier.generation != 0)
        throw new NotTestedException ("non-zero generation number in cross reference stream");

      for (int i = 0;i < fields.length;i++)
      {
        int length = fieldSizes [i];
        if (length != 0)
          fields [i] = Util.readInteger (data,pos,length);
        // for i == 0, the previous value of fields [0] doesn't matter
        else if ((fields [i] = fieldDefaults [fields [0]] [i]) == -1)
          throw new Error ("zero length for field without default value");
        pos += length;
      }
      
      int type = fields [0];
      
      switch (type)
      {
      case FREE :
        return null;
      case UNCOMPRESSED :
        return fields [2] == identifier.generation ? readObject (identifier,fields [1]) : null;
      case COMPRESSED :
        if (identifier.generation != 0)
          return null;
        int stream = fields [1];
        int index = fields [2];
        PDFObjectIdentifier streamID = new PDFObjectIdentifier (stream);
        ObjectStream objectStream = (ObjectStream) objectStreamCache.get (streamID);
        if (objectStream == null)
        {
          objectStream = new ObjectStream
          ((PDFStream) PDFFile.this.getObject (streamID));
          objectStreamCache.put (streamID,objectStream);
        }
        objectStream.seek (identifier.object,index);
        return objectStream.reader.readExternalObject ();
      default :
        throw new NotImplementedException ("entry type " + type);
      }
    }
  }
  
  PDFObject readObject (PDFObjectIdentifier identifier,int offset) throws IOException
  {
    file.seek (offset);
    Assertions.expect (readInt (),identifier.object);
    Assertions.expect (readInt (),identifier.generation);
    skip ("obj");
    return reader.readExternalObject ();
  }
  
  List crossReferences = new ArrayList ();
  Map objects = new HashMap ();
  int nobjects;
  
  PDFObject getObject (PDFObjectIdentifier identifier) throws IOException
  {
    if (Options.tracing)
      System.out.println ("getting " + identifier);

    if (identifier.object >= nobjects)
      return PDFNull.nullObject; 
    
    crypt.object = identifier.object;
    crypt.generation = identifier.generation;

    PDFObject object = (PDFObject) objects.get (identifier);
    if (object == null)
    {
      if (Options.tracing)
        System.out.println ("reading " + identifier);
      
      long pos = file.getFilePointer ();
      
      for (int i = 0;i < crossReferences.size () && object == null;i++)
        object = ((CrossReference) crossReferences.get (i)).getObject (identifier);
      file.seek (pos);
      
      if (object == null)
        object = PDFNull.nullObject;
      objects.put (identifier,object);
    }
    return object;
  }

  void backUpTo (String ref) throws IOException
  {
    do
      {
        backUp ();
        while (isLine (peek ()))
          backUp ();
        while (!isLine (peek ()))
          backUp ();
        skip ();
      }
    while (!at (ref));
  }

  void init (File file) throws IOException
  {
    if (loadIntoMemory.isSet ())
    {
      // read the file into a byte array; *much* quicker
      byte [] buf = new byte [(int) file.length ()];
      FileInputStream in = new FileInputStream (file);
      try { new DataInputStream (in).readFully (buf); }
      finally { in.close (); }
      init (new info.joriki.io.SeekableByteArray (buf));
    }
    else
      init (new SeekableFile (file,"r"));
  }

  void init (FullySeekable file) throws IOException
  {
    this.file = file;

    InputStream in = Util.toInputStream (file);
    file.seek (0);
    String line = new BufferedReader (new InputStreamReader (in)).readLine ();
    Assertions.expect (line.substring (0,5),"%PDF-");
    Version version = new Version (line.substring (5).trim ());
    reader = new PDFFileObjectReader (in,this);
    file.seekFromEnd (0);

    backUpTo ("startxref");
    file.seek (readInt ());
    PDFDictionary trailer = null;

    for (;;)
    {
      PDFDictionary nextTrailer = null;
      skipSpace (); // smartmoney200405.pdf
      boolean atStream = !at ("xref");
      
      if (!atStream)
      {
        crossReferences.add (new CrossReferenceTable ());
        skip ("trailer");
        nextTrailer = (PDFDictionary) reader.readExternalObject ();
        PDFInteger xrefStream = (PDFInteger) nextTrailer.get ("XRefStm");
        atStream = xrefStream != null;
        if (atStream)
          file.seek (xrefStream.val);
      }
      
      if (atStream)
      {
        readInt (); // num
        readInt (); // gen
        skip ("obj");
        PDFStream stream = (PDFStream) reader.readExternalObject ();
        if (nextTrailer == null)
        {  
          nextTrailer = stream;
          stream.use ("Prev"); // used below, after constructor calls checkUnused
        }
        crossReferences.add (new CrossReferenceStream (stream));
      }
      
      if (trailer == null)
        trailer = nextTrailer;
      
      PDFInteger prev = (PDFInteger) nextTrailer.get ("Prev");
      if (prev == null)
        break;
      file.seek (prev.val);
    }
    
    // everything up to here had to be direct objects (see Note before Table 3.15)
    nobjects = trailer.getInt ("Size");
    
    PDFArray id = (PDFArray) trailer.get ("ID");
    PDFDictionary encryptionDictionary = (PDFDictionary) trailer.get ("Encrypt");
    byte [] encryptionKey = null;
    if (encryptionDictionary != null)
    {
      PDFName filter = (PDFName) encryptionDictionary.get ("Filter");
      SecurityHandler securityHandler;
      
      if (filter.getName ().equals ("Standard"))
        securityHandler = new StandardSecurityHandler (encryptionDictionary,id);
      else
        throw new NotImplementedException ("non-standard security handler " + filter);
      
      int n = encryptionDictionary.getInt ("Length",40);
      Assertions.expect (n & 7,0);
      Assertions.limit (n,40,128);
      n >>= 3;

      int algorithm = encryptionDictionary.getInt ("V",0);
      switch (algorithm)
      {
      case 1 :
        Assertions.expect (n,5);
        if (encryptionDictionary.contains ("Length")) // UMNEastBank2002.pdf
          Options.warn ("redundant encryption key length specification");
        break;
      case 2 :
        break;
      default :
        throw new NotImplementedException
        ((algorithm == 3 ? "Thanks to the U.S. Department of Commerce, " :
        "") + "encryption algorithm " + algorithm);
      }

      encryptionKey = securityHandler.generateEncryptionKey (); // uses empty user password

      if (encryptionKey == null)
        throw new IOException ("Password required.");
      
      
      encryptionDictionary.checkUnused ("3.18-3.21");
      
      crypt = new Crypt (encryptionKey);
    }
    
    PDFDictionary root = (PDFDictionary) trailer.get ("Root");
    PDFDictionary info = (PDFDictionary) trailer.get ("Info");

    // ZA0506_0084.PDF
    trailer.ignore ("QuadDocName");
    trailer.ignore ("QuadDocStamp");

    // BDK-Anträge
    trailer.ignore ("DocChecksum");

    trailer.checkUnused ("3.13");
    
    Assertions.expect (root.isOfType ("Catalog"));
    
    if (root.contains ("Version"))
    {
      Version rootVersion = new Version (root.getName ("Version"));
      if (rootVersion.moreRecentThan (version))
        version = rootVersion;
    }
    
    document = new PDFDocument (root,info,version,id);
  }
  
  public PDFDocument getDocument ()
  {
    return document;
  }
  
  public void close () throws IOException
  {
    file.close ();
  }
  
  public static void main (String [] args) throws IOException
  {
    File inputFile = Options.getInputFile (args);
    final PDFFile file = inputFile == null ? null : new PDFFile (inputFile);
    final PDFDocument document = file == null ? null : file.getDocument ();
    new Options (PDFFile.class,"<PDF file>",null).parse (args,new OptionHandler () {
        PDFStream stream;
        info.joriki.sfnt.SFNTFile fontFile;
        String id;
        String dump;
        int num,gen;

        public boolean handle (char action,ArgumentIterator args)
        {
          PDFDictionary pageDictionary;
          try {
            switch (action)
              {
              case 'a' : // list annotations
                new DocumentTraversal ().traversePages (document,new PageHandler () {
                    public boolean handle (PageInfo info)
                    {
                      if (info.annotations != null)
                        {
                          System.out.println ("annotations on page " + info.pageNumber);
                          for (int i = 0;i < info.annotations.length;i++)
                          {  
                            PDFAnnotation annotation = info.annotations [i];
                            System.out.print (annotation.type);
                            if (annotation.action != null)
                              System.out.print (" : " + annotation.action.type);
                            System.out.println ();
                          }
                        }
                      return false;
                    }
                    public void finish () {}
                  });
                break;
              case 'b' : // turn page into form
                int page = args.nextInt ();
                PDFDictionary pageObject = document.getPage (page);
                PDFDictionary resources = (PDFDictionary) pageObject.get ("Resources");
                PDFArray mediaBox = pageObject.getMediaBox ();
                PDFStream form = new PDFStream (Util.toByteArray (pageObject.getContentStream ()));
                form.put ("Type","XObject");
                form.put ("Subtype","Form");
                form.put ("BBox",mediaBox);
                PDFDictionary parent = (PDFDictionary) pageObject.get ("Parent");
                PDFDictionary newPageObject = new PDFDictionary ("Page");
                newPageObject.putIndirect ("Parent",parent);
                newPageObject.put ("MediaBox",mediaBox);
                newPageObject.put ("Resources",resources);

                newPageObject.putIndirect ("Contents",new PDFStream ("/Form1 Do".getBytes ()));

                PDFDictionary forms = (PDFDictionary) resources.get ("XObject");
                if (forms == null)
                  {
                    forms = new PDFDictionary ();
                    resources.put ("XObject",forms);
                  }

                forms.putIndirect ("Form1",form);

                PDFArray kids = (PDFArray) parent.get ("Kids");
                for (int i = 0;i < kids.size ();i++)
                  if (kids.get (i) == pageObject)
                    {
                      kids.set (i,new PDFIndirectObject (newPageObject));
                      break;
                    }
                break;
              case 'c' : // dump content stream of a page
                pageDictionary = (PDFDictionary) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                Util.copy (pageDictionary.getContentStream (),args.nextString ());
                break;
              case 'd' : // dump an sfnt table from a font
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                fontFile = new info.joriki.sfnt.SFNTFile (new info.joriki.io.SeekableByteArray (stream.getData ()));
                id = args.nextString ();
                dump = args.nextString ();
                info.joriki.io.Util.dump (fontFile.getTable (id),dump);
                break;
              case 'f' : // list fonts
              case 'F' : // list fonts on first occurrence
                final Set fontSet = new HashSet ();
                final boolean firstOccurenceOnly = action == 'F';
                new DocumentTraversal ().traversePages (document,new PageHandler () {
                    ResourceResolver resourceResolver = new ResourceResolver ();
                    public boolean handle (PageInfo info)
                    {
                      PDFDictionary fonts = (PDFDictionary) info.resources.get ("Font");
                      System.out.println ("Fonts for page " + info.pageNumber + " :");
                      if (fonts == null)
                        return false;
                      Iterator keyIterator = fonts.keys ().iterator ();
                      while (keyIterator.hasNext ())
                        {
                          String key = (String) keyIterator.next ();
                          PDFFont font = (PDFFont) resourceResolver.getCachedObject
                            (FONT,fonts.get (key));
                          if (firstOccurenceOnly)
                            {
                              if (fontSet.contains (font))
                                continue;
                              fontSet.add (font);
                            }
                          System.out.println ("  " + key + " : " + font.getName () + " (" + font.fontDictionary.get ("Subtype") + ")");
                        }
                      return false;
                    }
                    public void finish () {}
                  });
                break;
               case 'i' : // info
                 System.out.println (document.getPageCount () + " pages.");
                 new DocumentTraversal ().traversePages (document,new PageHandler () {
                   public boolean handle (PageInfo info)
                   {
                     System.out.println ("page " + info.pageNumber + " :");
                     PDFDictionary xobjects = (PDFDictionary) info.resources.get ("XObject");
                     int nimages = 0;
                     if (xobjects != null)
                      {
                       Iterator keys = xobjects.keys ().iterator ();
                       while (keys.hasNext ())
                        {
                         PDFStream xobject = (PDFStream) xobjects.get ((String) keys.next ());
                         Assertions.expect (xobject.isOfType ("XObject"));
                         if (xobject.isOfSubtype ("Image"))
                         {
                           nimages++;
                           System.out.println ("color space : " + xobject.get ("ColorSpace")); 
                         }
                       }
                     }
                     System.out.println ("  " + nimages + " images");
                     return false;
                   }
                    public void finish () {}
                  });
                break;
               case 'J' : // extract JPEG colors
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                JPEGDecoder decoder = new JPEGDecoder (false,false);
                decoder.setSource (stream.getInputStream ());
                ImageBuffer imageBuffer = new ImageBuffer ();
                decoder.startProduction (imageBuffer);
                System.out.println ("width : " + imageBuffer.getWidth ());
                System.out.println ("height : " + imageBuffer.getHeight ());
                DataOutputStream dos = new DataOutputStream (new FileOutputStream (args.nextString ()));
                try {
                  int [] pixels = imageBuffer.getPixels ();
                  for (int pixel : pixels)
                    dos.writeInt (pixel);
                } catch (ClassCastException cce) {
                  dos.write (imageBuffer.getBytes ());
                }
                dos.close ();
                break;
              case 'j' : // replace stream contents
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                stream.setAndCompressData (Util.undump (args.nextString ()));
                break;
              case 'k' : // replace page contents
                pageDictionary = (PDFDictionary) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                Assertions.expect (pageDictionary.isOfType ("Page"));
                PDFStream pageStream = new PDFStream (Util.undump (args.nextString ()));
                pageDictionary.putIndirect ("Contents",pageStream);
                break;
              case 'l' : // look for a certain indirect object
                // This is a depth-first search. Breadth first would
                // make the path found from root to the desired object
                // shorter, but would make it more difficult to keep track.
                // Not worth the hassle since this will most likely never
                // be used again.
                num = args.nextInt ();
                gen = args.nextInt ();
                PDFObjectIdentifier ident = new PDFObjectIdentifier (num,gen);
                Stack iteratorStack = new Stack ();
                Stack idStack = new Stack ();
                Set examined = new HashSet ();
                iteratorStack.push (document.getRoot ().unresolvedIterator ());
                idStack.push ("root");
                while (!iteratorStack.isEmpty ())
                {
                  Iterator iterator = (Iterator) iteratorStack.pop ();
                  if (iterator.hasNext ())
                  {
                    Object object = iterator.next ();
                    iteratorStack.push (iterator);
                    Object identifier = null;
                    if (object instanceof PDFFileObject)
                    {
                      PDFFileObject fileObject = (PDFFileObject) object;
                      if (fileObject.identifier.equals (ident))
                        System.out.println (idStack);
                      identifier = fileObject.identifier;
                      object = fileObject.resolve ();
                    }
                    if (object instanceof PDFContainer && examined.add (object))
                    {
                      idStack.push (identifier);
                      iteratorStack.push (((PDFContainer) object).unresolvedIterator ());
                    }
                  }
                  else
                    idStack.pop ();
                }
                break;
              case 'n' : // dump page content stream by number
                final int wanted = args.nextInt ();
                final String filename = args.nextString ();
                new DocumentTraversal ().traversePages (document,new PageHandler () {
                    public boolean handle (PageInfo info)
                    {
                      if (info.pageNumber == wanted)
                        try {
                          Util.copy (info.pageObject.getContentStream (),filename);
                          return true;
                        } catch (IOException ioe) { ioe.printStackTrace (); }
                      return false;
                    }
                    public void finish () {}
                  });
                break;
              case 'N' : // dump all page content streams
                final String prefix = args.nextString ();
                new DocumentTraversal ().traversePages (document,new PageHandler () {
                  public boolean handle (PageInfo info)
                  {
                    try {
                      Util.copy (info.pageObject.getContentStream (),prefix + "_" + info.pageNumber + ".txt");
                    } catch (IOException ioe) { ioe.printStackTrace (); }
                    return false;
                  }
                  public void finish () {}
                });
                break;
              case 'o' : // write result
                new PDFWriter (args.nextString ()).write (document);
                break;
              case 'p' : // print stream
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                info.joriki.io.Util.copy (stream.getInputStream (),System.out);
                break;
              case 'r' : // dump a raw sfnt table from a font
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                fontFile = new info.joriki.sfnt.SFNTFile (new info.joriki.io.SeekableByteArray (stream.getData ()));
                id = args.nextString ();
                dump = args.nextString ();
                info.joriki.io.Util.dump (fontFile.getRawData (id),dump);
                break;
              case 's' : // strip an sfnt table from a font
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                fontFile = new info.joriki.sfnt.SFNTFile (new info.joriki.io.SeekableByteArray (stream.getData ()));
                fontFile.removeTable (args.nextString ());
                stream.setFontData (info.joriki.io.Util.toByteArray (fontFile));
                break;
              case 't' : // touch an sfnt table from a font
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                fontFile = new info.joriki.sfnt.SFNTFile (new info.joriki.io.SeekableByteArray (stream.getData ()));
                fontFile.getTable (args.nextString ());
                stream.setFontData (info.joriki.io.Util.toByteArray (fontFile));
                break;
              case 'w' : // touch all sfnt tables in a font
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                fontFile = new info.joriki.sfnt.SFNTFile (new info.joriki.io.SeekableByteArray (stream.getData ()));
                fontFile.rewrite ();
                stream.setFontData (info.joriki.io.Util.toByteArray (fontFile));
                break;
              case 'x' : // extract stream
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                info.joriki.io.Util.dump (stream.getData (),args.nextString ());
                break;
              case 'X' : // extract stream
                num = args.nextInt ();
                gen = args.nextInt ();
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (num,gen));
                info.joriki.io.Util.dump (stream.getData (),args.nextString ());
                break;
              case 'y' : // extract undecoded stream
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                Util.copy (stream.streamProvider.getInputStream (),args.nextString ());
                break;
              case 'z' : // extract undecrypted stream
                file.crypt = new Crypt (null);
                stream = (PDFStream) file.getObject (new PDFObjectIdentifier (args.nextInt ()));
                Util.copy (stream.getInputStream (),args.nextString ());
                break;
              default :
                return false;
              }
          } catch (IOException ioe) {
            ioe.printStackTrace ();
          }
          return true;
        }
      });
  }
}
