var events = [];

$(document).ready(function() {

	setup();
	
});

function setup(){

	if (includeAbnormalBehaviorTool){
		
		//loadEvents()
		//setInterval('checkArguments()', 1000);
		//setInterval('loadEvents()', 1000);

		//Set on change listener
		$('.abnormalInput').change(function() {
			loadEvents();
		});
		
	}
	
}

function loadEvents(){

	checkArguments();

	if (warnings == "" && abnormalOpen){

		var data = getEventArguments();

		$.getJSON(eventListUrl, data, 
			function (result) {

				events = [];

				// Load new vessels
				var JSONEvents = result;
		
				for (eventId in JSONEvents) {
					// Create vessel based on JSON data
					var eventJSON = JSONEvents[eventId];
					var event = new AbnormalEvent(
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

				drawEvents();
			}
		);
	}
}

function drawEvents(){

	// Clear event list
	$("#eventTableBody").empty('');

	// Rows
	$.each(events, function(key, value) { 

			$("#eventTableBody").append(value.toTableRow());
		}
		
	);

	// NumberOfEvents
	$("#flash").html(events.length + " events found.");
	$("#flash").css('visibility', 'visible');
	$("#flash").css('background-color', succesColor);
	
}

function getEventArguments(){

	// Get values
	var eventTypeCOG = $('#eventTypeCOG').attr('checked');
	var eventTypeSOG = $('#eventTypeSOG').attr('checked');
	var eventTypeSSC = $('#eventTypeSSC').attr('checked');
	var eventTypeCE = $('#eventTypeCE').attr('checked');
	var eventMMSI = $("#eventMMSI").val();
	var eventFromDate = $("#eventFromDate").val();
	var eventFromHour = $("#eventFromHour").val();
	var eventFromMin = $("#eventFromMin").val();
	var eventToDate = $("#eventToDate").val();
	var eventToHour = $("#eventToHour").val();
	var eventToMin = $("#eventToMin").val();

	var data = {};

	// Parse event types
	var eventTypes = [];
	if (eventTypeCOG){
		eventTypes.push("COG");
	}
	if (eventTypeSOG){
		eventTypes.push("SOG");
	}
	if (eventTypeSSC){
		eventTypes.push("SUDDEN_SPEED_CHANGE");
	}
	if (eventTypeCE){
		eventTypes.push("CLOSE_ENCOUNTER");
	}
	eventTypesStr = "";
	for(var i = 0; i < eventTypes.length; i++){
		eventTypesStr += eventTypes[i];
		if (i != eventTypes.length - 1){
			eventTypesStr += ",";
		}
	}
	data.eventTypes = eventTypesStr;

	// Parse MMSI
	if (eventMMSI != ""){
		data.mmsi = eventMMSI;
	}

	// Parse from date and time
	if (eventFromDate != "" 
			&& eventFromHour != ""  
			&& eventFromMin != ""){

		var dateFrom = parseDateAndTime(eventFromDate, eventFromHour, eventFromMin);
			
		data.timeMin = dateFrom.getTime();
		
	}

	// Parse to date and time
	if (eventToDate != "" 
			&& eventToHour != ""  
			&& eventToMin != ""){
			
		var dateTo = parseDateAndTime(eventToDate, eventToHour, eventToMin);
			
		data.timeMax = dateTo.getTime();
	}

	return data;
}


function parseDateAndTime(date, hour, min){

	var month = date.split("/")[0];
	month = parseInt(month) - 1;
	var day = date.split("/")[1];
	day = parseInt(day);
	var year = date.split("/")[2];
	year = parseInt(year);

	var myDate = new Date(year, month, day, hour, min, 0, 0);

	return myDate;

}

function checkArguments(){

	warnings = "";

	// Get values
	var eventTypeCOG = $('#eventTypeCOG').attr('checked');
	var eventTypeSOG = $('#eventTypeSOG').attr('checked');
	var eventTypeSSC = $('#eventTypeSSC').attr('checked');
	var eventTypeCE = $('#eventTypeCE').attr('checked');
	var eventMMSI = $("#eventMMSI").val();
	var eventFromDate = $("#eventFromDate").val();
	var eventFromHour = $("#eventFromHour").val();
	var eventFromMin = $("#eventFromMin").val();
	var eventToDate = $("#eventToDate").val();
	var eventToHour = $("#eventToHour").val();
	var eventToMin = $("#eventToMin").val();

	// Parse from date and time
	if (eventFromDate != "" 
			&& eventFromHour != ""  
			&& eventFromMin != ""
			&& eventToDate != ""
			&& eventToHour != ""  
			&& eventToMin != ""){

		var dateFrom = parseDateAndTime(eventFromDate, eventFromHour, eventFromMin);

		var dateTo = parseDateAndTime(eventToDate, eventToHour, eventToMin);

		// Compare dates and times
		if (dateFrom >= dateTo){
			warnings += '"From" must be before "To" <br>';
		}
		
	}
	
	if (warnings == ""){
		//$("#flash").css('visibility', 'hidden');
	} else {
		$("#flash").html(warnings);
		$("#flash").css('visibility', 'visible');
		$("#flash").css('background-color', warningColor);
	}

}

