package com.emistoolbox.common.fileDialog; 

public class FileStringUtils 
{
    public static final String PARENT_DIRECTORY = "..";
    
    public static String buildDirectoryString(final String defDir,final String current, final String selected)
    {
        String path = current.replace("\\", "/");
        if (path.endsWith("/")) 
            path = path.substring(0, path.length() - 1);
        
        StringBuilder dirRet = new StringBuilder();
        if (selected != null && !selected.equals(""))
        {
          if (selected.equals(PARENT_DIRECTORY))
          {
            if (path.contains("/")) path = path.substring(0, path.lastIndexOf("/"));
            dirRet.append(path);
          }
          else
              dirRet.append(path).append("/").append(selected); 

        } else
            dirRet.append(path);
        
        
        String ret = (dirRet.length() < defDir.length()) ? defDir : dirRet.toString();
        ret = ret.replace("//", "/");
        if (ret.endsWith("/")) 
            ret = ret.substring(0,ret.length()-1);
        return ret;
        //return ret.replace("/", File.separator);
    }
}
