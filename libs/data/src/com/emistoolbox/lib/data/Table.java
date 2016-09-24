package com.emistoolbox.lib.data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

// This is derived from some old code I had and may have some functionality that we don't need here
public class Table {
	public List<List<String>> rows = new ArrayList<List<String>> ();
	List<String> labels;
	String filename;
	
	public void readCSV (String file) throws IOException {
		readCSV (file,',');
	}
	
	public void readCSV (String file,char delimiter) throws IOException {
		setFilename (file);
		List<String> row = null;
		PushbackReader in = new PushbackReader (new FileReader (file));
		StringBuilder builder = null;
		int ncolumns = -1;	
		boolean inQuote = false;
		boolean rowEmpty = true;

		try {
			for (;;) {
				int c = in.read ();
				if (c < 0) {
					if (row == null)
						break;
					if (row.size () == ncolumns) {
						rows.add (row);
						break;
					}
				}
				if (c == '#' && row == null) {
					while (c != '\n' && c != '\r')
						c = in.read ();
				}
				if (row == null) {
					if (c == '\n' || c == '\r')
						continue;
					row = new ArrayList<String> ();
					builder = new StringBuilder ();
				}
				if (inQuote) {
					if (c == '"') {
						c = in.read ();
						if (c != '"') {
							if (c >= 0)
								in.unread (c);
							inQuote = false;
							continue;
						}
					}
				}
				else {
					if (c < 0)
						c = '\n';
					if (c != '\n' && c != '\r')
						rowEmpty = false;
					switch (c) {
					case '"' :
						inQuote = true;
						continue;
					case '\n':
					case '\r':
						if (rowEmpty)
							continue;
						row.add (builder.toString ());
						if (ncolumns < 0)
							ncolumns = row.size ();
						else if (row.size () != ncolumns)
							throw new Error ("varying column count : " + row);
						rows.add (row);
						row = null;
						rowEmpty = true;
						continue;
					default:
					    if (c == delimiter) {
					    	row.add (builder.toString ());
					    	builder = new StringBuilder ();
					    	continue;
					    }
					}
				}
				builder.append ((char) c);
			}
		} finally {
			in.close ();
		}
	}
	
	public void writeCSV (String file) throws IOException {
		FileWriter writer = new FileWriter (file);
		boolean firstRow = true;
		
		for (List<String> row : rows) {
			if (firstRow)
				firstRow = false;
			else
				writer.write ('\n');
			boolean first = true;
			for (String entry : row) {
				if (first)
					first = false;
				else
					writer.write (',');
				if (entry.indexOf (',') < 0 && entry.indexOf ('\n') < 0 && entry.indexOf ('"') < 0)
					writer.write (entry);
				else {
					writer.write ('"');
					writer.write (entry.replace ("\"","\"\""));
					writer.write ('"');
				}
			}
		}
		
		writer.close ();
	}

	private int [] getIndices (Table table) {
		return getIndices (table.labels);
	}
	
	private int [] getIndices (List<String> labels) {
		List<String> myLabels = labels;
		int [] indices = new int [labels.size ()];
		for (int i = 0;i < indices.length;i++) {
			indices [i] = myLabels.indexOf (labels.get (i));
			if (indices [i] < 0) {
				System.out.println ("can't find label '" + labels.get (i) + "' in labels " + myLabels);
				throw new Error ();
			}
		}
		return indices;
	}
	
	private int [] getIndices (String ... labels) {
		List<String> myLabels = this.labels;
		int [] indices = new int [labels.length];
		for (int i = 0;i < indices.length;i++) {
			indices [i] = myLabels.indexOf (labels [i]);
			if (indices [i] < 0) {
				System.out.println ("can't find label '" + labels [i] + "' in labels " + myLabels);
				throw new Error ();
			}
		}
		return indices;
	}
	
	public int getIndex (String label) {
		return getIndices (label) [0];
	}
	
	public int getRowCount () {
		return rows.size ();
	}
	
	public int getColumnCount () {
		return rows.isEmpty () ? 0 : rows.get (0).size ();
	}
	
	public Table select (String ... labels) {
		return select (getIndices (labels));
	}
	
	public Table select (List<String> labels) {
		return select (getIndices (labels));
	}
	
	public Table select (int ... indices) {
		Table table = new Table ();
		for (List<String> row : rows) {
			List<String> newRow = new ArrayList<String> ();
			for (int j : indices)
				newRow.add (row.get (j));
			table.rows.add (newRow);
		}
		table.setFilename (filename);
		return table;
	}
	
	public void setLabelRowIndex (int labelRowIndex) {
		labels = rows.get (labelRowIndex);
		while (labelRowIndex-- >= 0)
			rows.remove (0);
	}

	public void normalise () {
		for (List<String> row : rows)
			for (int j = 0;j < row.size ();j++)
				row.set (j,normaliseCase (row.get (j)).trim ());
	}
	
	static String normaliseCase (String s) {
		char [] chars = s.toCharArray ();
		boolean wasLetter = false;
		for (int i = 0;i < chars.length;i++) {
			boolean isLetter = Character.isLetter (chars [i]);
			if (wasLetter && isLetter)
				chars [i] = Character.toLowerCase (chars [i]);
			wasLetter = isLetter;
		}
		return new String (chars);
	}

	public String getFilename () {
		return filename;
	}

	public void setFilename (String filename) {
		this.filename = filename;
	}
}
