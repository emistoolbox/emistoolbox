/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Stack;

public class DeletionScript {
  private Stack<Deletion> deletions = new Stack<Deletion> ();
  
  public List<Deletion> getDeletions () {
    return deletions;
  }
  
  public void add (Deletion deletion) {
    if (deletion.position < (deletions.isEmpty () ? 0 : deletions.peek ().end))
      throw new IllegalArgumentException ("deletion in deletion script out of order");
    deletions.add (deletion);
  }
  
  public byte [] apply (byte [] superSequence) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int superIndex = 0;
    for (Deletion deletion : deletions) {
      baos.write (superSequence,superIndex,deletion.position - superIndex);
      superIndex = deletion.end;
    }
    baos.write (superSequence,superIndex,superSequence.length - superIndex);
    return baos.toByteArray ();
  }
}
