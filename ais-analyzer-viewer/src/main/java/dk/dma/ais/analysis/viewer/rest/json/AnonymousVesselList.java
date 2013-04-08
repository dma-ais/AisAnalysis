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
