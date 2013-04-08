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
package dk.dma.ais.analysis.viewer.rest.json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import dk.dma.ais.data.AisClassAPosition;
import dk.dma.ais.data.AisClassAStatic;
import dk.dma.ais.data.AisClassBTarget;
import dk.dma.ais.data.AisTargetDimensions;
import dk.dma.ais.data.AisVesselPosition;
import dk.dma.ais.data.AisVesselStatic;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.ais.data.IPastTrack;
import dk.dma.ais.message.NavigationalStatus;

public class VesselTargetDetails {    
    
    protected long id;
    protected long mmsi;
    protected String vesselClass;
    protected String lastReceived;
    protected long currentTime;
    protected String lat;
    protected String lon;
    protected String cog;
    protected boolean moored;
    protected String vesselType = "N/A";
    protected Short length = null;
    protected Short width = null;
    protected String sog;
    protected String name = "N/A";
    protected String callsign = "N/A";
    protected String imoNo = "N/A";
    protected String cargo = "N/A";
    protected String country;
    protected String draught = "N/A";
    protected String heading = "N/A";
    protected String rot = "N/A";
    protected String destination = "N/A";
    protected String navStatus = "N/A";
    protected String eta = "N/A";
    protected String posAcc = "N/A";
    protected String sourceType;
    protected String sourceSystem;
    protected String sourceRegion;
    protected String sourceBs;
    protected String sourceCountry;
    protected String pos;
    protected IPastTrack pastTrack;

    public VesselTargetDetails() {
        
    }
    
    public void init(AisVesselTarget target, int anonId) {        
        AisVesselPosition pos = target.getVesselPosition();
        if (pos == null || pos.getPos() == null) return;
        AisClassAPosition classAPos = null;
        if (pos instanceof AisClassAPosition) {
            classAPos = (AisClassAPosition)pos;
        }
                        
        this.currentTime = System.currentTimeMillis();
        this.id = anonId;
        this.mmsi = target.getMmsi();
        this.vesselClass = (target instanceof AisClassBTarget) ? "B" : "A";
        this.lastReceived = formatTime(currentTime - target.getLastReport().getTime());
        this.lat = latToPrintable(pos.getPos().getLatitude());
        this.lon = lonToPrintable(pos.getPos().getLongitude());
        this.cog = formatDouble(pos.getCog(), 0);        
        this.heading = formatDouble(pos.getHeading(), 1);
        this.sog = formatDouble(pos.getSog(), 1);    
        

        if (target.getCountry() != null) {
            this.country = target.getCountry().getName();
        } else {
            this.country ="N/A";
        }
        
        
        this.sourceType = target.getSourceData().getSourceType();
        
        this.sourceSystem = target.getSourceData().getTagging().getSourceId();
        if (this.sourceSystem == null) {
            this.sourceSystem = "N/A";
        }
        this.sourceRegion = target.getSourceData().getSourceRegion();
        if (this.sourceRegion == null) {
            this.sourceRegion = "N/A";
        }
        if (target.getSourceData().getTagging().getSourceBs() != null) {
            this.sourceBs = Integer.toString(target.getSourceData().getTagging().getSourceBs());
        } else {
            this.sourceBs = "N/A";
        }
        
        if (target.getSourceData().getTagging().getSourceCountry() != null) {
            this.sourceCountry = target.getSourceData().getTagging().getSourceCountry().getName();
        } else {
            this.sourceCountry = "N/A";
        }
        
        // Class A position
        if (classAPos != null) {
            NavigationalStatus navigationalStatus = new NavigationalStatus(classAPos.getNavStatus());
            this.navStatus = navigationalStatus.prettyStatus();
            this.moored = (classAPos.getNavStatus() == 1 || classAPos.getNavStatus() == 5);
            this.rot = formatDouble(classAPos.getRot(), 1);
        }
                
        if (pos.getPosAcc() == 1) {
            this.posAcc = "High";
        } else {
            this.posAcc = "Low";
        }
        
        this.pos = latToPrintable(pos.getPos().getLatitude()) + " - " + lonToPrintable(pos.getPos().getLongitude());
        
        
        AisVesselStatic statics = target.getVesselStatic();
        if (statics == null) return;
        AisClassAStatic classAStatics = null;
        if (statics instanceof AisClassAStatic) {
            classAStatics = (AisClassAStatic)statics;
        }
        
        AisTargetDimensions dim = statics.getDimensions();
        if (dim != null) {
            this.length = (short) (dim.getDimBow() + dim.getDimStern());
            this.width = (short) (dim.getDimPort() + dim.getDimStarboard());
        }
        if (statics.getShipTypeCargo() != null) {
            this.vesselType = statics.getShipTypeCargo().prettyType();
            this.cargo = statics.getShipTypeCargo().prettyCargo();
        }
        this.name = statics.getName();
        this.callsign = statics.getCallsign();        
        // Class A statics
        if (classAStatics != null) {
            if (classAStatics.getImoNo() != null) {
                this.imoNo = Integer.toString(classAStatics.getImoNo()); 
            } else {
                this.imoNo = "N/A";
            }                    
            this.destination = (classAStatics.getDestination() != null) ? classAStatics.getDestination() : "N/A";
            this.draught = (classAStatics.getDraught() != null) ? formatDouble((double)classAStatics.getDraught(), 1) : "N/A";
            this.eta = getISO8620(classAStatics.getEta());
        }        

    }
    
    public static String getISO8620(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat iso8601gmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        iso8601gmt.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
        return iso8601gmt.format(date);
    }
    
    public static String formatTime(Long time) {
        if (time == null) {
            return "N/A";
        }
        long secondInMillis = 1000;
        long minuteInMillis = secondInMillis * 60;
        long hourInMillis = minuteInMillis * 60;
        long dayInMillis = hourInMillis * 24;

        long elapsedDays = time / dayInMillis;
        time = time % dayInMillis;
        long elapsedHours = time / hourInMillis;
        time = time % hourInMillis;
        long elapsedMinutes = time / minuteInMillis;
        time = time % minuteInMillis;
        long elapsedSeconds = time / secondInMillis;

        if (elapsedDays > 0) {
            return String.format("%02d:%02d:%02d:%02d", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
        } else if (elapsedHours > 0) {
            return String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
        } else {
            return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
        }
    }
    
    public static String latToPrintable(Double lat) {
        if (lat == null) {
            return "N/A";
        }
        String ns = "N";
        if (lat < 0) {
            ns = "S";
            lat *= -1;
        }
        int hours = (int)lat.doubleValue();
        lat -= hours;
        lat *= 60;
        String latStr = String.format(Locale.US, "%3.3f", lat);
        while (latStr.indexOf('.') < 2) {
            latStr = "0" + latStr;
        }        
        return String.format(Locale.US, "%02d %s%s", hours, latStr, ns);
    }
    
    public static String lonToPrintable(Double lon) {
        if (lon == null) {
            return "N/A";
        }
        String ns = "E";
        if (lon < 0) {
            ns = "W";
            lon *= -1;
        }
        int hours = (int)lon.doubleValue();
        lon -= hours;
        lon *= 60;        
        String lonStr = String.format(Locale.US, "%3.3f", lon);
        while (lonStr.indexOf('.') < 2) {
            lonStr = "0" + lonStr;
        }        
        return String.format(Locale.US, "%03d %s%s", hours, lonStr, ns);
    }
    
    public static String formatDouble(Double d, int decimals) {
        if (d == null) {
            return "N/A";
        }
        if (decimals == 0) {
            return String.format(Locale.US, "%d", Math.round(d));
        }
        String format = "%." + decimals + "f";
        return String.format(Locale.US, format, d);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getImoNo() {
        return imoNo;
    }

    public void setImoNo(String imoNo) {
        this.imoNo = imoNo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDraught() {
        return draught;
    }

    public void setDraught(String draught) {
        this.draught = draught;
    }

    public String getRot() {
        return rot;
    }

    public void setRot(String rot) {
        this.rot = rot;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getNavStatus() {
        return navStatus;
    }

    public void setNavStatus(String navStatus) {
        this.navStatus = navStatus;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getPosAcc() {
        return posAcc;
    }

    public void setPosAcc(String posAcc) {
        this.posAcc = posAcc;
    }
    
    public long getMmsi() {
        return mmsi;
    }
    
    public void setMmsi(long mmsi) {
        this.mmsi = mmsi;
    }
    
    public String getVesselClass() {
        return vesselClass;
    }
    
    public void setVesselClass(String vesselClass) {
        this.vesselClass = vesselClass;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public boolean isMoored() {
        return moored;
    }
    
    public void setMoored(boolean moored) {
        this.moored = moored;
    }

    public String getVesselType() {
        return vesselType;
    }

    public void setVesselType(String vesselType) {
        this.vesselType = vesselType;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public String getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(String lastReceived) {
        this.lastReceived = lastReceived;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCog() {
        return cog;
    }

    public void setCog(String cog) {
        this.cog = cog;
    }

    public String getSog() {
        return sog;
    }

    public void setSog(String sog) {
        this.sog = sog;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setLength(Short length) {
        this.length = length;
    }

    public void setWidth(Short width) {
        this.width = width;
    }
    
    public String getPos() {
        return pos;
    }
    
    public void setPos(String pos) {
        this.pos = pos;
    }
    
    public String getSourceSystem() {
        return sourceSystem;
    }
    
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
    
    public String getSourceRegion() {
        return sourceRegion;
    }
    
    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }
    
    public String getSourceBs() {
        return sourceBs;
    }
    
    public void setSourceBs(String sourceBs) {
        this.sourceBs = sourceBs;
    }
    
    public String getSourceCountry() {
        return sourceCountry;
    }
    
    public void setSourceCountry(String sourceCountry) {
        this.sourceCountry = sourceCountry;
    }
    
    public IPastTrack getPastTrack() {
        return pastTrack;
    }
    
    public void setPastTrack(IPastTrack pastTrack) {
        this.pastTrack = pastTrack;
    }

}
