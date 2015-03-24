package xbase;

public class DateField extends CharacterField {
	protected DateField (FieldDescriptor fieldDescriptor) {
		super (fieldDescriptor);
	}

	protected boolean validate () {
		try {
			int day = getDay ();
			int month = getMonth ();
			getYear ();
			return chars.length == 8 && 1 <= month && month <= 12 && 1 <= day && day <= 31;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
	
	public int getYear () {
		return parse (0,4);
	}

	public int getMonth () {
		return parse (4,2);
	}

	public int getDay () {
		return parse (6,2);
	}

	private int parse (int offset,int length) {
		return Integer.parseInt (new String (chars,offset,length));
	}
}
