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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class Cell {
	
	
	private static final long serialVersionUID = 1L;
	private Map<Long, Ship> ships = new ConcurrentHashMap<Long, Ship>();
	private Long NOofReceivedSignals=0L; 
	private Long NOofMissingSignals=0L;
//	private int NOofReceivedSignals =0;
//	private int NOofMissingSignals =0;
	private double latitude;
	private double longitude;
	private String id;
	private Source grid;
	private int shipCount = 0;
	private List<TimeSpan> timeSpans;
	
	public List<TimeSpan> getTimeSpans() {
		return timeSpans;
	}
	public void setTimeSpans(List<TimeSpan> timeSpans) {
		this.timeSpans = timeSpans;
	}
	public Cell(Source grid, double lat, double lon, String id){
		this.latitude = lat;
		this.longitude = lon;
		this.grid = grid;
		this.id = id;
	}
	public Cell(double lat, double lon, String id)
	{
		this.latitude = lat;
		this.longitude = lon;
		this.id = id;
	}

	
//	public Cell(Source baseStation, double lat, double lon, String id) {
//	    this(lat, lon, id);
//        }
	
    public void incrementNOofReceivedSignals(){
		NOofReceivedSignals++;
	}
	public void incrementNOofMissingSignals(){
		NOofMissingSignals++;
	}
	public void incrementShipCount(){
		shipCount++;
	}
	
	public long getTotalNumberOfMessages(){
		return NOofReceivedSignals+NOofMissingSignals;
	}
	public double getCoverage(){
//		System.out.println(NOofReceivedSignals);
//		System.out.println(getTotalNumberOfMessages());
		return (double)NOofReceivedSignals/ (double)getTotalNumberOfMessages();
	}
	public Map<Long, Ship> getShips() {
		return ships;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Source getGrid() {
		return grid;
	}

	public void setGrid(Source grid) {
		this.grid = grid;
	}

	public int getShipCount() {
		return shipCount;
	}
	public Long getNOofReceivedSignals() {
		return NOofReceivedSignals;
	}

	public Long getNOofMissingSignals() {
		return NOofMissingSignals;
	}
	
//	public int getNOofReceivedSignals() {
//		return NOofReceivedSignals;
//	}
//
//	public int getNOofMissingSignals() {
//		return NOofMissingSignals;
//	}
	public void addReceivedSignals(long amount){
		this.NOofReceivedSignals += amount;
	}
	public void addNOofMissingSignals(long amount) {
		this.NOofMissingSignals+=amount;
	}
	
	public void setNoofMissingSignals(long amount){
		this.NOofMissingSignals=amount;
	}
}
