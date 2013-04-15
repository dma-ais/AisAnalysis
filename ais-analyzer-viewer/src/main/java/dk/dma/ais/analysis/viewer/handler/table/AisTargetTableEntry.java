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

import java.util.Date;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import dk.dma.ais.data.AisTarget;

@ThreadSafe
public class AisTargetTableEntry {
    
    @GuardedBy("AisTargetTableEntry.class")
    private static int anonymousCounter;
    
    @GuardedBy("this")
    private final AisTarget target;
    @GuardedBy("this")
    private Date lastPosReport;
    @GuardedBy("this")
    private final int anonId;
    
    
    public AisTargetTableEntry(AisTarget target) {
        this.target = target;
        synchronized (AisTargetTableEntry.class) {
            this.anonId = ++anonymousCounter;
        }
        
    }
    
    // Match filter methods
    
    public int getAnonId() {
        return anonId;
    }
    
    
}
