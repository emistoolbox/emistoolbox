/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class TriangleShading extends MeshShading
{
  public class Vertex
  {
    public float [] coors;
    public float [] color;
    public float [] value;
    
    void computeValue ()
    {
      value = hasFunction () ? color : (float []) baseColorSpace.mappedBase.toRGBArray (color).clone ();
    }
  }

  public List triangles;
  private List vertices;

  TriangleShading (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    // Type 5 uses Table 4.30 but isn't implemented.
    super (dictionary,resourceResolver,"4.29");
  }

  protected Vertex readVertex () throws IOException
  {
    Vertex vertex = new Vertex ();
    vertex.coors = readPoint ();
    vertex.color = readColor ();
    vertices.add (vertex);
    return vertex; 
  }

  protected void readData () throws IOException
  {
    triangles = new ArrayList ();
    vertices = new ArrayList ();

    if (type == FREE_TRIANGLE)
      {
        Assertions.unexpect (bitsPerFlag,0);

        Vertex [] previousVertices = null;

        while (moreData ())
          {
            Vertex [] vertices = new Vertex [3];
            switch (readFlag ())
              {
              case 0:
                vertices [0] = readVertex ();
                readFlag ();
                vertices [1] = readVertex ();
                readFlag ();
                break;
              case 1:
                vertices [0] = previousVertices [1];
                vertices [1] = previousVertices [2];
                break;
              case 2:
                vertices [0] = previousVertices [0];
                vertices [1] = previousVertices [2];
                break;
              default:
                throw new Error ("invalid flag");
              }
            vertices [2] = readVertex ();
            triangles.add (vertices);
            previousVertices = vertices;
          }
      }
    else
      throw new NotImplementedException ("triangle shading type " + type);
  }

  public void computeValues ()
  {
    for (int i = 0;i < vertices.size ();i++)
      ((Vertex) vertices.get (i)).computeValue ();
  }
}
