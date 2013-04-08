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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import dk.dma.ais.analysis.common.web.WebServerConfiguration;
import dk.dma.ais.configuration.bus.AisBusConfiguration;

/**
 * Class to represent AIS coverage configuration. To be marshalled and unmarshalled by JAXB.
 */
@XmlRootElement
public class AisViewConfiguration {

    private AisBusConfiguration aisbusConfiguration;
    private WebServerConfiguration serverConfiguration;

    public AisViewConfiguration() {

    }

    @XmlElement(name = "aisbus")
    public AisBusConfiguration getAisbusConfiguration() {
        return aisbusConfiguration;
    }

    public void setAisbusConfiguration(AisBusConfiguration aisbusConfiguration) {
        this.aisbusConfiguration = aisbusConfiguration;
    }

    public WebServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public void setServerConfiguration(WebServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }
    
    public static void save(String filename, AisViewConfiguration conf) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AisViewConfiguration.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.marshal(conf, new FileOutputStream(new File(filename)));
    }

    public static AisViewConfiguration load(String filename) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AisViewConfiguration.class);
        Unmarshaller um = context.createUnmarshaller();
        return (AisViewConfiguration) um.unmarshal(new FileInputStream(new File(filename)));
    }

}
