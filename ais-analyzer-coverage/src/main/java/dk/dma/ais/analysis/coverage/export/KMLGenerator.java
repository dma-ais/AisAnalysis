package dk.dma.ais.analysis.coverage.export;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Cell;



//TODO retrieve sources with larger cells.
//TODO retrieve cell data from both super and individual source


public class KMLGenerator {


//	public static void generateKML(CoverageCalculator calc, String path) {
	public static void generateKML(Collection<Source> grids, double latSize, double lonSize, HttpServletResponse response) {

		
		
		
//		FileWriter fstream = null;
//		BufferedWriter out = null;
		
		
		HttpServletResponse out = response;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		
		String fileName = ("aiscoverage-" + dateFormat.format(date)+ "_latSize "+latSize+"_lonSize "+lonSize+".kml");
		out.setContentType("application/vnd.google-earth.kml+xml");
		out.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		

//		try {
//			fstream = new FileWriter(path);
//			out = new BufferedWriter(fstream);
//		} catch (IOException e) {
//		}

			writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", out);
			writeLine("<kml>", out);
			writeLine("<Document>", out);
			writeLine("<name>AIS Coverage</name>", out);
			writeLine("<open>1</open>", out);
			writeLine("<Style id=\"redStyle\">", out);
			writeLine("	<IconStyle>", out);
			writeLine("		<scale>1.3</scale>", out);
			writeLine("		<Icon>", out);
			writeLine("			<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>", out);
			writeLine("		</Icon>", out);
			writeLine("		<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>", out);
			writeLine("	</IconStyle>", out);
			writeLine("	<LineStyle>", out);
			writeLine("		<color>ff0000ff</color>", out);
			writeLine("	</LineStyle>", out);
			writeLine("	<PolyStyle>", out);
			writeLine("		<color>ff0000ff</color>", out);
			writeLine("	</PolyStyle>", out);
			writeLine("</Style>", out);
			writeLine("<Style id=\"orangeStyle\">", out);
			writeLine("	<IconStyle>", out);
			writeLine("		<scale>1.3</scale>", out);
			writeLine("		<Icon>", out);
			writeLine("			<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>", out);
			writeLine("		</Icon>", out);
			writeLine("		<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>", out);
			writeLine("	</IconStyle>", out);
			writeLine("	<LineStyle>", out);
			writeLine("		<color>ff00aaff</color>", out);
			writeLine("	</LineStyle>", out);
			writeLine("	<PolyStyle>", out);
			writeLine("		<color>ff00aaff</color>", out);
			writeLine("	</PolyStyle>", out);
			writeLine("</Style>", out);
			writeLine("<Style id=\"greenStyle\">", out);
			writeLine("	<IconStyle>", out);
			writeLine("		<scale>1.3</scale>", out);
			writeLine("		<Icon>", out);
			writeLine("			<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>", out);
			writeLine("		</Icon>", out);
			writeLine("		<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>", out);
			writeLine("	</IconStyle>", out);
			writeLine("	<LineStyle>", out);
			writeLine("		<color>ff00ff00</color>", out);
			writeLine("	</LineStyle>", out);
			writeLine("	<PolyStyle>", out);
			writeLine("	<color>ff00ff55</color>", out);
			writeLine("</PolyStyle>", out);
			writeLine("</Style>", out);

			for (Source grid : grids) {
				generateGrid(grid.getIdentifier(), grid.getGrid().values(), out, latSize, lonSize);
			}

			writeLine("</Document>", out);
			writeLine("</kml>", out);

			
			
			
			//TODO check hvad det er der giver en aw snap internal error fejl efter kml generate er kørt
			try {
				out.getOutputStream().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			System.out.println("kml generated");

	}

//	private static void writeLine(String line, BufferedWriter out){
//		try{
//			out.write(line + "\n");
//			out.flush();
//					
//		}catch(Exception e){
//			System.out.println("baaah");
//		}
//	}
	
	private static void writeLine(String line, HttpServletResponse out){
		try{
			out.getOutputStream().write((line + "\n").getBytes());
			out.getOutputStream().flush();
		}catch(Exception e){
			System.out.println("baaah");
		}
	}
	
//	private static void generateGrid(String bsMmsi, Collection<Cell> cells,
//			BufferedWriter out, double latSize, double lonSize, StringBuilder sb) {
//
//			writeLine("<Folder>", out);
//			writeLine("<name>" + bsMmsi + "</name>", out);
//			writeLine("<open>1</open>", out);
//			for (Cell cell : cells) {
//
//				//We ignore cells, where average number of messages, is below 10 per ship
//				//Maybe there is a bug in AISMessage system, that assign some messages to wrong Base Stations
//				//Bug found and fixed
////				if (cell.NOofReceivedSignals / cell.ships.size() > 10) {
//
//					if (cell.getCoverage() > 0.8) { // green
//						generatePlacemark("#greenStyle", cell, 300, out, latSize, lonSize);
//					} else if (cell.getCoverage() > 0.5) { // orange
//						generatePlacemark("#orangeStyle", cell, 200, out, latSize, lonSize);
//					} else { // red
//						generatePlacemark("#redStyle", cell, 100, out, latSize, lonSize);
//					}
//
////				}
//
//			}
//
//			writeLine("</Folder>", out);
//
//	}
	
	private static void generateGrid(String bsMmsi, Collection<Cell> cells,
			HttpServletResponse out, double latSize, double lonSize) {

			writeLine("<Folder>", out);
			writeLine("<name>" + bsMmsi + "</name>", out);
			writeLine("<open>0</open>", out);
			for (Cell cell : cells) {

				//We ignore cells, where average number of messages, is below 10 per ship
				//Maybe there is a bug in AISMessage system, that assign some messages to wrong Base Stations
				//Bug found and fixed
//				if (cell.NOofReceivedSignals / cell.ships.size() > 10) {

					if (cell.getCoverage() > 0.8) { // green
						generatePlacemark("#greenStyle", cell, 300, out, latSize, lonSize);
					} else if (cell.getCoverage() > 0.5) { // orange
						generatePlacemark("#orangeStyle", cell, 200, out, latSize, lonSize);
					} else { // red
						generatePlacemark("#redStyle", cell, 100, out, latSize, lonSize);
					}

//				}

			}

			writeLine("</Folder>", out);

	}

//	private static void generatePlacemark(String style, Cell cell, int z,
//			BufferedWriter out, double latSize, double lonSize) {
//
//			writeLine("<Placemark>", out);
//			writeLine("<name>" + cell.getId() + "</name>", out);
//			writeLine("<styleUrl>" + style + "</styleUrl>", out);
//			writeLine("<Polygon>", out);
//			writeLine("<altitudeMode>relativeToGround</altitudeMode>", out);
//			writeLine("<tessellate>1</tessellate>", out);
//			writeLine("<outerBoundaryIs>", out);
//			writeLine("<LinearRing>", out);
//			writeLine("<coordinates>", out);
//
//			writeLine(		cell.getLongitude() + "," + cell.getLatitude() + "," + z+ " " + 
//							(cell.getLongitude() + lonSize) + "," + cell.getLatitude() + ","  + z + " " + 
//							(cell.getLongitude() + lonSize) + "," + (cell.getLatitude() + latSize) + "," + z + " " + 
//							cell.getLongitude() + "," + (cell.getLatitude() + latSize) + "," + z, out);
//
//
//			writeLine("</coordinates>", out);
//			writeLine("</LinearRing>", out);
//			writeLine("</outerBoundaryIs>", out);
//			writeLine("</Polygon>", out);
//			writeLine("</Placemark>", out);
//
//	}

	private static void generatePlacemark(String style, Cell cell, int z,
			HttpServletResponse out, double latSize, double lonSize) {

			writeLine("<Placemark>", out);
			writeLine("<name>" + cell.getId() + "</name>", out);
			writeLine("<styleUrl>" + style + "</styleUrl>", out);
			writeLine("<Polygon>", out);
			writeLine("<altitudeMode>relativeToGround</altitudeMode>", out);
			writeLine("<tessellate>1</tessellate>", out);
			writeLine("<outerBoundaryIs>", out);
			writeLine("<LinearRing>", out);
			writeLine("<coordinates>", out);

			writeLine(		cell.getLongitude() + "," + cell.getLatitude() + "," + z+ " " + 
							(cell.getLongitude() + lonSize) + "," + cell.getLatitude() + ","  + z + " " + 
							(cell.getLongitude() + lonSize) + "," + (cell.getLatitude() + latSize) + "," + z + " " + 
							cell.getLongitude() + "," + (cell.getLatitude() + latSize) + "," + z, out);


			writeLine("</coordinates>", out);
			writeLine("</LinearRing>", out);
			writeLine("</outerBoundaryIs>", out);
			writeLine("</Polygon>", out);
			writeLine("</Placemark>", out);

	}
	
	
}