package dk.dma.ais.analysis.coverage.calculator.geotools;

import dk.dma.enav.model.geometry.Position;


public class GeoConverter {
	public static double metersToLonDegree(double latitude, double meters){	
		
		//calculate length of 1 degree lon
		double latRad = Math.toRadians(latitude);
		double a = 6378137;
		double b = 6356752.3142;
		double ee = ( (a*a)-(b*b) )/(a*a);
		double oneDegreeLength = ( Math.PI * a * Math.cos(latRad) )/ ( 180 * Math.pow(1 - ee*( (Math.sin(latRad) * Math.sin(latRad) )), 0.5) );
		double lonDegree = (1/oneDegreeLength)*meters;
		
		return lonDegree;
	}
	public static double metersToLatDegree(double meters){
		return ((double)1/111000)*meters;
	}
//	public static double latToMeters(double p1Lat, double p2Lat, double lon){
//		Position p1 = new Position(p1Lat, lon);
//
//		Position p2 = new Position(p2Lat,lon);
//
//		double distanceInMeters = p1.getRhumbLineDistance(p2);
//		return distanceInMeters;
//	}
//	public static double lonToMeters(double p1Lon, double p2Lon, double lat){
//		GeoLocation p1 = new GeoLocation();
//		p1.setLongitude(p1Lon);
//		p1.setLatitude(lat);
//		GeoLocation p2 = new GeoLocation();
//		p2.setLongitude(p2Lon);
//		p2.setLatitude(lat);
//		double distanceInMeters = p1.getRhumbLineDistance(p2);
//		return distanceInMeters;
//	}
	
}
