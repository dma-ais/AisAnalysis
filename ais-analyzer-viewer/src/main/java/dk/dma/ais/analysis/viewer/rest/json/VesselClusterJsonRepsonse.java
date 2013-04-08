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
package dk.dma.ais.analysis.viewer.rest.json;

import java.util.ArrayList;


/**
 * A Json response consisting of a list of vessel clusters.
 */
public class VesselClusterJsonRepsonse extends JsonResponse{

    private ArrayList<VesselCluster> clusters;
    
    /**
     * Constructor of VesselClusterJsonRepsonse.
     * @param requestId 
     *             The id of the json request.
     * @param clusters
     *             A list of vessels clusters.
     * @param vesselsInWorld
     *             The number of known vessels.
     */
    public VesselClusterJsonRepsonse(int requestId, ArrayList<VesselCluster> clusters, int vesselsInWorld) {
        super(requestId, vesselsInWorld);
        this.clusters = clusters;
    }

    public ArrayList<VesselCluster> getClusters() {
        return clusters;
    }

    public void setClusters(ArrayList<VesselCluster> clusters) {
        this.clusters = clusters;
    }
    
}
