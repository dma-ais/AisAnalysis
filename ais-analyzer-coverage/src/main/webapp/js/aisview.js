// Global variables
var map;
var vessels = [];
var clusters = [];
var clustersDrawn = 0;
var topLeftPixel;
var botRightPixel;
var filterQuery = {};
var showVesselName = false;
var savedTracks;
var savedTimeStamps;
var loadSavedFeatures = false;
var timeOfLastLoad = 0;
var selectedVessel;
var selectedVesselInView = false;
var selectedFeature;
var vesselsResults = [];
var lastRequestId = 0;
var lastZoomLevel;
var polygonLayer;
var sourceLayer;
var selectedSource;


/**
 * Sets up the map by adding layers and overwriting 
 * the 'map' element in the HTML index file.
 * Vessels are loaded using JSON and drawn on the map.
 */



function setupMap(){

	includePanels();
	
	// Load cookies
	loadView();

	// Create the map and overwrite cotent of the map element
	map = new OpenLayers.Map({
        div: "map",
        projection: "EPSG:900913",
        fractionalZoom: true
    });

	// Add OpenStreetMap Layer
	var osm = new OpenLayers.Layer.OSM(
		"OSM",
		"http://a.tile.openstreetmap.org/${z}/${x}/${y}.png",
		{
			'layers':'basic',
			'isBaseLayer': true
		} 
	);
	map.addLayer(osm);

	var center = transformPosition(initialLon, initialLat);
	map.setCenter (center, initialZoom);
	lastZoomLevel = map.zoom;

	setupUI();

	polygonLayer = new OpenLayers.Layer.Vector("Polygon Layer");
	polygonLayer.styleMap.styles['default'].defaultStyle={fillColor: 'blue'}
	map.addLayers([polygonLayer]);
	

    var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
    renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
//    
	sourceLayer = new OpenLayers.Layer.Vector("Sources", {
		styleMap: new OpenLayers.StyleMap({
			"default": {
//                    pointRadius: "10", // sized according to type attribute
//                    fillColor: "#980000 ",
//                    strokeColor: "black",
//                    strokeWidth: 2,
//                    graphicZIndex: 1,
                    cursor: "crosshair"
			},
			"select": {
				fillColor: "#ffff66"
//				externalGraphic: "${image}"
			}
		}),
        renderers: renderer
    });
	
	
	map.addLayer(sourceLayer);
    
	hoverControl = new OpenLayers.Control.SelectFeature(sourceLayer, 
			{	
				hover: false,
	                highlightOnly: false,
	                eventListeners: {
	                   	featurehighlighted: onFeatureSelect,
	                   	featureunhighlighted: onFeatureUnselect
	                }
	
			}
		);
	map.addControl(hoverControl);
	hoverControl.activate();

	getSources();

	map.events.register('zoomend', this, function (event) {
        drawCoverage();
    });
	
	map.events.register('moveend', this, function (event) {
        drawCoverage();
    });
	
	
}


function onPopupClose(evt) {
    // 'this' is the popup.
//    selectControl.unselect(this.feature);
//	alert();
}
function onFeatureSelect(evt) {
	
    feature = evt.feature;
//    feature.style["strokeColor"] = "blue";
    
    selectedSource = sources[feature.mmsi];
    
//    sourceLayer.redraw();

    var checked="";
    if(sources[feature.mmsi].enabled){
    	checked='checked="checked"';
    }

    $("#sourceDetailsContainer").html('<div class="smallText">Id</div>'+
            '<div class="information">'+feature.mmsi+'</div>'+
            '<div class="smallText">Type</div>'+
            '<div class="information">'+feature.type+'</div>'+
            '<div class="smallText">Lat</div>'+
            '<div class="information">'+feature.lat.toFixed(4)+'</div>'+
            '<div class="smallText">Lon</div>'+
            '<div class="information">'+feature.lon.toFixed(4)+'</div>'+
            '<div class="smallText">Enabled</div>'+
            '<div class="information"><input type="checkbox" class="sourceCheckbox" id="'+feature.mmsi+'" '+checked+' name="" value=""/></div>');
    slideSourceDetailsDown();
    
    drawSources();
    
}
function onFeatureUnselect(evt) {
//	evt.feature.style["strokeColor"] = "black";
	selectedSource = "";
	slideSourceDetailsUp();
	drawSources();
//	sourceLayer.redraw();
}


/**
 * Loads vessels if time since last update is higher than loadFrequence. 
 */
function loadVesselsIfTime(){

	var timeSinceLastLoad = new Date().getTime() - timeOfLastLoad;
	
	if (timeOfLastLoad == 0 || timeSinceLastLoad >= loadFrequence){
		loadVessels();
	}	
	
}


function setTimeToLoad(ms){

	var timeSinceLastLoad = new Date().getTime() - timeOfLastLoad;

	timeOfLastLoad -= (loadFrequence - timeSinceLastLoad);
	timeOfLastLoad += ms;

}


/**
 * Loads the vessels using JSON.
 * If the zoom level is higher than or equal to the 
 * minimum zoom level it adds each vessel as a vessel 
 * instance to the list of vessels.
 * The vessels will be drawn when the JSON is received.
 * If the zoom level is lower than the minumum zoom level,
 * it draws the vesselclusters instead.
 */
function loadVessels(){

	// Reset list of vessels
	vessels = [];
	clusters = [];

	if (map.zoom >= vesselZoomLevel){

		// Show Loading panel
		$("#loadingPanel").css('visibility', 'visible');

		loadVesselList();

		clusterLayer.setVisibility(false);
		clusterTextLayer.setVisibility(false);
		indieVesselLayer.setVisibility(false);

		vesselLayer.setVisibility(true);
		selectionLayer.setVisibility(true);
		timeStampsLayer.setVisibility(true);
		tracksLayer.setVisibility(true);
		
	} else {

		if (showClustering){

			// Show Loading panel
			$("#loadingPanel").css('visibility', 'visible');
			
			loadVesselClusters();

			clusterLayer.setVisibility(true);
			clusterTextLayer.setVisibility(true);
			indieVesselLayer.setVisibility(true);

		}

		vesselLayer.setVisibility(false);
		selectionLayer.setVisibility(false);
		timeStampsLayer.setVisibility(false);
		tracksLayer.setVisibility(false);

	}

	// Set time of load
	timeOfLastLoad = new Date().getTime();

}

/**
 * Loads and draws all vessels in the view.
 */
function loadVesselList(){

	saveViewPort();

	// Generate data
	var data = filterQuery;
	lastRequestId++;
	data.requestId = lastRequestId;
	if (!loadViewportOnly){
		delete data.topLon; 
		delete data.topLat; 
		delete data.botLon; 
		delete data.botLat;
	}

	$.getJSON(listUrl, data, 
		function (result) {

			if (result.requestId != lastRequestId) return;
			
			// Update vessel counter
			$("#vesselsTotal").html(result.vesselsInWorld);

			// Load new vessels
			var JSONVessels = result.vesselList.vessels;
		
			for (vesselId in JSONVessels) {
				// Create vessel based on JSON data
				var vesselJSON = JSONVessels[vesselId];
				var vessel = new Vessel(vesselId, vesselJSON, 1);
			
				if (vessels[vesselId]) {
					// Vessel exists just update data
					vessels[vesselId] = vessel;
				} else {
					vessels.push(vessel);
				}
			
			}
		
			// Draw vessels
			drawVessels();

			// Hide Loading panel
			$("#loadingPanel").css('visibility', 'hidden');
		}
	);
}

/**
 * Loads and draws the vessel clusters.
 */
function loadVesselClusters(){

	saveViewPort();

	// Find cluster size
	var size = 10;
	for (i in clusterSizes){
		if (map.zoom >= clusterSizes[i].zoom){
			size = clusterSizes[i].size;
			//break;
		}
	}

	// Generate data
	var data = filterQuery;
	data.clusterLimit = clusterLimit;
	data.clusterSize = size;
	lastRequestId++;
	data.requestId = lastRequestId;
	if (!loadViewportOnly){
		delete data.topLon; 
		delete data.topLat; 
		delete data.botLon; 
		delete data.botLat;
	}
	
	$.getJSON(clusterUrl, data, 
		function (result) {

			if (result.requestId != lastRequestId) return;
			
			// Update vessel counter
			$("#vesselsTotal").html(result.vesselsInWorld);

			// Load vessel clusters
			var JSONClusters = result.clusters;

			for (clusterId in JSONClusters) {
			
				// Create vessel based on JSON data
				var JSONCluster = JSONClusters[clusterId];
				var from = transformPosition(JSONCluster.from.longitude, JSONCluster.from.latitude);
				var to = transformPosition(JSONCluster.to.longitude, JSONCluster.to.latitude);
				var count = JSONCluster.count;
				var density = JSONCluster.density;
				var locations = JSONCluster.locations;
				
				var cluster = new Cluster(from, to, count, density, locations);
				clusters.push(cluster);
				
			}
		
			// Draw clusters
			drawClusters();

			// Hide Loading panel
			$("#loadingPanel").css('visibility', 'hidden');

		}
	);
	
}

/**
 * Draws the vessel clusters.
 */
function drawClusters(){
	
	clusterTextLayer.removeAllFeatures();
	indieVesselLayer.removeAllFeatures();
	clusterLayer.removeAllFeatures();

	var vesselsInView = 0;
	
	for(id in clusters){
		if (clusters[id].count > clusterLimit){

			// Create polygon
			var points = [
				new OpenLayers.Geometry.Point(clusters[id].from.lon, clusters[id].from.lat),
				new OpenLayers.Geometry.Point(clusters[id].to.lon, clusters[id].from.lat),
				new OpenLayers.Geometry.Point(clusters[id].to.lon, clusters[id].to.lat),
				new OpenLayers.Geometry.Point(clusters[id].from.lon, clusters[id].to.lat)
			];
			var ring = new OpenLayers.Geometry.LinearRing(points);
			var polygon = new OpenLayers.Geometry.Polygon([ring]);

			// Create feature
			var feature = new OpenLayers.Feature.Vector(polygon,
			 	{	
					from: clusters[id].from,
					to: clusters[id].to,
					fill: findClusterColor(clusters[id])
				}
			);
			clusterLayer.addFeatures([feature]);

			// Draw text
			var textLon = clusters[id].from.lon + (clusters[id].to.lon - clusters[id].from.lon) / 2;
			var textLat = clusters[id].from.lat + (clusters[id].to.lat - clusters[id].from.lat) / 2;
			var textPos = new OpenLayers.Geometry.Point(textLon, textLat);
			var textFeature = new OpenLayers.Feature.Vector(textPos,
				{
					count: clusters[id].count,
					fontSize: clusterFontSize
				}
			);
			clusterTextLayer.addFeatures([textFeature]);

			vesselsInView += clusters[id].count;
			
		} else {

			// Draw indie vessels
			if (showIndividualVessels){
				for(locId in clusters[id].locations){
				
					var lat = clusters[id].locations[locId].latitude;
					var lon = clusters[id].locations[locId].longitude;

					var loc = transformPosition(lon, lat);
				
					feature = new OpenLayers.Feature.Vector(
				            new OpenLayers.Geometry.Point(loc.lon, loc.lat)
				        );

					indieVesselLayer.addFeatures([feature]);

					vesselsInView ++;
				}
			}
			
		}
    }

    // Update number of vessels
	$("#vesselsView").html(""+vesselsInView);

    clustersDrawn = clusters.length;
    
}

/**
 * Draws all known vessels using vector points styled to show images.
 * Vessels are drawn based on their color, angle and whether they are
 * moored on not.
 */
function drawVessels(){

	var vesselFeatures = [];
	var selectionFeatures = [];
	selectedVesselInView = false;

	// Draw all vessels or search results
	vesselsToDraw = vessels;
	if (map.zoom < vesselZoomLevel){
		vesselsToDraw = vesselsResults;
	}

	// Update number of vessels
	$("#vesselsView").html(""+vesselsToDraw.length);
	
	// Iterate through vessels where value refers to each vessel.
	$.each(vesselsToDraw, function(key, value) { 
		
		// Use styled vector points
		var feature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point( value.lon , value.lat ).transform(
				new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
				map.getProjectionObject() // to Spherical Mercator Projection
			),
		 	{	
				id: value.id,
				angle: value.degree - 90, 
				opacity:1, 
				image:"img/" + value.image,
				imageWidth: value.imageWidth,
				imageHeight: value.imageHeight,
				imageYOffset: value.imageYOffset,
				imageXOffset: value.imageXOffset,
				isVessel: true,
				vessel: value
			}
		);
		
		vesselFeatures.push(feature);

		// Set vessel in focus if searched
		if (selectedVessel && value.id == selectedVessel.id){

			selectedVesselInView = true;

			// Update selected vessel
			selectedVessel = value;
			selectedFeature = feature;

			// Update vessel details
			updateVesselDetails(feature);

		}
	});

	var selectionFeature;

	// Set search result in focus
	if (selectedFeature && selectedVesselInView){
		selectionFeature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point( selectedFeature.geometry.x , selectedFeature.geometry.y ),
		 	{	
				id: -1,
				angle: selectedFeature.attributes.angle - 90, 
				opacity:1, 
				image:"img/selection.png",
				imageWidth: 32,
				imageHeight: 32,
				imageYOffset: -16,
				imageXOffset: -16,
				isVessel: false
			}
		);
		
		selectionFeatures.push(selectionFeature);

	}

	// Redraw vessels and selection
	vesselLayer.removeAllFeatures();
	selectionLayer.removeAllFeatures();
	vesselLayer.addFeatures(vesselFeatures);
	selectionLayer.addFeatures(selectionFeatures);
	vesselLayer.redraw();
	selectionLayer.redraw();
	drawPastTrack(null);

}

/**
 * Redraws all features in vessel layer and selection layer.
 * Features are vessels.
 */
function redrawSelection(){
	var selectionFeature;
	var selectionFeatures = [];
	drawPastTrack(null);

	// Set search result in focus
	if (selectedFeature){
		selectionFeature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point( selectedFeature.geometry.x , selectedFeature.geometry.y ),
		 	{	
				id: -1,
				angle: selectedFeature.attributes.angle - 90, 
				opacity:1, 
				image:"img/selection.png",
				imageWidth: 32,
				imageHeight: 32,
				imageYOffset: -16,
				imageXOffset: -16,
				isVessel: false
			}
		);
		
		selectionFeatures.push(selectionFeature);
		selectedVesselInView = true;
		updateVesselDetails(selectedFeature);

	}

	selectionLayer.removeAllFeatures();
	selectionLayer.addFeatures(selectionFeatures);
	selectionLayer.redraw();
}

/**
 * Draws the past track.
 * If tracks are null, it will simply remove all tracks and draw nothing.
 *
 * @param tracks 
 *		Array of tracks
 */
function drawPastTrack(tracks) {

	// Remove old tracks
	tracksLayer.removeAllFeatures();
	timeStampsLayer.removeAllFeatures();

	// Draw tracks
	if (selectedVesselInView && tracks){
		var lastLon;
		var lastLat;
		var firstPoint = true;
		var untilTimeStamp = 0;
		var tracksBetweenTimeStamps = 80;

		for(track in tracks) {
			var currentTrack = tracks[track];
			if (!firstPoint){
				// Insert line
				var points = new Array(
					new OpenLayers.Geometry.Point(lastLon, lastLat).transform(
							new OpenLayers.Projection("EPSG:4326"), 
							map.getProjectionObject()),
					new OpenLayers.Geometry.Point(currentTrack.lon, currentTrack.lat).transform(
							new OpenLayers.Projection("EPSG:4326"), 
							map.getProjectionObject())
				);
			
				var line = new OpenLayers.Geometry.LineString(points);
				var lineFeature = new OpenLayers.Feature.Vector(line);
				tracksLayer.addFeatures([lineFeature]);

				// Insert timeStamp?
				if (untilTimeStamp == 0 && parseInt(track) + 10 < tracks.length){

					var timeStampPos = points[0];
					var timeStampFeature = new OpenLayers.Feature.Vector(timeStampPos);
					
					// Remove date from time
					var time = 	currentTrack.time.split(" ")[3] + " " +
								currentTrack.time.split(" ")[4];
					
					// Change to 24h clock
					time = to24hClock(time);
					
					timeStampFeature.attributes = {timeStamp: time};
					timeStampsLayer.addFeatures([timeStampFeature]);

					untilTimeStamp = tracksBetweenTimeStamps;

				} else {
					untilTimeStamp --;
				}
			}
			lastLon = currentTrack.lon;
			lastLat = currentTrack.lat;	
			firstPoint = false;
		}
	
		// Draw features
		tracksLayer.refresh();
		timeStampsLayer.refresh();
	}
}

/**
 * Finds the color of a cluster based on either density or count.
 */
function findClusterColor(cluster){

	if (baseColorsOn == "density"){ 
	
		for (var i = clusterColors.length - 1; i >= 0; i--){
			if (cluster.density >= clusterColors[i].densityLimit){
				return clusterColors[i].color;
			}
		}
		
	} else if (baseColorsOn == "count"){ 

		for (var i = clusterColors.length - 1; i >= 0; i--){
			if (cluster.count >= clusterColors[i].countLimit){
				return clusterColors[i].color;
			}
		}
		
	}
	
	return "#000000";
}

/**
 *	Saves the viewport to the filter query object.
 */
function saveViewPort(){

	// Get points from viewport
	var viewportWidth = $(map.getViewport()).width();
	var viewportHeight = $(map.getViewport()).height();
	topLeftPixel = new OpenLayers.Pixel(viewportWidth*0.00, viewportHeight*0.00);
	botRightPixel = new OpenLayers.Pixel(viewportWidth*1.00, viewportHeight*1.00);

	var top = map.getLonLatFromPixel(topLeftPixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);
	var bot = map.getLonLatFromPixel(botRightPixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);

	filterQuery.topLon = top.lon; 
	filterQuery.topLat = top.lat; 
	filterQuery.botLon = bot.lon; 
	filterQuery.botLat = bot.lat;
	
}

/**
 * Converts a string in 12hr format to 24h format.
 * 		
 * @param time
 * 		a string in the following format: "12:36:26 PM"
 */
function to24hClock(time){
	
	// Parse data
	var hour = parseInt(time.split(":")[0]);
	var min = parseInt(time.split(":")[1]);
	var sec = parseInt(time.split(":")[2]);
	var ampm = time.split(" ")[1];
	
	// AM?
	if (ampm == "PM"){
		hour += 12;
		if (hour == 24){
			hour = 12;
		}
	} else if(hour == 12){
		hour = 0;
	}
	
	// Insert zeroes
	hour = hour += "";
	min = min += "";
	sec = sec += "";
	if (hour.length == 1){
		hour = "0" + hour;
	}
	if (min.length == 1){
		min = "0" + min;
	}
	if (sec.length == 1){
		sec = "0" + sec;
	}
	
	return hour + ":" + min + ":" + sec;
	
}

/**
 * Searches for the vessel described in the search field.
 */
function search(){
	// Read search field
	var arg = $("#searchField").val();
	
//	if (arg.length > 0){
//
//		// Show loader
//		$("#searchLoad").css('visibility', 'visible');
//
//		// Load search results
//		$.getJSON(searchUrl, { argument: arg }, function (result) {
//				var s = "s";
//				
//				// Show search results
//				$("#searchResults").css('visibility', 'visible');
//					
//				// Search results
//				vesselsResults = [];
//
//				// Get vessels
//				for (vesselId in result.vessels) {
//					var vesselJSON = result.vessels[vesselId];
//					var vessel = new Vessel(vesselId, vesselJSON, 1);
//					vesselsResults.push(vessel);
//				}
//
//				// Focus on vessel
//				if (vesselsResults.length == 1){
//					s = "";
//					selectedVessel = vesselsResults[0];
//					var center = new OpenLayers.LonLat(selectedVessel.lon, selectedVessel.lat).transform(
//						new OpenLayers.Projection("EPSG:4326"), 
//						map.getProjectionObject()
//						);
//					map.setCenter (center, focusZoom);
//				}
//
//				$("#searchMatch").html(result.vesselCount + " vessel" + s + " match.");
//
//				// Hide loader
//				$("#searchLoad").css('visibility', 'hidden');
//
//			});
//	} else {
//		vesselsResults = [];
//		drawVessels();
//
//		// Hide results
//		$("#searchMatch").html('');
//		$("#searchResults").css('visibility', 'hidden');
//	}

}


/**
 * Transforms a position to a position that can be used 
 * by OpenLayers. The transformation uses 
 * OpenLayers.Projection("EPSG:4326").
 * 
 * @param lon
 *            The longitude of the position to transform
 * @param lat
 *            The latitude of the position to transform
 * @returns The transformed position as a OpenLayers.LonLat 
 * instance.
 */
function transformPosition(lon, lat){
	return new OpenLayers.LonLat( lon , lat )
		.transform(
			new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
			map.getProjectionObject() // to Spherical Mercator Projection
		);
}


/**
 * Resets the filterQuery object and adds a filter preset.
 * 
 * @param presetSelect
	 	A string in the following form:
 * 		"key = value"
 *
 */
function useFilterPreset(presetSelect) {
	
	resetFilterQuery();

	filter = presetSelect.options[presetSelect.selectedIndex].value;
	if (filter != ""){
		var expr = 'filterQuery.' + filter.split("=")[0] + ' = "' + filter.split("=")[1] + '"';
		eval(expr);
	}
	
	parseFilterQuery();			
	filterChanged();
}


/**
 * Resets the filterQuery.
 */
function resetFilterQuery(){
	delete filterQuery.country;
	delete filterQuery.sourceCountry;
	delete filterQuery.sourceType;
	delete filterQuery.sourceRegion;
	delete filterQuery.sourceBS;
	delete filterQuery.sourceSystem;
	delete filterQuery.vesselClass;
}


/**
 * Clears the values in the filter panel.
 */
function clearFilters() {
	$("#country").val("");
	$("#soruceCountry").val("");
	$("#sourceType").val("");
	$("#sourceRegion").val("");
	$("#sourceBS").val("");
	$("#sourceSystem").val("");
	$("#vesselClass").val("");
}


/**
 * Sets the values in the filter panel equal to 
 * the values of the filterQuery object.
 */
function parseFilterQuery() {
	clearFilters();
	$("#country").val(filterQuery.country);
	$("#soruceCountry").val(filterQuery.sourceCountry);
	$("#sourceType").val(filterQuery.sourceType);
	$("#sourceRegion").val(filterQuery.sourceRegion);
	$("#sourceBS").val(filterQuery.sourceBS);
	$("#sourceSystem").val(filterQuery.sourceSystem);
	$("#vesselClass").val(filterQuery.vesselClass);
}


/**
 * Applys the values of the filter panel to the 
 * filterQuery object.
 */
function applyFilter() {
	resetFilterQuery();
	
	filterQuery.country = $("#country").val();
	filterQuery.sourceCountry = $("#soruceCountry").val();
	filterQuery.sourceType = $("#sourceType").val();
	filterQuery.sourceRegion = $("#sourceRegion").val();
	filterQuery.sourceBS = $("#sourceBS").val();
	filterQuery.sourceSystem = $("#sourceSystem").val();
	filterQuery.vesselClass = $("#vesselClass").val();
	
	filterChanged();
}


/**
 * Method for refreshing when filtering is changed.
 */
function filterChanged() {
	// Save query cookie
	var f = JSON.stringify(filterQuery);
	setCookie("dma-ais-query", f, 30);
	
    loadVessels();
}


/**
 * Method for saving the current view into a cookie.
 */
function saveViewCookie() {
	var center = map.getCenter();
	setCookie("dma-ais-zoom", map.zoom, 30);
	var lonlat = new OpenLayers.LonLat(map.center.lon, map.center.lat).transform(
		map.getProjectionObject(), // from Spherical Mercator Projection
		new OpenLayers.Projection("EPSG:4326") // to WGS 1984
	); 
	setCookie("dma-ais-lat", lonlat.lat, 30);
	setCookie("dma-ais-lon", lonlat.lon, 30);	
}


/**
 * Get settings from cookies
 */
function loadView() {
	
	var zoom = getCookie("dma-ais-zoom");
	var lat = getCookie("dma-ais-lat");
	var lon = getCookie("dma-ais-lon");
	var q = getCookie("dma-ais-query");
	if (zoom) {
		initialZoom = parseInt(zoom);
	}
	if (lat && lon) {
		initialLat = parseFloat(lat);
		initialLon = parseFloat(lon);
	}
	if (q) {
		eval("filterQuery = " + q + ";");
	}
}


/**
 * Method for setting a cookie.
 */
function setCookie(c_name,value,exdays) {
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}


/**
 * Method for getting a cookie.
 */
function getCookie(c_name) {
	var i,x,y,ARRcookies=document.cookie.split(";");
	for (i=0;i<ARRcookies.length;i++) {
		x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		x=x.replace(/^\s+|\s+$/g,"");
		if (x==c_name) {
			return unescape(y);
		}
	}
}
