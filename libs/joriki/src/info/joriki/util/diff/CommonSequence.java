/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

import info.joriki.io.WriteableByteArray;
import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonSequence {
  private List<byte []> sequences;
  private byte [] commonSequence;
  private boolean matched;
  private int limit;
  private List<Integer> matchLengths;
  private List<int []> matchPositionss;

  public CommonSequence () {
    this (Integer.MAX_VALUE);
  }
  
  public CommonSequence (int limit) {
    this.limit = limit;
    sequences = new ArrayList<byte []> ();
  }
  
  public CommonSequence (byte [] commonSequence,byte [] ... sequences) {
    this.sequences = Arrays.asList (sequences.clone ());
    this.commonSequence = commonSequence.clone ();
  }
  
  public void add (byte [] sequence) {
    sequences.add (sequence);
    commonSequence =
      commonSequence == null ? sequence :
      Math.abs (sequence.length - commonSequence.length) > limit ? new byte [0] :
      new StandardDiff (sequence,commonSequence).longestCommonSequence ();
    matched = false;
  }

  public byte [] getCommonSequence () {
    return commonSequence;
  }

  public byte [] getMarkedCommonSequence (int insertionMarker) {
    match ();
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    int commonIndex = 0;
    for (int matchLength : matchLengths) {
      if (commonIndex != 0 || hasInitialSection ())
        baos.write (insertionMarker);
      baos.write (commonSequence,commonIndex,matchLength);
      commonIndex += matchLength;
    }
    if (hasFinalSection ())
      baos.write (insertionMarker);
    return baos.toByteArray ();
  }

  public InsertionScript [] getInsertionScripts (boolean includeZeroLength) {
    InsertionScript [] insertionScripts = new InsertionScript [sequences.size ()];
    match ();
    for (int i = 0;i < insertionScripts.length;i++) {
      insertionScripts [i] = new InsertionScript ();
      int commonIndex = 0;
      for (int j = 0;j <= matchLengths.size ();j++) {
        if ((j != 0 || hasInitialSection ()) && (j != matchLengths.size () || hasFinalSection ())) {
          int beg = j == 0 ? 0 : matchPositionss.get (j - 1) [i] + matchLengths.get (j - 1);
          int end = j == matchLengths.size () ? sequences.get (i).length : matchPositionss.get (j) [i];
          if (end != beg || includeZeroLength)
            insertionScripts [i].add (new Insertion (commonIndex,sequences.get (i),beg,end));
        }
        if (j != matchLengths.size ())
          commonIndex += matchLengths.get (j);
      }
    }
    return insertionScripts;
  }

  public DeletionScript [] getDeletionScripts (boolean includeZeroLength) {
    DeletionScript [] deletionScripts = new DeletionScript [sequences.size ()];
    match ();
    for (int i = 0;i < deletionScripts.length;i++) {
      deletionScripts [i] = new DeletionScript ();
      for (int j = 0;j <= matchLengths.size ();j++)
        if ((j != 0 || hasInitialSection ()) && (j != matchLengths.size () || hasFinalSection ())) {
          int beg = j == 0 ? 0 : matchPositionss.get (j - 1) [i] + matchLengths.get (j - 1);
          int end = j == matchLengths.size () ? sequences.get (i).length : matchPositionss.get (j) [i];
          if (end != beg || includeZeroLength)
            deletionScripts [i].add (new Deletion (beg,end));
        }
    }
    return deletionScripts;
  }

  private boolean hasInitialSection () {
    if (matchPositionss.isEmpty ())
      return true;
    int [] matchPositions = matchPositionss.get (0);
    for (int matchPosition : matchPositions)
      if (matchPosition != 0)
        return true;
    return false;
  }
  
  private boolean hasFinalSection () {
    if (matchPositionss.isEmpty ())
      return true;
    int lastIndex = matchPositionss.size () - 1;
    int [] matchPositions = matchPositionss.get (lastIndex);
    int matchLength = matchLengths.get (lastIndex);
    int i = 0;
    for (byte [] sequence : sequences)
      if (matchPositions [i++] + matchLength != sequence.length)
        return true;
    return false;
  }
  
  // Because we build the common sequence incrementally, it is sometimes not quite maximal.
  // This method tries to find bytes that are actually commmon but were discarded early in
  // the process in favor of other bytes which have since been discarded themselves. It is
  // rather simple-minded and makes no effort to fix complicated cases.
  
  public void repair () {
    WriteableByteArray whole = new WriteableByteArray ();
    WriteableByteArray piece = new WriteableByteArray ();
    int [] offsets = new int [sequences.size ()];
    for (int index = 0;index < commonSequence.length;index++) {
      byte b = commonSequence [index];
      int i = 0;
      for (byte [] sequence : sequences)
        if (sequence [index + offsets [i++]] != b) {
          CommonSequence commonPiece = new CommonSequence ();
          i = 0;
          for (byte [] s : sequences) {
            piece.reset ();
            for (;;) {
              byte c = s [index + offsets [i]];
              if (c == b)
                break;
              piece.write (c);
              offsets [i]++;
            }
            commonPiece.add (piece.toByteArray ());
            i++;
          }
          whole.write (commonPiece.getCommonSequence ());
          break;
        }
      // all sequences now coincide at b
      whole.write (b);
    }
    commonSequence = whole.toByteArray ();
    matched = false;
    throw new NotTestedException ();
  }
  
  private void match () {
    if (matched)
      return;
    matched = true;

    int n = sequences.size ();
    int [] [] firstPositions = new int [commonSequence.length] [n];
    int [] [] lastPositions = new int [commonSequence.length] [n];
    try {
      int which = 0;
      for (byte [] sequence : sequences) {
        for (int i = 0,j = 0;i < commonSequence.length;) {
          while (sequence [j] != commonSequence [i])
            j++;
          firstPositions [i++] [which] = j++;
        }
        for (int i = commonSequence.length - 1,j = sequence.length - 1;i >= 0;) {
          while (sequence [j] != commonSequence [i])
            j--;
          Assertions.expect (j >= firstPositions [i] [which]);
          lastPositions [i--] [which] = j--;
        }
        which++;
      }
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      throw new Error ("common sequence provided is not in fact common");
    }
    int commonIndex = 0;
    matchLengths = new ArrayList<Integer> ();
    matchPositionss = new ArrayList <int []> ();
    int previousLength = 0;
    int [] nextMatchPositions = new int [n];
    int [] previousMatchPositions = commonSequence.length == 0 ? null : firstPositions [commonIndex];
    // Each iteration of this loop finds (the first occurrences of) the
    // largest initial section of the remainder of the common sequence
    // that has a consecutive match in all sequences. If all these first
    // occurrences happen to be in the right order in all the sequences,
    // this must be the match-up with the fewest gaps. If not, some
    // recursion might be required.
    while (commonIndex < commonSequence.length) {
      int [] matchPositions = new int [n];
      for (int i = 0;i < matchPositions.length;i++)
      {
        matchPositions [i] = previousMatchPositions [i] + previousLength;
        while (sequences.get (i) [matchPositions [i]] != commonSequence [commonIndex])
          matchPositions [i]++;
      }
      // loop over the length of the desired initial section until we
      // reach the end of the common sequence or one of the sequences
      // no longer contains a match of this length
      int length;
    outer:
      for (length = 1;commonIndex + length < commonSequence.length;length++) {
        int i = 0;
        for (byte [] sequence : sequences) {
          int index = matchPositions [i] + length;
          int cindex = commonIndex + length;
          // no need to check for sequence end here;
          // lastPositionss takes care of that 
          if (sequence [index] == commonSequence [cindex])
            nextMatchPositions [i] = matchPositions [i];
          else {
            // this sequence no longer matches -- try to find a different match
            index = findSubSequence (commonSequence,commonIndex,length + 1,sequence,matchPositions [i],lastPositions [commonIndex] [i]);
            if (index == -1)
              break outer;
            nextMatchPositions [i] = index;
          }
          i++;
        }
        int [] tmp = matchPositions;
        matchPositions = nextMatchPositions;
        nextMatchPositions = tmp;
      }
//    initializing the match positions with
//    matchPositions [i] = firstPositions [commonIndex] [i];
//    will allow the following code to check for optimality,
//    but as no improvement in case of non-optimality is
//    currently implemented, there's no point in doing so. 
//      if (previousLength != 0)
//        for (int i = 0;i < n;i++)
//          if (matchPositions [i] < previousMatchPositions [i] + previousLength)
//            throw new NotImplementedException ("recursion in common sequence matching");
      
      matchLengths.add (length);
      matchPositionss.add (matchPositions);
      
      previousLength = length;
      previousMatchPositions = matchPositions;
      
      commonIndex += length;
    }
  }
  
  public static int findSubSequence (byte [] pattern,byte [] sequence) {
    return findSubSequence (pattern,sequence,0);
  }
  
  public static int findSubSequence (byte [] pattern,byte [] sequence,int firstIndex) {
    return findSubSequence (pattern,sequence,firstIndex,sequence.length - 1);
  }

  public static int findSubSequence (byte [] pattern,byte [] sequence,int firstIndex,int lastIndex) {
    return findSubSequence (pattern,0,pattern.length,sequence,firstIndex,lastIndex);
  }
  
  public static int findSubSequence (byte [] pattern,int patternIndex,int patternLength,byte [] sequence,int firstIndex,int lastIndex) {
    if (patternLength == 0)
      return firstIndex;
    lastIndex -= patternLength;
    for (;;) {
      while (firstIndex <= lastIndex && sequence [firstIndex] != pattern [patternIndex])
        firstIndex++;
      if (firstIndex > lastIndex)
        return -1;
      int i = 1;
      while (i < patternLength && sequence [firstIndex + i] == pattern [patternIndex + i])
        i++;
      if (i == patternLength)
        return firstIndex;
      firstIndex++;
    }
  }
}
