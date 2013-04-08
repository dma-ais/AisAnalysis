/**
 * Cluster object
 * from, to, count, vessels
 * @param from
 *			Top left LonLat
 * @param to
 *			Top right LonLat
 * @param count
 *			The number of vessels in the cluster
 * @param locations
 *			A list of vessel locations in the cluster.
 *			This list is empty if count is large.
 * @returns vessel object
 */
function Cluster(from, to, count, density, locations) {
	this.from = from;
	this.to = to;
	this.count = count;
	this.density = density;
	this.locations = locations;
}
