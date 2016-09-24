package com.emistoolbox.lib.data;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class SQLUtil {
	private SQLUtil () {}
	
	public static void insert (PrintStream out,String tableName,int id,String ... values) {
		insert (out,tableName,id,Arrays.asList (values));
	}
	
	public static void insert (PrintStream out,String tableName,int id,List<String> values) {
		out.println ("INSERT INTO " + tableName + " ");
		out.print ("VALUES (");
		out.print (id);
		for (String value : values) {
			out.print (',');
			if (value == null)
				out.print ("NULL");
			else {
				out.print ('\'');
				out.print (value);
				out.print ('\'');
			}
		}
		out.println (");");
	}
}
