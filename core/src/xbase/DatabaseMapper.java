package xbase;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class DatabaseMapper {
	public static void main (String [] args) throws IOException {
		String inputFile    = args [0];
		String outputFile   = args [1];
		String propertyFile = args [2];
		String fieldName    = args [3];
		
		Properties properties = new Properties ();
		BufferedInputStream propertyStream = new BufferedInputStream (new FileInputStream (propertyFile));
		properties.load (propertyStream);
		propertyStream.close ();
		
		DatabaseFile databaseFile = new DatabaseFile ();
		databaseFile.read (inputFile);
		
		int fieldIndex = databaseFile.getFieldIndex (fieldName);
		
		for (XBaseRecord record : databaseFile.records) {
			XBaseField field = record.fields [fieldIndex];			
			if (!(field instanceof NumberField))
				throw new Error ("can only map number fields");
			NumberField numberField = (NumberField) field;
			String newValue = properties.getProperty (Integer.toString ((int) numberField.doubleValue ()));
			if (newValue != null) {
				int length = numberField.chars.length;
				numberField.chars = String.format (Locale.US,"%" + length + "." + numberField.decimalCount + "f",Double.parseDouble (newValue)).getBytes ();
				if (numberField.chars.length != length)
					throw new Error ("numerical value " + newValue + " doesn't fit into column " + fieldName);
			}
		}
		
		databaseFile.write (outputFile);
	}
}
