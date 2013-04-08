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
package dk.dma.ais.analysis.viewer.rest.handler;

import dk.dma.ais.analysis.common.web.QueryParams;
import dk.dma.ais.analysis.viewer.handler.AisViewHandler;
import dk.dma.ais.analysis.viewer.rest.json.VesselClusterJsonRepsonse;
import dk.dma.enav.model.geometry.Position;

/**
 * JSON API for requesting a list of clusters.
 */
public class VesselClusterHandler {
    
    public static VesselClusterJsonRepsonse handle(QueryParams request, AisViewHandler handler) {        
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
        if (requestId == null){
            requestId = -1;
        }
                
        return handler.getClusterResponse(requestId, filter, pointA, pointB, limit, size);
    }

}
