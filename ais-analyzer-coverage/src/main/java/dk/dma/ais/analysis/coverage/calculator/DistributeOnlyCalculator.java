
package dk.dma.ais.analysis.coverage.calculator;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.coverage.AisCoverage;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.Station;
import dk.dma.ais.analysis.coverage.event.AisEvent;
import dk.dma.ais.analysis.coverage.event.AisEvent.Event;
import dk.dma.ais.analysis.coverage.event.IAisEventListener;
import dk.dma.ais.binary.SixbitException;


/**
 * This calculator requires an unfiltered ais stream (no doublet filtering).
 * It increments "received cell-message counter" for corresponding sources,
 * based on the messages that the supersource approves. 
 */
public class DistributeOnlyCalculator extends AbstractCalculator implements IAisEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(AisCoverage.class);
	private static final long serialVersionUID = -528305318453243556L;
	private long messagesProcessed = 0;
	private Date start = new Date();
	private LinkedHashMap<String, Map<String, CustomMessage>> receivedMessages = new LinkedHashMap<String, Map<String, CustomMessage>>() {
		private static final long serialVersionUID = -8805956769136748240L;
		@Override
	     protected boolean removeEldestEntry(Map.Entry eldest)
	     {
			((Map<String, CustomMessage>) eldest.getValue()).clear(); //seems to be necessary in order to keep application from performance degration.
	        return this.size() > 200000;   
	     }
	  };
	
	public DistributeOnlyCalculator(boolean ignoreRotation, HashMap<String, Station> map) {
		super(map);
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Date now = new Date();
					int elapsed = (int) ((now.getTime()-start.getTime())/1000);
//					System.out.println("messages/sec: "+ messagesProcessed/elapsed+ "... received messages "+receivedMessages.size());	
				}
			}
		});
		t1.start();
	}
	/*
	 * Takes supersource message as input. Finds matching messages for each source 
	 * and increments corresponding cells.
	 */
	private void approveMessage(CustomMessage aprrovedMessage){
		String key = aprrovedMessage.getKey();
		if(receivedMessages.containsKey(key)){
			Map<String, CustomMessage> approvedMessages = receivedMessages.get(key);
			for (CustomMessage customMessage : approvedMessages.values()) {
				//increment cell in each source
				Source source = dataHandler.getSource(customMessage.getSourceMMSI());
				
				Cell cell = dataHandler.getCell(source.getIdentifier(), customMessage.getLatitude(), customMessage.getLongitude());
				if (cell == null) {
					cell = dataHandler.createCell(source.getIdentifier(), customMessage.getLatitude(), customMessage.getLongitude());
				}
				
				dataHandler.getSource(source.getIdentifier()).incrementMessageCount();
				cell.incrementNOofReceivedSignals();
				dataHandler.updateCell(cell);
			}
			
			//Done processing - remove messages
			receivedMessages.remove(key);
		}
		else{
			LOG.error("Supersource approved a message, but it was not found in any sources "+key);
		}
	}
	/*
	 * When supersource approves a message, we need to find all sources that
	 * received the message and increment "received message counter" for corresponding cell
	 * in each source.
	 */
	@Override
	public void aisEventReceived(AisEvent event) {
		if(event.getEvent() == Event.AISMESSAGE_APPROVED){
			CustomMessage m = (CustomMessage) event.getEventObject();
			approveMessage(m);
			
		}else if(event.getEvent() == Event.AISMESSAGE_REJECTED){
			CustomMessage m = (CustomMessage) event.getEventObject();
			rejectMessage(m);
		}
	}
	
	private void rejectMessage(CustomMessage m) {
		Map<String, CustomMessage> map = receivedMessages.remove(m);
		if(map != null){
			map.clear();
		}
		
	}
	/*
	 * For testing purposes
	 */
	private void printMessage(CustomMessage m){
//		AisMessage aisM = m.getOriginalMessage();
//		try {
//			System.out.println(aisM.getEncoded().encode());
//		} catch (SixbitException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(m.getOriginalMessage());
//		System.out.println(m.getOriginalMessage().getClass());
//		System.out.println(m.getCog());
//		System.out.println(m.getLatitude());
//		System.out.println(m.getLongitude());
//		System.out.println(m.getShipMMSI());
//		System.out.println(m.getSog());
//		System.out.println(m.getTimestamp().getTime());
//		System.out.println(aisM.getSourceTag().getBaseMmsi());
//		System.out.println(messageToKey(m));
//		
//		System.out.println();
	}

	@Override
	public void calculate(CustomMessage m) {
		
		messagesProcessed++;
		Map<String, CustomMessage> list;
		String key = m.getKey();
		if(receivedMessages.containsKey(key)){
			list=receivedMessages.get(key);
		}else{
			list = new HashMap<String, CustomMessage>();
			receivedMessages.put(key, list);
		}
//		System.out.println(m.getLongitude());
		
		//we use a map, to filter doublets from a single source
		//Apparently, a ship sometimes send the same (apparently) message multiple times
		//within very little time... (smaller than the expected frequency).
		list.put(m.getSourceMMSI(),m);
	}
}
