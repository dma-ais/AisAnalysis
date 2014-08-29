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
