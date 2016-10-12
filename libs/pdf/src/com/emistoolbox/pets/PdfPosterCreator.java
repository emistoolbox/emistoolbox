package com.emistoolbox.pets;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutCoordinatePlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutImageElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableFormat;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;
import com.emistoolbox.lib.pdf.util.CMYKColor;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;

public class PdfPosterCreator {
	// pet poster detail sizes
	final private static int NORMAL = 1;
	final private static int BIGGER = 2;
	
	final private static double correctionFactor = .08; // correct for white space that's part of the "L" in "LOST PET" 
	final private static double gap = 18;
	final private static double chipLogoRatio = 0.4;
	final private static double slipSize = 150;
	final private static int slipCount = 10;
	final private static double halfLineWidth = 0.5;
	final private static double extraLeftSpace = 10;
	
	private String subtitleText = "Have you seen me?";
	private String chipText = "I am microchipped and registered with";
	private String footText = "PetLink is available 24/7/365 for lost & found pets!";
	private String missingSinceText = "Missing Since";
	private String lastSeenText = "Last Seen";

	private PDFLayoutFont chipFont = new PDFLayoutFont ("Helvetica",12,PDFLayoutFontStyle.PLAIN,Color.WHITE); // size isn't used, this gets scaled
	private PDFLayoutFont footFont = new PDFLayoutFont ("Helvetica", 9,PDFLayoutFontStyle.PLAIN);
	private PDFLayoutFont slipFont = new PDFLayoutFont ("Helvetica",10,PDFLayoutFontStyle.PLAIN);
	private PDFLayoutFont timeFont = new PDFLayoutFont ("Helvetica",20,PDFLayoutFontStyle.BOLD );
	private PDFLayoutFont nameFont = new PDFLayoutFont ("Helvetica",36,PDFLayoutFontStyle.PLAIN);
	
	private String contactFontName = "Helvetica";
	private String tableFontName = "Helvetica";
	
	private Color highlightColor = new CMYKColor (0,0.9f,0.86f,0);
	private String slipPhone = "1-877-738-5465";
	private String slipAddress = "www.petlink.net/us";
	private String URI = "https://" + slipAddress;
	private PDFLayoutLineStyle tableBorderStyle = null;
	private IOInput logo;

	public void setSubtitleText (String subtitleText) {
		this.subtitleText = subtitleText;
	}

	public void setChipText (String chipText) {
		this.chipText = chipText;
	}

	public void setFootText (String footText) {
		this.footText = footText;
	}

	public void setChipFont (PDFLayoutFont chipFont) {
		this.chipFont = chipFont;
	}

	public void setFootFont (PDFLayoutFont footFont) {
		this.footFont = footFont;
	}

	public void setSlipFont (PDFLayoutFont slipFont) {
		this.slipFont = slipFont;
	}

	public void setTimeFont (PDFLayoutFont timeFont) {
		this.timeFont = timeFont;
	}

	public void setContactFontName (String contactFontName) {
		this.contactFontName = contactFontName;
	}

	public void setTableFontName (String tableFontName) {
		this.tableFontName = tableFontName;
	}

	public void setHighlightColor (Color highlightColor) {
		this.highlightColor = highlightColor;
	}

	public void setSlipPhone (String slipPhone) {
		this.slipPhone = slipPhone;
	}

	public void setSlipAddress (String slipAddress) {
		this.slipAddress = slipAddress;
	}

	public void setURI (String uRI) {
		URI = uRI;
	}

	public void setTableBorderStyle (PDFLayoutLineStyle tableBorderStyle) {
		this.tableBorderStyle = tableBorderStyle;
	}

	public void setLogo (IOInput logo) {
		this.logo = logo;
	}

	final private static double [] tableFontSizes = {14,16,14,14}; 
	
	public void render (IOOutput target,IOInput image,int design,List<PetPosterDetail> details,Point pageSize, Rectangle margins) throws IOException {
		PDFLayoutImageElement imageElement = new PDFLayoutImageElement (image);

		PDFLayoutFont tableFont = new PDFLayoutFont (tableFontName,tableFontSizes [design],PDFLayoutFontStyle.PLAIN);

		List<PDFLayout> pages = new ArrayList<PDFLayout> ();
		PDFLayout page = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (pageSize.x - margins.xmin - margins.xmax,pageSize.y - margins.ymin - margins.ymax);
		outerFrame.pad (margins.xmin,margins.ymin,margins.xmax,margins.ymax);

		PDFLayoutFrameElement chipLogoBox = new PDFLayoutFrameElement (1,chipLogoRatio); // will be scaled
		
		chipLogoBox.addElement (new PDFLayoutTextElement (chipText,chipFont).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (8,4).color (highlightColor).fit (PDFLayoutObjectFit.CONTAIN));
		chipLogoBox.addElement (new PDFLayoutPDFElement (logo).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).fit (PDFLayoutObjectFit.CONTAIN).pad (7));

		PDFLayoutFrameElement findBox = new PDFLayoutFrameElement (1000,150); // width not used, just large enough so text doesn't wrap
		
		String [] textStrings = {
				"IF YOU FIND ME",
				"please call",
				"1-877-PETLINK",
				"(" + slipPhone + ")",
				"or file a \"found pet report\" at",
				URI
		};

		PDFLayoutFontStyle [] fontStyles = {
				PDFLayoutFontStyle.BOLD,
				PDFLayoutFontStyle.PLAIN,
				PDFLayoutFontStyle.BOLD,
				PDFLayoutFontStyle.PLAIN,
				PDFLayoutFontStyle.PLAIN,
				PDFLayoutFontStyle.PLAIN
		};

		double [] fontSizes = {
				18,16,18,14,14,14	
		};

		double [] paddings = {
				5,5,5,5,10,5
		};

		Color [] colors = {
				Color.BLACK,Color.BLACK,highlightColor,Color.BLACK,Color.BLACK,highlightColor	
		};

		for (int i = 0;i < 6;i++)
			findBox.addElement (new PDFLayoutTextElement (textStrings [i],new PDFLayoutFont (contactFontName,fontSizes [i],fontStyles [i],colors [i])).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,paddings [i],0,0));

		double boxSize;
		PDFLayoutFrameElement rightBox;
		
		double halfWidth = (outerFrame.getWidth () - gap) / 2;
		switch (design) {
		case 0:
			PDFLayoutFont titleFont = new PDFLayoutFont ("Helvetica",92,PDFLayoutFontStyle.BOLD);
			outerFrame.addElement (new PDFLayoutTextElement ("LOST PET",titleFont).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (extraLeftSpace,0,0,0));
			double correction = correctionFactor * titleFont.getFontSize ();
			outerFrame.addElement (new PDFLayoutTextElement (subtitleText,new PDFLayoutFont ("Helvetica",24,PDFLayoutFontStyle.PLAIN)).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (correction + extraLeftSpace,0,0,0));
			boxSize = (outerFrame.getWidth () - gap - 2 * correction - extraLeftSpace) / 2; 

			rightBox = new PDFLayoutFrameElement (boxSize,boxSize);
			rightBox.align (PDFLayoutHorizontalAlignment.RIGHT,PDFLayoutVerticalAlignment.BELOW).pad (0,10,correction,0);
			rightBox.addElement (chipLogoBox.align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).fit (PDFLayoutObjectFit.CONTAIN).pad (0,4,0,0));
			rightBox.addElement (findBox.pad (18,0,0,0).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW));
			outerFrame.addElement (rightBox);
			
			outerFrame.addElement (imageElement.wrap (boxSize,boxSize).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.PREVIOUS_TOP).pad (correction + extraLeftSpace,10,0,0));

			PDFLayoutTableElement table = new PDFLayoutTableElement ();

			PDFLayoutTableFormat format = new PDFLayoutTableFormat ();
			format.setFont (tableFont);
			format.setPlacement (new PDFLayoutPlacement (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.CENTER));
			format.setPadding (new PDFLayoutSides<Double> (5.));
			table.setDefaultFormat (format);

			table.setRowCount (details.size ()); // enough rows, truncate later
			table.setColCount (4);

			int x = 0;
			int y = 0;

			for (PetPosterDetail detail : details) {
				if (detail.size == BIGGER) {
					y += x;
					x = 0;
				}

				table.setText (y,2 * x    ,detail.title + ':');
				table.setText (y,2 * x + 1,detail.value);

				switch (detail.size) {
				case NORMAL: 
					if (++x == 2) {
						y++;
						x = 0;
					}
					break;
				case BIGGER:
					table.setCellSpan (y,2 * x + 1,1,3);
					y++;
					break;
				default:
					throw new Error ("unknown pet poster detail size: " + detail.size);
				}
			}

			table.setRowCount (y + x);
			table.pad (correction + extraLeftSpace,20,0,0).setPlacement (new PDFLayoutPlacement (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW));
			table.setAllBorderStyles (tableBorderStyle);

			outerFrame.addElement (table);
			outerFrame.addElement (new PDFLayoutFrameElement (1,12).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW)); // spacing
			break;
		case 1:
			outerFrame.addElement (new PDFLayoutTextElement ("LOST PET",new PDFLayoutFont ("Helvetica",81,PDFLayoutFontStyle.BOLD)).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
			outerFrame.addElement (new PDFLayoutTextElement (subtitleText,new PDFLayoutFont ("Helvetica",24,PDFLayoutFontStyle.PLAIN)).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,5,0,0));
			boxSize = 0.6 * outerFrame.getWidth (); 
			outerFrame.addElement (imageElement.wrap (boxSize,boxSize).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,15,0,15));
			for (PetPosterDetail detail : details)
				outerFrame.addElement (new PDFLayoutTextElement (detailString (detail),tableFont).pad (4).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
			outerFrame.addElement (new PDFLayoutFrameElement (1,50).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW)); // spacing
			double logoWidth = 0.6 * (outerFrame.getWidth () - gap);
			findBox.setPlacement (new PDFLayoutPlacement (new PDFLayoutCoordinatePlacement (logoWidth + gap),PDFLayoutVerticalAlignment.BELOW));
			outerFrame.addElement (findBox);
			outerFrame.addElement (chipLogoBox.wrap (logoWidth,100).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.PREVIOUS_TOP).pad (0,5,0,0));
			outerFrame.addElement (new PDFLayoutFrameElement (1,27).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW)); // spacing
			break;
		case 2:
			double imageWidth = 0.55 * outerFrame.getWidth ();
			double fontSize = 11;
			rightBox = new PDFLayoutFrameElement (imageWidth,imageWidth + 4 * fontSize + 27);
			rightBox.setBorderStyle (new PDFLayoutBorderStyle (new PDFLayoutSides<PDFLayoutLineStyle> (new PDFLayoutLineStyle (1.,Color.LIGHT_GRAY)),0));
			rightBox.addElement (imageElement.pad (7).fit (PDFLayoutObjectFit.CONTAIN).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
			
			for (PetPosterDetail detail : details)
				rightBox.addElement (new PDFLayoutTextElement (detailString (detail),new PDFLayoutFont (tableFontName,fontSize,PDFLayoutFontStyle.PLAIN)).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (9,5,0,0));
			
			outerFrame.addElement (rightBox.align (PDFLayoutHorizontalAlignment.RIGHT,PDFLayoutVerticalAlignment.TOP));
			outerFrame.addElement (new PDFLayoutTextElement ("LOST",new PDFLayoutFont ("Helvetica",70,PDFLayoutFontStyle.PLAIN)).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.TOP).pad (0,10,0,0));
			outerFrame.addElement (new PDFLayoutTextElement ("PET",new PDFLayoutFont ("Helvetica",96,PDFLayoutFontStyle.BOLD)).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,3,0,0));
			outerFrame.addElement (new PDFLayoutTextElement (subtitleText,new PDFLayoutFont ("Helvetica",21,PDFLayoutFontStyle.PLAIN)).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW));
			double chipLogoWidth = outerFrame.getWidth () - imageWidth - gap;
			outerFrame.addElement (chipLogoBox.wrap (chipLogoWidth,chipLogoWidth * chipLogoRatio).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,10,0,0));
			outerFrame.addElement (findBox.align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,10,0,0));
			break;
		case 3:
			outerFrame.addElement (new PDFLayoutTextElement ("LOST CAT",new PDFLayoutFont ("Helvetica",85,PDFLayoutFontStyle.BOLD)).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
			
			PDFLayoutFrameElement leftBox = new PDFLayoutFrameElement (halfWidth - 22,300);
			leftBox.addElement (new PDFLayoutTextElement (missingSinceText + ':',timeFont).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,0,0,80));
			leftBox.addElement (new PDFLayoutTextElement (lastSeenText + ':',timeFont).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,0,0,50));
			PDFLayoutFont spacedTableFont = new PDFLayoutFont (tableFontName,16,PDFLayoutFontStyle.PLAIN);
			spacedTableFont.setLineSpacing (1.5);
			for (PetPosterDetail detail : details)
				leftBox.addElement (new PDFLayoutTextElement (detailString (detail),spacedTableFont).align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).pad (0,14,0,0));
			leftBox.addElement (chipLogoBox.align (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BELOW).fit (PDFLayoutObjectFit.CONTAIN).pad (0,60,0,0));

			rightBox = new PDFLayoutFrameElement (halfWidth,1000);
			rightBox.addElement (new PDFLayoutTextElement ('“' + detailValue (details,"name").toUpperCase () + '”',nameFont).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,0,0,5));
			rightBox.addElement (imageElement.wrap (halfWidth,halfWidth).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
			rightBox.addElement (new PDFLayoutFrameElement (1,20).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW)); // spacing
			
			String [] [] mixedStrings = {
					{"IF YOU FIND ME","please call"},
					{"1-877-PETLINK","(" + slipPhone + ")"},
			};
			
			double [] [] mixedSizes = {
					{18,16},
					{22,13}
			};
			
			Color [] [] mixedColors = {
					{Color.BLACK,Color.BLACK},
					{highlightColor,Color.BLACK}
			};
			
			PDFLayoutFontStyle [] [] mixedStyles = {
					{PDFLayoutFontStyle.BOLD,PDFLayoutFontStyle.PLAIN},
					{PDFLayoutFontStyle.BOLD,PDFLayoutFontStyle.PLAIN}
			};
			
			// TODO: Hack to get these strings centered without frames that adjust to their contents
			double [] mixedPositions = {
					20,
					-10
			};
			
			for (int i = 0;i < mixedStrings.length;i++) {
				PDFLayoutFrameElement mixedElement = new PDFLayoutFrameElement (1000,20);
				for (int j = 0;j < mixedStrings [i].length;j++)
					mixedElement.addElement (new PDFLayoutTextElement (mixedStrings [i] [j],new PDFLayoutFont (contactFontName,mixedSizes [i] [j],mixedStyles [i] [j],mixedColors [i] [j])).align (PDFLayoutHorizontalAlignment.AFTER,PDFLayoutVerticalAlignment.BOTTOM).pad (0,0,7,0));
				rightBox.addElement (mixedElement.align (new PDFLayoutCoordinatePlacement (mixedPositions [i]),PDFLayoutVerticalAlignment.BELOW).pad (0,5));
			}
			
			rightBox.addElement (new PDFLayoutTextElement ("or file a “found pet report” at",new PDFLayoutFont (contactFontName,15,PDFLayoutFontStyle.PLAIN)).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,5));
			rightBox.addElement (new PDFLayoutTextElement (URI,new PDFLayoutFont (contactFontName,15,PDFLayoutFontStyle.PLAIN,highlightColor)).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,5));
			PDFLayoutFrameElement wrapper = new PDFLayoutFrameElement (0.65 * halfWidth,200);
			PDFLayoutFont wrapFont = new PDFLayoutFont (footFont.getFontName (),12,PDFLayoutFontStyle.PLAIN);
			wrapper.addElement (new PDFLayoutTextElement (footText,wrapFont).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
			wrapFont.setLineSpacing (1.5);
			rightBox.addElement (wrapper.align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,27,0,0));
			outerFrame.addElement (leftBox.align (PDFLayoutHorizontalAlignment.LEFT,new PDFLayoutCoordinatePlacement (150)));
			outerFrame.addElement (rightBox.align (PDFLayoutHorizontalAlignment.RIGHT,new PDFLayoutCoordinatePlacement (140)));
			
			double slipTop = pageSize.y - margins.ymin - slipSize;
			outerFrame.addElement (new PDFLayoutFrameElement (pageSize.x,0).align (new PDFLayoutCoordinatePlacement (-margins.xmin),new PDFLayoutCoordinatePlacement (slipTop)).border (halfLineWidth));
			for (int i = 0;i < slipCount;i++) {
				double slipLeft = i * pageSize.x / slipCount - margins.xmin;
				PDFLayoutElement slipLine = new PDFLayoutFrameElement (0,slipSize).align (new PDFLayoutCoordinatePlacement (slipLeft),new PDFLayoutCoordinatePlacement (slipTop));
				if (i != 0)
					slipLine.border (halfLineWidth);
				outerFrame.addElement (slipLine);
				// TODO: The positioning is hacked; positioning isn't interacting properly with rotation
				outerFrame.addElement (new PDFLayoutTextElement (slipAddress,slipFont).rotate (1).align (new PDFLayoutCoordinatePlacement (slipLeft + 100),PDFLayoutVerticalAlignment.PREVIOUS_BOTTOM).pad (0,0,0,10));
				outerFrame.addElement (new PDFLayoutTextElement (slipPhone,slipFont).rotate (1).align (new PDFLayoutCoordinatePlacement (slipLeft + 100),PDFLayoutVerticalAlignment.PREVIOUS_BOTTOM).pad (0,0,0,10));
			}
			break;
		default:
			throw new Error ("unknown pet poster design " + design);
		}
		
		if (design != 3) {
			outerFrame.addElement (new PDFLayoutFrameElement (1,0).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).fit (PDFLayoutObjectFit.CONTAIN).border (halfLineWidth));
			outerFrame.addElement (new PDFLayoutTextElement (footText,footFont).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW).pad (0,10,0,0));
		}
		
		page.setOuterFrame (outerFrame);
		pages.add (page);
		new PDFLayoutRenderer ().render (pages,target);
	}
	
	private String detailString (PetPosterDetail detail) {
		return detail.title + ":  " + detail.value;
	}
	
	private String detailValue (List<PetPosterDetail> details,String id) {
		for (PetPosterDetail detail : details)
			if (detail.id.equals (id))
				return detail.value;
		return null;
	}
}
