package dk.dma.ais.analysis.coverage.calculator.geotools;

public class SphereProjection {
	 
    private double radius;           //Radius of the sphere.
    private double lon0;  //Origin of projection in decimal degrees
    private double lat0;  //Origin of projection in decimal degrees
   
    public SphereProjection()
    {
       lon0=0.0;
       lat0=0.0;
       radius=6356752.3; //Radius of the sphere.
    }
   
    public SphereProjection(double r)
    {
       radius=r; //Radius of the sphere.
    }
   
    public SphereProjection(double longitude0,double latitude0) {
               lon0=longitude0;lat0=latitude0;
        radius=6356752.3; //Earth radius in m
    }
   
    public SphereProjection(double longitude0, double latitude0, double r)
    {
       lon0=longitude0;lat0=latitude0;
       radius=r; //Radius of the sphere.
    }
   
    public void setCentralPoint(double x0, double y0)
    {
        lon0=x0;
        lat0=y0;
    }
   
    public void setRadius(double r)
    {
        radius=r;
    }
   
    public double getRadius() {
               return radius;
    }
   
    public double getLon0() {
               return lon0;
    }
   
    public double getLat0() {
               return lat0;
    }
   

   
    //calculates the horizontal distance from lon0 to lon
    public double lon2x(double lon, double lat)
    {
        double deg2Rad=180.0/Math.PI;
        double lon_rad = lon / deg2Rad;
        double lat_rad = lat / deg2Rad;
        double lon0_rad = lon0 / deg2Rad;
        double lat0_rad = lat0 / deg2Rad;

        double x=0.0;
        double denom = (1.0 + Math.sin(lat0_rad) * Math.sin(lat_rad) + Math.cos(lat0_rad) * Math.cos(lat_rad) * Math.cos(lon_rad - lon0_rad));

        if (denom != 0.0) {
            x = ((2.0*radius) / denom) * Math.cos(lat_rad) * Math.sin(lon_rad - lon0_rad);
        }

        return x;
    }
   
   
    public double lat2y(double lon, double lat)
    {
        double deg2Rad=180.0/Math.PI;
        double lon_rad = lon / deg2Rad;
        double lat_rad = lat / deg2Rad;
        double lon0_rad = lon0 / deg2Rad;
        double lat0_rad = lat0 / deg2Rad;

        double y=0.0;
        double denom = (1.0 + Math.sin(lat0_rad) * Math.sin(lat_rad) + Math.cos(lat0_rad) * Math.cos(lat_rad) * Math.cos(lon_rad - lon0_rad));

        if (denom != 0.0) {
            y = ((2.0*radius) / denom) * (Math.cos(lat0_rad) * Math.sin(lat_rad) - Math.sin(lat0_rad) * Math.cos(lat_rad) * Math.cos(lon_rad - lon0_rad));
        }

        return y;
    }

    public double x2Lon(double x, double y)
    {
        double deg2Rad=180.0/Math.PI;
        double lon0_rad = lon0 / deg2Rad;
        double lat0_rad = lat0 / deg2Rad;

        double ro = Math.sqrt(x*x + y*y);
        double c = 2.0 * Math.atan(ro / (2 * radius));
        double denom = (ro * Math.cos(lat0_rad) * Math.cos(c) - y * Math.sin(lat0_rad) * Math.sin(c));

        double lon=lon0;
        if (denom != 0.0) {
            lon = lon0_rad + Math.atan((x * Math.sin(c)) / denom);
            lon *= deg2Rad;
        }

        return lon;
    }

    public double y2Lat(double x, double y)
    {
        double deg2Rad=180.0/Math.PI;
        double lat0_rad = lat0 / deg2Rad;

        double lat=lat0;
        double ro = Math.sqrt(x*x + y*y);
        if (ro!=0.0)
        {
            double c = 2.0 * Math.atan(ro / (2.0 * radius));
            lat = Math.asin(Math.cos(c) * Math.sin(lat0_rad) + (y * Math.sin(c) * Math.cos(lat0_rad)) / ro);
            lat *= deg2Rad;
        }
        return lat;
    }


    public double distBetweenPoints(double lon1, double lat1, double lon2, double lat2)
    {
        double lo0=lon0;
        double la0=lat0;
        lon0=lon1;
        lat0=lat1;
        double x1=lon2x(lon1,lat1);
        double y1=lat2y(lon1,lat1);
        double x2=lon2x(lon2,lat2);
        double y2=lat2y(lon2,lat2);
        double d=Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
        lon0=lo0;
        lat0=la0;
        return d;
    }
   
    public Object clone() {
               SphereProjection p;
               try
               {
                          p=(SphereProjection) super.clone();
               }
               catch(CloneNotSupportedException e)
               {
                          return null;
               }
               return p;
    }
   
}