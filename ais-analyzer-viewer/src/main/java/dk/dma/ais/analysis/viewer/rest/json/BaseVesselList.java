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
import java.util.HashMap;

import dk.dma.ais.data.AisVesselTarget;

public abstract class BaseVesselList {
    
    protected HashMap<Integer, ArrayList<String>> vessels = new HashMap<Integer, ArrayList<String>>();
    protected long currentTime;
    protected int vesselCount = 0;
    protected int inWorldCount = 0;
    
    protected static ShipTypeMapper shipTypeMapper = ShipTypeMapper.getInstance();
    
    public BaseVesselList() {
        currentTime = System.currentTimeMillis();
    }
    
    public abstract void addTarget(AisVesselTarget vesselTarget, int anonId);
    
    public HashMap<Integer, ArrayList<String>> getShips() {
        return vessels;
    }
    
    public long getCurrentTime() {
        return currentTime;
    }
    
    public int getVesselCount() {
        return vesselCount;
    }
    
    public int getInWorldCount() {
        return inWorldCount;
    }
    public void setInWorldCount(int inWorldCount) {
        this.inWorldCount = inWorldCount;
    }
    
}
