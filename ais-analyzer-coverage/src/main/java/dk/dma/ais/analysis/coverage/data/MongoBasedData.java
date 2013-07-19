package dk.dma.ais.analysis.coverage.data;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import dk.dma.ais.analysis.coverage.configuration.DatabaseConfiguration;
import dk.dma.ais.analysis.coverage.data.Ship.ShipClass;


public class MongoBasedData implements ICoverageData{
	public SourceHandler gridHandler = new SourceHandler(null);
	public Mongo mongo;
	public DB db;
	private String dbname = "cells";
	DBCollection latLonCollection;

	public MongoBasedData(DatabaseConfiguration dbConf)
	{
		try {
			//mongo connection
			//TODO set up for permanent central mongoDB?
			mongo = new Mongo(dbConf.getAddr(), dbConf.getPort());
			// get database from MongoDB,
			// if database doesn't exists, mongoDB will create it automatically
			db = mongo.getDB(dbConf.getDbName());
			latLonCollection = db.getCollection("latLon");
			
			System.out.println("db created");
		} catch (UnknownHostException e) {		e.printStackTrace();		}
	}
		
	
	//still run in memory
		@Override
		public Ship getShip(String sourceMmsi, long shipMmsi) {	return gridHandler.getGrid(sourceMmsi).getShip(shipMmsi);	}
		@Override
		public Ship createShip(String sourceMmsi, long shipMmsi, ShipClass shipClass) {	return gridHandler.getGrid(sourceMmsi).createShip(shipMmsi, shipClass);	}
		@Override
		public Source getSource(String sourceId) {	return gridHandler.getBaseStations().get(sourceId);	}
		@Override
		public Source createSource(String sourceId) {	return gridHandler.createGrid(sourceId);	}
		@Override
		public Collection<Source> getSources() {	return gridHandler.getBaseStations().values();	}
		
		
		
		@Override
		public void setLatSize(double latsize) 
		{	
			gridHandler.setLatSize(latsize);
			
			// create a DBObject to store calues
			BasicDBObject latDBObject = new BasicDBObject();
			latDBObject.put("_id", "lat");
			latDBObject.put("latSize", latsize);
		
			// save it into collection
			latLonCollection.insert(latDBObject);
			//
		}
		@Override
		public void setLonSize(double lonsize) 
		{	
			gridHandler.setLonSize(lonsize);	
		
			// create a DBObject to store calues
			BasicDBObject lonDBObject = new BasicDBObject();
			lonDBObject.put("_id", "lon");
			lonDBObject.put("lonSize", lonsize);
					
			// save it into collection
			latLonCollection.insert(lonDBObject);
		}
		@Override
		public double getLatSize() 
		{			
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("_id", "lat");
					 
			DBObject dbo = (DBObject) latLonCollection.findOne(searchQuery);
			
			if(dbo == null)
			{
				return -1;
			}
			else
			{
				//only getlatsize is called to determin if it exist already (from abstractcaculator)
				//this call ensures that lonsize is loaded as well
				double lonsize = getLonSize();
				
				 
				double latsize = (double) dbo.get("latSize");
				System.out.println("latsize =" + latsize);
				gridHandler.setLatSize(latsize);
				return latsize;
			}
		}
		@Override
		public double getLonSize() 
		{	
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("_id", "lon");
					 
			DBObject dbo = (DBObject) latLonCollection.findOne(searchQuery);
			
			if(dbo == null)
			{
				return -1;
			}
			else
			{
				double lonsize = (double) dbo.get("lonSize");
				System.out.println("lonsize = "+ lonsize);
				gridHandler.setLonSize(lonsize);
				return lonsize;
			}
		}
		
		
		//TODO find use?
		@Override
		public void updateShip(Ship ship) {		}
		@Override
		public List<Cell> getCells() {	return null;	}
		@Override
		public List<Cell> getCells(Map<String, Boolean> sources) {	return null;	}

		@Override
		public Cell getCell(String sourceMmsi, double lat, double lon) {

			// Get collection from MongoDB, database named "yourDB"
			// if collection doesn't exists, mongoDB will create it automatically
			DBCollection collection = db.getCollection(sourceMmsi);	

			// search query
			BasicDBObject searchQuery = new BasicDBObject();
//			String cellID =	(gridHandler.getGrid(sourceMmsi).getCellId(lat, lon));
			searchQuery.put("_id", gridHandler.getGrid(sourceMmsi).getCellId(lat, lon));
					 
			DBObject dbo = (DBObject) collection.findOne(searchQuery);
			
			if(dbo == null)
			{
				System.out.println("dbo var null + cellsize = " + gridHandler.getLatSize() + "-" + gridHandler.getLonSize());
				return null;
				
			}
			else{
			Cell cell = new Cell(gridHandler.getGrid(sourceMmsi), (double) dbo.get("lat"), (double) dbo.get("lon"), (String) dbo.get("_id"));
			
			cell.addNOofMissingSignals(Long.parseLong(dbo.get("NOofMissingSignals").toString()));
			cell.addReceivedSignals(Long.parseLong(dbo.get("NOofReceivedSignals").toString()));

			return cell;
			}
		}

		@Override
		public List<Cell> getCells(double latStart, double lonStart, double latEnd,
				double lonEnd, Map<String, Boolean> sources, int multiplicationFactor) {
			
//			System.out.println("ceeeels");
//			System.out.println(latStart + " " +lonStart);
			List<Cell> cells = new ArrayList<Cell>();
			Collection<Source> basestations = gridHandler.getBaseStations().values();
			for (Source basestation : basestations) {
				if ( sources.containsKey(basestation.getIdentifier()) ) {	
//					System.out.println(basestation.getIdentifier());
					
					//find collection
					DBCollection collection = db.getCollection(basestation.getIdentifier());	
					
					Source tempSource = new Source(basestation.getIdentifier(), gridHandler.getLatSize()*multiplicationFactor, gridHandler.getLonSize()*multiplicationFactor);
					// For each cell
					Long starttime = System.currentTimeMillis();
					System.out.println("started get ranged cells " + starttime);
					Collection<Cell> bscells = new ArrayList<Cell>();
					DBCursor cursor = collection.find();				
				 
					// loop over the cursor and display the retrieved result
					while (cursor.hasNext()) {
						DBObject dbo = cursor.next();
						//extract cell
						Cell cell = new Cell(new Source(), (double) dbo.get("lat"),(double) dbo.get("lon"),(String) dbo.get("id"));
						String value = dbo.get("NOofReceivedSignals").toString();
						Long NOofReceivedSignals = Long.parseLong(value);
						String value2 = dbo.get("NOofMissingSignals").toString();
//						System.out.println("received: " + value + "missing: " +value2);
						Long NOofMissingSignals = Long.parseLong(value2);
//						cell.addReceivedSignals(Long.valueOf(dbo.get("NOofReceivedSignals").toString()));
						cell.addNOofMissingSignals(NOofMissingSignals);
						cell.addReceivedSignals(NOofReceivedSignals);
						
						//add cell to list
						bscells.add(cell);
					}
					Long time = (System.currentTimeMillis() - starttime);
					System.out.println("finished loading ranged cells: " +time);
					
					for (Cell cell : bscells) {
						Cell tempCell = tempSource.getCell(cell.getLatitude(), cell.getLongitude());
						if(tempCell == null){
							tempCell = tempSource.createCell(cell.getLatitude(), cell.getLongitude());
						}
						tempCell.addNOofMissingSignals(cell.getNOofMissingSignals());
						tempCell.addReceivedSignals(cell.getNOofReceivedSignals());
					}
					
					// For each cell
					Collection<Cell> tempCells = tempSource.getGrid().values();
					for (Cell cell : tempCells) {
						if(cell.getLatitude() <= latStart && cell.getLatitude() >= latEnd ){
							if(cell.getLongitude() >= lonStart && cell.getLongitude() <= lonEnd ){
								cells.add(cell);
							}
						}
					}
				}
			}
			System.out.println("mm: " + cells.size());
			return cells;
		}
		
		@Override
		public Cell createCell(String sourceMmsi, double lat, double lon) {
			DBCollection collection = db.getCollection(sourceMmsi);	
			
			// create a DBObject to store calues
			BasicDBObject cellDBObject = new BasicDBObject();
			String cellID =	gridHandler.getGrid(sourceMmsi).getCellId(lat, lon);
			cellDBObject.put("_id", cellID);
			cellDBObject.put("lat", lat);
			cellDBObject.put("lon", lon);
			cellDBObject.put("NOofReceivedSignals", 0);
			cellDBObject.put("NOofMissingSignals", 0);
			//currently not used
			cellDBObject.put("shipCount", 0);			
			// save it into collection
			collection.insert(cellDBObject);
			
			//convert to cell for return
			return new Cell(gridHandler.getGrid(sourceMmsi), lat, lon, gridHandler.getGrid(sourceMmsi).getCellId(lat, lon));
		}

		@Override
		public String[] getSourceNames() {
			Set<String> set = gridHandler.getBaseStations().keySet();
			String[] bssmsis = new String[set.size()];
			int i = 0;
			for (String s : set) {
				bssmsis[i] = s;
				i++;
			}		
			return bssmsis;

		}
		
		@Override
		public void updateCell(Cell c) {
			
			String sourceMmsi = c.getGrid().getIdentifier();
			DBCollection collection = db.getCollection(sourceMmsi);	
			
			BasicDBObject cellDBObject = new BasicDBObject();
			cellDBObject.put("_id", c.getId());
			cellDBObject.put("lat", c.getLatitude());
			cellDBObject.put("lon", c.getLongitude());
			cellDBObject.put("NOofReceivedSignals", c.getNOofReceivedSignals());
			cellDBObject.put("NOofMissingSignals", c.getNOofMissingSignals());
			cellDBObject.put("shipCount", c.getShipCount());	
	
			//update cell in db
//			collection.remove(searchQuery);
//			collection.insert(cellDBObject);
			collection.save(cellDBObject);
		}
		
		public String getDbname() {
			return dbname;
		}

		public void setDbname(String dbname) {
			this.dbname = dbname;
		}
		

	}
