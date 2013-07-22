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

import dk.dma.ais.analysis.coverage.AisCoverage;
import dk.dma.ais.analysis.coverage.CoverageHandler;
import dk.dma.ais.analysis.coverage.data.Source;
import dk.dma.ais.analysis.coverage.data.Cell;
import dk.dma.ais.analysis.coverage.data.ICoverageData;
import dk.dma.ais.analysis.coverage.data.OnlyMemoryData;
import dk.dma.ais.analysis.coverage.data.TimeSpan;
import dk.dma.ais.analysis.coverage.data.json.JSonCoverageMap;
import dk.dma.ais.analysis.coverage.data.json.JsonConverter;
import dk.dma.ais.analysis.coverage.data.json.JsonSource;
import dk.dma.ais.analysis.coverage.export.KMLGenerator;
import dk.dma.ais.data.AisVesselTarget;

/**
 * JAX-RS rest services
 */
@Path("/")
public class CoverageRestService {

    private final CoverageHandler handler;
    

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
        System.out.println(q);
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
        System.out.println("get sources");
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
		
//		System.out.println(multiplicationFactor);
		
		double latStart = Double.parseDouble(areaArray[0]);
		double lonStart = Double.parseDouble(areaArray[1]);
		double latEnd = Double.parseDouble(areaArray[2]);
		double lonEnd = Double.parseDouble(areaArray[3]);
		
		Map<String, Boolean> sourcesMap = new HashMap<String, Boolean>();
		if(sources != null){
			String[] sourcesArray = sources.split(",");	
			for (String string : sourcesArray) {
				sourcesMap.put(string, true);
//				System.out.println(string);
			}
		}		
		
		return handler.getJsonCoverage(latStart, lonStart, latEnd, lonEnd, sourcesMap, multiplicationFactor);
    }
    
    @GET
    @Path("export")
    @Produces(MediaType.APPLICATION_JSON)
    public Object export(@QueryParam("exportType") String exportType, @QueryParam("exportMultiFactor") String exportMultiFactor, @Context HttpServletResponse response) {
    	System.out.println("we need to return the KML");


		int multiplicity = Integer.parseInt(exportMultiFactor);
		
		
//		BaseStationHandler gh = new BaseStationHandler();
		ICoverageData dh = new OnlyMemoryData();
		
		
		Collection<Source> sources = handler.getDistributeCalc().getDataHandler().getSources();
		
//		Collection<BaseStation> superSource = covH.getSupersourceCalculator().getDataHandler().getSources();
		
		Source superbs = handler.getSupersourceCalc().getDataHandler().getSource("supersource");	
		
		System.out.println("super source loaded " + superbs.getLatSize());
		
		System.out.println(sources.size());
		
		
		for (Source bs : sources) {
			Source summedbs = dh.createSource(bs.getIdentifier());
			summedbs.setLatSize(bs.getLatSize()*multiplicity);
			summedbs.setLonSize(bs.getLonSize()*multiplicity);
			dh.setLatSize(bs.getLatSize()*multiplicity);
			dh.setLonSize(bs.getLonSize()*multiplicity);
//			System.out.println(summedbs.getLatSize());
//			BaseStation tempSource = new BaseStation(basestation.getIdentifier(), gridHandler.getLatSize()*multiplicationFactor, gridHandler.getLonSize()*multiplicationFactor);
			
			
//			System.out.println("source created: " + summedbs.getIdentifier());
			
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
				
				//debug printing
//				System.out.println("cell for export created: " + summedbs.getCell(cell.getLatitude(), cell.getLongitude()).getNOofReceivedSignals() + "-" + summedbs.getCell(cell.getLatitude(), cell.getLongitude()).getNOofMissingSignals());
				
			}
			
//			System.out.println(summedbs.getGrid().size());
		}
		
		System.out.println(dh.getSources().size());

		
		//TODO print cells to kml with larger cellsize then the standart one (multiplication factor)
	
		KMLGenerator.generateKML(dh.getSources(), dh.getLatSize(), dh.getLonSize(), response);
		return null;
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
		Date first = null;
		for (TimeSpan timeSpan : timeSpans) {
			if(first == null)
				first = timeSpan.getFirstMessage();
			
			//from time, to time, data time, time since last package, accumulated time, signals, distinct ships
			String outstring = 	formatter.format(timeSpan.getFirstMessage())+","+
								formatter.format(timeSpan.getLastMessage())+","+	//last is determined by the order of receival, but it is not guaranteed that the tag is actually the last
								Math.abs(timeSpan.getLastMessage().getTime()-timeSpan.getFirstMessage().getTime())/1000/60+","+
								Math.abs(new Date().getTime()-timeSpan.getLastMessage().getTime())/1000/60+","+
								Math.abs(timeSpan.getLastMessage().getTime()-first.getTime())/1000/60+","+
								timeSpan.getMessageCounter()+ ","+
								timeSpan.getDistinctShips().size()+
								"\n";
			out.write(outstring.getBytes());

		}
		out.flush();

		return null;
    }
    
   
}
