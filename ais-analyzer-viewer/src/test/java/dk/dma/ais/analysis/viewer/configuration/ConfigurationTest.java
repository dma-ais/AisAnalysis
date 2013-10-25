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
package dk.dma.ais.analysis.viewer.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.configuration.bus.provider.TcpClientProviderConfiguration;

public class ConfigurationTest {
    
    @Test
    public void makeConfiguration() throws FileNotFoundException, JAXBException {
        String filename = "src/main/resources/aisview-test.xml";
        AisViewConfiguration conf = new AisViewConfiguration();
        AisBusConfiguration aisBusConf = new AisBusConfiguration();
        conf.setAisbusConfiguration(aisBusConf);
        
        // Provider
        TcpClientProviderConfiguration reader = new TcpClientProviderConfiguration();
        reader.getHostPort().add("ais163.sealan.dk:4712");
        aisBusConf.getProviders().add(reader);
        
        AisViewConfiguration.save(filename, conf);
        
        conf = AisViewConfiguration.load(filename);
        AisBus aisBus = conf.getAisbusConfiguration().getInstance();
        Assert.assertNotNull(aisBus);
    }

}
