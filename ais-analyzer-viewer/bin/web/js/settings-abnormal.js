/***********************
 * Configurations
 **********************/
var initialLat = 56.00;
var initialLon = 11.00;
var initialZoom = 2;
var focusZoom = 15;					// The zoom level when focus is changed to specific vessel
var vesselZoomLevel = 8;			// The maximum zoom level for which to draw vessels

// Loading
var loadAllVessels = false;			// Whether or not to load all vessels in the world.
var loadFixedAreaSize = true;		// Whether or not to load a fixed area size when zoom level is higher than
									// vesselZoomLevel. If set to false, it will always load the size of the
									// viewport. If set to true it will always load the area size specified
									// by the fixedLoadAreaSize variable below.
var fixedLoadAreaSize = 15;			// The size of the area to load in earth longitude and latitude.
var loadFrequence = 60*1000;		// The number of milliseconds between auto load*
var loadDelay = 400;				// The number of milliseconds between movement and load*
var loadCheckingFrequence = 100;	// The number of milliseconds between each check to see if it should load
var loadViewportOnly = true;

var reloadPageAfterTime = false;		// Whether or not to reload the page after being idle in some time
var reloadPageTime = 10*60*1000;	// The idle time to relead page
var repositionWhenReloading = false;	// Whether or not to reposition when reloading
var repositionLocation = 
	{ 
		latitude: initialLat,
		longitude: initialLon,
		zoom: initialZoom
	};

// * When the need for a load is detected it still waits for the loadCheckingFrequence.

// JSON URLs
var listUrl = '/aisview/json/anon_vessel_list';
var clusterUrl = '/aisview/json/vessel_clusters';
var detailsUrl = '/aisview/json/vessel_target_details';
var searchUrl = '/aisview/json/vessel_search';
var eventListUrl = "/abnormal/json/list";

// Clustering
var includeClustering = true;
var clusterLimit = 10;
var showIndividualVessels = true;
var clusterSizes = [
	{zoom: 1, size: 6},
	{zoom: 2, size: 6},
	{zoom: 3, size: 4.5},
	{zoom: 4, size: 2.5},
	{zoom: 5, size: 1.5},
	{zoom: 6, size: 0.5},
	{zoom: 7, size: 0.25},
	{zoom: 8, size: 0.08},
];

// Event feeds
var includeEventFeed = true;			// Whether or not to load and show event feeds
var loadBehaviorsFrequence = 10000;		// The number of milliseconds between each abnormal behavior load
var abnormalFeedLifeTime = 15;					// The number of minutes event feeds are kept alive
var includeEventDateTime = true;
var includeEventLocation = true;
var includeEventType = true; 
var includeEventVesselName = true; 
var includeEventVesselMMSI = true; 
var includeEventVesselType = true; 

// Searching
var searchResultsLimit = 1000;			// The number of search results that will be shown in the list
var searchResultsShowPositon = false;	// Whether or not to show the position in the search results

// Past tracks and time stamps
var includePastTracks = true;			// Whether or not to show past tracks of selected vessels
var includeTimeStamps = true;			// Whether or not to show time stamps on past tracks
var includeTimeStampsOnCL = false;			// Whether or not to show time stamps on past tracks
var tracksBetweenTimeStampsVL = 10;		// The number of tracks (single lines) between each timestamp on 
										// vessel level
var tracksBetweenTimeStampsCL = 20;		// The number of tracks (single lines) between each timestamp on 
										// cluster level


/***********************
 * Tools
 **********************/
var includeAdvancedTools = true;
var includeAbnormalBehaviorTool = true;


/***********************
 * Panels
 **********************/
var includeStatusPanel = true;
var includeLoadingPanel = true;
var includeLegendsPanel = true;
var includeSearchPanel = true;
var includeFilteringPanel = true;
var includeDetailsPanel = true;
var includeAbnormalFeedPanel = true;
	var feedPanelExpandedWidth = '500px'	// CSS class .arrowUpWide needs to have same width minus 10
var includeZoomPanel = true;
	var zoomPanelPositionLeft = '248px';
	var zoomPanelPositionTop = '8px';
var includeAbnormalBehaviorPanel = true;// Whether or not to load and show the abnormal behavior panel


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

var warningColor = '#ff9999';
var succesColor = '#99ff99';

var fixedFilterQuery = null;
