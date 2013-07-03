/**
 * Abnormal event object
 *
 * @param from
 *			Top left LonLat
 * @param to
 *			Top right LonLat
 * @param count
 *			The number of vessels in the cluster
 * @param locations
 *			A list of vessel locations in the cluster.
 *			This list is empty if count is large.
 * @returns vessel object
 */
function AbnormalEvent(
		eventType, 
		description,
		location, 
		date,
		vesselMMSI,
		vesselName,
		vesselType,
		vesselLength,
		involvedVessels) {

	this.eventType = eventType;
	this.description = description;
	this.location = location;
	this.date = date;
	this.vesselMMSI = vesselMMSI;
	this.vesselName = vesselName;
	this.vesselType = vesselType;
	this.vesselLength = vesselLength;
	this.involvedVessels = involvedVessels;

	this.toFeed = function(){

		// Row with details
		var details = false;
		if (includeEventDateTime || includeEventLocation || includeEventType){
			details = true;
		}

		// Location
		var locLat = this.location.latitude.toFixed(3);
		var locLon = this.location.longitude.toFixed(3);
		var locStr = locLat + ", " + locLon;

		// Time
		var time = 	this.date.split(" ")[3] + " " +
					this.date.split(" ")[4];

		time = to24hClock(time);

		// Build html
		var html = "";
		
		if (this.vesselMMSI && this.vesselMMSI != ""){
		
			html += "<div class='event oldEvent' onClick='goToVesselMMSI(" + 
			this.vesselMMSI + ")'>";
			
		} else {

			html += "<div class='event oldEvent' onClick='goToLocation(" + 
			this.location.longitude + ", " + this.location.latitude + ")'>";

		}

		if (details){
			html += "<div class='eventDetails'>";
		}
		if (includeEventDateTime){
			html += "<div class='eventDetail'><b>Time</b><br/>" + time + "</div>";
		}
		if (includeEventLocation){
			html += "<div class='eventDetail'><b>Location</b><br/>" + locStr + "</div>";
		}
		if (includeEventType){
			html += "<div class='eventDetail'><b>Type</b><br/>" + this.eventType  + "</div>";
		}
		if (includeEventVesselName){
			html += "<div class='eventDetail'><b>Vessel name</b><br/>" + this.vesselName + "</div>";
		}
		if (includeEventVesselType){
			html += "<div class='eventDetail'><b>Vessel type</b><br/>" + this.vesselType + "</div>";
		}
		if (includeEventVesselMMSI){
			html += "<div class='eventDetail'><b>Vessel MMSI</b><br/>" + this.vesselMMSI + "</div>";
		}
		
		
		
		if (details){
			html += "</div>";
		}

		html += "<div class='eventDescription'>" +
				"<b>Description</b><br />" +
				this.description +
				"</div>";
				
		html += "</div>";

		return html;

	}

	this.toTableRow = function(){

		// Location
		var locLat = this.location.latitude.toFixed(3);
		var locLon = this.location.longitude.toFixed(3);
		var locStr = locLat + ", " + locLon;

		// Date
		var date = this.date;
		date = date.split(" ")[0] + " " + date.split(" ")[1] + " " + date.split(" ")[2];

		// Time
		var time = 	this.date.split(" ")[3] + " " +
					this.date.split(" ")[4];

		time = to24hClock(time);

		var str = 	"<tr>" +
							"<td>" + date + " " + time + "</td>" +
							"<td>" + this.eventType + "</td>" +
							"<td>" + this.description + "</td>" +
							"<td>" + this.vesselName + "</td>" +
							"<td>" + this.vesselType + "</td>" +
							
							"<td>" + 
							"<a href='#' onclick='" +
							"goToVesselMMSI(" + this.vesselMMSI + "); " + 
							"hideToolboxes();" + 
							"'>" + this.vesselMMSI + 
							"</td>" +
							
							"<td>" + 
							"<a href='#' onclick='" +
							"goToLocation(" + locLon + ", " + locLat + "); " + 
							"hideToolboxes();" + 
							"'>" + 
							locStr + 
							"</td>" +
							
							//"<td>" + event.vesselLength + "</td>" +
							//"<td>" + event.vesselType + "</td>" + 
							"<td>";

		// Add involved vessels
		/*
		if (event.involvedVessels != undefined){
			$.each(event.involvedVessels, function(key, value) { 

					str += value;

				}
			);
		}
		str += "</td><tr>";
		*/
		return str;

	}
	
}
