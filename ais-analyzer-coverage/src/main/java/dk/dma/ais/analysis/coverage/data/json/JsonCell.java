package dk.dma.ais.analysis.coverage.data.json;

import java.io.Serializable;

public class JsonCell implements Serializable{
	public double lat;
	public double lon;
	public long nrOfRecMes;
	public long nrOfMisMes;
	public String sourceMmsi;
	
	public double getCoverage(){
		return (double) nrOfRecMes/ (double) (nrOfMisMes+nrOfRecMes);
	}

}
