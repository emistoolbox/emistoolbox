/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

import info.joriki.util.Collections;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

public class InsertionScript {
  private Stack<Insertion> insertions = new Stack<Insertion> ();
  
  public InsertionScript () {}

  public InsertionScript (InsertionScript...scripts) {
    Iterator<Insertion> iterator = getMergeIterator (scripts);
    while (iterator.hasNext ())
      insertions.add (iterator.next ());
  }
  
  public InsertionScript (byte [] subSequence,byte [] superSequence) {
    int [] superIndices = new int [subSequence.length];
    for (int superIndex = superSequence.length - 1,subIndex = subSequence.length - 1;subIndex >= 0;superIndex--)
      if (superSequence [superIndex] == subSequence [subIndex])
        superIndices [subIndex--] = superIndex;
    for (int subIndex = 0,superIndex = 0;;) {
      if (superIndex != superIndices [subIndex]) {
        insertions.add (new Insertion (subIndex,superSequence,superIndex,superIndices [subIndex]));
        superIndex = superIndices [subIndex];
      }
      while (superSequence [superIndex++] == subSequence [subIndex++])
        ;
    }
  }

  public boolean isMerge (InsertionScript...scripts) {
    Iterator<Insertion> iterator = getMergeIterator (scripts);
    for (Insertion insertion : insertions)
      if (!iterator.hasNext () || !iterator.next ().equals (insertion))
        return false;
    return !iterator.hasNext ();
  }
  
  private static Iterator<Insertion> getMergeIterator (final InsertionScript [] scripts) {
    return new Iterator<Insertion> () {
      Insertion [] insertions = new Insertion [scripts.length];
      Iterator<Insertion> [] iterators = new Iterator [scripts.length];
      
      {
        for (int i = 0;i < scripts.length;i++) {
          iterators [i] = scripts [i].insertions.iterator ();
          advance (i);
        }
      }
      
      private void advance (int index) {
        insertions [index] = Collections.next (iterators [index]);
      }
      
      public boolean hasNext () {
        for (int i = 0;i < insertions.length;i++)
          if (insertions [i] != null)
            return true;
        return false;
      }
      
      public Insertion next () {
        int nextIndex = 0;
        Insertion nextInsertion = null;
        for (int i = 0;i < insertions.length;i++)
          // if positions are equal, apply script with lower index first 
          if (insertions [i] != null && (nextInsertion == null || insertions [i].position < nextInsertion.position)) {
            nextInsertion = insertions [i];
            nextIndex = i;
          }
        if (nextInsertion == null)
          throw new NoSuchElementException ();
        advance (nextIndex);
        return nextInsertion;
      }

      public void remove () {
        throw new UnsupportedOperationException ();
      }
    };
  }
  
  public List<Insertion> getInsertions () {
    return insertions;
  }
  
  public void add (Insertion insertion) {
    if (insertion.position < (insertions.isEmpty () ? 0 : insertions.peek ().position))
      throw new IllegalArgumentException ("insertion in insertion script out of order");
    insertions.add (insertion);
  }
  
  public byte [] apply (byte [] subSequence) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int subIndex = 0;
    for (Insertion insertion : insertions) {
      while (subIndex < insertion.position)
        baos.write (subSequence [subIndex++]);
      insertion.byteArray.writeTo (baos);
    }
    while (subIndex < subSequence.length)
      baos.write (subSequence [subIndex++]);
    return baos.toByteArray ();
  }
}
