/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.analysis.viewer.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.dma.ais.analysis.common.web.QueryParams;
import dk.dma.ais.analysis.viewer.AisView;
import dk.dma.ais.analysis.viewer.handler.AisViewHandler;
import dk.dma.ais.analysis.viewer.rest.json.AisViewHandlerStats;
import dk.dma.ais.analysis.viewer.rest.json.AnonymousVesselList;
import dk.dma.ais.analysis.viewer.rest.json.BaseVesselList;
import dk.dma.ais.analysis.viewer.rest.json.VesselClusterJsonRepsonse;
import dk.dma.ais.analysis.viewer.rest.json.VesselList;
import dk.dma.ais.analysis.viewer.rest.json.VesselListJsonResponse;
import dk.dma.ais.analysis.viewer.rest.json.VesselTargetDetails;
import dk.dma.enav.model.geometry.Position;

/**
 * JAX-RS rest services
 */
@Path("/")
public class AisViewRestService {

    private final AisViewHandler handler;

    public AisViewRestService() {
        this.handler = AisView.get().getHandler();
    }

    @GET
    @Path("anon_vessel_list")
    @Produces(MediaType.APPLICATION_JSON)
    public VesselListJsonResponse anonVesselList(@Context UriInfo uriInfo) {
        QueryParams queryParams = new QueryParams(uriInfo.getQueryParameters());
        return vesselList(queryParams, true);
    }

    @GET
    @Path("vessel_list")
    @Produces(MediaType.APPLICATION_JSON)
    public VesselListJsonResponse vesselList(@Context UriInfo uriInfo) {
        QueryParams queryParams = new QueryParams(uriInfo.getQueryParameters());
        return vesselList(queryParams, handler.getConf().isAnonymous());
    }

    @GET
    @Path("vessel_clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public VesselClusterJsonRepsonse vesselClusters(@Context UriInfo uriInfo) {
        QueryParams queryParams = new QueryParams(uriInfo.getQueryParameters());
        return cluster(queryParams);
    }

    @GET
    @Path("vessel_target_details")
    @Produces(MediaType.APPLICATION_JSON)
    public VesselTargetDetails vesselTargetDetails(@Context UriInfo uriInfo) {
        QueryParams queryParams = new QueryParams(uriInfo.getQueryParameters());
        Integer id = queryParams.getInt("id");
        Integer mmsi = queryParams.getInt("mmsi");
        boolean pastTrack = queryParams.containsKey("past_track");
        VesselTargetDetails details = handler.getVesselTargetDetails(id, mmsi, pastTrack);
        if (details == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return details;
    }

    @GET
    @Path("vessel_search")
    @Produces(MediaType.APPLICATION_JSON)
    public VesselList vesselSearch(@QueryParam("argument") String argument) {
        if (handler.getConf().isAnonymous()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        // Get response from AisViewHandler and return it
        return handler.searchTargets(argument);
    }

    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    public AisViewHandlerStats stats() {
        return handler.getStat();
    }
    
    @GET
    @Path("rate")
    @Produces(MediaType.TEXT_PLAIN)
    public String rate(@QueryParam("expected") Double expected) {
        if (expected == null) {
            expected = 0.0;
        }
        return "status=" + ((handler.getStat().getRate() > expected) ? "ok" : "nok");
    }

    @GET
    @Path("kml")
    @Produces("application/vnd.google-earth.kml+xml")
    public String kml(@Context HttpServletResponse response, @Context HttpServletRequest request) throws MalformedURLException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String filename = ("aistargets-" + dateFormat.format(date) + ".kml");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        URL domain = new URL(request.getRequestURL().toString());
        String resources = "http://" + domain.getHost() + ":" + domain.getPort() + "/aisview/img/";

        return handler.generateKml(resources);
    }

    private VesselListJsonResponse vesselList(QueryParams request, boolean anonymous) {
        VesselListFilter filter = new VesselListFilter(request);
        // Get corners
        Double topLat = request.getDouble("topLat");
        Double topLon = request.getDouble("topLon");
        Double botLat = request.getDouble("botLat");
        Double botLon = request.getDouble("botLon");

        // Extract requested area
        Position pointA = null;
        Position pointB = null;

        if (topLat != null && topLon != null && botLat != null && botLon != null) {
            pointA = Position.create(topLat, topLon);
            pointB = Position.create(botLat, botLon);
        }

        // Get response from AisViewHandler and return it
        BaseVesselList list;
        if (anonymous) {
            list = new AnonymousVesselList();
        } else {
            list = new VesselList();
        }

        // Get request id
        Integer requestId = request.getInt("requestId");
        if (requestId == null) {
            requestId = -1;
        }

        return new VesselListJsonResponse(requestId, handler.getVesselList(list, filter, pointA, pointB));
    }

    private VesselClusterJsonRepsonse cluster(QueryParams request) {
        VesselListFilter filter = new VesselListFilter(request);

        // Extract cluster limit
        Integer limit = request.getInt("clusterLimit");
        if (limit == null) {
            limit = 10;
        }

        // Extract cluster size
        Double size = request.getDouble("clusterSize");
        if (size == null) {
            size = 4.0;
        }

        // Get corners
        Double topLat = request.getDouble("topLat");
        Double topLon = request.getDouble("topLon");
        Double botLat = request.getDouble("botLat");
        Double botLon = request.getDouble("botLon");

        // Extract requested area
        Position pointA = null;
        Position pointB = null;

        if (topLat != null && topLon != null && botLat != null && botLon != null) {
            pointA = Position.create(topLat, topLon);
            pointB = Position.create(botLat, botLon);
        }

        // Get request id
        Integer requestId = request.getInt("requestId");
        if (requestId == null) {
            requestId = -1;
        }

        return handler.getClusterResponse(requestId, filter, pointA, pointB, limit, size);
    }

}
