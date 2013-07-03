var eventListUrl = "/abnormal/json/list";
var events = [];

function loadBehaviors(){

	var data = {};

	//data.eventTypes = "COG, SOG, SUDDEN_SPEED_CHANGE, CLOSE_ENCOUNTER";
	var now = new Date();
	data.eventTypes = "COG,SOG";
	data.timeMin = now.getTime() - (abnormalFeedLifeTime * 60 * 1000);

	$.getJSON(eventListUrl, data, 
		function (result) {

			events = [];

			// Load new vessels
			var JSONEvents = result;
	
			for (eventId in JSONEvents) {
			
				// Create vessel based on JSON data
				var eventJSON = JSONEvents[eventId];
				event = new AbnormalEvent(
							eventJSON.type, 
							eventJSON.description,
							eventJSON.location, 
							eventJSON.date,
							eventJSON.vessel,
							eventJSON.vesselName,
							eventJSON.shipType,
							eventJSON.shipLength,
							eventJSON.involvedVessels
						);
			
				events.push(event);
			}
			if (feedOpen){
				updateFeed();
			}
		}
	);

}


function updateFeed(){

	$("#feedContent").empty();

	// Rows
	$.each(events, function(key, value) { 
			$("#feedContent").append(value.toFeed());
		}		
	);

	if (feedOpen){
		$("#feedContainer").html($("#feedContent").html());
	}
}

/**
 * Nov 5, 2012 11:14:31 AM
 * yyyy-mm-dd hh:mm:ss
 */
function dateFromString(date){

	var month = date.split(" ")[0];
	var day = parseInt(date.split(" ")[1].split(",")[0]);
	var year = parseInt(date.split(" ")[2]);
	var time = date.split(" ")[3];
	var ampm = date.split(" ")[4];

	var hours = parseInt(time.split(":")[0]);
	var minutes = parseInt(date.split(":")[1]);
	var seconds = parseInt(date.split(":")[2]);

	if (ampm == "PM" && hours != 12){
		hours += 12;
	}

	month = getMonthFromString(month);
	
	return new Date(year, month, day, hours, minutes, seconds, 0);

}

function getMonthFromString(month){

	switch(month){
		case 'Jan' : return 0;
		case 'Feb' : return 1;
		case 'Mar' : return 2;
		case 'Apr' : return 3;
		case 'May' : return 4;
		case 'Jun' : return 5;
		case 'Jul' : return 6;
		case 'Aug' : return 7;
		case 'Sep' : return 8;
		case 'Oct' : return 9;
		case 'Nov' : return 10;
		case 'Dec' : return 11;
	}

	return 0;

}

