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
import java.util.Calendar;
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
	
	//static folders
	private final Folder outerfolder;
	private final Folder shipnamefolder;
	private final Folder shiptypesfolder;
	private final Folder tanker;
	private final Folder cargo;
	private final Folder passenger;
	private final Folder support;
	private final Folder fishing;
	private final Folder other;
	private final Folder classb;
	private final Folder undefined;
	private final Folder SART;
	private Folder pickedfolder;
	private final Folder pasttrackfolder;
	private final Folder twentyfourhourfolder;
	private final Folder threedayfolder;
	

	public KmlGenerator(Map<Integer, AisTargetEntry> targetsMap,
			Map<Integer, IPastTrack> pastTrackMap, String resourceURL) {
		this.targetsMap = targetsMap;
		this.pastTrackMap = pastTrackMap;
		this.resourceUrl = resourceURL;
		kml = new Kml();
		document = kml.createAndSetDocument();
		
		//create the static folders
		outerfolder = document.createAndAddFolder().withName("Last known position");
		shipnamefolder = document.createAndAddFolder().withName("Ship names").withVisibility(false);
		shiptypesfolder = document.createAndAddFolder().withName("Ship types").withVisibility(false);
		tanker = outerfolder.createAndAddFolder().withName("Tanker").withVisibility(false);
		cargo = outerfolder.createAndAddFolder().withName("Cargo").withVisibility(false);
		passenger = outerfolder.createAndAddFolder().withName("Passenger").withVisibility(false);
		support = outerfolder.createAndAddFolder().withName("Support").withVisibility(false);
		fishing = outerfolder.createAndAddFolder().withName("Fishing").withVisibility(false);
		other = outerfolder.createAndAddFolder().withName("Other").withVisibility(false);
		classb = outerfolder.createAndAddFolder().withName("Classb").withVisibility(false);
		undefined = outerfolder.createAndAddFolder().withName("Undefined").withVisibility(false);
		SART = outerfolder.createAndAddFolder().withName("SART").withVisibility(false);
		pickedfolder = undefined;
		pasttrackfolder = document.createAndAddFolder().withName("Tracks").withVisibility(false);
		twentyfourhourfolder = pasttrackfolder.createAndAddFolder().withName("24 hours").withVisibility(false);
		threedayfolder = pasttrackfolder.createAndAddFolder().withName("72 hours").withVisibility(false);
		
		//Add styles
		addStyle("PassengerMoored", resourceUrl + "vessel_blue_moored.png",	"ff0000ff", 0.8, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("CargoMoored", resourceUrl + "vessel_green_moored.png","ff0000ff", .8, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("TankerMoored", resourceUrl + "vessel_red_moored.png",	"ff0000ff", .8, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("HighspeedcraftandWIGMoored", resourceUrl+ "vessel_yellow_moored.png", "ff0000ff", .8,	"<![CDATA[$[name]$[description]]]>", 0);
		addStyle("FishingMoored", resourceUrl + "vessel_orange_moored.png",	"ff0000ff", .8, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("SailingandpleasureMoored", resourceUrl+ "vessel_puple_moored.png", "ff0000ff", .8,	"<![CDATA[$[name]$[description]]]>", 0);
		addStyle("PilottugandothersMoored", resourceUrl + "vessel_turquoise_moored.png", "ff0000ff", .8,	"<![CDATA[$[name]$[description]]]>", 0);
		addStyle("UndefinedunknownMoored", resourceUrl + "vessel_gray_moored.png", "ff0000ff", .8, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("SailingandpleasureMoored", resourceUrl + "vessel_white_moored.png",	"ff0000ff", .8, "<![CDATA[$[name]$[description]]]>", 0);
		addStyle("empty", "", "", .8, "", 0);

		for (int i = 0; i < 360; i++) {
			addStyle(("Passenger-" + i), resourceUrl + "vessel_blue.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Cargo-" + i), resourceUrl + "vessel_green.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Tanker-" + i), resourceUrl + "vessel_red.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("HighspeedcraftandWIG-" + i), resourceUrl + "vessel_yellow.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Fishing-" + i), resourceUrl + "vessel_orange.png",	"ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Sailingandpleasure-" + i), resourceUrl + "vessel_purple.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Pilottugandothers-" + i), resourceUrl + "vessel_turquoise.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Undefinedunknown-" + i), resourceUrl	+ "vessel_gray.png", "ff0000ff", 1,	"<![CDATA[$[name]$[description]]]>", i+270);
			addStyle(("Sailing-" + i), resourceUrl + "vessel_white.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]]]>", i+270);
		}
	}

	public String generate() {
		

		// str.append(generateCamera());
		for (AisTargetEntry entry : targetsMap.values()) {
			
			//Initial checks. Vessel has to have at least a position
			AisTarget target = entry.getTarget();
			if (!(target instanceof AisVesselTarget)) {
				continue;
			}
			AisVesselTarget vesselTarget = (AisVesselTarget) target;
			AisVesselPosition vesselPosition = vesselTarget.getVesselPosition();
			if(vesselPosition == null)
				continue;
			if(vesselPosition.getPos()== null)
				continue;
			
			//get past track
			IPastTrack pastTrack = pastTrackMap.get(vesselTarget.getMmsi());
			List<PastTrackPoint> trackPoints = null;
			if(pastTrack != null)
				trackPoints = pastTrack.getPoints();

			//Set default ship information
			String name = ""+vesselTarget.getMmsi();
			String shiptype = "unknown";
			String style = "Undefinedunknown";
			String styleprefix = "Undefinedunknown";
			String description = "";
			
			//Extract information from vesselstatic
			AisVesselStatic vesselStatic = vesselTarget.getVesselStatic();
			if (vesselStatic != null) {
				
				name = vesselStatic.getName();
				ShipType type = null;

				if (vesselStatic.getShipTypeCargo() != null) {
					type = vesselStatic.getShipTypeCargo().getShipType();
		
					if(type != null){
	        			shiptype = type.toString();
	        			if(type.equals(ShipTypeCargo.ShipType.PASSENGER)){
	        				styleprefix = "Passenger";
	        				pickedfolder = passenger;
	        			}else if(type.equals(ShipTypeCargo.ShipType.CARGO)){
	        				styleprefix = "Cargo";
	        				pickedfolder = cargo;
	        			}else if(type.equals(ShipTypeCargo.ShipType.TANKER)){
	        				styleprefix = "Tanker";
	        				pickedfolder = tanker;
	        			}else if(type.equals(ShipTypeCargo.ShipType.HSC) || type.equals(ShipTypeCargo.ShipType.WIG)){
	        				styleprefix = "HighspeedcraftandWIG";
	        				pickedfolder = other;
	        			}else if(type.equals(ShipTypeCargo.ShipType.FISHING)){
	        				styleprefix = "Fishing";
	        				pickedfolder = fishing;
	        			}else if(type.equals(ShipTypeCargo.ShipType.PILOT) || type.equals(ShipTypeCargo.ShipType.MILITARY) || type.equals(ShipTypeCargo.ShipType.SAR) || type.equals(ShipTypeCargo.ShipType.DREDGING) || type.equals(ShipTypeCargo.ShipType.TUG) || type.equals(ShipTypeCargo.ShipType.TOWING) || type.equals(ShipTypeCargo.ShipType.TOWING_LONG_WIDE) || type.equals(ShipTypeCargo.ShipType.ANTI_POLLUTION) || type.equals(ShipTypeCargo.ShipType.LAW_ENFORCEMENT) || type.equals(ShipTypeCargo.ShipType.PORT_TENDER)){
	        				styleprefix = "Pilottugandothers";
	        				pickedfolder = other;
	        			}else if(type.equals(ShipTypeCargo.ShipType.SAILING) || type.equals(ShipTypeCargo.ShipType.PLEASURE)){
	        				styleprefix = "Sailingandpleasure";
	        				pickedfolder = other;
	        			}else{
	        				styleprefix = "Undefinedunknown";
	        				pickedfolder = undefined;
	        			}	
	        		}
					addToShipTypeFolder(styleprefix, vesselPosition);
					addToShipNameFolder(name, vesselPosition);
				}
			}
			
			//Check if vessel is moored
			Double sog = vesselPosition.getSog();
			if(sog != null && sog < 1){
				style = styleprefix+"Moored";
			}
			//If target is not moored, set direction
			else{
				int direction = 0;
				if (vesselPosition.getCog() != null) 
					direction = (int) Math.round(vesselPosition.getCog());
				
				style = pickStyle(styleprefix, direction);	
			}

//			age = vesselTarget.getCreated()
			
			
	        
	        
	       
	        
	        String destination = "Unknown";
	        Double draught = 0.0;
	        int imo = 0;
	        
	        
			if (vesselTarget instanceof AisClassATarget) {
				AisClassATarget classAtarget = (AisClassATarget) vesselTarget;
				AisClassAPosition classAPosition = classAtarget
						.getClassAPosition();
				AisClassAStatic classAStatic = classAtarget.getClassAStatic();
				
				
				if(classAStatic != null)
				{
				destination = classAStatic.getDestination();
				draught = classAStatic.getDraught();
				if(classAStatic.getImoNo() != null)
				{
					imo = classAStatic.getImoNo();
				}
				}
				if(classAPosition != null)
				{
					classAPosition.getNavStatus();
				}
				
			}
	        
	        
				if(vesselTarget != null && vesselStatic != null)
				{
					
					Date sDate = vesselTarget.getCreated();
			        Date eDate = new Date();
			        		
			       
			        Calendar cal3 = Calendar.getInstance();
			        cal3.setTime(sDate);
			        Calendar cal4 = Calendar.getInstance();
			        cal4.setTime(eDate);
			        long age = yearsBetween(cal3,cal4);
			        
			        int length = 0;
			        int breadth = 0;
			        
			        if(vesselStatic.getDimensions() != null)
			        {
			        	length = vesselStatic.getDimensions().getDimBow() + vesselStatic.getDimensions().getDimStern();
				        breadth = vesselStatic.getDimensions().getDimPort() + vesselStatic.getDimensions().getDimStarboard();
			        }
			        String flag = "";
			        if(vesselTarget.getCountry() != null)
			        {
			        	flag = vesselTarget.getCountry().toString();
			        }
					 
					
					description = "<font size= \"5\" color=\"black\">"+vesselTarget.getLastReport()+"Age "+age+"</font><table witdh=\"300\" align=\"centeret\"><tr>"+
							"<td Align=\"Left\">" +"Ship name: </td> <td Align=\"right\">"+vesselStatic.getName()+"</td></tr><tr><td Align=\"Left\">" +
							"mmsi: </td> <td Align=\"right\"><a href=\"http://www.marinetraffic.com/ais/showallphotos.aspx?mmsi="+vesselStatic.getMmsi()+"\"> "+vesselStatic.getMmsi() +
							"</a></td></tr><tr><td Align=\"Left\"> imo: </td> <td Align=\"right\"><a href=\"http://www.marinetraffic.com/ais/showallphotos.aspx?mmsi="+vesselStatic.getMmsi()+"\">"+imo+"</a></td></tr>" +
							"<tr><td Align=\"Left\"> Ship type: </td> <td Align=\"right\">"+shiptype+"</td></tr><tr>" +
							"<td Align=\"Left\"> Call sign: </td> <td Align=\"right\">"+vesselStatic.getCallsign()+"</td></tr><tr>" +
							"<td Align=\"Left\"> Flag: </td><td Align=\"right\">"+flag+"</td></tr><tr>"+
							"<td Align=\"Left\"> </td><td Align=\"right\"> </td></tr><tr>" +
							"<td Align=\"Left\"> Length (m): </td> <td Align=\"right\">"+ length+"</td></tr><tr>" +
							"<td Align=\"Left\"> Breadth (m): </td> <td Align=\"right\"> "+breadth+"</td></tr><tr>" + 
							"<td Align=\"Left\"> Draught (m): </td> <td Align=\"right\">"+draught+"</td></tr><tr>" + 
							"<td Align=\"Left\"> Gross ton (t): </td> <td Align=\"right\"> ??</td></tr><tr>" + 
							"<td Align=\"Left\"> </td><td Align=\"right\"> </td></tr><tr>"+
							"<td Align=\"Left\"> Nav. status: </td><td Align=\"right\">"+"Unknown"+"</td></tr><tr>" + 
							"<td Align=\"Left\"> Destination: </td><td Align=\"right\">"+destination+"</td></tr><tr>" + 
							"<td Align=\"Left\"> Heading: </td> <td Align=\"right\"> "+vesselPosition.getHeading()+"</td></tr><tr>" +
							"<td Align=\"Left\"> cog: </td> <td Align=\"right\"> "+vesselPosition.getCog()+"</td></tr><tr>" + 
							"<td Align=\"Left\"> sog (knots): </td> <td Align=\"right\">"+vesselPosition.getSog()+"</td></tr><tr>" +
							"<td Align=\"Left\"> AIS source: </td> <td Align=\"right\">??</td></tr></table>";
				}
					
			
			
			

//				//callsign
//				vesselStatic.getCallsign();
//				//forende + bagende
//				vesselStatic.getDimensions().getDimBow();
//				vesselStatic.getDimensions().getDimStern();
//				//højre+venstra side
//				vesselStatic.getDimensions().getDimPort();
//				vesselStatic.getDimensions().getDimStarboard();
//				
//				
//				vesselStatic.getName();
//				
//				vesselStatic.getShipTypeCargo().getShipType();
//				
//				vesselPosition.getCog();
//				//heading
//				vesselPosition.getHeading();
//				//sog
//				vesselPosition.getSog();
//				//age ??
//				vesselTarget.getCreated();
//				//lst report
//				vesselTarget.getLastReport();
//				//flag
//				vesselTarget.getCountry();
				
				
				
			
//			// Additional class A information
//			if (vesselTarget instanceof AisClassATarget) {
//				AisClassATarget classAtarget = (AisClassATarget) vesselTarget;
//				AisClassAPosition classAPosition = classAtarget
//						.getClassAPosition();
//				AisClassAStatic classAStatic = classAtarget.getClassAStatic();
//				
//				
//				if(classAStatic != null)
//				{
//				classAPosition.getNavStatus();
////				classAStatic.getDestination();
////				classAStatic.getDraught();
////				classAStatic.getImoNo();
//				System.out.println("dte = " + classAStatic.getImoNo());
//				}
//				
//			}
			
			
			
			addVessel(style, name, description, trackPoints, vesselPosition, twentyfourhourfolder, threedayfolder, vesselTarget.getMmsi());
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
		return (shiptype+"-" + direction);
	}

	private String generateCamera() {
		return "<Camera><longitude>-35</longitude><latitude>70</latitude><altitude>4200000</altitude><heading>0</heading></Camera>";
	}
	
	private void addStyle(String stylename, String iconUri, String iconColor, double iconScale, String ballonText, int heading){
		Style style = document.createAndAddStyle();
		style.withId(stylename);
		style.createAndSetIconStyle()
//		.withColor(iconColor)
		.withHeading(heading)
		.withScale(iconScale)
		.withIcon(new Icon().withHref(iconUri));

		
		style.createAndSetBalloonStyle().withText(ballonText);
	}

	private void addVessel(String stylename, String name, String description, List<PastTrackPoint> pastTrackPoints,AisVesselPosition vesselPosition, Folder twentyfourhour, Folder threeday, int mmsi){
//		if(pastTrackPoints.isEmpty())
//			return;
		
		Folder folder = pickedfolder.createAndAddFolder().withName(name);
		
		Folder folder1 = twentyfourhour.createAndAddFolder().withName(""+mmsi).withVisibility(false);
		Folder folder2 = threeday.createAndAddFolder().withName(""+mmsi).withVisibility(false);
		
		Placemark placemark2 = folder1.createAndAddPlacemark();
		LineString linestring1 = placemark2.createAndSetLineString();
		linestring1.withTessellate(new Boolean(true));
		
		Placemark placemark3 = folder2.createAndAddPlacemark();
		LineString linestring2 = placemark3.createAndSetLineString();
		linestring2.withTessellate(new Boolean(true));
		
		

		
		Placemark placemark1 = folder.createAndAddPlacemark();
//		LineString linestring = placemark1.createAndSetLineString();
//		linestring.withTessellate(new Boolean(true));
		
		//add pasttrack path
		if(pastTrackPoints != null){

			//check time of pathtrack points. Only keep points within 24 and 72 hours
			Date now = null; 
			if(!pastTrackPoints.isEmpty())
				now = pastTrackPoints.get(pastTrackPoints.size()-1).getTime();
				
			for (int i = pastTrackPoints.size()-1; i >= 0; i--) {
//				System.out.println(pastTrackPoints.get(i).getTime());
				int timeDif_Hours = (int)Math.abs((now.getTime()-pastTrackPoints.get(i).getTime().getTime())/1000/60/60);
				
				PastTrackPoint trackpoint = pastTrackPoints.get(i);
				
				//Put in 24hour folder
				if(timeDif_Hours <= 24){
					linestring1.addToCoordinates(trackpoint.getLon(), trackpoint.getLat());
				}
				//Put in 72hour folder
				if(timeDif_Hours <= 72){
					linestring2.addToCoordinates(trackpoint.getLon(), trackpoint.getLat());

				}else{
					break;
				}
//				System.out.println("time from now: "+timeDif_Hours);
			}
		}
		if(description != null){
			placemark1.withDescription(description);
		}
		
		folder.createAndAddPlacemark().withStyleUrl(stylename).withDescription(description)
		.createAndSetPoint().addToCoordinates(vesselPosition.getPos().getLongitude(), vesselPosition.getPos().getLatitude());
		
	}
	
	//Add to ship name folder
	private void addToShipNameFolder(String name, AisVesselPosition vesselPosition)
	{		
		shipnamefolder.createAndAddFolder().withName(name).withVisibility(false)
		.createAndAddPlacemark().withName(name).withVisibility(false).withStyleUrl("empty").createAndSetPoint().addToCoordinates(vesselPosition.getPos().getLongitude(), vesselPosition.getPos().getLatitude());
	}
	
	//Add to ship type folder
	private void addToShipTypeFolder(String name, AisVesselPosition vesselPosition)
	{
		
		shiptypesfolder.createAndAddFolder().withName(name).withVisibility(false)
		.createAndAddPlacemark().withName(name).withVisibility(false).withStyleUrl("empty").createAndSetPoint().addToCoordinates(vesselPosition.getPos().getLongitude(), vesselPosition.getPos().getLatitude());
		
	}
	
	
	private String marshall() throws FileNotFoundException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		kml.marshal(bos);
		return bos.toString();
	}
	
	public static long yearsBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        long daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        long years = daysBetween/365;
        return years;
    }

}
