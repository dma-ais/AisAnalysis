package dk.dma.ais.analysis.coverage.data.json;

import java.io.Serializable;
import java.util.Map;

public class JSonCoverageMap implements Serializable {
	public double latSize;
	public double lonSize;
	public Map<String,JsonCell> cells;
}
