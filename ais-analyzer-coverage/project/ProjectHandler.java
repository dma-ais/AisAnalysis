package dk.dma.aiscoverage.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import dk.dma.aiscoverage.event.AisEvent;
import dk.dma.aiscoverage.event.IAisEventListener;

/**
 * This singleton object gives access to the project instance and
 * makes it possible to save and load a project.
 * 
 * It's also useful for listening for project events, just register a ProjectHandlerListener.
 * Other objects can broadcast events to listeners via broadcastEvent(AisEvent).
 * 
 * Get access to this by calling ProjectHandler.getInstance()
 * 
 */
public class ProjectHandler {

	private List<IAisEventListener> listeners = new ArrayList<IAisEventListener>();
	private AisCoverageProject project = null;
	
	private void terminateProject(){
		if(project != null){
			if(this.project.isRunning()){
				this.project.stopAnalysis();
			}
		}
		project = null;
	}
	public void broadcastEvent(AisEvent event){
		for (IAisEventListener listener : listeners) {
			listener.aisEventReceived(event);
		}
	}
	
	public AisCoverageProject createProject(){
		terminateProject();
		this.project = new AisCoverageProject();
		
		AisEvent event = new AisEvent();
		event.setEvent(AisEvent.Event.PROJECT_CREATED);
		event.setSource(project);
		broadcastEvent(event);
			
		return project;
	}

	public void addProjectHandlerListener(IAisEventListener listener){
		listeners.add(listener);
	}
	public void saveProject(AisCoverageProject project, String filename){
		try {
			FileOutputStream saveFile = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(saveFile);
			out.writeObject(project);
			out.close();
			
			System.out.println("Project saved");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public AisCoverageProject loadProject(String filename){
		try {
			terminateProject();
			
			FileInputStream loadFile = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(loadFile);
			AisCoverageProject project = (AisCoverageProject) in.readObject();
			in.close();

			this.project = project;
			AisEvent event = new AisEvent();
			event.setEvent(AisEvent.Event.PROJECT_LOADED);
			event.setSource(project);
			broadcastEvent(event);
			
			return project;
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	public AisCoverageProject getProject() {
		return project;
	}
	
	//Singleton stuff
	private static ProjectHandler singletonObject;

	private ProjectHandler() {

	}
		
	public static synchronized ProjectHandler getInstance() {
		if (singletonObject == null) {
			singletonObject = new ProjectHandler();
		}
		return singletonObject;
	}
		
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
