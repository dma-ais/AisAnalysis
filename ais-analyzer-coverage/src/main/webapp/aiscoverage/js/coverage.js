var changed = true;
var tid = setInterval(refresh, 2000);
var sources = [];
function refresh() {
	if(changed){
		drawCoverage();
		changed= false;
	}
}
$(".sourceCheckbox").live("change", function(e) {	
	sources[this.id].enabled=$(this).is(':checked');
	refreshSourceList();
//	drawSources();
	changed=true;
});
$("#selectall").live("change", function(e) {
	var element = $(this);
	$.each(sources, function(key, source) {
		source.enabled=element.is(':checked');
	});
	refreshSourceList();
//	drawSources();
	changed=true;
});
function refreshSourceList(){
	var sourceContainer = $("#sourcesContainer");
	sourceContainer.html("");
	$.each(sources, function(key, source) {
		var checked = "";
		if(source.enabled){
			checked = 'checked="checked"';
		}
		sourceContainer.append('<div class="legendsText" style="clear: left;height:15px;"><div class="rowElement"><input type="checkbox" class="sourceCheckbox" id="'+source.mmsi+'" '+checked+' style="margin-top:0px;" name="" value=""/></div><div class="rowElement" style="width:75px">'+source.mmsi+'</div><div  class="smallText rowElement">'+source.type+'</div></div>');
	});
	drawSources();
}

function enabledSourcesToString(){
	var string="";
	$.each(sources, function(key, source) {
		if(source.enabled){
			string += source.mmsi+",";
		}
	});
	return string;
}
function getSources(){
	$.post('/json/coverage', { action: "getSources"}, function(data) {
		sources = data;
		$.each(sources, function(key, val) {
			val.enabled=true;
			val.selected=false;
		});
		drawSources();
	});
}

function getMultiplicationFactor(){
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
//	alert(multifactor)
	return multifactor;
}
function drawSources(){
	sourceLayer.removeAllFeatures();
	$.each(sources, function(key, val) {
		drawSource(val);
	});
}
function drawSource(val){
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
//			 {externalGraphic: image, graphicHeight: 21, graphicWidth: 16, cursor: "crosshair", fillColor: "#ffcc66", pointRadius: "10"});
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
	if(selectedSource == val){
		feature.style["strokeColor"] = "blue";
	}else{
		feature.style["strokeColor"] = "black";
	}

	sourceLayer.addFeatures(feature);
}
var loading = false;
function drawCoverage(){
	if(loading){
		return;
	}
	var multifactor = getMultiplicationFactor();
	$('#multiplicationFactor').html(multifactor);
	loading = true;
	$("#loadingPanel").css('visibility', 'visible');
//	var multifactor = 10;
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
	
	polygonLayer.removeAllFeatures();
	var dataToBeSent = enabledSourcesToString();
	$.post('/json/coverage', { action: "getCoverage", sources: dataToBeSent, area: screenarea, multiplicationFactor: multifactor }, function(data) {

		$('#latSize').html(data.latSize.toFixed(4));
		$('#lonSize').html(data.lonSize.toFixed(4));
		
		  $.each(data.cells, function(key, val) {

			  var points = [
			        		new OpenLayers.Geometry.Point(val.lon, val.lat),
			        		new OpenLayers.Geometry.Point(val.lon, val.lat+data.latSize),
			        		new OpenLayers.Geometry.Point(val.lon+data.lonSize, val.lat+data.latSize),
			        		new OpenLayers.Geometry.Point(val.lon+data.lonSize, val.lat)
			        	];
			  var coverageValue = val.nrOfRecMes/(val.nrOfMisMes+val.nrOfRecMes);
			  var color;
			  if(coverageValue > 0.80){
				  color ='green';
			  }else if(coverageValue > 0.5){
				  color ='yellow';
			  }else{
				  color ='red';
			  }
			  drawPolygon(points, color);
		  });
		  loading = false;
		  $("#loadingPanel").css('visibility', 'hidden');
	});
	
	
}
function drawPolygon(points, fillColor){
	var style = new OpenLayers.Style();
	style.fillColor = fillColor;
	style.strokeWidth = '1';
	style.fillOpacity = '0.5';
	style.strokeColor = 'black';
	
	
	var site_points = [];
	for (i in points) {
	    points[i].transform(
	        new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
	        map.getProjectionObject() // to Spherical Mercator Projection
	      );
	    site_points.push(points[i]);
	}	
	var linear_ring = new OpenLayers.Geometry.LinearRing(site_points);
	polygonFeature = new OpenLayers.Feature.Vector(
	            new OpenLayers.Geometry.Polygon([linear_ring]),null,style);
	polygonLayer.addFeatures([polygonFeature]);
	
}