package dk.dma.ais.analysis.coverage.data;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



public class SimpleDiskBasedData extends OnlyMemoryData {
	private int intervalMinutes = 15;
	private String filename = "coverageData.db";
	
	public SimpleDiskBasedData(){
		load();
		new Thread()
		{
		    public void run() {
		    	while(true){	
		    		try {
						Thread.sleep(intervalMinutes*60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    		save();
		    	}
		        
		    }
		}.start();

	}
	private void load(){
		long starttime = System.currentTimeMillis();
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
			BaseStationHandler handler = (BaseStationHandler) in.readObject();
			this.gridHandler = handler;
			System.out.println("DB loaded in " + (System.currentTimeMillis()-starttime));
		} catch (Exception e) {
			System.out.println("Using new DB");
			System.out.println(e.getMessage());
		} 
		
	}
	private void save(){
		long starttime = System.currentTimeMillis();
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(this.gridHandler);
			out.close();
			
			System.out.println("Project saved in " + (System.currentTimeMillis()-starttime) );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SimpleDiskBasedData();

	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
