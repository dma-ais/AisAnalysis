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

import java.io.Serializable;
import java.util.Collection;

import dk.dma.ais.analysis.viewer.handler.AisTargetEntry;
import dk.dma.ais.data.AisTarget;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.ais.data.IPastTrack;

public class AisViewHandlerStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalTargets = 0;
    private int pastTrackTargets = 0;
    private int vesselTargets = 0;
    private int pastTrackPoints = 0;
    private double rate = 0.0;

    public AisViewHandlerStats(Collection<AisTargetEntry> targets, Collection<IPastTrack> pastTracks, double rate) {
        this.rate = rate;
        totalTargets = targets.size();
        // Go through all targets
        for (AisTargetEntry targetEntry : targets) {
            AisTarget target = targetEntry.getTarget();                   
            if (target instanceof AisVesselTarget) {
                vesselTargets++;
            }
        }
        pastTrackTargets = pastTracks.size();
        for (IPastTrack pastTrack : pastTracks) {
            pastTrackPoints += pastTrack.getPoints().size();
        }
    }

    public int getTotalTargets() {
        return totalTargets;
    }

    public int getPastTrackTargets() {
        return pastTrackTargets;
    }

    public int getVesselTargets() {
        return vesselTargets;
    }

    public int getPastTrackPoints() {
        return pastTrackPoints;
    }
    
    public double getRate() {
        return rate;
    }

}
