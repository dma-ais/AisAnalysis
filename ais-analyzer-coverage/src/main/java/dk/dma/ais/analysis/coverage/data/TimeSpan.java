package dk.dma.ais.analysis.coverage.data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeSpan {
	private Map<String, Boolean> distinctShips = new ConcurrentHashMap<String, Boolean>();
	private Date firstMessage, lastMessage;
	
	public TimeSpan(Date firstMessage){
		this.firstMessage=firstMessage;
		this.lastMessage=firstMessage;
	}
	public Map<String, Boolean> getDistinctShips() {
		return distinctShips;
	}
	public Date getFirstMessage() {
		return firstMessage;
	}
	public void setFirstMessage(Date firstMessage) {
		this.firstMessage = firstMessage;
	}
	public Date getLastMessage() {
		return lastMessage;
	}
	public void setLastMessage(Date lastMessage) {
		this.lastMessage = lastMessage;
	}
	public int getMessageCounter() {
		return messageCounter;
	}
	public void setMessageCounter(int messageCounter) {
		this.messageCounter = messageCounter;
	}
	private int messageCounter = 0;
}
