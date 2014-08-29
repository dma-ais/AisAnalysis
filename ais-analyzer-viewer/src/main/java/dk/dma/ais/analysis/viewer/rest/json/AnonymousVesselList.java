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
package dk.dma.ais.analysis.viewer.rest.json;

import java.util.ArrayList;
import java.util.Locale;

import dk.dma.ais.data.AisClassAPosition;
import dk.dma.ais.data.AisVesselPosition;
import dk.dma.ais.data.AisVesselStatic;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.ais.message.ShipTypeCargo;

public class AnonymousVesselList extends BaseVesselList {

    public AnonymousVesselList() {
        super();
    }

    public void addTarget(AisVesselTarget vesselTarget, int anonId) {
        AisVesselPosition pos = vesselTarget.getVesselPosition();
        AisVesselStatic statics = vesselTarget.getVesselStatic();
        if (pos == null || pos.getPos() == null) {
            return;
        }

        Double cog = pos.getCog();
        Double sog = pos.getSog();
        Double lat = pos.getPos().getLatitude();
        Double lon = pos.getPos().getLongitude();

        String vesselClass;
        ShipTypeCargo shipTypeCargo = null;
        if (statics != null) {
            shipTypeCargo = statics.getShipTypeCargo();
        }

        Byte navStatus = null;
        if (pos instanceof AisClassAPosition) {
            navStatus = ((AisClassAPosition) pos).getNavStatus();
            vesselClass = "A";
        } else {
            vesselClass = "B";
        }

        if (cog == null) {
            cog = 0d;
        }
        if (sog == null) {
            sog = 0d;
        }

        // Round cog to nearest 10
        long cogL = Math.round(cog / 10.0) * 10;
        if (cogL == 360)
            cogL = 0;

        ArrayList<String> list = new ArrayList<String>();

        list.add(Long.toString(cogL));
        list.add(String.format(Locale.US, "%.5f", lat));
        list.add(String.format(Locale.US, "%.5f", lon));
        list.add(vesselClass);
        ShipTypeMapper.ShipTypeColor color = ShipTypeMapper.ShipTypeColor.GREY;
        if (shipTypeCargo != null) {
            color = shipTypeMapper.getColor(shipTypeCargo.getShipType());
        }
        list.add(Integer.toString(color.ordinal()));

        list.add((navStatus != null && (navStatus == 1 || navStatus == 5)) ? "1" : "0");

        vessels.put(anonId, list);
        vesselCount++;
    }

}
