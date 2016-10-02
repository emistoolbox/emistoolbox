package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.common.util.Rectangle;
import com.google.gwt.i18n.client.NumberFormat;

public class CSSCreator {
	private CSSCreator () {}
	
	public static String getCss (ChartColor color) {
		return "rgba(" + color.getRed () + "," + color.getGreen () + "," + color.getBlue () + "," + toString (color.getAlpha () / 255.) + ")";
	}
	
	public static Map<String,String> getCss (ChartFont font) {
		Map<String,String> map = new HashMap<String,String> ();
		map.put ("color",getCss (font.getColor ()));
		map.put ("font-size",getDimension (font.getSize ()));
		map.put ("font-family",font.getName ());

		String style = "normal";
		String weight = "normal";
		switch (font.getStyle ()) {
		case ChartFont.PLAIN : break;
		case ChartFont.BOLD : weight = "bold"; break;
		case ChartFont.ITALIC : style = "italic"; break;
		default: throw new Error ("font style " + font.getStyle () + " not implemented");
		}
		map.put ("font-style",style);
		map.put ("font-weight",weight);
		
		return map;
	}

	public static Map<String,String> getCss (LayoutFrameConfig config) {
		Map<String,String> map = new HashMap<String,String> ();
		
		if (config.getBackgroundColour () != null)
			map.put ("background-color",getCss (config.getBackgroundColour ()));

		if (config.getBorders () != null) {
			LayoutSides<BorderStyle> borders = config.getBorders ();
			List<String> borderWidths = reduceSides (borders,new Extractor<BorderStyle> () {
				public String extract (BorderStyle borderStyle) {
					return getDimension (borderStyle == null ? 0 : borderStyle.getWidth ());
				}
			});
			List<String> borderColors = reduceSides (borders,new Extractor<BorderStyle> () {
				public String extract (BorderStyle borderStyle) {
					return getCss (borderStyle == null ? new ChartColor () : borderStyle.getColour ());
				}
			});
			if (borderWidths.size () == 1 && borderColors.size () == 1)
				map.put ("border",borderWidths.get (0) + " solid " + borderColors.get (0));
			else {
				map.put ("border-width",reduce (borderWidths));
				map.put ("border-style","solid");
				map.put ("border-color",reduce (borderColors));
			}
		}		

		if (config.getPadding () != null)
			map.put ("padding",reduce (reduceSides (config.getPadding (),new Extractor<Double> () {
				public String extract (Double padding) {
					return getDimension (padding);
				}
			})));

		if (config.getBorderRadius () != 0)
			map.put ("border-radius",getDimension (config.getBorderRadius ()));
		
		/*
		if (config.getPosition () != null) {
			Rectangle position = config.getPosition ();
			map.put ("position","absolute");
			map.put ("left",getDimension (position.getLeft ()));
			map.put ("top",getDimension (position.getTop ()));
			map.put ("width",getDimension (position.getWidth ()));
			map.put ("height",getDimension (position.getHeight ()));
		}
		*/
		
		return map;
	}

	public static Map<String,String> getCss (LayoutPageConfig config) {
		Map<String,String> map = new HashMap<String,String> ();
		
		if (config.getBackgroundColour () != null)
			map.put ("background-color",getCss (config.getBackgroundColour ()));

		return map;
	}
	
	private static interface Extractor<T> {
		String extract (T t);
	}
	
	private static <T> List<String> reduceSides (LayoutSides<T> sides,Extractor<T> extractor) {
		List<String> list = new ArrayList<String> ();
		for (T t : sides.getCSSList ())
			list.add (extractor.extract (t));
		// starting with top, right, bottom, left: remove left = right -> bottom = top -> right/left = top/bottom  
		while (list.size () > 1 && list.get (list.size () - 1).equals (list.get (list.size () / 2 - 1)))
			list.remove (list.size () - 1);
		return list;
	}
	
	private static String reduce (List<String> strings) {
		StringBuilder builder = new StringBuilder ();
		for (String string : strings)
			builder.append (' ').append (string);
		return builder.toString ().substring (1);
	}
	
	private static String getDimension (double x) {
		return x == 0 ? "0" : toString (x) + "pt";
	}
	
	private static String toString (double x) {
		NumberFormat format = NumberFormat.getFormat("0.0##"); 
		return format.format(x).replaceAll("\\.?0*$",""); 
	}
}
