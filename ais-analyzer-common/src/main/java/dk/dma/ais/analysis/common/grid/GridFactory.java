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
package dk.dma.ais.analysis.common.grid;

public final class GridFactory {

    private static final double DEFAULT_SIZE = 0.0045;
    private static GridFactory factory;

    private GridFactory() {
    }

    public static synchronized GridFactory getInstance() {
        if (factory == null) {
            factory = new GridFactory();
        }

        return factory;
    }

    public Grid getDefaultGrid() {
        return getGrid(DEFAULT_SIZE);
    }

    public Grid getGrid(double size) {
        return new Grid(size);
    }
}
