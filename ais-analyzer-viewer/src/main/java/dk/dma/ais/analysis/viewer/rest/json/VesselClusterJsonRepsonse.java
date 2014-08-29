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
