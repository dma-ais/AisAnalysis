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


/**
 * A Json response consisting of a list of vessels.
 */
public class VesselListJsonResponse extends JsonResponse {
    
    private BaseVesselList vesselList;
    
    /**
     * Constructor of VesselListJsonResponse.
     * @param requestId 
     *             The id of the json request.
     * @param vesselsList
     *             An BaseVesselList of vessels.
     */
    public VesselListJsonResponse(int requestId, BaseVesselList vesselList){
        super(requestId, vesselList.getInWorldCount());
        this.vesselList = vesselList;
    }

    public BaseVesselList getVesselList() {
        return vesselList;
    }
    public void setVesselList(AnonymousVesselList vesselList) {
        this.vesselList = vesselList;
    }

}
