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
package dk.dma.ais.analysis.viewer.rest.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import dk.dma.ais.analysis.common.web.QueryParams;
import dk.dma.ais.data.AisClassATarget;
import dk.dma.ais.data.AisTargetSourceData;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.enav.model.Country;

public class VesselListFilter {

    private static final String[] filterNames = { "vesselClass", "country", "sourceType", "sourceCountry", "sourceRegion",
            "sourceBs", "sourceSystem", "staticReport" };

    private Map<String, HashSet<String>> filterMap = new HashMap<String, HashSet<String>>();

    public VesselListFilter(QueryParams request) {
        for (String filterName : filterNames) {
            if (request.containsKey(filterName)) {
                HashSet<String> values = new HashSet<String>();
                String[] arr = StringUtils.split(request.getFirst(filterName), ",");
                for (String value : arr) {
                    values.add(value);
                }
                if (values.size() > 0) {
                    filterMap.put(filterName, values);
                }
            }
        }
    }

    public boolean rejectedByFilter(AisVesselTarget target) {
        AisTargetSourceData data = target.getSourceData();
        Set<String> vesselClass = filterMap.get("vesselClass");
        if (vesselClass != null) {
            String vc = (target instanceof AisClassATarget) ? "A" : "B";
            if (!vesselClass.contains(vc)) {
                return true;
            }
        }
        Set<String> country = filterMap.get("country");
        if (country != null) {
            Country mc = target.getCountry();
            if (mc == null)
                return true;
            if (!country.contains(mc.getThreeLetter())) {
                return true;
            }
        }
        Set<String> sourceType = filterMap.get("sourceType");
        if (sourceType != null) {
            if (!sourceType.contains(data.getSourceType())) {
                return true;
            }
        }
        Set<String> sourceCountry = filterMap.get("sourceCountry");
        if (sourceCountry != null) {
            Country mc = data.getTagging().getSourceCountry();
            if (mc == null)
                return true;
            if (!sourceCountry.contains(mc.getThreeLetter())) {
                return true;
            }
        }
        Set<String> sourceRegion = filterMap.get("sourceRegion");
        if (sourceRegion != null) {
            if (data.getSourceRegion() == null)
                return true;
            if (!sourceRegion.contains(data.getSourceRegion())) {
                return true;
            }
        }
        Set<String> sourceBs = filterMap.get("sourceBs");
        if (sourceBs != null) {
            if (data.getTagging().getSourceBs() == null)
                return true;
            if (!sourceBs.contains(Long.toString(data.getTagging().getSourceBs()))) {
                return true;
            }
        }
        Set<String> sourceSystem = filterMap.get("sourceSystem");
        if (sourceSystem != null) {
            if (data.getTagging().getSourceId() == null)
                return true;
            if (!sourceSystem.contains(data.getTagging().getSourceId())) {
                return true;
            }
        }
        Set<String> staticReport = filterMap.get("staticReport");
        if (staticReport != null) {
            boolean hasStatic = (target.getVesselStatic() != null);
            if (staticReport.contains("yes") && !hasStatic) {
                return true;
            }
            if (staticReport.contains("no") && hasStatic) {
                return true;
            }
        }

        return false;
    }

}
