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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.coverage.AisCoverage;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.Source.ReceiverType;
import dk.dma.ais.analysis.coverage.data.TimeSpan;
import dk.dma.ais.packet.AisPacketTags.SourceType;


public class SatCalculator extends AbstractCalculator {

	private static final Logger LOG = LoggerFactory.getLogger(SatCalculator.class);
	private int timeMargin = 600000; //in ms
	private LinkedHashMap<String, Boolean> doubletBuffer = new LinkedHashMap<String, Boolean>()
	{
	     @Override
	     protected boolean removeEldestEntry(Map.Entry eldest)
	     {
	        return this.size() > 10000;   
	     }
	};	
	
	/**
	 * Retrieves a list of time spans based on a rectangle defined på two lat-lon points. 
	 * Cells within the rectangle each contain a number of time spans. Two time spans will be merged
	 * if they are close to each other (closeness is defined by timeMargin). In that way the rectangle
	 * given by the user will be seen as one big cell.
	 * @param latStart
	 * @param lonStart
	 * @param latEnd
	 * @param lonEnd
	 * @return
	 */
	public List<TimeSpan> getTimeSpans(Date startTime, Date endTime, double latStart, double lonStart, double latEnd, double lonEnd){
			
		//Retrieve cells within the specified rectangle
    	Collection<Cell> cells = dataHandler.getCells();
		List<Cell> areaFiltered = new ArrayList<Cell>();
		for (Cell cell : cells) {
			if(cell.getLatitude() <= latStart && cell.getLatitude() >= latEnd ){
				if(cell.getLongitude() >= lonStart && cell.getLongitude() <= lonEnd ){
					areaFiltered.add(cell);
				}
			}
		}
		
		//Store every time span of the filtered cells
		List<TimeSpan> spans = new ArrayList<TimeSpan>();
		if(startTime != null && endTime != null){
			for (Cell cell : areaFiltered) {
				List<TimeSpan> individualSpan = cell.getTimeSpans();
				for (TimeSpan timeSpan : individualSpan) {
					if(timeSpan.getFirstMessage().getTime() > startTime.getTime() &&
							timeSpan.getLastMessage().getTime() < endTime.getTime()){
						spans.add(timeSpan);
					}
				}
			}	
		}else{
	    	for (Cell cell : areaFiltered) {
				List<TimeSpan> individualSpan = cell.getTimeSpans();
				for (TimeSpan timeSpan : individualSpan) {
					spans.add(timeSpan);
				}
			}	
		}
    	
    	//sort time spans based on date
    	Collections.sort(spans, new SortByDate());
    	
    	//Merge time spans that are too close to each other (specified on timeMargin)
    	List<TimeSpan> merged = new ArrayList<TimeSpan>();
    	TimeSpan current = null;
    	for (int i = 0; i < spans.size(); i++) {
    		if(current == null){
    			current = spans.get(i).copy();
    			merged.add(current);
    		}else{
    			TimeSpan next = spans.get(i).copy();
    			if(	next.getFirstMessage().getTime() < current.getLastMessage().getTime() ||
    				Math.abs(next.getFirstMessage().getTime()-current.getLastMessage().getTime()) < timeMargin){
    				
    				//Merge current and next time span
    				TimeSpan m = mergeTimeSpans(current, next);
    				merged.remove(merged.size()-1);	
    				merged.add(m);
    				current = m;
    				
    			}else{
    				//Current and next don't need to be merged
    				current = next;
    				merged.add(current);
    			}
    		}
    		
    		LOG.debug(spans.get(i).getFirstMessage()+" "+spans.get(i).getLastMessage()+" "+spans.get(i).getMessageCounter()+ " "+spans.get(i).getDistinctShips().size());
		}

    	return merged;
	}
	public Collection<Cell> getCells(double latStart, double lonStart, double latEnd, double lonEnd){
		Map<String, Boolean> sourcesMap = new HashMap<String, Boolean>();
		sourcesMap.put("sat", true);
		return dataHandler.getCells(latStart, lonStart, latEnd, lonEnd, sourcesMap, 1);
	}
	
	/**
	 * The message belongs to a cell. In this cell a new time span will be created if
	 * the time since the last message arrival is more than the timeMargin. Else, the 
	 * current time span will be updated.
	 * 
	 * The order of messages is not guaranteed. Some times we can not just use the latest time span of the cell
	 * because the message might need to go into an older time span. Or a new time span might need to be created
	 * in between existing time spans. In this case two spans might need to be merged, if the time difference is smaller
	 * than the timeMargin.
	 */
	@Override
	public void calculate(CustomMessage m) {
		
		if(filterMessage(m))
			return;

		
		//get the right cell, or create it if it doesn't exist.
		Cell c = dataHandler.getCell("sat", m.getLatitude(), m.getLongitude());
		if(c == null){
			c = dataHandler.createCell("sat", m.getLatitude(), m.getLongitude());
			c.setTimeSpans(new ArrayList<TimeSpan>());
		}		
		
		//If no time spans exist for corresponding cell, create one
		if(c.getTimeSpans().isEmpty()){
			c.getTimeSpans().add(new TimeSpan(m.getTimestamp()));
		}
		
		//We can not be sure that the message belongs to the latest time span (because order is not guaranteed).
		//Search through list backwards, until a time span is found where first message is older than the new one.
		TimeSpan timeSpan = null;
		int timeSpanPos = 0;
		for (int i = c.getTimeSpans().size()-1; i >= 0; i--) {
			TimeSpan t = c.getTimeSpans().get(i);
			if(t.getFirstMessage().getTime() <= m.getTimestamp().getTime()){
				timeSpan = t;
				timeSpanPos = i;
			}
		}

		//if no time span was found a new one has to be inserted at the beginning of the list
		if(timeSpan == null){
			timeSpan = new TimeSpan(m.getTimestamp());
			c.getTimeSpans().add(0,timeSpan);
			timeSpanPos = 0; //not necessary.. should be 0 at this point. Just to be sure.
		}
		
		
		//if time span is out dated, create new one and add it right after timeSpan.
		if(Math.abs(m.getTimestamp().getTime()-timeSpan.getLastMessage().getTime()) > timeMargin){
			timeSpan = new TimeSpan(m.getTimestamp());
			c.getTimeSpans().add(timeSpanPos+1,timeSpan);
			timeSpanPos = timeSpanPos+1;
			
		}

		//Set the last message, if the new one is newer than the existing last message
		if(timeSpan.getLastMessage().getTime() < m.getTimestamp().getTime()){
			timeSpan.setLastMessage(m.getTimestamp());
			
			//Check if the time span needs to be merged with the next (if timeMargin is larger than time difference)
			if(c.getTimeSpans().size() > timeSpanPos+1){
				TimeSpan nextSpan = c.getTimeSpans().get(timeSpanPos+1);
				if(Math.abs(nextSpan.getFirstMessage().getTime() - timeSpan.getLastMessage().getTime()) <= timeMargin){
					//remove old timespans from list
					c.getTimeSpans().remove(timeSpanPos);
					c.getTimeSpans().remove(timeSpanPos);
					
					//add the merged time span to the list
					TimeSpan merged = mergeTimeSpans(timeSpan, nextSpan);
					c.getTimeSpans().add(timeSpanPos, merged);
					timeSpan = merged;
				}
			}
			
		
			
		}
		
		
		
		//Put ship mmsi in the map
		timeSpan.getDistinctShips().put(""+m.getShipMMSI(), true);
		
		//Increment message counter
		timeSpan.setMessageCounter(timeSpan.getMessageCounter()+1);
				
	}
	private TimeSpan mergeTimeSpans(TimeSpan span1, TimeSpan span2){
		
		TimeSpan merged = new TimeSpan(span1.getFirstMessage());
		//merge two timespans
		merged.setLastMessage(span2.getLastMessage());
		merged.setMessageCounter(span1.getMessageCounter()+span2.getMessageCounter());
		Set<String> span1DistinctShips = span1.getDistinctShips().keySet();
		Set<String> span2DistinctShips = span2.getDistinctShips().keySet();
		for (String string : span1DistinctShips) {
			merged.getDistinctShips().put(string, true);
		}
		for (String string : span2DistinctShips) {
			merged.getDistinctShips().put(string, true);
		}
		return merged;
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
	 * Rules for filtering
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
