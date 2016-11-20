/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

public class ArithmeticCodingContext
{
  static class State
  {
    int q;
    int nmps;
    int nlps;
    boolean toggle;

    State (int q,int nmps,int nlps,boolean toggle)
    {
      this.q = q;
      this.nmps = nmps;
      this.nlps = nlps;
      this.toggle = toggle;
    }
  }

  final static State [] states = {
    new State (0x5601,1,1,true),
    new State (0x3401,2,6,false),
    new State (0x1801,3,9,false),
    new State (0x0AC1,4,12,false),
    new State (0x0521,5,29,false),
    new State (0x0221,38,33,false),
    new State (0x5601,7,6,true),
    new State (0x5401,8,14,false),
    new State (0x4801,9,14,false),
    new State (0x3801,10,14,false),
    new State (0x3001,11,17,false),
    new State (0x2401,12,18,false),
    new State (0x1C01,13,20,false),
    new State (0x1601,29,21,false),
    new State (0x5601,15,14,true),
    new State (0x5401,16,14,false),
    new State (0x5101,17,15,false),
    new State (0x4801,18,16,false),
    new State (0x3801,19,17,false),
    new State (0x3401,20,18,false),
    new State (0x3001,21,19,false),
    new State (0x2801,22,19,false),
    new State (0x2401,23,20,false),
    new State (0x2201,24,21,false),
    new State (0x1C01,25,22,false),
    new State (0x1801,26,23,false),
    new State (0x1601,27,24,false),
    new State (0x1401,28,25,false),
    new State (0x1201,29,26,false),
    new State (0x1101,30,27,false),
    new State (0x0AC1,31,28,false),
    new State (0x09C1,32,29,false),
    new State (0x08A1,33,30,false),
    new State (0x0521,34,31,false),
    new State (0x0441,35,32,false),
    new State (0x02A1,36,33,false),
    new State (0x0221,37,34,false),
    new State (0x0141,38,35,false),
    new State (0x0111,39,36,false),
    new State (0x0085,40,37,false),
    new State (0x0049,41,38,false),
    new State (0x0025,42,39,false),
    new State (0x0015,43,40,false),
    new State (0x0009,44,41,false),
    new State (0x0005,45,42,false),
    new State (0x0001,45,43,false),
    new State (0x5601,46,46,false)
  };

  State initialState;
  State state;
  boolean mps;

  ArithmeticCodingContext (int initialIndex)
  {
    initialState = states [initialIndex];
    reset ();
  }

  void reset ()
  {
    state = initialState;
    mps = false;
  }

  boolean step (boolean lps)
  {
    boolean result = mps ^ lps;
    if (lps)
      {
	mps ^= state.toggle;
	state = states [state.nlps];
      }
    else
      state = states [state.nmps];
    return result;
  }
}
