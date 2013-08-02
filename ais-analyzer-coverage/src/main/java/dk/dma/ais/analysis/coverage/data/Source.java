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
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.analysis.coverage.data.Ship.ShipClass;


public class Source implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap<String, Cell> grid = new ConcurrentHashMap<String, Cell>();
	private ConcurrentHashMap<Long, Ship> ships = new ConcurrentHashMap<Long, Ship>();
	private String name = "";
	private String identifier;
	private double latSize;
	private double lonSize;
	private double latitude;
	private double longitude;
	private long messageCount = 0;
	private boolean isVisible = true;
	private ReceiverType receiverType = ReceiverType.NOTDEFINED;

	
	public enum ReceiverType {
		BASESTATION, REGION, NOTDEFINED
	}
	
	public Source(String identifier, double latSize, double lonSize) {
		this.identifier = identifier;
		this.latSize = latSize;
		this.lonSize = lonSize;
	}
	public Source() {

	}
	
	/**
	 * latitude is rounded down
	 * longitude is rounded up.
	 * The id is lat-lon-coords representing top-left point in cell
	 */
	public String getCellId(double latitude, double longitude){
		//TODO move CellId convertion somewhere better
		double lat;
		double lon;
		if(latitude < 0){
			latitude +=latSize;
			lat = (double)((int)(10000*((latitude)- (latitude % latSize))))/10000;
			
		}else{
			lat = (double)((int)(10000*((latitude)- (latitude % latSize))))/10000;
		}
		
		if(longitude < 0){
			lon = (double)((int)(10000*(longitude - (longitude % lonSize))))/10000;
			
		}else{
			longitude -=lonSize;
			lon = (double)((int)(10000*(longitude - (longitude % lonSize))))/10000;
		}
		
		String cellId =  lat+"_"+lon;	
		return cellId;
	}

	public void incrementMessageCount(){
		messageCount++;
	}
	public ReceiverType getReceiverType() {
		return receiverType;
	}
	public void setReceiverType(ReceiverType receiverType) {
		this.receiverType = receiverType;
	}
	public boolean isVisible() {
		return isVisible;
	}
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	public Cell getCell(double latitude, double longitude){
		return grid.get(getCellId(latitude, longitude));
	}
	public Cell createCell(double latitude, double longitude){
		String id = getCellId(latitude, longitude);
		double lat = (double)((int)(10000*(latitude - (latitude % latSize))))/10000;
		double lon = (double)((int)(10000*(longitude - (longitude % lonSize))))/10000;
		Cell cell = new Cell(this, lat, lon, id);
		grid.put(cell.getId(), cell);		
		
		return cell;
	}
	
	/*
	 * Create ship
	 */
	public Ship createShip(Long mmsi, ShipClass shipClass){
		Ship ship = new Ship(mmsi, shipClass);
		ships.put(mmsi, ship);
		return ship;
	}
	public Ship getShip(Long mmsi){
		return ships.get(mmsi);
	}
	public ConcurrentHashMap<String, Cell> getGrid() {
		return grid;
	}
	public void setGrid(ConcurrentHashMap<String, Cell> grid) {
		this.grid = grid;
	}
	public ConcurrentHashMap<Long, Ship> getShips() {
		return ships;
	}
	public void setShips(ConcurrentHashMap<Long, Ship> ships) {
		this.ships = ships;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public long getMessageCount() {
		return messageCount;
	}
}
