package info.joriki.sfnt;

import info.joriki.util.Arrays;

class DataBlock {
	  byte [] data;
	  int offset;
	  
	  public DataBlock (byte [] data) {
		  this.data = data;
	  }
	  
	  public boolean equals (Object o) {
		  return o instanceof DataBlock && Arrays.equals (((DataBlock) o).data,data);
	  }
	  
	  public int hashCode () {
		  int hashCode = 0;
		  for (byte b : data) {
			  hashCode ^= b;
			  hashCode *= 13;
		  }
		  return hashCode;
	  }
}