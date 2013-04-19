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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketTagging;
import dk.dma.ais.packet.AisPacketTagging.SourceType;
import dk.dma.ais.proprietary.GatehouseSourceTag;
import dk.dma.ais.proprietary.IProprietaryTag;
import dk.dma.enav.model.Country;

/**
 * Class to data about the source of an AIS target
 */
public class TargetSourceData implements Serializable {

    private static final long serialVersionUID = 1L;

    private AisPacketTagging lastTagging = new AisPacketTagging();
    private String lastSourceRegion;
    private final Date created;
    
    /**
     * Map from source type to last time of reception
     */
    private final Map<String, Long> sourceTypeTime = new HashMap<>();
    
    /**
     * Map from source country to last time of reception
     */
    private final Map<String, Long> sourceCountryTime = new HashMap<>();
    
    /**
     * Map from source region to last time of reception
     */
    private final Map<String, Long> sourceRegionTime = new HashMap<>();
    
    /**
     * Map from source base station to last time of reception
     */    
    private final Map<String, Long> sourceBsTime = new HashMap<>();
    
    /**
     * Map from source system to last time of reception
     */    
    private final Map<String, Long> sourceSystemTime = new HashMap<>();
    
    
    public TargetSourceData() {
        this.created = new Date();
    }

    public void update(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();
        if (message == null) {
            return;
        }
        
        this.lastSourceRegion = null;
        // Get source region from Gatehouse tag
        if (message.getTags() != null) {
            for (IProprietaryTag tag : message.getTags()) {
                if (tag instanceof GatehouseSourceTag) {
                    GatehouseSourceTag ghTag = (GatehouseSourceTag) tag;
                    this.lastSourceRegion = ghTag.getRegion();
                }
            }
        }
        this.lastTagging = AisPacketTagging.parse(message.getVdm());
        
        // Update times of reception of time
        Long now = System.currentTimeMillis();
        
        SourceType sourceType = lastTagging.getSourceType();
        if (sourceType == null) {
            sourceType = SourceType.TERRESTRIAL;
        }
        sourceTypeTime.put(sourceType.encode(), now);
        
        Country srcCnt = lastTagging.getSourceCountry();
        if (srcCnt != null) {
            sourceCountryTime.put(srcCnt.getThreeLetter(), now);
        }
        
        if (lastSourceRegion != null) {
            sourceRegionTime.put(lastSourceRegion, now);
        }
        
        if (lastTagging.getSourceBs() != null) {
            sourceBsTime.put(Integer.toString(lastTagging.getSourceBs()), now);
        }
        
        if (lastTagging.getSourceId() != null) {
            sourceSystemTime.put(lastTagging.getSourceId(), now);
        }
        
    }

    public AisPacketTagging getTagging() {
        return lastTagging;
    }

    public void setTagging(AisPacketTagging tagging) {
        this.lastTagging = tagging;
    }

    public String getSourceRegion() {
        return lastSourceRegion;
    }

    public void setSourceRegion(String sourceRegion) {
        this.lastSourceRegion = sourceRegion;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isSatData() {
        SourceType sourceType = lastTagging.getSourceType();
        return sourceType != null && sourceType == SourceType.SATELLITE;
    }
    
    public String getSourceType() {
        SourceType sourceType = lastTagging.getSourceType();
        if (sourceType != null && sourceType == SourceType.SATELLITE) {
            return "SAT";
        } else {
            return "LIVE";
        }
    }

    public boolean isSourceType(String st, int liveTargetTtl, int satTargetTtl) {
        SourceType sourceType = SourceType.fromString(st);
        if (sourceType == null) {
            return false;
        }
        int ttl = liveTargetTtl;
        if (sourceType == SourceType.SATELLITE) {
            ttl = satTargetTtl;                    
        }
        
        return isFresh(sourceTypeTime, st, ttl);
    }

    public boolean isCountry(String cnt, int ttl) {
        return isFresh(sourceCountryTime, cnt, ttl);
    }
    
    public boolean isRegion(String region, int ttl) {
        return isFresh(sourceRegionTime, region, ttl);
    }
    
    public boolean isBs(String bs, int ttl) {
        return isFresh(sourceBsTime, bs, ttl);
    }
    
    public boolean isSystem(String sys, int ttl) {
        return isFresh(sourceSystemTime, sys, ttl);
    }

    private static boolean isFresh(Map<String, Long> timeMap, String key, int ttl) {
        Long last = timeMap.get(key);
        if (last == null) {
            last = 0L;
        }
        long elapsed = System.currentTimeMillis() - last;
        return elapsed < ttl * 1000;
    }

}
