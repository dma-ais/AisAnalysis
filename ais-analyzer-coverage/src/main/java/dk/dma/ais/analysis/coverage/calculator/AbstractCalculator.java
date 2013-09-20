package dk.dma.ais.analysis.coverage.calculator;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import dk.dma.ais.analysis.coverage.AisCoverageGUI;
import dk.dma.ais.analysis.coverage.calculator.geotools.GeoConverter;
import dk.dma.ais.analysis.coverage.calculator.geotools.Helper;
import dk.dma.ais.analysis.coverage.calculator.geotools.SphereProjection;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Source.ReceiverType;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.ICoverageData;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.analysis.coverage.data.Ship;
import dk.dma.ais.analysis.coverage.data.Ship.ShipClass;
import dk.dma.ais.analysis.coverage.data.Station;
import dk.dma.ais.analysis.coverage.event.AisEvent;
import dk.dma.ais.analysis.coverage.event.EventBroadcaster;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage1;
import dk.dma.ais.message.AisMessage4;
import dk.dma.ais.message.AisMessage5;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.message.ShipTypeCargo;
import dk.dma.ais.message.ShipTypeCargo.ShipType;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.proprietary.IProprietarySourceTag;
import dk.dma.enav.model.geometry.Position;


/**
 * See CoverageCalculator and DensityPlotCalculator for examples of how to extend this class.
 * When a calculator is added to an AisCoverageProject instance, the calculator automatically receives
 * CustomMessages via calculate().
 * 
 */
public abstract class AbstractCalculator implements Serializable {

	private static final long serialVersionUID = 1L;
	transient protected SphereProjection projection = new SphereProjection();
	private int cellSize = 2500;
//	protected AisCoverageProject project;
	protected Map<ShipClass, ShipClass> allowedShipClasses = new ConcurrentHashMap<ShipClass, ShipClass>();
	protected Map<ShipType, ShipType> allowedShipTypes = new ConcurrentHashMap<ShipType, ShipType>();
	protected Map<Integer, Boolean> allowedShips = new ConcurrentHashMap<Integer, Boolean>();
	protected CustomMessage firstMessage = null;
	protected CustomMessage currentMessage = null;	
	protected ICoverageData dataHandler = new OnlyMemoryData();
	protected double filterTimeDifference;
	protected int maxDistanceBetweenFirstAndLast = 2000;
	public HashMap<String, Station> sourcenames;
	protected int minAllowedSpeed = 3;
	protected int maxAllowedSpeed = 50;
	private static final Logger LOG = LoggerFactory.getLogger(AisCoverageGUI.class);
	
	
	
	
	public double getFilterTimeDifference() {
		return filterTimeDifference;
	}

	public void setFilterTimeDifference(double filterTimeDifference) {
		this.filterTimeDifference = filterTimeDifference;
	}

	public int getMaxDistanceBetweenFirstAndLast() {
		return maxDistanceBetweenFirstAndLast;
	}

	public void setMaxDistanceBetweenFirstAndLast(int maxDistanceBetweenFirstAndLast) {
		this.maxDistanceBetweenFirstAndLast = maxDistanceBetweenFirstAndLast;
	}

	public int getMinAllowedSpeed() {
		return minAllowedSpeed;
	}

	public void setMinAllowedSpeed(int minAllowedSpeed) {
		this.minAllowedSpeed = minAllowedSpeed;
	}

	public int getMaxAllowedSpeed() {
		return maxAllowedSpeed;
	}

	public void setMaxAllowedSpeed(int maxAllowedSpeed) {
		this.maxAllowedSpeed = maxAllowedSpeed;
	}

	abstract public void calculate(CustomMessage m);
	
	/**
	 * This is called by message handlers whenever a new message is received.
	 */
	public void processMessage(AisPacket packet, String defaultID) {
		
		AisMessage message = packet.tryGetAisMessage();
        if (message == null) {	return;	}
        
		
		CustomMessage newMessage = aisToCustom(message, defaultID);
		if(newMessage != null){
			newMessage.setSourceType(packet.getTags().getSourceType());
			calculate(newMessage);
		}
	}
	
	/**
	 * Determines the expected transmitting frequency, based on speed over ground(sog),
	 * whether the ship is rotating and ship class.
	 * This can be used to calculate coverage.
	 */
	public double getExpectedTransmittingFrequency(double sog, boolean rotating, ShipClass shipClass){
		double expectedTransmittingFrequency;
		if(shipClass == ShipClass.CLASS_A){		
			if(rotating){
				if(sog < .2)
					expectedTransmittingFrequency = 180;
				else if(sog < 14)
					expectedTransmittingFrequency = 3.33;
				else if(sog < 23)
					expectedTransmittingFrequency = 2;
				else 
					expectedTransmittingFrequency = 2;
			}else{
				if(sog < .2)
					expectedTransmittingFrequency = 180;
				else if(sog < 14)
					expectedTransmittingFrequency = 10;
				else if(sog < 23)
					expectedTransmittingFrequency = 6;
				else 
					expectedTransmittingFrequency = 2;
			}
		}
		else{
			if(sog <= 2)
				expectedTransmittingFrequency = 180;
			else
				expectedTransmittingFrequency = 30;
		}
		
		return expectedTransmittingFrequency;
		
	}
	
	/*
	 * Use this method to filter out unwanted messages. 
	 * The filtering is based on rules of thumbs. For instance, if a distance 
	 * between two messages is over 2000m, we filter
	 */
	public boolean filterMessage(CustomMessage customMessage){
		
		if(customMessage.getSog() < 3 || customMessage.getSog() > 50)
			return true;
		if(customMessage.getCog() == 360){
			return true;
		}

		Ship ship = dataHandler.getShip(customMessage.getSourceMMSI(), customMessage.getShipMMSI());
		
		CustomMessage firstMessage = ship.getFirstMessageInBuffer();
		CustomMessage lastMessage = ship.getLastMessageInBuffer();
		if(lastMessage != null){
			
			// Filter message based on distance between first and last message
			projection.setCentralPoint(firstMessage.getLongitude(), firstMessage.getLatitude());
			double distance = projection.distBetweenPoints(firstMessage.getLongitude(), firstMessage.getLatitude(), lastMessage.getLongitude(), lastMessage.getLatitude());
			if(distance > 2000){
				return true;
			}
			
			// Filter message based on time between first and last message
			double timeDifference = this.getTimeDifference(firstMessage, lastMessage);
			if( timeDifference > 1200 ){
				return true;
			}
			
		}
		return false;
	}

	protected void extractBaseStationPosition(AisMessage4 m){
		Source b = dataHandler.getSource(m.getUserId()+"");

		if (b != null) {
			Position pos = m.getPos().getGeoLocation();
			if(pos != null){
				b.setLatitude( m.getPos().getGeoLocation().getLatitude() );
				b.setLongitude( m.getPos().getGeoLocation().getLongitude() );

				EventBroadcaster.getInstance().broadcastEvent(new AisEvent(AisEvent.Event.BS_POSITION_FOUND, this, b));
			}		
		}
	}
	protected boolean isShipAllowed(AisMessage aisMessage){
		if(allowedShipTypes.size() > 0){
			
			// Ship type message
			if(aisMessage instanceof AisMessage5){
				
				//if ship type is allowed, we add ship mmsi to allowedShips map
				AisMessage5 m = (AisMessage5) aisMessage;
				ShipTypeCargo shipTypeCargo = new ShipTypeCargo(m.getShipType());
				if(allowedShipTypes.containsKey(shipTypeCargo.getShipType())){
					allowedShips.put(m.getUserId(), true);
				}
				// It's not a position message, so we return false
				return false;
			}	
			
			// if ship isn't in allowedShips we don't process the message
			if(!allowedShips.containsKey(aisMessage.getUserId()) ){
				return false;
			}
		}
		return true;
	}
	protected ShipClass extractShipClass(AisMessage aisMessage){
		if (aisMessage.getMsgId() == 18) {
			// class B
			return Ship.ShipClass.CLASS_B;
		} else {
			// class A
			return Ship.ShipClass.CLASS_A;
		}
	}
	
	/**
	 * Calculates lat/lon sizes based on a meter scale and a lat/lon position
	 */
	protected void calculateLatLonSize(double latitude){
		if(Helper.latSize == -1){
			
			double cellInMeters= getCellSize(); //cell size in meters
			dataHandler.setLatSize(GeoConverter.metersToLatDegree(cellInMeters));
			dataHandler.setLonSize(GeoConverter.metersToLonDegree(latitude, cellInMeters));
			LOG.info("lat size initiated with: "+Helper.latSize);
			LOG.info("lon size initiated with: "+Helper.lonSize);
		}
	}
	
	/**
	 * Check if grid exists (If a message with that bsmmsi has been received before)
	 * Otherwise create a grid for corresponding base station.
	 */
	protected Source extractBaseStation(String baseId, ReceiverType receiverType){
		Source grid = dataHandler.getSource(baseId);
		if (grid == null) {
			grid = dataHandler.createSource(baseId);
			grid.setReceiverType(receiverType);
		}
		return grid;
	}
	
	/**  Check which ship sent the message.
	 *	If it's the first message from that ship, create ship and put it in
	 *	base statino that received message
	 */
	protected Ship extractShip(long mmsi, ShipClass shipClass, Source baseStation){
		Ship ship = dataHandler.getShip(baseStation.getIdentifier(), mmsi);
		if (ship == null) {
			ship = dataHandler.createShip(baseStation.getIdentifier(), mmsi, shipClass);
		}
		return ship;
	}
	
	/** The aisToCustom method is used to map AisMessages to CustomMessages. It also takes care of creating base station instances,
	 * ship instances and to set up references between these. Override it if you want to handle this in a different way.
	 */
	public CustomMessage aisToCustom(AisMessage aisMessage, String defaultID){
		
		
		
//		Stops analysis if project has been running longer than timeout
//		long timeSinceStart = project.getRunningTime();
//		if (project.getTimeout() != -1 && timeSinceStart > project.getTimeout())
//			project.stopAnalysis();

		String baseId = null;
		String name = "";
		ReceiverType receiverType = ReceiverType.NOTDEFINED;
//		IGeneralPositionMessage posMessage = null;
//		GeoLocation pos = null;
		Date timestamp = null;
		ShipClass shipClass = null;
		AisPositionMessage posMessage;

		
		// Get source tag properties
		IProprietarySourceTag sourceTag = aisMessage.getSourceTag();
		
		if (sourceTag != null) {
			Integer bsmmsi = sourceTag.getBaseMmsi();
			timestamp = sourceTag.getTimestamp();
//			srcCountry = sourceTag.getCountry();
			String region = sourceTag.getRegion();
			
			if(defaultID != "sat"){
				if(bsmmsi == null){
					if(!region.equals("")){
						if (sourcenames.containsKey(region)) {
							name = sourcenames.get(region).getName(); 
							Source b = dataHandler.getSource(region);

							if (b != null) {
									b.setLatitude( sourcenames.get(region).getLatitude() );
									b.setLongitude( sourcenames.get(region).getLongitude() );
									EventBroadcaster.getInstance().broadcastEvent(new AisEvent(AisEvent.Event.BS_POSITION_FOUND, this, b));	
							}
						}
							baseId = region;
						receiverType = ReceiverType.REGION;
					}
				}
				else{
					if (sourcenames.containsKey(bsmmsi.toString())) {
						name = sourcenames.get(bsmmsi.toString()).getName();
						Source b = dataHandler.getSource(bsmmsi+"");

						if (b != null) {
								b.setLatitude( sourcenames.get(bsmmsi.toString()).getLatitude() );
								b.setLongitude( sourcenames.get(bsmmsi.toString()).getLongitude() );
								EventBroadcaster.getInstance().broadcastEvent(new AisEvent(AisEvent.Event.BS_POSITION_FOUND, this, b));	
						}
					}
						baseId = bsmmsi+"";
					receiverType = ReceiverType.BASESTATION;
				}
			}
			
		}
		

		//Checks if its neither a basestation nor a region
		if (baseId == null){	baseId = defaultID;	}
		
		// If time stamp is not present, we add one
		if(timestamp == null){	timestamp = new Date();	}
		
		// It's a base station positiion message
		if (aisMessage instanceof AisMessage4) {			
			extractBaseStationPosition((AisMessage4) aisMessage);	
			return null;
		}
		
		
		// if no allowed ship types has been set, we process all ship types
		if(!isShipAllowed(aisMessage))
			return null;

		
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
		Position pos = posMessage.getPos().getGeoLocation();
		
		//calculate lat lon size based on first message
//		if(firstMessage == null){
//			calculateLatLonSize(pos.getLatitude());
//		}
		
//
		
		// Extract Base station
		Source baseStation = extractBaseStation(baseId, receiverType);
		baseStation.setName(name);
//
//		// Extract ship
		Ship ship = extractShip(aisMessage.getUserId(), shipClass, baseStation);

		CustomMessage newMessage = new CustomMessage();
		newMessage.setCog( (double) posMessage.getCog() / 10 );
		newMessage.setSog( (double) posMessage.getSog() / 10 );
		newMessage.setLatitude( posMessage.getPos().getGeoLocation().getLatitude() );
		newMessage.setLongitude( posMessage.getPos().getGeoLocation().getLongitude() );
		newMessage.setTimestamp( timestamp );
		newMessage.setSourceMMSI(baseStation.getIdentifier());
		newMessage.setShipMMSI( ship.getMmsi() );
//		newMessage.setOriginalMessage(aisMessage);
		newMessage.setKey(messageToKey(newMessage));

		// Keep track of current message
		currentMessage = newMessage;
		
		// Keep track of first message
		if(firstMessage == null){
			firstMessage = newMessage;
		}
//		return null;
		return newMessage;
	}
	
	protected String messageToKey(CustomMessage m){
		return m.getCog()+""+m.getLatitude()+""+m.getLongitude()+""+m.getShipMMSI()+""+m.getSog();

//		return m.getCog()+""+m.getLatitude()+""+m.getLongitude()+""+m.getShipMMSI()+""+m.getSog()+""+m.getSourceMMSI();
//		return m.getShipMMSI()+""+m.getTimestamp().getTime();
//		return m.getShipMMSI()+""+m.getLatitude()+""+m.getLongitude();
	}
	
	//getters and setters
	public ICoverageData getDataHandler(){
		return dataHandler;
	}
	public void setDataHandler(ICoverageData dataHandler) {
		this.dataHandler = dataHandler;
	}
	public String[] getBaseStationNames(){
		return dataHandler.getSourceNames();
	}
	public int getCellSize() {
		return cellSize;
	}
	public void setCellSize(int cellSize) {
		this.cellSize = cellSize;
	}
	
	/**
	 * Time difference between two messages in seconds
	 */
	public double getTimeDifference(CustomMessage m1, CustomMessage m2){
		return (double) Math.abs(((m2.getTimestamp().getTime() - m1.getTimestamp().getTime())) / 1000);
	}
	public double getTimeDifference(Long m1, Long m2){
		return  ((double)Math.abs((m2 - m1)) / 1000);
	}
	public Map<ShipClass, ShipClass> getAllowedShipClasses() {
		return allowedShipClasses;
	}
	public void setAllowedShipClasses(Map<ShipClass, ShipClass> allowedShipClasses) {
		this.allowedShipClasses = allowedShipClasses;
	}
	public Map<ShipType, ShipType> getAllowedShipTypes() {
		return allowedShipTypes;
	}
	public void setAllowedShipTypes(Map<ShipType, ShipType> allowedShipTypes) {
		this.allowedShipTypes = allowedShipTypes;
	}
	public AbstractCalculator(){
//		this.project = project;
	}
	public AbstractCalculator(HashMap<String, Station> sourcenamemap){
//		this.project = project;
		sourcenames = sourcenamemap;
	}
	public CustomMessage getFirstMessage() {
		return firstMessage;
	}
	public CustomMessage getCurrentMessage() {
		return currentMessage;
	}

}
