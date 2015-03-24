package com.emistoolbox.server.results;

import java.util.Map;

public interface ResultCollectorCallback
{
    public void setResult(int id, Map<String, String> entityValues, double[] values); 
}
