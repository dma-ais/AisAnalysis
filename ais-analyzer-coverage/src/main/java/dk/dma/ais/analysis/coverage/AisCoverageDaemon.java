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

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;

import dk.dma.ais.analysis.coverage.configuration.AisCoverageConfiguration;
import dk.dma.app.application.AbstractDaemon;

/**
 * AIS coverage analyzer daemon
 */
public class AisCoverageDaemon extends AbstractDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(AisCoverageDaemon.class);

    @Parameter(names = "-file", description = "AisCoverage configuration file")
    String confFile = "aiscoverage.xml";

    @Override
    protected void runDaemon(Injector injector) throws Exception {
        LOG.info("Starting AisCoverageDaemon with configuration: " + confFile);

        // Get configuration
        AisCoverageConfiguration conf;
        try {
            conf = AisCoverageConfiguration.load(confFile);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            return;
        }

        // Create and start
        AisCoverage.create(conf);
        AisCoverage.get().start();
    }

    @Override
    protected void shutdown() {
        LOG.info("Shutting down");
        AisCoverage.get().stop();
        super.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new AisCoverageDaemon().execute(args);
    }

}
