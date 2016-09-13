/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

class JPEGCosineTransform implements JPEGSpeaker
{
  final static float c2 = (float) Math.cos (1*Math.PI/DCTlength);
  final static float c4 = (float) Math.cos (2*Math.PI/DCTlength);
  final static float c6 = (float) Math.cos (3*Math.PI/DCTlength);

  static void transform (float [] data)
  {
    float tmp0,tmp1,tmp2,tmp3,tmp4,tmp5,tmp6,tmp7;
    float tmp10,tmp11,tmp12,tmp13;
    float z1,z2,z3,z4,z5;
    float z11,z13;

    int i0,i1,i2,i3,i4,i5,i6,i7;

    for (int pass = 0;pass < 2;pass++)
      {
        int step = pass == 0 ? DCTlength : 1;
        int incr = pass == 0 ? 1 : DCTlength;
        int lim  = DCTlength * incr;

        for (int off = 0;off < lim;off += incr)
          {
            i7 = step +
              (i6 = step + 
               (i5 = step + 
                (i4 = step + 
                 (i3 = step + 
                  (i2 = step + 
                   (i1 = step + 
                    (i0 = off)))))));

            tmp0 = data [i0] + data [i7];
            tmp7 = data [i0] - data [i7];
            tmp1 = data [i1] + data [i6];
            tmp6 = data [i1] - data [i6];
            tmp2 = data [i2] + data [i5];
            tmp5 = data [i2] - data [i5];
            tmp3 = data [i3] + data [i4];
            tmp4 = data [i3] - data [i4];

            /* Even part */
    
            tmp10 = tmp0 + tmp3;/* phase 2 */
            tmp13 = tmp0 - tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;
    
            data [i0] = tmp10 + tmp11; /* phase 3 */
            data [i4] = tmp10 - tmp11;
    
            z1 = c4 * (tmp12 + tmp13);
            data [i2] = tmp13 + z1;/* phase 5 */
            data [i6] = tmp13 - z1;
    
            /* Odd part */
    
            tmp10 = tmp4 + tmp5;/* phase 2 */
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;
    
            /* The rotator is modified from fig 4-8 to avoid extra negations. */
            z5 = c6 * (tmp10 - tmp12);
            z2 = (c2 - c6) * tmp10 + z5;
            z4 = (c2 + c6) * tmp12 + z5;
            z3 = c4 * tmp11;
    
            z11 = tmp7 + z3;/* phase 5 */
            z13 = tmp7 - z3;
    
            data [i5] = z13 + z2;/* phase 6 */
            data [i3] = z13 - z2;
            data [i1] = z11 + z4;
            data [i7] = z11 - z4;
          }
      }

    renormalize (data);
  }

  static void inverseTransform (float [] data)
  {
    float tmp0,tmp1,tmp2,tmp3,tmp4,tmp5,tmp6,tmp7;
    float tmp10,tmp11,tmp12,tmp13;
    float z10,z11,z12,z13;
    float delta;

    int i0,i1,i2,i3,i4,i5,i6,i7;

    for (int pass = 0;pass < 2;pass++)
      {
        int step = pass == 0 ? DCTlength : 1;
        int incr = pass == 0 ? 1 : DCTlength;
        int lim  = DCTlength * incr;

        for (int off = 0;off < lim;off += incr)
          {
            i7 = step +
              (i6 = step + 
               (i5 = step + 
                (i4 = step + 
                 (i3 = step + 
                  (i2 = step + 
                   (i1 = step + 
                    (i0 = off)))))));
            /* Even part */

            tmp0 = data [i0];
            tmp1 = data [i2];
            tmp2 = data [i4];
            tmp3 = data [i6];

            tmp10 = tmp0 + tmp2;/* phase 3 */
            tmp11 = tmp0 - tmp2;
    
            tmp13 = tmp1 + tmp3;/* phases 5-3 */
            tmp12 = (2*c4) * (tmp1 - tmp3) - tmp13;
    
            tmp0 = tmp10 + tmp13;/* phase 2 */
            tmp3 = tmp10 - tmp13;
            tmp1 = tmp11 + tmp12;
            tmp2 = tmp11 - tmp12;
    
            /* Odd part */
    
            tmp4 = data [i1];
            tmp5 = data [i3];
            tmp6 = data [i5];
            tmp7 = data [i7];

            z13 = tmp6 + tmp5;/* phase 6 */
            z10 = tmp6 - tmp5;
            z11 = tmp4 + tmp7;
            z12 = tmp4 - tmp7;
    
            tmp7 = z11 + z13;/* phase 5 */
            tmp11 = (2*c4) * (z11 - z13);
    
            delta = (2*c2) * (z10 + z12);
            tmp10 = (2*(c2 - c6)) * z12 - delta;
            tmp12 = (-2*(c2+c6)) * z10 + delta;

            tmp6 = tmp12 - tmp7;/* phase 2 */
            tmp5 = tmp11 - tmp6;
            tmp4 = tmp10 + tmp5;

            data [i0] = tmp0 + tmp7;
            data [i7] = tmp0 - tmp7;
            data [i1] = tmp1 + tmp6;
            data [i6] = tmp1 - tmp6;
            data [i2] = tmp2 + tmp5;
            data [i5] = tmp2 - tmp5;
            data [i4] = tmp3 + tmp4;
            data [i3] = tmp3 - tmp4;
          }
      }

    renormalize (data);
  }

  static void transform (float [] inData,float [] outData,int inOff,int inScan)
  {
    float tmp0,tmp1,tmp2,tmp3,tmp4,tmp5,tmp6,tmp7;
    float tmp10,tmp11,tmp12,tmp13;
    float z1,z2,z3,z4,z5;
    float z11,z13;

    int i0,i1,i2,i3,i4,i5,i6,i7;

    for (int pass = 0;pass < 2;pass++)
      {
        float [] srcData = inData;
        float [] destData = pass == 0 ? inData : outData;
        int step = pass == 0 ? inScan : 1;
        int inIncr = pass == 0 ? 1 : inScan;
        int outIncr = pass == 0 ? 1 : DCTlength;
        int diff = pass == 0 ? 0 : -inOff;
        int diffIncr = outIncr - inIncr;
        int lim = inOff + DCTlength * inIncr;

        for (int off = inOff;off < lim;off += inIncr,diff += diffIncr)
          {
            i7 = step +
              (i6 = step + 
               (i5 = step + 
                (i4 = step + 
                 (i3 = step + 
                  (i2 = step + 
                   (i1 = step + 
                    (i0 = off)))))));

            tmp0 = srcData [i0] + srcData [i7];
            tmp7 = srcData [i0] - srcData [i7];
            tmp1 = srcData [i1] + srcData [i6];
            tmp6 = srcData [i1] - srcData [i6];
            tmp2 = srcData [i2] + srcData [i5];
            tmp5 = srcData [i2] - srcData [i5];
            tmp3 = srcData [i3] + srcData [i4];
            tmp4 = srcData [i3] - srcData [i4];

            /* Even part */
    
            tmp10 = tmp0 + tmp3;/* phase 2 */
            tmp13 = tmp0 - tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;
    
            destData [i0 + diff] = tmp10 + tmp11; /* phase 3 */
            destData [i4 + diff] = tmp10 - tmp11;
    
            z1 = c4 * (tmp12 + tmp13);
            destData [i2 + diff] = tmp13 + z1;/* phase 5 */
            destData [i6 + diff] = tmp13 - z1;
    
            /* Odd part */
    
            tmp10 = tmp4 + tmp5;/* phase 2 */
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;
    
            /* The rotator is modified from fig 4-8 to avoid extra negations. */
            z5 = c6 * (tmp10 - tmp12);
            z2 = (c2 - c6) * tmp10 + z5;
            z4 = (c2 + c6) * tmp12 + z5;
            z3 = c4 * tmp11;
    
            z11 = tmp7 + z3;/* phase 5 */
            z13 = tmp7 - z3;
    
            destData [i5 + diff] = z13 + z2;/* phase 6 */
            destData [i3 + diff] = z13 - z2;
            destData [i1 + diff] = z11 + z4;
            destData [i7 + diff] = z11 - z4;
          }
      }

    renormalize (outData);
  }

  static void inverseTransform (float [] inData,float [] outData,int outOff,int outScan)
  {
    renormalize (inData);

    float tmp0,tmp1,tmp2,tmp3,tmp4,tmp5,tmp6,tmp7;
    float tmp10,tmp11,tmp12,tmp13;
    float z10,z11,z12,z13;
    float delta;

    int i0,i1,i2,i3,i4,i5,i6,i7;

    for (int pass = 0;pass < 2;pass++)
      {
        float [] srcData = inData;
        float [] destData = pass == 0 ? inData : outData;
        int step = pass == 0 ? DCTlength : 1;
        int inIncr = pass == 0 ? 1 : DCTlength;
        int outIncr = pass == 0 ? 1 : outScan;
        int diff = pass == 0 ? 0 : outOff;
        int diffIncr = outIncr - inIncr;
        int lim = DCTlength * inIncr;

        for (int off = 0;off < lim;off += inIncr,diff += diffIncr)
          {
            i7 = step +
              (i6 = step + 
               (i5 = step + 
                (i4 = step + 
                 (i3 = step + 
                  (i2 = step + 
                   (i1 = step + 
                    (i0 = off)))))));
            /* Even part */

            tmp0 = srcData [i0];
            tmp1 = srcData [i2];
            tmp2 = srcData [i4];
            tmp3 = srcData [i6];

            tmp10 = tmp0 + tmp2;/* phase 3 */
            tmp11 = tmp0 - tmp2;
    
            tmp13 = tmp1 + tmp3;/* phases 5-3 */
            tmp12 = (2*c4) * (tmp1 - tmp3) - tmp13;
    
            tmp0 = tmp10 + tmp13;/* phase 2 */
            tmp3 = tmp10 - tmp13;
            tmp1 = tmp11 + tmp12;
            tmp2 = tmp11 - tmp12;
    
            /* Odd part */
    
            tmp4 = srcData [i1];
            tmp5 = srcData [i3];
            tmp6 = srcData [i5];
            tmp7 = srcData [i7];

            z13 = tmp6 + tmp5;/* phase 6 */
            z10 = tmp6 - tmp5;
            z11 = tmp4 + tmp7;
            z12 = tmp4 - tmp7;
    
            tmp7 = z11 + z13;/* phase 5 */
            tmp11 = (2*c4) * (z11 - z13);
    
            delta = (2*c2) * (z10 + z12);
            tmp10 = (2*(c2 - c6)) * z12 - delta;
            tmp12 = (-2*(c2+c6)) * z10 + delta;

            tmp6 = tmp12 - tmp7;/* phase 2 */
            tmp5 = tmp11 - tmp6;
            tmp4 = tmp10 + tmp5;

            destData [i0 + diff] = tmp0 + tmp7;
            destData [i7 + diff] = tmp0 - tmp7;
            destData [i1 + diff] = tmp1 + tmp6;
            destData [i6 + diff] = tmp1 - tmp6;
            destData [i2 + diff] = tmp2 + tmp5;
            destData [i5 + diff] = tmp2 - tmp5;
            destData [i4 + diff] = tmp3 + tmp4;
            destData [i3 + diff] = tmp3 - tmp4;
          }
      }
  }

  final static double norm = 1. / DCTlength;

  final static void renormalize (float [] data)
  {
    for (int i = 0;i < DCTsize;i++)
      data [i] *= norm;
  }
}

