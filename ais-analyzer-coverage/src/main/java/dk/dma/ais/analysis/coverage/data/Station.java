package dk.dma.ais.analysis.coverage.data;

public class Station {

	private String name = "";
	private double latitude;
	private double longitude;
	
	public Station()
	{
		
	}
	public Station(String name, double lat, double lon){
		this.name=name;
		latitude = lat;
		longitude = lon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
}
