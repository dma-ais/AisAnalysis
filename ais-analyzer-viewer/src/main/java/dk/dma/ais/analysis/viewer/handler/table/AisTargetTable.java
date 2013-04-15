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

import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;
import dk.dma.ais.data.AisTarget;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.util.function.Consumer;

/**
 * Table of AIS targets with different query methods. Must be started
 * as thread to run cleanup methods.
 */
@ThreadSafe
public class AisTargetTable extends Thread implements Consumer<AisPacket> {
    
    private final AisTargetTableConfiguration conf;
    
    /**
     * Thread safe map to hold target entries
     */
    private final ConcurrentHashMap<Integer, AisTargetTableEntry> targets = new ConcurrentHashMap<>();
    
    
    public AisTargetTable(AisTargetTableConfiguration conf) {
        this.conf = conf;
    }
    
    @Override
    public void accept(AisPacket packet) {
        // Get AisMessage
        AisMessage aisMessage = packet.tryGetAisMessage();
        if (aisMessage == null) {
            return;
        }
        // We only want to handle messages containing targets data
        // #1-#3, #4, #5, #18, #21, #24
        if (!AisTarget.isTargetDataMessage(aisMessage)) {
            return;
        }
        
        
        
        
    }
    
    @Override
    public void run() {
        // TODO cleanup
    }

}
