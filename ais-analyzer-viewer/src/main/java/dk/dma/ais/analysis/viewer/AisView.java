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
package dk.dma.ais.analysis.viewer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.common.web.WebServer;
import dk.dma.ais.analysis.viewer.configuration.AisViewConfiguration;
import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;

/**
 * AIS viewer
 */
@ThreadSafe
public class AisView {

    private static final Logger LOG = LoggerFactory.getLogger(AisView.class);

    @GuardedBy("AisView")
    private static AisView instance;

    private final AisViewConfiguration conf;
    private final AisViewerHandler handler;
    private final AisBus aisBus;
    private final WebServer webServer;

    private AisView(AisViewConfiguration conf) {
        this.conf = conf;

        // Create handler
        handler = new AisViewerHandler(conf);

        // Create AisBus
        aisBus = conf.getAisbusConfiguration().getInstance();

        // Create web server
        if (conf.getServerConfiguration() != null) {
            webServer = new WebServer(conf.getServerConfiguration());
        } else {
            webServer = null;
        }

        // Create distributor consumer and add to aisBus
        DistributerConsumer distributer = new DistributerConsumer();
        distributer.getConsumers().add(handler);
        distributer.init();
        aisBus.registerConsumer(distributer);

    }

    public void start() {
        // Start aisBus
        aisBus.start();
        aisBus.startConsumers();
        aisBus.startProviders();
        // Start web server
        if (webServer != null) {
            try {
                webServer.start();
            } catch (Exception e) {
                LOG.error("Failed to start web server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        // Stop AisBus
        aisBus.cancel();
    }

    public AisViewConfiguration getConf() {
        return conf;
    }

    public AisViewerHandler getHandler() {
        return handler;
    }

    public static synchronized AisView create(AisViewConfiguration conf) {
        instance = new AisView(conf);
        return instance;
    }

    public static synchronized AisView get() {
        return instance;
    }

}
