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
package dk.dma.ais.analysis.common.web;

import java.util.Objects;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Helper class that wraps a javax.ws.rs.core.MultivaluedMap
 */
public class QueryParams {
    
    private final MultivaluedMap<String, String> queryParams;
    
    public QueryParams(MultivaluedMap<String, String> queryParams) {
        Objects.requireNonNull(queryParams);
        this.queryParams = queryParams;
    }
    
    public String getFirst(String key) {
        return queryParams.getFirst(key);
    }
    
    public boolean containsKey(String key) {
        return queryParams.containsKey(key);
    }
    
    public Integer getInt(String key) {
        String valStr = queryParams.getFirst(key);
        if (valStr == null) {
            return null;
        }
        try { 
            return Integer.parseInt(valStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getDouble(String key) {
        String valStr = queryParams.getFirst(key);
        if (valStr == null) {
            return null;
        }
        try { 
            return Double.parseDouble(valStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
