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
 * Class to represent AisView configuration. To be marshalled and unmarshalled by JAXB.
 */
@XmlRootElement
public class AisViewConfiguration {

    private AisBusConfiguration aisbusConfiguration;
    private WebServerConfiguration serverConfiguration;
    private boolean anonymous = false;
    private boolean recordPastTrack = false;
    private int liveTargetTtl = 1200; // 20 min
    private int satTargetTtl = 172800; // 48 hours
    private int pastTrackMinDist = 100; // 100 meters
    private int pastTrackLiveTtl = 3600; // 1 hour
    private int pastTrackSatTtl = 3600; // 1 hour
    private int cleanupInterval = 600; // 10 minutes
    private int cleanupTtl = 43200; // 12 hours

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
    
    public boolean isAnonymous() {
        return anonymous;
    }
    
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isRecordPastTrack() {
        return recordPastTrack;
    }

    public void setRecordPastTrack(boolean recordPastTrack) {
        this.recordPastTrack = recordPastTrack;
    }

    public int getLiveTargetTtl() {
        return liveTargetTtl;
    }

    public void setLiveTargetTtl(int liveTargetTtl) {
        this.liveTargetTtl = liveTargetTtl;
    }

    public int getSatTargetTtl() {
        return satTargetTtl;
    }

    public void setSatTargetTtl(int satTargetTtl) {
        this.satTargetTtl = satTargetTtl;
    }

    public int getPastTrackMinDist() {
        return pastTrackMinDist;
    }

    public void setPastTrackMinDist(int pastTrackMinDist) {
        this.pastTrackMinDist = pastTrackMinDist;
    }

    public int getPastTrackLiveTtl() {
        return pastTrackLiveTtl;
    }

    public void setPastTrackLiveTtl(int pastTrackLiveTtl) {
        this.pastTrackLiveTtl = pastTrackLiveTtl;
    }

    public int getPastTrackSatTtl() {
        return pastTrackSatTtl;
    }

    public void setPastTrackSatTtl(int pastTrackSatTtl) {
        this.pastTrackSatTtl = pastTrackSatTtl;
    }

    public int getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(int cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public int getCleanupTtl() {
        return cleanupTtl;
    }

    public void setCleanupTtl(int cleanupTtl) {
        this.cleanupTtl = cleanupTtl;
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
