/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

// This is utterly unoptimized.
// getInputValue is particularly horrible.

package info.joriki.awt.image.jpeg;

import java.io.IOException;

import info.joriki.util.Assertions;

public class JPEGUpSampler extends JPEGResampler
{
  JPEGRequest deallocationRequest = new JPEGRequest (DIRECT);

  boolean linearInterpolation;

  public JPEGUpSampler ()
  {
    this (false);
  }
  
  public JPEGUpSampler (boolean linearInterpolation)
  {
    this.linearInterpolation = linearInterpolation;
  }

  public void setFormat (JPEGFormat inputFormat)
  {
    this.inputFormat = inputFormat;

    MCULayout inputLayout = inputFormat.layout;

    MCULayout outputLayout = new MCULayout (inputLayout);
    outputLayout.tableSpec = inputLayout.tableSpec;

    JPEGComponent [] inputComponents = inputLayout.components;
    for (int i = 0;i < inputComponents.length;i++)
      outputLayout.components [i] = new JPEGComponent (inputComponents [i].id,1,1,inputComponents [i].quant);

    outputLayout.calculateDimensions ();

    setFactors (outputLayout,inputLayout);

    outputFormat = new JPEGFormat 
      (outputLayout,
       inputFormat.pixelWidth,
       inputFormat.pixelHeight);
  }

  final private float getInputValue (int component,int ix,int iy,int hSamp,int vSamp)
  {
    // if the point is outside the image, use the nearest inside the image.
    if (x == 0 && ix < 0)
      ix = 0;
    else if (x == inputFormat.width - 1 && ix * hfac / hSamp >= inputFormat.lastWidth)
      ix = ((inputFormat.lastWidth + hfac / hSamp - 1) * hSamp) / hfac - 1;
    if (y == 0 && iy < 0)
      iy = 0;
    else if (y == inputFormat.height - 1 && iy * vfac / vSamp >= inputFormat.lastHeight)
      iy = ((inputFormat.lastHeight + vfac / vSamp - 1) * vSamp) / vfac - 1;

    return inputFormat.mcus
      [y + (iy + (vSamp << 3)) / (vSamp << 3) - 1]
      [x + (ix + (hSamp << 3)) / (hSamp << 3) - 1].floatData
      [component] [((iy >> 3) & (vSamp - 1)) * hSamp + ((ix >> 3) & (hSamp - 1))]
      [((iy & 7) << 3) + (ix & 7)];
  }

  public int crank () throws IOException
  {
    if (linearInterpolation)
      {
        readRequest.x = Math.max (x - 1,0);
        readRequest.y = Math.max (y - 1,0);
        readRequest.width  = Math.min (x + 2,inputFormat.width)  - readRequest.x;
        readRequest.height = Math.min (y + 2,inputFormat.height) - readRequest.y;
      }
    else
      {
        readRequest.x = x;
        readRequest.y = y;
      }

    int eod = source.readRequest (readRequest);
    if (eod != OK)
      return eod;

    int wx = writeRequest.x = hfac * x;
    int wy = writeRequest.y = vfac * y;

    writeRequest.width  = Math.min (hfac,outputFormat.width  - wx);
    writeRequest.height = Math.min (vfac,outputFormat.height - wy);
    
    sink.allocationRequest (writeRequest);
    
    MCU [] [] outputMCUs = outputFormat.mcus;
    JPEGComponent [] components = inputFormat.layout.components;

    for (int i = 0;i < components.length;i++)
      {
        int vSamp = components [i].vSamp;
        int hSamp = components [i].hSamp;

        float [] [] inputs = inputFormat.mcus [y] [x].floatData [i];

        // There is a special case here when the upsampling would
        // create MCUs at the edges that are entirely outside
        // the image. The output format is set up not to contain
        // these, since otherwise all other code would have to
        // deal with the case of the missing MCU.
        // This case is not too common since often image sizes
        // are multiples of eight, and anyway it occurs only at
        // the edges; we therefore proceed as if it can't occur
        // and catch the ArrayIndexOutOfBoundsExceptions that
        // are thrown.

        if (vSamp == vfac && hSamp == hfac) // just copy
          {
            for (int y = 0,index = 0;y < vSamp;y++)
              for (int x = 0;x < hSamp;x++,index++)
                // here we need to try/catch inside the loop
                // so that index is updated correctly.
                try {
                  System.arraycopy (inputs [index],0,outputMCUs [wy + y] [wx + x].floatData [i] [0],0,DCTsize);
                } catch (ArrayIndexOutOfBoundsException aioobe) {}
          }
        else if (linearInterpolation)
          // This is utterly unoptimized.
          {
            double dy = vSamp / (double) vfac;
            double y = (dy - 1) / 2;
            for (int py = 0;py < vdim;py++,y += dy)
              {
                double dx = hSamp / (double) hfac;
                double x = (dx - 1) / 2;
                int iy2 = (int) (y + 1);
                int iy1 = iy2 - 1;
                double cy2 = y - iy1;
                double cy1 = iy2 - y;
                try {
                  for (int px = 0;px < hdim;px++,x += dx)
                    {
                      int ix2 = (int) (x + 1);
                      int ix1 = ix2 - 1;
                      double cx2 = x - ix1;
                      double cx1 = ix2 - x;
                      outputMCUs [wy + (py >> 3)] [wx + (px >> 3)].floatData [i] [0] [((py & 7) << 3) + (px & 7)] = (float)
                        (cx1 * cy1 * getInputValue (i,ix1,iy1,hSamp,vSamp) +
                         cx1 * cy2 * getInputValue (i,ix1,iy2,hSamp,vSamp) +
                         cx2 * cy1 * getInputValue (i,ix2,iy1,hSamp,vSamp) +
                         cx2 * cy2 * getInputValue (i,ix2,iy2,hSamp,vSamp));
                    }
                } catch (ArrayIndexOutOfBoundsException aioobe) {}
              }
          }
        else
          {
            // I've never seen a case where a component had sampling factors
            // in between minimal (1,1) and maximal (hfac,vfac)
            Assertions.expect (vSamp,1);
            Assertions.expect (hSamp,1);
            float [] input = inputs [0];
            for (int py = 0;py < vdim;py++)
              try {
                for (int px = 0;px < hdim;px++)
                  outputMCUs [wy + (py >> 3)] [wx + (px >> 3)].floatData [i] [0] [((py & 7) << 3) + (px & 7)] = input [((py / vfac) << 3) + (px / hfac)];
              } catch (ArrayIndexOutOfBoundsException aioobe) {}
          }
      }
    
    if (x > 0 && y > 0)
      deallocate (1,1);
    if (x == inputFormat.width - 1 && y > 0)
      deallocate (0,1);
    if (y == inputFormat.height - 1 && x > 0)
      deallocate (1,0);
    if (x == inputFormat.width - 1 && y == inputFormat.height - 1)
      deallocate (0,0);

    sink.writeRequest (writeRequest);

    if (++x == inputFormat.width)
      {
        x = 0;
        y++;
      }

    return OK;
  }

  void deallocate (int dx,int dy)
  {
    deallocationRequest.x = x - dx;
    deallocationRequest.y = y - dy;
    source.deallocationRequest (deallocationRequest);
  }
}
