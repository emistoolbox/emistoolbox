/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;

import java.util.Stack;

import info.joriki.io.filter.AtomicBuffer;

import info.joriki.util.Assertions;
import info.joriki.util.DebugStack;

public class JPEGBuffer extends AtomicBuffer implements JPEGSource, JPEGSink
{
  final static boolean debugging = false;

  JPEGFormat format;
  MCU [] [] mcus;
  Stack mcuStack = debugging ? new DebugStack ("mcu") : new Stack ();

  boolean [] [] available;
  boolean [] [] requested;
  int pendingRequests;
  int navailable = 0;

  public JPEGBuffer () {}

  public JPEGBuffer (JPEGFormat format)
  {
    setFormat (format);
  }

  public void setFormat (JPEGFormat format)
  {
    this.format = format;
    this.mcus = format.mcus;
    available = new boolean [format.height] [format.width];
    requested = new boolean [format.height] [format.width];
  }

  public void allocationRequest (JPEGRequest request)
  {
    if (debugging)
      System.out.println (this + " allocation request : " + request);
    for (int y = request.y;y < request.y + request.height;y++)
      for (int x = request.x;x < request.x + request.width;x++)
        {
          Assertions.expect (!available [y] [x]);
          mcus [y] [x] = mcuStack.isEmpty () ?
            new MCU (format.layout) :
            (MCU) mcuStack.pop ();
          mcus [y] [x].allocate (request.type);
        }
  }

  public void deallocationRequest (JPEGRequest request)
  {
    if (debugging)
      System.out.println (this + " deallocation request : " + request);
    for (int y = request.y;y < request.y + request.height;y++)
      for (int x = request.x;x < request.x + request.width;x++)
        {
          Assertions.expect (available [y] [x]);
          mcus [y] [x].deallocate (request.type);
          mcuStack.push (mcus [y] [x]);
          mcus [y] [x] = null;
          available [y] [x] = false;
        }

    navailable -= request.width * request.height;
  }

  public void writeRequest (JPEGRequest request) throws IOException
  {
    if (debugging)
      System.out.println (this + " write request : " + request);
    for (int y = request.y;y < request.y + request.height;y++)
      for (int x = request.x;x < request.x + request.width;x++)
        {
          Assertions.expect (mcus [y] [x].type,request.type);
          Assertions.expect (!available [y] [x]);
          available [y] [x] = true;
          if (requested [y] [x])
            {
              requested [y] [x] = false;
              pendingRequests--;
            }
        }

    navailable += request.width * request.height;

    if (outputCrank != null)
      while (pendingRequests == 0 && navailable != 0 && outputCrank.crank () == OK)
        ;
  }

  public int readRequest (JPEGRequest request) throws IOException
  {
    if (debugging)
      System.out.println (this + " read request : " + request);
    // count how many of the requested MCUs are not available yet
    pendingRequests = 0;
    for (int y = request.y;y < request.y + request.height;y++)
      for (int x = request.x;x < request.x + request.width;x++)
        if (!available [y] [x])
          {
            requested [y] [x] = true;
            pendingRequests++;
          }

    if (pendingRequests != 0)
      {
        // some are not available.
        // If we don't have an input crank, that just means end of data.
        if (inputCrank == null)
          return EOD;
        // if we do, crank it like mad
        while (inputCrank.crank () == OK && pendingRequests != 0)
          ;
        // if there are still pending requests, tough luck
        if (pendingRequests != 0)
          return EOI;
      }

    // now all requests are satified.
    // transform them to the right data type.
    for (int y = request.y;y < request.y + request.height;y++)
      for (int x = request.x;x < request.x + request.width;x++)
        mcus [y] [x].transformToMeet (request);

    return OK;
  }
}
