package dk.dma.ais.analysis.coverage.calculator.geotools;

import java.util.Calendar;
import java.util.Date;

public class Helper {
	
	public static double latSize = 0.022522522522522525;
	public static double lonSize = 0.03868125413876516;

	public static Date getFloorDate(Date d){
		Calendar cal = Calendar.getInstance();  
		cal.setTime(d);  
		  
		// Set time fields to zero  
		cal.set(Calendar.MINUTE, 0);  
		cal.set(Calendar.SECOND, 0);  
		cal.set(Calendar.MILLISECOND, 0);  
		  
		return cal.getTime(); 
	}
	public static Date getCeilDate(Date d){
		Calendar cal = Calendar.getInstance();  
		cal.setTime(new Date(d.getTime()+1000*60*60));  
		  
		// Set time fields to zero  
		cal.set(Calendar.MINUTE, 0);  
		cal.set(Calendar.SECOND, 0);  
		cal.set(Calendar.MILLISECOND, 0);  
		return cal.getTime();  

	}
	/**
	 * latitude is rounded down
	 * longitude is rounded up.
	 * The id is lat-lon-coords representing top-left point in cell
	 */
	public static String getCellId(double latitude, double longitude, int multiplicationFactor){
//		System.out.println("roundlat="+roundLat(latitude, multiplicationFactor));
//		System.out.println("roundLon="+roundLon(latitude, multiplicationFactor));
//		System.out.println();
		return roundLat(latitude, multiplicationFactor)+"_"+roundLon(longitude, multiplicationFactor);	
	}
	public static double roundLat(double latitude, int multiplicationFactor){
		double lat;
		if(latitude < 0){
			latitude +=latSize;
			lat = (double)((int)(10000*((latitude)- (latitude % (latSize*multiplicationFactor)))))/10000;
			
		}else{
			lat = (double)((int)(10000*((latitude)- (latitude % (latSize*multiplicationFactor)))))/10000;
		}
		return lat;
	}
	public static double roundLon(double longitude, int multiplicationFactor){
		double lon;
		if(longitude < 0){
			lon = (double)((int)(10000*(longitude - (longitude % (lonSize*multiplicationFactor)))))/10000;
			
		}else{
			longitude -=lonSize;
			lon = (double)((int)(10000*(longitude - (longitude % (lonSize*multiplicationFactor)))))/10000;
		}
		return lon;
	}
}
