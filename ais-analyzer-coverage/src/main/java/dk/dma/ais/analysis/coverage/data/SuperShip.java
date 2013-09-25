package dk.dma.ais.analysis.coverage.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dk.dma.ais.analysis.coverage.calculator.geotools.Helper;

/**
 * There should be only one instance of this per ship
 * Used to store signals from the ship and be able to
 * see periods where the ship is not covered by any sources.
 *
 */
public class SuperShip {

	//Hours since start time of analysis
	private Map<Short, Hour> hours = new HashMap<Short, Hour>();
	public Map<Short, Hour> getHours() {
		return hours;
	}
	public void registerMessage(Date timestamp, float lat, float lon){
		int minutesSince = (int) ((timestamp.getTime()-Helper.analysisStarted.getTime())/1000/60);
		short hoursSince = (short) ((Helper.getFloorDate(timestamp).getTime()-Helper.analysisStarted.getTime())/1000/60/60);
		int minutesOffset = minutesSince-hoursSince*60;
		if(minutesSince < 0)return;
		
		Hour hour = hours.get(hoursSince);
		if(hour==null){
			hour = new Hour();
			hours.put(hoursSince, hour);
		}
		hour.setPosition(minutesOffset, lat, lon);
		
	}
	public static void main(String[] args){
		SuperShip ss = new SuperShip();
		Hour h = ss.new Hour();
		h.setPosition(0, 57.7819f, 2.8765f);
		h.setPosition(0, 57.8819f, 2.8765f);
		h.setPosition(13, 57.58194683f, 2.63f);
//		for (int i = 0; i < 60; i++) {
//			System.out.println(i+ " "+h.gotSignal(i));
//		}
		System.out.println(h.getLat(0));
		System.out.println(h.getLon(9));
		
	}


	
	public class Hour{
		
		//Position offset 
		float latOffset;
		float lonOffset;
		
		//Positions per 10th minute in 10-meters from offset 
		short[] positions = new short[6*2]; 
		
		//Two integers represents 60 bits (60 minutes)
		//Indicating if a message was received in the corresponding minute
		public int half1=0;
		public int half2=0;
		
		/**
		 * Sets the position at the given minute
		 * A position must not be more than 32,767*10 meters from offset
		 * We assume no ships travel more than 328 km within an hour
		 * In this way we can store a position using 2 shorts isntead of 2 floats
		 * @param minute
		 * @param lat
		 * @param lon
		 */
		public void setPosition(int minute, float lat, float lon){
			
			//Set bit-flag at minute 
			if(minute < 30){
				half1 |= 1 << minute;
			}else{
				half2 |= 1 << (minute-30);
			}
			
			//If lat-lon offset has not been set, set it
			if(latOffset == 0){
				latOffset=lat;
				lonOffset=lon;	
			}
			
			double p1X = Helper.getProjection().lon2x(lonOffset, latOffset);
			double p1Y = Helper.getProjection().lat2y(lonOffset, latOffset);
			double p2X = Helper.getProjection().lon2x(lon, lat);
			double p2Y = Helper.getProjection().lat2y(lon, lat);
			short xDistance=(short) ((p1X-p2X)/10); //Distance to offset in xDistance*10 meters
			short yDistance=(short) ((p1Y-p2Y)/10); //Distance to offset in yDistance*10 meters
			if(xDistance == 0)xDistance=1;
			if(yDistance == 0)xDistance=1;
			if(xDistance > 32767 || yDistance > 32767) return; //Something wrong with this message

			//Set meters from offset at the right position field
			if(minute < 5){
				positions[0]=xDistance;
				positions[1]=yDistance;
			}else if(minute < 15){
				positions[2]=xDistance;
				positions[3]=yDistance;
			}else if(minute < 25){
				positions[4]=xDistance;
				positions[5]=yDistance;
			}else if(minute < 35){
				positions[6]=xDistance;
				positions[7]=yDistance;
			}else if(minute < 45){
				positions[8]=xDistance;
				positions[9]=yDistance;
			}else if(minute < 55){
				positions[10]=xDistance;
				positions[11]=yDistance;
			}
		}
		public float getLat(int minute){
			double p1X = Helper.getProjection().lon2x(lonOffset, latOffset);
			double p1Y = Helper.getProjection().lat2y(lonOffset, latOffset);
			if(minute < 5){
				if(positions[0]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[0]*10) , p1Y-(positions[1]*10));
			}else if(minute < 15){
				if(positions[2]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[2]*10) , p1Y-(positions[3]*10));
			}else if(minute < 25){
				if(positions[4]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[4]*10) , p1Y-(positions[5]*10));
			}else if(minute < 35){
				if(positions[6]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[6]*10) , p1Y-(positions[7]*10));
			}else if(minute < 45){
				if(positions[8]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[8]*10) , p1Y-(positions[9]*10));
			}else if(minute < 55){
				if(positions[10]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[10]*10) , p1Y-(positions[11]*10));
			}else{
				if(positions[10]==0)return 0;
				return (float) Helper.getProjection().y2Lat( p1X-(positions[10]*10) , p1Y-(positions[11]*10));
			}
		}
		public float getLon(int minute){
			double p1X = Helper.getProjection().lon2x(lonOffset, latOffset);
			double p1Y = Helper.getProjection().lat2y(lonOffset, latOffset);
			if(minute < 5){
				if(positions[0]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[0]*10) , p1Y-(positions[1]*10));
			}else if(minute < 15){
				if(positions[2]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[2]*10) , p1Y-(positions[3]*10));
			}else if(minute < 25){
				if(positions[4]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[4]*10) , p1Y-(positions[5]*10));
			}else if(minute < 35){
				if(positions[6]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[6]*10) , p1Y-(positions[7]*10));
			}else if(minute < 45){
				if(positions[8]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[8]*10) , p1Y-(positions[9]*10));
			}else if(minute < 55){
				if(positions[10]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[10]*10) , p1Y-(positions[11]*10));
			}else{
				if(positions[10]==0)return 0;
				return (float) Helper.getProjection().x2Lon( p1X-(positions[10]*10) , p1Y-(positions[11]*10));
			}
		}
		public boolean gotSignal(int minute){
			if(minute < 30){
				if( (half1 & (1 << minute)) == 0 )return false;
				return true;
			}else{
				minute=minute-30;
				if( (half2 & (1 << minute)) == 0 )return false;
				return true;
			}
		}
	}
}

