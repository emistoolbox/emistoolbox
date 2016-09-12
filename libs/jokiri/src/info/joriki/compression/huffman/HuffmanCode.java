/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import info.joriki.util.General;
import info.joriki.util.Handler;
import info.joriki.util.Assertions;
import info.joriki.util.Traversable;

public class HuffmanCode implements Traversable<HuffmanLeaf>
{
  int maxlen;
  List<HuffmanLeaf> leaves;

  private HuffmanCode () {}

  // reads in a Huffman table as coded in JPEG files
  public HuffmanCode (InputStream in,int maxlen) throws IOException
  {
    this.maxlen = maxlen;
    
    final int [] nsymbols = new int [maxlen+1];
    for (int i = 1;i <= maxlen;i++)
      nsymbols [i] = in.read ();
    
    List<HuffmanLeaf> [] byLength = new List [maxlen+1];
    
    for (int i = 1;i <= maxlen;i++)
      {
        List<HuffmanLeaf> next = byLength [i] = new ArrayList<HuffmanLeaf> ();
        for (int j = 0;j < nsymbols [i];j++)
          {
            HuffmanLeaf leaf = new HuffmanLeaf ();
            leaf.codeLength = i;
            leaf.symbol = in.read ();
            if ((leaf.symbol & 0xf) > 11)
              System.err.println ("??????? big size " +
                                  (leaf.symbol & 0xf)); 
            next.add (leaf);
          }
      }

    makeLeaves (byLength);
  }

  // builds a huffman code from binary strings
  // symbols can be null, in which case the symbols are the indices into binaryStrings
  public HuffmanCode (String [] binaryStrings,int [] symbols)
  {
    if (symbols != null)
      Assertions.expect (symbols.length == binaryStrings.length);

    leaves = new ArrayList<HuffmanLeaf> (binaryStrings.length);

    for (int i = 0;i < binaryStrings.length;i++)
      {
        int len = binaryStrings [i].length ();
        maxlen = Math.max (maxlen,len);

        HuffmanLeaf leaf = new HuffmanLeaf ();
        leaf.symbol = symbols != null ? symbols [i] : i;
        leaf.codeLength = len;
        leaf.code = Integer.parseInt (binaryStrings [i],2);
        leaves.add (leaf);
      }
  }

  public HuffmanCode (HuffmanCode code)
  {
    this.maxlen = code.maxlen;
    this.leaves = new ArrayList<HuffmanLeaf> (code.leaves);
  }

  int nodeNumber;
  class Node implements Comparable
  {
    int num = nodeNumber++;
    int depth;
    int symbol;
    int frequency;
    Node left;
    Node right;

    Node (int symbol,int frequency)
    {
      this.symbol = symbol;
      this.frequency = frequency;
      this.depth = 1;
    }

    Node (Node left,Node right)
    {
      this.left = left;
      this.right = right;
      this.frequency = left.frequency + right.frequency;
      this.depth = Math.max (left.depth,right.depth) + 1;
    }
    
    public int compareTo (Object o)
    {
      Node n = (Node) o;
      int dif = frequency - n.frequency;
      if (dif != 0)
        return dif;
      dif = n.depth - depth;
      if (dif != 0)
        return dif;
      dif = num - n.num;
      return dif;
    }

    void traverse (int length)
    {
      Assertions.expect (right != null,left != null);
      if (left != null)
        {
          left.traverse (length+1);
          right.traverse (length+1);
        }
      else
        {
          HuffmanLeaf leaf = new HuffmanLeaf ();
          leaf.codeLength = length;
          leaf.symbol = symbol;
          leaves.add (leaf);
          maxlen = Math.max (maxlen,length);
        }
    }
  }

  // constructs an optimal huffman code
  public HuffmanCode (int [] symbols,int [] frequencies)
  {
    this (symbols,frequencies,Integer.MAX_VALUE);
  }

  public HuffmanCode (int [] symbols,int [] frequencies,int lengthConstraint)
  {
    Assertions.expect (symbols == null || symbols.length == frequencies.length);

    SortedSet<Node> nodeSet = new TreeSet<Node> ();
    for (int i = 0;i < frequencies.length;i++)
      if (frequencies [i] != 0)
        nodeSet.add (new Node (symbols == null ? i : symbols [i],frequencies [i]));

    // dummy symbol to prevent allocation of all-| code
    nodeSet.add (new Node (-1,0));

    while (nodeSet.size () > 1)
      {
        Node right = nodeSet.first ();
        nodeSet.remove (right);
        Node left = nodeSet.first ();
        nodeSet.remove (left);
        nodeSet.add (new Node (left,right));
      }

    leaves = new ArrayList<HuffmanLeaf> (frequencies.length + 1);

    nodeSet.first ().traverse (0);

    // we could assign codes in the traversal,
    // but we'd have to make them conform to
    // how JPEG assigns them. If the length
    // constraint is violated, we go by length
    // anyway, so why bother

    List<HuffmanLeaf> [] byLength = new List [maxlen+1];
    for (int i = 1;i <= maxlen;i++)
      byLength [i] = new ArrayList<HuffmanLeaf> ();
    Iterator<HuffmanLeaf> iterator = leaves.iterator ();

    while (iterator.hasNext ())
      {
        HuffmanLeaf leaf = iterator.next ();
        byLength [leaf.codeLength].add (leaf);
      }

    while (maxlen > lengthConstraint)
      {
        iterator = byLength [maxlen--].iterator ();
        while (iterator.hasNext ())
          {
            HuffmanLeaf left  = iterator.next ();
            HuffmanLeaf right = iterator.next ();
    
            int l = maxlen;
            while (byLength [--l].isEmpty ())
              ;
            HuffmanLeaf demotee = General.removeLast (byLength [l]);
            byLength [l+1].add (demotee);
            byLength [l+1].add (left);
            byLength [maxlen].add (right);
          }
      }

    // remove dummy
    Assertions.expect ((General.removeLast (byLength [maxlen])).symbol,-1);

    makeLeaves (byLength);
  }

  // generates a list of leaves with codes
  // from an array of lists of leaves without codes ordered by length
  private void makeLeaves (List<HuffmanLeaf> [] byLength)
  {
    leaves = new ArrayList<HuffmanLeaf> ();
    for (int i = 1,code = 0;i <= maxlen;i++,code <<= 1)
      for (int j = 0;j < byLength [i].size ();j++)  
        {
          HuffmanLeaf leaf = byLength [i].get (j);
          leaf.codeLength = i;
          leaf.code = code++;
          leaves.add (leaf);
        }
  }

  // traverses linear vector of leaves.
  public void traverse (Handler<HuffmanLeaf> handler)
  {
    General.traverse (leaves,handler);
  }

  public void addSymbol (int symbol)
  {
    // make sure we don't mess things up in another code that
    // shares some of our leaves
    HuffmanLeaf lastLeaf;
    do
      lastLeaf = (HuffmanLeaf) (leaves.remove (leaves.size () - 1)).clone ();
    while (lastLeaf.codeLength == maxlen);
    HuffmanLeaf newLeaf = new HuffmanLeaf ();
    lastLeaf.code <<= 1;
    newLeaf.code = lastLeaf.code + 1;
    newLeaf.codeLength = ++lastLeaf.codeLength;
    newLeaf.symbol = symbol;
    leaves.add (lastLeaf);
    leaves.add (newLeaf);
  }

  public void writeTo (final OutputStream out) throws IOException
  {
    final byte [] cnt = new byte [16];
    traverse (new Handler<HuffmanLeaf> () {
        public void handle (HuffmanLeaf leaf)
        {
          cnt [leaf.codeLength - 1]++;
        }
      });

    out.write (cnt);
      
    traverse (new Handler<HuffmanLeaf> () {
        public void handle (HuffmanLeaf leaf)
        {
          try {
            out.write (leaf.symbol);
          } catch (IOException ioe) { ioe.printStackTrace (); }
        }
      });
  }

  public static HuffmanCode uniformCode (int nbits)
  {
    HuffmanCode uniformCode = new HuffmanCode ();

    uniformCode.leaves = new ArrayList<HuffmanLeaf> ();
    for (int code = 0;code < (1 << nbits) - 1;code++)
      {
        HuffmanLeaf leaf = new HuffmanLeaf ();
        leaf.code = leaf.symbol = code;
        leaf.codeLength = nbits;
        uniformCode.leaves.add (leaf);
      }
    uniformCode.maxlen = nbits;

    return uniformCode;
  }
}
