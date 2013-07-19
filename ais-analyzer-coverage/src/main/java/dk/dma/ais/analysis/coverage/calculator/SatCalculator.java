package dk.dma.ais.analysis.coverage.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
			timeSpan = new TimeSpan(m.getTimestamp());
			c.getTimeSpans().add(timeSpan);
		}
		
		//if timespan is outdated, create new
		if(Math.abs(m.getTimestamp().getTime()-timeSpan.getLastMessage().getTime()) > timeMargin){
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

}
