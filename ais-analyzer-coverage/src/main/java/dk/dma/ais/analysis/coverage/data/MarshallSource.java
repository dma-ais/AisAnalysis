package dk.dma.ais.analysis.coverage.data;

import java.util.HashMap;
//import java.util.concurrent.ConcurrentHashMap;


public class MarshallSource {
	
//	private String name = "";
	private String identifier;
//	private double latitude;
//	private double longitude;
//	private long messageCount = 0;
//	private ReceiverType receiverType = ReceiverType.NOTDEFINED;
	private HashMap<String, MarshallCell> grid = new HashMap<String, MarshallCell>();
	
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

//	public double getLatitude() {
//		return latitude;
//	}
//
//	public void setLatitude(double latitude) {
//		this.latitude = latitude;
//	}
//
//	public double getLongitude() {
//		return longitude;
//	}
//
//	public void setLongitude(double longitude) {
//		this.longitude = longitude;
//	}

//	public long getMessageCount() {
//		return messageCount;
//	}
//
//	public void setMessageCount(long messageCount) {
//		this.messageCount = messageCount;
//	}

//	public ReceiverType getReceiverType() {
//		return receiverType;
//	}
//
//	public void setReceiverType(ReceiverType receiverType) {
//		this.receiverType = receiverType;
//	}

	public HashMap<String, MarshallCell> getGrid() {
		return grid;
	}

	public void setGrid(HashMap<String, MarshallCell> grid) {
		this.grid = grid;
	}

	public enum ReceiverType {
		BASESTATION, REGION, NOTDEFINED
	}
	

}
