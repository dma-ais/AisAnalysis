package dk.dma.ais.analysis.coverage.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QueryParams {
	public Double latStart=null, lonStart=null, latEnd=null, lonEnd=null;
	public Map<String, Boolean> sources=new HashMap<String,Boolean>();
	public Integer multiplicationFactor= null;
	public Date startDate=null, endDate=null;
}
