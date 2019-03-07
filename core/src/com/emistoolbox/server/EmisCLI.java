package com.emistoolbox.server;

import java.io.IOException;

import com.emistoolbox.common.model.meta.EmisMeta;

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
        System.out.println("    EmisCLI load model|mapping|report <dataset>"); 
        System.out.println(""); 
                
        if ("import".equals(args[0]))
        	doImport(args);
        else if ("load".equals(args[0]))
        	doLoad(args); 
    }
    
    private static void doImport(String[] args)
    	throws IOException
    {
        String globalQuery = null; 
        if (args.length >= 3)
            globalQuery = args[2]; 
        
        EmisToolboxServiceImpl.runImport(args[1], globalQuery); 
    }
    
    private static final int LOAD_MODEL = 1; 
    private static final int LOAD_MAPPING= 2; 
    private static final int LOAD_REPORT = 3; 
    
    private static void doLoad(String[] args)
    	throws IOException
    {
    	int load = LOAD_REPORT; 
    	
    	String dataset = args.length > 2 ? args[2] : null;   
    	if ("mapping".equals(args[1]))
    		load = LOAD_MAPPING; 
    	else if ("report".equals(args[1]))
    		;
    	else if ("model".equals(args[1]))
    		load = LOAD_MODEL; 
    	else  
    		dataset = args[1]; 
    	
    	if (dataset == null)
    		throw new IllegalArgumentException("Missing dataset"); 
    	
    	EmisMeta meta = EmisToolboxIO.loadModelXml(dataset);
    	System.out.println("Model loaded"); 
    	if (load == LOAD_MODEL)
    		return; 
    	
    	EmisToolboxIO.loadMappingXml(dataset, meta); 
    	System.out.println("Mapping loaded"); 
    	if (load == LOAD_MAPPING)
    		return; 
    	
    	EmisToolboxIO.loadReportXml(dataset, meta); 
    	System.out.println("Reports loaded"); 
    }
}
