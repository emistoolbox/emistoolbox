/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.type1;

import java.io.InputStream;
import java.io.IOException;

import java.util.List;
import java.util.Map;

import info.joriki.adobe.Name;

import info.joriki.font.GlyphProvider;

import info.joriki.util.General;
import info.joriki.util.Assertions;

import info.joriki.charstring.CharStringAdapter;
import info.joriki.charstring.Type1CharStringDecoder;

public class MultipleMasterFontFile extends Type1FontFile
{
  final static int WIDTH = 0;
  final static int WEIGHT = 1;
  final static String [] axisTypes = {"Width","Weight"};
  int [] indices = new int [2];
  double [] verticalStemWidths;

  public MultipleMasterFontFile (InputStream in) throws IOException
  {
    super (in,false,6);
    List blendAxisTypes = (List) fontInfoDictionary.get ("BlendAxisTypes");
    Assertions.expect (blendAxisTypes.size (),2);
    for (int i = 0;i < blendAxisTypes.size ();i++)
    {
      String axisType = ((Name) blendAxisTypes.get (i)).name;
      for (int j = 0;j < 2;j++)
        if (axisType.equals (axisTypes [j]))
          indices [j] = i;
    }
    verticalStemWidths = General.toDoubleArray
      ((List) ((List) ((Map) ((Map) fontDictionary.get ("Blend")).get ("Private")).get ("StdVW")).get (0));
    Assertions.expect (verticalStemWidths.length,4);
  }

  static double twist (double [] v1,double [] v2)
  {
    double sum = 0;
    for (int i = -1,k = 0,l = 3;i <= 1;i += 2)
      for (int j = -1;j <= 1;j += 2,k++,l--)
	sum += i * j * v1 [k] * v2 [l];
    return sum;
  }

  static void orthogonalize (double [] a,double [] b)
  {
    double ab = 0;
    double bb = 0;
    for (int j = 0;j < 4;j++)
      {
	ab += a [j] * b [j];
	bb += b [j] * b [j];
      }
    for (int j = 0;j < 4;j++)
      a [j] -= b [j] * ab / bb;
  }

  java.util.Random random = new java.util.Random (0);

  public GlyphProvider getGlyphProvider (String glyph,double width,double verticalStemWidth)
  {
    if (width <= 0)
      throw new IllegalArgumentException ("non-positive width in glyph synthesis");
    byte [] charString = getCharString (glyph);
    if (charString == null)
      return null;
    Type1CharStringDecoder decoder = getCharStringDecoder ();
    decoder.setCharString (charString);
    final double [] widths = new double [4];
    for (int i = 0;i < 4;i++)
    {
      decoder.weights = new double [4];
      decoder.weights [i] = 1;
      final int index = i;
      decoder.interpret (new CharStringAdapter () {
        public void setAdvance (double x,double y)
        {
          Assertions.expect (y,0);
          widths [index] = x;
        }
      });
      Assertions.unexpect (widths [index],0);
    }
    double [] parameters = new double [2];
    parameters [indices [WIDTH]] = width;
    parameters [indices [WEIGHT]] = verticalStemWidth;
    
    double [] [] parameterSets = new double [2] [];
    parameterSets [indices [WIDTH]] = widths;
    parameterSets [indices [WEIGHT]] = verticalStemWidths.clone ();
    
    /* We are looking for four weights such that a) the corresponding
       linear combination of the parameter sets yields the parameters
       and b) the weights are expressible as a*b,(1-a)*b,a*(1-b),(1-a)*(1-b).
       a) yields two linear equations, which we homogenize by subtracting
       the parameter value, using the fact that the weights add up to 1 */
    for (int i = 0;i < 2;i++)
      for (int j = 0;j < 4;j++)
        parameterSets [i] [j] -= parameters [i];
    // now we find two vectors that satisfy these homogeneous linear
    // equations by projecting random vectors.
    orthogonalize (parameterSets [0],parameterSets [1]);
    double [] [] vectors = new double [2] [4];
    for (int i = 0;i < 2;i++)
    {
      double [] vector = vectors [i];
      for (int j = 0;j < 4;j++)
        vector [j] = random.nextDouble ();
      for (int k = 0;k < 2;k++)
        orthogonalize (vector,parameterSets [k]);
      double sum = 0;
      for (int j = 0;j < 4;j++)
        sum += vector [j];
      for (int j = 0;j < 4;j++)
        vector [j] /= sum;
    }
    
    // b) can be expressed as the two conditions that
    // i) the sum of weights is 1 and ii) w1*w4 == w2*w3.
    // We have already normalized the vectors to sum to 1 above.
    for (int j = 0;j < 4;j++)
      vectors [1] [j] -= vectors [0] [j];
    
    // Now vectors [1] sums to 0, and it remains to find values of lambda
    // for which vectors [0] + lambda * vectors [1] fulfills ii).
    
    double a = twist (vectors [1],vectors [1]);
    double b = twist (vectors [0],vectors [1]);
    double c = twist (vectors [0],vectors [0]);
    double min = Double.MAX_VALUE;
    for (int sign = -1;sign <= 1;sign += 2)
    {
      double [] solution = new double [4];
      double lambda = Math.abs (a*c) > 1e-10 * b*b ? 
                      (-b + sign * Math.sqrt (b*b - a*c)) / a :
                       -(1 + (3*a*c)/(4*b*b))*c/(2*b);
      double norm = 0;
      for (int j = 0;j < 4;j++)
      {
        solution [j] = vectors [0] [j] + lambda * vectors [1] [j];
        norm += Math.abs (solution [j] - .5);
      }
      if (norm < min)
      {
        decoder.weights = solution;
        min = norm;
      }
    }
    return decoder;
  }

  public double getNominalVerticalStemWidth ()
  {
    return ((Number) ((List) privateDictionary.get ("StdVW")).get (0)).doubleValue ();
  }
}
