package dk.dma.ais.analysis.coverage.export;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

    private Graphics2D ig2;
    
	public void generateChart(Date startTime, Date endTime, List<TimeSpan> timeSpans){
		
	    if(timeSpans.isEmpty()){
	    	bi = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		    ig2 = bi.createGraphics();
		    ig2.setPaint(Color.black);
		    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		    ig2.drawString("No data available", 10, 10);
		    return;
	    }
//	    Date firstDate = timeSpans.get(0).getFirstMessage();
//	    Date lastDate = timeSpans.get(timeSpans.size()-1).getLastMessage();
	    
		Date floorDate = new Date(startTime.getTime());
		floorDate.setMinutes(0);
    	floorDate.setSeconds(0);
    	Date ceilDate = new Date(endTime.getTime()+1000*60*60);
    	ceilDate.setMinutes(0);
    	ceilDate.setMinutes(0);
    	long timeDifference = (long) Math.ceil((ceilDate.getTime() - floorDate.getTime())/1000/60/60); //in hours
    	
    	SimpleDateFormat dt = new SimpleDateFormat("dd-MM HH:mm");
//    	Date floorDate = new Date(startTime.getTime());
//    	Calendar.
    	
    	
    	int offset = 10;
    	int width = (int) (timeDifference*60)+offset;
    	int height = 450;
    	int bottomOffset = 55;
    	int topOffset = 100;
    	int maxBarHeight = height-bottomOffset-topOffset;
    	
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    ig2 = bi.createGraphics();
	    ig2.setPaint(Color.black);
	    ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    Font defaultFont = ig2.getFont();
	    Font small = new Font("Dialog", Font.PLAIN, 11);
//	    ig2.setFont(small);
	    AffineTransform orig = ig2.getTransform();
	    
	    int maxheight = 0;
	    //Find max height and calculate scale
	    for (TimeSpan timeSpan : timeSpans) {
	    	if(timeSpan.getMessageCounter() > maxheight){maxheight=timeSpan.getMessageCounter();}
		}
	    double scale = 1;
    	if(maxheight > maxBarHeight){
    		scale = (double)maxBarHeight/maxheight;
    	}
    	
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
    	TimeSpan last = null;
    	ig2.setColor(Color.GRAY);
    	for (TimeSpan timeSpan : timeSpans) {
    		
    		
    		
    		//Draw bar
    		long difference = timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime();
    		long diffFromFloorDate = timeSpan.getFirstMessage().getTime()-floorDate.getTime();
    		int x = (int) (Math.floor(diffFromFloorDate/1000/60)+offset);
    		int y = (int) (height-1-(timeSpan.getMessageCounter()*scale)-bottomOffset);
    		int barwidth = (int) (difference/1000/60);
    		ig2.fillRect(x, y, barwidth, (int) Math.ceil((timeSpan.getMessageCounter()*scale)));
    		
    		//Draw counter label
    		ig2.setColor(Color.black);
    		int stringLen = (int)  ig2.getFontMetrics().getStringBounds(""+timeSpan.getMessageCounter(), ig2).getWidth();
    		ig2.drawString(""+timeSpan.getMessageCounter(),x+(barwidth/2)-(stringLen/2),y-5);
    		
    		
    		//Draw width label
    		ig2.setFont(small);
    		ig2.rotate(Math.toRadians(90.0));
    		stringLen = (int)  ig2.getFontMetrics().getStringBounds(barwidth + " min", ig2).getWidth();
    		ig2.drawString(barwidth + " min", height-1-stringLen, (x+(barwidth/2)-4)*-1);	
    		ig2.setColor(Color.GRAY);
    		
    		//Draw label between two bars
    		if(last != null){
    			long betweenBarWidth = (timeSpan.getFirstMessage().getTime()-last.getLastMessage().getTime())/1000/60;
    			stringLen = (int)  ig2.getFontMetrics().getStringBounds(betweenBarWidth + " min", ig2).getWidth();
    			ig2.drawString(betweenBarWidth + " min", height-1-stringLen, (x-(betweenBarWidth/2)-4)*-1);
    		}
    		last = timeSpan;
    		ig2.setTransform(orig);
    		ig2.setFont(defaultFont);
    		
    		

		}
    	
    	//draw horizontal nd vertical line
    	ig2.setPaint(Color.black);
    	ig2.drawLine(offset, height-1-bottomOffset, width, height-1-bottomOffset);
    	ig2.drawLine(offset, 80, offset, height-1-bottomOffset);
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
	public void exportAsPNG(OutputStream o){
		try {
			ImageIO.write(bi, "PNG", o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	  public static void main(String args[]) throws Exception {
		  TimeSpan s1 = new TimeSpan(new Date());
		  s1.setLastMessage(new Date(s1.getFirstMessage().getTime()+1000*60*140));
		  s1.setMessageCounter(1000);
		  
		  TimeSpan s2 = new TimeSpan( new Date(s1.getLastMessage().getTime()+1000*60*60*1) );
		  s2.setLastMessage(new Date(s2.getFirstMessage().getTime()+1000*60*8));
		  s2.setMessageCounter(200);
  
		  List<TimeSpan> l = new ArrayList<TimeSpan>();
		  l.add(s1);
		  l.add(s2);
		  
		  ChartGenerator cg =  new ChartGenerator();
		  
		  cg.generateChart(s1.getFirstMessage(), new Date(s2.getLastMessage().getTime()+1000*60*60*24), l);
		  
	   

	  }
}
	

