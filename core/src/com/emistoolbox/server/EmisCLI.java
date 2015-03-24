package com.emistoolbox.server;

import java.io.IOException;

public class EmisCLI
{
    /** EMIS Toolbox command line interface. 
     * 
     *  import <dataset>
     *  import <dataset> <global query>
     */ 
    public static void main(String[] args)
        throws IOException
    {
        System.out.println("Usage:"); 
        System.out.println("    EmisCLI import <dataset> [<global query>]"); 
        System.out.println(""); 
        
        if (args.length != 2)
            return; 
        
        if (!args[0].equals("import"))
            return; 
        
        String globalQuery = null; 
        if (args.length >= 3)
            globalQuery = args[2]; 
        
        EmisToolboxServiceImpl.runImport(args[1], globalQuery); 
    }
}
