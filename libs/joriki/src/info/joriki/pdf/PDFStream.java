/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Random;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import info.joriki.awt.SimpleFileDialog;
import info.joriki.crypto.EncypheringOutputStream;
import info.joriki.crypto.StreamCypher;
import info.joriki.io.EmptyInputStreamProvider;
import info.joriki.io.Util;
import info.joriki.io.Readable;
import info.joriki.io.Streamable;
import info.joriki.io.InputStreamProvider;
import info.joriki.io.FileInputStreamProvider;
import info.joriki.io.ByteArrayInputStreamProvider;
import info.joriki.io.StreamableInputStreamProvider;
import info.joriki.util.Assertions;

public class PDFStream extends PDFDictionary
{
  private static final byte [] streamPrefix = "stream\n".getBytes ();    // newline required
  private static final byte [] streamSuffix = "\nendstream".getBytes (); // newline recommended

  InputStreamProvider streamProvider;
  boolean isFont;

  public PDFStream ()
  {
    this (new EmptyInputStreamProvider (),0);
  }

  public PDFStream (byte [] data)
  {
    this (Util.compress (data),"FlateDecode");
  }

  public PDFStream (byte [] data,String ... filters)
  {
    this (new ByteArrayInputStreamProvider (data),data.length);
    PDFArray filterArray = new PDFArray ();
    for (int i = 0;i < filters.length;i++)
      filterArray.add (new PDFName (filters [i]));
    map.put ("Filter",filterArray);
  }
  
  public PDFStream (InputStreamProvider streamProvider,int length)
  {
    this.streamProvider = streamProvider;
    setLength (length);
  }

  public PDFStream (byte [] data,int width,int height,int bitsPerComponent,
		    int ncomponents,PDFObject colorSpace)
  {
    this (data,width,height,bitsPerComponent,ncomponents,colorSpace,true);
  }
  
  // construct an image XObject
  public PDFStream (byte [] data,int width,int height,int bitsPerComponent,
      int ncomponents,PDFObject colorSpace,boolean deflated)
  {
    super ("XObject");
    put ("Subtype","Image");
    put ("Width",width);
    put ("Height",height);
    if (colorSpace == null)
    {
      Assertions.expect (ncomponents,1);
      Assertions.expect (bitsPerComponent,1);
      put ("ImageMask",new PDFBoolean (true));
    }
    else
    {
      put ("BitsPerComponent",bitsPerComponent);
      put ("ColorSpace",colorSpace instanceof PDFName ? colorSpace : new PDFIndirectObject (colorSpace));
    }
    if (deflated) {
      put ("Filter","FlateDecode");
      PDFDictionary decodeParameters = new PDFDictionary ();
      decodeParameters.put ("Predictor",15);
      decodeParameters.put ("Columns",width);
      if (ncomponents != 1)
        decodeParameters.put ("Colors",ncomponents);
      if (bitsPerComponent != 8)
        decodeParameters.put ("BitsPerComponent",bitsPerComponent);
      put ("DecodeParms",decodeParameters);
    }
    useData (data);
  }

  public PDFStream (PDFStream stream) {
    super (stream);
    streamProvider = stream.streamProvider;
    isFont = stream.isFont;
  }

  private void removeFilters () {
    remove ("Filter");
    remove ("DecodeParms");
  }
  
  public void setStreamable (Streamable streamable) {
    streamProvider = new StreamableInputStreamProvider (streamable);
    remove ("Length");
    removeFilters ();
  }
  
  protected boolean write (PDFObjectWriter writer) throws IOException
  {
    byte [] data = null;
    boolean hasLength = contains ("Length");
    if (!hasLength) {
      data = getData ();
      if (isFont)
        setFontLength (data.length);
      setLength (data.length);
    }

    super.write (writer);
    writer.write (streamPrefix);
    StreamCypher cypher = writer.crypt.getCypher ();
    OutputStream out = cypher == null ? writer : new EncypheringOutputStream (writer,cypher);
    if (data != null)
      out.write (data);
    else
      Util.copy (streamProvider.getInputStream (),out);
    writer.write (streamSuffix);
    if (!hasLength)
      remove ("Length");
    return false; // endstreamendobj works, but causes warnings in ghostview
  }

  public InputStream getInputStream () throws IOException
  {
    return getFilteredInputStream (streamProvider.getInputStream ());
  }
  
  public InputStream getInputStream (String table) throws IOException
  {
    InputStream inputStream = getInputStream ();
    checkUnused (table);
    return inputStream;
  }

  public Readable getRawReadable () throws IOException {
    return Util.toReadable (streamProvider.getInputStream ());
  }
  
  public int getLength () throws IOException
  {
    return contains ("Length") ? getInt ("Length") : streamProvider.toByteArray ().length;
  }

  public void setLength (int length)
  {
    map.put ("Length",new PDFInteger (length));
  }

  public void setFontLength (int length)
  {
    map.put ("Length1",new PDFInteger (length));
  }

  public byte [] getData () throws IOException
  {
    return contains ("Filter") ? Util.toByteArray (getInputStream ()) : streamProvider.toByteArray ();
  }
  
  public byte [] getData (String table) throws IOException
  {
    byte [] data = getData ();
    checkUnused (table);
    return data;
  }

  private void useData (byte [] data)
  {
    streamProvider = new ByteArrayInputStreamProvider (data);
    setLength (data.length);
  }

  public void setData (byte [] data)
  {
    removeFilters ();
    useData (data);
  }

  public void setFontData (byte [] data)
  {
    setFontLength (data.length);
    setAndCompressData (data);
  }

  public void setAndCompressData (byte [] data)
  {
    map.put ("Filter",new PDFName ("FlateDecode"));
    useData (Util.compress (data));
  }
  
  // check that this is a valid form stream
  public void formCheck ()
  {
    // The latter is actually not optional, but I had a case
    // where it was missing. The GhostScript source says that
    // these are generated by Acrobat Distiller!
    Assertions.expect (isOptionallyOfType ("XObject"));
    Assertions.expect (isOptionallyOfSubtype ("Form"));
    Assertions.expect (getInt ("FormType",1),1);
  }

  public String toString ()
  {
    return super.toString () + "\nstream\n...\nendstream\n";
  }

  public void buildEditor (Container container,final EditHandler editor)
  {
    container.setLayout (new BorderLayout ());
    Panel dictionaryPanel = new Panel ();
    super.buildEditor (dictionaryPanel,editor);
    container.add (dictionaryPanel,"Center");
    ActionListener actionListener = new ActionListener () {
      public void actionPerformed (ActionEvent ae)
      {
        String command = ae.getActionCommand ();
        try {
          String name = editor.getTitle ();
          if (command.equals ("Save"))
            name = new SimpleFileDialog ().getPathname (SimpleFileDialog.SAVE,"Saving contents of stream " + name,name);
          File file = new File (name);
          Util.dump (getData (),file);
          if (command.equals ("Edit")) {
            streamProvider = new FileInputStreamProvider (file);
            if (!new PDFName ("DCTDecode").equals (get ("Filter")))
              removeFilters ();
            remove ("Length");
            isFont = contains ("Length1");
//          built-in terminal emacs
//          Runtime.getRuntime ().exec (new String [] {"osascript","-e","tell app \"Terminal\" to do script \"emacs '" + file.getCanonicalPath () + "'\""});
            Runtime.getRuntime ().exec (new String [] {"open","-a","Aquamacs Emacs",file.getCanonicalPath ()});
          }
        } catch (IOException ioe) {
          ioe.printStackTrace ();
        }
      }
    };
    Panel buttonPanel = new Panel (new GridLayout (0,1));
    Button editButton = new Button ("Edit");
    editButton.addActionListener (actionListener);
    buttonPanel.add (editButton);
    Button saveButton = new Button ("Save");
    saveButton.addActionListener (actionListener);
    buttonPanel.add (saveButton);
    Button loadButton = new Button ("Load");
    loadButton.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent e) {
        try {
          setData (Util.undump (new SimpleFileDialog ().getPathname (SimpleFileDialog.LOAD,"file to load into stream " + editor.getTitle ())));
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    buttonPanel.add (loadButton);
    Button clearButton = new Button ("Clear");
    clearButton.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent e) {
        setData (new byte [0]);
      }
    });
    buttonPanel.add (clearButton);
    Button randomizeButton = new Button ("Randomize");
    randomizeButton.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent e) {
        try {
          byte [] data = getData ();
          new Random ().nextBytes (data);
          setData (data);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    buttonPanel.add (randomizeButton);
    container.add (buttonPanel,"South");
  }

  protected String displayString ()
  {
    return "stream";
  }
  
  public boolean equalsStream (PDFStream stream)  {
	  try {
		return stream instanceof PDFStream && equalsDictionary (stream) && !dataDiffers ((PDFStream) stream);
	} catch (IOException e) {
		throw new Error ();
	}
  }

  private boolean dataDiffers (PDFContainer container) throws IOException {
    InputStream in1 = getInputStream ();
    InputStream in2 = ((PDFStream) container).getInputStream ();
    for (;;)
      {
      int b1 = in1.read ();
      int b2 = in2.read ();
      if (b1 != b2)
        return true;
      if (b1 == -1)
        return false;
    }
  }
  
  void diff (PDFContainer container,String name) throws IOException
  {
    super.diff (container,name);
    if (dataDiffers (container)) {
      System.out.println ("data  diff : " + name);
      if (PDFDiff.diffStreams.isSet ()) {
        File file1 = File.createTempFile (name + "1",null);
        Util.dump (getData (),file1);
        File file2 = File.createTempFile (name + "2",null);
        Util.dump (((PDFStream) container).getData (),file2);
        Process process = Runtime.getRuntime ().exec (new String [] {"diff",file1.getPath (),file2.getPath ()});
        Util.copy (process.getInputStream (),System.out);
        try {
          process.waitFor ();
        } catch (InterruptedException ie) {
          ie.printStackTrace ();
        }
	file1.delete ();
	file2.delete ();
      }
    }
  }
  
  public PDFStream clone () {
    return new PDFStream (this);
  }
  
  public boolean assimilate (PDFContainer container) throws IOException {
    if (super.assimilate (container))
      return true;
    boolean dataDiffers = dataDiffers (container);
    if (dataDiffers)
      setData (((PDFStream) container).getData ());
    return dataDiffers;
  }
}
