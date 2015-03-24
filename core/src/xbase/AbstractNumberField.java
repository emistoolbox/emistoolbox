package xbase;

abstract class AbstractNumberField extends CharacterField {
	int decimalCount;
	
	public AbstractNumberField (FieldDescriptor fieldDescriptor) {
		super (fieldDescriptor);
		this.decimalCount = fieldDescriptor.decimalCount;
	}
	
	public double doubleValue () {
		return Double.parseDouble (toString ().trim ());
	}
	
	protected boolean validate () {
		try {
			doubleValue ();
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
}
