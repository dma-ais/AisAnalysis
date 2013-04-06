function CoverageUI () {
	var self = this;
	this.loading = false;
    this.sources = [];
    this.selectedSource = null;
    this.changed = true;
    this.minThreshold = 50;
    this.maxThreshold = 80;
    this.minExpectedMessages = 100;
    this.exportMultiplicationFactor = 4;


    
    this.setupUI = function(){

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

    	// Init expandable panels
    	$('#thresholdPanel').expandable({
    		header: "Coverage Settings"
    	});
    	$('#featureDetailsPanel').expandable({
    		header: "Source Details"
    	});
    	$('#sourcesPanel').expandable({
    		header: 'Sources </div><div id="selectAllHeader" class="smallText" style="margin:10px;">Select all<input type="checkbox" id="selectall" checked="yes" />'
    	});
    	$('#exportPanel').expandable({
    		header: "Export"
    	});

    	//add check box listeners
    	$(".sourceCheckbox").live("change", function(e) {	
    		self.sources[this.id].enabled=$(this).is(':checked');
    		self.refreshSourceList();
    		self.refreshSourceDetails();
    		self.changed=true;
    	});
    	$("#selectall").live("change", function(e) {
    		var element = $(this);
    		$.each(self.sources, function(key, source) {
    			source.enabled=element.is(':checked');
    		});
    		self.refreshSourceList();
    		self.refreshSourceDetails();
    		self.changed=true;
    	});
    	
    	//add export button listener
//    	$("#exportButton").click(function(){
//    		aisJsonClient.export(exportMultiplicationFactor, function(data){
//    			//done loading
//    			alert();
//    		})
//
//    	})
    	
    	//setup the threshold slider
    	$( "#slider-range" ).slider({
    		range: true,
    	    min: 0,
    	    max: 100,
    	    values: [ self.minThreshold, self.maxThreshold ],
    	    slide: function( event, ui ) {
    	    	self.minThreshold = ui.values[ 0 ];
    	    	self.maxThreshold = ui.values[ 1 ];
    	    	$( "#min-range" ).html( " < " + self.minThreshold + "% <= " );
    	    	$( "#max-range" ).html( " < " + self.maxThreshold + "% <= " );
    	    	self.changed = true;
    	    }
    	});
    	$( "#min-range" ).html( " < " + self.minThreshold + "% <= " );
    	$( "#max-range" ).html( " < " + self.maxThreshold + "% <= " );
    	
    	//setup min expected messages per cell slider
    	var minExpected = $("#minExpected");
    	minExpected.html(self.minExpectedMessages);
    	$( "#filterSlider" ).slider({
    		min: 0,
    		max: 1000,
    		value: self.minExpectedMessages,
    		slide: function(event, ui){
    			self.minExpectedMessages=ui.value;
    			minExpected.html(ui.value);
    			self.changed = true;
    		}
    	});
    	
    	//setup
    	var exportMultiplicationDivHidden = $("#exportMultiHidden");
    	var exportMultiplicationDiv = $("#exportMultiplicationFactor");
    	exportMultiplicationDivHidden.val(self.exportMultiplicationFactor);
    	exportMultiplicationDiv.html(self.exportMultiplicationFactor);
    	$( "#multiplicationSlider" ).slider({
    		min: 1,
    		max: 60,
    		value: self.exportMultiplicationFactor,
    		slide: function(event, ui){
    			self.minExpectedMessages=ui.value;
    			exportMultiplicationDiv.html(ui.value);
    			exportMultiplicationDivHidden.val(ui.value);
    		}
    	});
    	
    	
    	//setting the loop function
    	var myTimeout; 
    	function loopFunction () {;
	    	if(self.changed){
	    		self.drawCoverage();
	    		self.changed = false;
	    	}
    		myTimeout = setTimeout(loopFunction, 2000);
    	}
    	myTimeout = setTimeout(loopFunction, 2000);
    	
    	//map changed listener
    	map.events.register('moveend', this, function (event) {
    		
    		//update export div
    		self.exportMultiplicationFactor = self.getMultiplicationFactor();
    		exportMultiplicationDivHidden.val(self.exportMultiplicationFactor);
        	exportMultiplicationDiv.html(self.exportMultiplicationFactor);
    		$( "#multiplicationSlider" ).slider({value:self.getMultiplicationFactor()});
        	
        	
        	//draw coverage
    		coverageUI.drawCoverage();
        });
    }
    
    /**
     * Adds all the layers that will contain graphic.
     */
    this.addLayers = function(){

    	// Get renderer
    	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
    	renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
    	// renderer = ["Canvas", "SVG", "VML"];

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

    	//polygon layer
    	polygonLayer = new OpenLayers.Layer.Vector("Polygon Layer",{
    	    styleMap: new OpenLayers.StyleMap({
    	        "default": new OpenLayers.Style({
    	            strokeColor: "black",
    	            strokeOpacity: .7,
    	            strokeWidth: 1,
    	            fillColor: "${fillcolor}",
    	            fillOpacity: .6,
    	            cursor: "pointer"
    	        }),
    	        "select": new OpenLayers.Style({
    	            strokeColor: "blue",
    	            strokeOpacity: .7,
    	            strokeWidth: 4,
    	        })
    	    })
    	});
//    	polygonLayer.styleMap.styles['default'].defaultStyle={fillColor: 'blue'}
    	map.addLayers([polygonLayer]);
    	
    	//source layer
    	sourceLayer = new OpenLayers.Layer.Vector("Sources", {
    	    styleMap: new OpenLayers.StyleMap({
    	        "default": new OpenLayers.Style({
    	            strokeColor: "black",
    	            strokeOpacity: .7,
    	            strokeWidth: 1,
    	            fillColor: "${fillcolor}",
    	            fillOpacity: .6,
    	            cursor: "pointer"
    	        }),
    	        "select": new OpenLayers.Style({
    	            strokeColor: "blue",
    	            strokeOpacity: .7,
    	            strokeWidth: 4,
    	        })
    	    })
    	});
    	map.addLayer(sourceLayer);
    	
    	//select control for source layer
    	selectControl = new OpenLayers.Control.SelectFeature([sourceLayer, polygonLayer], 
    			{	
    				clickout: true,
    				hover: false,
    	            highlightOnly: false,
    	            eventListeners: {
    	                featurehighlighted: self.onFeatureSelect,
    	                featureunhighlighted: self.onFeatureUnselect
    	            }
    	
    			}
    		);
    	selectControl.handlers.feature.stopDown = false; 
    	map.addControl(selectControl);
    	selectControl.activate();
    }
    
    this.refreshSourceList = function(){	
    	var sourceContainer = $("#sourcesPanel");
    	sourceContainer.html("");
    	var sourceshtml = "";
    	$.each(self.sources, function(key, source) {
    		var checked = "";
    		if(source.enabled){
    			checked = 'checked="checked"';
    		}
    		sourceshtml += '<div class="legendsText" style="clear: left;height:15px;"><div class="rowElement"><input type="checkbox" class="sourceCheckbox" id="'+source.mmsi+'" '+checked+' style="margin-top:0px;" name="" value=""/></div><div class="rowElement" style="width:75px">'+source.mmsi+'</div><div  class="smallText rowElement">'+source.type+'</div></div>';
    	});
    	sourceContainer.html(sourceshtml);
    	self.drawSources();
    }
    
    this.refreshSourceDetails = function(){
    	
    	if(self.selectedSource == null){
    		return;
    	}
    	var checked="";
        if(self.selectedSource.enabled){
        	checked='checked="checked"';
        }

        $("#featureDetailsPanel").html('<div class="smallText">Id</div>'+
                '<div class="information">'+feature.mmsi+'</div>'+
                '<div class="smallText">Type</div>'+
                '<div class="information">'+feature.type+'</div>'+
                '<div class="smallText">Lat</div>'+
                '<div class="information">'+feature.lat.toFixed(4)+'</div>'+
                '<div class="smallText">Lon</div>'+
                '<div class="information">'+feature.lon.toFixed(4)+'</div>'+
                '<div class="smallText">Enabled</div>'+
                '<div class="information"><input type="checkbox" class="sourceCheckbox" id="'+feature.mmsi+'" '+checked+' name="" value=""/></div>');
    }
    
    this.drawSources = function(){
    	sourceLayer.removeAllFeatures();
    	var drawTheSource = this.drawSource
    	$.each(this.sources, function(key, val) {
    		drawTheSource(val);
    	});
    }
    
    this.drawSource = function(val){
    	var image;
    	if(val.enabled){
    		image = 'img/marker.png';
    	}else{
    		image = 'img/marker2.png'
    	}
    	var feature = new OpenLayers.Feature.Vector(
    			new OpenLayers.Geometry.Point( val.lon , val.lat ).transform(
    					new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
    					map.getProjectionObject() // to Spherical Mercator Projection
    				),
    			 {hmm:'100'});
//    			 {externalGraphic: image, graphicHeight: 21, graphicWidth: 16, cursor: "crosshair", fillColor: "#ffcc66", pointRadius: "10"});
    	feature.mmsi = val.mmsi;
    	feature.type = val.type;
    	feature.lat = val.lat;
    	feature.lon = val.lon;
    	feature.source = val;
    	val.feature=feature;
    	
    	if(val.enabled){
    		feature.style = {
    	            pointRadius: "10", // sized according to type attribute
    	            fillColor: "#980000 ",
    	            strokeColor: "black",
    	            strokeWidth: 2,
    	            graphicZIndex: 1,
    	            cursor: "crosshair"
    		}
    	}else{
    		feature.style = {
    	            pointRadius: "10", // sized according to type attribute
    	            fillColor: "white",
    	            strokeColor: "black",
    	            strokeWidth: 2,
    	            graphicZIndex: 1,
    	            cursor: "crosshair"
    		}
    	}
    	if(self.selectedSource == val){
    		feature.style["strokeColor"] = "blue";
    	}else{
    		feature.style["strokeColor"] = "black";
    	}

    	sourceLayer.addFeatures(feature);
    }
    
    
    this.drawCoverage = function(){
    	
    	//get the multiplication factor for corresponding zoom level
    	var multifactor = self.getMultiplicationFactor();
//    	multifactor = 5;
    	$('#multiplicationFactor').html(multifactor);
    	
    	// activate loading panel
    	$("#loadingPanel").css('visibility', 'visible');
    	
    	// get lat lon points for each screen corner
    	var topleftpixel = new OpenLayers.Pixel(0, 0);
    	var bottomrightpixel = new OpenLayers.Pixel($("#map").width(), $("#map").height());
    	var topleftlonlat = map.getLonLatFromPixel(topleftpixel).transform(
    		map.getProjectionObject(), // from Spherical Mercator Projection
    		new OpenLayers.Projection("EPSG:4326") // to WGS 1984
    	);
    	var bottomrightlonlat = map.getLonLatFromPixel(bottomrightpixel).transform(
    		map.getProjectionObject(), // from Spherical Mercator Projection
    		new OpenLayers.Projection("EPSG:4326") // to WGS 1984
    	);
    	var screenarea = topleftlonlat.lat.toFixed(4)+","+topleftlonlat.lon.toFixed(4)+","+bottomrightlonlat.lat.toFixed(4)+","+bottomrightlonlat.lon.toFixed(4);
    	
    	//convert enabled sources to string to be sent
    	var dataToBeSent = self.enabledSourcesToString();
    	
    	polygonLayer.removeAllFeatures();
    	
    	//use json client to fetch data from web service
    	aisJsonClient.getCoverage(dataToBeSent, screenarea, multifactor, function(data){
    		
    		$('#latSize').html(data.latSize.toFixed(4));
    		$('#lonSize').html(data.lonSize.toFixed(4));
    		
    		var minExpectedMessages = 
    		$.each(data.cells, function(key, val) {
  			  var points = [
  			        		new OpenLayers.Geometry.Point(val.lon, val.lat),
  			        		new OpenLayers.Geometry.Point(val.lon, val.lat+data.latSize),
  			        		new OpenLayers.Geometry.Point(val.lon+data.lonSize, val.lat+data.latSize),
  			        		new OpenLayers.Geometry.Point(val.lon+data.lonSize, val.lat)
  			        	];
  			  var expectedMessages = (val.nrOfMisMes+val.nrOfRecMes);
  			  var coverageValue = val.nrOfRecMes/expectedMessages;
  			  var color;
  			  if(expectedMessages >= self.minExpectedMessages){
	  			  if(coverageValue >= self.maxThreshold/100){
	  				  color ='green';
	  			  }else if(coverageValue >= self.minThreshold/100){
	  				  color ='yellow';
	  			  }else{
	  				  color ='red';
	  			  }
	  			  
	  			  self.drawPolygon({
	  				  lat: val.lat,
	  				  lon: val.lon,
	  				  points: points,
	  				  fillcolor: color,
	  				  totalMessages: (val.nrOfMisMes+val.nrOfRecMes),
	  				  receivedMessages: val.nrOfRecMes
	  			  });
  			  }
  		  });
//  		  self.loading = false;
  		  $("#loadingPanel").css('visibility', 'hidden');
    	});
    }
    
    this.drawPolygon = function(options){
    	var site_points = [];
    	for (i in options.points) {
    	    options.points[i].transform(
    	        new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
    	        map.getProjectionObject() // to Spherical Mercator Projection
    	      );
    	    site_points.push(options.points[i]);
    	}	
    	var linear_ring = new OpenLayers.Geometry.LinearRing(site_points);
    	polygonFeature = new OpenLayers.Feature.Vector(
    	            new OpenLayers.Geometry.Polygon([linear_ring]),null);
    	polygonFeature.attributes.fillcolor = options.fillcolor;
    	polygonFeature.type = "cell";
    	polygonFeature.lon = options.lon;
    	polygonFeature.lat = options.lat;
    	polygonFeature.totalMessages = options.totalMessages;
    	polygonFeature.receivedMessages = options.receivedMessages;
    	polygonLayer.addFeatures([polygonFeature]);
    	
    }
    
    this.enabledSourcesToString = function(){
    	var string="";
    	$.each(this.sources, function(key, source) {
    		if(source.enabled){
    			string += source.mmsi+",";
    		}
    	});
    	return string;
    }
    
    this.getMultiplicationFactor = function(){
    	var zLevel = map.getZoom();
    	var multifactor;
    	if(zLevel > 10){
        	multifactor = 1;
        }else if(zLevel == 10){
        	multifactor = 1;
        }else if(zLevel == 9){
        	multifactor = 2;
        }else if(zLevel == 8){
        	multifactor = 3;
        }else if(zLevel == 7){
        	multifactor = 4;
        }else if(zLevel == 6){
        	multifactor = 8;
        }else if(zLevel == 5){
        	multifactor = 20;
        }else if(zLevel == 4){
        	multifactor = 40;
        }else if(zLevel == 3){
        	multifactor = 60;
        }else if(zLevel == 2){
        	multifactor = 60;
        }else if(zLevel == 1){
        	multifactor = 80;
        }
//    	alert(multifactor)
    	return multifactor;
    }
    
    this.onFeatureSelect = function(evt) {	
    	
        feature = evt.feature;
        
        //determine if feature is a cell or a source
        if(feature.type == "cell"){
        	
        	//remove potential source
        	self.selectedSource = null;
        	self.drawSources();
        	
        	//Setting up cell details panel
        	$("#featureDetailsPanel > .panelHeader").html("Cell Details");
        	$("#featureDetailsPanel").html('<div class="smallText">Source</div>'+
                    '<div class="information">'+feature.mmsi+'</div>'+
                    '<div class="smallText">Cell Latitude</div>'+
                    '<div class="information">'+feature.lat.toFixed(4)+'</div>'+
                    '<div class="smallText">Cell Longitude</div>'+
                    '<div class="information">'+feature.lon.toFixed(4)+'</div>'+
                    '<div class="smallText">Received Messages</div>'+
                    '<div class="information">'+feature.receivedMessages+'</div>'+
                    '<div class="smallText">Expected Messages</div>'+  
                    '<div class="information">'+feature.totalMessages+'</div>'+
                    '<div class="smallText">Coverage</div>'+
                    '<div class="information">'+((feature.receivedMessages/feature.totalMessages)*100).toFixed(2)+' %</div>');
        	$("#featureDetailsPanel").slideDown();
        	
        	
        }else{
        	self.selectedSource = self.sources[feature.mmsi];
            self.refreshSourceDetails();
            $("#featureDetailsPanel").slideDown();
            $("#featureDetailsPanel > .panelHeader").html("Source Details");
            self.drawSources();
        }
        
    }
    
    this.onFeatureUnselect = function(evt) {

//    	feature = evt.feature;
//    	
//    	//determine if feature is a cell or a source
//    	if(feature.type == "cell"){
//
//        }
////    	else{
//        	self.selectedSource = null;
////        	$("#featureDetailsPanel").slideUp();
//        	self.drawSources();
////        }
    	
    }
 
}