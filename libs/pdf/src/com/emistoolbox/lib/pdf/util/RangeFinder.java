package com.emistoolbox.lib.pdf.util;

import info.joriki.util.Range;

import java.util.ArrayList;
import java.util.List;

public class RangeFinder {
	private RangeFinder () {}
	
	public static <T> List<Range> findRanges (T [] items) {
		ArrayList<Range> ranges = new ArrayList<Range> ();
		if (items.length != 0) {
			int i = 0;
			do {
				T t = items [i];
				int beg = i;
				while (++i < items.length && (items [i] == null ? t == null : items [i].equals (t)))
					;
				ranges.add (new Range (beg,i));
			} while (i < items.length);
		}
		return ranges;
	}
}
