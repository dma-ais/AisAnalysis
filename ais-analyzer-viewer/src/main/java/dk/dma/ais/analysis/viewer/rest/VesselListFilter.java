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
package dk.dma.ais.analysis.viewer.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import dk.dma.ais.analysis.common.web.QueryParams;

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

    public Map<String, HashSet<String>> getFilterMap() {
        return filterMap;
    }

}
