package com.emistoolbox.server;

public class EmisConfig
{
	public static final String EMISTOOLBOX_PATH = "emistoolbox.path"; 
	public static final String EMISTOOLBOX_PATH_WRITABLE = "emistoolbox.path.writable";
	
    public static String get(String name, String defaultValue)
    {
        String result = System.getProperty(name);
        if (result == null)
            return defaultValue;

        return result;
    }
}
