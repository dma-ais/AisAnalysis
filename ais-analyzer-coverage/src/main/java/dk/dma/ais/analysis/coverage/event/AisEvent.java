package dk.dma.ais.analysis.coverage.event;

public class AisEvent {
	
	public static enum Event {
		PROJECT_LOADED, PROJECT_CREATED, ANALYSIS_STARTED, ANALYSIS_STOPPED, 
		BS_VISIBILITY_CHANGED, BS_ADDED , BS_POSITION_FOUND, AISMESSAGE_APPROVED, AISMESSAGE_REJECTED
	}
	
	private Event event;
	private Object source;
	private Object eventObject;
	
	public AisEvent(){
	}
	
	public AisEvent(Event event, Object source, Object eventObject){
		this.event = event;
		this.source = source;
		this.eventObject = eventObject;
	}
	public Object getSource() {
		return source;
	}
	public void setSource(Object source) {
		this.source = source;
	}
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}
	public Object getEventObject() {
		return eventObject;
	}
	public void setEventObject(Object eventObject) {
		this.eventObject = eventObject;
	}

}
