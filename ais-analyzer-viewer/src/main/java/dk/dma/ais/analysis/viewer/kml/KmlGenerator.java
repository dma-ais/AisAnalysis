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
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;

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
	final Kml kml;
	final Document document;

	public KmlGenerator(Map<Integer, AisTargetEntry> targetsMap,
			Map<Integer, IPastTrack> pastTrackMap, String resourceURL) {
		this.targetsMap = targetsMap;
		this.pastTrackMap = pastTrackMap;
		this.resourceUrl = resourceURL;
		kml = new Kml();
		document = kml.createAndSetDocument();
	}

	public String generate() {
//		VesselViewKML vvk = new VesselViewKML();
		Folder outerfolder = document.createAndAddFolder().withName("Last known position");
		
		Folder shipnamefolder = document.createAndAddFolder().withName("Ship names")
				.withVisibility(false);
		Folder shiptypesfolder = document.createAndAddFolder().withName("Ship types").withVisibility(false);
		Folder Tanker = outerfolder.createAndAddFolder().withName("Tanker").withVisibility(false);
		Folder cargo = outerfolder.createAndAddFolder().withName("Cargo").withVisibility(false);
		Folder passenger = outerfolder.createAndAddFolder().withName("Passenger").withVisibility(false);
		Folder support = outerfolder.createAndAddFolder().withName("Support").withVisibility(false);
		Folder fishing = outerfolder.createAndAddFolder().withName("Fishing").withVisibility(false);
		Folder other = outerfolder.createAndAddFolder().withName("Other").withVisibility(false);
		Folder classb = outerfolder.createAndAddFolder().withName("Classb").withVisibility(false);
		Folder undefined = outerfolder.createAndAddFolder().withName("Undefined").withVisibility(false);
		Folder SART = outerfolder.createAndAddFolder().withName("SART").withVisibility(false);
		Folder pickedfolder = null;


		addStyle("PassengerMoored", resourceUrl + "vessel_blue_moored.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("CargoMoored", resourceUrl + "vessel_green_moored.png","ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("TankerMoored", resourceUrl + "vessel_red_moored.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("HighspeedcraftandWIGMoored", resourceUrl+ "vessel_yellow_moored.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", 0);
		addStyle("FishingMoored", resourceUrl + "vessel_orange_moored.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("SailingandpleasureMoored", resourceUrl+ "vessel_puple_moored.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", 0);
		addStyle("PilottugandothersMoored", resourceUrl + "vessel_turquoise_moored.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", 0);
		addStyle("UndefinedunkownMoored", resourceUrl + "vessel_gray_moored.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("SailingMoored", resourceUrl + "vessel_white_moored.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("empty", "", "", 1, "", 0);

		for (int i = 0; i < 360; i++) {
			addStyle(("Passenger-" + i), resourceUrl + "vessel_blue.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Cargo-" + i), resourceUrl + "vessel_green.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Tanker-" + i), resourceUrl + "vessel_red.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("HighspeedcraftandWIG-" + i), resourceUrl + "vessel_yellow.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Fishing-" + i), resourceUrl + "vessel_orange.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Sailingandpleasure-" + i), resourceUrl + "vessel_purple.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Pilottugandothers-" + i), resourceUrl + "vessel_turquoise.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Undefinedunkown-" + i), resourceUrl	+ "vessel_gray.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Sailing-" + i), resourceUrl + "vessel_white.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
		}

		// str.append(generateCamera());
		for (AisTargetEntry entry : targetsMap.values()) {
			AisTarget target = entry.getTarget();
			if (!(target instanceof AisVesselTarget)) {
				continue;
			}
			AisVesselTarget vesselTarget = (AisVesselTarget) target;
			IPastTrack pastTrack = pastTrackMap.get(vesselTarget.getMmsi());
			List<PastTrackPoint> trackPoints = pastTrack.getPoints();

			AisVesselPosition vesselPosition = vesselTarget.getVesselPosition();
			AisVesselStatic vesselStatic = vesselTarget.getVesselStatic();

			String name = "unknown";
			String shiptype = "unknown";
			String style = "UndefinedunkownMoored";
			String styleprefix = "UndefinedunkownMoored";

			

			if (vesselStatic != null) {
				

				name = vesselStatic.getName();
				ShipType type = null;
				
//				if(name == "FLEUR DE MER")
//				{
//					System.out.println(vesselStatic.getShipTypeCargo().getShipType());
//				}

				if (vesselTarget.getVesselPosition() != null) {
				
				if (vesselStatic.getShipTypeCargo() != null) {
					type = vesselStatic.getShipTypeCargo().getShipType();
					
					
					
					if(type != null){
	        			shiptype = type.toString();
//	        			System.out.println(shiptype);
	        			if(type.equals(ShipTypeCargo.ShipType.PASSENGER)){
	        				styleprefix = "Passenger";
	        				pickedfolder = passenger;
	        				addshiptypeplacemark("Passenger", trackPoints, shiptypesfolder);
	        			}else if(type.equals(ShipTypeCargo.ShipType.CARGO)){
	        				styleprefix = "Cargo";
	        				pickedfolder = cargo;
	        				addshiptypeplacemark("Cargo", trackPoints, shiptypesfolder);
	        			}else if(type.equals(ShipTypeCargo.ShipType.TANKER)){
	        				styleprefix = "Tanker";
	        				pickedfolder = Tanker;
	        				addshiptypeplacemark("Tanker", trackPoints, shiptypesfolder);
	        			}else if(type.equals(ShipTypeCargo.ShipType.HSC) || type.equals(ShipTypeCargo.ShipType.WIG)){
	        				styleprefix = "HighspeedcraftandWIG";
	        				pickedfolder = other;
	        				addshiptypeplacemark("HighspeedcraftandWIG", trackPoints, shiptypesfolder);
	        			}else if(type.equals(ShipTypeCargo.ShipType.FISHING)){
	        				styleprefix = "Fishing";
	        				pickedfolder = fishing;
	        				addshiptypeplacemark("Fishing", trackPoints, shiptypesfolder);
	        			}else if(type.equals(ShipTypeCargo.ShipType.PILOT) || type.equals(ShipTypeCargo.ShipType.MILITARY) || type.equals(ShipTypeCargo.ShipType.SAR) || type.equals(ShipTypeCargo.ShipType.DREDGING) || type.equals(ShipTypeCargo.ShipType.TUG) || type.equals(ShipTypeCargo.ShipType.TOWING) || type.equals(ShipTypeCargo.ShipType.TOWING_LONG_WIDE) || type.equals(ShipTypeCargo.ShipType.ANTI_POLLUTION) || type.equals(ShipTypeCargo.ShipType.LAW_ENFORCEMENT) || type.equals(ShipTypeCargo.ShipType.PORT_TENDER)){
	        				styleprefix = "Pilottugandothers";
	        				pickedfolder = other;
	        				addshiptypeplacemark("Pilottugandothers", trackPoints, shiptypesfolder);
	        			}else if(type.equals(ShipTypeCargo.ShipType.SAILING) || type.equals(ShipTypeCargo.ShipType.PLEASURE)){
	        				styleprefix = "Sailingandpleasure";
	        				pickedfolder = other;
	        				addshiptypeplacemark("Sailing", trackPoints, shiptypesfolder);
	        			}else{
	        				styleprefix = "Undefinedunkown";
	        				pickedfolder = undefined;
	        				addshiptypeplacemark("Undefinedunkown", trackPoints, shiptypesfolder);
	        			}	
	        		}
					else
					{
						System.out.println("vesselstatic null");
					}
					addshipnamefolder(name, trackPoints, shipnamefolder);
					
					if (type != null) {
						shiptype = type.toString();

						
//							if (vesselPosition.getSog() != null)
//							{
//								long speed = Math.round(vesselTarget.getVesselPosition().getSog());
//								if(speed == 0)
//								{
//									style =  (styleprefix+"Moored");
//								}
//							}
							
							
							if (vesselPosition.getCog() != null) {
								int direction = (int) Math.round(vesselPosition.getCog());
								style = pickStyle(styleprefix, direction);

							}
							else {
								System.out.println("cog = null");
							}
						}
					else
					{
						System.out.println("type was null");
					}
					}
				}
				addVessel(style, name, "stort skiw", trackPoints, pickedfolder);
			}

			// Additional class A information
			if (vesselTarget instanceof AisClassATarget) {
				AisClassATarget classAtarget = (AisClassATarget) vesselTarget;
				AisClassAPosition classAPosition = classAtarget
						.getClassAPosition();
				AisClassAStatic classAStatic = classAtarget.getClassAStatic();
			}
			

		}

		try {
			return marshall();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	private String pickStyle(String shiptype, int direction) {
		
		
		String styleName = (shiptype+"-" + direction);
		return styleName;
	}

	private String generateCamera() {
		return "<Camera><longitude>-35</longitude><latitude>70</latitude><altitude>4200000</altitude><heading>0</heading></Camera>";
	}
	
	private void generateFolders(){
		
	}
	public void addStyle(String stylename, String iconUri, String iconColor, double iconScale, String ballonText, int heading){
		Style style = document.createAndAddStyle();
		style.withId(stylename);
		style.createAndSetIconStyle()
//		.withColor(iconColor)
		.withHeading(heading)
		.withScale(iconScale)
		.withIcon(new Icon().withHref(iconUri));

		
		style.createAndSetBalloonStyle()
		.withText(ballonText);
	}
	public void addVessel(String stylename, String name, String description, List<PastTrackPoint> pastTrackPoints, Folder Tanker){
		if(pastTrackPoints.isEmpty())
			return;
		
		Folder folder = Tanker.createAndAddFolder().withName(name);

		
		Placemark placemark1 = folder.createAndAddPlacemark();
		LineString linestring = placemark1.createAndSetLineString();
		linestring.withTessellate(new Boolean(true));
		for (PastTrackPoint pastTrackPoint : pastTrackPoints) {
			linestring.addToCoordinates(pastTrackPoint.getLon(), pastTrackPoint.getLat());
		}
		if(description != null){
			placemark1.withDescription(description);
		}
		
		PastTrackPoint lastPoint = pastTrackPoints.get(pastTrackPoints.size() - 1);
		folder.createAndAddPlacemark().withStyleUrl(stylename)
		.createAndSetPoint().addToCoordinates(lastPoint.getLon(), lastPoint.getLat());
		
		//check time of pathtrack points. Only keep points within 24 and 72 hours
		Date now = new Date();
		for (int i = pastTrackPoints.size()-1; i >= 0; i--) {
//			System.out.println(pastTrackPoints.get(i).getTime());
			int timeDif_Hours = (int)Math.abs((now.getTime()-pastTrackPoints.get(i).getTime().getTime())/1000/60/60);
			
			//Put in 24hour folder
			if(timeDif_Hours <= 24){
				//TODO put in 24hour folder
			}
			//Put in 72hour folder
			if(timeDif_Hours <= 72){
				//TODO put in 72hour folder

			}else{
				break;
			}
//			System.out.println("time from now: "+timeDif_Hours);
		}

		
		
//		addshipnamefolder(name, lastPoint, shipnamefolder);
		
		

	}
	
	public void addshipnamefolder(String name, List<PastTrackPoint> pasttrack, Folder shipnamefolder)
	{
		if(pasttrack.isEmpty())
			return;
		
		PastTrackPoint lastPoint = pasttrack.get(pasttrack.size() - 1);
		Folder fold = shipnamefolder.createAndAddFolder().withName(name).withVisibility(false);
		fold.createAndAddPlacemark().withName(name).withVisibility(false).withStyleUrl("empty").createAndSetPoint().addToCoordinates(lastPoint.getLon(), lastPoint.getLat());
		
		

	}
	
	public void addshiptypeplacemark(String name, List<PastTrackPoint> pasttrack, Folder shiptypefolder)
	{
		if(pasttrack.isEmpty())
			return;
		
		PastTrackPoint lastPoint = pasttrack.get(pasttrack.size() - 1);
		Folder fold = shiptypefolder.createAndAddFolder().withName(name).withVisibility(false);
		fold.createAndAddPlacemark().withName(name).withVisibility(false).withStyleUrl("empty").createAndSetPoint().addToCoordinates(lastPoint.getLon(), lastPoint.getLat());
		
	}
	
	
	public String marshall() throws FileNotFoundException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		kml.marshal(bos);
		return bos.toString();
	}
	
	

}
