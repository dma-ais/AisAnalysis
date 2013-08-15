/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.analysis.coverage.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.coverage.AisCoverage;
import dk.dma.ais.analysis.coverage.AisCoverageGUI;
import dk.dma.ais.analysis.coverage.CoverageHandler;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.ICoverageData;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.analysis.coverage.data.TimeSpan;
import dk.dma.ais.analysis.coverage.data.json.JSonCoverageMap;
import dk.dma.ais.analysis.coverage.data.json.JsonConverter;
import dk.dma.ais.analysis.coverage.data.json.JsonSource;
import dk.dma.ais.analysis.coverage.data.json.Status;
import dk.dma.ais.analysis.coverage.export.CSVGenerator;
import dk.dma.ais.analysis.coverage.export.KMLGenerator;
import dk.dma.ais.analysis.coverage.export.XMLGenerator;
import dk.dma.ais.data.AisVesselTarget;

/**
 * JAX-RS rest services
 */
@Path("/")
public class CoverageRestService {

    private final CoverageHandler handler;
    private static final Logger LOG = LoggerFactory.getLogger(CoverageRestService.class);
    

    public CoverageRestService() {
        this.handler = AisCoverage.get().getHandler();
    }

    @POST
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> test(@QueryParam("q") String q) {
        Objects.requireNonNull(handler);
        Map<String, String> map = new HashMap<String, String>();
        map.put("q", q);
        LOG.debug(q);
        return map;
    }

    @GET
    @Path("test2")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> test2(@Context UriInfo uriInfo) {
        Objects.requireNonNull(handler);
        Map<String, String> map = new HashMap<String, String>();
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        for (String key : queryParams.keySet()) {
            map.put(key, queryParams.getFirst(key));
        }
        return map;
    }
    
    
    /*
     * returns the source list
     */
    @GET
    @Path("sources")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, JsonSource> sources(@Context UriInfo uriInfo) {
        Objects.requireNonNull(handler);
        LOG.info("getting sources");
		Collection<Source> sources = handler.getDistributeCalc().getDataHandler().getSources();
		return JsonConverter.toJsonSources(sources);
    }
    
    /*
     * Returns the coverage.
     * Takes a list of sources, multiplicationfactor and a geographical area as input
     */
    @GET
    @Path("coverage")
    @Produces(MediaType.APPLICATION_JSON)
    public JSonCoverageMap coverage(@Context HttpServletRequest request) {
        Objects.requireNonNull(handler);
        String sources = request.getParameter("sources");
		String area = request.getParameter("area");
		String[] areaArray = area.split(",");
		
		int multiplicationFactor = Integer.parseInt(request.getParameter("multiplicationFactor"));
		
		double latStart = Double.parseDouble(areaArray[0]);
		double lonStart = Double.parseDouble(areaArray[1]);
		double latEnd = Double.parseDouble(areaArray[2]);
		double lonEnd = Double.parseDouble(areaArray[3]);
		
		Map<String, Boolean> sourcesMap = new HashMap<String, Boolean>();
		if(sources != null){
			String[] sourcesArray = sources.split(",");	
			for (String string : sourcesArray) {
				sourcesMap.put(string, true);
			}
		}		
		
		return handler.getJsonCoverage(latStart, lonStart, latEnd, lonEnd, sourcesMap, multiplicationFactor);
    }
    
    @GET
    @Path("export")
    @Produces(MediaType.APPLICATION_JSON)
    public Object export(@QueryParam("exportType") String exportType, @QueryParam("exportMultiFactor") String exportMultiFactor, @Context HttpServletResponse response) {

		int multiplicity = Integer.parseInt(exportMultiFactor);
		
//		BaseStationHandler gh = new BaseStationHandler();
		ICoverageData dh = new OnlyMemoryData();
		
		
		Collection<Source> sources = handler.getDistributeCalc().getDataHandler().getSources();
		
//		Collection<BaseStation> superSource = covH.getSupersourceCalculator().getDataHandler().getSources();
		
		Source superbs = handler.getSupersourceCalc().getDataHandler().getSource("supersource");	
		
		for (Source bs : sources) {
			Source summedbs = dh.createSource(bs.getIdentifier());
			summedbs.setLatSize(bs.getLatSize()*multiplicity);
			summedbs.setLonSize(bs.getLonSize()*multiplicity);
			dh.setLatSize(bs.getLatSize()*multiplicity);
			dh.setLonSize(bs.getLonSize()*multiplicity);
//			BaseStation tempSource = new BaseStation(basestation.getIdentifier(), gridHandler.getLatSize()*multiplicationFactor, gridHandler.getLonSize()*multiplicationFactor);
			
			Collection<Cell> cells = bs.getGrid().values();
			
			for (Cell cell : cells)
			{
				Cell dhCell = summedbs.getCell(cell.getLatitude(), cell.getLongitude());
				if(dhCell == null)
				{
					dhCell = summedbs.createCell(cell.getLatitude(), cell.getLongitude());
				}		
				dhCell.addReceivedSignals(cell.getNOofReceivedSignals());
				dhCell.addNOofMissingSignals((superbs.getGrid().get(cell.getId()).getTotalNumberOfMessages() - cell.getNOofReceivedSignals()));
				
//				LOG.debug("cell for export created: " + summedbs.getCell(cell.getLatitude(), cell.getLongitude()).getNOofReceivedSignals() + "-" + summedbs.getCell(cell.getLatitude(), cell.getLongitude()).getNOofMissingSignals());
			}
		}
		if (exportType.equals("KML")) {
//			System.out.println(expotype);
			KMLGenerator.generateKML(dh.getSources(), dh.getLatSize(), dh.getLonSize(), multiplicity, response);
		}
		else if (exportType.equals("CSV")) {
//			System.out.println(expotype);
			CSVGenerator.generateCSV(dh.getSources(), dh.getLatSize(), dh.getLonSize(), multiplicity, response);
		}
		else if (exportType.equals("XML")) {
//			System.out.println(expotype);
			XMLGenerator.generateXML(dh.getSources(), dh.getLatSize(), dh.getLonSize(), multiplicity, response);
		}
		else
		{
			System.out.println("wrong exporttype");
		}
		return null;
    }
    
    
    @GET
    @Path("satCoverage")
    @Produces(MediaType.APPLICATION_JSON)
    public Object satCoverage(@Context HttpServletRequest request, @QueryParam("area") String area) {
    	String[] points = area.split(",");
    	if(points.length != 4){
    		LOG.warn("SatCoverage requires 2 latlon coordinates.");
    		return null;
    	}
    	LOG.info("Finding sat coverage for area: "+area);
    	double latPoint1 = Double.parseDouble(points[1]);
    	double latPoint2 = Double.parseDouble(points[3]);
    	double lonPoint1 = Double.parseDouble(points[0]);
    	double lonPoint2 = Double.parseDouble(points[2]);
    	
    	//Determine which points are which
    	double lonLeft;
    	double lonRight;
    	if(lonPoint1 < lonPoint2){
    		lonLeft = lonPoint1;
    		lonRight = lonPoint2;
    	}else{
    		lonLeft = lonPoint2;
    		lonRight = lonPoint1;
    	}
    	double latTop;
    	double latBottom;
    	if(latPoint1 < latPoint2){
    		latTop = latPoint2;
    		latBottom = latPoint1;
    	}else{
    		latTop = latPoint1;
    		latBottom = latPoint2;
    	}
    	
    	
    	
    	return JsonConverter.toJsonTimeSpan(handler.getSatCalc().getTimeSpans(latTop, lonLeft, latBottom, lonRight));
    }
    
    @GET
    @Path("satExport")
    @Produces(MediaType.APPLICATION_JSON)
    public Object satExport(@QueryParam("test") String test,  @Context HttpServletResponse response) throws IOException {
    	double latTop = 62.47;
    	double latBottom = 57.5;
    	double lonRight = -35;
    	double lonLeft = -55;
    	

    	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment; filename=" + "satexport.txt");
		ServletOutputStream out = response.getOutputStream();
		
		
		List<TimeSpan> timeSpans = handler.getSatCalc().getTimeSpans(latTop, lonLeft, latBottom, lonRight);
		TimeSpan first = null;
		TimeSpan previous = null;
		for (TimeSpan timeSpan : timeSpans) {
			if(first == null)
				first = timeSpan;
			
			long timeSinceLastTimeSpan = 0;
			if(previous != null)
				timeSinceLastTimeSpan=Math.abs(timeSpan.getFirstMessage().getTime() - previous.getLastMessage().getTime())/1000/60;
				
			//last is determined by the order of reception, but it is not guaranteed that the tag is actually the last
			//from time, to time, data time, time since last time span, accumulated time, signals, distinct ships
			String outstring = 	formatter.format(timeSpan.getFirstMessage())+","+	//from time
								formatter.format(timeSpan.getLastMessage())+","+	//to time 
								Math.abs(timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime())/1000/60+","+		//Timespan length
								timeSinceLastTimeSpan+","+		// Time since last timestamp
								Math.abs(timeSpan.getLastMessage().getTime()-first.getLastMessage().getTime())/1000/60+","+		//accumulated time
								timeSpan.getMessageCounter()+ ","+	//signals
								timeSpan.getDistinctShips().size()+	//distinct ships
								"\n";
			out.write(outstring.getBytes());
			previous=timeSpan;

		}
		out.flush();

		return null;
    }
    
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Object status() throws IOException {
    	LOG.info("getting status");
    	Date first = handler.getSupersourceCalc().getFirstMessage().getTimestamp();
    	Date last = handler.getSupersourceCalc().getCurrentMessage().getTimestamp();
    	
    	
    	Status s = new Status();
    	s.firstMessage=first.getTime();
    	s.lastMessage=last.getTime();
    	s.analysisStatus="Running";
    	return s;
    	
    }
   
}
