package dk.dma.ais.analysis.coverage.export;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import dk.dma.ais.analysis.coverage.calculator.geotools.Helper;
import dk.dma.ais.analysis.coverage.data.TimeSpan;
import dk.dma.ais.analysis.coverage.data.json.ExportShipTimeSpan;

public class ChartGenerator {
	private BufferedImage bi;
	private static Color terBarColor = new Color(  Integer.parseInt( "6699FF",16) );
	private static Color satBarColor = new Color(  Integer.parseInt( "99CC66",16) );
	private static Color chartBackgroundColor = new Color(  Integer.parseInt( "F0F0F0",16) );
 	private static Color backgroundColor = Color.white;
 	private static Image logo = loadImage(new File("web\\img\\logo.png"));
 	
 	
    private Graphics2D ig2;
	private int width;
	private int height=400;
	private int bottomOffset=60;
	private Date floorDate;
	private Date ceilDate;
	private int offset=300;
	private int topOffset=100;
	private int noOfNonContactPeriods;
	private int periodsOverOne;
	private int periodsOverThree;
	private int periodsOverSix;
	private int minutesCovered;
	private int minutesNotCovered;
	private int maxBetweenBarWidth;
	private int avgBetweenBarWidth;
	private AffineTransform orig;
	private Font defaultFont;
	private Font small = new Font("Dialog", Font.PLAIN, 11);
	private Font bold = new Font("Dialog", Font.BOLD, 12);
	private Font headerFont = new Font("Dialog", Font.PLAIN, 18);
	private int descriptionTopOffset=100;
	private int descriptionWidth=280;
	private int periodsOverTwo;
	private int periodsOverFour;
	private int periodsOverEight;
	
	public void printMessage(String message){
		bi = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		ig2 = bi.createGraphics();
		ig2.setPaint(Color.black);
		ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		ig2.drawString(message, 10, 20);

	}
	public void generateChartMethod2(Date startTime, Date endTime, List<TimeSpan> timeSpans, double latMin, double latMax, double lonMin, double lonMax, boolean use3d){
    	 if(timeSpans.isEmpty()){
 	    	bi = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
 		    ig2 = bi.createGraphics();
 		    ig2.setPaint(Color.black);
 		    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 	        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		    ig2.drawString("No data available", 10, 10);
 		    return;
 	    }
 	   
    	 this.floorDate = Helper.getFloorDate(startTime);
     	this.ceilDate = Helper.getFloorDate(endTime);
     	long timeDifference = (long) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
     	long exactTimeDifference = (long) Math.ceil((endTime.getTime() - startTime.getTime())/1000/60); //in minutes
     	
     	SimpleDateFormat dt = new SimpleDateFormat("dd-MM HH:mm");
     	
     	
      	this.width = (int) (timeDifference*60)+offset+20;
      	int maxBarHeight = height-bottomOffset-topOffset;
     	
     	
 		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	    ig2 = bi.createGraphics();
 	    ig2.setPaint(Color.black);
 	    ig2.setColor(backgroundColor);
 	    ig2.fillRect(0, 0, width, height);
 	    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 	    this.defaultFont = ig2.getFont();
// 	    Font small = new Font("Dialog", Font.PLAIN, 11);
 	    this.orig = ig2.getTransform();
 	    
 	    
 	    
 	    int maxheight = 0;
 	    //Find max height and calculate scale
 	    for (TimeSpan timeSpan : timeSpans) {
 	    	if(timeSpan.getMessageCounterSat() > maxheight){maxheight=timeSpan.getMessageCounterSat();}
 	    	if(timeSpan.getMessageCounterTerrestrialUnfiltered() > maxheight){maxheight=timeSpan.getMessageCounterTerrestrialUnfiltered();}
 		}
 	    double scale = 1;
     	if(maxheight > maxBarHeight){
     		scale = (double)maxBarHeight/maxheight;
     	}
     	
     	//draw logo
     	ig2.drawImage(logo, 3, 3, logo.getWidth(null), logo.getHeight(null), null);
     	
     	//Paint background
     	ig2.setPaint(chartBackgroundColor);
     	ig2.fillRect(offset+10,  topOffset-10-1, (int) (timeDifference*60), height-1-bottomOffset-topOffset+1);
     	
//     	ig2.setPaint(blendColor(chartBackgroundColor, Color.white, .5f));
//     	Polygon p = new Polygon();
//     	p.addPoint(offset, height-1-bottomOffset);
//     	p.addPoint(offset+10, height-1-bottomOffset-10);
//     	p.addPoint((int) (offset+10+(timeDifference*60)),height-1-bottomOffset-10);
//     	p.addPoint((int) (offset+(timeDifference*60)),height-1-bottomOffset);
//     	ig2.fillPolygon(p);
//     	
//     	ig2.setPaint(blendColor(chartBackgroundColor, Color.black, .85f));
//     	Polygon p2 = new Polygon();
//     	p2.addPoint(offset, height-1-bottomOffset);
//     	p2.addPoint(offset, 80+10);
//     	p2.addPoint(offset+10,80);
//     	p2.addPoint(offset+10,height-1-bottomOffset-10);
//     	ig2.fillPolygon(p2);
     	
     	
     	//more vertical lines
     	ig2.setPaint(Color.LIGHT_GRAY);
     	for (int i = 0; i < timeDifference; i++) {
     		
     		if(i%3==0){
     			ig2.setPaint(Color.black);
     			ig2.rotate(Math.toRadians(90.0));
//     			ig2.drawLine(i*60, 0, i*60, height-1);
     			Date currentDate = new Date(floorDate.getTime()+(1000*60*60*i));
//      			currentDate.setHours(currentDate.getHours()+i+1);
     			ig2.drawString(dt.format(currentDate), topOffset-80, i*60*-1-offset+4-10);	
     			ig2.setTransform(orig);
     			ig2.setPaint(Color.LIGHT_GRAY);
//     			dateLabel = ('0' + currentDate.getDate()).slice(-2)+"-"+('0' + currentDate.getMonth()).slice(-2)+" "+(('0' + currentDate.getHours()).slice(-2)+":00");
//     			console.log(outer.html());
//     			result+='<div class="labelDate" style="left:'+((i)*60-50)+'px">'+dateLabel+'</div>';
     		}
//     		if(i != 0){
     			ig2.drawLine(offset+i*60+10, topOffset-10-1, offset+i*60+10, height-1-bottomOffset-10);
//     			ig2.drawLine(offset+i*60, height-1-bottomOffset, offset+i*60+10, height-1-bottomOffset-10);
//     		}
//     		if(i == 0){
//     			result+='<div class="leftVerticalLine" style="left:'+(i)*60+'px;"></div>';
//     		}else{
//     			result+='<div class="line" style="left:'+(i)*60+'px;"></div>';
//     		}
     		
     	}
     	
     	//draw horizontal and vertical line
     	ig2.setPaint(Color.gray);
     	ig2.drawLine(offset+10, height-1-bottomOffset-10, width-11, height-1-bottomOffset-10);
     	ig2.drawLine(offset+10, topOffset-10-1, offset+10, height-1-bottomOffset-10);
     	
//     	List<JsonTimeSpan> jt = JsonConverter.toJsonTimeSpan(timeSpans);
     	long maxBetweenBarWidth = 0;
     	long avgBetweenBarWidth = 0;
     	long minutesCovered = 0;
     	long minutesNotCovered = 0;
     	double coverageRatio = 0;
     	int barsOverTwo = 0;
     	int barsOverFour = 0;
     	int barsOverEight= 0;
     	
     	TimeSpan last = null;
//     	ig2.setColor(Color.GRAY);
     	for (TimeSpan timeSpan : timeSpans) {
     		
     		//Find total distinct ships
     		Map<String, Boolean> totalShips = new HashMap<String, Boolean>();	
     		for (String mmsi : timeSpan.getDistinctShipsSat().keySet()) {
				totalShips.put(mmsi, true);
			}
     		for (String mmsi : timeSpan.getDistinctShipsTerrestrial().keySet()) {
				totalShips.put(mmsi, true);
			}
     		int totalDistinctShips = totalShips.size();
     		
     		//Draw terrestrial bar
     		double terrestrialCoverage = ((double)timeSpan.getDistinctShipsTerrestrial().size()/(double)totalDistinctShips);
     		long difference = timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime();
     		long diffFromFloorDate = timeSpan.getFirstMessage().getTime()-floorDate.getTime();
     		String label="";
     		int x = (int) (Math.floor(diffFromFloorDate/1000/60)+offset);
//     		int y = (int) (height-1-(timeSpan.getMessageCounterTerrestrialUnfiltered()*scale)-bottomOffset);
     		int barwidth = (int) (difference/1000/60);
     		ig2.setColor(terBarColor);
     		if(terrestrialCoverage > .15){
     			label=round(terrestrialCoverage*100, 2)+"%";
     		}
     		draw3dBar(x, 5, barwidth, (int) (terrestrialCoverage*200), terBarColor, label);
     		label="";
     		//Draw sat bar
     		ig2.setColor(satBarColor);
     		double satCoverage = ((double)timeSpan.getDistinctShipsSat().size()/(double)totalDistinctShips);
     		if(satCoverage > .15){
     			label=round(satCoverage*100, 2)+"%";
     		}
     		draw3dBar(x, 0, barwidth, (int) (satCoverage*200), satBarColor, label);


     		ig2.setTransform(orig);
     		ig2.setFont(defaultFont);
     		
     		//Draw distinct ship stats
     		ig2.setFont(small);
     		ig2.setColor(Color.DARK_GRAY);
     		ig2.drawString(totalDistinctShips+"", x+30-getTextWidth(totalDistinctShips+"")/2, height-1-bottomOffset+15);
     		ig2.drawString(timeSpan.getDistinctShipsTerrestrial().size()+"", x+30-getTextWidth(timeSpan.getDistinctShipsTerrestrial().size()+"")/2, height-1-bottomOffset+30);
     		ig2.drawString(timeSpan.getDistinctShipsSat().size()+"", x+30-getTextWidth(timeSpan.getDistinctShipsSat().size()+"")/2, height-1-bottomOffset+45);
     		
     		

 		}
     	if(timeSpans.size() > 1)
     		avgBetweenBarWidth=(long) (minutesNotCovered/(timeSpans.size()-1));
     	
     	ig2.setFont(small);
     	ig2.setColor(Color.gray);
     	ig2.drawString("Distinct Ships - Total", offset-getTextWidth("Distinct Ships- Total"), height-1-bottomOffset+15);
     	ig2.drawString("Distinct Ships - Terrestrial", offset-getTextWidth("Distinct Ships- Terrestrial"), height-1-bottomOffset+30);
     	ig2.drawString("Distinct Ships - Satellite", offset-getTextWidth("Distinct Ships- Satellite"), height-1-bottomOffset+45);
     	
     	coverageRatio=((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);
     	System.out.println((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);

     	
     	drawHeader("Satellite-Terrestrial: Fixed Periods");
    	drawDescription("Time span: ", dt.format(floorDate) +" - "+dt.format(ceilDate));
    	drawDescription("Lat-min: ", round(latMin,2)+" degrees");
    	drawDescription("Lat-max: ", round(latMax,2)+" degrees");
    	drawDescription("Lon-min: ", round(lonMin,2)+" degrees");
    	drawDescription("Lon-max: ", round(lonMax,2)+" degrees");
    	this.descriptionTopOffset+=10;
//     	
//     	descriptionTopOffset+=30;
//     	ig2.drawString("Max. \"hole\" width: ", 10, descriptionTopOffset);
//     	ig2.drawString(maxBetweenBarWidth+" min", descriptionWidth-getTextWidth(maxBetweenBarWidth+" min"), descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("Avg. \"hole\" width: ", 10, descriptionTopOffset);
//     	ig2.drawString(avgBetweenBarWidth+" min", descriptionWidth-getTextWidth(avgBetweenBarWidth+" min"), descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("Avg. max. \"hole\" width per day: ", 10, descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("Avg. \"hole\" width per day: ", 10, descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("Coverage ratio: ", 10, descriptionTopOffset);
//     	ig2.drawString(coverageRatio+" %", descriptionWidth-getTextWidth(coverageRatio+" %"), descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("Avg. coverage ratio per day: ", 10, descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("# holes per day ( > 2 hours): ", 10, descriptionTopOffset);
//     	ig2.drawString(""+barsOverTwo, descriptionWidth-getTextWidth(""+barsOverTwo), descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("# holes per day ( > 4 hours): ", 10, descriptionTopOffset);
//     	ig2.drawString(""+barsOverFour, descriptionWidth-getTextWidth(""+barsOverFour), descriptionTopOffset);
//     	
//     	descriptionTopOffset+=20;
//     	ig2.drawString("# holes per day ( > 8 hours): ", 10, descriptionTopOffset);
//     	ig2.drawString(""+barsOverEight, descriptionWidth-getTextWidth(""+barsOverEight), descriptionTopOffset);

    }
	private void draw3dBar(int x, int z, int width, int height, Color c, String label){
		ig2.setColor(blendColor(c, Color.white, .50f));
		Polygon p = new Polygon();
		p.addPoint(x+z, this.height-height-bottomOffset-1-z);
		p.addPoint(x+5+z, this.height-height-bottomOffset-1-5-z);
		p.addPoint(x+5+width+z, this.height-height-bottomOffset-1-5-z);
		p.addPoint(x+width+z, this.height-height-bottomOffset-1-z);
		ig2.fillPolygon(p);
		
		ig2.setColor(blendColor(c, Color.black, .80f));
		Polygon p2 = new Polygon();		
		p2.addPoint(x+width+z, this.height-height-bottomOffset-1-z);
		p2.addPoint(x+5+width+z, this.height-height-bottomOffset-1-5-z);
		p2.addPoint(x+5+width+z, this.height-bottomOffset-1-5-z);
		p2.addPoint(x+width+z, this.height-bottomOffset-1-z);
		ig2.fillPolygon(p2);
		
		ig2.setColor(c);
 		ig2.fillRect(x+z, this.height-height-bottomOffset-1-z, width, height);
 		
 		ig2.setColor(Color.black);
 		int stringLen = getTextWidth(label);
 		ig2.drawString(label,x+(width/2)-(stringLen/2)+z,this.height-bottomOffset-height+20-z);
		
	}
	private Color blendColor(Color clOne, Color clTwo, float fAmount) {
	    float fInverse = (float) (1.0 - fAmount);

	    // I had to look up getting colour components in java.  Google is good :)
	    float afOne[] = new float[3];
	    clOne.getColorComponents(afOne);
	    float afTwo[] = new float[3]; 
	    clTwo.getColorComponents(afTwo);    

	    float afResult[] = new float[3];
	    afResult[0] = afOne[0] * fAmount + afTwo[0] * fInverse;
	    afResult[1] = afOne[1] * fAmount + afTwo[1] * fInverse;
	    afResult[2] = afOne[2] * fAmount + afTwo[2] * fInverse;

	    return new Color (afResult[0], afResult[1], afResult[2]);
	}
	public void generateChartMethod1(Date startTime, Date endTime, List<TimeSpan> timeSpans, double latMin, double latMax, double lonMin, double lonMax){
	    if(timeSpans.isEmpty()){
	    	bi = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		    ig2 = bi.createGraphics();
		    ig2.setPaint(Color.black);
		    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		    ig2.drawString("No data available", 10, 10);
		    return;
	    }
	    
	    this.floorDate = Helper.getFloorDate(startTime);
    	this.ceilDate = Helper.getFloorDate(endTime);
    	long timeDifference = (long) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
    	long exactTimeDifference = (long) Math.ceil((endTime.getTime() - startTime.getTime())/1000/60); //in minutes
    	
    	SimpleDateFormat dt = new SimpleDateFormat("dd-MM HH:mm");
    	
    	
//    	int offset = 300;
    	this.width = (int) (timeDifference*60)+offset+20;
//    	int height = 450;
//    	int bottomOffset = 55;
//    	int topOffset = 100;
    	int maxBarHeight = height-bottomOffset-topOffset;
//    	Color backgroundColor = Color.white;
//    	Image logo = loadImage(new File("web\\img\\logo.png"));
    	
    	
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    ig2 = bi.createGraphics();
	    ig2.setPaint(Color.black);
	    ig2.setColor(backgroundColor);
	    ig2.fillRect(0, 0, width, height);
	    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    this.defaultFont = ig2.getFont();
	   
	    this.orig = ig2.getTransform();
	    
	    
	    
	    int maxheight = 0;
	    //Find max height and calculate scale
	    for (TimeSpan timeSpan : timeSpans) {
	    	if(timeSpan.getMessageCounterSat() > maxheight){maxheight=timeSpan.getMessageCounterSat();}
		}
	    double scale = 1;
    	if(maxheight > maxBarHeight){
    		scale = (double)maxBarHeight/maxheight;
    	}
    	
    	//draw logo
    	ig2.drawImage(logo, 3, 3, logo.getWidth(null), logo.getHeight(null), null);
    	
    	//Paint background
     	ig2.setPaint(chartBackgroundColor);
     	ig2.fillRect(offset,  80, (int) (timeDifference*60), height-1-bottomOffset-80);
    	
    	//more vertical lines
    	ig2.setPaint(Color.LIGHT_GRAY);
    	for (int i = 0; i < timeDifference; i++) {
    		
    		if(i%3==0){
    			ig2.setPaint(Color.black);
    			ig2.rotate(Math.toRadians(90.0));
//    			ig2.drawLine(i*60, 0, i*60, height-1);
    			Date currentDate = new Date(floorDate.getTime()+(1000*60*60*i));
//     			currentDate.setHours(currentDate.getHours()+i+1);
    			ig2.drawString(dt.format(currentDate), 5, i*60*-1-offset+4);	
    			ig2.setTransform(orig);
    			ig2.setPaint(Color.LIGHT_GRAY);
//    			dateLabel = ('0' + currentDate.getDate()).slice(-2)+"-"+('0' + currentDate.getMonth()).slice(-2)+" "+(('0' + currentDate.getHours()).slice(-2)+":00");
//    			console.log(outer.html());
//    			result+='<div class="labelDate" style="left:'+((i)*60-50)+'px">'+dateLabel+'</div>';
    		}
    		if(i != 0){
    			ig2.drawLine(offset+i*60, 80, offset+i*60, height-1-bottomOffset);
    		}
//    		if(i == 0){
//    			result+='<div class="leftVerticalLine" style="left:'+(i)*60+'px;"></div>';
//    		}else{
//    			result+='<div class="line" style="left:'+(i)*60+'px;"></div>';
//    		}
    		
    	}
    	
  	
    	//draw horizontal and vertical line
    	ig2.setPaint(Color.gray);
    	ig2.drawLine(offset, height-1-bottomOffset, (int) ((timeDifference*60)+offset), height-1-bottomOffset);
    	ig2.drawLine(offset, 80, offset, height-1-bottomOffset);
    	
//     	ig2.drawLine(offset+10, height-1-bottomOffset-10, width-11, height-1-bottomOffset-10);
//     	ig2.drawLine(offset+10, topOffset-10-1, offset+10, height-1-bottomOffset-10);
     	
//    	List<JsonTimeSpan> jt = JsonConverter.toJsonTimeSpan(timeSpans);
    	this.maxBetweenBarWidth = 0;
    	this.avgBetweenBarWidth = 0;
    	this.minutesCovered = 0;
    	this.minutesNotCovered = 0;
    	double coverageRatio = 0;
    	
    	Date last = floorDate;
    	
    	for (TimeSpan timeSpan : timeSpans) {
    		
    		
    		
    		//Draw bar
    		ig2.setColor(satBarColor);
    		long difference = timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime();
    		long diffFromFloorDate = timeSpan.getFirstMessage().getTime()-floorDate.getTime();
    		int x = (int) (Math.floor(diffFromFloorDate/1000/60)+offset);
    		int y = (int) (height-1-(timeSpan.getMessageCounterSat()*scale)-bottomOffset);
    		int barwidth = (int) (difference/1000/60);
    		ig2.fillRect(x, y, barwidth, (int) Math.ceil((timeSpan.getMessageCounterSat()*scale)));
    		
    		
    		//Draw counter label
    		ig2.setColor(Color.black);
    		int stringLen = (int)  ig2.getFontMetrics().getStringBounds(""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")", ig2).getWidth();
    		ig2.drawString(""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")",x+(barwidth/2)-(stringLen/2),y-5);
    		
    		
    		//Draw width label
    		ig2.setFont(small);
    		ig2.rotate(Math.toRadians(90.0));
    		stringLen = (int)  ig2.getFontMetrics().getStringBounds(barwidth + " min", ig2).getWidth();
    		ig2.drawString(barwidth + " min", height-bottomOffset-1-stringLen+50, (x+(barwidth/2)-4)*-1);	
    		ig2.setColor(Color.GRAY);
     		ig2.setTransform(orig);
    		ig2.setFont(defaultFont);
    		ig2.setColor(Color.DARK_GRAY);
    		
    		minutesCovered+=barwidth;
    		
    		//Draw label between two bars
    		if(last != null){
    			drawBetweenBarLabel(last.getTime(), timeSpan.getFirstMessage().getTime(), 0);
    		}
    		
    		last = new Date(timeSpan.getLastMessage().getTime());
    	
    		ig2.setTransform(orig);
    		ig2.setFont(defaultFont);
    		
    		

		}
    	drawBetweenBarLabel(last.getTime(), ceilDate.getTime(), 0);
    	if(noOfNonContactPeriods > 0){
	    	avgBetweenBarWidth= (minutesNotCovered/noOfNonContactPeriods);
	    	coverageRatio=((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);
    	}


    	
    	drawHeader("Satellite-Only: Adaptive Periods");
    	drawDescription("Time span: ", dt.format(floorDate) +" - "+dt.format(ceilDate));
    	drawDescription("Lat-min: ", round(latMin,2)+" degrees");
    	drawDescription("Lat-max: ", round(latMax,2)+" degrees");
    	drawDescription("Lon-min: ", round(lonMin,2)+" degrees");
    	drawDescription("Lon-max: ", round(lonMax,2)+" degrees");
    	this.descriptionTopOffset+=10;
    	drawDescription("Longest non-contact period: ", maxBetweenBarWidth+" min");
    	drawDescription("Average non-contact period: ", avgBetweenBarWidth+" min");
    	drawDescription("Coverage ratio: ", coverageRatio+" %");
    	drawDescription("No. of non-contact periods above 2 h: ", periodsOverTwo+"");
    	drawDescription("No. of non-contact periods above 4 h: ", periodsOverFour+"");
    	drawDescription("No. of non-contact periods above 8 h: ", periodsOverEight+"");

//    	descriptionTopOffset+=20;
//    	ig2.drawString("Avg. max. \"hole\" width per day: ", 10, descriptionTopOffset);
//    	
//    	descriptionTopOffset+=20;
//    	ig2.drawString("Avg. \"hole\" width per day: ", 10, descriptionTopOffset);
//    	

	}
	private void drawHeader(String header){
		ig2.setColor(Color.DARK_GRAY);
    	ig2.setFont(headerFont);
    	ig2.drawString(header, 10, descriptionTopOffset);
    	ig2.setColor(Color.LIGHT_GRAY);
    	descriptionTopOffset+=10;
    	ig2.drawLine(10, descriptionTopOffset, descriptionWidth, descriptionTopOffset);
    	ig2.setColor(Color.DARK_GRAY);
    	descriptionTopOffset+=20;
	}
	private void drawDescription(String key, String value){
		ig2.setFont(bold);
    	ig2.drawString(key, 10, descriptionTopOffset);
    	ig2.setFont(defaultFont);
    	ig2.drawString(value, descriptionWidth-getTextWidth(value), descriptionTopOffset);
    	descriptionTopOffset+=20;
	}
	
	public static double round(double value, int decimals){
		int multi = (int) Math.pow(10, decimals);
		int result = (int) Math.round((value*multi));
		
		return (double)result/multi;
	}
	public static Image loadImage(File f){
		BufferedImage img = null;
		try {
		    img = ImageIO.read(f);
		} catch (IOException e) {
			System.out.println("image not found");
		}
		return img;
	}
	public int getTextWidth(String text){
		return (int) ig2.getFontMetrics().getStringBounds(text, ig2).getWidth();
	}
	public void exportAsPNG(OutputStream o){
		try {
			ImageIO.write(bi, "PNG", o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	  public static void main(String args[]) throws Exception {
		  TimeSpan s1 = new TimeSpan(new Date(1378065228317L));
		  s1.setLastMessage(new Date(s1.getFirstMessage().getTime()+1000*60*140));
		  s1.setMessageCounterSat(1000);
		  s1.getDistinctShipsSat().put("123", true);
		  s1.getDistinctShipsSat().put("1234", true);
		  s1.getDistinctShipsSat().put("12", true);
		  s1.getDistinctShipsTerrestrial().put("123", true);
		  s1.getDistinctShipsTerrestrial().put("1234", true);
		  s1.getDistinctShipsTerrestrial().put("12", true);
		  s1.getDistinctShipsTerrestrial().put("1", true);
		  s1.setMessageCounterTerrestrial(1250);
		  
		  TimeSpan s2 = new TimeSpan( new Date(s1.getLastMessage().getTime()+1000*60*60*1) );
		  s2.setLastMessage(new Date(s2.getFirstMessage().getTime()+1000*60*8));
		  s2.setMessageCounterSat(200);
		  s2.getDistinctShipsSat().put("123", true);
		  s2.getDistinctShipsSat().put("1234", true);
		  s2.getDistinctShipsSat().put("12", true);
		  s2.getDistinctShipsSat().put("1", true);  
		  s2.getDistinctShipsTerrestrial().put("123", true);
		  s2.getDistinctShipsTerrestrial().put("1234", true);
		  s2.getDistinctShipsTerrestrial().put("12", true);
		  s2.getDistinctShipsTerrestrial().put("1", true);
		  s2.setMessageCounterTerrestrial(340);
		  
		  TimeSpan s3 = new TimeSpan( new Date(s2.getLastMessage().getTime()+1000*60*60*2) );
		  s3.setLastMessage(new Date(s3.getFirstMessage().getTime()+1000*60*8));
		  s3.setMessageCounterSat(200);
		  s3.getDistinctShipsSat().put("123", true);
		  s3.getDistinctShipsSat().put("1", true);
		  s3.setMessageCounterTerrestrial(480);
		  s3.getDistinctShipsTerrestrial().put("123", true);
		  s3.getDistinctShipsTerrestrial().put("1234", true);
		  s3.getDistinctShipsTerrestrial().put("12", true);
		  s3.getDistinctShipsTerrestrial().put("1", true);
  
		  List<TimeSpan> l = new ArrayList<TimeSpan>();
		  l.add(s1);
		  l.add(s2);
		  l.add(s3);
		  
		  ChartGenerator cg =  new ChartGenerator();
		  
		  cg.generateChartMethod2(s1.getFirstMessage(), new Date(s2.getLastMessage().getTime()+1000*60*60*8), l, 10.234, 12.342, 55.7453, 56.02938, true);
		  
		  FileOutputStream fo = new FileOutputStream(new File("d:\\test.png"));
		  cg.exportAsPNG(fo);
		  
	   

	  }
	public void generateChartMethod3(Date startTime, Date endTime,
			int shipMMsi, List<ExportShipTimeSpan> timeSpans, boolean b) {
		if(timeSpans.isEmpty()){
	    	bi = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		    ig2 = bi.createGraphics();
		    ig2.setPaint(Color.black);
		    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		    ig2.drawString("No data available", 10, 10);
		    return;
	    }
	    
		this.floorDate = Helper.getFloorDate(startTime);
    	this.ceilDate = Helper.getFloorDate(endTime);
    	long timeDifference = (long) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
    	long exactTimeDifference = (long) Math.ceil((endTime.getTime() - startTime.getTime())/1000/60); //in minutes
    	
    	SimpleDateFormat dt = new SimpleDateFormat("dd-MM HH:mm");
    	
    	
     	this.width = (int) (timeDifference*60)+offset+20;
     	int maxBarHeight = height-bottomOffset-topOffset;

    	
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    ig2 = bi.createGraphics();
	    ig2.setPaint(Color.black);
	    ig2.setColor(backgroundColor);
	    ig2.fillRect(0, 0, width, height);
	    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    this.defaultFont = ig2.getFont();
	    
	    this.orig = ig2.getTransform();
	    


    	
    	//draw logo
     	ig2.drawImage(logo, 3, 3, logo.getWidth(null), logo.getHeight(null), null);
     	
     	//Paint background
     	ig2.setPaint(chartBackgroundColor);
     	ig2.fillRect(offset+10,  topOffset-10-1, (int) (timeDifference*60), height-1-bottomOffset-topOffset+1);
     	
     	
     	//more vertical lines
     	ig2.setPaint(Color.LIGHT_GRAY);
     	for (int i = 0; i < timeDifference; i++) {
     		
     		if(i%3==0){
     			ig2.setPaint(Color.black);
     			ig2.rotate(Math.toRadians(90.0));
//     			ig2.drawLine(i*60, 0, i*60, height-1);
     			Date currentDate = new Date(floorDate.getTime()+(1000*60*60*i));
//      			currentDate.setHours(currentDate.getHours()+i+1);
     			ig2.drawString(dt.format(currentDate), topOffset-80, i*60*-1-offset+4-10);	
     			ig2.setTransform(orig);
     			ig2.setPaint(Color.LIGHT_GRAY);
//     			dateLabel = ('0' + currentDate.getDate()).slice(-2)+"-"+('0' + currentDate.getMonth()).slice(-2)+" "+(('0' + currentDate.getHours()).slice(-2)+":00");
//     			console.log(outer.html());
//     			result+='<div class="labelDate" style="left:'+((i)*60-50)+'px">'+dateLabel+'</div>';
     		}
//     		if(i != 0){
     			ig2.drawLine(offset+i*60+10, topOffset-10-1, offset+i*60+10, height-1-bottomOffset-10);
//     			ig2.drawLine(offset+i*60, height-1-bottomOffset, offset+i*60+10, height-1-bottomOffset-10);
//     		}
//     		if(i == 0){
//     			result+='<div class="leftVerticalLine" style="left:'+(i)*60+'px;"></div>';
//     		}else{
//     			result+='<div class="line" style="left:'+(i)*60+'px;"></div>';
//     		}
     		
     	}
     	
     	//draw horizontal and vertical line
     	ig2.setPaint(Color.gray);
     	ig2.drawLine(offset+10, height-1-bottomOffset-10, width-11, height-1-bottomOffset-10);
     	ig2.drawLine(offset+10, topOffset-10-1, offset+10, height-1-bottomOffset-10);
     	
//    	List<JsonTimeSpan> jt = JsonConverter.toJsonTimeSpan(timeSpans);
    	this.maxBetweenBarWidth = 0;
    	this.avgBetweenBarWidth = 0;
    	this.minutesCovered = 0;
    	this.minutesNotCovered = 0;
    	double coverageRatio = 0;
    	this.noOfNonContactPeriods = 0;
    	this.periodsOverOne = 0;
    	this.periodsOverThree = 0;
    	this.periodsOverSix= 0;
    	
    	Date last = floorDate;
    	
    	for (ExportShipTimeSpan timeSpan : timeSpans) {
    		
    		
    		
    		//Draw bar
    		ig2.setColor(satBarColor);
    		long difference = timeSpan.getLastMessage()-timeSpan.getFirstMessage();
    		long diffFromFloorDate = timeSpan.getFirstMessage()-floorDate.getTime();
    		int x = (int) (Math.floor(diffFromFloorDate/1000/60)+offset);
    		int barwidth = (int) (difference/1000/60);
//    		ig2.fillRect(x, y, barwidth, (int) Math.ceil((timeSpan.getMessageCounterSat()*scale)));
    		
    		draw3dBar(x, 5, barwidth, 200, satBarColor, "");
    		
    		
    		//Draw counter label
//    		ig2.setColor(Color.black);
//    		int stringLen = (int)  ig2.getFontMetrics().getStringBounds(""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")", ig2).getWidth();
//    		ig2.drawString(""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")",x+(barwidth/2)-(stringLen/2),y-5);
    		
    		
    		//Draw width label
     		ig2.setFont(small);
     		ig2.rotate(Math.toRadians(90.0));
     		int stringLen = (int)  ig2.getFontMetrics().getStringBounds(barwidth + " min", ig2).getWidth();
     		ig2.drawString(barwidth + " min", height-bottomOffset-1-stringLen+50, (x+(barwidth/2)-4)*-1-10);	
     		ig2.setColor(Color.GRAY);
     		ig2.setTransform(orig);
    		ig2.setFont(defaultFont);
    		ig2.setColor(Color.DARK_GRAY);
    		
    		minutesCovered+=barwidth;
    		
    		//Draw label between two bars
    		if(last != null){
    			drawBetweenBarLabel(last.getTime(), timeSpan.getFirstMessage(), 10);
    		}
    		
    		last = new Date(timeSpan.getLastMessage());
    		
    		
    		

		}
    	drawBetweenBarLabel(last.getTime(), ceilDate.getTime(), 10);
    	
    	if(noOfNonContactPeriods > 0){
	    	avgBetweenBarWidth= (minutesNotCovered/noOfNonContactPeriods);
	    	coverageRatio=((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);
    	}

    	drawHeader("Ship Tracking");

    	drawDescription("Time span: ", dt.format(floorDate) +" - "+dt.format(ceilDate));
    	drawDescription("Ship MMSI: ", shipMMsi+"");
    	descriptionTopOffset+=10;
    	drawDescription("Longest non-contact period: ", maxBetweenBarWidth+" min");
    	drawDescription("Average non-contact period: ", avgBetweenBarWidth+" min");
    	drawDescription("No. of non-contact periods above 1 h: ", periodsOverOne+"");
    	drawDescription("No. of non-contact periods above 3 h: ", periodsOverThree+"");
    	drawDescription("No. of non-contact periods above 6 h: ", periodsOverSix+"");
    	
		
	}
	private void drawBetweenBarLabel(long d1, long d2, int z){
		ig2.setFont(small);
 		ig2.rotate(Math.toRadians(90.0));
 		ig2.setColor(Color.GRAY);
		int betweenBarWidth = (int) ((d2-d1)/1000/60);
		int x = (int) (Math.floor((d2-floorDate.getTime())/1000/60)+offset);
		if(betweenBarWidth > 0){
			System.out.println("between bar "+betweenBarWidth);
			System.out.println();
			int stringLen = (int)  getTextWidth(betweenBarWidth + " min");
				ig2.drawString(betweenBarWidth + " min", height-bottomOffset-1-stringLen+50, (x-(betweenBarWidth/2)-4+z)*-1);
				noOfNonContactPeriods++;
		
		
			//calculate between bar widths statistics
			if(betweenBarWidth > maxBetweenBarWidth)
				maxBetweenBarWidth=betweenBarWidth;
			
			minutesNotCovered+=betweenBarWidth; 
			
			double widthInOurs = (double)betweenBarWidth/60;
			
			if(widthInOurs > 1)
				periodsOverOne++;
			if(widthInOurs > 2)
				periodsOverTwo++;
			if(widthInOurs > 3)
				periodsOverThree++;
			if(widthInOurs > 4)
				periodsOverFour++;
			if(widthInOurs > 6)
				periodsOverSix++;
			if(widthInOurs > 8)
				periodsOverEight++;
		}
		ig2.setTransform(orig);
		ig2.setFont(defaultFont);
		ig2.setColor(Color.DARK_GRAY);
	}
}
	

