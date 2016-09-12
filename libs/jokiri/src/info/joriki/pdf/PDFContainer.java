/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.awt.Container;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import info.joriki.util.Handler;

public abstract class PDFContainer extends PDFObject implements Iterable<PDFObject>
{
  abstract boolean iterateInParallel (PDFContainer container,PairHandler handler);
  abstract void buildEditor (Container container,EditHandler editor);
  abstract void add (PDFObject object);
  abstract public boolean assimilate (PDFContainer container) throws IOException;
  abstract void diff (PDFContainer container,String name) throws IOException;
  abstract Collection<PDFObject> elements ();
  abstract int size ();

  static ThreadLocal<Map<PDFObject,PDFObject>> compared = new ThreadLocal<Map<PDFObject,PDFObject>> ();

  void diff (PDFObject valueA,PDFObject valueB,String name) throws IOException
  {
    if (valueA == null && valueB == null)
      return;
    else if (valueA == null)
      System.out.println ("A is null : " + name);
    else if (valueB == null)
      System.out.println ("B is null : " + name);
    else if (valueA.getClass () != valueB.getClass ())
      System.out.println ("type diff  : " + name);
    else if (valueA instanceof PDFContainer) {
      Map<PDFObject,PDFObject> map = compared.get ();
      PDFObject old = map.get (valueA);
      if (old == null) {
        map.put (valueA,valueB);
        ((PDFContainer) valueA).diff ((PDFContainer) valueB,name);
      }
      else if (old != valueB) {
        System.out.println ("structure diff : " + name);
        System.out.println (valueA);
        System.out.println ("is referred to twice, corresponding first to");
        System.out.println (old);
        System.out.println ("and then to");
        System.out.println (valueB);
      }
    }
    else if (!valueA.equals (valueB))
      System.out.println ("value diff : " + name + " (" + valueA + " != " + valueB + ")");
  }

  final Iterator<PDFObject> unresolvedIterator () {
    return elements ().iterator ();
  }

  public Iterator<PDFObject> iterator ()
  {
    return new Iterator<PDFObject> () {
      Iterator<PDFObject> elements = unresolvedIterator ();
      
      public boolean hasNext ()
      {
        return elements.hasNext ();
      }
      
      public PDFObject next ()
      {
        return elements.next ().resolve ();
      }

      public void remove ()
      {
        elements.remove ();
      }
    };
  }

  public void traverse (Handler<PDFObject> handler,boolean resolve)
  {
    traverse (this,handler,new HashSet<PDFObject> (),resolve);
  }
  
  private void traverse (PDFObject object,Handler<PDFObject> handler,Set<PDFObject> descendants,boolean resolve)
  {
    if (!descendants.add (object))
      return;
    handler.handle (object);
    if (object instanceof PDFContainer)
    {
      PDFContainer container = (PDFContainer) object;
      Iterator iterator = resolve ? container.iterator () : container.unresolvedIterator ();
      while (iterator.hasNext ())
        traverse ((PDFObject) iterator.next (),handler,descendants,resolve);
    }
  }
  
  abstract public PDFContainer clone ();
}
