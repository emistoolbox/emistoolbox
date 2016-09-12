/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

interface JPEGSpeaker
{
  byte RAW     = 0;
  byte FOURIER = 1;
  byte DIRECT  = 2;
  byte COLORS  = 3;
  byte PIXELS  = 4;

  int DCTshift = 3;
  int DCTlength =  1 << DCTshift;
  int DCTsize = DCTlength * DCTlength;
  int DCTmask = DCTlength - 1;

  int huffmanLength = 16;

  int DC = 0;
  int AC = 1;

  // segment types
  int SOF0  = 0xc0;
  int SOF1  = 0xc1;
  int SOF2  = 0xc2;
  int SOF3  = 0xc3;
  
  int SOF5  = 0xc5;
  int SOF6  = 0xc6;
  int SOF7  = 0xc7;
  
  int JPG   = 0xc8;
  int SOF9  = 0xc9;
  int SOF10 = 0xca;
  int SOF11 = 0xcb;
  
  int SOF13 = 0xcd;
  int SOF14 = 0xce;
  int SOF15 = 0xcf;
  
  int DHT   = 0xc4;
  
  int DAC   = 0xcc;
  
  int RST0  = 0xd0;
  int RST1  = 0xd1;
  int RST2  = 0xd2;
  int RST3  = 0xd3;
  int RST4  = 0xd4;
  int RST5  = 0xd5;
  int RST6  = 0xd6;
  int RST7  = 0xd7;
  
  int SOI   = 0xd8;
  int EOI   = 0xd9;
  int SOS   = 0xda;
  int DQT   = 0xdb;
  int DNL   = 0xdc;
  int DRI   = 0xdd;
  int DHP   = 0xde;
  int EXP   = 0xdf;
  
  int APP0  = 0xe0;
  int APP1  = 0xe1;
  int APP2  = 0xe2;
  int APP3  = 0xe3;
  int APP4  = 0xe4;
  int APP5  = 0xe5;
  int APP6  = 0xe6;
  int APP7  = 0xe7;
  int APP8  = 0xe8;
  int APP9  = 0xe9;
  int APP10 = 0xea;
  int APP11 = 0xeb;
  int APP12 = 0xec;
  int APP13 = 0xed;
  int APP14 = 0xee;
  int APP15 = 0xef;
  
  int JPG0  = 0xf0;
  int JPG13 = 0xfd;
  int COM   = 0xfe;
  
  int TEM   = 0x01;
  
  int ERROR = 0x100;

  int ESC = 0xff;
}
