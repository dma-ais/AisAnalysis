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
package dk.dma.ais.analysis.viewer.kml;

import java.util.Map;

import dk.dma.ais.analysis.viewer.handler.AisTargetEntry;
import dk.dma.ais.data.IPastTrack;

public class KmlGenerator {

    private final Map<Integer, AisTargetEntry> targetsMap;
    private final Map<Integer, IPastTrack> pastTrackMap;

    public KmlGenerator(Map<Integer, AisTargetEntry> targetsMap, Map<Integer, IPastTrack> pastTrackMap) {
        this.targetsMap = targetsMap;
        this.pastTrackMap = pastTrackMap;
    }

    public String generate() {
        StringBuilder str = new StringBuilder();
        str.append("<kml>\n<Document>");
        str.append(generateCamera());
        str.append("</Document></kml>");
        return str.toString();
    }

    private String generateCamera() {
        return "<Camera><longitude>-35</longitude><latitude>70</latitude><altitude>4200000</altitude><heading>0</heading></Camera>";
    }

}
