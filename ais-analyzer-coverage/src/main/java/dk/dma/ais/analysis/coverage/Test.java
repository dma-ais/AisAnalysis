package dk.dma.ais.analysis.coverage;

import java.util.Date;
import java.util.Random;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long nrofmessages = 280000000;
		long[] messages = new long[(int) (nrofmessages*2)];
		Random rand = new Random();
		for (int i = 0; i < messages.length; i++) {
			
			messages[i] = rand.nextLong();
			
		}
		Date then = new Date();
		long last = 0;
		int counter=0;
		for (int i = 0; i < messages.length; i++) {
		
			if(messages[i] > last && messages[i+1] < 90 && messages[i+1] > 12){
				counter++;
			}
			last = messages[i];
			i++;
		}
		Date now = new Date();
		System.out.println((((now.getTime()-then.getTime()))));
//		System.out.println(counter);
		
//		byte
		// TODO Auto-generated method stub

	}

}
