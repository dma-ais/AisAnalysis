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

import dk.dma.ais.analysis.coverage.calculator.DistributeOnlyCalculator2;
import dk.dma.ais.analysis.coverage.calculator.SupersourceCoverageCalculator;
import dk.dma.ais.analysis.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacket;

/**
 * Handler for received AisPackets 
 */
public class CoverageHandler {

    private final AisCoverageConfiguration conf;
    private SupersourceCoverageCalculator superCalc;
    private DistributeOnlyCalculator2 distributeOnlyCalc;
    private int cellSize=2500;
   
    
    public CoverageHandler(AisCoverageConfiguration conf) {
        this.conf = conf;
        
        superCalc = new SupersourceCoverageCalculator( false);
		superCalc.setCellSize(cellSize);	
		
		distributeOnlyCalc = new DistributeOnlyCalculator2( false);
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
        distributeOnlyCalc.processMessage(message, "1");	
        
    }
    
    public void receiveFiltered(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();
        if (message == null) {
            return;
        }
        
        superCalc.processMessage(message, "supersource");
    }

}
