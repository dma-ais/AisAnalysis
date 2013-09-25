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
package dk.dma.ais.analysis.coverage;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.coverage.calculator.DistributeOnlyCalculator;
import dk.dma.ais.analysis.coverage.calculator.SatCalculator;
import dk.dma.ais.analysis.coverage.calculator.SupersourceCoverageCalculator;
import dk.dma.ais.analysis.coverage.calculator.geotools.Helper;
import dk.dma.ais.analysis.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.CustomMessage;
import dk.dma.ais.analysis.coverage.data.ICoverageData;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.analysis.coverage.data.QueryParams;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.SuperShip;
import dk.dma.ais.analysis.coverage.data.json.JSonCoverageMap;
import dk.dma.ais.analysis.coverage.data.json.ExportCell;
import dk.dma.ais.analysis.coverage.data.json.JsonConverter;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketTags.SourceType;
import dk.dma.ais.transform.SourceTypeSatTransformer;

/**
 * Handler for received AisPackets 
 */
public class CoverageHandler {

    private final AisCoverageConfiguration conf;
    private SupersourceCoverageCalculator superCalc;
    private DistributeOnlyCalculator distributeOnlyCalc;
    private SatCalculator satCalc;
    private int cellSize=2500;
    private static final Logger LOG = LoggerFactory.getLogger(CoverageHandler.class);
    

	public CoverageHandler(AisCoverageConfiguration conf) {
        this.conf = conf;
        
        superCalc = new SupersourceCoverageCalculator( false, conf.getSourceNameMap());
		superCalc.setCellSize(cellSize);	
		
		distributeOnlyCalc = new DistributeOnlyCalculator( false, conf.getSourceNameMap());
		distributeOnlyCalc.setCellSize(cellSize);	
		superCalc.addListener(distributeOnlyCalc);
		
		satCalc = new SatCalculator();
		satCalc.setCellSize(cellSize);
		
		
		//Setting data handlers
		if(conf.getDatabaseConfiguration().getType().toLowerCase().equals("memoryonly")){
			ICoverageData dataH = new OnlyMemoryData();
			distributeOnlyCalc.setDataHandler(new OnlyMemoryData());
			superCalc.setDataHandler(dataH);
			satCalc.setDataHandler(dataH);	
			LOG.info("coverage calculators set up with memory only data handling");
		}
//		else{
//			distributeOnlyCalc.setDataHandler(new MongoBasedData(conf.getDatabaseConfiguration()));
//			superCalc.setDataHandler(new MongoBasedData(conf.getDatabaseConfiguration()));
//			LOG.info("coverage calculators set up with mongodb data handling");
//		}
		
		
		//setting grid granularity
		Helper.latSize=conf.getLatSize();
		Helper.lonSize=conf.getLonSize();
		distributeOnlyCalc.getDataHandler().setLatSize(conf.getLatSize());
		distributeOnlyCalc.getDataHandler().setLonSize(conf.getLonSize());
		superCalc.getDataHandler().setLatSize(conf.getLatSize());
		superCalc.getDataHandler().setLonSize(conf.getLonSize());
		satCalc.getDataHandler().setLatSize(conf.getLatSize());
		satCalc.getDataHandler().setLonSize(conf.getLonSize());
		LOG.info("grid granularity initiated with lat: "+conf.getLatSize() + " and lon: " + conf.getLonSize());
		
		final Date then = new Date();
		Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				while(true){
					try{
						Thread.sleep(10000);
					}catch(Exception e){
							
					}
				
					Date now = new Date();
//					System.out.println((((now.getTime()-then.getTime())/1000)));
					System.out.println("messages per second: "+(unfiltCount/(((now.getTime()-then.getTime())/1000))));
					System.out.println("messages processed: "+unfiltCount);
					System.out.println("biggest delay in minutes: "+ biggestDelay/1000/60);
					System.out.println("weird stamps: "+weird);
					System.out.println("delayed more than ten min: "+delayedMoreThanTen);
					System.out.println("delayed less than ten min: "+delayedLessThanTen);
					long numberofcells = 0;
					long uniquecells = 0;
					long uniqueships = 0;
					long uniqueShipHours = 0;
					for (SuperShip ss : satCalc.getSuperships().values()) {
						uniqueShipHours+=ss.getHours().size();
					}
					for (Source s : satCalc.getDataHandler().getSources()) {
						numberofcells+= s.getGrid().size();
						uniquecells+= s.getGrid().size();
						uniqueships+=s.getShips().size();
					}
					for (Source s : distributeOnlyCalc.getDataHandler().getSources()) {
						numberofcells+= s.getGrid().size();
					}
					System.out.println("total cells: "+numberofcells);
					System.out.println("Unique cells: "+uniquecells);
					System.out.println("Unique ships: "+uniqueships);
					System.out.println("Unique ship hours: "+uniqueShipHours);
					System.out.println(satCalc.getDataHandler().getSources().size());
					System.out.println();
				}		
				
			}
		});
		
		t.start();
		
    }
//    int pr=0;
	int unfiltCount = 0;
	Date start = new Date();
	long biggestDiff = 0;
	long now = 0;
	AisPacket lastTer = null;
	long biggestDelay = 0;
	int weird=0;
	int delayedMoreThanTen = 0;
	int delayedLessThanTen = 0;
    public void receiveUnfiltered(AisPacket packet) {
    	
    		
		unfiltCount++;
//		AisMessage message = packet.tryGetAisMessage();
//        if (message == null) {	return;	}
//        
//		
//		CustomMessage c = satCalc.aisToCustom(message, "sat");
//		if(c == null)return;
//		if(packet.getTags().getSourceType() == SourceType.SATELLITE){
////			System.out.println("sdf");
////			System.out.println(packet.getReceiveTimestamp());
//			if(c.getTimestamp().getTime() < now){
//				long delay = now-c.getTimestamp().getTime();
////				System.out.println(delay);
//				if(delay > 600000){
//					delayedMoreThanTen++;
//				}
//				else{
//					delayedLessThanTen++;
//				}
//				if(delay < 86400000){
//					
//					if(delay > biggestDelay)
//						biggestDelay=delay;
//				}else{
//					weird++;
//				}
////				System.out.println(now-c.getTimestamp().getTime());
////				System.out.println("mja");
//			}
//		}else{
////			System.out.println((c.getTimestamp().getTime()-now));
//			if(now == 0)
//				now=c.getTimestamp().getTime();
//			else{
//				if( c.getTimestamp().getTime()-now < 86400000){
//					
//					if(now < c.getTimestamp().getTime()){
//						now = c.getTimestamp().getTime();
//	//					System.out.println(now);
//					}
//				}
//			}
//			
////			System.out.println(packet.getReceiveTimestamp());
////			if(packet.getReceiveTimestamp())
////			lastTer=packet;
////			if(lastTer == null)
////				last
//		}
    	superCalc.processMessage(packet, "supersource");
    	distributeOnlyCalc.processMessage(packet, "1");    
        satCalc.processMessage(packet, "sat");

    }
    
    int filtCount = 0;
    public void receiveFiltered(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();
        if (message == null) {
            return;
        }
        filtCount++;
//        superCalc.processMessage(message, "supersource");
    }
    
    public JSonCoverageMap getJsonCoverage(double latStart, double lonStart, double latEnd, double lonEnd, Map<String, Boolean> sources, int multiplicationFactor, Date starttime, Date endtime) {

		JSonCoverageMap map = new JSonCoverageMap();
		map.latSize=Helper.latSize*multiplicationFactor;
		map.lonSize=Helper.lonSize*multiplicationFactor;
		

		HashMap<String, ExportCell> JsonCells = new HashMap<String, ExportCell>();

		QueryParams params = new QueryParams();
		params.latStart=latStart;
		params.latEnd=latEnd;
		params.lonStart=lonStart;
		params.lonEnd=lonEnd;
		params.sources=sources;
		params.multiplicationFactor=multiplicationFactor;
		params.startDate=starttime;
		params.endDate=endtime;

		List<Cell> celllist = distributeOnlyCalc.getDataHandler().getCells( params );
		HashMap<String,Boolean> superSourceIsHere = new HashMap<String,Boolean>();
		superSourceIsHere.put("supersource", true);
		params.sources=superSourceIsHere;
		List<Cell> celllistSuper = superCalc.getDataHandler().getCells( params );
		Map<String,Cell> superMap = new HashMap<String,Cell>();
		for (Cell cell : celllistSuper) {
			if(cell.getNOofReceivedSignals() > 0){
				superMap.put(cell.getId(), cell);
			}
//			System.out.println("yir"+cell.getNOofReceivedSignals( starttime,  endtime));
//			System.out.println("yir"+cell.getNOofReceivedSignals());
		}
		
		if(!celllist.isEmpty())
			map.latSize = Helper.latSize*multiplicationFactor;
		
		for (Cell cell : celllist) {
			Cell superCell = superMap.get(cell.getId());
			if(superCell == null){

			}else{
				ExportCell existing = JsonCells.get(cell.getId());
				ExportCell theCell = JsonConverter.toJsonCell(cell, superCell, starttime, endtime);
				if (existing == null)
					existing = JsonCells.put(cell.getId(), JsonConverter.toJsonCell(cell, superCell, starttime, endtime));
				else if (theCell.getCoverage() > existing.getCoverage()){
					JsonCells.put(cell.getId(), theCell);
				}
			}
		}

		map.cells = JsonCells;
		for (ExportCell cell : JsonCells.values()) {
			
//			System.out.println(cell.nrOfRecMes);
		}
//		System.out.println("map="+map.cells.size());
//		System.out.println(map.cells.values()));
		return map;
	}
    
    
    public DistributeOnlyCalculator getDistributeCalc(){	return distributeOnlyCalc;	}
    public SupersourceCoverageCalculator getSupersourceCalc(){	return superCalc;	}
	public SatCalculator getSatCalc(){	return satCalc;	}


}
