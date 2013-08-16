// Global variables
var map;
var lastZoomLevel;
var polygonLayer;
var sourceLayer;
//var selectedSource = null;

var aisJsonClient;
var coverageUI;
var boxLayer;


/**
 * Sets up the map by adding layers and overwriting 
 * the 'map' element in the HTML index file.
 * Vessels are loaded using JSON and drawn on the map.
 */
function setupMap(){

	// Load cookies
	loadView();

	// Create the map and overwrite cotent of the map element
	map = new OpenLayers.Map({
        div: "map",
        projection: "EPSG:900913",
        fractionalZoom: true
    });

	aisJsonClient = new AisJsonClient();
	coverageUI = new CoverageUI();
	
	coverageUI.addLayers();
	
	var center = transformPosition(initialLon, initialLat);
	map.setCenter (center, initialZoom);
	lastZoomLevel = map.zoom;
	
	coverageUI.setupUI();

	
	aisJsonClient.getSources(function(sources){
		
		coverageUI.sources=sources;
		coverageUI.refreshSourceList();
		coverageUI.drawSources();
		
	});
	
	
	
	
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
function toLonLat(x, y){
	var pixel = new OpenLayers.Pixel(x, y);
	console.log("weh:"+y);
	return map.getLonLatFromPixel(pixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);
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
