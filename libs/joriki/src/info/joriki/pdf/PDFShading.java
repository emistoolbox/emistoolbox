/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

import info.joriki.awt.image.RGBColorModel;

import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;

public class PDFShading
{
  public final static int FUNCTION = 1;
  public final static int AXIAL = 2;
  public final static int RADIAL = 3;
  public final static int FREE_TRIANGLE = 4;
  public final static int LATTICE_TRIANGLE = 5;
  public final static int COONS_PATCH = 6;
  public final static int TENSOR_PATCH = 7;

  public int type;

  public BaseColorSpace baseColorSpace;

  public boolean antiAlias;
  public Rectangle boundingBox;
  public float [] backgroundColor;

  PDFFunction function = null;
  PDFFunction [] functions = null;

  int ncomponents;

  PDFShading (PDFDictionary dictionary,
              ResourceResolver resourceResolver,
              int ninput)
  {
    type = dictionary.getInt ("ShadingType");
    PDFColorSpace colorSpace = resourceResolver.getColorSpace (dictionary.get ("ColorSpace"));
    baseColorSpace = new BaseColorSpace (colorSpace);
    antiAlias = dictionary.getBoolean ("AntiAlias",false);
    boundingBox = dictionary.getRectangle ("BBox");
    backgroundColor = dictionary.getFloatArray ("Background");

    PDFObject functionObject = dictionary.get ("Function");
    if (functionObject instanceof PDFArray)
      {
        PDFArray functionArray = (PDFArray) functionObject;
        functions = new PDFFunction [functionArray.size ()];
        for (int i = 0;i < functions.length;i++)
          {
            functions [i] = (PDFFunction) resourceResolver.getCachedObject
              (ObjectTypes.FUNCTION,functionArray.get (i));
            Assertions.expect (functions [i].m,ninput);
            Assertions.expect (functions [i].n,1);
          }
        Assertions.expect (functions.length,colorSpace.ncomponents);
      }
    else if (functionObject instanceof PDFDictionary)
      {
        function = (PDFFunction) resourceResolver.getCachedObject
          (ObjectTypes.FUNCTION,functionObject);
        Assertions.expect (function.m,ninput);
        Assertions.expect (function.n,colorSpace.ncomponents);
      }
    else if (functionObject != null)
      throw new IllegalArgumentException ("Function must be specified by array or dictionary");

    if (functionObject != null && colorSpace instanceof IndexedColorSpace)
      throw new IllegalArgumentException
        ("Can't use indexed color space in an interpolating shading");

    ncomponents = functionObject != null ? ninput : colorSpace.ncomponents;
  }

  final static String [] shadingTypes =
  {"","Function","Axial","Radial","Triangle","Triangle","Patch","Patch"};

  public static PDFShading getInstance
    (PDFObject specification,ResourceResolver resourceResolver) throws Exception
  {
    return (PDFShading) Class.forName ("info.joriki.pdf." + shadingTypes [((PDFDictionary) specification).getInt ("ShadingType")] + "Shading").getDeclaredConstructors () [0].newInstance (new Object [] {specification,resourceResolver});
  }

  protected void checkDomain (float [] domain)
  {
    if (function != null)
      Assertions.expect (function.isDefinedOn (domain));
    else if (functions != null)
      for (int i = 0;i < functions.length;i++)
        Assertions.expect (functions [i].isDefinedOn (domain));
  }

  public boolean hasFunction ()
  {
    return function != null || functions != null;
  }

  public Sample [] getSamples (float a,float b,double smoothness)
  {
    float [] range = baseColorSpace.mappedBase.defaultDecode;
    if (function != null)
      return function.getSamples
        (a,b,smoothness,range);
    if (functions != null)
      return PDFFunction.getSamples
        (functions,a,b,smoothness,range);
    throw new InternalError ("no function to sample");
  }

  static interface Shader
  {
    float [] shade (Point p);
  }

  class Subsampler
  {
    int nsub;
    java.awt.Rectangle bbox;
    float [] [] [] [] [] values;

    Subsampler (java.awt.Rectangle bbox,int nsub)
    {
      this.bbox = bbox;
      this.nsub = nsub;
      values = new float [bbox.height] [bbox.width] [] [] [];
    }

    void sample (double [] point,float [] value)
    {
      int x = (int) Math.floor (point [0]);
      int y = (int) Math.floor (point [1]);
      int a = (int) (nsub * (point [0] - x));
      int b = (int) (nsub * (point [1] - y));
      set (x - bbox.x,y - bbox.y,a,b,value);
    }

    private void set (int x,int y,int a,int b,float [] value)
    {
      if (value != null)
      {
        if (values [y] [x] == null)
          values [y] [x] = new float [nsub] [nsub] [];
        values [y] [x] [b] [a] = value;
      }
    }

    void traverse (Shader shader)
    {
      double step = 1. / nsub;
      for (int x = 0;x < bbox.width;x++)
        for (int y = 0;y < bbox.height;y++)
          for (int a = 0;a < nsub;a++)
            for (int b = 0;b < nsub;b++)
              set (x,y,a,b,shader.shade (new Point (bbox.x + x + a * step,bbox.y + y + b * step)));
    }

    public void draw (int [] pixels,int background,boolean translucent)
    {
      for (int y = 0,index = 0;y < bbox.height;y++)
        for (int x = 0;x < bbox.width;x++,index++)
        {
          float [] [] [] square = values [y] [x];
          if (square == null)
            pixels [index] = background;
          else
          {
            int n = 0;
            float [] accu = new float [ncomponents];
            for (int sy = 0;sy < nsub;sy++)
              for (int sx = 0;sx < nsub;sx++)
              {
                float [] color = square [sy] [sx];
                if (color != null)
                {
                  n++;
                  for (int i = 0;i < ncomponents;i++)
                    accu [i] += color [i];
                }
              }
            for (int i = 0;i < ncomponents;i++)
              accu [i] /= n; // n shouldn't be zero: square was allocated
            int alpha = Math.round (0xff * n / (float) (nsub * nsub));
            if (function != null)
              accu = function.valueFor (accu);
            else if (functions != null)
            {
              float t = accu [0];
              accu = new float [functions.length];
              for (int i = 0;i < functions.length;i++)
                accu [i] = functions [i].valueFor (new float [] {t}) [0];
              throw new NotTestedException ();
            }
            int rgb = baseColorSpace.mappedBase.toRGB (accu);
            if (translucent)
              rgb |= alpha << 24;
            else if (alpha == 0)
              rgb = background;
            else if (alpha != 0xff)
              for (int shift = 0;shift < 24;shift += 8)
              {
                int fore = (rgb >> shift) & 0xff;
                int back = (background >> shift) & 0xff;
                rgb &= ~(0xff << shift);
                rgb |= ((int) ((alpha * fore + (0xff - alpha) * back) / 255. + .5) << shift);
              }
            pixels [index] = rgb;
          }
        }
    }
  }

  protected Rectangle getBoundingBox (Transformation transform)
  {
    return Rectangle.getInfinitePlane ();
  }

  protected void rasterize (Transformation transform,Subsampler subsampler)
  {
    throw new NotImplementedException ("rasterization for " + getClass ());
  }

  public ImageProducer getImageProducer (Transformation transform,int nsub,Rectangle bbox,boolean useBackground)
  {
    useBackground &= backgroundColor != null;
    if (!useBackground)
      bbox.intersectWith (getBoundingBox (transform));
    if (boundingBox != null)
      bbox.intersectWith (new Rectangle (boundingBox).transformBy (transform));
    Assertions.expect (bbox.isFinite ());
    bbox.round ();
    java.awt.Rectangle box = bbox.toAWTRectangle ();
    int [] pixels = new int [box.width * box.height];
    draw (pixels,transform,nsub,box,useBackground ? baseColorSpace.mappedBase.toRGB (backgroundColor) : 0,!useBackground);
    return new MemoryImageSource (box.width,box.height,new RGBColorModel (!useBackground),pixels,0,box.width);
  }
  
  protected void draw (int [] pixels,Transformation transform,int nsub,java.awt.Rectangle bbox,int background,boolean translucent)
  {
    Subsampler subsampler = new Subsampler (bbox,nsub);
    rasterize (transform,subsampler);
    subsampler.draw (pixels,background,translucent);
  }
}
