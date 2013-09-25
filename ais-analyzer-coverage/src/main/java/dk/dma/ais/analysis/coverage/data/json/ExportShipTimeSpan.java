package dk.dma.ais.analysis.coverage.data.json;

import java.util.ArrayList;
import java.util.List;

public class ExportShipTimeSpan {
	
	private List<LatLon> positions = new ArrayList<LatLon>();
	public ExportShipTimeSpan(long time) {
		firstMessage=time;
		lastMessage=time;
	}

	public List<LatLon> getPositions() {
		return positions;
	}

	public void setPositions(List<LatLon> positions) {
		this.positions = positions;
	}

	public long getFirstMessage() {
		return firstMessage;
	}

	public void setFirstMessage(long firstMessage) {
		this.firstMessage = firstMessage;
	}

	public long getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(long lastMessage) {
		this.lastMessage = lastMessage;
	}

	private long firstMessage, lastMessage;
	
	public class LatLon{
		private float lat, lon;

		public LatLon(float lat, float lon) {
			this.lat=lat;
			this.lon=lon;
		}

		public float getLat() {
			return lat;
		}

		public void setLat(float lat) {
			this.lat = lat;
		}

		public float getLon() {
			return lon;
		}

		public void setLon(float lon) {
			this.lon = lon;
		}
	}
	
}
