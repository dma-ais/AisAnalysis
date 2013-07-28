package dk.dma.ais.analysis.coverage.data.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.TimeSpan;



public class JsonConverter {

	public static Map<String,JsonSource> toJsonSources(Collection<Source> sources){
		Map<String,JsonSource> sourcesMap = new HashMap<String,JsonSource>();
		for (Source baseStation : sources) {
			JsonSource s = new JsonSource();
			s.mmsi=baseStation.getIdentifier();
			
			s.type=baseStation.getReceiverType().name();
			
			if(baseStation.getLatitude() != null)
				s.lat=baseStation.getLatitude();
			
			if(baseStation.getLongitude() != null)
				s.lon=baseStation.getLongitude();
			
			sourcesMap.put(s.mmsi, s);
		}
		return sourcesMap;
	}
	
	public static JsonCell toJsonCell(Cell cell, Cell superCell) {
		long expected = (superCell.getNOofReceivedSignals()+superCell.getNOofMissingSignals());
		
		JsonCell Jcell = new JsonCell();
		Jcell.lat = cell.getLatitude();
		Jcell.lon = cell.getLongitude();
		Jcell.nrOfMisMes = expected - cell.getNOofReceivedSignals();
		Jcell.nrOfRecMes = cell.getNOofReceivedSignals();

		return Jcell;
	}
	
	public static List<JsonTimeSpan> toJsonTimeSpan(List<TimeSpan> timespans){
		List<JsonTimeSpan> jsonSpans = new ArrayList<JsonTimeSpan>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		
		TimeSpan first = null;
		TimeSpan previous = null;
		for (TimeSpan timeSpan : timespans) {
			JsonTimeSpan jsonspan = new JsonTimeSpan();
			
			if(first == null)
				first = timeSpan;
			
			long timeSinceLastTimeSpan = 0;
			if(previous != null)
				timeSinceLastTimeSpan=Math.abs(timeSpan.getFirstMessage().getTime() - previous.getLastMessage().getTime())/1000/60;
				
			//last is determined by the order of receival, but it is not guaranteed that the tag is actually the last
			//from time, to time, data time, time since last timespan, accumulated time, signals, distinct ships
			jsonspan.fromTime=timeSpan.getFirstMessage().getTime();
			jsonspan.toTime=timeSpan.getLastMessage().getTime();
			jsonspan.spanLength=(int) (Math.abs(timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime())/1000/60);
			if(jsonspan.spanLength == 0)
				jsonspan.spanLength=1;
			jsonspan.timeSinceLastSpan=(int) timeSinceLastTimeSpan;
			jsonspan.accumulatedTime=(int) (Math.abs(timeSpan.getLastMessage().getTime()-first.getLastMessage().getTime())/1000/60);
			jsonspan.signals=timeSpan.getMessageCounter();
			jsonspan.distinctShips=timeSpan.getDistinctShips().size();
			
			jsonSpans.add(jsonspan);
			previous=timeSpan;

		}
		return jsonSpans;
	}
}
