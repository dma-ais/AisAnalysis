package dk.dma.ais.analysis.viewer.kml;



import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

import de.micromata.opengis.kml.v_2_2_0.Style;
import dk.dma.ais.data.PastTrackPoint;

public class VesselViewKML {
	final Kml kml;
	final Document document;
	
	public VesselViewKML(){
		kml = new Kml();
		document = kml.createAndSetDocument();
	}
	

	public void addStyle(String stylename, String iconUri, String iconColor, double iconScale, String ballonText){
		Style style = document.createAndAddStyle();
		style.withId(stylename);
		style.createAndSetIconStyle()
//		.withColor(iconColor)
		.withScale(iconScale)
		.withIcon(new Icon().withHref(iconUri));

		
		style.createAndSetBalloonStyle()
		.withText(ballonText);
	}
	public void addVessel(String stylename, String name, String description, List<PastTrackPoint> pastTrackPoints){
		if(pastTrackPoints.isEmpty())
			return;
		
		Folder folder = document.createAndAddFolder().withName(name);
		
		
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
	}
	
	public String marshall() throws FileNotFoundException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		kml.marshal(bos);
		return bos.toString();
	}
		

	public static void main(String[] args) throws FileNotFoundException {
		VesselViewKML vesselview = new VesselViewKML();
		vesselview.addStyle("red", "/yees.png", "ff0000ff", 10.8, "<![CDATA[$[name]$[description]]]>");
//		vesselview.addVessel("red", "934584", null);
//		vesselview.addVessel("red", "heysa", "stort skiw");
		vesselview.marshall();
	}
}
