package dk.dma.ais.analysis.coverage.timewindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import dk.dma.ais.analysis.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.analysis.coverage.data.ICoverageData;
import dk.dma.ais.analysis.coverage.data.MarshallSource;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.SourceHandler;

@XmlRootElement
public class XMLExporter {

//	private ICoverageData dh = new OnlyMemoryData();
//	protected SourceHandler gridHandler;
	private double latSize;
	private double lonSize;
	private Collection<MarshallSource> baseStations;
	
	public double getLatSize() {
		return latSize;
	}
	public void setLatSize(double latSize) {
		this.latSize = latSize;
	}
	public double getLonSize() {
		return lonSize;
	}
	public void setLonSize(double lonSize) {
		this.lonSize = lonSize;
	}

	
	
	
	
	
	
	public Collection<MarshallSource> getBaseStations() {
		return baseStations;
	}
	public void setBaseStations(Collection<MarshallSource> baseStations) {
		this.baseStations = baseStations;
	}
	
	
	
	
	public static void save(String filename, XMLExporter timespan) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(XMLExporter.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.marshal(timespan, new FileOutputStream(new File(filename)));
    }

    public static AisCoverageConfiguration load(String filename) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AisCoverageConfiguration.class);
        Unmarshaller um = context.createUnmarshaller();
        return (AisCoverageConfiguration) um.unmarshal(new FileInputStream(new File(filename)));
    }
}
