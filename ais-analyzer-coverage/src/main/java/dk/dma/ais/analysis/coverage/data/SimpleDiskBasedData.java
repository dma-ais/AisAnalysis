package dk.dma.ais.analysis.coverage.data;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.coverage.AisCoverageGUI;



public class SimpleDiskBasedData extends OnlyMemoryData {
	private int intervalMinutes = 15;
	private String filename = "coverageData.db";
	private static final Logger LOG = LoggerFactory.getLogger(SimpleDiskBasedData.class);
	
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
//		    		LOG.info("data saved to disk");
		    	}
		        
		    }
		}.start();

	}
	private void load(){
		long starttime = System.currentTimeMillis();
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
			SourceHandler handler = (SourceHandler) in.readObject();
			this.gridHandler = handler;
			LOG.info("DB loaded in " + (System.currentTimeMillis()-starttime));
		} catch (Exception e) {
			LOG.info("DB not found, using new DB");
			LOG.error(e.getMessage());
		} 
		
	}
	private void save(){
		long starttime = System.currentTimeMillis();
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(this.gridHandler);
			out.close();
			LOG.info("project saved in " + (System.currentTimeMillis()-starttime));
		} catch (IOException e) {
			LOG.error(e.getMessage());
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
