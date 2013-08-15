package dk.dma.ais.analysis.coverage.data.json;

public class Status {
	public long firstMessage, lastMessage, analysisStartTime, analysisEndTime, messagesProcessed;
	public long totalMessages; 	//if source type is a file, we will know the total number of messages
								//This will most likely be an estimate, based on file size
	public String analysisStatus = "Running";
	public String sourceType = "file"; //file or stream
	
}
