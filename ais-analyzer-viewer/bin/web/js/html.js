
function searchResultToHTML(vessel, key){

	var html = "<div class='event oldEvent' onClick='goToSearchedVessel(" + key + ")'>" +
					"<div class='panelText'>" +
						vessel.vesselName +
					"</div>";

	if (searchResultsShowPositon){
	html += 		"<div class='smallText'>" +
						vessel.lon + ", " + vessel.lat + 
					"</div>";

	}
	
	html +=		"</div>";

	return html;

}

