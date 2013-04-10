package dk.dma.ais.analysis.coverage.calculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import dk.dma.ais.analysis.coverage.data.BaseStation;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.Ship;
import dk.dma.ais.analysis.coverage.data.json.JSonCoverageMap;
import dk.dma.ais.analysis.coverage.data.json.JsonCell;
import dk.dma.ais.analysis.coverage.data.json.JsonSource;



/**
 * This calculator maintains a buffer for each Ship instance.
 * 
 * If more than one base station receives messages from a single real world ship
 * a ship instance will be created and associated with each corresponding base
 * station. This is because ship instances holds a message buffer, and this
 * buffer can't be mixed up between base stations.
 * 
 * Rotation is determined based on difference between course over ground (cog)
 * from first and last message in buffer. If rotation is ignored, missing points
 * will only be calculated for ships that are NOT rotating.
 */
public class CoverageCalculator extends AbstractCalculator {

	private static final long serialVersionUID = 1L;
	private int bufferInSeconds = 20;
	private int degreesPerMinute = 20;
	private boolean ignoreRotation;
	private double highThreshold = .8;
	private double lowThreshold = .3;

//	public CoverageCalculator(AisCoverageProject project, boolean ignoreRotation) {
//		super(project);
//		this.ignoreRotation = ignoreRotation;
//	}
	public CoverageCalculator(boolean ignoreRotation) {
		super();
		this.ignoreRotation = ignoreRotation;
	}

	/**
	 * This is called whenever a message is received
	 */
	public void calculate(CustomMessage message) {
		
		Ship ship = dataHandler.getShip(message.getSourceMMSI(), message.getShipMMSI());
//		System.out.println(dataHandler.getSources().size());
//		System.out.println(message.getSourceMMSI() +" "+ message.getShipMMSI());

		// put message in ships' buffer
		ship.addToBuffer(message);
				
		// If this message is filtered, we empty the ships' buffer and returns
		if (filterMessage(message)) {
			ship.emptyBuffer();
			return;
		}
		

		// Time difference between first and last message in buffer
		CustomMessage firstMessage = ship.getFirstMessageInBuffer();
		CustomMessage lastMessage = ship.getLastMessageInBuffer();
		
		if(ship.getMessages().size() == 1)
			return;
		
		double timeDifference = this.getTimeDifference(firstMessage, lastMessage);
//		System.out.println(ship.getMessages().size());
		// Check if it is time to process the buffer
		if (timeDifference >= bufferInSeconds) {

			// process messages if there is no more then 5 minutes between the
			// messages in message buffer
			if (timeDifference < 300) {
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
		
		BaseStation source = dataHandler.getSource(m1.getSourceMMSI());
		Ship ship = dataHandler.getShip(m1.getSourceMMSI(), m1.getShipMMSI());
		
		// Get cell from first message and increment message count
		Cell cell = dataHandler.getCell(source.getIdentifier(), m1.getLatitude(), m1.getLongitude());
		if (cell == null) {
			cell = dataHandler.createCell(source.getIdentifier(), m1.getLatitude(), m1.getLongitude());
		}
		
		dataHandler.getSource(source.getIdentifier()).incrementMessageCount();
		cell.incrementNOofReceivedSignals();
		dataHandler.updateCell(cell);

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

	/**
	 * @return A combined coverage of cells from selected base stations. If two
	 *         base stations cover same area, the best coverage is chosen.
	 * 
	 *         Consider optimizing?
	 */
	public Collection<Cell> getCoverage() {
		HashMap<String, Cell> cells = new HashMap<String, Cell>();

		List<Cell> celllist = dataHandler.getCells();

		for (Cell cell : celllist) {
			Cell existing = cells.get(cell.getId());
			if (existing == null)
				cells.put(cell.getId(), cell);
			else if (cell.getCoverage() > existing.getCoverage())
				cells.put(cell.getId(), cell);
		}
		return cells.values();
	}

	/**
	 * @return A combined coverage of cells from selected base stations. If two
	 *         base stations cover same area, the best coverage is chosen.
	 * 
	 *         Consider optimizing?
	 */
	public Map<String, Cell> getCoverageMap() {
		HashMap<String, Cell> cells = new HashMap<String, Cell>();

		List<Cell> celllist = dataHandler.getCells();

		for (Cell cell : celllist) {
			Cell existing = cells.get(cell.getId());
			if (existing == null)
				cells.put(cell.getId(), cell);
			else if (cell.getCoverage() > existing.getCoverage())
				cells.put(cell.getId(), cell);
		}
		return cells;
	}

	public JSonCoverageMap getJsonCoverage() {
		JSonCoverageMap map = new JSonCoverageMap();
		map.latSize=dataHandler.getLatSize();
		map.lonSize=dataHandler.getLonSize();
		
		HashMap<String, JsonCell> JsonCells = new HashMap<String, JsonCell>();

		List<Cell> celllist = dataHandler.getCells();
		map.latSize = celllist.get(0).getGrid().getLatSize();
		
		for (Cell cell : celllist) {
			JsonCell existing = JsonCells.get(cell.getId());
			if (existing == null)
				JsonCells.put(cell.getId(), toJsonCell(cell));
			else if (cell.getCoverage() > existing.getCoverage())
				JsonCells.put(cell.getId(), toJsonCell(cell));
		}

		map.cells = JsonCells;
		return map;
	}
	public JSonCoverageMap getJsonCoverage(double latStart, double lonStart, double latEnd, double lonEnd, Map<String, Boolean> sources, int multiplicationFactor) {
		JSonCoverageMap map = new JSonCoverageMap();
		map.latSize=dataHandler.getLatSize()*multiplicationFactor;
		map.lonSize=dataHandler.getLonSize()*multiplicationFactor;
		

		HashMap<String, JsonCell> JsonCells = new HashMap<String, JsonCell>();

		List<Cell> celllist = dataHandler.getCells( latStart,  lonStart,  latEnd, lonEnd, sources, multiplicationFactor);
		if(!celllist.isEmpty())
			map.latSize = celllist.get(0).getGrid().getLatSize();
		
		for (Cell cell : celllist) {
			JsonCell existing = JsonCells.get(cell.getId());
			if (existing == null)
				JsonCells.put(cell.getId(), toJsonCell(cell));
			else if (cell.getCoverage() > existing.getCoverage()){
				JsonCells.put(cell.getId(), toJsonCell(cell));
			}
		}

		map.cells = JsonCells;
		return map;
	}

	public JsonCell toJsonCell(Cell cell) {
		JsonCell Jcell = new JsonCell();
		Jcell.lat = cell.getLatitude();
		Jcell.lon = cell.getLongitude();
		Jcell.nrOfMisMes = cell.getNOofMissingSignals();
		Jcell.nrOfRecMes = cell.getNOofReceivedSignals();

		return Jcell;
	}
	
	public Map<String,JsonSource> getJsonSources(){
		Map<String,JsonSource> sources = new HashMap<String,JsonSource>();
		Collection<BaseStation> sor = dataHandler.getSources();
		for (BaseStation baseStation : sor) {
			JsonSource s = new JsonSource();
			s.mmsi=baseStation.getIdentifier();
			
			s.type=baseStation.getReceiverType().name();
			
			if(baseStation.getLatitude() != null)
				s.lat=baseStation.getLatitude();
			
			if(baseStation.getLongitude() != null)
				s.lon=baseStation.getLongitude();
			
			sources.put(s.mmsi, s);
		}
		return sources;
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

	public double getHighThreshold() {
		return highThreshold;
	}

	public void setHighThreshold(double highThreshold) {
		this.highThreshold = highThreshold;
	}

	public double getLowThreshold() {
		return lowThreshold;
	}

	public void setLowThreshold(double lowTHreshold) {
		this.lowThreshold = lowTHreshold;
	}

}
