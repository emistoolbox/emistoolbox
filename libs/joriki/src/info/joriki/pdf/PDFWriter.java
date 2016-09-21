/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.util.Handler;
import info.joriki.util.Numberer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class PDFWriter extends PDFObjectWriter implements PDFPermissions {
  private static final byte [] objectPrefix = " 0 obj".getBytes ();
  private static final byte [] objectSuffix = "endobj\n".getBytes ();

  Queue<PDFObject> objects;
  Numberer<PDFObject> numberer;
  List<Integer> offsets;
  PDFDictionary trailer;
  Crypt encryptionCrypt = crypt;
  byte [] digits = "0000000000 00000 n \n".getBytes (); // first ten get overwritten

  Handler<PDFObject> handler = new Handler<PDFObject> () {
    public void handle (PDFObject o) {
      objects.add (o);
    }
  };

  public PDFWriter (OutputStream out) {
	  super (new BufferedOutputStream (out));
  }

  public PDFWriter (File file) throws FileNotFoundException {
    this (new FileOutputStream (file));
  }
  
  public PDFWriter (String filename) throws FileNotFoundException {
    this (new File (filename));
  }

  int numberFor (PDFObject object) {
    return numberer.numberFor (object,handler);
  }

  void print (int value) throws IOException {
    int index = 10;
    do {
      int next = value / 10; 
      digits [--index] = (byte) ('0' + value - 10 * next);
      value = next;
    } while (value != 0);
    write (digits,index,10 - index);
  }

  public void write (PDFDocument document) throws IOException {
    write (document,null,0);
  }
  
  public void write (PDFDocument document,String password,int permissions) throws IOException {
    try {
      write ("%PDF-");
      write (document.version.toString ());
      write ('\n');
      write ('%' );
      write (0xde);
      write (0xad);
      write (0xbe);
      write (0xef);
      write ('\n');
      
      objects = new LinkedList<PDFObject> ();
      numberer = new Numberer<PDFObject> (1);
      offsets = new ArrayList<Integer> ();
      trailer = new PDFDictionary ();

      if (document.id != null) {
        byte [] second = new byte [16];
        new Random ().nextBytes (second);
        document.id.set (1,new PDFString (second));
        trailer.put ("ID",document.id);
      }
      
      PDFDictionary encryptionDictionary = null;
      if (password != null) {
        encryptionDictionary = new PDFDictionary ();
        encryptionDictionary.put ("Filter","Standard");
        encryptionDictionary.put ("Length",128);
        encryptionDictionary.put ("V",2);
        encryptionDictionary.put ("R",3);
        encryptionDictionary.put ("P",permissions | reservedBits);
        Assertions.expect (permissions & reservedMask,0);
        StandardSecurityHandler standardSecurityHandler = new StandardSecurityHandler (encryptionDictionary,document.id);
        standardSecurityHandler.setPasswords ("",password);
        encryptionDictionary.put ("U",new PDFString (standardSecurityHandler.user));
        encryptionDictionary.put ("O",new PDFString (standardSecurityHandler.owner));
        trailer.putIndirect ("Encrypt",encryptionDictionary);
        crypt = encryptionCrypt = new Crypt (standardSecurityHandler.generateEncryptionKey ());
      }
      
      numberFor (document.root);
      trailer.putIndirect ("Root",document.root);

      if (document.info != null) {
        numberFor (document.info);
        trailer.putIndirect ("Info",document.info);
      }

      for (;;) {
        PDFObject object;
        if (objects.isEmpty ()) {
          if (encryptionDictionary == null)
            break;
          numberFor (encryptionDictionary);
          encryptionDictionary = null;
          crypt = new Crypt (null);
          continue;
        }
        object = objects.remove ();
        offsets.add (getCount ());
        crypt.object = numberFor (object);
        print (crypt.object);
        write (objectPrefix);
        isDelimited = false;
        write (object);
        delimit ();
        write (objectSuffix);
      }
      
      int start = getCount ();
      int size = offsets.size () + 1;
      trailer.put ("Size",new PDFInteger (size));
      write ("xref\n0 ");
      print (size);
      write ("\n0000000000 65535 f \n");
      for (int offset : offsets) {
        for (int j = 10;j > 0;) {
          int next = offset / 10;
          digits [--j] = (byte) ('0' + offset - 10 * next);
          offset = next;
        }
        write (digits);
      }
      // add three further offsets for use by the print preprocessor 
      offsets.add (start);       // end of last object 
      offsets.add (getCount ()); // start of trailer
      write ("trailer\n");
      trailer.write (this);
      write ("\nstartxref\n");
      offsets.add (getCount ()); // end of trailer
      print (start);
      write ("\n%%EOF");
      flush ();
    } finally { close (); }
  }
}
