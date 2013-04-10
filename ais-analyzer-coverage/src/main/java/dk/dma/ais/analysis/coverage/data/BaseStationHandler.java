/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.analysis.coverage.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.analysis.coverage.calculator.AbstractCalculator;
import dk.dma.ais.analysis.coverage.event.AisEvent;


public class BaseStationHandler implements Serializable {

	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap<String, BaseStation> baseStations = new ConcurrentHashMap<String, BaseStation>();
//	private double latSize = -1;
//	private double lonSize = -1;
	private double latSize = 0.022522522522522525;
	private double lonSize = 0.03868125413876516;
	private AbstractCalculator calculator;
	
	/*
	 * Create grid associated to a specific transponder
	 */
	public BaseStation createGrid(String bsMmsi){
		BaseStation grid = new BaseStation(bsMmsi, latSize, lonSize);
		baseStations.put(bsMmsi, grid);
		
//		AisEvent event = new AisEvent();
//		event.setEvent(AisEvent.Event.BS_ADDED);
//		event.setSource(this);
//		event.setEventObject(grid);
//		ProjectHandler.getInstance().broadcastEvent(event);

		return grid;
	}
	
	
	public void setAllVisible(boolean b){
		Collection<BaseStation> basestations = baseStations.values();
		for (BaseStation baseStation : basestations) {	
			setVisible(baseStation.getIdentifier(), b);
		}
	}
	public void setVisible(String mmsi, boolean b){
		BaseStation baseStation = baseStations.get(mmsi);
		if(baseStation != null){
			baseStation.setVisible(b);
			
//			ProjectHandler.getInstance().broadcastEvent(new AisEvent(AisEvent.Event.BS_VISIBILITY_CHANGED, calculator, baseStation));
			
		}
	}
	
	/**
	 * latitude is rounded down
	 * longitude is rounded up.
	 * The id is lat-lon-coords representing top-left point in cell
	 */
//	public String getCellId(double latitude, double longitude){
//
//		double lat;
//		double lon;
//		if(latitude < 0){
//			latitude +=latSize;
//			lat = (double)((int)(10000*((latitude)- (latitude % latSize))))/10000;
//			
//		}else{
//			lat = (double)((int)(10000*((latitude)- (latitude % latSize))))/10000;
//		}
//		
//		if(longitude < 0){
//			lon = (double)((int)(10000*(longitude - (longitude % lonSize))))/10000;
//			
//		}else{
//			longitude -=lonSize;
//			lon = (double)((int)(10000*(longitude - (longitude % lonSize))))/10000;
//		}
//		
//		String cellId =  lat+"_"+lon;	
//		return cellId;
//	}
	
	
	public BaseStation getGrid(String bsMmsi){
		return baseStations.get(bsMmsi);
	}
	public Map<String, BaseStation> getBaseStations() {
		return baseStations;
	}
	public BaseStationHandler(AbstractCalculator calculator){
		this.calculator = calculator;
	}
	
	public BaseStationHandler()
	{
		
	}
	
	public double getLatSize() {
		return latSize;
	}

	public void setLatSize(double latSize) {
		this.latSize = latSize;
	}

	public double getLonSize() {
		return lonSize;
	}

	public void setLonSize(double lonSize) {
		this.lonSize = lonSize;
	}
	
}
