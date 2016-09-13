package info.joriki.truetype;

public interface ComplexGlyphSpeaker {
	short ARGS_ARE_WORDS            = 0x0001;
	short ARGS_ARE_XY_VALUES        = 0x0002;
	short ROUND_XY_TO_GRID          = 0x0004;
	short HAS_SCALE                 = 0x0008;
	short RESERVED                  = 0x0010;
	short MORE_COMPONENTS           = 0x0020;
	short HAS_X_AND_Y_SCALE         = 0x0040;
	short HAS_TWO_BY_TWO            = 0x0080;
	short HAS_INSTRUCTIONS          = 0x0100;
	short USE_MY_METRICS            = 0x0200;
	short OVERLAP_COMPOUND          = 0x0400;
	short SCALED_COMPONENT_OFFSET   = 0x0800;
	short UNSCALED_COMPONENT_OFFSET = 0x1000;
}
