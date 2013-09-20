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
import java.util.List;

import javax.imageio.ImageIO;

import dk.dma.ais.analysis.coverage.data.TimeSpan;

public class ChartGenerator {
	private BufferedImage bi;
	private static Color terBarColor = new Color(  Integer.parseInt( "6699FF",16) );
	private static Color satBarColor = new Color(  Integer.parseInt( "99CC66",16) );
	private static Color chartBackgroundColor = new Color(  Integer.parseInt( "F0F0F0",16) );

    private Graphics2D ig2;
	private int width;
	private int height;
	private int bottomOffset;
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
 	   
 		Date floorDate = new Date(startTime.getTime());
 		floorDate.setMinutes(0);
     	floorDate.setSeconds(0);
     	Date ceilDate = new Date(endTime.getTime()+1000*60*60);
     	ceilDate.setMinutes(0);
     	ceilDate.setMinutes(0);
     	long timeDifference = (long) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
     	long exactTimeDifference = (long) Math.ceil((endTime.getTime() - startTime.getTime())/1000/60); //in minutes
     	
     	SimpleDateFormat dt = new SimpleDateFormat("dd-MM HH:mm");
     	
     	
     	int offset = 300;
     	this.width = (int) (timeDifference*60)+offset+20;
     	this.height = 500;
     	this.bottomOffset = 60;
     	int topOffset = 100;
     	int maxBarHeight = height-bottomOffset-topOffset;
     	int descriptionWidth = 250;
     	int descriptionTopOffset = 110;
     	Color backgroundColor = Color.white;
     	Image logo = loadImage(new File("web\\img\\logo.png"));
     	
     	
 		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	    ig2 = bi.createGraphics();
 	    ig2.setPaint(Color.black);
 	    ig2.setColor(backgroundColor);
 	    ig2.fillRect(0, 0, width, height);
 	    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 	    Font defaultFont = ig2.getFont();
 	    Font small = new Font("Dialog", Font.PLAIN, 11);
 	    AffineTransform orig = ig2.getTransform();
 	    
 	    
 	    
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
     		
     		
     		
     		//Draw terrestrial bar
     		long difference = timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime();
     		long diffFromFloorDate = timeSpan.getFirstMessage().getTime()-floorDate.getTime();
     		int x = (int) (Math.floor(diffFromFloorDate/1000/60)+offset);
     		int y = (int) (height-1-(timeSpan.getMessageCounterTerrestrialUnfiltered()*scale)-bottomOffset);
     		int barwidth = (int) (difference/1000/60);
     		ig2.setColor(terBarColor);
     		draw3dBar(x, 5, barwidth, (int) (timeSpan.getMessageCounterTerrestrialUnfiltered()*scale), terBarColor, ""+timeSpan.getMessageCounterTerrestrialUnfiltered()+" ("+timeSpan.getDistinctShipsTerrestrial().size()+")");
//     		ig2.fillRect(x, y, barwidth, (int) Math.ceil((timeSpan.getMessageCounterTerrestrial()*scale)));
     		
     		//Draw terrestrial counter label
     		ig2.setColor(Color.black);
     		String label = ""+timeSpan.getMessageCounterTerrestrialUnfiltered()+" ("+timeSpan.getDistinctShipsTerrestrial().size()+")";
     		int stringLen = getTextWidth(label);
//     		ig2.drawString(""+timeSpan.getMessageCounterTerrestrial()+" ("+timeSpan.getDistinctShipsTerrestrial().size()+")",x+(barwidth/2)-(stringLen/2),y+20);
     		
     		
     		//Draw sat bar
     		ig2.setColor(satBarColor);
//     		y = (int) (height-1-(timeSpan.getMessageCounterSat()*scale)-bottomOffset);
     		draw3dBar(x, 0, barwidth, (int) (timeSpan.getMessageCounterSat()*scale), satBarColor, ""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")");

//     		ig2.fillRect(x, y, barwidth, (int) Math.ceil((timeSpan.getMessageCounterSat()*scale)));
     		   		
     		//Draw sat counter label
//     		ig2.setColor(Color.black);
//     		label=""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")";
//     		stringLen = getTextWidth(label);
//     		ig2.drawString(""+timeSpan.getMessageCounterSat()+" ("+timeSpan.getDistinctShipsSat().size()+")",x+(barwidth/2)-(stringLen/2),y+20);
     		
     		
     		//Draw width label
     		ig2.setFont(small);
     		ig2.rotate(Math.toRadians(90.0));
     		stringLen = (int)  ig2.getFontMetrics().getStringBounds(barwidth + " min", ig2).getWidth();
     		ig2.drawString(barwidth + " min", height-bottomOffset-1-stringLen+50, (x+(barwidth/2)-4)*-1-10);	
     		ig2.setColor(Color.GRAY);
     		
     		minutesCovered+=barwidth;
     		
     		//Draw label between two bars
     		if(last != null){
     			long betweenBarWidth = (timeSpan.getFirstMessage().getTime()-last.getLastMessage().getTime())/1000/60;
     			stringLen = (int)  ig2.getFontMetrics().getStringBounds(betweenBarWidth + " min", ig2).getWidth();
     			ig2.drawString(betweenBarWidth + " min", height-bottomOffset-1-stringLen+50, (x-(betweenBarWidth/2)-4)*-1-10);
     			
     			//calculate between bar widths statistics
     			if(betweenBarWidth > maxBetweenBarWidth)
     				maxBetweenBarWidth=betweenBarWidth;
     			
     			minutesNotCovered+=betweenBarWidth; 
     			
     			double widthInOurs = (double)betweenBarWidth/60;
         		
         		if(widthInOurs > 2)
         			barsOverTwo++;
         		if(widthInOurs > 4)
         			barsOverFour++;
         		if(widthInOurs > 8)
         			barsOverEight++;
     		}
     		last = timeSpan;
     		ig2.setTransform(orig);
     		ig2.setFont(defaultFont);
     		
     		

 		}
     	if(timeSpans.size() > 1)
     		avgBetweenBarWidth=(long) (minutesNotCovered/(timeSpans.size()-1));
     	
     	coverageRatio=((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);
     	System.out.println((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);

     	
     	
     	//draw description
     	ig2.setColor(Color.BLACK);
     	ig2.drawString("Time span: ", 10, descriptionTopOffset);
     	ig2.drawString(dt.format(floorDate) +" - "+dt.format(ceilDate), descriptionWidth-getTextWidth(dt.format(floorDate) +" - "+dt.format(ceilDate)), descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
//     	ig2.drawString("Box:", 10, descriptionTopOffset);
     	ig2.drawString("Lat-min ", 10, descriptionTopOffset);
     	ig2.drawString(round(latMin,2)+" degrees", descriptionWidth-getTextWidth(round(latMin,2)+" degrees"), descriptionTopOffset);
     	descriptionTopOffset+=20;
     	ig2.drawString("Lat-max", 10, descriptionTopOffset);
     	ig2.drawString(round(latMax,2)+" degrees", descriptionWidth-getTextWidth(round(latMax,2)+" degrees"), descriptionTopOffset);
     	descriptionTopOffset+=20;
     	ig2.drawString("Lon-min", 10, descriptionTopOffset);
     	ig2.drawString(round(lonMin,2)+" degrees", descriptionWidth-getTextWidth(round(lonMin,2)+" degrees"), descriptionTopOffset);
     	descriptionTopOffset+=20;
     	ig2.drawString("Lon-max", 10, descriptionTopOffset);
     	ig2.drawString(round(lonMax,2)+" degrees", descriptionWidth-getTextWidth(round(lonMax,2)+" degrees"), descriptionTopOffset);

     	
     	descriptionTopOffset+=30;
     	ig2.drawString("Max. \"hole\" width: ", 10, descriptionTopOffset);
     	ig2.drawString(maxBetweenBarWidth+" min", descriptionWidth-getTextWidth(maxBetweenBarWidth+" min"), descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("Avg. \"hole\" width: ", 10, descriptionTopOffset);
     	ig2.drawString(avgBetweenBarWidth+" min", descriptionWidth-getTextWidth(avgBetweenBarWidth+" min"), descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("Avg. max. \"hole\" width per day: ", 10, descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("Avg. \"hole\" width per day: ", 10, descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("Coverage ratio: ", 10, descriptionTopOffset);
     	ig2.drawString(coverageRatio+" %", descriptionWidth-getTextWidth(coverageRatio+" %"), descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("Avg. coverage ratio per day: ", 10, descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("# holes per day ( > 2 hours): ", 10, descriptionTopOffset);
     	ig2.drawString(""+barsOverTwo, descriptionWidth-getTextWidth(""+barsOverTwo), descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("# holes per day ( > 4 hours): ", 10, descriptionTopOffset);
     	ig2.drawString(""+barsOverFour, descriptionWidth-getTextWidth(""+barsOverFour), descriptionTopOffset);
     	
     	descriptionTopOffset+=20;
     	ig2.drawString("# holes per day ( > 8 hours): ", 10, descriptionTopOffset);
     	ig2.drawString(""+barsOverEight, descriptionWidth-getTextWidth(""+barsOverEight), descriptionTopOffset);

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
	    
		Date floorDate = new Date(startTime.getTime());
		floorDate.setMinutes(0);
    	floorDate.setSeconds(0);
    	Date ceilDate = new Date(endTime.getTime()+1000*60*60);
    	ceilDate.setMinutes(0);
    	ceilDate.setMinutes(0);
    	long timeDifference = (long) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
    	long exactTimeDifference = (long) Math.ceil((endTime.getTime() - startTime.getTime())/1000/60); //in minutes
    	
    	SimpleDateFormat dt = new SimpleDateFormat("dd-MM HH:mm");
    	
    	
    	int offset = 300;
    	int width = (int) (timeDifference*60)+offset;
    	int height = 450;
    	int bottomOffset = 55;
    	int topOffset = 100;
    	int maxBarHeight = height-bottomOffset-topOffset;
    	int descriptionWidth = 250;
    	int descriptionTopOffset = 110;
    	Color backgroundColor = Color.white;
    	Image logo = loadImage(new File("web\\img\\logo.png"));
    	
    	
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    ig2 = bi.createGraphics();
	    ig2.setPaint(Color.black);
	    ig2.setColor(backgroundColor);
	    ig2.fillRect(0, 0, width, height);
	    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    Font defaultFont = ig2.getFont();
	    Font small = new Font("Dialog", Font.PLAIN, 11);
	    AffineTransform orig = ig2.getTransform();
	    
	    
	    
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
//    	List<JsonTimeSpan> jt = JsonConverter.toJsonTimeSpan(timeSpans);
    	long maxBetweenBarWidth = 0;
    	long avgBetweenBarWidth = 0;
    	long minutesCovered = 0;
    	long minutesNotCovered = 0;
    	double coverageRatio = 0;
    	int barsOverTwo = 0;
    	int barsOverFour = 0;
    	int barsOverEight= 0;
    	
    	TimeSpan last = null;
    	
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
    		ig2.drawString(barwidth + " min", height-1-stringLen, (x+(barwidth/2)-4)*-1);	
    		ig2.setColor(Color.GRAY);
    		
    		minutesCovered+=barwidth;
    		
    		//Draw label between two bars
    		if(last != null){
    			long betweenBarWidth = (timeSpan.getFirstMessage().getTime()-last.getLastMessage().getTime())/1000/60;
    			stringLen = (int)  ig2.getFontMetrics().getStringBounds(betweenBarWidth + " min", ig2).getWidth();
    			ig2.drawString(betweenBarWidth + " min", height-1-stringLen, (x-(betweenBarWidth/2)-4)*-1);
    			
    			//calculate between bar widths statistics
    			if(betweenBarWidth > maxBetweenBarWidth)
    				maxBetweenBarWidth=betweenBarWidth;
    			
    			minutesNotCovered+=betweenBarWidth; 
    			
    			double widthInOurs = (double)betweenBarWidth/60;
        		
        		if(widthInOurs > 2)
        			barsOverTwo++;
        		if(widthInOurs > 4)
        			barsOverFour++;
        		if(widthInOurs > 8)
        			barsOverEight++;
    		}
    		last = timeSpan;
    		ig2.setTransform(orig);
    		ig2.setFont(defaultFont);
    		
    		

		}
    	avgBetweenBarWidth=(long) (minutesNotCovered/(timeSpans.size()-1));
    	coverageRatio=((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);
    	System.out.println((double)(Math.round((double)minutesCovered/exactTimeDifference*10000))/100);

    	//draw horizontal nd vertical line
    	ig2.setPaint(Color.black);
    	ig2.drawLine(offset, height-1-bottomOffset, width, height-1-bottomOffset);
    	ig2.drawLine(offset, 80, offset, height-1-bottomOffset);
    	
    	
    	ig2.drawString("Time span: ", 10, descriptionTopOffset);
    	ig2.drawString(dt.format(floorDate) +" - "+dt.format(ceilDate), descriptionWidth-getTextWidth(dt.format(floorDate) +" - "+dt.format(ceilDate)), descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
//    	ig2.drawString("Box:", 10, descriptionTopOffset);
    	ig2.drawString("Lat-min ", 10, descriptionTopOffset);
    	ig2.drawString(round(latMin,2)+" degrees", descriptionWidth-getTextWidth(round(latMin,2)+" degrees"), descriptionTopOffset);
    	descriptionTopOffset+=20;
    	ig2.drawString("Lat-max", 10, descriptionTopOffset);
    	ig2.drawString(round(latMax,2)+" degrees", descriptionWidth-getTextWidth(round(latMax,2)+" degrees"), descriptionTopOffset);
    	descriptionTopOffset+=20;
    	ig2.drawString("Lon-min", 10, descriptionTopOffset);
    	ig2.drawString(round(lonMin,2)+" degrees", descriptionWidth-getTextWidth(round(lonMin,2)+" degrees"), descriptionTopOffset);
    	descriptionTopOffset+=20;
    	ig2.drawString("Lon-max", 10, descriptionTopOffset);
    	ig2.drawString(round(lonMax,2)+" degrees", descriptionWidth-getTextWidth(round(lonMax,2)+" degrees"), descriptionTopOffset);

    	
    	descriptionTopOffset+=30;
    	ig2.drawString("Max. \"hole\" width: ", 10, descriptionTopOffset);
    	ig2.drawString(maxBetweenBarWidth+" min", descriptionWidth-getTextWidth(maxBetweenBarWidth+" min"), descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("Avg. \"hole\" width: ", 10, descriptionTopOffset);
    	ig2.drawString(avgBetweenBarWidth+" min", descriptionWidth-getTextWidth(avgBetweenBarWidth+" min"), descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("Avg. max. \"hole\" width per day: ", 10, descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("Avg. \"hole\" width per day: ", 10, descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("Coverage ratio: ", 10, descriptionTopOffset);
    	ig2.drawString(coverageRatio+" %", descriptionWidth-getTextWidth(coverageRatio+" %"), descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("Avg. coverage ratio per day: ", 10, descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("# holes per day ( > 2 hours): ", 10, descriptionTopOffset);
    	ig2.drawString(""+barsOverTwo, descriptionWidth-getTextWidth(""+barsOverTwo), descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("# holes per day ( > 4 hours): ", 10, descriptionTopOffset);
    	ig2.drawString(""+barsOverFour, descriptionWidth-getTextWidth(""+barsOverFour), descriptionTopOffset);
    	
    	descriptionTopOffset+=20;
    	ig2.drawString("# holes per day ( > 8 hours): ", 10, descriptionTopOffset);
    	ig2.drawString(""+barsOverEight, descriptionWidth-getTextWidth(""+barsOverEight), descriptionTopOffset);
//    	try {
//			ImageIO.write(bi, "PNG", new File("d:\\yourImageName.PNG"));
//			ImageIO.write(im, formatName, output)
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	floorDate.set
//    	floorDate.setMinutes(0);
//    	offset = new Date(array[0].fromTime).getMinutes();
//    	var accumulatedTime = offset;
//    	var maxHeight = 0;
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
}
	

