package dk.dma.ais.analysis.coverage.data;

import java.io.Serializable;
import java.util.Date;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacketTags.SourceType;


public class CustomMessage implements Serializable {
	private AisMessage originalMessage;
	
	private static final long serialVersionUID = 1L;
	private double cog;
	private double sog;
	private double latitude;
	private double longitude;
	private Date timestamp = null;
	private String sourceMMSI;
	private long shipMMSI;
	private long timeSinceLastMsg;
	private String key;
	private SourceType sourceType;
	
	public SourceType getSourceType() {
		return sourceType;
	}
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public double getCog() {
		return cog;
	}
	public void setCog(double cog) {
		this.cog = cog;
	}
	public double getSog() {
		return sog;
	}
	public void setSog(double sog) {
		this.sog = sog;
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
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getSourceMMSI() {
		return sourceMMSI;
	}
	public long getShipMMSI() {
		return shipMMSI;
	}
	public void setShipMMSI(long shipMMSI) {
		this.shipMMSI = shipMMSI;
	}
	public void setSourceMMSI(String sourceMMSI) {
		this.sourceMMSI = sourceMMSI;
	}
	public long getTimeSinceLastMsg() {
		return timeSinceLastMsg;
	}
	public void setTimeSinceLastMsg(long timeSinceLastMsg) {
		this.timeSinceLastMsg = timeSinceLastMsg;
	}
	public AisMessage getOriginalMessage() {
		return originalMessage;
	}
	public void setOriginalMessage(AisMessage originalMessage) {
		this.originalMessage = originalMessage;
	}

}
