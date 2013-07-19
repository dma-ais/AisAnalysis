package dk.dma.ais.analysis.coverage.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Source.ReceiverType;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.Ship;
import dk.dma.ais.analysis.coverage.data.Ship.ShipClass;
import dk.dma.ais.analysis.coverage.event.AisEvent;
import dk.dma.ais.analysis.coverage.event.AisEvent.Event;
import dk.dma.ais.analysis.coverage.event.IAisEventListener;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage4;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.proprietary.IProprietarySourceTag;
import dk.dma.enav.model.geometry.Position;


/**
 * This calculator expects a filtered data stream! (No doublets)
 * The stream must not be downsampled!
 * 
 * It maintains a buffer for each Ship instance.
 * 
 * Rotation is determined based on difference between course over ground (cog)
 * from first and last message in buffer. If rotation is ignored, missing points
 * will only be calculated for ships that are NOT rotating.
 */
public class SupersourceCoverageCalculator extends AbstractCalculator {

	private static final long serialVersionUID = 1L;
	private int bufferInSeconds = 20;
	private int degreesPerMinute = 20;
	private boolean ignoreRotation;
	private List<IAisEventListener> listeners = new ArrayList<IAisEventListener>();
	private LinkedHashMap<String, CustomMessage> doubletBuffer = new LinkedHashMap<String, CustomMessage>()
			  {
	     @Override
	     protected boolean removeEldestEntry(Map.Entry eldest)
	     {
	        return this.size() > 10000;   
	     }
	};	

	public void addListener(IAisEventListener l){
		listeners.add(l);
	}
	public void broadcastEvent(AisEvent e){
		for (IAisEventListener l : listeners) {
			l.aisEventReceived(e);
		}
	}

//	public SupersourceCoverageCalculator(AisCoverageProject project, boolean ignoreRotation) {
//		super(project);
//		this.ignoreRotation = ignoreRotation;
//		
//		
//	}
	public SupersourceCoverageCalculator(boolean ignoreRotation) {
		super();
		this.ignoreRotation = ignoreRotation;
		
		
	}

	private boolean checkDoublets(CustomMessage m){
		String key = m.getKey();
//		System.out.println(key);

		//if message exist in queue return true, otherwise false.
		if(doubletBuffer.containsKey(key)){
//			System.out.println(bufferInSeconds);
			return true;
		}
		doubletBuffer.put(key, m);		
		return false;
		
	}
	
	public boolean debug = false;
	/**
	 * This is called whenever a message is received
	 */
	public void calculate(CustomMessage message) {

		if(checkDoublets(message))
			return;
		
		Ship ship = dataHandler.getShip(message.getSourceMMSI(), message.getShipMMSI());

		// put message in ships' buffer
		ship.addToBuffer(message);
				
		// If this message is filtered, we empty the ships' buffer and returns
		if (filterMessage(message)) {
			ship.emptyBuffer();
			List<CustomMessage> list = ship.getMessages();
			for (CustomMessage m : list) {
				this.broadcastEvent(new AisEvent(Event.AISMESSAGE_REJECTED,this,m));
			}
			
			return;
		}		

		// Time difference between first and last message in buffer
		CustomMessage firstMessage = ship.getFirstMessageInBuffer();
		CustomMessage lastMessage = ship.getLastMessageInBuffer();
		
		if(ship.getMessages().size() == 1)
			return;
		
		double timeDifference = this.getTimeDifference(firstMessage, lastMessage);
		// Check if it is time to process the buffer
		if (timeDifference >= bufferInSeconds) {

			List<CustomMessage> buffer = ship.getMessages();
			double rotation = Math.abs(angleDiff(firstMessage.getCog(),
					lastMessage.getCog()));

			// Ship is rotating
			if (rotation > ((double) degreesPerMinute / 60)
					* timeDifference) {
				if (!ignoreRotation) {
					for (int i = 0; i < ship.getMessages()
							.size() - 1; i++) {
						calculateMissingPoints(buffer.get(i),
								buffer.get(i + 1), true);
					}
				}
			} 
			//ship is not rotating
			else {
				for (int i = 0; i < ship.getMessages().size() - 1; i++) {
					calculateMissingPoints(buffer.get(i),
							buffer.get(i + 1), false);
				}
			}

			// empty buffer
			ship.emptyBuffer();
		}
	}

	/**
	 * Calculates missing points between two messages and add them to
	 * corresponding cells
	 */
	private void calculateMissingPoints(CustomMessage m1, CustomMessage m2,
			boolean rotating) {
		
		Source source = dataHandler.getSource(m1.getSourceMMSI());
		Ship ship = dataHandler.getShip(m1.getSourceMMSI(), m1.getShipMMSI());
		
		// Get cell from first message and increment message count
		Cell cell = dataHandler.getCell(source.getIdentifier(), m1.getLatitude(), m1.getLongitude());
		if (cell == null) {
			cell = dataHandler.createCell(source.getIdentifier(), m1.getLatitude(), m1.getLongitude());
		}
		dataHandler.getSource(source.getIdentifier()).incrementMessageCount();
		cell.incrementNOofReceivedSignals();
		dataHandler.updateCell(cell);
		this.broadcastEvent(new AisEvent(Event.AISMESSAGE_APPROVED,this,m1));

		Long p1Time = m1.getTimestamp().getTime();
		Long p2Time = m2.getTimestamp().getTime();
		double p1Lat = m1.getLatitude();
		double p1Lon = m1.getLongitude();
		double p2Lat = m2.getLatitude();
		double p2Lon = m2.getLongitude();
		projection.setCentralPoint(p1Lon, p1Lat);
		double p1X = projection.lon2x(p1Lon, p1Lat);
		double p1Y = projection.lat2y(p1Lon, p1Lat);
		double p2X = projection.lon2x(p2Lon, p2Lat);
		double p2Y = projection.lat2y(p2Lon, p2Lat);

		double timeSinceLastMessage = getTimeDifference(p1Time, p2Time);
		int sog = (int) m2.getSog();
		double expectedTransmittingFrequency = getExpectedTransmittingFrequency(
				sog, rotating, ship.getShipClass());
		/*
		 * Calculate missing messages and increment missing signal to
		 * corresponding cell. Lat-lon points are calculated to metric x-y
		 * coordinates before missing points are calculated. In order to find
		 * corresponding cell, x-y coords are converted back to lat-lon.
		 */
		
		int missingMessages;
		if (timeSinceLastMessage > expectedTransmittingFrequency) {

			// Number of missing points between the two points
			missingMessages = (int) (Math.round(timeSinceLastMessage
					/ expectedTransmittingFrequency) - 1);

			// Finds lat/lon of each missing point and adds "missing signal" to
			// corresponding cell
			for (int i = 1; i <= missingMessages; i++) {
				double xMissing = getX((i * expectedTransmittingFrequency),
						p1Time, p2Time, p1X, p2X);
				double yMissing = getY(i * expectedTransmittingFrequency,
						p1Time, p2Time, p1Y, p2Y);

				// Add number of missing messages to cell
				Cell c = dataHandler.getCell(source.getIdentifier(), projection.y2Lat(xMissing, yMissing), projection.x2Lon(xMissing, yMissing));
				if (c == null) {
					c = dataHandler.createCell(source.getIdentifier(), projection.y2Lat(xMissing, yMissing), projection.x2Lon(xMissing, yMissing));
				}
				//TODO shipslist in cell current not used?
//				c.getShips().put(m1.getShip().getMmsi(), m1.getShip());
				c.incrementNOofMissingSignals();
				dataHandler.updateCell(c);
				
			}
		}
	}

	
	/** We need to override; we only need one source. Instead of distributing the
	 *  messages to the "origin source", we distribute to the super source.
	 */
	@Override
	public CustomMessage aisToCustom(AisMessage aisMessage, String defaultID){
		
		//Stops analysis if project has been running longer than timeout
//		long timeSinceStart = project.getRunningTime();
//		if (project.getTimeout() != -1 && timeSinceStart > project.getTimeout())
//			project.stopAnalysis();

		String baseId = null;
		ReceiverType receiverType = ReceiverType.NOTDEFINED;
		AisPositionMessage posMessage = null;
		Position pos = null;
		Date timestamp = null;
		ShipClass shipClass = null;

		// Get source tag properties
		IProprietarySourceTag sourceTag = aisMessage.getSourceTag();
		if (sourceTag != null) {
			Integer bsmmsi = sourceTag.getBaseMmsi();
			bsmmsi=-666;
			timestamp = sourceTag.getTimestamp();
//			srcCountry = sourceTag.getCountry();
			String region = sourceTag.getRegion();
			if(bsmmsi == null){
				if(!region.equals("")){
					baseId = region;
					receiverType = ReceiverType.REGION;
				}
			}else{
				baseId = bsmmsi+"";
				receiverType = ReceiverType.BASESTATION;
			}
		}

		//Checks if its neither a basestation nor a region
		if (baseId == null){
			baseId = defaultID;
		}
		
		// If time stamp is not present, we add one
		if(timestamp == null){
			timestamp = new Date();
		}
		
		// It's a base station positiion message
		if (aisMessage instanceof AisMessage4) {
			extractBaseStationPosition((AisMessage4) aisMessage);	
			return null;
		}
		
		// if no allowed ship types has been set, we process all ship types
//		if(!isShipAllowed(aisMessage))
//			return null;

		// Handle position messages. If it's not a position message 
		// the calculators can't use them
		if (aisMessage instanceof AisPositionMessage)
			posMessage = (AisPositionMessage) aisMessage;
		else 
			return null;

		//Check if ship type is allowed
//		shipClass = extractShipClass(aisMessage);
//		if(!allowedShipClasses.containsKey(shipClass))
//			return null;

		// Check if position is valid
		if (!posMessage.isPositionValid()) {
			return null;
		}
		
		// Get location
		pos = posMessage.getPos().getGeoLocation();
		
		//calculate lat lon size based on first message
//		if(firstMessage == null){
//			System.out.println(aisMessage.getUserId());
//			calculateLatLonSize(pos.getLatitude());
//		}

		// Extract Base station
//		BaseStation baseStation = extractBaseStation(baseId, receiverType);
		Source baseStation = extractBaseStation("supersource", ReceiverType.NOTDEFINED);

		// Extract ship
		Ship ship = extractShip(aisMessage.getUserId(), shipClass, baseStation);

		CustomMessage newMessage = new CustomMessage();
		newMessage.setCog( (double) posMessage.getCog() / 10 );
		newMessage.setSog( (double) posMessage.getSog() / 10 );
		newMessage.setLatitude( posMessage.getPos().getGeoLocation()
				.getLatitude() );
		newMessage.setLongitude( posMessage.getPos().getGeoLocation()
				.getLongitude() );
		newMessage.setTimestamp( timestamp );
		newMessage.setSourceMMSI(baseStation.getIdentifier());
		newMessage.setShipMMSI( ship.getMmsi() );
//		newMessage.setOriginalMessage(aisMessage); //impacts performance if this is set. Only for test purposes
		newMessage.setKey(messageToKey(newMessage));
		
		// Keep track of current message
		currentMessage = newMessage;
		
		// Keep track of first message
		if(firstMessage == null){
			firstMessage = newMessage;
		}
		return newMessage;
	}
	
	
	/**
	 * Calculates the signed difference between angle A and angle B
	 * 
	 * @param a
	 *            Angle1 in degrees
	 * @param b
	 *            Angle2 in degrees
	 * @return The difference in degrees
	 */
	private double angleDiff(double a, double b) {
		double difference = b - a;
		while (difference < -180.0)
			difference += 360.0;
		while (difference > 180.0)
			difference -= 360.0;
		return difference;
	}

	// Getters and setters
	private double getY(double seconds, Long p1Time, Long p2Time, double p1y,
			double p2y) {
		double distanceInMeters = p2y - p1y;
		double timeDiff = getTimeDifference(p1Time, p2Time);
		double metersPerSec = distanceInMeters / timeDiff;
		return p1y + (metersPerSec * seconds);
	}

	private double getX(double seconds, Long p1Time, Long p2Time, double p1x,
			double p2x) {
		double distanceInMeters = p2x - p1x;
		double timeDiff = getTimeDifference(p1Time, p2Time);
		double metersPerSec = distanceInMeters / timeDiff;
		return p1x + (metersPerSec * seconds);
	}

	public int getBufferInSeconds() {
		return bufferInSeconds;
	}

	public void setBufferInSeconds(int bufferInSeconds) {
		this.bufferInSeconds = bufferInSeconds;
	}

	public int getDegreesPerMinute() {
		return degreesPerMinute;
	}

	public void setDegreesPerMinute(int degreesPerMinute) {
		this.degreesPerMinute = degreesPerMinute;
	}

	public boolean isIgnoreRotation() {
		return ignoreRotation;
	}

	public void setIgnoreRotation(boolean ignoreRotation) {
		this.ignoreRotation = ignoreRotation;
	}


}
