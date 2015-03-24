function ToolboxOpenLayers(divId, renderPoints)
{
    this.debug = true; 

    this.renderPoints = renderPoints; 
    this.map = new OpenLayers.Map(divId, { allOverlays: true }); 

    this.divId = divId; 
    this.vectors = []; 
    this.features = []; 
    this.layerFilenames = []; 
    this.layerNames = []; 
    this.rules = []; 
    
    this.userStyles = [];

    this.geoJsonParser = new OpenLayers.Format.GeoJSON();
    
    this.setBackgroundImage = ToolboxOpenLayers_setBackgroundImage; 
    this.setBackgroundGoogleMaps = ToolboxOpenLayers_setBackgroundGoogleMaps; 
    this.setColourThresholds = ToolboxOpenLayers_setColourThresholds; 
    this.addShapefileLayer = ToolboxOpenLayers_addShapefileLayer;
    this.loadSingleLayer = ToolboxOpenLayers_loadSingleLayer; 
    this.loadAllLayers = ToolboxOpenLayers_loadAllLayers; 
    this.zoom = ToolboxOpenLayers_zoom; 
    this.zoomToBest = ToolboxOpenLayers_zoomToBest; 
    
    this.getStyle = ToolboxOpenLayers_getStyle; 
    this.setStyle = ToolboxOpenLayers_setStyle; 
    
    this.adjustLabelLevel = ToolboxOpenLayers_adjustLabelLevel
}

function ToolboxOpenLayers_setBackgroundImage(url, bounds, size)
{
    this.baseLayer = new OpenLayers.Layer.Image('background', url, new OpenLayers.Bounds(bounds[0], bounds[1], bounds[2], bounds[3]), new OpenLayers.Size(size[0], size[1]), {rendererOptions: { zIndexing: true}});
    this.map.addLayer(this.baseLayer);
    this.baseLayer.setZIndex(0); 
}

function ToolboxOpenLayers_zoom(bounds)
{ this.map.zoomToExtent(new OpenLayers.Bounds(bounds[0], bounds[1], bounds[2], bounds[3])); }

function ToolboxOpenLayers_zoomToBest()
{ this.map.zoomToMaxExtent(); }


function ToolboxOpenLayers_setBackgroundGoogleMaps()
{}

function ToolboxOpenLayers_addShapefileLayer(filename, layerName)
{
    var result = this.layerFilenames.length; 
    this.layerFilenames[this.layerFilenames.length] = filename; 
    this.layerNames[this.layerNames.length] = layerName; 
    
    return result; 
}

function ToolboxOpenLayers_loadAllLayers()
{
    this.loadSingleLayer(0); 
}

function ToolboxOpenLayers_loadSingleLayer(index)
{
    if (index >= this.layerFilenames.length)
        return; 

    var self = this; 
   
    this.vectors[index] = new OpenLayers.Layer.Vector(this.layerNames[index], { styleMap: this.getStyle(index), renderOptions: { zIndexing: true }}); // styleMap :
    new Shapefile({ 
        shp: this.layerFilenames[index], 
        dbf: this.layerFilenames[index].replace(".shp", ".dbf")
    }, function (data) {
        self.vectors[index].addFeatures(self.geoJsonParser.read(data.geojson));
        self.map.zoomToExtent(self.vectors[index].getDataExtent());
        self.loadSingleLayer(index + 1); 
    }); 
    
    this.map.addLayer(this.vectors[index]);
    this.vectors[index].setZIndex(100 + index * 10);
}

function ToolboxOpenLayers_setStyle(index, style)
{
    this.userStyles[index] = style; 
}

function ToolboxOpenLayers_adjustLabelLevel(labelLevel)
{
    // LabelLevel should most detailed polygon layer. This normally shows too much text, so we want to move it up 
    // to the second most detailed layer. 

    var dataLayerCount = 0;
    var index = 0; 
    while (index < labelLevel)
    {
        if (!this.userStyles[index])
            dataLayerCount++; 

        if (dataLayerCount == 2)
            return index; 

        index++; 
    }
    
    return labelLevel; 
}

function ToolboxOpenLayers_getStyle(index)
{
    if (this.userStyles[index])
        return new OpenLayers.Style(this.userStyles[index]); 

    var labelLevel = this.layerFilenames.length - (this.renderPoints ? 2 : 1);
    labelLevel = this.adjustLabelLevel(labelLevel); 
    
    var style = null; 
    if (index == this.layerFilenames.length - 1)
        style = { fillOpacity: 0.25, strokeWidth: 1, strokeColor: "black" }; 
    else
    {
        // Black outlines for border ranges. 
        //
        var width = 2 * (this.layerFilenames.length - 1 - index) + 1; 
        style = { fillOpacity: 0, strokeColor: "black", strokeWidth: width }; 
    }        

    if (index == labelLevel)
    {
        if (index == this.layerFilenames.length - 1)
            style.label = "${Title} (${Value})"; 
        else
            style.label = "${Title}"; 
    }
    
    var result = new OpenLayers.Style(style); 
    if (index == this.layerFilenames.length - 1)
        result.addRules(this.rules); 
    
    return result; 
}

function ToolboxOpenLayers_setColourThresholds(colours, thresholds, asPoints)
{
    this.rules = []; 
    
    for (var i = 0; i < thresholds.length; i++) 
    {
        var rule = new OpenLayers.Rule({
            filter: new OpenLayers.Filter.Comparison({
                evaluate: getFilter(thresholds[i], i > 0 ? thresholds[i - 1] : 0)
            }),
            symbolizer: { fillColor: getColour(colours[i]) }
        });
        
        this.rules.push(rule); 
    }
    
    var elseRule = new OpenLayers.Rule({
        elseFilter: true,
        symbolizer: { fillColor: getColour(colours[0]) }
    });
    
    this.rules.push(elseRule); 
}

function getFilter(upperThreshold, lowerThreshold)
{
    return function(c) { 
        return (parseFloat(c.Value) >= lowerThreshold) && (parseFloat(c.Value) < upperThreshold); 
    }
}

function getColour(value)
{
    return "#" + value; 
}

function hex(value)
{
    var tmp = "00" + value.toString(16);     
    return tmp.substr(-2); 
}
