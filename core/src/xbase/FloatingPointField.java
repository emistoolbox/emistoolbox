package xbase;

public class FloatingPointField extends AbstractNumberField {
	public FloatingPointField (FieldDescriptor fieldDescriptor) {
		super (fieldDescriptor);
	}

	// This used to check for presence of an 'e' for the exponent, but
	// MLI_adm1.dbf contains a floating point field without exponent. Now a
	// floating point field works just like a number field.
	public boolean validate () {
		return super.validate ();
	}
}
