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
package dk.dma.ais.analysis.viewer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.analysis.common.web.WebServer;
import dk.dma.ais.analysis.viewer.configuration.AisViewConfiguration;
import dk.dma.ais.analysis.viewer.handler.AisViewHandler;
import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;

/**
 * AIS viewer
 */
@ThreadSafe
public final class AisView {

    private static final Logger LOG = LoggerFactory.getLogger(AisView.class);

    @GuardedBy("AisView")
    private static AisView instance;

    private final AisViewConfiguration conf;
    private final AisViewHandler handler;
    private final AisBus aisBus;
    private final WebServer webServer;

    private AisView(AisViewConfiguration conf) {
        this.conf = conf;

        // Create and start handler
        handler = new AisViewHandler(conf);
        handler.start();

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
        // Stop handler
        handler.interrupt();
        // Stop AisBus
        aisBus.cancel();
    }

    public AisViewConfiguration getConf() {
        return conf;
    }

    public AisViewHandler getHandler() {
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
