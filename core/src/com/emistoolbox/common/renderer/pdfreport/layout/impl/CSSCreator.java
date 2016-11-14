package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.util.LayoutSides;
import com.google.gwt.i18n.client.NumberFormat;

public class CSSCreator {
	private CSSCreator () {}
	
	public static String getCss (ChartColor color) {
		if (color == null)
			return "rgb(0, 0, 0)"; 
		
		return "rgba(" + color.getRed () + "," + color.getGreen () + "," + color.getBlue () + "," + toString (color.getAlpha () / 255.) + ")";
	}

	public static String getCssAsString(Map<String, String> values)
	{
		String result = ""; 
		for (Map.Entry<String, String> entry : values.entrySet())
			result += entry.getKey() + ": " + entry.getValue() + ";"; 

		return result; 
	}
	
	public static String getCssAsString(ChartFont font) {
		return getCssAsString(getCss(font)); 
	}
	
	public static Map<String,String> getCss (ChartFont font) {
		Map<String,String> map = new HashMap<String,String> ();
		if (font == null)
			return map; 
		
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

	public static String getCssAsString(LayoutFrameConfig config) {
		return getCssAsString(getCss(config)); 
	}

	public static String getCssAsString(BorderStyle border)
	{
		if (border == null || border.getWidth() == 0)
			return null; 
		
		if (border.getColour() == null)
			border.setColor(new ChartColor(0, 0, 0));  
		
		return getCssAsString(getCss(border)); 
	}
	
	public static Map<String,String> getCss (BorderStyle border) {
		return Collections.singletonMap ("border",getSolidBorder (border));
	}
	
	public static String getCssAsString(ChartFont font, ChartColor bgColor, BorderStyle border)
	{ return getCssAsString(getCss(font, bgColor, border)); }
	
	public static Map<String, String> getCss(ChartFont font, ChartColor bgColor, BorderStyle border)
	{
		Map<String, String> result = new HashMap<String, String>(); 
		result.putAll(getCss(font)); 
		result.put("background-color", getCss(bgColor)); 
		result.putAll(getCss(border)); 
		
		return result; 
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
					return getCss (borderStyle == null || borderStyle.getColour () == null ? new ChartColor () : borderStyle.getColour ());
				}
			});
			if (borderWidths.size () == 1 && borderColors.size () == 1)
				map.put ("border",getSolidBorder (borderWidths.get (0),borderColors.get (0)));
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

	public static String getCssAsString(LayoutPageConfig config) {
		return getCssAsString(getCss(config)); 
	}
	

	public static Map<String,String> getCss (LayoutPageConfig config) {
		Map<String,String> map = new HashMap<String,String> ();
		
		if (config.getBackgroundColour () != null)
			map.put ("background-color",getCss (config.getBackgroundColour ()));

		return map;
	}
	
	private static String getSolidBorder (String width,String color) {
		return  width + " solid " + color;
	}

	private static String getSolidBorder (BorderStyle border) {
		return getSolidBorder (getDimension (border.getWidth ()),getCss (border.getColour ()));
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
