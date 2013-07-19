package dk.dma.ais.analysis.coverage.data.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Cell;



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
}
