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
