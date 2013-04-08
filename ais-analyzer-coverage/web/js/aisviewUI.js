// Panel variables
var detailsOpen = false;
var legendsOpen = false;
var filteringOpen = false;
var sourcesOpen = false;
var searchOpen = false;
var detailsReadyToClose = false;
var selectControl;
var hoverControl;

// Layers
var vesselLayer;
var selectionLayer;
var tracksLayer;
var timeStampsLayer;
var clusterLayer;
var clusterTextLayer;
var indieVesselLayer;

function includePanels(){

	if (includeStatusPanel){
		$("#sourceDetailsPanel").css('visibility', 'visible');
	} else {
		$("#sourceDetailsPanel").remove();
	}
	
	if (includeStatusPanel){
		$("#sourcesPanel").css('visibility', 'visible');
	} else {
		$("#sourcesPanel").remove();
	}
	
	if (includeStatusPanel){
		$("#statusPanel").css('visibility', 'visible');
	} else {
		$("#statusPanel").remove();
	}

	if (includeLoadingPanel){
		$("#loadingPanel").css('visibility', 'hidden');
	} else {
		$("#loadingPanel").remove();
	}

	if (includeLegendsPanel){
		$("#legendsPanel").css('visibility', 'visible');
	} else {
		$("#legendsPanel").remove();
	}

	if (includeSearchPanel){
		$("#searchPanel").css('visibility', 'visible');
	} else {
		$("#searchPanel").remove();
	}

	if (includeFilteringPanel){
		$("#filteringPanel").css('visibility', 'visible');
	} else {
		$("#filteringPanel").remove();
	}

	if (includeDetailsPanel){
		$("#detailsPanel").css('visibility', 'visible');
	} else {
		$("#detailsPanel").remove();
	}

	if (!includeZoomPanel){
		$(".olControlZoom").remove();
	}
}

/**
 * Adds all the layers that will contain graphic.
 */
function addLayers(){

	// Get renderer
	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
	renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
	// renderer = ["Canvas", "SVG", "VML"];

	// Create vector layer with a stylemap for vessels
	vesselLayer = new OpenLayers.Layer.Vector(
			"Vessels",
			{
				styleMap: new OpenLayers.StyleMap({
					"default": {
						externalGraphic: "${image}",
						graphicWidth: "${imageWidth}",
						graphicHeight: "${imageHeight}",
						graphicYOffset: "${imageYOffset}",
						graphicXOffset: "${imageXOffset}",
						rotation: "${angle}"
					},
					"select": {
						cursor: "crosshair",
						externalGraphic: "${image}"
					}
				}),
				renderers: renderer
			}
		);

	map.addLayer(vesselLayer);

	// Create vector layer with a stylemap for the selection image
	selectionLayer = new OpenLayers.Layer.Vector(
			"Selection",
			{
				styleMap: new OpenLayers.StyleMap({
					"default": {
						externalGraphic: "${image}",
						graphicWidth: "${imageWidth}",
						graphicHeight: "${imageHeight}",
						graphicYOffset: "${imageYOffset}",
						graphicXOffset: "${imageXOffset}",
						rotation: "${angle}"
					},
					"select": {
						cursor: "crosshair",
						externalGraphic: "${image}"
					}
				}),
				renderers: renderer
			}
		);
		
	map.addLayer(selectionLayer);

	// Create vector layer for past tracks
	tracksLayer = new OpenLayers.Layer.Vector("trackLayer", {
        styleMap: new OpenLayers.StyleMap({'default':{
            strokeColor: pastTrackColor,
            strokeOpacity: pastTrackOpacity,
            strokeWidth: pastTrackWidth
        }})
    });

    map.addLayer(tracksLayer); 
	map.addControl(new OpenLayers.Control.DrawFeature(tracksLayer, OpenLayers.Handler.Path));  

	// Create vector layer for time stamps
	timeStampsLayer = new OpenLayers.Layer.Vector("timeStampsLayer", {
        styleMap: new OpenLayers.StyleMap({'default':{
            label : "${timeStamp}",
			fontColor: timeStampColor,
			fontSize: timeStampFontSize,
			fontFamily: timeStampFontFamily,
			fontWeight: timeStampFontWeight,
			labelAlign: "${align}",
			labelXOffset: "${xOffset}",
			labelYOffset: "${yOffset}",
			labelOutlineColor: timeStamtOutlineColor,
			labelOutlineWidth: 5,
			labelOutline:1
        }})
    });

	map.addLayer(timeStampsLayer); 

	// Create cluster layer
	clusterLayer = new OpenLayers.Layer.Vector( "Clusters", 
		{
		    styleMap: new OpenLayers.StyleMap({'default':{
		        fillColor: "${fill}",
		        fillOpacity: clusterFillOpacity,
		        strokeColor: clusterStrokeColor,
		        strokeOpacity: clusterStrokeOpacity,
		        strokeWidth: clusterStrokeWidth
        }})
    });
		
	map.addLayer(clusterLayer);

	// Create cluster text layer
	clusterTextLayer = new OpenLayers.Layer.Vector("Cluster text", 
		{
		    styleMap: new OpenLayers.StyleMap({'default':{
		        label : "${count}",
				fontColor: clusterFontColor,
				fontSize: "${fontSize}",
                fontWeight: clusterFontWeight,
				fontFamily: clusterFontFamily,
				labelAlign: "c",
        }})
    });

	map.addLayer(clusterTextLayer); 

	// Create layer for indivisual vessels in cluster 
	indieVesselLayer = new OpenLayers.Layer.Vector("Points", 
		{
		    styleMap: new OpenLayers.StyleMap({"default": {
                pointRadius: indieVesselRadius,
                fillColor: indieVesselColor,
                strokeColor: indieVesselStrokeColor,
                strokeWidth: indieVesselStrokeWidth,
                graphicZIndex: 1
        }})
    });

    map.addLayer(indieVesselLayer); 

	// Add OpenStreetMap Layer
	var osm = new OpenLayers.Layer.OSM(
		"OSM",
		"http://a.tile.openstreetmap.org/${z}/${x}/${y}.png",
		{
			'layers':'basic',
			'isBaseLayer': true
		} 
	);

	// Add OpenStreetMap Layer
	map.addLayer(osm);
	
	// Add KMS Layer
	//addKMSLayer();

}

/**
 * Sets up the panels, event listeners and selection controllers.
 */
function setupUI(){

	// Set zoom panel positon
	$(".olControlZoom").css('left', zoomPanelPositionLeft);
	$(".olControlZoom").css('top', zoomPanelPositionTop);

	// Set loading panel positon
	var x = $(document).width() / 2 - $("#loadingPanel").width() / 2;
	$("#loadingPanel").css('left', x);

	// Update mouse location when moved
	map.events.register("mousemove", map, function(e) { 
		var position = this.events.getMousePosition(e);
		pixel = new OpenLayers.Pixel(position.x, position.y);
		var lonLat = map.getLonLatFromPixel(pixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);
		$("#location").html(lonLat.lon.toFixed(4) + ", " + lonLat.lat.toFixed(4));
	});
	
	// Create functions for hovering a vessel
	var showName = function(e) {
		if (e.feature.attributes.vessel){
			$.getJSON(detailsUrl, {
				past_track: '1',
				id: e.feature.attributes.id
			}, function(result) {
				var pointVessel = new OpenLayers.Geometry.Point(e.feature.geometry.x, e.feature.geometry.y);
				var lonlatVessel = new OpenLayers.LonLat(pointVessel.x, pointVessel.y);
				var pixelVessel = map.getPixelFromLonLat(lonlatVessel);
				var pixelTopLeft = new OpenLayers.Pixel(0,0);
				var lonlatTopLeft = map.getLonLatFromPixel(pixelTopLeft)
				pixelTopLeft = map.getPixelFromLonLat(lonlatTopLeft);

				var x = pixel.x - pixelTopLeft.x;
				var y = pixel.y - pixelTopLeft.y;

				$("#vesselNameBox").html(result.name);
				$("#vesselNameBox").css('visibility', 'visible');
				$("#vesselNameBox").css('top', (y - 26) + 'px');
				$("#vesselNameBox").css('left', x + 'px');
			});
		}
	};

	var hideName = function(e) {
    	$("#vesselNameBox").css('visibility', 'hidden');
    };

	// Set click events on vessel details panel
	$("#sourceDetailsHeader").click(function() {
		if (detailsOpen){
			slideSourceDetailsDown();
		} else {
			slideSourceDetailsDown();
		}
	});
	
	// Set click events on sources panel
	$("#sourcesHeader").click(function() {
		if (sourcesOpen){
			$("#sourcesContainer").slideUp(
				{
					complete:function(){
						sourcesOpen = false;
						$("#sourcesContainer").html("");
						$("#sourcesPanel").removeClass("arrowUp");
						$("#sourcesPanel").addClass("arrowDown");
						checkForPanelOverflow();
					}
				}
			);
		} else {
			$("#sourcesContainer").css('display', 'none');
			refreshSourceList();
			$("#sourcesContainer").slideDown(
				{
					complete:function(){
						sourcesOpen = true;
						$("#sourcesPanel").removeClass("arrowDown");
						$("#sourcesPanel").addClass("arrowUp");
						checkForPanelOverflow();
					}
				}
			);
		}
	});
	
	// Close empty panels
	setInterval("closeEmptyPanels()", 1000);
	
}
function slideSourceDetailsUp(){
	$("#sourceDetailsHeader").html("Source details");
	$("#sourceDetailsContainer").slideUp(
			{
					complete:function(){
						detailsOpen = false;
						$("#sourceDetailsPanel").removeClass("arrowUp");
						$("#sourceDetailsPanel").addClass("arrowDown");
						checkForPanelOverflow();
				}
			}
		);
}
function slideSourceDetailsDown(){
	$("#sourceDetailsHeader").html("Source details <hr>");
	$("#sourceDetailsContainer").slideDown(
			{
				complete:function(){
					detailsOpen=true;
					$("#sourceDetailsPanel").removeClass("arrowDown");
					$("#sourceDetailsPanel").addClass("arrowUp");
					checkForPanelOverflow();
				}
			}
	);
}

var lastSearch = "";

function checkForSearch(){
	var val = $("#searchField").val();
	if (val != lastSearch){
		lastSearch = val;
		search(val);
	}
}

/**
 * Sets up the panels, event listeners and selection controller.
 */
function closeEmptyPanels(){
	if (detailsReadyToClose){
		$("#detailsContainer").slideUp(
			{
				complete:function(){
					$("#detailsHeader").html("Vessel details");
					detailsOpen = false;
					$("#detailsPanel").removeClass("arrowUp");
					$("#detailsPanel").addClass("arrowDown");
					checkForPanelOverflow();
					$("#detailsContainer").html("");
					detailsReadyToClose = false;

				}
			}
		);
	}
}

/**
 * Check if a panel overflows the window height.
 * The height of the panels will correct to fit.
 */
function checkForPanelOverflow(){
	var h = $(window).height();
	var lh = 370;				// The height used by legends
	var vdh = 496;				// The height of the vessel details
	var fih = 380;				// The height of the filtering
	var sh = 92;				// The height of the search panel

	if (searchOpen){
		sh = 92;
	} else {
		sh = 27;
	}
	h -= sh;
	
	if (legendsOpen && detailsOpen && filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, (h-lh)/2));
		$("#detailsContainer").css("overflow-y", "scroll");
		$("#filteringContainer").height(Math.min(fih, (h-lh)/2));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (legendsOpen && detailsOpen && !filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, h-lh));
		$("#detailsContainer").css("overflow-y", "scroll");
	} else if (legendsOpen && !detailsOpen && filteringOpen){
		$("#filteringContainer").height(Math.min(fih, h-lh));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!legendsOpen && detailsOpen && filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, h/2 - 70));
		$("#detailsContainer").css("overflow-y", "scroll");
		$("#filteringContainer").height(Math.min(fih, h/2 - 70));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!legendsOpen && !detailsOpen && filteringOpen){
		$("#filteringContainer").height(Math.min(fih, h - 60));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!legendsOpen && detailsOpen && !filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, h - 110));
		$("#detailsContainer").css("overflow-y", "scroll");
	} else {
		$("#filteringContainer").height('');
		$("#filteringContainer").css("overflow-y", "auto");
		$("#detailsContainer").height('');
		$("#detailsContainer").css("overflow-y", "auto");
	}
}

/**
 * Updates the vessel details panel to show informaiton 
 * of a specific vessel.
 *
 * @param feature
 *            The feature of the vessel
 */
function updateVesselDetails(feature){
	// Get details from server
	$.getJSON(detailsUrl, {
			past_track: '1',
			id: feature.attributes.id
		}, function(result) {			
			// Load and draw tracks
			var tracks = result.pastTrack.points;
			drawPastTrack(tracks);			

			// Load details
			$("#detailsContainer").html("");
			$("#vd_mmsi").html(result.mmsi);
			$("#vd_class").html(result.vesselClass);
			$("#vd_name").html(result.name);
			$("#vd_callsign").html(result.callsign);
			$("#vd_lat").html(result.lat);
			$("#vd_lon").html(result.lon);
			$("#vd_imo").html(result.imoNo);
			$("#vd_source").html(result.sourceType);
			$("#vd_type").html(result.vesselType);
			$("#vd_cargo").html(result.cargo);
			$("#vd_country").html(result.country);
			$("#vd_sog").html(result.sog + ' kn');
			$("#vd_cog").html(result.cog + ' &deg;');
			$("#vd_heading").html(result.heading + ' &deg;');
			$("#vd_draught").html(result.draught + ' m');
			$("#vd_rot").html(result.rot + ' &deg;/min');
			$("#vd_width").html(result.width + ' m');
			$("#vd_length").html(result.length + ' m');
			$("#vd_destination").html(result.destination);
			$("#vd_navStatus").html(result.navStatus);
			$("#vd_eta").html(result.eta);
			$("#vd_posAcc").html(result.posAcc);
			if (result.lastReceived != "undefined"){
				$("#vd_lastReport").html(	result.lastReceived.split(":")[0] + " min " + 
											result.lastReceived.split(":")[1] + " sec");
			} else {
				$("#vd_lastReport").html("undefined");
			}
			
			$("#vd_link").html('<a href="http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi=' + result.mmsi + '" target="_blank">Target info</a>');
			
			// Append details to vessel details panel
			$("#detailsContainer").append($("#vesselDetails").html());
			detailsReadyToClose = false;

			// Open vessel detals
			if (!detailsOpen){
				$("#detailsContainer").css('display', 'none');
				$("#detailsHeader").html("Vessel details<br /><hr />");
				$("#detailsContainer").slideDown(
					{
						complete:function(){
							detailsOpen = true;
							$("#detailsPanel").removeClass("arrowDown");
							$("#detailsPanel").addClass("arrowUp");
							checkForPanelOverflow();
							$("#detailsPanel").css('background-image', 'url("../img/arrowUp.png"');
						}
					}
				);
			}
		});
}

