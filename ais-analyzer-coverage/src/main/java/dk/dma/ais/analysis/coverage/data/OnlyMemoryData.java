package dk.dma.ais.analysis.coverage.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.DBObject;

import dk.dma.ais.analysis.coverage.calculator.geotools.Helper;
import dk.dma.ais.analysis.coverage.data.Ship.ShipClass;


public class OnlyMemoryData implements ICoverageData{
	
	protected SourceHandler gridHandler = new SourceHandler(null);
	
	@Override
	public Ship getShip(String sourceMmsi, long shipMmsi) {
		return gridHandler.getGrid(sourceMmsi).getShip(shipMmsi);
	}
	@Override
	public void updateShip(Ship ship) {

	}
	@Override
	public Cell getCell(String sourceMmsi, double lat, double lon) {
		return gridHandler.getGrid(sourceMmsi).getCell(lat, lon);
	}

	@Override
	public void updateCell(Cell c) {
		// TODO Auto-generated method stub
		
	}
	
	private List<Cell> getCells() {
		List<Cell> cells = new ArrayList<Cell>();
		Collection<Source> basestations = gridHandler.getBaseStations().values();
		for (Source basestation : basestations) {
			if (basestation.isVisible()) {
				
				// For each cell
				Collection<Cell> bscells = basestation.getGrid().values();
				for (Cell cell : bscells) {
					cells.add(cell);
				}
			}

		}
		return cells;
	}

	@Override
	public Ship createShip(String sourceMmsi, long shipMmsi, ShipClass shipClass) {
		return gridHandler.getGrid(sourceMmsi).createShip(shipMmsi, shipClass);
	}

	@Override
	public Cell createCell(String sourceMmsi, double lat, double lon) {
		return gridHandler.getGrid(sourceMmsi).createCell(lat, lon);
	}

	@Override
	public Source getSource(String sourceId) {
		return gridHandler.getBaseStations().get(sourceId);
	}

	@Override
	public Source createSource(String sourceId) {
		return gridHandler.createGrid(sourceId);
	}

	@Override
	public void setLatSize(double latsize) {
		Helper.latSize=latsize;
		
	}

	@Override
	public void setLonSize(double lonsize) {
		Helper.lonSize=lonsize;
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
	public Collection<Source> getSources() {
		return gridHandler.getBaseStations().values();
	}

//	@Override
//	public double getLatSize() {
//		return gridHandler.getLatSize();
//	}
//
//	@Override
//	public double getLonSize() {
//		return gridHandler.getLonSize();
//	}

	
	private List<Cell> getCells(double latStart, double lonStart, double latEnd,
			double lonEnd, Map<String, Boolean> sources, int multiplicationFactor, Date starttime, Date endtime) {
		
		List<Cell> cells = new ArrayList<Cell>();
		Collection<Source> basestations = gridHandler.getBaseStations().values();
		
		for (Source basestation : basestations) {
			if ( sources.containsKey(basestation.getIdentifier()) ) {	
				
				Source tempSource = new Source(basestation.getIdentifier());
				tempSource.setMultiplicationFactor(multiplicationFactor);
				// For each cell
				Collection<Cell> bscells = basestation.getGrid().values();
				for (Cell cell : bscells) {
					Cell tempCell = tempSource.getCell(cell.getLatitude(), cell.getLongitude());
					if(tempCell == null){
						tempCell = tempSource.createCell(cell.getLatitude(), cell.getLongitude());
					}
					tempCell.addNOofMissingSignals((int)cell.getNOofMissingSignals(starttime, endtime));
					tempCell.addReceivedSignals(cell.getNOofReceivedSignals(starttime, endtime));
					
//					System.out.println(cell.getNOofReceivedSignals(starttime, endtime));
				}
				
				// For each cell
				Collection<Cell> tempCells = tempSource.getGrid().values();
				for (Cell cell : tempCells) {
					
					if(cell.getLatitude() <= latStart && cell.getLatitude() >= latEnd ){
						if(cell.getLongitude() >= lonStart && cell.getLongitude() <= lonEnd ){
							
							//Only add if cell has received message n given timespan
							if(cell.getNOofReceivedSignals() > 0)
								cells.add(cell);
						}
					}	
				}
			}
		}
		
		return cells;
	}

	
	@Override
	public List<Cell> getCells(QueryParams params) {
		if(params == null) return getCells();
		return getCells(params.latStart, params.lonStart, params.latEnd, params.lonEnd, params.sources, params.multiplicationFactor, params.startDate, params.endDate);

	}
	@Override
	public void incrementReceivedSignals(String sourceMmsi, double lat,
			double lon, Date timestamp) {
		Cell cell = getCell(sourceMmsi, lat, lon);
		if (cell == null) {
			cell = createCell(sourceMmsi, lat, lon);
		}
		Date id = Helper.getFloorDate(timestamp);
		TimeSpan ts = cell.getFixedWidthSpans().get(id.getTime());
		if(ts==null){
			ts=new TimeSpan(id);
			ts.setLastMessage(Helper.getCeilDate(timestamp));
			cell.getFixedWidthSpans().put(id.getTime(), ts);
		}
		ts.setMessageCounterTerrestrial(ts.getMessageCounterTerrestrial()+1);
		
		
	}
	@Override
	public void incrementMissingSignals(String sourceMmsi, double lat,
			double lon, Date timestamp) {
		
		Cell cell = getCell(sourceMmsi, lat, lon);
		if (cell == null) {
			cell = createCell(sourceMmsi, lat, lon);
		}
		Date id = Helper.getFloorDate(timestamp);
		TimeSpan ts = cell.getFixedWidthSpans().get(id.getTime());
		if(ts==null){
			ts=new TimeSpan(id);
			ts.setLastMessage(Helper.getCeilDate(timestamp));
			cell.getFixedWidthSpans().put(id.getTime(), ts);
		}
		ts.incrementMissingSignals();
		
	}


}
