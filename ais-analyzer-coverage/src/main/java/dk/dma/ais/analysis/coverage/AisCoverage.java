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
package dk.dma.ais.analysis.coverage;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.common.web.WebServer;
import dk.dma.ais.analysis.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;
import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.util.function.Consumer;

/**
 * AIS coverage analyzer
 */
@ThreadSafe
public class AisCoverage {

    private static final Logger LOG = LoggerFactory.getLogger(AisCoverage.class);
    
    @GuardedBy("AisCoverage")
    private static AisCoverage instance;

    private final AisCoverageConfiguration conf;
    private final CoverageHandler handler;
    private final AisBus aisBus;
    private final WebServer webServer;

    private AisCoverage(AisCoverageConfiguration conf) {
        this.conf = conf;

        // Create handler
        handler = new CoverageHandler(conf);

        // Create AisBus
        aisBus = conf.getAisbusConfiguration().getInstance();
        
        // Create web server
        if (conf.getServerConfiguration() != null) {
            webServer = new WebServer(conf.getServerConfiguration());
        } else {
            webServer = null;
        }

        // Get distributers
//        final DistributerConsumer filteredConsumer = (DistributerConsumer) aisBus.getConsumer("FILTERED");
//        if (filteredConsumer == null) {
//            LOG.error("Could not find distributer with name: FILTERED");
//            return;
//        }
        final DistributerConsumer unfilteredConsumer = (DistributerConsumer) aisBus.getConsumer("UNFILTERED");
        if (unfilteredConsumer == null) {
            LOG.error("Could not find distributer with name: UNFILTERED");
            return;
        }

        // Delegate filtered packets to handler
//        filteredConsumer.getConsumers().add(new Consumer<AisPacket>() {
//            @Override
//            public void accept(AisPacket packet) {
//                handler.receiveFiltered(packet);
//            }
//        });

        // Delegate unfiltered packets to handler
        unfilteredConsumer.getConsumers().add(new Consumer<AisPacket>() {
            @Override
            public void accept(AisPacket packet) {
                handler.receiveUnfiltered(packet);
            }
        });

    }

    public void start() {
        // Start aisBus
        aisBus.start();
        aisBus.startConsumers();
        aisBus.startProviders();
        LOG.info("aisbus startet");
        // Start web server
        if (webServer != null) {
            try {
                webServer.start();
                LOG.info("webserver startet");
            } catch (Exception e) {
                LOG.error("Failed to start web server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        // Stop AisBus
        aisBus.cancel();
        LOG.info("aisbus stopped");
    }
    
    public AisCoverageConfiguration getConf() {	return conf;	}
    public CoverageHandler getHandler() {	return handler;	}
    
    public static synchronized AisCoverage create(AisCoverageConfiguration conf) {
        instance = new AisCoverage(conf);
        return instance;
    }
    
    public static synchronized AisCoverage get() {	return instance;	}

}
