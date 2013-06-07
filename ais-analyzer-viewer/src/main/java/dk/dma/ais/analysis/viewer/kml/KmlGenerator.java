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
package dk.dma.ais.analysis.viewer.kml;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import dk.dma.ais.analysis.viewer.handler.AisTargetEntry;
import dk.dma.ais.data.AisClassAPosition;
import dk.dma.ais.data.AisClassAStatic;
import dk.dma.ais.data.AisClassATarget;
import dk.dma.ais.data.AisTarget;
import dk.dma.ais.data.AisVesselPosition;
import dk.dma.ais.data.AisVesselStatic;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.ais.data.IPastTrack;
import dk.dma.ais.data.PastTrackPoint;

public class KmlGenerator {

    private final Map<Integer, AisTargetEntry> targetsMap;
    private final Map<Integer, IPastTrack> pastTrackMap;

    public KmlGenerator(Map<Integer, AisTargetEntry> targetsMap, Map<Integer, IPastTrack> pastTrackMap) {
        this.targetsMap = targetsMap;
        this.pastTrackMap = pastTrackMap;
    }
    public String generate() {
    	VesselViewKML vvk = new VesselViewKML();
    	
    	vvk.addStyle("Passenger", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Cargo", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Tanker", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("HighspeedcraftandWIG", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Fishing", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Sailingandpleasure", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Pilottugandothers", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Undefinedunkown", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("Sailing", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("AnchoredMoored", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
    	
		
		
    	
    	
    	
//        str.append(generateCamera());
        for (AisTargetEntry entry : targetsMap.values()) {
            AisTarget target = entry.getTarget();
            if (!(target instanceof AisVesselTarget)) {
                continue;
            }            
            AisVesselTarget vesselTarget = (AisVesselTarget)target;            
            IPastTrack pastTrack = pastTrackMap.get(vesselTarget.getMmsi());
            List<PastTrackPoint> trackPoints = pastTrack.getPoints();

            
            AisVesselPosition vesselPosition = vesselTarget.getVesselPosition();
            AisVesselStatic vesselStatic = vesselTarget.getVesselStatic();
            
            // Additional class A information
            if (vesselTarget instanceof AisClassATarget) {
                AisClassATarget classAtarget = (AisClassATarget)vesselTarget;
                AisClassAPosition classAPosition = classAtarget.getClassAPosition();
                AisClassAStatic classAStatic = classAtarget.getClassAStatic();
            }
//            vesselTarget.
            vvk.addVessel("red", "skibsnavn", "stort skiw", trackPoints);
            
        }
       
        
        
        try {
			return vvk.marshall();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

    }

    private String generateCamera() {
        return "<Camera><longitude>-35</longitude><latitude>70</latitude><altitude>4200000</altitude><heading>0</heading></Camera>";
    }

}
