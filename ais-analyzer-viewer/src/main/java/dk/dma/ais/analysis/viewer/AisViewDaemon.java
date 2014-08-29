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

import java.io.FileNotFoundException;
import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;

import dk.dma.ais.analysis.viewer.configuration.AisViewConfiguration;
import dk.dma.commons.app.AbstractDaemon;

/**
 * AIS viewer daemon
 */
public class AisViewDaemon extends AbstractDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(AisViewDaemon.class);

    @Parameter(names = "-file", description = "AisViewDaemon configuration file")
    String confFile = "aisview.xml";
    
    private AisView aisView;

    @Override
    protected void runDaemon(Injector injector) throws Exception {
        LOG.info("Starting AisViewDaemon with configuration: " + confFile);

        // Get configuration
        AisViewConfiguration conf;
        try {
            conf = AisViewConfiguration.load(confFile);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            return;
        }

        // Create and start
        aisView = AisView.create(conf);
        aisView.start();
    }

    @Override
    public void shutdown() {
        LOG.info("Shutting down");
        if (aisView != null) {
            aisView.stop();
        }
        super.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {            
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error("Uncaught exception in thread " + t.getClass().getCanonicalName() + ": " + e.getMessage(), e);
                System.exit(-1);
            }
        });
        new AisViewDaemon().execute(args);
    }

}
