package dk.dma.ais.analysis.coverage.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.Source.ReceiverType;
import dk.dma.ais.analysis.coverage.data.TimeSpan;
import dk.dma.ais.packet.AisPacketTags.SourceType;


public class SatCalculator extends AbstractCalculator {

	private int timeMargin = 600000; //in ms
	private LinkedHashMap<String, Boolean> doubletBuffer = new LinkedHashMap<String, Boolean>()
			  {
	     @Override
	     protected boolean removeEldestEntry(Map.Entry eldest)
	     {
	        return this.size() > 10000;   
	     }
	};	
	
	public List<TimeSpan> getTimeSpans(double latStart, double lonStart, double latEnd, double lonEnd){
		List<TimeSpan> spans = new ArrayList<TimeSpan>();
    	Collection<Cell> cells = dataHandler.getCells();
		List<Cell> areaFiltered = new ArrayList<Cell>();
		for (Cell cell : cells) {
			if(cell.getLatitude() <= latStart && cell.getLatitude() >= latEnd ){
				if(cell.getLongitude() >= lonStart && cell.getLongitude() <= lonEnd ){
					areaFiltered.add(cell);
				}
			}
		}
    	for (Cell cell : areaFiltered) {
			List<TimeSpan> individualSpan = cell.getTimeSpans();
			for (TimeSpan timeSpan : individualSpan) {
				spans.add(timeSpan);
			}
		}	
    	Collections.sort(spans, new SortByDate());
    	List<TimeSpan> merged = new ArrayList<TimeSpan>();
    	TimeSpan current = null;
    	for (int i = 0; i < spans.size(); i++) {
    		if(current == null){
    			current = spans.get(i).copy();
    			merged.add(current);
    		}else{
    			TimeSpan next = spans.get(i);
    			if(Math.abs(current.getLastMessage().getTime()-next.getLastMessage().getTime()) > timeMargin){
    				current = next.copy();
    				merged.add(current);
    			}else{
    				//merge two timespans
    				current.setLastMessage(next.getLastMessage());
    				current.setMessageCounter(current.getMessageCounter()+next.getMessageCounter());
    				Map<String, Boolean> distinctShips = current.getDistinctShips();
    				Set<String> nextDistinctShips = next.getDistinctShips().keySet();
    				for (String string : nextDistinctShips) {
    					distinctShips.put(string, true);
					}
    			}
    		}
    		
    		
    		System.out.println(spans.get(i).getFirstMessage()+" "+spans.get(i).getLastMessage()+" "+spans.get(i).getMessageCounter()+ " "+spans.get(i).getDistinctShips().size());
		}
    	System.out.println();
    	return merged;
	}
	public Collection<Cell> getCells(double latStart, double lonStart, double latEnd, double lonEnd){
		Map<String, Boolean> sourcesMap = new HashMap<String, Boolean>();
		sourcesMap.put("sat", true);
		return dataHandler.getCells(latStart, lonStart, latEnd, lonEnd, sourcesMap, 1);
	}
	
	@Override
	public void calculate(CustomMessage m) {
		
		if(filterMessage(m))
			return;

		//get the right cell
		Cell c = dataHandler.getCell("sat", m.getLatitude(), m.getLongitude());
		if(c == null){
			c = dataHandler.createCell("sat", m.getLatitude(), m.getLongitude());
			c.setTimeSpans(new ArrayList<TimeSpan>());
		}
		
		//Get or create time span
		TimeSpan timeSpan = null;
		if(!c.getTimeSpans().isEmpty()){
			timeSpan = c.getTimeSpans().get(c.getTimeSpans().size()-1);		
		}
		
		//If timespan does not exist, create it
		if(timeSpan == null){
//			System.out.println("CREATE");
			timeSpan = new TimeSpan(m.getTimestamp());
			c.getTimeSpans().add(timeSpan);
		}
		
		//if timespan is outdated, create new
		if(Math.abs(m.getTimestamp().getTime()-timeSpan.getLastMessage().getTime()) > timeMargin){
//			System.out.println("CREATE");
			timeSpan = new TimeSpan(m.getTimestamp());
			c.getTimeSpans().add(timeSpan);
		}

		//Update last message timestamp
		timeSpan.setLastMessage(m.getTimestamp());
		
		//Put ship mmsi in the map
		timeSpan.getDistinctShips().put(""+m.getShipMMSI(), true);
		
		//Increment message counter
		timeSpan.setMessageCounter(timeSpan.getMessageCounter()+1);
				
	}
	
	@Override
	/**
	 * Pretend that all messages are from same source
	 */
	protected Source extractBaseStation(String baseId, ReceiverType receiverType){
		Source grid = dataHandler.getSource("sat");
		if (grid == null) {
			grid = dataHandler.createSource("sat");
		}
		return grid;
	}
	
	/**
	 * Create rules for filtering
	 */
	@Override
	public boolean filterMessage(CustomMessage customMessage){
		if(customMessage.getSourceType() != SourceType.SATELLITE)
			return true;
		
		if(customMessage.getSog() < 3 || customMessage.getSog() > 50)
			return true;
		if(customMessage.getCog() == 360){
			return true;
		}
		if(isDoublet(customMessage))
			return true;
		
		return false;
	}

	private boolean isDoublet(CustomMessage m){
			String key = m.getKey();
	
			//if message exist in queue return true, otherwise false.
			if(doubletBuffer.containsKey(key)){
	//			System.out.println(bufferInSeconds);
				return true;
			}
			doubletBuffer.put(key, true);		
			return false;
			
		}
	
	 public class SortByDate implements Comparator<TimeSpan> {

	        public int compare(TimeSpan a1, TimeSpan a2) {
	            Date s1 = a1.getFirstMessage();
	            Date s2 = a2.getFirstMessage();
	            if(!s1.before(s2))
	            	return 1;
	            
	            return -1;
	        }
	    }

}
