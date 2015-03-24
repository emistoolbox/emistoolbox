package com.emistoolbox.client.admin.ui;

public class OpenLayersUtil
{
    public static void showGeojsonMap(String jsonFeatures) 
    {
        loadGeoJson("/emistoolbox/content?chart=" + jsonFeatures, null); 
    }; 
    
    public static native void loadGeoJson(String jsUrl, String divId) /*-{
        var script = $doc.createElement('script');
        script.type = 'text/javascript';

        script.onload = function() {
            alert("done"); 
            alert($wnd.features); 
            alert($wnd.bounds); 
        }; 
        
        script.src = jsUrl;
        $doc.body.appendChild(script);
    }-*/; 
}
