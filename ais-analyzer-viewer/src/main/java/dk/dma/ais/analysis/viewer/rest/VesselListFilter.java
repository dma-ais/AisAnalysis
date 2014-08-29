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
