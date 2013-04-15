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
package dk.dma.ais.analysis.viewer.handler.table;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AisTargetTableConfiguration {

    private int liveTargetTtl = 1200; // 20 min
    private int satTargetTtl = 172800; // 48 hours
    private int cleanupInterval = 600; // 10 minutes
    private int cleanupTtl = 43200; // 12 hours

    // TODO past track as own component
//    private boolean recordPastTrack = false;
//    private int pastTrackMinDist = 100; // 100 meters
//    private int pastTrackLiveTtl = 3600; // 1 hour
//    private int pastTrackSatTtl = 3600; // 1 hour

    public AisTargetTableConfiguration() {

    }

    public int getLiveTargetTtl() {
        return liveTargetTtl;
    }

    public void setLiveTargetTtl(int liveTargetTtl) {
        this.liveTargetTtl = liveTargetTtl;
    }

    public int getSatTargetTtl() {
        return satTargetTtl;
    }

    public void setSatTargetTtl(int satTargetTtl) {
        this.satTargetTtl = satTargetTtl;
    }

    public int getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(int cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public int getCleanupTtl() {
        return cleanupTtl;
    }

    public void setCleanupTtl(int cleanupTtl) {
        this.cleanupTtl = cleanupTtl;
    }

}
