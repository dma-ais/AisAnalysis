/***********************
 * Configurations
 **********************/
var initialLat = 56.00;
var initialLon = 11.00;
var initialZoom = 7;
var focusZoom = 10;					// The zoom level when focus is changed to specific vessel
var vesselZoomLevel = 8;			// The maximum zoom level for which to draw vessels
var loadFrequence = 20*1000;		// The number of milliseconds between auto load*
var loadDelay = 400;				// The number of milliseconds between movement and load*
var loadCheckingFrequence = 100;	// The number of milliseconds between each check to see if it should load
var loadViewportOnly = true;

// * When the need for a load is detected it still waits for the loadCheckingFrequence.

// JSON URLs
var listUrl = '/aisview/json/anon_vessel_list';
var clusterUrl = '/aisview/json/vessel_clusters';
var detailsUrl = '/aisview/json/vessel_target_details';
var searchUrl = '/aisview/json/vessel_search';

// Clustering
var showClustering = true;
var clusterLimit = 10;
var showIndividualVessels = true;
var clusterSizes = [
	{zoom: 1, size: 5},
	{zoom: 2, size: 5},
	{zoom: 3, size: 4.5},
	{zoom: 4, size: 2.5},
	{zoom: 5, size: 1},
	{zoom: 6, size: 0.6},
	{zoom: 7, size: 0.32},
	{zoom: 8, size: 0.18},
];

/***********************
 * Panels
 **********************/
var includeStatusPanel = true;
var includeLoadingPanel = true;
var includeLegendsPanel = true;
var includeSearchPanel = true;
var includeFilteringPanel = true;
var includeDetailsPanel = true;
var includeZoomPanel = true;
	var zoomPanelPositionLeft = '248px';
	var zoomPanelPositionTop = '8px';


/***********************
 * Styling
 **********************/
var baseColorsOn = "count";
var clusterColors = [
	{color: "#ffdd00", densityLimit: 0.0	, countLimit: 0},	// Yellow
	{color: "#ff8800", densityLimit: 0.00125, countLimit: 50},	// Orange
	{color: "#ff0000", densityLimit: 0.004	, countLimit: 250},	// Red
	{color: "#ff00ff", densityLimit: 0.008	, countLimit: 1000}	// Purple
];

var clusterFillOpacity = 0.5;
var clusterStrokeColor = "#333333";
var clusterStrokeOpacity = 0.2;
var clusterStrokeWidth = 1;
var clusterFontColor = "#222222";
var clusterFontWeight = "bold";
var clusterFontSize = 8;
var clusterFontSizeFactor = 2;	// font size = map.zoom * factor
var clusterFontFamily = "'Lucida Grande', Verdana, Geneva, Lucida, Arial, Helvetica, sans-serif";

var indieVesselColor = "#550055";
var indieVesselStrokeColor = "#555555";
var indieVesselRadius = 2;
var indieVesselStrokeWidth = 1;

var pastTrackColor = "#CC2222";
var pastTrackOpacity = 1;
var pastTrackWidth = 3;

var timeStampColor = "#222222";
var timeStampFontSize = "11px";
var timeStampFontFamily = "'Lucida Grande', Verdana, Geneva, Lucida, Arial, Helvetica, sans-serif";
var timeStampFontWeight = "bold";
var timeStamtOutlineColor = "#eaeaea";


