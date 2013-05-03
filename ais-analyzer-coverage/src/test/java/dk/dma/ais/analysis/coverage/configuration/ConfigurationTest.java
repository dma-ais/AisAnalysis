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
package dk.dma.ais.analysis.coverage.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;
import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.configuration.bus.consumer.DistributerConsumerConfiguration;
import dk.dma.ais.configuration.bus.provider.TcpClientProviderConfiguration;
import dk.dma.ais.configuration.filter.DuplicateFilterConfiguration;

public class ConfigurationTest {
    
    @Test
    public void makeConfiguration() throws FileNotFoundException, JAXBException {
        String filename = "src/main/resources/coverage-test.xml";
        AisCoverageConfiguration conf = new AisCoverageConfiguration();
        AisBusConfiguration aisBusConf = new AisBusConfiguration();
        
        // Provider
        TcpClientProviderConfiguration reader = new TcpClientProviderConfiguration();
        reader.getHostPort().add("ais163.sealan.dk:65262");
        aisBusConf.getProviders().add(reader);
        
        // Unfiltered consumer
        DistributerConsumerConfiguration unfilteredDist = new DistributerConsumerConfiguration();
        unfilteredDist.setName("UNFILTERED");
        aisBusConf.getConsumers().add(unfilteredDist);
        
        // Filtered consumer
        DistributerConsumerConfiguration filteredDist = new DistributerConsumerConfiguration();
        filteredDist.setName("FILTERED");
        DuplicateFilterConfiguration duplicateFilter = new DuplicateFilterConfiguration();
        filteredDist.getFilters().add(duplicateFilter);
        aisBusConf.getConsumers().add(filteredDist);
        conf.setAisbusConfiguration(aisBusConf);
        
        conf.setLatSize(1.5);
        conf.setLonSize(1.5);
        DatabaseConfiguration dbConf = new DatabaseConfiguration();
        conf.setDatabaseConfiguration(dbConf);
//        dbConf.set
//        dbConf.setName("MongoDB");
//        dbConf.setAddr("localhost");
//        dbConf.setPort(9999);
//        conf.setDatabase("MemoryOnly");
        
        AisCoverageConfiguration.save(filename, conf);
        
        conf = AisCoverageConfiguration.load(filename);
        AisBus aisBus = conf.getAisbusConfiguration().getInstance();
        DistributerConsumer filtered = (DistributerConsumer)aisBus.getConsumer("FILTERED");
        Assert.assertNotNull(filtered);
        DistributerConsumer unfiltered = (DistributerConsumer)aisBus.getConsumer("UNFILTERED");
        Assert.assertNotNull(unfiltered);
    }

}
