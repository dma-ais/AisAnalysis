package dk.dma.ais.analysis.coverage.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarshallCell {

	private Long NOofReceivedSignals=0L; 
	private Long NOofMissingSignals=0L;
	private double latitude;
	private double longitude;
//	private String id;
//	private String sourceID;
	
//	public String getSourceID() {
//		return sourceID;
//	}
//	public void setSourceID(String sourceID) {
//		this.sourceID = sourceID;
//	}
	public Long getNOofReceivedSignals() {
		return NOofReceivedSignals;
	}
	public void setNOofReceivedSignals(Long nOofReceivedSignals) {
		NOofReceivedSignals = nOofReceivedSignals;
	}
	public Long getNOofMissingSignals() {
		return NOofMissingSignals;
	}
	public void setNOofMissingSignals(Long nOofMissingSignals) {
		NOofMissingSignals = nOofMissingSignals;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
//	public String getId() {
//		return id;
//	}
//	public void setId(String id) {
//		this.id = id;
//	}
}
