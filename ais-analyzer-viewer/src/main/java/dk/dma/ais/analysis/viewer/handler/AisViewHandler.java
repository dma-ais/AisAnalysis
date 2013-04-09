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
package dk.dma.ais.analysis.viewer.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import dk.dma.ais.analysis.common.grid.Grid;
import dk.dma.ais.analysis.common.grid.GridFactory;
import dk.dma.ais.analysis.viewer.configuration.AisViewConfiguration;
import dk.dma.ais.analysis.viewer.rest.VesselListFilter;
import dk.dma.ais.analysis.viewer.rest.json.AisViewHandlerStats;
import dk.dma.ais.analysis.viewer.rest.json.BaseVesselList;
import dk.dma.ais.analysis.viewer.rest.json.VesselCluster;
import dk.dma.ais.analysis.viewer.rest.json.VesselClusterJsonRepsonse;
import dk.dma.ais.analysis.viewer.rest.json.VesselList;
import dk.dma.ais.analysis.viewer.rest.json.VesselTargetDetails;
import dk.dma.ais.data.AisClassATarget;
import dk.dma.ais.data.AisTarget;
import dk.dma.ais.data.AisVesselPosition;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.ais.data.IPastTrack;
import dk.dma.ais.data.PastTrackSortedSet;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.util.function.Consumer;

/**
 * Handler for received AisPackets
 */
public class AisViewHandler extends Thread implements Consumer<AisPacket> {

    private static Logger LOG = Logger.getLogger(AisViewHandler.class);

    private final AisViewConfiguration conf;
    // Map from mmsi to newest position report
    private Map<Integer, Date> lastPosReportMap = new HashMap<Integer, Date>();
    // Map from MMSI to AisTarget data object
    private Map<Integer, AisTarget> targetsMap = new HashMap<Integer, AisTarget>();
    // Map from anonymous id to MMSI
    private Map<Integer, Integer> anonIdMap = new HashMap<Integer, Integer>();
    // Map from MMSI to anonymous id
    private Map<Integer, Integer> mmsiAnonIdMap = new HashMap<Integer, Integer>();
    // Map from MMSI to PastTrack
    private Map<Integer, IPastTrack> pastTrackMap = new HashMap<Integer, IPastTrack>();

    // Counter to for mmsi <-> anonid mappings
    private int anonymousCounter = 0;
    // Time of last cleanup
    private long lastCleanup = 0;

    public AisViewHandler(AisViewConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public synchronized void accept(AisPacket packet) {
        // Get AisMessage
        AisMessage aisMessage = packet.tryGetAisMessage();
        if (aisMessage == null) {
            return;
        }
        // We only want to handle messages containing targets data
        // #1-#3, #4, #5, #18, #21, #24
        if (!AisTarget.isTargetDataMessage(aisMessage)) {
            return;
        }

        boolean newTarget = false;
        // Get existing AisTarget or create new
        AisTarget target = getTarget(aisMessage.getUserId());
        if (target == null) {
            newTarget = true;
            target = AisTarget.createTarget(aisMessage);
        }
        // Check if message actually relates to a target
        if (target == null) {
            return;
        }

        // We want to avoid to update a target position with an older position
        // than the last one received
        boolean oldPos = false;
        if (aisMessage instanceof IVesselPositionMessage) {
            Date lastReport = lastPosReportMap.get(aisMessage.getUserId());
            Date thisReport = null;
            // Get timestamp for message tag or fallback to time now
            thisReport = aisMessage.getVdm().getTimestamp();
            if (thisReport == null) {
                thisReport = new Date();
            }
            if (lastReport != null) {
                // We will not update if this report is older than last
                if (thisReport.before(lastReport)) {
                    oldPos = true;
                }
            }
            lastPosReportMap.put(aisMessage.getUserId(), thisReport);
        }

        // Update target data
        if (!oldPos) {
            try {
                target.update(aisMessage);
            } catch (IllegalArgumentException e) {
                // Trying to update target with report of different type of target.
                // Replace target with new target
                target = AisTarget.createTarget(aisMessage);
                target.update(aisMessage);
                replaceTarget(aisMessage.getUserId(), target);
            }
        }

        if (newTarget) {
            newTarget(aisMessage.getUserId(), target);
        }

        // Get or create past track entry for mmsi
        IPastTrack pastTrack = null;
        // Update pasttrack
        if (conf.isRecordPastTrack()) {
            pastTrack = getPastTrack(aisMessage.getUserId());
            if (aisMessage instanceof IVesselPositionMessage) {
                IVesselPositionMessage posMessage = (IVesselPositionMessage) aisMessage;
                Position pos = posMessage.getPos().getGeoLocation();
                if (pos != null) {
                    // Make VesselPosition instance
                    AisVesselPosition vesselPosition = new AisVesselPosition();
                    vesselPosition.update((IVesselPositionMessage) aisMessage);

                    // Update past track
                    pastTrack.addPosition(vesselPosition, conf.getPastTrackMinDist());
                }
            }
        }

    }

    /**
     * Get target given MMSI number
     * 
     * @param mmsi
     * @return
     */
    public synchronized AisTarget getTarget(int mmsi) {
        return targetsMap.get(mmsi);
    }

    /**
     * Insert new target
     * 
     * @param userId
     * @param target
     */
    public synchronized void newTarget(int mmsi, AisTarget target) {
        int anonId = nextAnonId();
        targetsMap.put(mmsi, target);
        anonIdMap.put(anonId, mmsi);
        mmsiAnonIdMap.put(mmsi, anonId);
    }

    /**
     * Replace existing target with new target
     * 
     * @param mmsi
     * @param target
     */
    public synchronized void replaceTarget(int mmsi, AisTarget target) {
        targetsMap.put(mmsi, target);
        // Remove past track
        pastTrackMap.remove(mmsi);
    }

    public synchronized void removeTarget(int mmsi) {
        Integer anonId = mmsiAnonIdMap.get(mmsi);
        if (anonId != null) {
            anonIdMap.remove(anonId);
        }
        mmsiAnonIdMap.remove(mmsi);
        targetsMap.remove(mmsi);
        pastTrackMap.remove(mmsi);
    }

    /**
     * Get past track of target
     * 
     * @param mmsi
     * @return
     */
    public synchronized IPastTrack getPastTrack(int mmsi) {
        IPastTrack pastTrack = pastTrackMap.get(mmsi);
        if (pastTrack == null) {
            pastTrack = new PastTrackSortedSet();
            pastTrackMap.put(mmsi, pastTrack);
        }
        return pastTrack;
    }

    /**
     * Get collection of all targets
     * 
     * @return
     */
    public synchronized Collection<AisTarget> getAllTargets() {
        return targetsMap.values();
    }

    public synchronized Collection<IPastTrack> getAllPastTracks() {
        return pastTrackMap.values();
    }

    /**
     * Get anonymous id for MMSI number
     * 
     * @param mmsi
     * @return
     */
    public synchronized Integer getAnonId(int mmsi) {
        return mmsiAnonIdMap.get(mmsi);
    }

    /**
     * Get MMSI for anonymous id
     * 
     * @param anonId
     * @return
     */
    public synchronized Integer getMmsi(int anonId) {
        return anonIdMap.get(anonId);
    }

    private synchronized int nextAnonId() {
        return this.anonymousCounter++;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.info("Stopping AisViewHandler");
                return;
            }
            cleanup();
        }
    }

    private synchronized void cleanup() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastCleanup;
        if (elapsed < (conf.getCleanupInterval() * 1000)) {
            return;
        }
        lastCleanup = now;
        List<Integer> deadTargets = new ArrayList<>();
        for (AisTarget target : getAllTargets()) {
            Date lastReport = target.getLastReport();
            elapsed = System.currentTimeMillis() - lastReport.getTime();
            if (elapsed > conf.getCleanupTtl() * 1000) {
                deadTargets.add(target.getMmsi());
            }
        }
        // Cleanup past track
        if (conf.isRecordPastTrack()) {
            for (AisTarget target : getAllTargets()) {
                IPastTrack pastTrack = getPastTrack(target.getMmsi());
                if (pastTrack != null) {
                    pastTrack.cleanup(target.getSourceData().isSatData() ? conf.getPastTrackSatTtl() : conf.getPastTrackLiveTtl());
                }
            }
        }
        // Cleanup
        LOG.info("Removing " + deadTargets.size() + " dead targets");
        for (Integer mmsi : deadTargets) {
            // LOG.info("Removing target: " + mmsi);
            removeTarget(mmsi);
            lastPosReportMap.remove(mmsi);
        }
    }

    public synchronized BaseVesselList getVesselList(BaseVesselList list, VesselListFilter filter, Position pointA, Position pointB) {
        // Iterate through all vessel targets and add to response
        int inWorld = 0;
        for (AisTarget target : getAllTargets()) {
            AisVesselTarget vesselTarget = getFilteredAisVessel(target, filter);
            if (vesselTarget == null || vesselTarget.getVesselPosition() == null
                    || vesselTarget.getVesselPosition().getPos() == null)
                continue;

            inWorld++;

            // Is it inside the requested area
            if (rejectedByPosition(vesselTarget, pointA, pointB)) {
                continue;
            }

            list.addTarget(vesselTarget, getAnonId(vesselTarget.getMmsi()));
        }

        list.setInWorldCount(inWorld);

        return list;
    }

    /**
     * Returns a casted AisVesselTarget of the given AisTarget if it is an instance of AisVesselTarget.
     * 
     * @param target
     * @param filter
     * @return
     */
    private synchronized AisVesselTarget getFilteredAisVessel(AisTarget target, VesselListFilter filter) {
        if (!(target instanceof AisVesselTarget)) {
            return null;
        }
        // Determine TTL
        boolean satData = target.getSourceData().isSatData();
        int ttl = (satData) ? conf.getSatTargetTtl() : conf.getLiveTargetTtl();
        // Is it alive
        if (!target.isAlive(ttl)) {
            return null;
        }

        // Maybe filtered away
        if (filter.rejectedByFilter((AisVesselTarget) target)) {
            return null;
        }

        return (AisVesselTarget) target;
    }

    /**
     * Returns false if target is out of specified area. Nothing will be rejected if the area is not specified.
     * 
     * @param target
     *            The target to test
     * @param pointA
     *            Upper left corner of area.
     * @param pointB
     *            Bottom right corner of area.
     * @return false if target is out of specified area, else true.
     */
    private static boolean rejectedByPosition(AisVesselTarget target, Position pointA, Position pointB) {

        // Check if requested area is null
        if (pointA == null || pointB == null) {
            return false;
        }

        // Check if vessel has a position
        if (target.getVesselPosition() == null || target.getVesselPosition().getPos() == null) {
            return true;
        }

        // Latitude check - Reject targets not between A and B
        if (target.getVesselPosition().getPos().getLatitude() <= pointA.getLatitude()
                && target.getVesselPosition().getPos().getLatitude() >= pointB.getLatitude()) {

            // Longitude check - Accept targets between A and B
            if (pointB.getLongitude() <= pointA.getLongitude()
                    && (target.getVesselPosition().getPos().getLongitude() >= pointA.getLongitude() || target.getVesselPosition()
                            .getPos().getLongitude() <= pointB.getLongitude())) {

                return false;
            }

            // Longitude - Reject targets between B and A - Accept others
            if (pointA.getLongitude() <= pointB.getLongitude()
                    && (target.getVesselPosition().getPos().getLongitude() >= pointB.getLongitude() || target.getVesselPosition()
                            .getPos().getLongitude() <= pointA.getLongitude())) {
                return true;

            } else if (pointA.getLongitude() <= pointB.getLongitude()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a list of vessel clusters based on a filtering. The returned list does only contain clusters with vessels.
     * 
     * @param filter
     * @param size
     * @param limit
     * @return
     */
    public synchronized VesselClusterJsonRepsonse getClusterResponse(int requestId, VesselListFilter filter, Position pointA,
            Position pointB, int limit, double size) {

        Grid grid = GridFactory.getInstance().getGrid(size);

        // Maps cell ids to vessel clusters
        HashMap<Long, VesselCluster> map = new HashMap<Long, VesselCluster>();

        // Iterate over targets
        int inWorld = 0;
        for (AisTarget target : getAllTargets()) {

            AisVesselTarget vesselTarget = getFilteredAisVessel(target, filter);
            if (vesselTarget == null || vesselTarget.getVesselPosition() == null
                    || vesselTarget.getVesselPosition().getPos() == null) {
                continue;
            }

            inWorld++;

            // Is it inside the requested area
            if (rejectedByPosition(vesselTarget, pointA, pointB)) {
                continue;
            }

            Position vesselPosition = vesselTarget.getVesselPosition().getPos();
            long cellId = grid.getCellId(vesselPosition.getLatitude(), vesselPosition.getLongitude());

            // Only create vessel cluster if new
            if (map.containsKey(cellId)) {

                map.get(cellId).incrementCount();

                if (map.get(cellId).getCount() < limit) {
                    map.get(cellId).getVessels().addTarget(vesselTarget, getAnonId(vesselTarget.getMmsi()));
                }

            } else {

                Position from = grid.getGeoPosOfCellId(cellId);

                double toLon = from.getLongitude() + grid.getCellSizeInDegrees();
                double toLat = from.getLatitude() + grid.getCellSizeInDegrees();
                Position to = Position.create(toLat, toLon);

                VesselCluster cluster = new VesselCluster(from, to, 1, new VesselList());
                map.put(cellId, cluster);
                map.get(cellId).getVessels().addTarget(vesselTarget, getAnonId(vesselTarget.getMmsi()));

            }
        }

        // Calculate density
        ArrayList<VesselCluster> clusters = new ArrayList<VesselCluster>(map.values());
        for (VesselCluster c : clusters) {

            Position from = Position.create(c.getFrom().getLatitude(), c.getFrom().getLongitude());
            Position to = Position.create(c.getTo().getLatitude(), c.getTo().getLongitude());
            Position topRight = Position.create(from.getLatitude(), to.getLongitude());
            Position botLeft = Position.create(to.getLatitude(), from.getLongitude());
            double width = from.geodesicDistanceTo(topRight) / 1000;
            double height = from.geodesicDistanceTo(botLeft) / 1000;
            double areaSize = width * height;
            double density = (double) c.getCount() / areaSize;
            c.setDensity(density);

        }
        VesselClusterJsonRepsonse response = new VesselClusterJsonRepsonse(requestId, clusters, inWorld);
        return response;
    }

    public synchronized VesselTargetDetails getVesselTargetDetails(Integer anonId, Integer mmsi, boolean pastTrack) {
        // Get MMSI for anonymous id if mmsi not given
        if (mmsi == null && anonId != null) {
            mmsi = getMmsi(anonId);
        }
        if (mmsi == null) {
            return null;
        }
        anonId = getAnonId(mmsi);
        AisTarget target = getTarget(mmsi);
        if (target == null) {
            return null;
        }
        if (!(target instanceof AisVesselTarget)) {
            return null;
        }

        VesselTargetDetails details = new VesselTargetDetails();
        details.init((AisVesselTarget) target, anonId);
        if (pastTrack) {
            details.setPastTrack(getPastTrack(mmsi));
        }

        return details;
    }

    /**
     * Get simple list of anonymous targets that matches the search criteria.
     * 
     * @param searchCriteria
     *            A string that will be matched to all vessel names, IMOs and MMSIs.
     * @return A list of targets.
     */
    public synchronized VesselList searchTargets(String searchCriteria) {

        VesselList response = new VesselList();

        // Iterate through all vessel targets and add to response
        for (AisTarget target : getAllTargets()) {
            if (!(target instanceof AisVesselTarget)) {
                continue;
            }

            // Determine TTL (could come from configuration)
            boolean satData = target.getSourceData().isSatData();
            int ttl = (satData) ? conf.getSatTargetTtl() : conf.getLiveTargetTtl();

            // Is it alive
            if (!target.isAlive(ttl)) {
                continue;
            }

            // Maybe filtered away
            if (rejectedBySearchCriteria(target, searchCriteria)) {
                continue;
            }

            response.addTarget((AisVesselTarget) target, getAnonId(target.getMmsi()));
        }

        return response;
    }

    /**
     * Returns false if target matches a given searchCriteria. This method only matches on the targets name, mmsi and imo.
     * 
     * @param target
     * @param searchCriteria
     * @return false if the target matches the search criteria.
     * @throws JsonApiException
     */
    private static boolean rejectedBySearchCriteria(AisTarget target, String searchCriteria) {

        if (!(target instanceof AisVesselTarget)) {
            return true;
        }

        // Get length of search criteria
        int searchLength = searchCriteria.length();

        AisVesselTarget vessel = (AisVesselTarget) target;

        // Get details
        Integer mmsi = vessel.getMmsi();

        // Check mmsi
        String mmsiString = Long.toString(mmsi);
        if (mmsiString.length() >= searchLength && mmsiString.substring(0, searchLength).equals(searchCriteria)) {
            return false;
        }

        // Check name
        if (vessel.getVesselStatic() != null && vessel.getVesselStatic().getName() != null) {
            String name = vessel.getVesselStatic().getName().toUpperCase();

            // Check entire name
            if (name.length() >= searchLength && name.substring(0, searchLength).equals(searchCriteria.toUpperCase())) {
                return false;
            }

            // Check each word
            String[] words = name.split(" ");
            for (String w : words) {
                if (w.length() >= searchLength && w.substring(0, searchLength).equals(searchCriteria.toUpperCase())) {
                    return false;
                }
            }
        }

        // Check imo - if Class A
        if (vessel instanceof AisClassATarget) {
            AisClassATarget classAVessel = (AisClassATarget) vessel;
            if (classAVessel.getClassAStatic() != null && classAVessel.getClassAStatic().getImoNo() != null) {
                int imo = classAVessel.getClassAStatic().getImoNo();
                String imoString = Integer.toString(imo);
                if (imoString.length() >= searchLength && imoString.substring(0, searchLength).equals(searchCriteria)) {
                    return false;
                }
            }
        }

        return true;
    }

    public synchronized AisViewHandlerStats getStat() {
        AisViewHandlerStats stats = new AisViewHandlerStats(getAllTargets(), getAllPastTracks());
        return stats;
    }

}
