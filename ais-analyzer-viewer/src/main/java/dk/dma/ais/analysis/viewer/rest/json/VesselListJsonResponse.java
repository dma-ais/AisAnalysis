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
