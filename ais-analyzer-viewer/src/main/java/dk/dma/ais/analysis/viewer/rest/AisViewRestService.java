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
package dk.dma.ais.analysis.viewer.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import dk.dma.ais.analysis.viewer.AisView;
import dk.dma.ais.analysis.viewer.AisViewerHandler;

/**
 * JAX-RS rest services
 */
@Path("/")
public class AisViewRestService {

    private final AisViewerHandler handler;

    public AisViewRestService() {
        this.handler = AisView.get().getHandler();
    }

    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> test(@QueryParam("q") String q) {
        Objects.requireNonNull(handler);
        Map<String, String> map = new HashMap<String, String>();
        map.put("q", q);
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

}