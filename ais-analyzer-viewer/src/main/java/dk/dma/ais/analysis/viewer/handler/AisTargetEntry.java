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
package dk.dma.ais.analysis.viewer.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dk.dma.ais.data.AisTarget;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.packet.AisPacket;

public class AisTargetEntry {
    
    private static int anonymousCounter;
    
    // Map from MMSI to anonymous id
    private static Map<Integer, Integer> mmsiAnonIdMap = new HashMap<>();
    // Map from anonymous id to MMSI
    private static Map<Integer, Integer> anonIdMap = new HashMap<>();
    
    private Date lastReport;
    private AisTarget target;
    private final int anonId;
    private final TargetSourceData sourceData = new TargetSourceData();
    
    public AisTargetEntry(AisPacket packet) {
        AisMessage aisMessage = packet.tryGetAisMessage();        
        this.anonId = ++anonymousCounter;
        mmsiAnonIdMap.put(aisMessage.getUserId(), this.anonId);
        anonIdMap.put(this.anonId, aisMessage.getUserId());
        this.target = AisTarget.createTarget(aisMessage);
    }
    
    public boolean update(AisPacket packet) {
        boolean targetReplaced = false;
        AisMessage aisMessage = packet.tryGetAisMessage();
        sourceData.update(packet);
        // We want to avoid to update a target position with an older position
        // than the last one received
        boolean oldPos = false;
        if (aisMessage instanceof IVesselPositionMessage) {
            Date thisReport = null;
            // Get timestamp for message tag or fallback to time now
            thisReport = aisMessage.getVdm().getTimestamp();
            if (thisReport == null) {
                thisReport = new Date();
            }
            if (lastReport != null) {
                // We will not update if this report is older than last
                if (thisReport.before(lastReport)) {
                    oldPos = true;
                }
            }
            lastReport = thisReport;
        }
        // Update target data
        if (!oldPos) {
            try {
                target.update(aisMessage);
            } catch (IllegalArgumentException e) {
                // Trying to update target with report of different type of target.
                // Replace target with new target
                target = AisTarget.createTarget(aisMessage);
                target.update(aisMessage);
                targetReplaced = true;
            }
        }

        return targetReplaced;
    }

    public AisTarget getTarget() {
        return target;
    }
    
    public int getAnonId() {
        return anonId;
    }
    
    public TargetSourceData getSourceData() {
        return sourceData;
    }
    
    public static Integer getMmsi(int anonId) {
        return anonIdMap.get(anonId);
    }
    
    
}
