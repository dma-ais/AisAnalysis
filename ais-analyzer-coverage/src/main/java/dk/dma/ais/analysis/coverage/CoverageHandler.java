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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dma.ais.analysis.coverage.calculator.DistributeOnlyCalculator;
import dk.dma.ais.analysis.coverage.calculator.SupersourceCoverageCalculator;
import dk.dma.ais.analysis.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.analysis.coverage.data.json.JSonCoverageMap;
import dk.dma.ais.analysis.coverage.data.json.JsonCell;
import dk.dma.ais.analysis.coverage.data.json.JsonConverter;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacket;

/**
 * Handler for received AisPackets 
 */
public class CoverageHandler {

    private final AisCoverageConfiguration conf;
    private SupersourceCoverageCalculator superCalc;
    private DistributeOnlyCalculator distributeOnlyCalc;
    private int cellSize=2500;
   
    
    public CoverageHandler(AisCoverageConfiguration conf) {
        this.conf = conf;
        
        superCalc = new SupersourceCoverageCalculator( false);
		superCalc.setCellSize(cellSize);	
		
		distributeOnlyCalc = new DistributeOnlyCalculator( false);
		distributeOnlyCalc.setCellSize(cellSize);	
		superCalc.addListener(distributeOnlyCalc);
		
		
		distributeOnlyCalc.setDataHandler(new OnlyMemoryData());
		superCalc.setDataHandler(new OnlyMemoryData());
		
		distributeOnlyCalc.getDataHandler().setLatSize(0.0225225225);
		distributeOnlyCalc.getDataHandler().setLonSize(0.0386812541);
		superCalc.getDataHandler().setLatSize(0.0225225225);
		superCalc.getDataHandler().setLonSize(0.0386812541);
		
    }

    public void receiveUnfiltered(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();
        if (message == null) {
            return;
        }
        superCalc.processMessage(message, "supersource");
        distributeOnlyCalc.processMessage(message, "1");	

        
    }
    
    int filtCount = 0;
    public void receiveFiltered(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();
        if (message == null) {
            return;
        }
        filtCount++;
//        superCalc.processMessage(message, "supersource");
//        System.out.println("filt: "+filtCount);
    }
    
    public JSonCoverageMap getJsonCoverage(double latStart, double lonStart, double latEnd, double lonEnd, Map<String, Boolean> sources, int multiplicationFactor) {

		JSonCoverageMap map = new JSonCoverageMap();
		map.latSize=distributeOnlyCalc.getDataHandler().getLatSize()*multiplicationFactor;
		map.lonSize=distributeOnlyCalc.getDataHandler().getLonSize()*multiplicationFactor;
		

		HashMap<String, JsonCell> JsonCells = new HashMap<String, JsonCell>();

		List<Cell> celllist = distributeOnlyCalc.getDataHandler().getCells( latStart,  lonStart,  latEnd, lonEnd, sources, multiplicationFactor);
		HashMap<String,Boolean> superSourceIsHere = new HashMap<String,Boolean>();
		superSourceIsHere.put("supersource", true);
		List<Cell> celllistSuper = superCalc.getDataHandler().getCells( latStart,  lonStart,  latEnd, lonEnd, superSourceIsHere, multiplicationFactor);
		System.out.println("WEEEE"+celllistSuper.size());
		System.out.println("MUUUU"+celllist.size());
		Map<String,Cell> superMap = new HashMap<String,Cell>();
		for (Cell cell : celllistSuper) {
			superMap.put(cell.getId(), cell);
		}
		
		if(!celllist.isEmpty())
			map.latSize = celllist.get(0).getGrid().getLatSize();
		
		for (Cell cell : celllist) {
			Cell superCell = superMap.get(cell.getId());
			if(superCell == null){
//				System.out.println("prit");
			}else{
				JsonCell existing = JsonCells.get(cell.getId());
				JsonCell theCell = JsonConverter.toJsonCell(cell, superCell);
				if (existing == null)
					existing = JsonCells.put(cell.getId(), JsonConverter.toJsonCell(cell, superCell));
				else if (theCell.getCoverage() > existing.getCoverage()){
					JsonCells.put(cell.getId(), theCell);
				}
			}
		}

		map.cells = JsonCells;
		return map;
	}
    
    
    public DistributeOnlyCalculator getDistributeCalc(){
    	return distributeOnlyCalc;
    }
    public SupersourceCoverageCalculator getSupersourceCalc(){
    	return superCalc;
    }


}
