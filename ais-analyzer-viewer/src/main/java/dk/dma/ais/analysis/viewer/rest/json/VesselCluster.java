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

import static java.util.Objects.requireNonNull;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.enav.model.geometry.Position;

/**
 * A cluster of vessels described as an area with a number of known vessel positions. A cluster knows its density in
 * vessels per kilometer.
 */
public class VesselCluster {

    private Position from;
    private Position to;
    private int count;
    private double density;
    private BaseVesselList vessels;

    /**
     * Constructor of Vessel Cluster.
     * 
     * @param from
     *            Top left corner of area.
     * @param to
     *            Bottom right corner of area.
     * @param count
     *            The number of vessels in the area.
     * @param locations
     *            The knoe locations in the area.
     */
    public VesselCluster(Position from, Position to, int count, BaseVesselList vessels) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
        this.count = count;
        this.vessels = vessels;
    }

    public Position getFrom() {
        return from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return to;
    }

    public void setTo(Position to) {
        this.to = to;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BaseVesselList getVessels() {
        return vessels;
    }

    public void setVessels(BaseVesselList vessels) {
        this.vessels = vessels;
    }

    public void incrementCount() {
        count++;
    }

    public void addVessel(AisVesselTarget target, int anonId) {
        vessels.addTarget(target, anonId);
    }

    /**
     * Gets the density in vessels per kilometer.
     */
    public double getDensity() {
        return density;
    }

    /**
     * Sets the density in vessels per kilometer.
     */
    public void setDensity(double density) {
        this.density = density;
    }

}
