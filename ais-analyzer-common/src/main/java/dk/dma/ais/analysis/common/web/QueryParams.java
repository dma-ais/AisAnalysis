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
