/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.List;
import java.util.Vector;

import info.joriki.adobe.PostScriptFile;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class CalculatorFunction extends PDFFunction
{
  float [] result;
  PostScriptFile interpreter;
  List procedure;
  
  public CalculatorFunction (float [] domain,float [] range,PDFStream code)
  {
    super (domain,range);
    result = new float [n];
    try {
      interpreter = new PostScriptFile (code.getInputStream ("3.34"),false);
      procedure = (List) interpreter.operandStack.pop (); 
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error ("couldn't read code stream for calculator function");
    }
  }
  
  protected float [] valueFor (float [] x)
  {
    for (int i = 0;i < x.length;i++)
      interpreter.operandStack.push (new Double (x [i]));
    interpreter.executionStack.push (procedure);
    try {
      interpreter.execute ();
    } catch (IOException ioe) {
      throw new Error ("can't execute calculator procedure");
    }
    Assertions.expect (interpreter.operandStack.size (),n);
    for (int i = n - 1;i >= 0;i--)
      result [i] = ((Number) interpreter.operandStack.pop ()).floatValue ();

    return result;
  }

  protected void addClippedSamples (Vector sampleVector,
                                    float xa,float xb,
                                    float [] innerRange,
                                    double maximalDeviation)
  {
    throw new NotImplementedException ("sampling for type 4 functions");
  }
}
