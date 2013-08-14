package dk.dma.ais.analysis.coverage.timewindow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
	
	
	
	
	public static void save(String foldername, String filename, XMLExporter timespan) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(XMLExporter.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        
        File folderExistCheck = new File("src/main/resources/"+foldername);
        folderExistCheck.mkdir();	
        
        ZipOutputStream zos = null; 
        try {
          zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("src/main/resources/"+foldername+"/"+filename+".zip")));
          // add zip-entry descriptor
          ZipEntry ze1 = new ZipEntry(filename+".xml");
          zos.putNextEntry(ze1);
          // add zip-entry data
          m.marshal(timespan, zos);
//          marshaller.marshal(jaxbElement1, zos);
//          ZipEntry ze2 = new ZipEntry("xml-file-2.xml");
//          zos.putNextEntry(ze2);
//          marshaller.marshal(jaxbElement2, zos);
          zos.flush();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
          if (zos != null) {
            try {
				zos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          }
        }
        
//        m.marshal(timespan, new FileOutputStream(new File("src/main/resources/"+foldername+"/"+filename)));
//        m.marshal(timespan, new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(new File("src/main/resources/"+foldername+"/"+filename)))));
    }

    public static AisCoverageConfiguration load(String filename) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AisCoverageConfiguration.class);
        Unmarshaller um = context.createUnmarshaller();
        return (AisCoverageConfiguration) um.unmarshal(new FileInputStream(new File(filename)));
    }
}
