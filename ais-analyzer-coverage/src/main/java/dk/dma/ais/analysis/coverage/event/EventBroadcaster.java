package dk.dma.ais.analysis.coverage.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful for listening for AIS events, just register a IAisEventListener.
 * Other objects can broadcast events to listeners via broadcastEvent(AisEvent).
 * 
 * Get access to this by calling ProjectHandler.getInstance()
 * 
 */
public class EventBroadcaster {

	private List<IAisEventListener> listeners = new ArrayList<IAisEventListener>();
	
	public void broadcastEvent(AisEvent event){
		for (IAisEventListener listener : listeners) {
			listener.aisEventReceived(event);
		}
	}
	public void addProjectHandlerListener(IAisEventListener listener){
		listeners.add(listener);
	}
	//Singleton stuff
	private static EventBroadcaster singletonObject;
	private EventBroadcaster() {

	}
	public static synchronized EventBroadcaster getInstance() {
		if (singletonObject == null) {
			singletonObject = new EventBroadcaster();
		}
		return singletonObject;
	}
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
