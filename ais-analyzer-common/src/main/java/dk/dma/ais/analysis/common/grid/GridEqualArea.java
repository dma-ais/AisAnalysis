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
package dk.dma.ais.analysis.common.grid;

import dk.dma.enav.model.geometry.Position;

//Each latitude strip is stored with the parameters below
class LatitudeStrip {
    int nColumns; // Number of columns in the strip
    double latmin; // The lower latitude of the strip
    double cellHeightInDeg; // The height of the latitude strip
    double cellWidthInDeg; // the width of the cells in the strip
}

public class GridEqualArea {
    private double earthRadius = 6371228; // [m]
    private double poleLatitude = 89.8; // The poles are defined in a single cell

    public double cellHeightInMeter; // The height and width of the cell
    public double latmin; // Minimum latitude for the grid
    public double latmax; // Maximum latitude for the grid
    public double lonmin; // Maximum latitude for the grid
    public double lonmax; // Maximum longitude for the grid
    public int numberOfParallelStrips; //
    public int numberOfCells; // Total number of cells in the grid
    public LatitudeStrip[] parallelStrips; // List with each parallel strip

    // Must be called before the grid can be used
    public void initGrid(double lonmin_, double latmin_, double lonmax_, double latmax_, double cellHeightInMeter_) {
        double lat;
        double cellHeightInDeg;

        cellHeightInMeter = cellHeightInMeter_;
        lonmin = lonmin_;
        lonmax = lonmax_;
        latmin = latmin_;
        latmax = latmax_;

        calcNumberOfParallelStrips();
        parallelStrips = new LatitudeStrip[numberOfParallelStrips];

        numberOfCells = 0;
        lat = latmin;
        if (lat < -poleLatitude) { // South pole cap
            lat = -poleLatitude;
            LatitudeStrip strip = new LatitudeStrip();
            strip.nColumns = 1;
            strip.latmin = latmin;
            strip.cellHeightInDeg = -poleLatitude - latmin;
            strip.cellWidthInDeg = -1;
            parallelStrips[0] = strip;
            numberOfCells = 1;
        }

        long j = 0;
        if (latmax > poleLatitude)
            j = 1; // Make room for the polecap
        int i = 0;
        for (i = numberOfCells; i <= numberOfParallelStrips - 1 - j; i++) {
            cellHeightInDeg = cellHeightInMeter * LatitudeDeg2m(lat);
            LatitudeStrip strip = new LatitudeStrip();
            strip.nColumns = calcNumberOfColumns(lat);
            strip.latmin = lat;
            strip.cellHeightInDeg = cellHeightInDeg;
            parallelStrips[i] = strip;

            double d = CalcCircumference(lat) * ((lonmax - lonmin) / 360.0);
            double cellWidthInMeter_locale = d / parallelStrips[i].nColumns; // Attempts to create a hole number of cells around a
                                                                             // latitude strip
            parallelStrips[i].cellWidthInDeg = cellWidthInMeter_locale * LongitudeDeg2m(lat);
            lat = lat + cellHeightInDeg;
            numberOfCells = numberOfCells + parallelStrips[i].nColumns;
        }

        // Create the pole cap
        if (latmax > poleLatitude) {
            LatitudeStrip strip = new LatitudeStrip();
            strip.nColumns = 1;
            strip.latmin = poleLatitude;
            strip.cellHeightInDeg = latmax - poleLatitude;
            strip.cellWidthInDeg = -1.0;
            parallelStrips[i] = strip;
            numberOfCells = numberOfCells + 1;
        }
    }

    // Get how many longitude degrees at a given latitude 1 meter is.
    public double LongitudeDeg2m(double lat) {
        double d = 0.000005164 * lat * lat * lat * lat + 0.0001753 * Math.abs(lat * lat * lat) - 0.287705412 * lat * lat
                + 0.101570737 * Math.abs(lat) + 1854.974604345;
        return 1.0 / (d * 60.0);
    }

    // Get how many latitude degrees at a given latitude 1 meter is.
    public double LatitudeDeg2m(double lat) {
        double d = -0.00005743 * Math.abs(lat * lat * lat) + 0.00777424 * lat * lat - 0.02882651 * Math.abs(lat) + 1842.98959689;
        return 1.0 / (d * 60.0);
    }

    // Calculates how many rows of cells there are between maximum latitude and minimum latitude
    private int calcNumberOfParallelStrips() {
        double lat;

        numberOfParallelStrips = 0;
        lat = latmin;
        if (lat < -poleLatitude) {
            lat = -poleLatitude;
            numberOfParallelStrips = 1;
        }
        if (lat > poleLatitude)
            lat = poleLatitude;

        while (lat < latmax && lat < poleLatitude) {
            double cellHeightInDeg = cellHeightInMeter * LatitudeDeg2m(lat);
            lat = lat + cellHeightInDeg;
            numberOfParallelStrips = numberOfParallelStrips + 1;
        }

        if (latmax > poleLatitude)
            numberOfParallelStrips = numberOfParallelStrips + 1;
        return numberOfParallelStrips;
    }

    // Calculates the number of cells in a latitude strip at a given latitude
    public int calcNumberOfColumns(double lat) {
        double lat0;

        if ((lat < -poleLatitude) || (lat >= poleLatitude))
            return 1;

        lat0 = calcLat0(lat); // Calculates the lower border of the cell where lat is located
        double d = CalcCircumference(lat0) * ((lonmax - lonmin) / 360.0);

        if (d > cellHeightInMeter) {
            if (lonmax - lonmin == 360.0) {
                return (int) Math.floor(d / cellHeightInMeter) + 1;
            } else {
                return (int) Math.ceil(d / cellHeightInMeter);
            }
        } else
            return 1;
    }

    // Calculates the earth radius at a given latitude
    public double CalcCircumference(double lat) {
        // radius1 = majorAxis * (1 - flattening * Sin(lat / 180# * Pi) ^ 2 - 3 / 8 * flattening ^ 2 * Sin(2 * lat / 180# * Pi) ^ 2)
        // CalcCircumference = 2# * Pi * radius1
        return 2.0 * Math.PI * earthRadius * Math.cos(lat / 180.0 * Math.PI);
    }

    // Calculates the lower latitude of the cell that contains the latitude lat
    public double calcLat0(double lat) {
        double lat0;

        if (lat >= -poleLatitude)
            lat0 = latmin;
        else
            lat0 = -poleLatitude;

        for (int i = 0; i <= numberOfParallelStrips - 1; i++) {
            double latPrevious = lat0;
            double cellHeightInDeg = cellHeightInMeter * LatitudeDeg2m(lat0);
            lat0 = lat0 + cellHeightInDeg;
            if (lat0 > lat) {
                lat0 = latPrevious;
                return lat0; // Break for loop
            }
        }
        return lat0;
    }

    // Calculates the cell id of a position
    // Returns -1 if it cannot be calculated
    public int getCellId(double lon, double lat) {
        if (lon < lonmin || lon > lonmax || lat < latmin || lat > latmax) {
            return -1;
        }

        int id = 0;
        if (lat < -poleLatitude)
            return id;
        if (lat > poleLatitude)
            return numberOfCells - 1;

        int Row = 0;
        if (parallelStrips[0].nColumns == 1)
            Row = 1;
        for (int i = 0; i <= numberOfParallelStrips - 1; i++) {
            if ((parallelStrips[i].latmin + parallelStrips[i].cellHeightInDeg) < lat) {
                id = id + parallelStrips[i].nColumns;
                if (i > 0)
                    id = id + 1;
                Row = i + 1;
            } else
                i = numberOfParallelStrips; // Break the for loop

        }

        int nColumns = (int) Math.floor((lon - lonmin) / (lonmax - lonmin) * parallelStrips[Row].nColumns);
        id = id + nColumns;

        return id;
    }

    // Calculates the lat,lon of a cell with id
    // return not null if all went well
    public Position getGeoPosOfCellId(int cellId, double lon, double lat) {
        if (cellId < 0 || cellId > numberOfCells) {
            lon = 181.0;
            lat = 91.0;
            return null;
        }

        int id = 0;
        if (cellId == 0) {
            lat = latmin;
            lon = lonmin;
            return Position.create(lat, lon);
        }

        if (cellId == numberOfCells - 1) {
            lat = latmax;
            lon = latmin;
            return Position.create(lat, lon);
        }

        id = cellId;
        int i = 0;
        while (id > parallelStrips[0].nColumns - 1) {
            id -= parallelStrips[0].nColumns;
            i++;
        }
        if (id < 0)
            id = id + parallelStrips[i - 1].nColumns;
        lat = parallelStrips[i].latmin;
        lon = lonmin + parallelStrips[i].cellWidthInDeg * id;
        return Position.create(lat, lon);
    }
}
