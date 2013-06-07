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
import dk.dma.ais.message.ShipTypeCargo;
import dk.dma.ais.message.ShipTypeCargo.ShipType;

public class KmlGenerator {

    private final Map<Integer, AisTargetEntry> targetsMap;
    private final Map<Integer, IPastTrack> pastTrackMap;
	private String resourceUrl;

    public KmlGenerator(Map<Integer, AisTargetEntry> targetsMap, Map<Integer, IPastTrack> pastTrackMap, String resourceURL) {
        this.targetsMap = targetsMap;
        this.pastTrackMap = pastTrackMap;
        this.resourceUrl=resourceURL;
    }
    public String generate() {
    	VesselViewKML vvk = new VesselViewKML();
    	
    	vvk.addStyle("Passenger", resourceUrl+"vessel_blue.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("PassengerMoored", resourceUrl+"vessel_blue_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	
    	vvk.addStyle("Cargo", resourceUrl+"vessel_green.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("CargoMoored", resourceUrl+"vessel_green_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	
    	vvk.addStyle("Tanker", resourceUrl+"vessel_red.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("TankerMoored", resourceUrl+"vessel_red_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");

    	vvk.addStyle("HighspeedcraftandWIG", resourceUrl+"vessel_yellow.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("HighspeedcraftandWIGMoored", resourceUrl+"vessel_yellow_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");

    	vvk.addStyle("Fishing", resourceUrl+"vessel_orange.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("FishingMoored", resourceUrl+"vessel_orange_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");

    	vvk.addStyle("Sailingandpleasure", resourceUrl+"vessel_puple.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("SailingandpleasureMoored", resourceUrl+"vessel_puple_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");

    	vvk.addStyle("Pilottugandothers", resourceUrl+"vessel_turquoise.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("PilottugandothersMoored", resourceUrl+"vessel_turquoise_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");

    	vvk.addStyle("Undefinedunkown", resourceUrl+"vessel_gray.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("UndefinedunkownMoored", resourceUrl+"vessel_gray_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");

    	vvk.addStyle("Sailing", resourceUrl+"vessel_white.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	vvk.addStyle("SailingMoored", resourceUrl+"vessel_white_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>");
    	
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
            
            
            
            String name = "unknown";
            String shiptype = "unknown";
            String style = "Undefinedunkown";
            if(vesselStatic != null){
            	name = vesselStatic.getName();
            	ShipType type = null;
            	if(vesselStatic.getShipTypeCargo() != null){
            		type = vesselStatic.getShipTypeCargo().getShipType();
            		if(type != null){
            			shiptype = type.toString();
            			if(type.equals(ShipTypeCargo.ShipType.PASSENGER)){
            				style = "Passenger";
            			}else if(type.equals(ShipTypeCargo.ShipType.CARGO)){
            				style = "Cargo";
            			}else if(type.equals(ShipTypeCargo.ShipType.TANKER)){
            				style = "Tanker";
            			}else if(type.equals(ShipTypeCargo.ShipType.HSC)){
            				style = "HighspeedcraftandWIG";
            			}else if(type.equals(ShipTypeCargo.ShipType.FISHING)){
            				style = "Fishing";
            			}else if(type.equals(ShipTypeCargo.ShipType.PILOT)){
            				style = "Pilottugandothers";
            			}else if(type.equals(ShipTypeCargo.ShipType.SAILING)){
            				style = "Sailing";
            			}else{
            				style = "Undefinedunkown";
            			}	
            		}
            	}	
            }
   

            
            // Additional class A information
            if (vesselTarget instanceof AisClassATarget) {
                AisClassATarget classAtarget = (AisClassATarget)vesselTarget;
                AisClassAPosition classAPosition = classAtarget.getClassAPosition();
                AisClassAStatic classAStatic = classAtarget.getClassAStatic();
            }
            vvk.addVessel(style, name, "stort skiw", trackPoints);
            
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
